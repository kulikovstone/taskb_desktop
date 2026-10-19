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
    CompositionLocalProvider(
        LocalCustomColors provides if (darkTheme) DarkCustomColors else LightCustomColors,
    ) {
        MaterialTheme(
            colors = if (darkTheme) DarkColors else LightColors,
            typography = TaskBTypography,
            content = content,
        )
    }
}

object TaskBTheme {
    val customColors: CustomColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomColors.current
}
