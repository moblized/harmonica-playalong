package app.harmonicaplayalong.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.harmonicaplayalong.BuildConfig
import app.harmonicaplayalong.data.AppPrefs
import app.harmonicaplayalong.data.Settings
import app.harmonicaplayalong.data.ThemeChoice
import app.harmonicaplayalong.music.HarpKey
import app.harmonicaplayalong.music.TabStyle
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(prefs: AppPrefs, settings: Settings, modifier: Modifier = Modifier) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Section("Harmonica key") {
            Text(
                "Pick the key stamped on your harmonica. A C harp is the usual starting point.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (k in HarpKey.entries) {
                    FilterChip(selected = k == settings.harpKey, onClick = { prefs.update { it.copy(harpKey = k) } }, label = { Text(k.label) })
                }
            }
        }

        Section("How tabs are written") {
            for (style in TabStyle.entries) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { prefs.update { it.copy(tabStyle = style) } }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = settings.tabStyle == style, onClick = { prefs.update { it.copy(tabStyle = style) } })
                    Column {
                        Text(style.label, style = MaterialTheme.typography.bodyLarge)
                        Text(style.example, style = TabTextStyle.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Legend()
            SwitchRow("Show note names under the tab", settings.showNoteNames) { v -> prefs.update { it.copy(showNoteNames = v) } }
        }

        Section("Playing") {
            SwitchRow("Count in before the music starts", settings.countIn) { v -> prefs.update { it.copy(countIn = v) } }
            Spacer(Modifier.height(6.dp))
            Text("Screen timing: ${if (settings.syncOffsetMs > 0) "+" else ""}${settings.syncOffsetMs} ms", style = MaterialTheme.typography.bodyLarge)
            Text(
                "If the notes reach the line before you hear them (common with Bluetooth headphones), move this right.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(
                value = settings.syncOffsetMs.toFloat(),
                onValueChange = { v -> prefs.update { it.copy(syncOffsetMs = (v / 10f).roundToInt() * 10) } },
                valueRange = -200f..400f,
            )
        }

        Section("Microphone") {
            Text("Sensitivity", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Lower it in a noisy room; raise it if quiet notes are missed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Slider(
                value = settings.micSensitivity,
                onValueChange = { v -> prefs.update { it.copy(micSensitivity = v) } },
                valueRange = 0f..1f,
            )
            SwitchRow("Count a note in any octave as correct", settings.anyOctave) { v -> prefs.update { it.copy(anyOctave = v) } }
        }

        Section("Look") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (t in ThemeChoice.entries) {
                    FilterChip(selected = settings.theme == t, onClick = { prefs.update { it.copy(theme = t) } }, label = { Text(t.label) })
                }
            }
        }

        Section("About") {
            Text(
                "Harmonica Play-Along ${BuildConfig.VERSION_NAME}. Melodies come from public-domain scores; " +
                    "notes and harmonies were checked against the Mutopia Project's free editions (mutopiaproject.org). " +
                    "The app picks the key that needs the fewest bends on a standard Richter-tuned diatonic harmonica.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun Legend() {
    val p = LocalHarpPalette.current
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(vertical = 6.dp)) {
        Text("● blow", color = p.blow, style = MaterialTheme.typography.labelLarge)
        Text("● draw", color = p.draw, style = MaterialTheme.typography.labelLarge)
        Text("◌ bend  ' = half step", color = p.bend, style = MaterialTheme.typography.labelLarge)
    }
}
