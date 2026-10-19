package com.mkulikov.taskb.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

internal val LightColors = lightColors(
    primary = Palette.LightBlue.s500,
    primaryVariant = Palette.LightBlue.s700,
    secondary = Palette.Orange.s700,
    secondaryVariant = Palette.Orange.s900,
    background = Palette.Grey.s50,
    surface = Color.White,
    error = Palette.Red.s700,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Palette.Grey.s900,
    onSurface = Palette.Grey.s900,
    onError = Color.White,
)

internal val DarkColors = darkColors(
    primary = Palette.LightBlue.s500,
    primaryVariant = Palette.LightBlue.s700,
    secondary = Palette.Orange.s700,
    secondaryVariant = Palette.Orange.s900,
    background = Palette.Grey.s900,
    surface = Palette.Grey.s900,
    error = Palette.Red.s300,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black,
)
