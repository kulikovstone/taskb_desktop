package com.mkulikov.taskb.feature.task.component

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun taskDateLabel(date: LocalDate, today: LocalDate, includeDate: Boolean = false): String {
    val relative = when (date) {
        today.minusDays(1) -> "Yesterday"
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> null
    }
    val pattern = if (date.year != today.year) "MMM d, yyyy" else "MMM d"
    val formatted = date.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
    return if (relative != null) if (includeDate) "$relative, $formatted" else relative
    else if (includeDate || date.year != today.year) formatted
    else date.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH))
}
