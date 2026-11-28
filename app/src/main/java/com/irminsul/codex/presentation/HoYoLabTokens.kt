package com.irminsul.codex.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * HoYoLAB WebView design tokens transcribed from docs/m_55ae43ba.css and the
 * five HTML extracts in docs/.
 *
 * Approved conversion policy: 1 --gpx = 1.dp for layout dimensions and
 * 1 --gpx = 1.sp for typography. Each value below is the original --gpx
 * coefficient under that policy. If the app later adopts another scale,
 * change this layer rather than individual screen values.
 *
 * Tokens marked "shared/inferred" in the companion reference are not claims
 * about calculator sub-screens absent from enhancement-calculator-body.html.
 */
internal object HoYoLabSpacing {
    val hairline: Dp = 0.5.dp // tw-h-[0.5px]
    val hairlinePx: Dp = 1.dp // tw-h-[1px], tw-w-px
    val xxs: Dp = 2.dp // tw-mx-0.5, tw-rounded-[2px]
    val xs: Dp = 4.dp // tw-space-y-1, tw-mb-1
    val sm: Dp = 6.dp // tw-py-1.5
    val md: Dp = 8.dp // tw-gap-2, tw-px-2, tw-space-y-2
    val mdPlus: Dp = 10.dp // tw-py-2.5; navigation vertical inset
    val lg: Dp = 12.dp // tw-px-3, tw-gap-x-3
    val lgPlus: Dp = 14.dp // tw-mr-3.5
    val pageInset: Dp = 16.dp // tw-px-4; navigation horizontal inset
    val xl: Dp = 20.dp // tw-pl-5
    val xxl: Dp = 24.dp // tw-pb-6
    val xxxl: Dp = 32.dp // tw-h-8 / tw-w-8

    val navigationVertical: Dp = 10.dp // .container[data-v-4b6b351e]
    val navigationHorizontal: Dp = 16.dp // .container[data-v-4b6b351e]
    val skillInfoBottom: Dp = 10.dp // .mhy-skill-dialog .container .info
    val runeDialogPadding: Dp = 15.dp // .mhy-rune-dialog .container[data-v-043f08a2]
    val buffDescriptionTop: Dp = 9.dp // .buff-dialog .container .desc-container
    val dialogSectionGap: Dp = 10.dp // .mhy-skill-dialog .container .info
}

internal object HoYoLabRadius {
    val small: Dp = 4.dp // tw-rounded
    val medium: Dp = 6.dp // tw-rounded-md
    val large: Dp = 8.dp // tw-rounded-lg
    val extraLarge: Dp = 10.dp // tw-rounded-[10px]
    val card: Dp = 16.dp // tw-rounded-2xl
    val pill: Dp = 9999.dp // tw-rounded-full
    val scrollbarThumb: Dp = 2.dp // .mhy-rune-dialog ... ::-webkit-scrollbar-thumb
}

internal object HoYoLabType {
    val pageTitle = TextStyle(
        fontSize = 16.sp, // .title[data-v-4b6b351e]
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp // body[data-game-id="2"] .title[data-v-4b6b351e]
    )

    val body = TextStyle(
        fontSize = 16.sp, // tw-text-base-m
        lineHeight = 20.sp
    )

    val bodySmall = TextStyle(
        fontSize = 14.sp, // tw-text-sm-m
        lineHeight = 18.sp
    )

    val compact = TextStyle(
        fontSize = 12.sp, // tw-text-xs-m / tw-text-xs
        lineHeight = 16.sp
    )

    val caption = TextStyle(
        fontSize = 10.sp, // tw-text-xxs-m
        lineHeight = 12.sp
    )

    val microLabel = TextStyle(
        fontSize = 9.sp, // tw-text-[9px]
        lineHeight = 10.sp // tw-leading-[10px]
    )

    val tinyLabel = TextStyle(
        fontSize = 8.sp // tw-text-[8px]; no line-height in this utility
    )

    val sectionTitle = TextStyle(
        fontSize = 18.sp // tw-text-[18px]; line-height is contextual
    )

    val numericEmphasis = TextStyle(
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold
    )

    val regularWeight: FontWeight = FontWeight.Normal // tw-font-normal = 400
    val mediumWeight: FontWeight = FontWeight.Medium // tw-font-medium = 500
    val semiboldWeight: FontWeight = FontWeight.SemiBold // tw-font-semibold = 600
}

internal object HoYoLabIconSize {
    val navigation: Dp = 24.dp // .ic[data-v-4b6b351e]
    val avatar: Dp = 28.dp // tw-w-7 / tw-h-7 in Chronicle fragments
    val small: Dp = 16.dp // tw-w-4 / tw-h-4
    val compact: Dp = 20.dp // tw-w-5 / tw-h-5
    val skillBacking: Dp = 48.dp // .mhy-skill-dialog .container .info .skill-bg
    val skillGlyph: Dp = 34.dp // .mhy-skill-dialog ... .skill-bg .icon
    val runeLogoWidth: Dp = 48.dp // .mhy-rune-dialog .container .logo
    val runeLogoHeight: Dp = 92.dp // .mhy-rune-dialog .container .logo
}

internal object HoYoLabSurface {
    val brandGold = Color(0xFFD3BC8E) // body[data-game-id="2"] .title; tw-ys-l-brand-yellow-3
    val parchmentText = Color(0xFFF0EAE0) // tw-text-ys-l-grey-white-2
    val mutedParchment = Color(0xFFCBBDA7) // tw-text-ys-l-grey-white-5
    val white90 = Color(0xE6FFFFFF) // tw-text-gt-l-text-white-2
    val white65 = Color(0xA6FFFFFF) // tw-text-gt-l-text-white-3
    val white64 = Color(0xA3FFFFFF) // tw-bg-gt-g-grey-white-9
    val white16 = Color(0x29FFFFFF) // tw-bg-gt-g-grey-white-4
    val white08 = Color(0x14FFFFFF) // tw-bg-gt-g-grey-white-6
    val black32 = Color(0x52000000) // tw-bg-gt-g-grey-black-3

    val borderWhite32 = Color(0x52FFFFFF) // tw-border-gt-g-grey-white-3
    val borderWhite16 = Color(0x29FFFFFF) // tw-border-gt-g-grey-white-4
    val borderWhite12 = Color(0x1FFFFFFF) // tw-border-gt-g-grey-white-5
    val borderWhite08 = Color(0x14FFFFFF) // tw-border-gt-g-grey-white-6
    val borderBlack08 = Color(0x14000000) // tw-border-gt-g-grey-black-6

    val dialogTitle = Color(0xFF333333) // .mhy-skill-dialog .container .info .name
    val dialogDescription = Color(0xFF7F858A) // .mhy-skill-dialog .container .desc
    val buffTitle = Color(0xFF1E1F20) // .buff-dialog .container .info .name
    val buffDescription = Color(0xFF7F858A) // .buff-dialog .container .desc
}
