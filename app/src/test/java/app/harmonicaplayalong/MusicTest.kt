package app.harmonicaplayalong

import app.harmonicaplayalong.audio.Sequencer
import app.harmonicaplayalong.audio.Yin
import app.harmonicaplayalong.music.Arranger
import app.harmonicaplayalong.music.Breath
import app.harmonicaplayalong.music.HarpKey
import app.harmonicaplayalong.music.Pitch
import app.harmonicaplayalong.music.Richter
import app.harmonicaplayalong.music.SongLibrary
import app.harmonicaplayalong.music.TabStyle
import app.harmonicaplayalong.music.TimelineBuilder
import app.harmonicaplayalong.music.WaitTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class MusicTest {

    @Test
    fun everySongParsesWithMatchingChords() {
        for (song in SongLibrary.songs) {
            val score = song.score
            assertTrue("${song.id} has notes", score.notes.isNotEmpty())
            assertTrue("${song.id} has chords", score.chords.isNotEmpty())
            val last = score.chords.maxOf { it.start + it.duration }
            assertTrue("${song.id} chords cover the song", last >= score.totalBeats - 1e-6)
        }
    }

    @Test
    fun songIdsAreUnique() {
        val ids = SongLibrary.songs.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun richterLayoutIsChromaticFromC4() {
        for (s in 0..36) assertTrue("semitone $s playable", Richter.options(s).isNotEmpty())
        assertEquals(Breath.DRAW, Richter.easiest(2)!!.breath)
        assertEquals("-3''", Richter.easiest(9)!!.label(TabStyle.MINUS))
    }

    @Test
    fun odeToJoyGetsTheClassicTab() {
        val arr = Arranger.best(SongLibrary.byId("ode-to-joy")!!)
        val tab = arr.notes.take(15).joinToString(" ") { it.harp!!.label(TabStyle.MINUS) }
        assertEquals("5 5 -5 6 6 -5 5 -4 4 4 -4 5 5 -4 -4", tab)
        assertEquals(0, arr.bends)
        assertEquals(1, arr.position)
    }

    @Test
    fun everySongFitsTheHarmonica() {
        for (song in SongLibrary.songs) {
            val arr = Arranger.best(song)
            assertEquals("${song.id} unplayable notes", 0, arr.unplayable)
        }
    }

    @Test
    fun sequencerRendersCleanAudio() {
        val arr = Arranger.best(SongLibrary.byId("ode-to-joy")!!)
        val tl = TimelineBuilder.build(arr, HarpKey.C)
        val seq = Sequencer(tl, 44100, 0.0, 1)
        seq.bpm = 108.0
        seq.clickOn = true
        val buf = FloatArray(512)
        var peak = 0f
        var blocks = 0
        while (!seq.finished && blocks < 10000) {
            seq.render(buf, buf.size) { _, _, _ -> }
            for (x in buf) {
                assertTrue(!x.isNaN())
                peak = maxOf(peak, abs(x))
            }
            blocks++
        }
        assertTrue("finished", seq.finished)
        assertTrue("audible", peak > 0.05f)
        assertTrue("not clipping", peak <= 1f)
    }

    @Test
    fun yinFindsHarmonicaPitches() {
        val sr = 44100
        val yin = Yin(sr, 2048)
        for (midi in listOf(55, 60, 67, 72, 79, 84, 91, 96)) {
            val f = Pitch.frequency(midi.toDouble())
            val buf = FloatArray(2048) { i ->
                var s = 0.0
                for (h in 1..8) s += sin(2 * PI * f * h * i / sr) / h
                (0.2 * s).toFloat()
            }
            val r = yin.detect(buf)
            assertTrue("voiced at $midi", r.voiced)
            assertEquals("pitch of $midi", midi.toDouble(), Pitch.midi(r.frequency), 0.1)
        }
    }

    @Test
    fun waitModeNeedsReattackForRepeatedNotes() {
        val arr = Arranger.best(SongLibrary.byId("ode-to-joy")!!)
        val w = WaitTracker(arr.notes, HarpKey.C.baseMidi)
        val e = (HarpKey.C.baseMidi + arr.notes[0].semitone).toDouble()
        repeat(3) { w.onPitch(e, 0.1) }
        assertEquals(1, w.index)
        // holding the same E does not count for the second E
        repeat(5) { w.onPitch(e, 0.1) }
        assertEquals(1, w.index)
        w.onPitch(Double.NaN, 0.0)
        repeat(3) { w.onPitch(e, 0.1) }
        assertEquals(2, w.index)
    }
}
