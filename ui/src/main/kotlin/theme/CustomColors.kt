package com.mkulikov.taskb.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColors(
    val tertiary: Color,
    val selectedGroupBackground: Color,
    val groupListBackground: Color,
    val listTitleColor: Color,
)

internal val LightCustomColors = CustomColors(
    tertiary = green400,
    selectedGroupBackground = blue50,
    groupListBackground = grey50,
    listTitleColor = grey500,
)

internal val DarkCustomColors = CustomColors(
    tertiary = green400,
    selectedGroupBackground = blue50,
    groupListBackground = grey100,
    listTitleColor = grey500,
)

internal val LocalCustomColors = staticCompositionLocalOf<CustomColors> {
    error("TaskBTheme is not present")
}
