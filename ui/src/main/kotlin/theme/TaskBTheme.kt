package com.mkulikov.taskb.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun TaskBTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkTaskBColors else LightTaskBColors
    CompositionLocalProvider(LocalTaskBColors provides colors) {
        MaterialTheme(
            colors = colors.toMaterialColors(darkTheme),
            typography = TaskBTypography.material,
            content = content,
        )
    }
}

object TaskBTheme {
    val colors: TaskBColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTaskBColors.current

    val typography: TaskBTypography
        get() = TaskBTypography
}
