package app.harmonicaplayalong.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import app.harmonicaplayalong.music.Breath
import app.harmonicaplayalong.music.HarpNote

/**
 * Draws a 10-hole harmonica into [rect]: chrome cover plates, the comb and its holes, hole numbers,
 * and an optional lit hole (blue for blow, orange for draw).
 */
fun DrawScope.drawHarmonica(
    rect: Rect,
    palette: HarpPalette,
    textMeasurer: TextMeasurer,
    lit: HarpNote? = null,
    litColor: Color = Color.Unspecified,
    litStrength: Float = 1f,
) {
    val h = rect.height
    val w = rect.width
    val radius = CornerRadius(h * 0.22f, h * 0.22f)
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to palette.chromeLight,
            0.32f to palette.chromeDark,
            0.68f to palette.chromeDark,
            1f to palette.chromeLight,
            startY = rect.top,
            endY = rect.bottom,
        ),
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = radius,
    )
    val combTop = rect.top + h * 0.34f
    val combH = h * 0.32f
    drawRect(palette.comb, topLeft = Offset(rect.left + w * 0.015f, combTop), size = Size(w * 0.97f, combH))
    val cell = w / 10f
    val numberStyle = TextStyle(color = palette.engraving, fontSize = (h * 0.2f / density).coerceIn(8f, 14f).sp, fontWeight = FontWeight.Bold)
    for (i in 0 until 10) {
        val x = rect.left + i * cell
        val holeRect = Rect(x + cell * 0.2f, combTop + combH * 0.16f, x + cell * 0.8f, combTop + combH * 0.84f)
        val isLit = lit != null && lit.hole == i + 1
        drawRoundRect(
            color = if (isLit) litColor.copy(alpha = 0.35f + 0.65f * litStrength) else palette.hole,
            topLeft = holeRect.topLeft,
            size = holeRect.size,
            cornerRadius = CornerRadius(combH * 0.12f, combH * 0.12f),
        )
        if (isLit) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(litColor.copy(alpha = 0.55f * litStrength), Color.Transparent),
                    center = holeRect.center,
                    radius = cell * 0.9f,
                ),
                radius = cell * 0.9f,
                center = holeRect.center,
            )
            // a small arrow on the plate: up for blow, down for draw
            val arrow = if (lit.breath == Breath.BLOW) "▲" else "▼"
            val layout = textMeasurer.measure(arrow, numberStyle.copy(color = litColor))
            val y = if (lit.breath == Breath.BLOW) rect.top + h * 0.03f else rect.bottom - h * 0.03f - layout.size.height
            drawText(layout, topLeft = Offset(holeRect.center.x - layout.size.width / 2f, y))
        } else {
            val layout = textMeasurer.measure("${i + 1}", numberStyle)
            drawText(layout, topLeft = Offset(x + cell / 2f - layout.size.width / 2f, rect.top + (combTop - rect.top - layout.size.height) / 2f))
        }
    }
    drawRoundRect(
        color = palette.engraving.copy(alpha = 0.5f),
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = radius,
        style = Stroke(width = 1.2f * density),
    )
}

/** A stand-alone harmonica. Tapping the top half of a hole plays its blow note, the bottom half its draw note. */
@Composable
fun HarmonicaDiagram(
    modifier: Modifier = Modifier,
    lit: HarpNote? = null,
    onTap: ((hole: Int, breath: Breath) -> Unit)? = null,
) {
    val palette = LocalHarpPalette.current
    val tm = rememberTextMeasurer(cacheSize = 32)
    val tap = if (onTap == null) Modifier else Modifier.pointerInput(onTap) {
        detectTapGestures { p ->
            val hole = (p.x / size.width * 10).toInt().coerceIn(0, 9) + 1
            onTap(hole, if (p.y < size.height / 2f) Breath.BLOW else Breath.DRAW)
        }
    }
    Canvas(modifier.then(tap)) {
        drawHarmonica(
            Rect(Offset.Zero, size),
            palette,
            tm,
            lit,
            palette.forNote(lit),
        )
    }
}
