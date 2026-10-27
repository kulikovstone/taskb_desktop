package com.mkulikov.taskb.feature.task.screen.task_editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.task.component.GroupButton
import com.mkulikov.taskb.feature.task.component.GroupTextField
import com.mkulikov.taskb.feature.task.component.taskDateLabel
import com.mkulikov.taskb.feature.task.model.Group
import com.mkulikov.taskb.ui.icon.TaskBIcons
import com.mkulikov.taskb.ui.theme.TaskBTheme
import java.time.LocalDate

@Composable
internal fun TaskEditorForm(state: TaskEditorState, groups: List<Group>, today: LocalDate, onIntent: (TaskEditorIntent) -> Unit, onChooseGroup: () -> Unit, wide: Boolean, modifier: Modifier) {
    val enabled = !state.isBusy && state.dialog == null
    val group = groups.find { it.id == state.groupId }
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = if (wide) 0.dp else 24.dp, vertical = 24.dp)) {
        Text("Task name", style = TaskBTheme.typography.label, color = TaskBTheme.colors.text, modifier = Modifier.height(18.dp))
        Spacer(Modifier.height(8.dp))
        GroupTextField(state.name, { onIntent(TaskEditorIntent.NameChanged(it)) }, "Task name", placeholder = "Enter task name", enabled = enabled,
            autoFocus = true, onDone = { if (state.canSave) onIntent(TaskEditorIntent.Save) }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        Text("Date", style = TaskBTheme.typography.label, color = TaskBTheme.colors.text, modifier = Modifier.height(18.dp))
        Spacer(Modifier.height(8.dp))
        TaskSelectionField(state.dueDate?.let { taskDateLabel(it, today, true) } ?: "Choose date", TaskBIcons.Calendar, TaskBTheme.colors.muted,
            placeholder = state.dueDate == null, description = "Choose date", enabled = enabled,
            onClick = { onIntent(TaskEditorIntent.ChooseDate) }, onClear = state.dueDate?.let { { onIntent(TaskEditorIntent.DateChanged(null)) } }, clearDescription = "Clear date")
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickDate("Today", state.dueDate == today, enabled, { onIntent(TaskEditorIntent.DateChanged(today)) }, Modifier.weight(1f))
            QuickDate("Tomorrow", state.dueDate == today.plusDays(1), enabled, { onIntent(TaskEditorIntent.DateChanged(today.plusDays(1))) }, Modifier.weight(1f))
        }
        if (state.dueDate != null) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickDate("day", false, enabled, { onIntent(TaskEditorIntent.ShiftDate(days = 1)) }, Modifier.weight(1f), true)
                QuickDate("week", false, enabled, { onIntent(TaskEditorIntent.ShiftDate(days = 7)) }, Modifier.weight(1f), true)
                QuickDate("month", false, enabled, { onIntent(TaskEditorIntent.ShiftDate(months = 1)) }, Modifier.weight(1f), true)
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Group", style = TaskBTheme.typography.label, color = TaskBTheme.colors.text, modifier = Modifier.height(18.dp))
        Spacer(Modifier.height(8.dp))
        TaskSelectionField(group?.name ?: "No group", TaskBIcons.FolderFilled, group?.color?.color ?: TaskBTheme.colors.muted,
            placeholder = group == null, description = "Choose task group", enabled = enabled, onClick = onChooseGroup,
            onClear = state.groupId?.let { { onIntent(TaskEditorIntent.GroupChanged(null)) } }, clearDescription = "Clear task group")
        if (wide) {
            Spacer(Modifier.height(48.dp))
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!state.isNew) GroupButton("Delete", TaskBIcons.Delete, { onIntent(TaskEditorIntent.PrepareDelete) }, enabled = enabled, destructive = true)
                GroupButton(if (state.isNew) "Create task" else "Save changes", if (state.isNew) TaskBIcons.Add else TaskBIcons.Done,
                    { onIntent(TaskEditorIntent.Save) }, enabled = state.canSave, modifier = Modifier.semantics { contentDescription = "Save task" })
            }
        }
    }
}

@Composable
private fun TaskSelectionField(label: String, icon: ImageVector, tint: Color, placeholder: Boolean, description: String, enabled: Boolean, onClick: () -> Unit, onClear: (() -> Unit)?, clearDescription: String) {
    Row(Modifier.fillMaxWidth().height(56.dp).border(1.dp, TaskBTheme.colors.border, RoundedCornerShape(8.dp)), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f).fillMaxHeight().clickable(enabled = enabled, role = Role.Button, onClick = onClick).semantics { contentDescription = description }.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
            Text(label, color = if (placeholder) TaskBTheme.colors.muted else TaskBTheme.colors.text, style = TaskBTheme.typography.body, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Icon(TaskBIcons.ChevronRight, null, tint = TaskBTheme.colors.muted, modifier = Modifier.size(18.dp))
        }
        onClear?.let { IconButton(it, enabled = enabled, modifier = Modifier.width(44.dp).fillMaxHeight()) { Icon(TaskBIcons.Clear, clearDescription, tint = TaskBTheme.colors.muted) } }
    }
}

@Composable
private fun QuickDate(label: String, isSelected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier, add: Boolean = false) {
    Row(modifier.height(44.dp).background(if (isSelected) TaskBTheme.colors.selected else TaskBTheme.colors.surface, RoundedCornerShape(8.dp))
        .border(1.dp, if (isSelected) TaskBTheme.colors.accent else TaskBTheme.colors.border, RoundedCornerShape(8.dp))
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick).semantics { selected = isSelected; if (add) contentDescription = "Add one $label" },
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        if (add) { Icon(TaskBIcons.Add, null, tint = TaskBTheme.colors.accent, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
        Text(label, style = TaskBTheme.typography.button, color = TaskBTheme.colors.accent)
    }
}
