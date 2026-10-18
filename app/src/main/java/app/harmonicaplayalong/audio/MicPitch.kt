package app.harmonicaplayalong.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Process
import androidx.core.content.ContextCompat
import app.harmonicaplayalong.music.Pitch

/** One microphone analysis. [midi] is NaN when nothing clear was heard. */
data class PitchReading(
    val midi: Double,
    val rms: Double,
    val clarity: Double,
    /** System.nanoTime() of the middle of the analysed audio. */
    val nanoTime: Long,
) {
    val voiced: Boolean get() = !midi.isNaN()
}

/** Listens to the microphone and reports the pitch about 43 times a second on a background thread. */
class MicPitch(private val context: Context, private val onReading: (PitchReading) -> Unit) {
    companion object {
        private const val RATE = 44100
        private const val WINDOW = 2048
        private const val HOP = 1024
    }

    @Volatile private var running = false
    private var thread: Thread? = null

    /** 0 (needs a loud note) to 1 (hears very quiet notes). */
    @Volatile var sensitivity: Float = 0.6f

    val isRunning: Boolean get() = running

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun start(): Boolean {
        if (running) return true
        if (!hasPermission()) return false
        val minBuf = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minBuf <= 0) return false
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val unprocessed = am.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED) == "true"
        val source = if (unprocessed) MediaRecorder.AudioSource.UNPROCESSED else MediaRecorder.AudioSource.VOICE_RECOGNITION
        val record = try {
            AudioRecord(source, RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuf, WINDOW * 4))
        } catch (_: Exception) {
            return false
        }
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            return false
        }
        running = true
        val t = Thread({ loop(record) }, "harmonica-mic")
        thread = t
        t.start()
        return true
    }

    fun stop() {
        running = false
        thread?.join(300)
        thread = null
    }

    private fun loop(record: AudioRecord) {
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
        val yin = Yin(RATE, WINDOW)
        val shorts = ShortArray(HOP)
        val window = FloatArray(WINDOW)
        try {
            record.startRecording()
            while (running) {
                var got = 0
                while (got < HOP && running) {
                    val n = record.read(shorts, got, HOP - got)
                    if (n <= 0) break
                    got += n
                }
                if (got < HOP) break
                val now = System.nanoTime()
                System.arraycopy(window, HOP, window, 0, WINDOW - HOP)
                for (i in 0 until HOP) window[WINDOW - HOP + i] = shorts[i] / 32768f
                yin.silence = 0.002 + (1.0 - sensitivity) * 0.03
                val r = yin.detect(window)
                // the middle of the window, plus a typical input latency
                val centre = now - (WINDOW / 2) * 1_000_000_000L / RATE - 25_000_000L
                val midi = if (r.voiced && r.clarity > 0.6) Pitch.midi(r.frequency) else Double.NaN
                onReading(PitchReading(midi, r.rms, r.clarity, centre))
            }
        } catch (_: Exception) {
        } finally {
            try {
                record.stop()
            } catch (_: Exception) {
            }
            record.release()
            running = false
        }
    }
}
