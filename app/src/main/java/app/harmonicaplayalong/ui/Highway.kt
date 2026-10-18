package app.harmonicaplayalong.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.harmonicaplayalong.music.ArrangedNote
import app.harmonicaplayalong.music.TabStyle
import kotlin.math.ceil

/** What the highway needs to know about the run in progress. */
class HighwayInfo(
    val notes: List<ArrangedNote>,
    val barStarts: List<Double>,
    val tabStyle: TabStyle,
    val bpm: Double,
    val clickEvery: Double,
    /** True when the microphone is scoring, so passed notes show hit or missed. */
    val scoring: Boolean,
    val isHit: (Int) -> Boolean,
    /** Index of the note being waited for in wait mode, or -1. */
    val waitIndex: Int,
    /** Where the music starts after the count-in, or null when no count-in is running. */
    val countInEnd: () -> Double?,
)

/**
 * Notes fall down ten lanes, one per hole, onto a harmonica. A note reaches the line just as it should be played;
 * its height is its length. Blue is blow, orange is draw, a pink outline means bend.
 */
@Composable
fun NoteHighway(info: HighwayInfo, beat: State<Double>, modifier: Modifier = Modifier) {
    val palette = LocalHarpPalette.current
    val tm = rememberTextMeasurer(cacheSize = 96)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val primary = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val b = beat.value
        val w = size.width
        val harpH = (size.height * 0.16f).coerceIn(44.dp.toPx(), 72.dp.toPx())
        val nowY = size.height - harpH - 10.dp.toPx()
        val lane = w / 10f
        val aheadBeats = (info.bpm / 60.0 * 3.2).coerceIn(2.0, 10.0)
        val ppb = (nowY / aheadBeats).toFloat()

        // lanes
        for (i in 0 until 10) {
            drawRect(if (i % 2 == 0) palette.laneA else palette.laneB, Offset(i * lane, 0f), Size(lane, nowY))
        }
        // bar lines
        for (bs in info.barStarts) {
            val y = nowY - ((bs - b) * ppb).toFloat()
            if (y < 0f || y > nowY) continue
            drawLine(onSurface.copy(alpha = 0.12f), Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
        }

        val labelStyle = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        val pad = (lane * 0.1f).coerceAtMost(5.dp.toPx())
        val corner = CornerRadius(7.dp.toPx(), 7.dp.toPx())
        var lit: ArrangedNote? = null

        clipRect(0f, 0f, w, nowY + 2.dp.toPx()) {
            for ((i, n) in info.notes.withIndex()) {
                val start = n.note.start
                val end = n.note.end
                val yBottom = nowY - ((start - b) * ppb).toFloat()
                val yTop = nowY - ((end - b) * ppb).toFloat()
                if (yBottom < -4f) break
                if (yTop > nowY + 40f) continue
                val hole = n.harp?.hole ?: 1
                val x = (hole - 1) * lane + pad
                val rectTop = yTop + 1.5.dp.toPx()
                val rectH = (yBottom - rectTop).coerceAtLeast(6.dp.toPx())
                val sounding = b >= start - 1e-3 && b < end
                val waiting = info.waitIndex == i
                val passed = if (info.waitIndex >= 0) i < info.waitIndex else b >= end
                if (sounding || waiting) lit = n
                val base = palette.forNote(n.harp)
                val fill = when {
                    passed && info.scoring && info.isHit(i) -> palette.hit.copy(alpha = 0.8f)
                    passed && info.scoring -> palette.miss.copy(alpha = 0.45f)
                    passed && info.waitIndex >= 0 -> palette.hit.copy(alpha = 0.6f)
                    passed -> base.copy(alpha = 0.35f)
                    sounding || waiting -> base
                    else -> base.copy(alpha = 0.82f)
                }
                drawRoundRect(fill, Offset(x, rectTop), Size(lane - 2 * pad, rectH), corner)
                if (palette.isBent(n.harp) || n.harp == null) {
                    drawRoundRect(
                        palette.bend,
                        Offset(x, rectTop),
                        Size(lane - 2 * pad, rectH),
                        corner,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            pathEffect = if (n.harp?.overblow == true) PathEffect.dashPathEffect(floatArrayOf(10f, 8f)) else null,
                        ),
                    )
                }
                if (sounding || waiting) {
                    drawRoundRect(Color.White.copy(alpha = 0.85f), Offset(x, rectTop), Size(lane - 2 * pad, rectH), corner, style = Stroke(2.dp.toPx()))
                }
                val label = n.harp?.label(info.tabStyle) ?: "?"
                val layout = tm.measure(label, labelStyle.copy(color = if (passed) onSurface.copy(alpha = 0.7f) else Color.White))
                val inside = rectH >= layout.size.height + 6.dp.toPx()
                val tx = (hole - 1) * lane + lane / 2f - layout.size.width / 2f
                val ty = if (inside) rectTop + rectH - layout.size.height - 3.dp.toPx() else rectTop - layout.size.height - 1.dp.toPx()
                if (inside) {
                    drawText(layout, topLeft = Offset(tx, ty))
                } else {
                    drawText(tm.measure(label, labelStyle.copy(color = base)), topLeft = Offset(tx, ty))
                }
            }
        }

        // the "now" line
        drawLine(onSurface.copy(alpha = 0.55f), Offset(0f, nowY), Offset(w, nowY), strokeWidth = 2.dp.toPx())

        val harpRect = Rect(2.dp.toPx(), nowY + 8.dp.toPx(), w - 2.dp.toPx(), nowY + 8.dp.toPx() + harpH)
        val l = lit
        drawHarmonica(harpRect, palette, tm, l?.harp, palette.forNote(l?.harp))

        // count-in numbers
        val countEnd = info.countInEnd()
        if (countEnd != null && b < countEnd - 1e-6) {
            val remaining = ceil((countEnd - b) / info.clickEvery - 1e-6).toInt()
            if (remaining in 1..12) {
                val big = tm.measure(
                    "$remaining",
                    TextStyle(fontSize = 64.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, color = primary),
                )
                drawText(big, topLeft = Offset(w / 2f - big.size.width / 2f, nowY * 0.35f - big.size.height / 2f))
            }
        }
    }
}
