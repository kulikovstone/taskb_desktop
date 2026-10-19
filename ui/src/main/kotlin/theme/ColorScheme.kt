package com.mkulikov.taskb.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

internal val LightColors = lightColors(
    primary = lightBlue500,
    primaryVariant = lightBlue700,
    secondary = orange700,
    secondaryVariant = orange900,
    background = grey50,
    surface = Color.White,
    error = red700,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = grey900,
    onSurface = grey900,
    onError = Color.White,
)

internal val DarkColors = darkColors(
    primary = lightBlue500,
    primaryVariant = lightBlue700,
    secondary = orange700,
    secondaryVariant = orange900,
    background = grey900,
    surface = grey900,
    error = red300,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black,
)
