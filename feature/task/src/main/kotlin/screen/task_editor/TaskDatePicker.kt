package com.mkulikov.taskb.feature.task.screen.task_editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mkulikov.taskb.ui.icon.TaskBIcons
import com.mkulikov.taskb.ui.theme.TaskBTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun TaskDatePicker(date: LocalDate?, today: LocalDate, onChoose: (LocalDate) -> Unit, onDismiss: () -> Unit, availableHeight: Dp) {
    var month by remember { mutableStateOf(YearMonth.from(date ?: today)) }
    Dialog(onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, scrimColor = TaskBTheme.colors.scrim)) {
        Surface(shape = RoundedCornerShape(10.dp), color = TaskBTheme.colors.surface, elevation = 4.dp) {
            Column(Modifier.width(360.dp).heightIn(max = (availableHeight - 48.dp).coerceAtLeast(160.dp)).verticalScroll(rememberScrollState()).padding(20.dp)) {
                Text("Choose date", style = TaskBTheme.typography.title, color = TaskBTheme.colors.heading)
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton({ month = month.minusMonths(1) }) { Icon(TaskBIcons.ChevronLeft, "Previous month", tint = TaskBTheme.colors.muted) }
                    Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)), style = TaskBTheme.typography.section, color = TaskBTheme.colors.text, modifier = Modifier.weight(1f).wrapContentWidth())
                    IconButton({ month = month.plusMonths(1) }) { Icon(TaskBIcons.ChevronRight, "Next month", tint = TaskBTheme.colors.muted) }
                }
                Row(Modifier.fillMaxWidth()) { listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { Text(it, color = TaskBTheme.colors.muted, style = TaskBTheme.typography.small, modifier = Modifier.weight(1f).wrapContentWidth()) } }
                val offset = month.atDay(1).dayOfWeek.value - 1
                repeat((offset + month.lengthOfMonth() + 6) / 7) { week ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { day ->
                            val number = week * 7 + day - offset + 1
                            Box(Modifier.weight(1f).height(42.dp), contentAlignment = Alignment.Center) {
                                if (number in 1..month.lengthOfMonth()) {
                                    val value = month.atDay(number)
                                    val chosen = value == date
                                    Box(Modifier.size(36.dp).background(if (chosen) TaskBTheme.colors.selected else TaskBTheme.colors.surface, CircleShape)
                                        .then(if (value == today) Modifier.border(1.dp, TaskBTheme.colors.accent, CircleShape) else Modifier)
                                        .clickable(role = Role.Button) { onChoose(value) }.semantics { selected = chosen; contentDescription = "Choose ${value.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH))}" }, contentAlignment = Alignment.Center) {
                                        Text(number.toString(), color = if (chosen) TaskBTheme.colors.accent else TaskBTheme.colors.text, style = TaskBTheme.typography.body)
                                    }
                                }
                            }
                        }
                    }
                }
                TextButton(onDismiss, Modifier.align(Alignment.End)) { Text("Cancel") }
            }
        }
    }
}
