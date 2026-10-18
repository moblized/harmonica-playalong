package app.harmonicaplayalong.audio

import app.harmonicaplayalong.music.Channel
import app.harmonicaplayalong.music.Instrument
import app.harmonicaplayalong.music.Pitch
import app.harmonicaplayalong.music.SeqEvent
import app.harmonicaplayalong.music.Timeline
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/** Band-limited single-cycle waveforms, one per harmonic budget, so high notes never alias. */
class Wavetable private constructor(private val tables: Array<FloatArray>) {
    fun forFrequency(freq: Double, sampleRate: Int): FloatArray {
        val limit = sampleRate * 0.45
        var best = tables[0]
        for (i in LEVELS.indices) if (freq * LEVELS[i] < limit) best = tables[i]
        return best
    }

    companion object {
        const val SIZE = 2048
        private val LEVELS = intArrayOf(1, 2, 3, 4, 6, 8, 12, 16, 24, 32)

        fun build(amplitude: (Int) -> Double): Wavetable {
            val tables = Array(LEVELS.size) { li ->
                val acc = DoubleArray(SIZE)
                for (h in 1..LEVELS[li]) {
                    val a = amplitude(h)
                    if (a == 0.0) continue
                    for (i in 0 until SIZE) acc[i] += a * sin(2.0 * PI * h * i / SIZE)
                }
                var peak = 1e-9
                for (v in acc) peak = max(peak, abs(v))
                val t = FloatArray(SIZE + 1)
                for (i in 0 until SIZE) t[i] = (acc[i] / peak).toFloat()
                t[SIZE] = t[0]
                t
            }
            return Wavetable(tables)
        }
    }
}

class InstrumentSpec(
    val table: Wavetable,
    val attack: Double,
    /** Time constant of the decay towards [sustain], in seconds. */
    val decay: Double,
    val sustain: Float,
    val release: Double,
    val gain: Float,
    val vibrato: Boolean,
    /** Share of the written length the note is held for. */
    val gateFraction: Double,
)

object Instruments {
    private val specs: Map<Instrument, InstrumentSpec> by lazy {
        mapOf(
            // Reedy and bright: strong low harmonics, a slow roll-off and a small dip at the 4th like a real reed.
            Instrument.HARMONICA to InstrumentSpec(
                Wavetable.build { h -> if (h == 4) 0.35 else 1.0 / h.toDouble().pow(0.75) },
                attack = 0.025, decay = 0.25, sustain = 0.85f, release = 0.07, gain = 0.30f,
                vibrato = true, gateFraction = 0.92,
            ),
            Instrument.PIANO to InstrumentSpec(
                Wavetable.build { h -> 1.0 / (h * h).toDouble() + if (h == 2) 0.25 else 0.0 },
                attack = 0.004, decay = 0.45, sustain = 0f, release = 0.12, gain = 0.14f,
                vibrato = false, gateFraction = 1.0,
            ),
            // Enough upper harmonics that the bass line is still heard on a phone speaker.
            Instrument.BASS to InstrumentSpec(
                Wavetable.build { h -> if (h > 6) 0.0 else 1.0 / h },
                attack = 0.006, decay = 0.6, sustain = 0f, release = 0.1, gain = 0.26f,
                vibrato = false, gateFraction = 1.0,
            ),
            Instrument.PAD to InstrumentSpec(
                Wavetable.build { h -> if (h > 4) 0.0 else 1.0 / (h * h * h).toDouble() },
                attack = 0.25, decay = 1.2, sustain = 0.7f, release = 0.45, gain = 0.09f,
                vibrato = false, gateFraction = 1.0,
            ),
            Instrument.CLICK to InstrumentSpec(
                Wavetable.build { h -> if (h == 1) 1.0 else 0.0 },
                attack = 0.0005, decay = 0.012, sustain = 0f, release = 0.01, gain = 0.32f,
                vibrato = false, gateFraction = 1.0,
            ),
        )
    }

    fun spec(i: Instrument): InstrumentSpec = specs.getValue(i)

    /** Builds the wavetables up front so the first note does not stutter. */
    fun warmUp() {
        specs.size
    }
}

internal class Voice {
    var active = false
        private set
    var channel = Channel.MELODY
        private set
    var serial = 0L
        private set

    private var table = FloatArray(Wavetable.SIZE + 1)
    private var phase = 0.0
    private var inc = 0.0
    private var env = 0f
    private var stage = 0
    private var attackStep = 0f
    private var decayMul = 1f
    private var sustain = 0f
    private var releaseMul = 0f
    private var gate = 0
    private var gain = 0f
    private var vibrato = false
    private var vibPhase = 0.0
    private var vibInc = 0.0
    private var vibDelay = 0
    private var age = 0

    fun start(spec: InstrumentSpec, freq: Double, sampleRate: Int, gateSamples: Int, velocity: Float, channel: Channel, serial: Long) {
        table = spec.table.forFrequency(freq, sampleRate)
        phase = 0.0
        inc = freq * Wavetable.SIZE / sampleRate
        env = 0f
        stage = 0
        attackStep = (1.0 / max(1.0, spec.attack * sampleRate)).toFloat()
        decayMul = exp(-1.0 / (spec.decay * sampleRate)).toFloat()
        sustain = spec.sustain
        releaseMul = exp(-5.0 / (spec.release * sampleRate)).toFloat()
        gate = max(1, gateSamples)
        gain = spec.gain * velocity
        vibrato = spec.vibrato
        vibPhase = 0.0
        vibInc = 2.0 * PI * 5.2 / sampleRate
        vibDelay = (0.22 * sampleRate).toInt()
        age = 0
        this.channel = channel
        this.serial = serial
        active = true
    }

    fun release() {
        if (active && stage < 2) stage = 2
    }

    fun kill() {
        active = false
    }

    fun render(out: FloatArray, from: Int, to: Int) {
        if (!active) return
        val t = table
        var i = from
        while (i < to) {
            when (stage) {
                0 -> {
                    env += attackStep
                    if (env >= 1f) {
                        env = 1f
                        stage = 1
                    }
                }
                1 -> {
                    env = sustain + (env - sustain) * decayMul
                    if (sustain == 0f && env < 0.0005f) {
                        active = false
                        return
                    }
                }
                else -> {
                    env *= releaseMul
                    if (env < 0.0003f) {
                        active = false
                        return
                    }
                }
            }
            if (stage < 2) {
                gate--
                if (gate <= 0) stage = 2
            }
            val idx = phase.toInt()
            val frac = (phase - idx).toFloat()
            val s = t[idx] + (t[idx + 1] - t[idx]) * frac
            out[i] += s * env * gain
            var step = inc
            if (vibrato) {
                age++
                if (age > vibDelay) {
                    vibPhase += vibInc
                    step *= 1.0 + 0.0035 * sin(vibPhase)
                }
            }
            phase += step
            if (phase >= Wavetable.SIZE) phase -= Wavetable.SIZE
            i++
        }
    }
}

/**
 * Renders a [Timeline] to audio. Tempo, mutes and the loop can change while it plays.
 * Positions are in quarter-note beats; negative beats are the count-in.
 */
class Sequencer(
    timeline: Timeline,
    private val sampleRate: Int,
    playFrom: Double,
    countInBars: Int,
) {
    @Volatile var bpm: Double = 100.0
    @Volatile var melodyOn: Boolean = true
    @Volatile var backingOn: Boolean = true
    @Volatile var clickOn: Boolean = false
    @Volatile private var loop: DoubleArray? = null

    private val events: List<SeqEvent>
    private var next = 0
    private val voices = Array(48) { Voice() }
    private var serial = 0L
    private val endBeat = timeline.endBeat

    /** Where the music (not the count-in) starts. */
    val playStart: Double = playFrom

    var beat: Double
        private set

    val finished: Boolean get() = loop == null && beat >= endBeat + 0.8

    init {
        val countIn = ArrayList<SeqEvent>()
        var startBeat = playFrom
        if (countInBars > 0) {
            val countStart = timeline.barStartAtOrBefore(playFrom) - countInBars * timeline.beatsPerBar
            var t = countStart
            while (t < playFrom - 1e-9) {
                val accent = timeline.isDownbeat(t)
                countIn += SeqEvent(t, 0.05, 0, Channel.COUNT_IN, Instrument.CLICK, if (accent) 1f else 0.7f, accent = accent)
                t += timeline.clickEvery
            }
            startBeat = countStart
        }
        events = (countIn + timeline.events.filter { it.start >= playFrom - 1e-9 }).sortedBy { it.start }
        beat = startBeat
    }

    fun setLoop(start: Double, end: Double) {
        loop = doubleArrayOf(start, end)
    }

    fun clearLoop() {
        loop = null
    }

    private fun firstEventAtOrAfter(b: Double): Int {
        var lo = 0
        var hi = events.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (events[mid].start < b - 1e-9) lo = mid + 1 else hi = mid
        }
        return lo
    }

    private fun audible(c: Channel): Boolean = when (c) {
        Channel.MELODY -> melodyOn
        Channel.BASS, Channel.CHORD -> backingOn
        Channel.CLICK -> clickOn
        Channel.COUNT_IN -> true
    }

    private fun trigger(e: SeqEvent, beatsPerFrame: Double) {
        if (!audible(e.channel)) return
        if (e.channel == Channel.MELODY) {
            for (v in voices) if (v.active && v.channel == Channel.MELODY) v.release()
        }
        val spec = Instruments.spec(e.instrument)
        val freq = if (e.instrument == Instrument.CLICK) {
            if (e.accent) 2000.0 else 1400.0
        } else {
            Pitch.frequency(e.midi.toDouble())
        }
        val gate = (e.duration * spec.gateFraction / beatsPerFrame).toInt()
        var voice = voices.firstOrNull { !it.active }
        if (voice == null) voice = voices.minByOrNull { it.serial }!!
        voice.start(spec, freq, sampleRate, gate, e.velocity, e.channel, serial++)
    }

    private fun applyMutes() {
        for (v in voices) {
            if (v.active && !audible(v.channel)) v.release()
        }
    }

    /** Stops every sound right away, for example before a jump. */
    fun releaseAll() {
        for (v in voices) v.release()
    }

    /**
     * Fills [out] with [frames] samples. [checkpoint] is told the beat at the start of the block and after any loop jump,
     * as (frame offset, beat, beats per frame), so the caller can map played frames back to beats.
     */
    fun render(out: FloatArray, frames: Int, checkpoint: (Int, Double, Double) -> Unit) {
        java.util.Arrays.fill(out, 0, frames, 0f)
        val beatsPerFrame = bpm / 60.0 / sampleRate
        applyMutes()
        checkpoint(0, beat, beatsPerFrame)
        var i = 0
        while (i < frames) {
            while (next < events.size && events[next].start <= beat + 1e-9) {
                trigger(events[next], beatsPerFrame)
                next++
            }
            var n = frames - i
            if (next < events.size) {
                n = min(n, max(1, ceil((events[next].start - beat) / beatsPerFrame).toInt()))
            }
            val l = loop
            val looping = l != null && beat < l[1]
            if (looping) n = min(n, max(1, ceil((l!![1] - beat) / beatsPerFrame).toInt()))
            for (v in voices) if (v.active) v.render(out, i, i + n)
            beat += n * beatsPerFrame
            i += n
            if (looping && beat >= l!![1] - 1e-9) {
                beat = l[0]
                releaseAll()
                next = firstEventAtOrAfter(l[0])
                checkpoint(i, beat, beatsPerFrame)
            }
        }
        for (j in 0 until frames) out[j] = softClip(out[j])
    }

    private fun softClip(x: Float): Float {
        val c = if (x > 3f) 3f else if (x < -3f) -3f else x
        val x2 = c * c
        return c * (27f + x2) / (27f + 9f * x2)
    }
}
