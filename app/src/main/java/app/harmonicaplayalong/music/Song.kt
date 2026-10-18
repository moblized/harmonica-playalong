package app.harmonicaplayalong.music

import kotlin.math.abs

enum class Mode { MAJOR, MINOR }

/** How the backing band accompanies a song. */
enum class BackingStyle {
    /** Bass on the strong beats, chord stabs in between. */
    MARCH,
    /** Bass on beat 1, chord on beats 2 and 3. */
    WALTZ,
    /** Root, fifth and octave as quick sixteenths at the start of each bar (Für Elise). */
    ARPEGGIO,
    /** Broken chords in eighth notes. */
    FLOWING,
    /** Broken chords in sixteenth notes, for very slow pieces. */
    FLOWING16,
    /** Triplet broken chords over a held bass (Moonlight Sonata). */
    TRIPLETS,
    /** Soft sustained chords. */
    PAD,
}

/**
 * A song as written in the library.
 *
 * Melody notation, one phrase per line:
 *   `E5q`  pitch + octave + duration,  `F#4e.` dotted,  `Bb4h~` tied to the next note,
 *   `rq`   rest,  `|`  bar line.
 * Durations in quarter-note beats: w=4 h=2 q=1 e=1/2 s=1/4 z=1/8 T=2/3 t=1/3 u=1/6, and '.' adds half.
 *
 * Chords: one group per bar separated by `|`. The chords in a bar share it equally,
 * `.` holds the previous chord for another share and `-` means no chord.
 */
data class Song(
    val id: String,
    val title: String,
    val composer: String,
    val work: String,
    val year: String,
    val about: String,
    val timeSignature: String,
    val tempo: Int,
    val key: String,
    val pickupBeats: Double = 0.0,
    val style: BackingStyle,
    val notation: String,
    val chords: String,
    val source: String,
) {
    val score: Score by lazy { SongParser.parse(this) }
    val isBeethoven: Boolean get() = composer.contains("Beethoven")
}

data class MelodyNote(
    val index: Int,
    val midi: Int,
    val start: Double,
    val duration: Double,
    val bar: Int,
    val phrase: Int,
) {
    val end: Double get() = start + duration
}

enum class ChordQuality(val intervals: IntArray, val suffix: String) {
    MAJOR(intArrayOf(0, 4, 7), ""),
    MINOR(intArrayOf(0, 3, 7), "m"),
    DOM7(intArrayOf(0, 4, 7, 10), "7"),
    MIN7(intArrayOf(0, 3, 7, 10), "m7"),
    DIM(intArrayOf(0, 3, 6), "dim"),
}

data class Chord(val root: Int, val quality: ChordQuality) {
    fun transposed(semitones: Int) = Chord(Math.floorMod(root + semitones, 12), quality)
    fun name(flats: Boolean = false) = Pitch.className(root, flats) + quality.suffix
    val fifth: Int get() = Math.floorMod(root + quality.intervals[2], 12)
    val third: Int get() = Math.floorMod(root + quality.intervals[1], 12)

    companion object {
        fun parse(text: String): Chord {
            val rootLen = if (text.length > 1 && (text[1] == '#' || text[1] == 'b')) 2 else 1
            val root = Pitch.parseClass(text.substring(0, rootLen))
            val quality = when (val q = text.substring(rootLen)) {
                "" -> ChordQuality.MAJOR
                "m" -> ChordQuality.MINOR
                "7" -> ChordQuality.DOM7
                "m7" -> ChordQuality.MIN7
                "dim" -> ChordQuality.DIM
                else -> throw IllegalArgumentException("Unknown chord quality '$q' in '$text'")
            }
            return Chord(root, quality)
        }
    }
}

data class ChordEvent(val start: Double, val duration: Double, val chord: Chord)

data class Score(
    val notes: List<MelodyNote>,
    val chords: List<ChordEvent>,
    /** Length of a full bar in quarter-note beats. */
    val beatsPerBar: Double,
    /** Metronome spacing in quarter-note beats. */
    val clickEvery: Double,
    val pickup: Double,
    val totalBeats: Double,
    /** Start beat of each bar; bar 0 is the pickup bar when there is one. */
    val barStarts: List<Double>,
    val phraseCount: Int,
    val tonic: Int,
    val mode: Mode,
) {
    /** Start beat of each phrase (the first note of each notation line). */
    val phraseStarts: List<Double> by lazy {
        (0 until phraseCount).map { p -> notes.first { it.phrase == p }.start }
    }

    fun phraseRange(phrase: Int): ClosedFloatingPointRange<Double> {
        val start = phraseStarts[phrase]
        val end = if (phrase + 1 < phraseCount) phraseStarts[phrase + 1] else totalBeats
        return start..end
    }

    fun phraseAt(beat: Double): Int {
        var p = 0
        for (i in phraseStarts.indices) if (phraseStarts[i] <= beat + 1e-6) p = i
        return p
    }

    /** Index of the note sounding at (or most recently started before) [beat], or -1 before the first note. */
    fun noteIndexAt(beat: Double): Int {
        var lo = 0
        var hi = notes.size - 1
        var found = -1
        while (lo <= hi) {
            val mid = (lo + hi) / 2
            if (notes[mid].start <= beat + 1e-9) {
                found = mid
                lo = mid + 1
            } else {
                hi = mid - 1
            }
        }
        return found
    }
}

class NotationException(message: String) : IllegalArgumentException(message)

object SongParser {
    private val NOTE = Regex("^([A-G])([#b]?)(-?\\d)([whqeszTtu])(\\.?)(~?)$")
    private val REST = Regex("^r([whqeszTtu])(\\.?)$")
    private const val EPS = 1e-6

    fun duration(code: Char, dotted: Boolean): Double {
        val base = when (code) {
            'w' -> 4.0
            'h' -> 2.0
            'q' -> 1.0
            'e' -> 0.5
            's' -> 0.25
            'z' -> 0.125
            'T' -> 2.0 / 3.0
            't' -> 1.0 / 3.0
            'u' -> 1.0 / 6.0
            else -> throw NotationException("Unknown duration '$code'")
        }
        return if (dotted) base * 1.5 else base
    }

    fun beatsPerBar(timeSignature: String): Double {
        val (num, den) = timeSignature.split("/").map { it.trim().toInt() }
        return num * 4.0 / den
    }

    fun clickEvery(timeSignature: String): Double {
        val (num, den) = timeSignature.split("/").map { it.trim().toInt() }
        return when {
            den == 8 && num % 3 == 0 && num > 3 -> 1.5
            den == 8 -> 0.5
            den == 2 -> 2.0
            else -> 1.0
        }
    }

    fun parseKey(key: String): Pair<Int, Mode> {
        val parts = key.trim().split(" ")
        val tonic = Pitch.parseClass(parts[0])
        val mode = if (parts.getOrNull(1)?.lowercase() == "minor") Mode.MINOR else Mode.MAJOR
        return tonic to mode
    }

    fun parse(song: Song): Score {
        val bpb = beatsPerBar(song.timeSignature)
        val pickup = song.pickupBeats
        val notes = ArrayList<MelodyNote>()
        val barStarts = arrayListOf(0.0)
        var pos = 0.0
        var bar = 0
        var barAccum = 0.0
        var phrase = 0
        var tiePending = false

        fun expectedBarLength(b: Int) = if (b == 0 && pickup > 0) pickup else bpb

        fun closeBar(where: String, last: Boolean = false) {
            val expected = expectedBarLength(bar)
            val wrong = if (last) barAccum - expected > EPS || barAccum < EPS else abs(barAccum - expected) > EPS
            if (wrong) {
                throw NotationException(
                    "${song.id}: bar ${bar + 1} has ${"%.3f".format(barAccum)} beats, expected $expected (near '$where')"
                )
            }
            bar++
            barAccum = 0.0
            barStarts.add(pos)
        }

        val lines = song.notation.lines().map { it.trim() }.filter { it.isNotEmpty() }
        for ((li, line) in lines.withIndex()) {
            var notesInLine = 0
            val tokens = line.split(Regex("\\s+"))
            for ((ti, token) in tokens.withIndex()) {
                if (token == "|") {
                    // The final bar line may close a short last bar (it completes the pickup).
                    val last = li == lines.lastIndex && ti == tokens.lastIndex
                    closeBar(tokens.subList(maxOf(0, ti - 2), ti + 1).joinToString(" "), last)
                    continue
                }
                val rest = REST.matchEntire(token)
                if (rest != null) {
                    if (tiePending) throw NotationException("${song.id}: tie into a rest at '$token'")
                    val d = duration(rest.groupValues[1][0], rest.groupValues[2] == ".")
                    pos += d
                    barAccum += d
                    continue
                }
                val m = NOTE.matchEntire(token) ?: throw NotationException("${song.id}: cannot read '$token'")
                val midi = Pitch.parse(m.groupValues[1] + m.groupValues[2] + m.groupValues[3])
                val d = duration(m.groupValues[4][0], m.groupValues[5] == ".")
                if (tiePending) {
                    val prev = notes.last()
                    if (prev.midi != midi) throw NotationException("${song.id}: tie between different pitches at '$token'")
                    notes[notes.size - 1] = prev.copy(duration = prev.duration + d)
                } else {
                    notes.add(MelodyNote(notes.size, midi, pos, d, bar, phrase))
                    notesInLine++
                }
                tiePending = m.groupValues[6] == "~"
                pos += d
                barAccum += d
            }
            if (notesInLine > 0) phrase++
        }
        if (tiePending) throw NotationException("${song.id}: the song ends with an open tie")
        if (barAccum > EPS) {
            if (barAccum - expectedBarLength(bar) > EPS) {
                throw NotationException("${song.id}: last bar is too long (${barAccum} beats)")
            }
        } else {
            // the last token was a bar line, drop the empty bar start
            barStarts.removeAt(barStarts.size - 1)
        }
        if (notes.isEmpty()) throw NotationException("${song.id}: no notes")

        val (tonic, mode) = parseKey(song.key)
        val chords = parseChords(song, barStarts, pos, bpb, pickup)
        return Score(
            notes = notes,
            chords = chords,
            beatsPerBar = bpb,
            clickEvery = clickEvery(song.timeSignature),
            pickup = pickup,
            totalBeats = pos,
            barStarts = barStarts,
            phraseCount = phrase,
            tonic = tonic,
            mode = mode,
        )
    }

    private fun parseChords(
        song: Song,
        barStarts: List<Double>,
        totalBeats: Double,
        bpb: Double,
        pickup: Double,
    ): List<ChordEvent> {
        val text = song.chords.trim()
        if (text.isEmpty()) return emptyList()
        val bars = text.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        if (bars.size != barStarts.size) {
            throw NotationException("${song.id}: ${bars.size} chord bars for ${barStarts.size} melody bars")
        }
        val events = ArrayList<ChordEvent>()
        for ((b, group) in bars.withIndex()) {
            val start = barStarts[b]
            val end = if (b + 1 < barStarts.size) barStarts[b + 1] else maxOf(totalBeats, start + if (b == 0 && pickup > 0) pickup else bpb)
            val tokens = group.split(Regex("\\s+"))
            val share = (end - start) / tokens.size
            for ((i, tok) in tokens.withIndex()) {
                val t = start + i * share
                when (tok) {
                    "." -> {
                        val last = events.lastOrNull()
                        if (last != null && abs(last.start + last.duration - t) < EPS) {
                            events[events.size - 1] = last.copy(duration = last.duration + share)
                        }
                    }
                    "-" -> Unit
                    else -> events.add(ChordEvent(t, share, Chord.parse(tok)))
                }
            }
        }
        return events
    }
}
