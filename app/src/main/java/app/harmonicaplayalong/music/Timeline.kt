package app.harmonicaplayalong.music

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/** What a note belongs to, for muting. */
enum class Channel { MELODY, BASS, CHORD, CLICK, COUNT_IN }

/** Which sound plays it. */
enum class Instrument { HARMONICA, PIANO, BASS, PAD, CLICK }

data class SeqEvent(
    val start: Double,
    val duration: Double,
    val midi: Int,
    val channel: Channel,
    val instrument: Instrument,
    val velocity: Float = 1f,
    val noteIndex: Int = -1,
    val accent: Boolean = false,
)

/** Everything the sequencer plays for one arrangement, in quarter-note beats from the start of the song. */
data class Timeline(
    val events: List<SeqEvent>,
    val endBeat: Double,
    val beatsPerBar: Double,
    val clickEvery: Double,
    val pickup: Double,
) {
    /** Downbeat of the bar containing [beat]. Bar lines fall at pickup + k * beatsPerBar. */
    fun barStartAtOrBefore(beat: Double): Double {
        val k = floor((beat - pickup) / beatsPerBar + 1e-9)
        return pickup + k * beatsPerBar
    }

    fun isDownbeat(beat: Double): Boolean {
        val pos = (beat - pickup) / beatsPerBar
        return abs(pos - pos.roundToInt()) < 1e-6
    }
}

object TimelineBuilder {
    private const val BASS_LOW = 43   // G2
    private const val CHORD_LOW = 55  // G3

    fun build(arrangement: Arrangement, key: HarpKey): Timeline {
        val score = arrangement.score
        val events = ArrayList<SeqEvent>()
        for ((i, n) in arrangement.notes.withIndex()) {
            events += SeqEvent(
                start = n.note.start,
                duration = n.note.duration,
                midi = key.baseMidi + n.semitone,
                channel = Channel.MELODY,
                instrument = Instrument.HARMONICA,
                noteIndex = i,
            )
        }
        val transpose = arrangement.shift + key.offset
        for (ce in score.chords) {
            backing(events, ce.start, ce.duration, ce.chord.transposed(transpose), score, arrangement.song.style)
        }
        // Metronome on the bar grid, from the first beat of the song to the end.
        val origin = score.pickup
        var k = floor(-origin / score.clickEvery).toInt()
        while (true) {
            val t = origin + k * score.clickEvery
            k++
            if (t < -1e-9) continue
            if (t >= score.totalBeats - 1e-9) break
            val pos = (t - origin) / score.beatsPerBar
            val accent = abs(pos - pos.roundToInt()) < 1e-6
            events += SeqEvent(t, 0.05, 0, Channel.CLICK, Instrument.CLICK, if (accent) 1f else 0.7f, accent = accent)
        }
        events.sortWith(compareBy<SeqEvent> { it.start }.thenBy { it.channel.ordinal })
        return Timeline(events, score.totalBeats, score.beatsPerBar, score.clickEvery, score.pickup)
    }

    private fun lowestAtOrAbove(pc: Int, low: Int): Int {
        var m = low
        while (Math.floorMod(m, 12) != Math.floorMod(pc, 12)) m++
        return m
    }

    private fun chordTones(c: Chord): List<Int> {
        val root = lowestAtOrAbove(c.root, CHORD_LOW)
        return c.quality.intervals.map { root + it }
    }

    private fun beatInBar(t: Double, score: Score): Double {
        val r = (t - score.pickup) % score.beatsPerBar
        val v = if (r < 0) r + score.beatsPerBar else r
        return if (abs(v - score.beatsPerBar) < 1e-6) 0.0 else v
    }

    private fun backing(out: MutableList<SeqEvent>, start: Double, duration: Double, chord: Chord, score: Score, style: BackingStyle) {
        val end = start + duration
        val bass = lowestAtOrAbove(chord.root, BASS_LOW)
        val fifth = bass + (chord.quality.intervals[2])
        val third = bass + chord.quality.intervals[1]
        val tones = chordTones(chord)

        fun bassNote(t: Double, d: Double, midi: Int, v: Float = 0.9f) {
            out += SeqEvent(t, d, midi, Channel.BASS, Instrument.BASS, v)
        }
        fun stab(t: Double, d: Double, v: Float = 0.7f) {
            for (m in tones) out += SeqEvent(t, d, m, Channel.CHORD, Instrument.PIANO, v)
        }
        fun pluck(t: Double, d: Double, midi: Int, v: Float = 0.75f) {
            out += SeqEvent(t, d, midi, Channel.CHORD, Instrument.PIANO, v)
        }
        fun steps(step: Double, action: (t: Double, k: Int) -> Unit) {
            var t = start
            var k = 0
            while (t < end - 1e-9) {
                action(t, k)
                t += step
                k++
            }
        }

        when (style) {
            BackingStyle.MARCH -> {
                val step = if (score.beatsPerBar % 1.5 == 0.0 && score.clickEvery == 1.5) 1.5 else 1.0
                steps(step) { t, _ ->
                    val beat = (beatInBar(t, score) / step).roundToInt()
                    if (beat % 2 == 0) {
                        bassNote(t, step * 0.9, if (beat % 4 == 2) fifth else bass)
                    } else {
                        stab(t, step * 0.5)
                    }
                }
            }
            BackingStyle.WALTZ -> {
                // one-two-three on the beats of the bar: quarters in 3/4, eighths in 3/8
                val step = if (score.beatsPerBar < 2.0) score.beatsPerBar / 3.0 else 1.0
                steps(step) { t, _ ->
                    if (beatInBar(t, score) < 1e-6) bassNote(t, step * 0.95, bass) else stab(t, step * 0.8, 0.6f)
                }
            }
            BackingStyle.ARPEGGIO -> steps(score.beatsPerBar) { t, _ ->
                val notes = listOf(bass, fifth, bass + 12)
                for ((i, m) in notes.withIndex()) {
                    out += SeqEvent(t + i * 0.25, 1.0, m, Channel.BASS, Instrument.BASS, 0.8f)
                }
            }
            BackingStyle.FLOWING, BackingStyle.FLOWING16 -> {
                val step = if (style == BackingStyle.FLOWING) 0.5 else 0.25
                val pattern = listOf(bass, fifth, third + 12, fifth)
                steps(step) { t, k ->
                    val m = pattern[k % pattern.size]
                    if (k % pattern.size == 0) bassNote(t, step * 3, m, 0.8f) else pluck(t, step * 2.5, m, 0.6f)
                }
            }
            BackingStyle.TRIPLETS -> {
                bassNote(start, duration, bass, 0.7f)
                val top = tones[0]
                val pattern = listOf(top - 12 + chord.quality.intervals[2], top, top + chord.quality.intervals[1])
                steps(1.0 / 3.0) { t, k -> pluck(t, 0.6, pattern[k % 3], 0.55f) }
            }
            BackingStyle.PAD -> {
                bassNote(start, duration, bass, 0.6f)
                for (m in tones) out += SeqEvent(start, duration, m, Channel.CHORD, Instrument.PAD, 0.8f)
            }
        }
    }
}
