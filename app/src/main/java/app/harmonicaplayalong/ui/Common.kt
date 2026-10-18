package app.harmonicaplayalong.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.harmonicaplayalong.music.Difficulty

@Composable
fun Pill(text: String, accent: Color = Color.Unspecified, modifier: Modifier = Modifier) {
    val color = if (accent == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else accent
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
fun difficultyColor(d: Difficulty): Color {
    val p = LocalHarpPalette.current
    return when (d) {
        Difficulty.BEGINNER -> p.hit
        Difficulty.INTERMEDIATE -> p.draw
        Difficulty.ADVANCED -> p.bend
    }
}

@Composable
fun DifficultyPill(d: Difficulty) {
    val c = difficultyColor(d)
    Text(
        d.label,
        style = MaterialTheme.typography.labelMedium,
        color = c,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(c.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** 0-3 stars for a best score. */
@Composable
fun Stars(best: Int, modifier: Modifier = Modifier) {
    val n = when {
        best >= 90 -> 3
        best >= 70 -> 2
        best >= 40 -> 1
        else -> 0
    }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until 3) {
            Icon(
                if (i < n) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = null,
                tint = if (i < n) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
