package app.harmonicaplayalong.ui

import android.Manifest
import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.harmonicaplayalong.music.ArrangedNote
import app.harmonicaplayalong.music.Pitch
import app.harmonicaplayalong.music.Richter
import app.harmonicaplayalong.music.TabStyle
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlayScreen(songId: String, onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as Application
    val vm: PlayViewModel = viewModel(key = "play-$songId") { PlayViewModel(app, songId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val arr = state.arrangement
    val score = arr.score
    val key = state.settings.harpKey
    val flats = arr.usesFlats(key)

    DisposableEffect(vm) { onDispose { vm.onLeave() } }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.onLeave() }

    val view = LocalView.current
    DisposableEffect(state.playing, state.waitMode) {
        view.keepScreenOn = state.playing || state.waitMode
        onDispose { view.keepScreenOn = false }
    }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { vm.onMicPermission(it) }
    LaunchedEffect(state.askMic) {
        if (state.askMic) {
            vm.micPermissionAsked()
            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val beat = remember { mutableDoubleStateOf(vm.displayBeat()) }
    LaunchedEffect(vm) {
        while (true) {
            withFrameNanos { beat.doubleValue = vm.displayBeat() }
        }
    }
    val currentIndex by remember(arr, state.waitMode, state.waitIndex) {
        derivedStateOf { if (state.waitMode) state.waitIndex else score.noteIndexAt(beat.doubleValue + 1e-3) }
    }

    var showSheet by remember { mutableStateOf(false) }
    var showTranspose by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(vm.song.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            vm.song.composer,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = vm::toggleFavorite) {
                        Icon(
                            if (state.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favourite",
                            tint = if (state.favorite) LocalHarpPalette.current.bend else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { showSheet = true }) { Icon(Icons.Rounded.GridView, contentDescription = "Whole tab") }
                    Box {
                        IconButton(onClick = { showTranspose = true }) { Icon(Icons.Rounded.SwapVert, contentDescription = "Transpose") }
                        DropdownMenu(expanded = showTranspose, onDismissRequest = { showTranspose = false }) {
                            DropdownMenuItem(text = { Text("Octave up") }, onClick = { vm.nudgeShift(12); showTranspose = false })
                            DropdownMenuItem(text = { Text("Half step up") }, onClick = { vm.nudgeShift(1); showTranspose = false })
                            DropdownMenuItem(text = { Text("Half step down") }, onClick = { vm.nudgeShift(-1); showTranspose = false })
                            DropdownMenuItem(text = { Text("Octave down") }, onClick = { vm.nudgeShift(-12); showTranspose = false })
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(if (vm.isAutoShift) "Best fit (current)" else "Back to best fit") },
                                onClick = { vm.setShift(null); showTranspose = false },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            InfoRow(state)
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(16.dp)),
            ) {
                NoteHighway(
                    info = HighwayInfo(
                        notes = arr.notes,
                        barStarts = score.barStarts,
                        tabStyle = state.settings.tabStyle,
                        bpm = state.bpm,
                        clickEvery = score.clickEvery,
                        scoring = state.micOn && !state.waitMode,
                        isHit = vm::isHit,
                        waitIndex = if (state.waitMode) state.waitIndex else -1,
                        countInEnd = vm::countInEnd,
                    ),
                    beat = beat,
                    modifier = Modifier.fillMaxSize(),
                )
                if (state.waitMode) {
                    WaitBanner(arr.notes.getOrNull(state.waitIndex), key.baseMidi, flats, state.settings.tabStyle, Modifier.align(Alignment.TopCenter))
                }
            }
            TabStrip(
                notes = arr.notes,
                current = currentIndex,
                tabStyle = state.settings.tabStyle,
                showNames = state.settings.showNoteNames,
                noteName = { i -> Pitch.name(arr.soundingMidi(i, key), flats) },
                onTap = vm::seekToNote,
            )
            if (state.micOn) {
                MicFeedback(vm, state)
            }
            Controls(vm, state)
            ToggleRow(vm, state)
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            TabSheet(
                notes = arr.notes,
                phraseCount = score.phraseCount,
                tabStyle = state.settings.tabStyle,
                showNames = state.settings.showNoteNames,
                noteName = { i -> Pitch.name(arr.soundingMidi(i, key), flats) },
                current = currentIndex,
                onTap = { vm.seekToNote(it); showSheet = false },
            )
        }
    }

    state.result?.let { r ->
        AlertDialog(
            onDismissRequest = vm::dismissResult,
            title = { Text(if (r.waitMode) "Well played!" else resultTitle(r.percent)) },
            text = {
                Column {
                    if (r.waitMode) {
                        Text("You played all ${r.total} notes of ${vm.song.title} in ${formatTime(r.seconds)}.")
                        Spacer(Modifier.height(8.dp))
                        Text("Try it in play-along mode next, with the band.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("${r.percent}%", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                        Text("${r.hits} of ${r.total} notes on pitch and on time")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (r.newBest) "New personal best!" else "Best so far: ${r.best}%",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.dismissResult(); vm.restart(); if (!r.waitMode) vm.play() }) { Text("Play again") }
            },
            dismissButton = { TextButton(onClick = vm::dismissResult) { Text("Close") } },
        )
    }
}

private fun resultTitle(p: Int) = when {
    p >= 90 -> "Bravo!"
    p >= 70 -> "Nicely done"
    p >= 40 -> "Getting there"
    else -> "Keep practising"
}

private fun formatTime(seconds: Int) = "%d:%02d".format(seconds / 60, seconds % 60)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoRow(state: PlayState) {
    val arr = state.arrangement
    val key = state.settings.harpKey
    FlowRow(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Pill("${key.label} harp")
        Pill("${arr.positionName()} · ${arr.playingKeyName(key)}")
        DifficultyPill(arr.difficulty)
        if (arr.bends > 0) Pill("${arr.bends} bends", LocalHarpPalette.current.bend)
        if (arr.overblows > 0) Pill("${arr.overblows} overblows", LocalHarpPalette.current.bend)
    }
}

@Composable
private fun WaitBanner(note: ArrangedNote?, base: Int, flats: Boolean, tabStyle: TabStyle, modifier: Modifier) {
    if (note == null) return
    val palette = LocalHarpPalette.current
    Surface(
        modifier.padding(top = 10.dp),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.92f),
        tonalElevation = 2.dp,
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Play ", style = MaterialTheme.typography.bodyMedium)
            Text(
                note.harp?.label(tabStyle) ?: "?",
                style = TabTextStyle.copy(fontSize = 18.sp),
                color = palette.forNote(note.harp),
            )
            Text(
                "  ${Pitch.name(base + note.semitone, flats)} · ${note.harp?.describe() ?: "out of range"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TabStrip(
    notes: List<ArrangedNote>,
    current: Int,
    tabStyle: TabStyle,
    showNames: Boolean,
    noteName: (Int) -> String,
    onTap: (Int) -> Unit,
) {
    val palette = LocalHarpPalette.current
    val list = rememberLazyListState()
    LaunchedEffect(current) {
        if (current >= 0) list.animateScrollToItem((current - 3).coerceAtLeast(0))
    }
    LazyRow(
        state = list,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(notes) { i, n ->
            val newPhrase = i > 0 && notes[i - 1].note.phrase != n.note.phrase
            val newBar = i > 0 && notes[i - 1].note.bar != n.note.bar
            val isCurrent = i == current
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (newPhrase) Spacer(Modifier.width(14.dp))
                if (newBar && !newPhrase) {
                    Box(
                        Modifier
                            .padding(horizontal = 2.dp)
                            .width(1.dp)
                            .height(28.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant),
                    )
                }
                Column(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        .clickable { onTap(i) }
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        n.harp?.label(tabStyle) ?: "?",
                        style = TabTextStyle.copy(fontSize = 17.sp),
                        color = if (palette.isBent(n.harp)) palette.bend else palette.forNote(n.harp),
                    )
                    if (showNames) {
                        Text(noteName(i), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TabSheet(
    notes: List<ArrangedNote>,
    phraseCount: Int,
    tabStyle: TabStyle,
    showNames: Boolean,
    noteName: (Int) -> String,
    current: Int,
    onTap: (Int) -> Unit,
) {
    val palette = LocalHarpPalette.current
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
            .padding(bottom = 24.dp),
    ) {
        Text("Whole tab", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Tap any note to start from there. Blue = blow, orange = draw, pink = bend.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        for (p in 0 until phraseCount) {
            Spacer(Modifier.height(14.dp))
            Text("Phrase ${p + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for ((i, n) in notes.withIndex()) {
                    if (n.note.phrase != p) continue
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (i == current) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { onTap(i) }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            n.harp?.label(tabStyle) ?: "?",
                            style = TabTextStyle.copy(fontSize = 16.sp),
                            color = if (palette.isBent(n.harp)) palette.bend else palette.forNote(n.harp),
                        )
                        if (showNames) Text(noteName(i), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun MicFeedback(vm: PlayViewModel, state: PlayState) {
    val reading by vm.reading.collectAsStateWithLifecycle()
    val palette = LocalHarpPalette.current
    val base = state.settings.harpKey.baseMidi
    val flats = state.arrangement.usesFlats(state.settings.harpKey)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Hearing", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(10.dp))
        val r = reading
        if (r == null || !r.voiced) {
            Text("…", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            val semi = (r.midi - base).roundToInt()
            val harp = Richter.easiest(semi)
            val cents = ((r.midi - (base + semi)) * 100).roundToInt()
            Text(
                harp?.label(state.settings.tabStyle) ?: "–",
                style = TabTextStyle.copy(fontSize = 18.sp),
                color = palette.forNote(harp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "${Pitch.name(base + semi, flats)}  ${if (cents >= 0) "+" else ""}$cents¢",
                style = MaterialTheme.typography.bodyMedium,
                color = if (abs(cents) <= 20) palette.hit else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.weight(1f))
        if (!state.waitMode && state.settings.melody) {
            Text(
                "Tip: headphones, or Melody off",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Controls(vm: PlayViewModel, state: PlayState) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            IconButton(onClick = vm::restart) { Icon(Icons.Rounded.Replay, contentDescription = "Start again") }
            IconButton(onClick = vm::toggleLoop) {
                Icon(
                    Icons.Rounded.Repeat,
                    contentDescription = "Loop this phrase",
                    tint = if (state.loop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.waitMode) {
                FilledIconButton(
                    onClick = vm::hearCurrent,
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                ) { Icon(Icons.Rounded.Hearing, contentDescription = "Hear this note", modifier = Modifier.size(30.dp)) }
            } else {
                FilledIconButton(
                    onClick = vm::togglePlay,
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Icon(
                        if (state.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.playing) "Pause" else "Play",
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
            IconButton(onClick = vm::hearCurrent) { Icon(Icons.Rounded.Hearing, contentDescription = "Hear the next note") }
            Text(
                if (state.loop) "Loop ${vm.loopPhrase()}" else "${state.tempoPercent}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(56.dp),
            )
        }
        if (!state.waitMode) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tempo", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(56.dp))
                Slider(
                    value = state.tempoPercent.toFloat(),
                    onValueChange = { vm.setTempo((it / 5f).roundToInt() * 5) },
                    valueRange = 40f..150f,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "♩ ${state.bpm.roundToInt()}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .width(64.dp)
                        .padding(start = 8.dp),
                )
            }
        } else {
            Text(
                "Wait mode: the music waits until you play the highlighted note.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 10.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ToggleRow(vm: PlayViewModel, state: PlayState) {
    FlowRow(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(selected = state.settings.melody, onClick = vm::toggleMelody, label = { Text("Melody") })
        FilterChip(selected = state.settings.backing, onClick = vm::toggleBacking, label = { Text("Band") })
        FilterChip(selected = state.settings.click, onClick = vm::toggleClick, label = { Text("Click") })
        FilterChip(selected = state.micOn, onClick = vm::toggleMic, label = { Text("Listen") })
        FilterChip(selected = state.waitMode, onClick = vm::toggleWait, label = { Text("Wait for me") })
    }
    if (state.micUnavailable && !state.micOn) {
        Text(
            "Microphone not available — allow it in Android settings to score your playing.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
}
