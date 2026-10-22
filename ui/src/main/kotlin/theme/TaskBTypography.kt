@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.mkulikov.taskb.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object TaskBTypography {
    private val family = FontFamily("Noto Sans")
    val small = TextStyle(
        fontFamily = family,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    )
    val label = small.copy(fontWeight = FontWeight.Medium)
    val body = small.copy(fontSize = 14.sp, lineHeight = 22.sp)
    val button = body.copy(fontWeight = FontWeight.Medium, lineHeight = 20.sp)
    val section = body.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium)
    val title = body.copy(fontSize = 22.sp, lineHeight = 28.sp)
    val display = body.copy(fontSize = 32.sp, lineHeight = 44.sp)
    val number = body.copy(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp)

    internal val material = Typography(
        defaultFontFamily = family,
        h4 = display,
        h5 = title,
        h6 = title,
        subtitle1 = section,
        subtitle2 = label,
        body1 = body,
        body2 = body,
        caption = small,
        button = button,
        overline = label,
    )
}
