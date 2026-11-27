package com.irminsul.codex.presentation

import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowInsetsControllerCompat

@Composable
internal fun CodexSystemBars(
    useDarkTheme: Boolean,
    window: Window,
    view: View
) {
    SideEffect {
        val controller = WindowInsetsControllerCompat(window, view)
        controller.isAppearanceLightStatusBars = !useDarkTheme
        controller.isAppearanceLightNavigationBars = !useDarkTheme
    }
}
