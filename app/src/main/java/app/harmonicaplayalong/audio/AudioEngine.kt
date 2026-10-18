package app.harmonicaplayalong.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTimestamp
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.os.Process
import app.harmonicaplayalong.music.Channel
import app.harmonicaplayalong.music.Instrument
import app.harmonicaplayalong.music.SeqEvent
import app.harmonicaplayalong.music.Timeline

/** Mix settings that can change while playing. */
data class Mix(val melody: Boolean, val backing: Boolean, val click: Boolean)

/**
 * Plays timelines through an AudioTrack and reports the beat that is being heard right now,
 * using the track's timestamps so the screen stays in step with the sound.
 */
class AudioEngine {
    companion object {
        const val SAMPLE_RATE = 44100
        private const val BLOCK = 256
    }

    private val main = Handler(Looper.getMainLooper())
    private var session: Session? = null
    private var preview: Session? = null

    val isPlaying: Boolean get() = session?.isAlive == true

    init {
        Thread { Instruments.warmUp() }.start()
    }

    /**
     * Starts playing [timeline] from [fromBeat] with [countInBars] bars of clicks first.
     * [onFinished] runs on the main thread when the end is reached (not when [stop] is called).
     */
    fun play(
        timeline: Timeline,
        fromBeat: Double,
        countInBars: Int,
        bpm: Double,
        mix: Mix,
        loop: ClosedFloatingPointRange<Double>?,
        onFinished: () -> Unit,
    ) {
        stop()
        val seq = Sequencer(timeline, SAMPLE_RATE, fromBeat, countInBars)
        seq.bpm = bpm
        applyMix(seq, mix)
        if (loop != null) seq.setLoop(loop.start, loop.endInclusive)
        val s = Session(seq) { main.post { onFinished() } }
        session = s
        s.start()
    }

    /** Stops playback and returns the beat that was being heard, or null if nothing was playing. */
    fun stop(): Double? {
        val s = session ?: return null
        session = null
        val beat = s.beatAt(System.nanoTime())
        s.shutdown()
        return beat
    }

    /** The beat being heard right now, or null when stopped. */
    fun beatNow(nanoTime: Long = System.nanoTime()): Double? = session?.beatAt(nanoTime)

    /** Beat being heard at an earlier moment, for lining up microphone readings. */
    fun beatAt(nanoTime: Long): Double? = session?.beatAt(nanoTime)

    /** Where the music (after the count-in) starts in the current session. */
    val playStart: Double? get() = session?.sequencer?.playStart

    fun setBpm(bpm: Double) {
        session?.sequencer?.bpm = bpm
    }

    fun setMix(mix: Mix) {
        session?.sequencer?.let { applyMix(it, mix) }
    }

    fun setLoop(loop: ClosedFloatingPointRange<Double>?) {
        val seq = session?.sequencer ?: return
        if (loop == null) seq.clearLoop() else seq.setLoop(loop.start, loop.endInclusive)
    }

    /** Plays a single note, for "hear it" buttons and tapping holes. */
    fun preview(midi: Int, seconds: Double = 0.7) {
        preview?.shutdown()
        val ev = SeqEvent(0.0, seconds, midi, Channel.MELODY, Instrument.HARMONICA)
        val tl = Timeline(listOf(ev), seconds, 4.0, 1.0, 0.0)
        val seq = Sequencer(tl, SAMPLE_RATE, 0.0, 0)
        seq.bpm = 60.0
        val s = Session(seq) {}
        preview = s
        s.start()
    }

    fun release() {
        stop()
        preview?.shutdown()
        preview = null
    }

    private fun applyMix(seq: Sequencer, mix: Mix) {
        seq.melodyOn = mix.melody
        seq.backingOn = mix.backing
        seq.clickOn = mix.click
    }

    private class Session(val sequencer: Sequencer, private val onEnd: () -> Unit) : Thread("harmonica-audio") {
        @Volatile private var running = true
        private val track: AudioTrack
        private val lock = Any()
        private val cpFrame = LongArray(512)
        private val cpBeat = DoubleArray(512)
        private val cpRate = DoubleArray(512)
        private var cpCount = 0
        @Volatile private var framesWritten = 0L
        private val timestamp = AudioTimestamp()

        init {
            val minBuf = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(maxOf(minBuf, BLOCK * 4 * 8))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build()
        }

        override fun run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            val buf = FloatArray(BLOCK)
            var started = false
            var ended = false
            try {
                while (running) {
                    val base = framesWritten
                    sequencer.render(buf, BLOCK) { offset, beat, rate -> checkpoint(base + offset, beat, rate) }
                    val n = track.write(buf, 0, BLOCK, AudioTrack.WRITE_BLOCKING)
                    if (n < 0) break
                    framesWritten += n
                    if (!started) {
                        track.play()
                        started = true
                    }
                    if (sequencer.finished) {
                        ended = true
                        break
                    }
                }
                if (ended && running) {
                    // let the tail of the sound play out before reporting the end
                    val target = framesWritten
                    var waited = 0
                    while (running && track.playbackHeadPosition.toLong() < target && waited < 2000) {
                        Thread.sleep(10)
                        waited += 10
                    }
                }
            } catch (_: InterruptedException) {
            } catch (_: IllegalStateException) {
            } finally {
                try {
                    track.pause()
                    track.flush()
                } catch (_: IllegalStateException) {
                }
                track.release()
            }
            if (ended && running) onEnd()
        }

        private fun checkpoint(frame: Long, beat: Double, rate: Double) {
            synchronized(lock) {
                val i = cpCount % cpFrame.size
                cpFrame[i] = frame
                cpBeat[i] = beat
                cpRate[i] = rate
                cpCount++
            }
        }

        /** The beat at the moment the listener hears the frame being played at [nanoTime]. */
        fun beatAt(nanoTime: Long): Double {
            var played: Double
            try {
                played = if (track.getTimestamp(timestamp)) {
                    timestamp.framePosition + (nanoTime - timestamp.nanoTime) * SAMPLE_RATE / 1e9
                } else {
                    track.playbackHeadPosition.toDouble()
                }
            } catch (_: IllegalStateException) {
                played = framesWritten.toDouble()
            }
            played = played.coerceIn(0.0, framesWritten.toDouble())
            synchronized(lock) {
                if (cpCount == 0) return sequencer.beat
                val newest = cpCount - 1
                val oldest = maxOf(0, cpCount - cpFrame.size)
                var k = newest
                while (k > oldest && cpFrame[k % cpFrame.size] > played) k--
                val i = k % cpFrame.size
                return cpBeat[i] + (played - cpFrame[i]) * cpRate[i]
            }
        }

        fun shutdown() {
            running = false
            interrupt()
            try {
                join(400)
            } catch (_: InterruptedException) {
            }
        }
    }
}
