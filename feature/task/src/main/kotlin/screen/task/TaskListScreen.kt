package com.mkulikov.taskb.feature.task.screen.task

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.task.component.GroupButton
import com.mkulikov.taskb.feature.task.component.GroupTextField
import com.mkulikov.taskb.feature.task.screen.task_editor.TaskEditorIntent
import com.mkulikov.taskb.feature.task.screen.task_editor.TaskEditorState
import com.mkulikov.taskb.feature.task.screen.task_editor.TaskEditorWidePanel
import com.mkulikov.taskb.ui.component.TaskBTopAppBar
import com.mkulikov.taskb.ui.icon.TaskBIcons
import com.mkulikov.taskb.ui.theme.TaskBTheme

@Composable
internal fun TaskListScreen(
    state: TaskState, onIntent: (TaskIntent) -> Unit,
    editor: TaskEditorState?, onEditorIntent: (TaskEditorIntent) -> Unit,
    wide: Boolean, onBack: () -> Unit,
) {
    val enabled = editor?.let { !it.isBusy && it.dialog == null } ?: true
    val actions: @Composable RowScope.() -> Unit = {
        IconButton({ onIntent(TaskIntent.Choose(TaskSelection.Groups)) }, enabled = enabled) {
            Icon(TaskBIcons.Folder, "Choose group", tint = TaskBTheme.colors.accent)
        }
        IconButton({ onIntent(TaskIntent.Choose(TaskSelection.Filters)) }, enabled = enabled) {
            Icon(TaskBIcons.Filter, "Choose task filter", tint = TaskBTheme.colors.accent)
        }
        if (wide) {
            Spacer(Modifier.width(16.dp))
            GroupButton("New task", TaskBIcons.Add, { onIntent(TaskIntent.NewTask) }, enabled = enabled)
        } else IconButton({ onIntent(TaskIntent.NewTask) }, enabled = enabled) { Icon(TaskBIcons.Add, "Add task", tint = TaskBTheme.colors.accent) }
    }
    Column(Modifier.fillMaxSize()) {
        if (wide) Row(Modifier.fillMaxWidth().height(96.dp).padding(start = 16.dp, end = 32.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack, enabled = enabled) { Icon(TaskBIcons.Back, "Back", tint = TaskBTheme.colors.heading) }
            Spacer(Modifier.width(8.dp))
            Text("Tasks", style = TaskBTheme.typography.display, color = TaskBTheme.colors.heading)
            Spacer(Modifier.weight(1f))
            actions()
        } else TaskBTopAppBar("Tasks", onBack, titleSpacing = 4.dp, actions = actions)
        Column(Modifier.weight(1f).padding(start = if (wide) 32.dp else 24.dp, end = if (wide) 32.dp else 24.dp, top = if (wide) 16.dp else 24.dp)) {
            if (wide) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                GroupTextField(state.query, { onIntent(TaskIntent.Search(it)) }, "Search tasks", placeholder = "Search tasks", search = true, modifier = Modifier.width(300.dp))
                ActiveTaskFilters(state, onIntent, Modifier.weight(1f))
            } else {
                GroupTextField(state.query, { onIntent(TaskIntent.Search(it)) }, "Search tasks", placeholder = "Search tasks", search = true, modifier = Modifier.fillMaxWidth())
                if (state.filter != null || state.groupFilter != TaskGroupFilter.All) {
                    Spacer(Modifier.height(16.dp))
                    ActiveTaskFilters(state, onIntent)
                }
            }
            Spacer(Modifier.height(if (wide) 24.dp else 16.dp))
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                TaskDirectory(state, onIntent, wide, enabled, Modifier.weight(1f).fillMaxHeight())
                if (wide && editor != null) TaskEditorWidePanel(editor, state.groups, state.today, onEditorIntent,
                    onChooseGroup = { onIntent(TaskIntent.Choose(TaskSelection.EditorGroup)) },
                    onClose = { onIntent(TaskIntent.CloseEditor) }, modifier = Modifier.width(348.dp).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun ActiveTaskFilters(state: TaskState, onIntent: (TaskIntent) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.groupFilter != TaskGroupFilter.All) {
            val group = state.groups.find { it.id == (state.groupFilter as? TaskGroupFilter.Selected)?.id }
            TaskFilterChip(group?.name ?: "No group", TaskBIcons.FolderFilled, group?.color?.color ?: TaskBTheme.colors.muted,
                { onIntent(TaskIntent.Choose(TaskSelection.Groups)) }, { onIntent(TaskIntent.FilterGroup(TaskGroupFilter.All)) }, "Clear group filter")
        }
        state.filter?.let { TaskFilterChip(it.label, TaskBIcons.Filter, TaskBTheme.colors.accent,
            { onIntent(TaskIntent.Choose(TaskSelection.Filters)) }, { onIntent(TaskIntent.Filter(null)) }, "Clear task filter") }
    }
}

@Composable
private fun TaskFilterChip(label: String, icon: ImageVector, tint: Color, onChange: () -> Unit, onClear: () -> Unit, clearDescription: String) {
    Row(Modifier.height(44.dp).background(TaskBTheme.colors.selected, RoundedCornerShape(8.dp)), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.height(44.dp).clickable(onClick = onChange).padding(start = 12.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            Text(label, color = TaskBTheme.colors.accent, style = TaskBTheme.typography.button)
        }
        IconButton(onClear, Modifier.size(44.dp)) { Icon(TaskBIcons.Clear, clearDescription, tint = TaskBTheme.colors.accent, modifier = Modifier.size(24.dp)) }
    }
}
