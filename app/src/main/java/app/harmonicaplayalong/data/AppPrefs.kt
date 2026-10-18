package app.harmonicaplayalong.data

import android.content.Context
import app.harmonicaplayalong.music.HarpKey
import app.harmonicaplayalong.music.TabStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeChoice(val label: String) { SYSTEM("Follow phone"), LIGHT("Light"), DARK("Dark") }

data class Settings(
    val harpKey: HarpKey = HarpKey.C,
    val tabStyle: TabStyle = TabStyle.MINUS,
    val showNoteNames: Boolean = true,
    val theme: ThemeChoice = ThemeChoice.SYSTEM,
    val countIn: Boolean = true,
    /** Moves the falling notes earlier (+) or later (-) to match your device's audio delay. */
    val syncOffsetMs: Int = 0,
    val micSensitivity: Float = 0.6f,
    val anyOctave: Boolean = false,
    val melody: Boolean = true,
    val backing: Boolean = true,
    val click: Boolean = false,
)

data class SongProgress(
    val best: Int = -1,
    val favorite: Boolean = false,
    val plays: Int = 0,
    /** User's own transposition, or null to use the automatic best fit. */
    val shift: Int? = null,
)

/** Settings and per-song progress, kept in SharedPreferences. */
class AppPrefs(context: Context) {
    private val sp = context.getSharedPreferences("harmonica", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    private val _progress = MutableStateFlow<Map<String, SongProgress>>(readProgress())
    val progress: StateFlow<Map<String, SongProgress>> = _progress.asStateFlow()

    fun update(change: (Settings) -> Settings) {
        val s = change(_settings.value)
        _settings.value = s
        sp.edit()
            .putString("harpKey", s.harpKey.name)
            .putString("tabStyle", s.tabStyle.name)
            .putBoolean("noteNames", s.showNoteNames)
            .putString("theme", s.theme.name)
            .putBoolean("countIn", s.countIn)
            .putInt("syncOffset", s.syncOffsetMs)
            .putFloat("micSensitivity", s.micSensitivity)
            .putBoolean("anyOctave", s.anyOctave)
            .putBoolean("melody", s.melody)
            .putBoolean("backing", s.backing)
            .putBoolean("click", s.click)
            .apply()
    }

    fun progressFor(id: String): SongProgress = _progress.value[id] ?: SongProgress()

    fun updateProgress(id: String, change: (SongProgress) -> SongProgress) {
        val p = change(progressFor(id))
        _progress.value = _progress.value + (id to p)
        sp.edit()
            .putInt("best_$id", p.best)
            .putBoolean("fav_$id", p.favorite)
            .putInt("plays_$id", p.plays)
            .putInt("shift_$id", p.shift ?: NO_SHIFT)
            .putBoolean("known_$id", true)
            .apply()
    }

    private fun readSettings(): Settings {
        val d = Settings()
        return Settings(
            harpKey = HarpKey.fromName(sp.getString("harpKey", null)),
            tabStyle = TabStyle.entries.firstOrNull { it.name == sp.getString("tabStyle", null) } ?: d.tabStyle,
            showNoteNames = sp.getBoolean("noteNames", d.showNoteNames),
            theme = ThemeChoice.entries.firstOrNull { it.name == sp.getString("theme", null) } ?: d.theme,
            countIn = sp.getBoolean("countIn", d.countIn),
            syncOffsetMs = sp.getInt("syncOffset", d.syncOffsetMs),
            micSensitivity = sp.getFloat("micSensitivity", d.micSensitivity),
            anyOctave = sp.getBoolean("anyOctave", d.anyOctave),
            melody = sp.getBoolean("melody", d.melody),
            backing = sp.getBoolean("backing", d.backing),
            click = sp.getBoolean("click", d.click),
        )
    }

    private fun readProgress(): Map<String, SongProgress> {
        val out = HashMap<String, SongProgress>()
        for (key in sp.all.keys) {
            if (!key.startsWith("known_")) continue
            val id = key.removePrefix("known_")
            val shift = sp.getInt("shift_$id", NO_SHIFT)
            out[id] = SongProgress(
                best = sp.getInt("best_$id", -1),
                favorite = sp.getBoolean("fav_$id", false),
                plays = sp.getInt("plays_$id", 0),
                shift = if (shift == NO_SHIFT) null else shift,
            )
        }
        return out
    }

    private companion object {
        const val NO_SHIFT = Int.MIN_VALUE
    }
}
