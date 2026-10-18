package app.harmonicaplayalong.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.harmonicaplayalong.data.AppPrefs
import app.harmonicaplayalong.data.Settings
import app.harmonicaplayalong.data.SongProgress
import app.harmonicaplayalong.music.Arranger
import app.harmonicaplayalong.music.Arrangement as SongArrangement
import app.harmonicaplayalong.music.HarpKey
import app.harmonicaplayalong.music.SongLibrary

private enum class Filter(val label: String) {
    ALL("All"),
    BEETHOVEN("Beethoven"),
    OTHERS("Other composers"),
    EASY("No bends"),
    FAVORITES("Favourites"),
}

/** Best-fit arrangements, worked out once. */
private val arrangements: Map<String, SongArrangement> by lazy {
    SongLibrary.songs.associate { it.id to Arranger.best(it) }
}

private fun arrangementOf(id: String, progress: SongProgress?): SongArrangement {
    val auto = arrangements.getValue(id)
    val shift = progress?.shift ?: return auto
    return if (shift == auto.shift) auto else Arranger.arrange(auto.song, shift)
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    prefs: AppPrefs,
    settings: Settings,
    progress: Map<String, SongProgress>,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filter by rememberSaveable { mutableStateOf(Filter.ALL) }
    var pickKey by remember { mutableStateOf(false) }

    val songs = SongLibrary.songs.filter { s ->
        when (filter) {
            Filter.ALL -> true
            Filter.BEETHOVEN -> s.isBeethoven
            Filter.OTHERS -> !s.isBeethoven
            Filter.EASY -> arrangementOf(s.id, progress[s.id]).let { it.bends == 0 && it.overblows == 0 }
            Filter.FAVORITES -> progress[s.id]?.favorite == true
        }
    }

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                Modifier
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp),
            ) {
                Text(
                    "HARMONICA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 3.sp,
                )
                Text("Play-Along", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Beethoven and other classics for the 10-hole diatonic harmonica. " +
                        "Follow the falling notes, play with the band, or let the app wait for you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                AssistChip(
                    onClick = { pickKey = true },
                    label = { Text("My harmonica: ${settings.harpKey.label}") },
                )
            }
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(Filter.entries.toList()) { f ->
                    FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f.label) })
                }
            }
        }
        if (songs.isEmpty()) {
            item {
                Text(
                    if (filter == Filter.FAVORITES) "Tap the heart on a song to keep it here." else "Nothing here yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                )
            }
        }
        items(songs, key = { it.id }) { song ->
            val p = progress[song.id]
            SongCard(
                arrangement = arrangementOf(song.id, p),
                settings = settings,
                progress = p,
                onOpen = { onOpen(song.id) },
                onFavorite = { prefs.updateProgress(song.id) { it.copy(favorite = !it.favorite) } },
            )
        }
    }

    if (pickKey) {
        KeyPickerDialog(
            current = settings.harpKey,
            onPick = { k -> prefs.update { it.copy(harpKey = k) }; pickKey = false },
            onDismiss = { pickKey = false },
        )
    }
}

@Composable
private fun SongCard(
    arrangement: SongArrangement,
    settings: Settings,
    progress: SongProgress?,
    onOpen: () -> Unit,
    onFavorite: () -> Unit,
) {
    val song = arrangement.song
    val palette = LocalHarpPalette.current
    val seconds = (song.score.totalBeats * 60.0 / song.tempo).toInt()
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(start = 18.dp, end = 8.dp, top = 14.dp, bottom = 14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(song.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${song.composer} · ${song.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (progress?.favorite == true) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favourite",
                        tint = if (progress?.favorite == true) palette.bend else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            // the first few notes as tab, in colour
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (n in arrangement.notes.take(9)) {
                    Text(
                        n.harp?.label(settings.tabStyle) ?: "?",
                        style = TabTextStyle.copy(fontSize = 15.sp),
                        color = if (palette.isBent(n.harp)) palette.bend else palette.forNote(n.harp),
                    )
                }
                Text("…", style = TabTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 10.dp)) {
                DifficultyPill(arrangement.difficulty)
                Spacer(Modifier.width(8.dp))
                Text(
                    "%d:%02d · %s".format(seconds / 60, seconds % 60, arrangement.positionName()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                if ((progress?.best ?: -1) >= 0) {
                    Stars(progress!!.best)
                    Spacer(Modifier.width(4.dp))
                    Text("${progress.best}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KeyPickerDialog(current: HarpKey, onPick: (HarpKey) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Which harmonica?") },
        text = {
            Column {
                Text(
                    "The key is stamped on the cover plate. Tabs stay the same; the band and note names follow your harp.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (k in HarpKey.entries) {
                        FilterChip(selected = k == current, onClick = { onPick(k) }, label = { Text(k.label) })
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}
