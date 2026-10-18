package app.harmonicaplayalong

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.harmonicaplayalong.ui.FreePlayScreen
import app.harmonicaplayalong.ui.HarmonicaTheme
import app.harmonicaplayalong.ui.LibraryScreen
import app.harmonicaplayalong.ui.PlayScreen
import app.harmonicaplayalong.ui.SettingsScreen
import app.harmonicaplayalong.ui.isDark

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as HarmonicaApp
        setContent {
            val settings by app.prefs.settings.collectAsStateWithLifecycle()
            val dark = isDark(settings.theme)
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = if (dark) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                    navigationBarStyle = if (dark) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                )
                onDispose {}
            }
            HarmonicaTheme(dark) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppContent(app)
                }
            }
        }
    }
}

private enum class Tab(val label: String) { LIBRARY("Songs"), FREE("Free play"), SETTINGS("Settings") }

@Composable
private fun AppContent(app: HarmonicaApp) {
    val settings by app.prefs.settings.collectAsStateWithLifecycle()
    val progress by app.prefs.progress.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(Tab.LIBRARY) }
    var playing by rememberSaveable { mutableStateOf<String?>(null) }

    val songId = playing
    if (songId != null) {
        BackHandler { playing = null }
        PlayScreen(songId = songId, onBack = { playing = null })
        return
    }
    BackHandler(enabled = tab != Tab.LIBRARY) { tab = Tab.LIBRARY }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                for (t in Tab.entries) {
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = {
                            Icon(
                                when (t) {
                                    Tab.LIBRARY -> Icons.Rounded.LibraryMusic
                                    Tab.FREE -> Icons.Rounded.GraphicEq
                                    Tab.SETTINGS -> Icons.Rounded.Settings
                                },
                                contentDescription = null,
                            )
                        },
                        label = { Text(t.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                Tab.LIBRARY -> LibraryScreen(app.prefs, settings, progress, onOpen = { playing = it })
                Tab.FREE -> FreePlayScreen()
                Tab.SETTINGS -> SettingsScreen(app.prefs, settings)
            }
        }
    }
}
