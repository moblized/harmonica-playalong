package app.harmonicaplayalong.ui

import android.Manifest
import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.withFrameNanos
import app.harmonicaplayalong.HarmonicaApp
import app.harmonicaplayalong.audio.MicPitch
import app.harmonicaplayalong.audio.PitchReading
import app.harmonicaplayalong.music.Breath
import app.harmonicaplayalong.music.HarpKey
import app.harmonicaplayalong.music.HarpNote
import app.harmonicaplayalong.music.Pitch
import app.harmonicaplayalong.music.Richter
import app.harmonicaplayalong.music.TabStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.roundToInt

class FreePlayViewModel(app: Application) : AndroidViewModel(app) {
    private val harmonicaApp = app as HarmonicaApp
    private val engine = harmonicaApp.engine
    val prefs = harmonicaApp.prefs

    private val _reading = MutableStateFlow<PitchReading?>(null)
    val reading: StateFlow<PitchReading?> = _reading.asStateFlow()
    private val _listening = MutableStateFlow(false)
    val listening: StateFlow<Boolean> = _listening.asStateFlow()
    private val _askMic = MutableStateFlow(false)
    val askMic: StateFlow<Boolean> = _askMic.asStateFlow()
    private val _denied = MutableStateFlow(false)
    val denied: StateFlow<Boolean> = _denied.asStateFlow()

    /** Recent pitches for the trace: MIDI (NaN = silence) and time. */
    private val historyMidi = DoubleArray(260) { Double.NaN }
    private val historyTime = LongArray(260)
    private var historyNext = 0
    private val lock = Any()
    @Volatile private var ignoreUntil = 0L

    private val mic = MicPitch(app) { r -> if (r.nanoTime >= ignoreUntil) record(r) }

    private fun record(r: PitchReading) {
        _reading.value = r
        synchronized(lock) {
            historyMidi[historyNext] = r.midi
            historyTime[historyNext] = r.nanoTime
            historyNext = (historyNext + 1) % historyMidi.size
        }
    }

    fun toggle() {
        if (_listening.value) stop() else start()
    }

    fun start() {
        mic.sensitivity = prefs.settings.value.micSensitivity
        if (!mic.hasPermission()) {
            _askMic.value = true
            return
        }
        _listening.value = mic.start()
        _denied.value = !_listening.value
    }

    fun stop() {
        mic.stop()
        _listening.value = false
        _reading.value = null
    }

    fun permissionAsked() {
        _askMic.value = false
    }

    fun onPermission(granted: Boolean) {
        if (granted) start() else _denied.value = true
    }

    fun play(hole: Int, breath: Breath) {
        val key = prefs.settings.value.harpKey
        val semi = HarpNote(hole, breath).semitone
        ignoreUntil = System.nanoTime() + 900_000_000L
        engine.preview(key.baseMidi + semi)
    }

    /** Copies the recent history (oldest first) as (age in seconds, midi). */
    fun history(now: Long): List<Pair<Double, Double>> = synchronized(lock) {
        val out = ArrayList<Pair<Double, Double>>(historyMidi.size)
        for (k in 0 until historyMidi.size) {
            val i = (historyNext + k) % historyMidi.size
            if (historyTime[i] == 0L) continue
            out += (now - historyTime[i]) / 1e9 to historyMidi[i]
        }
        out
    }

    override fun onCleared() {
        stop()
    }
}

@Composable
fun FreePlayScreen(modifier: Modifier = Modifier) {
    val app = LocalContext.current.applicationContext as Application
    val vm: FreePlayViewModel = viewModel { FreePlayViewModel(app) }
    val reading by vm.reading.collectAsStateWithLifecycle()
    val listening by vm.listening.collectAsStateWithLifecycle()
    val askMic by vm.askMic.collectAsStateWithLifecycle()
    val denied by vm.denied.collectAsStateWithLifecycle()
    val settings by vm.prefs.settings.collectAsStateWithLifecycle()
    val key = settings.harpKey
    val palette = LocalHarpPalette.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { vm.onPermission(it) }
    LaunchedEffect(askMic) {
        if (askMic) {
            vm.permissionAsked()
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.stop() }
    DisposableEffect(vm) { onDispose { vm.stop() } }

    val r = reading
    val semi = if (r != null && r.voiced) (r.midi - key.baseMidi).roundToInt() else null
    val harp = semi?.let { Richter.easiest(it) }
    val cents = if (r != null && semi != null) ((r.midi - (key.baseMidi + semi)) * 100).roundToInt() else 0

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text("Free play", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Play any single note to see which hole it is, how in tune it is, and how far you are bending.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))

        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (harp != null && r != null) {
                    Text(
                        harp.label(settings.tabStyle),
                        style = TabTextStyle.copy(fontSize = 56.sp),
                        color = if (palette.isBent(harp)) palette.bend else palette.forNote(harp),
                    )
                    Text(
                        "${Pitch.name(key.baseMidi + semi!!, false)} · ${harp.describe()}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    CentsMeter(cents)
                } else {
                    Text("–", style = TabTextStyle.copy(fontSize = 56.sp), color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        if (listening) "Listening… play one clean note" else "Tap Start listening, then blow or draw a note",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        if (harp != null && r != null) {
            BendMeter(harp, r.midi, key, settings.tabStyle)
        }

        Spacer(Modifier.height(14.dp))
        PitchTrace(vm, key, settings.tabStyle, listening)

        Spacer(Modifier.height(18.dp))
        Text("Your ${key.label} harmonica", style = MaterialTheme.typography.titleMedium)
        Text(
            "Tap the top half of a hole to hear its blow note, the bottom half for draw.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        NoteRow(key, Breath.BLOW)
        HarmonicaDiagram(
            Modifier
                .fillMaxWidth()
                .height(72.dp),
            lit = harp,
            onTap = vm::play,
        )
        NoteRow(key, Breath.DRAW)

        Spacer(Modifier.height(18.dp))
        if (listening) {
            OutlinedButton(onClick = vm::toggle, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.MicOff, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Stop listening")
            }
        } else {
            Button(onClick = vm::toggle, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Mic, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start listening")
            }
        }
        if (denied) {
            Text(
                "The microphone isn't available. Allow it for this app in Android settings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun NoteRow(key: HarpKey, breath: Breath) {
    val palette = LocalHarpPalette.current
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        for (hole in 1..10) {
            val midi = key.baseMidi + HarpNote(hole, breath).semitone
            Text(
                Pitch.className(midi, key.offset in listOf(-4, -2, 1, 3, 5)),
                style = MaterialTheme.typography.labelMedium,
                color = if (breath == Breath.BLOW) palette.blow else palette.draw,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CentsMeter(cents: Int) {
    val palette = LocalHarpPalette.current
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val onSurface = MaterialTheme.colorScheme.onSurfaceVariant
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(18.dp),
        ) {
            val mid = size.width / 2f
            drawRoundRect(track, Offset(0f, size.height * 0.35f), Size(size.width, size.height * 0.3f), CornerRadius(8f, 8f))
            drawLine(onSurface, Offset(mid, 0f), Offset(mid, size.height), strokeWidth = 2f)
            val x = mid + (cents.coerceIn(-50, 50) / 50f) * mid
            drawCircle(if (abs(cents) <= 15) palette.hit else palette.draw, radius = size.height / 2.2f, center = Offset(x, size.height / 2f))
        }
        Text(
            when {
                abs(cents) <= 15 -> "In tune"
                cents < 0 -> "$cents¢ flat"
                else -> "+$cents¢ sharp"
            },
            style = MaterialTheme.typography.labelMedium,
            color = onSurface,
        )
    }
}

/** For bendable holes: how far down the bend you are, step by step. */
@Composable
private fun BendMeter(harp: HarpNote, midi: Double, key: HarpKey, tabStyle: TabStyle) {
    val depth = Richter.bendDepth(harp.hole, harp.breath)
    if (depth == 0 || harp.overblow) return
    val palette = LocalHarpPalette.current
    val natural = key.baseMidi + HarpNote(harp.hole, harp.breath).semitone
    val bent = (natural - midi).coerceIn(-0.5, depth + 0.5)
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val tm = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Spacer(Modifier.height(12.dp))
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Bend meter · hole ${harp.hole} ${if (harp.breath == Breath.DRAW) "draw" else "blow"}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                "Drop your jaw and tongue to pull the note down. Each mark is a half step.",
                style = MaterialTheme.typography.bodySmall,
                color = labelColor,
            )
            Spacer(Modifier.height(10.dp))
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                val left = 24f
                val right = size.width - 24f
                val y = size.height * 0.35f
                val step = (right - left) / depth
                drawLine(track, Offset(left, y), Offset(right, y), strokeWidth = 10f, cap = StrokeCap.Round)
                for (k in 0..depth) {
                    val x = left + k * step
                    drawCircle(labelColor, radius = 5f, center = Offset(x, y))
                    val label = HarpNote(harp.hole, harp.breath, bend = k).label(tabStyle)
                    val layout = tm.measure(label, TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = labelColor))
                    drawText(layout, topLeft = Offset(x - layout.size.width / 2f, y + 14f))
                }
                val x = left + (bent.toFloat().coerceIn(0f, depth.toFloat())) * step
                drawCircle(palette.bend, radius = 13f, center = Offset(x, y))
            }
        }
    }
}

@Composable
private fun PitchTrace(vm: FreePlayViewModel, key: HarpKey, tabStyle: TabStyle, listening: Boolean) {
    val palette = LocalHarpPalette.current
    val grid = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val bg = MaterialTheme.colorScheme.surfaceContainerLow
    val tm = rememberTextMeasurer(cacheSize = 32)
    val frame = remember { mutableLongStateOf(0L) }
    LaunchedEffect(listening) {
        while (listening) {
            withFrameNanos { frame.longValue = it }
        }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(150.dp),
    ) {
        Canvas(Modifier.matchParentSize()) {
            frame.longValue
            drawRoundRect(bg, cornerRadius = CornerRadius(36f, 36f))
            val pts = vm.history(System.nanoTime()).filter { it.first <= 4.0 }
            val voiced = pts.filter { !it.second.isNaN() }
            val centre = if (voiced.isEmpty()) key.baseMidi + 14.0 else voiced.takeLast(12).map { it.second }.average()
            val span = 7.0
            val top = centre + span
            fun y(m: Double) = ((top - m) / (2 * span) * size.height).toFloat()
            fun x(age: Double) = (size.width - age / 4.0 * size.width).toFloat()
            val lo = (centre - span).roundToInt()
            val hi = (centre + span).roundToInt()
            for (m in lo..hi) {
                val h = Richter.easiest(m - key.baseMidi) ?: continue
                val yy = y(m.toDouble())
                drawLine(grid.copy(alpha = 0.5f), Offset(70f, yy), Offset(size.width, yy), strokeWidth = 1f)
                val layout = tm.measure(h.label(tabStyle), TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = labelColor))
                drawText(layout, topLeft = Offset(12f, yy - layout.size.height / 2f))
            }
            var prev: Pair<Double, Double>? = null
            for (p in pts) {
                val pr = prev
                if (!p.second.isNaN() && pr != null && !pr.second.isNaN() && abs(pr.second - p.second) < 1.5) {
                    val note = Richter.easiest((p.second - key.baseMidi).roundToInt())
                    drawLine(
                        palette.forNote(note),
                        Offset(x(pr.first), y(pr.second)),
                        Offset(x(p.first), y(p.second)),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round,
                    )
                }
                prev = p
            }
        }
        if (!listening) {
            Text(
                "Pitch trace",
                style = MaterialTheme.typography.labelMedium,
                color = labelColor,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 120.dp, height = 24.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

