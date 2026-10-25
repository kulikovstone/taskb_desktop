package com.mkulikov.taskb.feature.task.screen.group_editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.ui.component.TaskBTopAppBar
import com.mkulikov.taskb.ui.icon.TaskBIcons
import com.mkulikov.taskb.ui.theme.TaskBTheme

@Composable
internal fun GroupEditorCompactScreen(state: GroupEditorState, onIntent: (GroupEditorIntent) -> Unit, onBack: () -> Unit) {
    val colors = TaskBTheme.colors
    val canSave = state.canSave && state.dialog == null
    val canDelete = state.original != null && !state.isBusy && !state.isFinished && state.dialog == null
    Column(Modifier.fillMaxSize()) {
        TaskBTopAppBar(
            title = if (state.isNew) "New group" else "Group", onBack = onBack,
            backEnabled = !state.isSaving && !state.isDeleting,
            titleSpacing = 4.dp,
        ) {
            if (!state.isNew) {
                IconButton(onClick = { onIntent(GroupEditorIntent.PrepareDelete) }, enabled = canDelete) {
                    Icon(
                        TaskBIcons.Delete, contentDescription = "Delete group",
                        tint = if (canDelete) colors.danger else colors.muted.copy(alpha = 0.5f),
                    )
                }
            }
            IconButton(onClick = { onIntent(GroupEditorIntent.Save) }, enabled = canSave) {
                Icon(
                    TaskBIcons.Done, contentDescription = "Save group",
                    tint = if (canSave) colors.accent else colors.muted.copy(alpha = 0.5f),
                )
            }
        }
        GroupEditorForm(state, onIntent, modifier = Modifier.weight(1f).padding(horizontal = 24.dp), autoFocusName = state.isNew)
    }
}
