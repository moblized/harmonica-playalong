package app.harmonicaplayalong.music

import kotlin.math.log2
import kotlin.math.pow

/** MIDI note helpers. Middle C (C4) is MIDI 60. */
object Pitch {
    private val SHARPS = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val FLATS = arrayOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    fun className(pitchClass: Int, flats: Boolean = false): String =
        (if (flats) FLATS else SHARPS)[Math.floorMod(pitchClass, 12)]

    fun name(midi: Int, flats: Boolean = false): String =
        className(midi, flats) + (Math.floorDiv(midi, 12) - 1)

    fun frequency(midi: Double): Double = 440.0 * 2.0.pow((midi - 69.0) / 12.0)

    fun midi(frequency: Double): Double = 69.0 + 12.0 * log2(frequency / 440.0)

    /** Parses a pitch class such as "C", "F#" or "Bb". */
    fun parseClass(text: String): Int {
        require(text.isNotEmpty()) { "Empty pitch name" }
        val base = when (text[0]) {
            'C' -> 0
            'D' -> 2
            'E' -> 4
            'F' -> 5
            'G' -> 7
            'A' -> 9
            'B' -> 11
            else -> throw IllegalArgumentException("Bad pitch name '$text'")
        }
        var pc = base
        for (c in text.substring(1)) {
            pc += when (c) {
                '#' -> 1
                'b' -> -1
                else -> throw IllegalArgumentException("Bad pitch name '$text'")
            }
        }
        return Math.floorMod(pc, 12)
    }

    /** Parses a full pitch such as "C#4" or "Eb5" into a MIDI number. */
    fun parse(text: String): Int {
        val octaveStart = text.indexOfFirst { it.isDigit() || it == '-' }
        require(octaveStart > 0) { "Bad pitch '$text'" }
        val name = text.substring(0, octaveStart)
        val octave = text.substring(octaveStart).toInt()
        val base = parseClass(name.substring(0, 1))
        var accidental = 0
        for (c in name.substring(1)) {
            accidental += when (c) {
                '#' -> 1
                'b' -> -1
                else -> throw IllegalArgumentException("Bad pitch '$text'")
            }
        }
        return (octave + 1) * 12 + base + accidental
    }

    /** Whether a key is usually written with flats (used for note names on screen). */
    fun keyUsesFlats(tonic: Int, mode: Mode): Boolean {
        val majorTonic = if (mode == Mode.MAJOR) Math.floorMod(tonic, 12) else Math.floorMod(tonic + 3, 12)
        return majorTonic in setOf(5, 10, 3, 8, 1)
    }
}
