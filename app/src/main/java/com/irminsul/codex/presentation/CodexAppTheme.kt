package com.irminsul.codex.presentation

import android.view.View
import android.view.Window
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable

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
        colorScheme = if (useDarkTheme) CodexDarkColors else CodexLightColors
    ) {
        Surface(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}
