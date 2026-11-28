package com.irminsul.codex.presentation

import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp

/** Native vertical wheel picker with child focus blocked to prevent a software keyboard. */
@Composable
internal fun WheelNumberPicker(label: String, value: Int, minimum: Int, maximum: Int, modifier: Modifier = Modifier, onValueChange: (Int) -> Unit) {
    val safeMinimum = minimum.coerceAtLeast(0)
    val safeMaximum = maximum.coerceAtLeast(safeMinimum)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        AndroidView(
            modifier = Modifier.width(88.dp).height(104.dp),
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = safeMinimum
                    maxValue = safeMaximum
                    this.value = value.coerceIn(safeMinimum, safeMaximum)
                    wrapSelectorWheel = false
                    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    setOnValueChangedListener { _, _, next -> onValueChange(next) }
                }
            },
            update = { picker ->
                picker.setOnValueChangedListener(null)
                picker.minValue = safeMinimum
                picker.maxValue = safeMaximum
                picker.value = value.coerceIn(safeMinimum, safeMaximum)
                picker.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                picker.setOnValueChangedListener { _, _, next -> onValueChange(next) }
            }
        )
    }
}
