package app.katib.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.katib.R

val Plex = FontFamily(
    Font(R.font.plex_arabic_regular, FontWeight.Normal),
    Font(R.font.plex_arabic_medium, FontWeight.Medium),
    Font(R.font.plex_arabic_semibold, FontWeight.SemiBold),
    Font(R.font.plex_arabic_bold, FontWeight.Bold),
)
val PlexMono = FontFamily(Font(R.font.plex_mono_regular))

@Immutable
data class KatibColors(
    val paper: Color, val surface: Color, val ink: Color, val muted: Color, val line: Color,
    val accent: Color, val onAccent: Color, val rec: Color, val ok: Color,
    val speakers: List<Color>,
)

// Calm editorial palette: warm paper + ink, one deep-teal accent, signal orange only for "recording".
private val Light = KatibColors(
    paper = Color(0xFFF6F3EE), surface = Color(0xFFEDE8E0), ink = Color(0xFF1A1A18),
    muted = Color(0xFF5F5B54), line = Color(0xFFD8D1C6),
    accent = Color(0xFF0F5E5A), onAccent = Color(0xFFF6F3EE), rec = Color(0xFFC2410C), ok = Color(0xFF2F6B3A),
    // Okabe-Ito derived, darkened for text contrast on paper.
    speakers = listOf(Color(0xFF0072B2), Color(0xFFB35900), Color(0xFF00785A), Color(0xFFAA3377),
        Color(0xFF6B5B00), Color(0xFF4B4B9E), Color(0xFF8A4F00), Color(0xFF3A6F8F)),
)
private val Dark = KatibColors(
    paper = Color(0xFF141413), surface = Color(0xFF1F1E1C), ink = Color(0xFFEDEAE4),
    muted = Color(0xFFA29C92), line = Color(0xFF34322E),
    accent = Color(0xFF5FB3AC), onAccent = Color(0xFF0C1F1E), rec = Color(0xFFFF8A4C), ok = Color(0xFF7BC489),
    speakers = listOf(Color(0xFF56B4E9), Color(0xFFE69F00), Color(0xFF3CC7A0), Color(0xFFEE88BB),
        Color(0xFFE6D74C), Color(0xFF9F9FE8), Color(0xFFF0B060), Color(0xFF8CC4E6)),
)

val LocalKatib = staticCompositionLocalOf { Light }

object E { val c: KatibColors @Composable get() = LocalKatib.current }

@Composable
fun KatibTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val c = if (dark) Dark else Light
    val scheme = (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = c.accent, onPrimary = c.onAccent, background = c.paper, onBackground = c.ink,
        surface = c.paper, onSurface = c.ink, surfaceVariant = c.surface, onSurfaceVariant = c.muted,
        surfaceContainer = c.surface, surfaceContainerHigh = c.surface, surfaceContainerLow = c.paper,
        surfaceContainerHighest = c.surface,
        outline = c.line, outlineVariant = c.line, secondaryContainer = c.surface, onSecondaryContainer = c.ink,
        error = c.rec, primaryContainer = c.surface, onPrimaryContainer = c.ink,
    )
    val base = Typography()
    fun TextStyle.p() = copy(fontFamily = Plex)
    val type = Typography(
        displaySmall = base.displaySmall.p().copy(fontWeight = FontWeight.SemiBold, fontSize = 34.sp),
        headlineSmall = base.headlineSmall.p().copy(fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.p().copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.p().copy(fontWeight = FontWeight.Medium),
        titleSmall = base.titleSmall.p().copy(fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.p().copy(fontSize = 18.sp, lineHeight = 28.sp),
        bodyMedium = base.bodyMedium.p().copy(fontSize = 15.sp, lineHeight = 22.sp),
        bodySmall = base.bodySmall.p(),
        labelLarge = base.labelLarge.p().copy(fontWeight = FontWeight.Medium),
        labelMedium = base.labelMedium.p(),
        labelSmall = base.labelSmall.p(),
    )
    androidx.compose.runtime.CompositionLocalProvider(LocalKatib provides c) {
        MaterialTheme(colorScheme = scheme, typography = type, content = content)
    }
}
