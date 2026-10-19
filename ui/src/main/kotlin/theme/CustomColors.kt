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
    tertiary = Palette.Green.s400,
    selectedGroupBackground = Palette.Blue.s50,
    groupListBackground = Palette.Grey.s50,
    listTitleColor = Palette.Grey.s500,
)

internal val DarkCustomColors = CustomColors(
    tertiary = Palette.Green.s400,
    selectedGroupBackground = Palette.Blue.s50,
    groupListBackground = Palette.Grey.s100,
    listTitleColor = Palette.Grey.s500,
)

internal val LocalCustomColors = staticCompositionLocalOf<CustomColors> {
    error("TaskBTheme is not present")
}
