package app.harmonicaplayalong.music

import kotlin.math.abs
import kotlin.math.max

enum class Difficulty(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
}

/** A melody note placed on the harmonica. [semitone] is relative to hole 1 blow; [harp] is null when out of range. */
data class ArrangedNote(val note: MelodyNote, val semitone: Int, val harp: HarpNote?)

/** A song transposed by [shift] semitones and laid out as harmonica tab (for a C harmonica; tabs are the same in every key). */
data class Arrangement(
    val song: Song,
    val shift: Int,
    val notes: List<ArrangedNote>,
    val cost: Double,
    val auto: Boolean,
) {
    val score: Score get() = song.score
    val bends: Int = notes.count { it.harp?.technique == Technique.BEND }
    val overblows: Int = notes.count { it.harp?.technique == Technique.OVERBLOW }
    val unplayable: Int = notes.count { it.harp == null }
    val lowestHole: Int = notes.mapNotNull { it.harp?.hole }.minOrNull() ?: 1
    val highestHole: Int = notes.mapNotNull { it.harp?.hole }.maxOrNull() ?: 10
    val tonicOnCHarp: Int = Math.floorMod(song.score.tonic + shift, 12)
    val position: Int = Positions.position(tonicOnCHarp)

    val maxJump: Int = notes.zipWithNext { a, b ->
        val ha = a.harp?.hole ?: return@zipWithNext 0
        val hb = b.harp?.hole ?: return@zipWithNext 0
        abs(ha - hb)
    }.maxOrNull() ?: 0

    val difficulty: Difficulty = run {
        val quick = notes.count { it.note.duration * 60.0 / song.tempo < 0.22 }
        val deepBends = notes.count { (it.harp?.bend ?: 0) >= 2 }
        when {
            unplayable > 0 || overblows > 0 -> Difficulty.ADVANCED
            deepBends > 2 || bends > max(3, notes.size / 8) -> Difficulty.ADVANCED
            bends > 0 -> Difficulty.INTERMEDIATE
            quick > notes.size / 10 || maxJump > 4 -> Difficulty.INTERMEDIATE
            else -> Difficulty.BEGINNER
        }
    }

    /** MIDI pitch a harmonica in [key] produces for note [i]. */
    fun soundingMidi(i: Int, key: HarpKey): Int = key.baseMidi + notes[i].semitone

    fun usesFlats(key: HarpKey): Boolean = Pitch.keyUsesFlats(tonicOnCHarp + key.offset, score.mode)

    fun playingKeyName(key: HarpKey): String {
        val tonic = tonicOnCHarp + key.offset
        val name = Pitch.className(tonic, Pitch.keyUsesFlats(tonic, score.mode))
        return name + if (score.mode == Mode.MAJOR) " major" else " minor"
    }

    fun positionName(): String = Positions.ordinal(position) + " position"
}

object Arranger {
    private const val UNPLAYABLE = 200.0

    /** How hard one way of playing a note is. Natural notes in holes 4-7 are free. */
    fun techniqueCost(n: HarpNote): Double {
        // Deep bends (a whole step or more) are much harder than a single half-step bend,
        // and harder still in quick passages, so they cost nearly as much as an overblow.
        val technique = when {
            n.overblow -> 16.0
            n.bend > 0 && n.breath == Breath.BLOW -> 4.5 + 3.5 * (n.bend - 1)
            n.bend == 1 -> 3.0
            n.bend == 2 -> 7.0
            n.bend > 2 -> 10.0
            else -> 0.0
        }
        val register = when (n.hole) {
            1 -> 0.35
            2, 3 -> 0.15
            8 -> 0.2
            9 -> 0.3
            10 -> 0.4
            else -> 0.0
        }
        return technique + register
    }

    private fun transition(a: HarpNote?, b: HarpNote?): Double {
        if (a == null || b == null) return 0.0
        val d = abs(a.hole - b.hole)
        return 0.12 * d + if (d > 4) 0.3 else 0.0
    }

    /** Lays the song out with a fixed transposition, choosing the smoothest holes (Viterbi over all ways to play each note). */
    fun arrange(song: Song, shift: Int, auto: Boolean = false): Arrangement {
        val notes = song.score.notes
        val n = notes.size
        val semis = IntArray(n) { notes[it].midi + shift - 60 }
        val options = List(n) { Richter.options(semis[it]) }
        val cost = Array(n) { DoubleArray(max(1, options[it].size)) }
        val back = Array(n) { IntArray(max(1, options[it].size)) }
        for (i in 0 until n) {
            val opts = options[i]
            for (j in cost[i].indices) {
                val own = if (opts.isEmpty()) UNPLAYABLE else techniqueCost(opts[j])
                if (i == 0) {
                    cost[0][j] = own
                    back[0][j] = -1
                    continue
                }
                var best = Double.MAX_VALUE
                var arg = 0
                val prev = options[i - 1]
                for (p in cost[i - 1].indices) {
                    val c = cost[i - 1][p] + transition(prev.getOrNull(p), opts.getOrNull(j))
                    if (c < best) {
                        best = c
                        arg = p
                    }
                }
                cost[i][j] = best + own
                back[i][j] = arg
            }
        }
        var j = cost[n - 1].indices.minByOrNull { cost[n - 1][it] } ?: 0
        val total = cost[n - 1][j]
        val chosen = arrayOfNulls<HarpNote>(n)
        for (i in n - 1 downTo 0) {
            chosen[i] = options[i].getOrNull(j)
            if (i > 0) j = back[i][j]
        }
        val arranged = List(n) { ArrangedNote(notes[it], semis[it], chosen[it]) }
        return Arrangement(song, shift, arranged, total, auto)
    }

    /** Tries every transposition that fits the harmonica and keeps the easiest. */
    fun best(song: Song): Arrangement {
        val midis = song.score.notes.map { it.midi }
        val lo = midis.min()
        val hi = midis.max()
        val minShift = Richter.lowest + 60 - lo
        val maxShift = Richter.highest + 60 - hi
        val range = if (minShift <= maxShift) minShift..maxShift else -24..24
        var best: Arrangement? = null
        var bestScore = Double.MAX_VALUE
        for (shift in range) {
            val a = arrange(song, shift, auto = true)
            val holes = a.notes.mapNotNull { it.harp?.hole }
            val mean = if (holes.isEmpty()) 5.0 else holes.average()
            val score = a.cost + 0.5 * abs(mean - 5.0) + 0.001 * abs(shift)
            if (score < bestScore) {
                bestScore = score
                best = a
            }
        }
        return best!!
    }
}
