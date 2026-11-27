package com.irminsul.codex.presentation

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Application-level Material 3 palettes.
 *
 * Keep game identity colors separate from Material status colors and app identity.
 * Feature screens should consume theme roles or the semantic palettes below rather
 * than defining their own raw color values.
 */
internal val CodexDarkColors = darkColorScheme(
    primary = Color(0xFFD3BC8E),
    onPrimary = Color(0xFF1A1814),
    primaryContainer = Color(0xFF4B402E),
    onPrimaryContainer = Color(0xFFF2E5C9),
    secondary = Color(0xFFC8B99A),
    onSecondary = Color(0xFF292317),
    secondaryContainer = Color(0xFF443B2A),
    onSecondaryContainer = Color(0xFFEDE0C4),
    tertiary = Color(0xFFB8C6D8),
    onTertiary = Color(0xFF1D2631),
    background = Color(0xFF111216),
    onBackground = Color(0xFFE8E6E1),
    surface = Color(0xFF191A20),
    onSurface = Color(0xFFE8E6E1),
    surfaceVariant = Color(0xFF25262D),
    onSurfaceVariant = Color(0xFFC2BFB7),
    outline = Color(0xFF8F897D),
    outlineVariant = Color(0xFF49463F),
    inverseSurface = Color(0xFFE8E6E1),
    inverseOnSurface = Color(0xFF292A2F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

internal val CodexLightColors = lightColorScheme(
    primary = Color(0xFF8E6E35),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF1E4C9),
    onPrimaryContainer = Color(0xFF30230C),
    secondary = Color(0xFF695B40),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF0E2C6),
    onSecondaryContainer = Color(0xFF251A07),
    tertiary = Color(0xFF526579),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF8F7F3),
    onBackground = Color(0xFF1C1B18),
    surface = Color(0xFFFFFEFA),
    onSurface = Color(0xFF1C1B18),
    surfaceVariant = Color(0xFFECE9E1),
    onSurfaceVariant = Color(0xFF514E47),
    outline = Color(0xFF81796B),
    outlineVariant = Color(0xFFD0C9BB),
    inverseSurface = Color(0xFF30302D),
    inverseOnSurface = Color(0xFFF5F1E8),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

/** Contextual Genshin element colors. Use only when element identity conveys information. */
internal data class CodexElementPalette(
    val pyro: Color,
    val hydro: Color,
    val anemo: Color,
    val electro: Color,
    val dendro: Color,
    val cryo: Color,
    val geo: Color
)

internal val CodexDarkElementColors = CodexElementPalette(
    pyro = Color(0xFFFF8068),
    hydro = Color(0xFF75B7FF),
    anemo = Color(0xFF83D6B5),
    electro = Color(0xFFC4A0FF),
    dendro = Color(0xFFA9D96B),
    cryo = Color(0xFF9DE5F2),
    geo = Color(0xFFFFD078)
)

internal val CodexLightElementColors = CodexElementPalette(
    pyro = Color(0xFFB94732),
    hydro = Color(0xFF236DB5),
    anemo = Color(0xFF287D5B),
    electro = Color(0xFF7950B5),
    dendro = Color(0xFF527F20),
    cryo = Color(0xFF267D91),
    geo = Color(0xFF94620C)
)

/** Character rarity colors are semantic game identity, not app status colors. */
internal data class CodexRarityPalette(
    val fourStar: Color,
    val fiveStar: Color
)

internal val CodexDarkRarityColors = CodexRarityPalette(
    fourStar = Color(0xFFC4A0FF),
    fiveStar = Color(0xFFFFD078)
)

internal val CodexLightRarityColors = CodexRarityPalette(
    fourStar = Color(0xFF7950B5),
    fiveStar = Color(0xFF94620C)
)
