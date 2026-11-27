package com.irminsul.codex.presentation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

/**
 * Central registry for application/system icons.
 *
 * Genshin character, weapon, element, rarity, region and mode artwork must use
 * the existing game-asset paths instead of this registry.
 */
internal enum class AppIconName(val resourceId: Int) {
    HOME(R.drawable.lucide_ic_house),
    VISUALS(R.drawable.lucide_ic_chart_column_increasing),
    TOOLS(R.drawable.lucide_ic_wrench),
    ACCOUNT(R.drawable.lucide_ic_user_round),
    AI_ASSISTANT(R.drawable.lucide_ic_sparkles),
    BACK(R.drawable.lucide_ic_arrow_left),
    EXPAND_LESS(R.drawable.lucide_ic_chevron_up),
    EXPAND_MORE(R.drawable.lucide_ic_chevron_down)
}

@Composable
internal fun AppIcon(
    name: AppIconName,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Icon(
        painter = painterResource(name.resourceId),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}
