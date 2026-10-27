package com.mkulikov.taskb.feature.task.screen.task_editor

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.task.component.GroupLoadError
import com.mkulikov.taskb.feature.task.component.GroupLoading
import com.mkulikov.taskb.feature.task.model.Group
import com.mkulikov.taskb.ui.component.TaskBTopAppBar
import com.mkulikov.taskb.ui.icon.TaskBIcons
import com.mkulikov.taskb.ui.theme.TaskBTheme
import java.time.LocalDate

@Composable
internal fun TaskEditorCompactScreen(state: TaskEditorState, groups: List<Group>, today: LocalDate, onIntent: (TaskEditorIntent) -> Unit, onChooseGroup: () -> Unit, onClose: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TaskBTopAppBar(if (state.isNew) "New task" else "Task", onClose, backEnabled = !state.isBusy, titleSpacing = 4.dp, actions = {
            if (!state.isNew) IconButton({ onIntent(TaskEditorIntent.PrepareDelete) }, enabled = !state.isBusy && state.original != null) {
                Icon(TaskBIcons.Delete, "Delete task", tint = TaskBTheme.colors.danger)
            }
            IconButton({ onIntent(TaskEditorIntent.Save) }, enabled = state.canSave) {
                Icon(TaskBIcons.Done, "Save task", tint = if (state.canSave) TaskBTheme.colors.accent else TaskBTheme.colors.border)
            }
        })
        TaskEditorBody(state, groups, today, onIntent, onChooseGroup, false, Modifier.weight(1f).fillMaxWidth())
    }
}

@Composable
internal fun TaskEditorWidePanel(state: TaskEditorState, groups: List<Group>, today: LocalDate, onIntent: (TaskEditorIntent) -> Unit, onChooseGroup: () -> Unit, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (state.isNew) "New task" else "Task", color = TaskBTheme.colors.text, style = TaskBTheme.typography.title, modifier = Modifier.weight(1f))
            IconButton(onClose, enabled = !state.isBusy, modifier = Modifier.size(24.dp)) { Icon(TaskBIcons.Clear, "Close task", tint = TaskBTheme.colors.muted) }
        }
        TaskEditorBody(state, groups, today, onIntent, onChooseGroup, true, Modifier.weight(1f).fillMaxWidth())
    }
}

@Composable
private fun TaskEditorBody(state: TaskEditorState, groups: List<Group>, today: LocalDate, onIntent: (TaskEditorIntent) -> Unit, onChooseGroup: () -> Unit, wide: Boolean, modifier: Modifier) {
    when {
        state.isLoading -> GroupLoading(modifier)
        state.loadError != null -> GroupLoadError(state.loadError, { onIntent(TaskEditorIntent.RetryLoad) }, modifier)
        else -> TaskEditorForm(state, groups, today, onIntent, onChooseGroup, wide, modifier)
    }
}
