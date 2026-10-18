package app.harmonicaplayalong.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.harmonicaplayalong.data.ThemeChoice
import app.harmonicaplayalong.music.Breath
import app.harmonicaplayalong.music.HarpNote
import app.harmonicaplayalong.music.Technique

/** Colours that mean something on the harmonica: blow, draw, bends, hits and misses, and the instrument itself. */
@Immutable
data class HarpPalette(
    val blow: Color,
    val draw: Color,
    val bend: Color,
    val hit: Color,
    val miss: Color,
    val laneA: Color,
    val laneB: Color,
    val chromeLight: Color,
    val chromeDark: Color,
    val comb: Color,
    val hole: Color,
    val engraving: Color,
) {
    fun forNote(n: HarpNote?): Color = when {
        n == null -> miss
        n.breath == Breath.BLOW -> blow
        else -> draw
    }

    fun isBent(n: HarpNote?): Boolean = n != null && n.technique != Technique.NATURAL
}

private val DarkPalette = HarpPalette(
    blow = Color(0xFF5AA9FF),
    draw = Color(0xFFFFA24C),
    bend = Color(0xFFFF5D8F),
    hit = Color(0xFF4ADE80),
    miss = Color(0xFFF87171),
    laneA = Color(0xFF111620),
    laneB = Color(0xFF151B26),
    chromeLight = Color(0xFFE3E8EE),
    chromeDark = Color(0xFF8C97A6),
    comb = Color(0xFF2B2F36),
    hole = Color(0xFF07090C),
    engraving = Color(0xFF3B4350),
)

private val LightPalette = HarpPalette(
    blow = Color(0xFF1D6FD6),
    draw = Color(0xFFD9670B),
    bend = Color(0xFFD02F67),
    hit = Color(0xFF16A34A),
    miss = Color(0xFFDC2626),
    laneA = Color(0xFFF3EDE2),
    laneB = Color(0xFFEDE5D6),
    chromeLight = Color(0xFFF4F6F8),
    chromeDark = Color(0xFF9CA6B3),
    comb = Color(0xFF3A3F47),
    hole = Color(0xFF101318),
    engraving = Color(0xFF5B6574),
)

val LocalHarpPalette = staticCompositionLocalOf { DarkPalette }

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFE2B65C),
    onPrimary = Color(0xFF2B1D00),
    primaryContainer = Color(0xFF4A3810),
    onPrimaryContainer = Color(0xFFFFE2A8),
    secondary = Color(0xFF9FC2FF),
    onSecondary = Color(0xFF0B2550),
    secondaryContainer = Color(0xFF1F3557),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = Color(0xFFFFB38A),
    onTertiary = Color(0xFF4A1E00),
    background = Color(0xFF0D1016),
    onBackground = Color(0xFFE9E5DC),
    surface = Color(0xFF0D1016),
    onSurface = Color(0xFFE9E5DC),
    surfaceVariant = Color(0xFF232A36),
    onSurfaceVariant = Color(0xFFBAB5AA),
    surfaceContainerLowest = Color(0xFF090C10),
    surfaceContainerLow = Color(0xFF12161E),
    surfaceContainer = Color(0xFF161B24),
    surfaceContainerHigh = Color(0xFF1C222D),
    surfaceContainerHighest = Color(0xFF232A36),
    outline = Color(0xFF4A5262),
    outlineVariant = Color(0xFF2C3340),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF8A6214),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDEA0),
    onPrimaryContainer = Color(0xFF2B1D00),
    secondary = Color(0xFF2F5DA8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD8E2FF),
    onSecondaryContainer = Color(0xFF001A43),
    tertiary = Color(0xFFA0461A),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFAF6EE),
    onBackground = Color(0xFF1E1B16),
    surface = Color(0xFFFAF6EE),
    onSurface = Color(0xFF1E1B16),
    surfaceVariant = Color(0xFFEFE8DA),
    onSurfaceVariant = Color(0xFF4E473B),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6F1E7),
    surfaceContainer = Color(0xFFF1EBDF),
    surfaceContainerHigh = Color(0xFFECE5D8),
    surfaceContainerHighest = Color(0xFFE6DED0),
    outline = Color(0xFF807667),
    outlineVariant = Color(0xFFD5CCBB),
)

private val Serif = FontFamily.Serif

private val AppTypography = Typography().let { t ->
    t.copy(
        displaySmall = t.displaySmall.copy(fontFamily = Serif, fontWeight = FontWeight.SemiBold),
        headlineMedium = t.headlineMedium.copy(fontFamily = Serif, fontWeight = FontWeight.SemiBold),
        headlineSmall = t.headlineSmall.copy(fontFamily = Serif, fontWeight = FontWeight.SemiBold),
        titleLarge = t.titleLarge.copy(fontFamily = Serif, fontWeight = FontWeight.SemiBold),
        labelSmall = t.labelSmall.copy(letterSpacing = 1.2.sp),
    )
}

/** Monospaced-looking tab digits, so columns of tab line up. */
val TabTextStyle = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)

@Composable
fun isDark(choice: ThemeChoice): Boolean = when (choice) {
    ThemeChoice.SYSTEM -> isSystemInDarkTheme()
    ThemeChoice.LIGHT -> false
    ThemeChoice.DARK -> true
}

@Composable
fun HarmonicaTheme(dark: Boolean, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalHarpPalette provides if (dark) DarkPalette else LightPalette) {
        MaterialTheme(
            colorScheme = if (dark) DarkScheme else LightScheme,
            typography = AppTypography,
            content = content,
        )
    }
}
