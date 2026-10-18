package app.harmonicaplayalong

import android.app.Application
import app.harmonicaplayalong.audio.AudioEngine
import app.harmonicaplayalong.data.AppPrefs

class HarmonicaApp : Application() {
    lateinit var prefs: AppPrefs
        private set

    val engine: AudioEngine by lazy { AudioEngine() }

    override fun onCreate() {
        super.onCreate()
        prefs = AppPrefs(this)
    }
}
