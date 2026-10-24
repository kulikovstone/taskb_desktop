package com.mkulikov.taskb.feature.task.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mkulikov.taskb.ui.theme.Palette

internal enum class GroupColor(val label: String, val color: Color) {
    Red("Red", Palette.Red.s400),
    Pink("Pink", Palette.Pink.s400),
    Purple("Purple", Palette.Purple.s400),
    DeepPurple("Deep purple", Palette.DeepPurple.s400),
    Indigo("Indigo", Palette.Indigo.s400),
    Blue("Blue", Palette.Blue.s400),
    LightBlue("Light blue", Palette.LightBlue.s400),
    Cyan("Cyan", Palette.Cyan.s400),
    Teal("Teal", Palette.Teal.s400),
    Green("Green", Palette.Green.s400),
    LightGreen("Light green", Palette.LightGreen.s400),
    Lime("Lime", Palette.Lime.s400),
    Yellow("Yellow", Palette.Yellow.s400),
    Amber("Amber", Palette.Amber.s400),
    Orange("Orange", Palette.Orange.s400),
    DeepOrange("Deep orange", Palette.DeepOrange.s400),
    Brown("Brown", Palette.Brown.s400),
    Grey("Grey", Palette.Grey.s400),
    BlueGrey("Blue grey", Palette.BlueGrey.s400);

    companion object {
        fun fromArgb(argb: Int): GroupColor = entries.firstOrNull { it.color.toArgb() == argb } ?: Grey
    }
}
