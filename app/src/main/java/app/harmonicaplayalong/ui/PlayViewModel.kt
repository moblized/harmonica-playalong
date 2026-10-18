package app.harmonicaplayalong.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.harmonicaplayalong.HarmonicaApp
import app.harmonicaplayalong.audio.MicPitch
import app.harmonicaplayalong.audio.Mix
import app.harmonicaplayalong.audio.PitchReading
import app.harmonicaplayalong.data.Settings
import app.harmonicaplayalong.music.Arrangement
import app.harmonicaplayalong.music.Arranger
import app.harmonicaplayalong.music.PerformanceTracker
import app.harmonicaplayalong.music.Richter
import app.harmonicaplayalong.music.Song
import app.harmonicaplayalong.music.SongLibrary
import app.harmonicaplayalong.music.Timeline
import app.harmonicaplayalong.music.TimelineBuilder
import app.harmonicaplayalong.music.WaitTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RunResult(
    val percent: Int,
    val hits: Int,
    val total: Int,
    val best: Int,
    val newBest: Boolean,
    val waitMode: Boolean,
    val seconds: Int,
)

data class PlayState(
    val arrangement: Arrangement,
    val settings: Settings,
    val playing: Boolean = false,
    val tempoPercent: Int = 100,
    val micOn: Boolean = false,
    val waitMode: Boolean = false,
    val loop: Boolean = false,
    val waitIndex: Int = 0,
    val best: Int = -1,
    val favorite: Boolean = false,
    val result: RunResult? = null,
    /** Set when the UI should ask for the microphone permission. */
    val askMic: Boolean = false,
    val micUnavailable: Boolean = false,
) {
    val bpm: Double get() = arrangement.song.tempo * tempoPercent / 100.0
}

class PlayViewModel(app: Application, songId: String) : AndroidViewModel(app) {
    private val prefs = (app as HarmonicaApp).prefs
    private val engine = (app as HarmonicaApp).engine
    val song: Song = SongLibrary.byId(songId) ?: SongLibrary.songs.first()
    private val autoArrangement: Arrangement = Arranger.best(song)

    private val _state = MutableStateFlow(
        PlayState(
            arrangement = arrangementFor(prefs.progressFor(song.id).shift),
            settings = prefs.settings.value,
            best = prefs.progressFor(song.id).best,
            favorite = prefs.progressFor(song.id).favorite,
        ),
    )
    val state: StateFlow<PlayState> = _state.asStateFlow()

    private val _reading = MutableStateFlow<PitchReading?>(null)
    val reading: StateFlow<PitchReading?> = _reading.asStateFlow()

    private var timeline: Timeline = TimelineBuilder.build(_state.value.arrangement, _state.value.settings.harpKey)
    private var tracker = newTracker()
    private var waitTracker = newWaitTracker()
    private val lock = Any()

    /** Position while stopped. */
    private var pausedBeat = 0.0
    private var waitShown = 0.0
    private var lastFrame = 0L
    private var runStarted = 0L
    private var lastHarpKey = _state.value.settings.harpKey

    private val mic = MicPitch(app) { onReading(it) }

    init {
        viewModelScope.launch {
            prefs.settings.collect { s ->
                val keyChanged = s.harpKey != lastHarpKey
                lastHarpKey = s.harpKey
                _state.update { it.copy(settings = s) }
                mic.sensitivity = s.micSensitivity
                synchronized(lock) {
                    tracker.anyOctave = s.anyOctave
                    waitTracker.anyOctave = s.anyOctave
                }
                engine.setMix(mixOf(s))
                if (keyChanged) rebuild()
            }
        }
    }

    // ---------- arrangement ----------

    private fun arrangementFor(shift: Int?): Arrangement =
        if (shift == null || shift == autoArrangement.shift) autoArrangement else Arranger.arrange(song, shift)

    private fun newTracker() = PerformanceTracker(_state.value.arrangement.notes, _state.value.settings.harpKey.baseMidi).also {
        it.anyOctave = _state.value.settings.anyOctave
    }

    private fun newWaitTracker() = WaitTracker(_state.value.arrangement.notes, _state.value.settings.harpKey.baseMidi).also {
        it.anyOctave = _state.value.settings.anyOctave
    }

    private fun rebuild() {
        stopEngine()
        timeline = TimelineBuilder.build(_state.value.arrangement, _state.value.settings.harpKey)
        synchronized(lock) {
            tracker = newTracker()
            val at = waitTracker.index
            waitTracker = newWaitTracker()
            waitTracker.reset(at)
        }
    }

    /** Moves the whole song up or down; null goes back to the automatic best fit. */
    fun setShift(shift: Int?) {
        val arr = arrangementFor(shift)
        prefs.updateProgress(song.id) { it.copy(shift = if (shift == null || arr === autoArrangement) null else shift) }
        _state.update { it.copy(arrangement = arr) }
        rebuild()
    }

    fun nudgeShift(semitones: Int) {
        val current = _state.value.arrangement.shift
        val lo = song.score.notes.minOf { it.midi }
        val hi = song.score.notes.maxOf { it.midi }
        val target = current + semitones
        // keep the melody within the harmonica's reach
        if (lo + target - 60 < Richter.lowest || hi + target - 60 > Richter.highest) return
        setShift(target)
    }

    val isAutoShift: Boolean get() = _state.value.arrangement === autoArrangement

    // ---------- transport ----------

    private fun mixOf(s: Settings) = Mix(s.melody, s.backing, s.click)

    private fun countInBars(): Int {
        if (!_state.value.settings.countIn) return 0
        return if (song.score.beatsPerBar <= 2.0) 2 else 1
    }

    fun togglePlay() {
        if (_state.value.playing) pause() else play()
    }

    fun play() {
        val s = _state.value
        if (s.waitMode) return
        val total = song.score.totalBeats
        if (pausedBeat >= total - 1e-6) pausedBeat = 0.0
        if (pausedBeat <= 1e-6) {
            synchronized(lock) { tracker.reset() }
            runStarted = System.nanoTime()
        }
        val loopRange = if (s.loop) loopRange(pausedBeat) else null
        engine.play(timeline, pausedBeat, countInBars(), s.bpm, mixOf(s.settings), loopRange) { onFinished() }
        _state.update { it.copy(playing = true, result = null) }
    }

    fun pause() {
        val b = engine.stop()
        if (b != null) pausedBeat = b.coerceIn(0.0, song.score.totalBeats)
        _state.update { it.copy(playing = false) }
    }

    private fun stopEngine() {
        if (engine.isPlaying) pause()
    }

    fun restart() {
        val wasPlaying = _state.value.playing
        stopEngine()
        pausedBeat = 0.0
        synchronized(lock) {
            tracker.reset()
            waitTracker.reset(0)
        }
        waitShown = 0.0
        runStarted = System.nanoTime()
        _state.update { it.copy(waitIndex = 0, result = null) }
        if (wasPlaying) play()
    }

    /** Jumps to note [index] (from tapping the tab strip). */
    fun seekToNote(index: Int) {
        val notes = _state.value.arrangement.notes
        if (index !in notes.indices) return
        val wasPlaying = _state.value.playing
        stopEngine()
        pausedBeat = notes[index].note.start
        if (_state.value.waitMode) {
            synchronized(lock) { waitTracker.reset(index) }
            _state.update { it.copy(waitIndex = index) }
        } else if (wasPlaying) {
            play()
        }
    }

    private fun onFinished() {
        val s = _state.value
        pausedBeat = 0.0
        prefs.updateProgress(song.id) { it.copy(plays = it.plays + 1) }
        if (s.micOn && !s.waitMode) {
            val percent = synchronized(lock) { tracker.percent() }
            val hits = synchronized(lock) { tracker.hits }
            finishRun(percent, hits, waitMode = false)
        }
        _state.update { it.copy(playing = false) }
    }

    private fun finishRun(percent: Int, hits: Int, waitMode: Boolean) {
        val old = prefs.progressFor(song.id).best
        val newBest = !waitMode && percent > old
        if (newBest) prefs.updateProgress(song.id) { it.copy(best = percent) }
        val best = maxOf(old, if (waitMode) old else percent)
        val seconds = ((System.nanoTime() - runStarted) / 1_000_000_000L).toInt()
        _state.update {
            it.copy(
                best = best,
                result = RunResult(percent, hits, it.arrangement.notes.size, best, newBest, waitMode, seconds),
            )
        }
    }

    fun dismissResult() {
        _state.update { it.copy(result = null) }
    }

    fun setTempo(percent: Int) {
        _state.update { it.copy(tempoPercent = percent.coerceIn(40, 150)) }
        engine.setBpm(_state.value.bpm)
    }

    fun toggleMelody() = prefs.update { it.copy(melody = !it.melody) }
    fun toggleBacking() = prefs.update { it.copy(backing = !it.backing) }
    fun toggleClick() = prefs.update { it.copy(click = !it.click) }

    fun toggleFavorite() {
        prefs.updateProgress(song.id) { it.copy(favorite = !it.favorite) }
        _state.update { it.copy(favorite = prefs.progressFor(song.id).favorite) }
    }

    private fun loopRange(atBeat: Double): ClosedFloatingPointRange<Double> {
        val score = song.score
        return score.phraseRange(score.phraseAt(atBeat))
    }

    fun toggleLoop() {
        val on = !_state.value.loop
        _state.update { it.copy(loop = on) }
        if (_state.value.playing) {
            val now = engine.beatNow() ?: pausedBeat
            engine.setLoop(if (on) loopRange(now.coerceAtLeast(0.0)) else null)
        }
    }

    /** The phrase being looped, for the label. */
    fun loopPhrase(): Int = song.score.phraseAt(currentRawBeat().coerceAtLeast(0.0)) + 1

    // ---------- microphone ----------

    fun toggleMic() {
        if (_state.value.micOn) {
            mic.stop()
            _reading.value = null
            _state.update { it.copy(micOn = false, waitMode = false) }
        } else {
            startMic()
        }
    }

    private fun startMic(): Boolean {
        if (!mic.hasPermission()) {
            _state.update { it.copy(askMic = true) }
            return false
        }
        val ok = mic.start()
        _state.update { it.copy(micOn = ok, micUnavailable = !ok) }
        return ok
    }

    fun micPermissionAsked() {
        _state.update { it.copy(askMic = false) }
    }

    fun onMicPermission(granted: Boolean) {
        if (granted) {
            startMic()
            if (_pendingWait) {
                _pendingWait = false
                enterWait()
            }
        } else {
            _pendingWait = false
            _state.update { it.copy(micUnavailable = true) }
        }
    }

    private var _pendingWait = false

    fun toggleWait() {
        if (_state.value.waitMode) {
            _state.update { it.copy(waitMode = false) }
            return
        }
        stopEngine()
        if (!_state.value.micOn) {
            if (!mic.hasPermission()) {
                _pendingWait = true
                _state.update { it.copy(askMic = true) }
                return
            }
            if (!startMic()) return
        }
        enterWait()
    }

    private fun enterWait() {
        val idx = song.score.noteIndexAt(pausedBeat).coerceAtLeast(0)
        synchronized(lock) { waitTracker.reset(idx) }
        waitShown = _state.value.arrangement.notes[idx].note.start
        runStarted = System.nanoTime()
        _state.update { it.copy(waitMode = true, waitIndex = idx, result = null) }
    }

    /** Plays the note you are waiting on (wait mode) or the next one. */
    fun hearCurrent() {
        val s = _state.value
        val i = if (s.waitMode) s.waitIndex else song.score.noteIndexAt(currentRawBeat()).coerceAtLeast(0)
        if (i in s.arrangement.notes.indices) {
            // don't let the microphone mistake the preview for your playing
            ignoreUntil = System.nanoTime() + 1_000_000_000L
            engine.preview(s.arrangement.soundingMidi(i, s.settings.harpKey))
        }
    }

    @Volatile private var ignoreUntil = 0L

    private fun onReading(r: PitchReading) {
        _reading.value = r
        if (r.nanoTime < ignoreUntil) return
        val s = _state.value
        if (s.waitMode) {
            var finished = false
            var idx = 0
            synchronized(lock) {
                if (waitTracker.onPitch(r.midi, r.rms)) {
                    idx = waitTracker.index
                    finished = waitTracker.done
                } else {
                    return
                }
            }
            if (finished) {
                pausedBeat = 0.0
                _state.update { it.copy(waitIndex = idx) }
                finishRun(100, s.arrangement.notes.size, waitMode = true)
                synchronized(lock) { waitTracker.reset(0) }
                _state.update { it.copy(waitIndex = 0) }
                waitShown = 0.0
            } else {
                pausedBeat = s.arrangement.notes[idx].note.start
                _state.update { it.copy(waitIndex = idx) }
            }
        } else if (s.playing && r.voiced) {
            val beat = engine.beatAt(r.nanoTime) ?: return
            synchronized(lock) { tracker.onPitch(beat, r.midi, s.bpm) }
        }
    }

    fun isHit(i: Int): Boolean = synchronized(lock) { tracker.isHit(i) }

    // ---------- position for the screen ----------

    private fun currentRawBeat(): Double = if (_state.value.playing) engine.beatNow() ?: pausedBeat else pausedBeat

    /** Beat to draw this frame (smoothed in wait mode, shifted by the user's sync offset). */
    fun displayBeat(): Double {
        val s = _state.value
        val now = System.nanoTime()
        if (s.waitMode) {
            val target = s.arrangement.notes.getOrNull(s.waitIndex)?.note?.start ?: 0.0
            val dt = if (lastFrame == 0L) 0.016 else ((now - lastFrame) / 1e9).coerceIn(0.0, 0.1)
            lastFrame = now
            waitShown += (target - waitShown) * (dt * 9.0).coerceAtMost(1.0)
            return waitShown
        }
        lastFrame = now
        val offsetBeats = s.settings.syncOffsetMs / 1000.0 * s.bpm / 60.0
        return currentRawBeat() - offsetBeats
    }

    /** End of the count-in while one is running. */
    fun countInEnd(): Double? = if (_state.value.playing) engine.playStart else null

    /** Called when the screen goes away or the app goes to the background. */
    fun onLeave() {
        stopEngine()
        if (mic.isRunning) {
            mic.stop()
            _reading.value = null
            _state.update { it.copy(micOn = false, waitMode = false) }
        }
    }

    override fun onCleared() {
        onLeave()
    }
}
