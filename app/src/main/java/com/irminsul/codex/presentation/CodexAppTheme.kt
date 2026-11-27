package com.irminsul.codex.presentation

import android.view.View
import android.view.Window
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun CodexAppTheme(
    useDarkTheme: Boolean,
    window: Window,
    view: View,
    content: @Composable () -> Unit
) {
    CodexSystemBars(
        useDarkTheme = useDarkTheme,
        window = window,
        view = view
    )

    MaterialTheme(
        colorScheme = if (useDarkTheme) CodexDarkColors else CodexLightColors,
        typography = CodexTypography,
        shapes = CodexShapes
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}
