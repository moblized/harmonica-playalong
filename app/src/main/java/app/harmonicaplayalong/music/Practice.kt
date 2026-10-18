package app.harmonicaplayalong.music

import kotlin.math.abs

/** Pitch distance in semitones, optionally ignoring which octave was played. */
fun pitchError(detected: Double, target: Int, anyOctave: Boolean): Double {
    var d = detected - target
    if (anyOctave) {
        d %= 12.0
        if (d > 6) d -= 12.0
        if (d < -6) d += 12.0
    }
    return d
}

/**
 * Scores a play-along run: a note counts as hit when the microphone heard the right pitch
 * while the note was due (with a little slack either side).
 */
class PerformanceTracker(private val notes: List<ArrangedNote>, private val harpBase: Int) {
    private val heard = IntArray(notes.size)
    private val hit = BooleanArray(notes.size)

    var toleranceSemitones = 0.5
    var anyOctave = false

    /** Bumped whenever a note turns into a hit, so the UI knows to redraw. */
    var version = 0
        private set

    fun reset() {
        heard.fill(0)
        hit.fill(false)
        version++
    }

    fun isHit(i: Int): Boolean = hit[i]

    val hits: Int get() = hit.count { it }
    val total: Int get() = notes.size

    fun percent(from: Int = 0, to: Int = notes.size): Int {
        val n = to - from
        if (n <= 0) return 0
        var h = 0
        for (i in from until to) if (hit[i]) h++
        return (100.0 * h / n).toInt()
    }

    /** [beat] is where in the song the sound was made, [midi] the detected pitch (NaN for silence). */
    fun onPitch(beat: Double, midi: Double, bpm: Double) {
        if (midi.isNaN() || notes.isEmpty()) return
        val slack = 0.12 * bpm / 60.0
        // last note starting before beat + slack
        var lo = 0
        var hi = notes.size - 1
        var idx = -1
        while (lo <= hi) {
            val mid = (lo + hi) / 2
            if (notes[mid].note.start <= beat + slack) {
                idx = mid
                lo = mid + 1
            } else {
                hi = mid - 1
            }
        }
        var i = idx
        while (i >= 0 && i >= idx - 3) {
            val n = notes[i]
            if (n.note.end + slack * 0.5 >= beat && !hit[i]) {
                val target = harpBase + n.semitone
                if (abs(pitchError(midi, target, anyOctave)) <= toleranceSemitones) {
                    heard[i]++
                    val seconds = n.note.duration * 60.0 / bpm
                    val need = if (seconds < 0.2) 1 else 2
                    if (heard[i] >= need) {
                        hit[i] = true
                        version++
                    }
                }
            }
            i--
        }
    }
}

/**
 * "Wait for me" practice: the song only moves on when you play the next note.
 * A repeated note has to be re-attacked (a short dip in volume or pitch) before it counts again.
 */
class WaitTracker(private val notes: List<ArrangedNote>, private val harpBase: Int) {
    var index = 0
        private set
    var toleranceSemitones = 0.5
    var anyOctave = false

    private var matched = 0
    private var needReattack = false
    private var peak = 0.0

    val done: Boolean get() = index >= notes.size

    fun reset(to: Int = 0) {
        index = to.coerceIn(0, notes.size)
        matched = 0
        needReattack = false
        peak = 0.0
    }

    /** Returns true when this reading moved on to the next note. */
    fun onPitch(midi: Double, rms: Double): Boolean {
        if (done) return false
        val target = harpBase + notes[index].semitone
        val ok = !midi.isNaN() && abs(pitchError(midi, target, anyOctave)) <= toleranceSemitones
        if (needReattack) {
            if (!ok || rms < peak * 0.5) needReattack = false
            return false
        }
        if (ok) {
            matched++
            if (rms > peak) peak = rms
        } else {
            matched = 0
        }
        if (matched >= 3) {
            index++
            matched = 0
            needReattack = !done && notes[index].semitone == notes[index - 1].semitone
            if (!needReattack) peak = 0.0
            return true
        }
        return false
    }
}
