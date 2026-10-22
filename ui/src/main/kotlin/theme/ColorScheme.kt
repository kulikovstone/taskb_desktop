package com.mkulikov.taskb.ui.theme

import androidx.compose.material.Colors

internal fun TaskBColors.toMaterialColors(darkTheme: Boolean) = Colors(
    primary = accent,
    primaryVariant = actionFill,
    secondary = success,
    secondaryVariant = success,
    background = surface,
    surface = surface,
    error = danger,
    onPrimary = if (darkTheme) surface else onAction,
    onSecondary = if (darkTheme) surface else onAction,
    onBackground = heading,
    onSurface = text,
    onError = if (darkTheme) surface else onAction,
    isLight = !darkTheme,
)
