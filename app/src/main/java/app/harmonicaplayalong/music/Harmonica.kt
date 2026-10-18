package app.harmonicaplayalong.music

enum class Breath { BLOW, DRAW }

enum class Technique { NATURAL, BEND, OVERBLOW }

/** How tabs are written on screen. */
enum class TabStyle(val label: String, val example: String) {
    MINUS("Draw is minus", "4  -4  -3'"),
    PLUS_MINUS("Plus and minus", "+4  -4  -3'"),
    ARROWS("Arrows", "↑4  ↓4  ↓3'"),
}

/**
 * One way of playing a note on a 10-hole diatonic harmonica.
 * [bend] counts semitones bent down. [overblow] covers overblows (holes 1-6) and overdraws (7-10).
 */
data class HarpNote(val hole: Int, val breath: Breath, val bend: Int = 0, val overblow: Boolean = false) {
    val technique: Technique
        get() = when {
            overblow -> Technique.OVERBLOW
            bend > 0 -> Technique.BEND
            else -> Technique.NATURAL
        }

    /** Semitones above hole 1 blow. */
    val semitone: Int
        get() {
            val i = hole - 1
            val blow = Richter.BLOW[i]
            val draw = Richter.DRAW[i]
            return when {
                overblow && breath == Breath.BLOW -> draw + 1
                overblow -> blow + 1
                breath == Breath.BLOW -> blow - bend
                else -> draw - bend
            }
        }

    fun label(style: TabStyle): String {
        val marks = "'".repeat(bend) + if (overblow) "o" else ""
        return when (style) {
            TabStyle.MINUS -> (if (breath == Breath.DRAW) "-" else "") + hole + marks
            TabStyle.PLUS_MINUS -> (if (breath == Breath.DRAW) "-" else "+") + hole + marks
            TabStyle.ARROWS -> (if (breath == Breath.DRAW) "↓" else "↑") + hole + marks
        }
    }

    /** Plain-language description, e.g. "Draw hole 3, bent a whole step". */
    fun describe(): String {
        val base = (if (breath == Breath.BLOW) "Blow" else "Draw") + " hole $hole"
        return when {
            overblow && breath == Breath.BLOW -> "Overblow hole $hole"
            overblow -> "Overdraw hole $hole"
            bend == 1 -> "$base, bent a half step"
            bend == 2 -> "$base, bent a whole step"
            bend == 3 -> "$base, bent a step and a half"
            else -> base
        }
    }
}

/** Standard Richter tuning, in semitones above hole 1 blow (C4 on a C harmonica). */
object Richter {
    val BLOW = intArrayOf(0, 4, 7, 12, 16, 19, 24, 28, 31, 36)
    val DRAW = intArrayOf(2, 7, 11, 14, 17, 21, 23, 26, 29, 33)

    /** Every playable note: natural notes, draw bends (holes 1-6), blow bends (7-10), overblows and overdraws. */
    val all: List<HarpNote> = buildList {
        for (hole in 1..10) {
            val i = hole - 1
            add(HarpNote(hole, Breath.BLOW))
            add(HarpNote(hole, Breath.DRAW))
            val gap = DRAW[i] - BLOW[i]
            if (gap > 0) {
                for (b in 1 until gap) add(HarpNote(hole, Breath.DRAW, bend = b))
                add(HarpNote(hole, Breath.BLOW, overblow = true))
            } else if (gap < 0) {
                for (b in 1 until -gap) add(HarpNote(hole, Breath.BLOW, bend = b))
                add(HarpNote(hole, Breath.DRAW, overblow = true))
            }
        }
    }

    private val bySemitone: Map<Int, List<HarpNote>> = all.groupBy { it.semitone }

    val lowest: Int = all.minOf { it.semitone }
    val highest: Int = all.maxOf { it.semitone }

    fun options(semitone: Int): List<HarpNote> = bySemitone[semitone].orEmpty()

    /** The easiest way to play a semitone, or null when it is out of range. */
    fun easiest(semitone: Int): HarpNote? = options(semitone).minByOrNull { Arranger.techniqueCost(it) }

    /** How far a draw (holes 1-6) or blow (holes 8-10) can be bent, in semitones. */
    fun bendDepth(hole: Int, breath: Breath): Int {
        val i = hole - 1
        val gap = DRAW[i] - BLOW[i]
        return when {
            breath == Breath.DRAW && gap > 1 -> gap - 1
            breath == Breath.BLOW && gap < -1 -> -gap - 1
            else -> 0
        }
    }
}

/** Harmonica keys, with the pitch of hole 1 blow relative to a C harmonica (C4). */
enum class HarpKey(val label: String, val offset: Int) {
    G("G", -5),
    AB("Ab", -4),
    A("A", -3),
    BB("Bb", -2),
    B("B", -1),
    C("C", 0),
    DB("Db", 1),
    D("D", 2),
    EB("Eb", 3),
    E("E", 4),
    F("F", 5),
    FS("F#", 6);

    /** MIDI note of hole 1 blow. */
    val baseMidi: Int get() = 60 + offset

    companion object {
        fun fromName(name: String?): HarpKey = entries.firstOrNull { it.name == name } ?: C
    }
}

/** Harmonica "positions": the key you play in relative to the harp's key, counted round the circle of fifths. */
object Positions {
    fun position(tonicOnCHarp: Int): Int {
        val pc = Math.floorMod(tonicOnCHarp, 12)
        for (n in 0 until 12) if (Math.floorMod(7 * n, 12) == pc) return n + 1
        return 1
    }

    fun ordinal(n: Int): String = when {
        n % 100 in 11..13 -> "${n}th"
        n % 10 == 1 -> "${n}st"
        n % 10 == 2 -> "${n}nd"
        n % 10 == 3 -> "${n}rd"
        else -> "${n}th"
    }
}
