package com.mkulikov.taskb.feature.task.screen.group_editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.task.component.GroupButton
import com.mkulikov.taskb.ui.icon.TaskBIcons
import com.mkulikov.taskb.ui.theme.TaskBTheme

@Composable
internal fun GroupEditorWidePanel(state: GroupEditorState, onIntent: (GroupEditorIntent) -> Unit, onClose: () -> Unit) {
    val colors = TaskBTheme.colors
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (state.isNew) "New group" else "Group", style = TaskBTheme.typography.title, color = colors.text)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onClose, enabled = !state.isSaving && !state.isDeleting, modifier = Modifier.size(24.dp)) {
                Icon(TaskBIcons.Clear, contentDescription = "Close group", tint = colors.muted)
            }
        }
        GroupEditorForm(state, onIntent, modifier = Modifier.weight(1f), autoFocusName = state.isNew) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!state.isNew) {
                    GroupButton(
                        "Delete", TaskBIcons.Delete, { onIntent(GroupEditorIntent.PrepareDelete) },
                        enabled = !state.isBusy && !state.isFinished, destructive = true,
                    )
                }
                GroupButton(
                    label = if (state.isNew) "Create group" else "Save changes", icon = if (state.isNew) TaskBIcons.Add else TaskBIcons.Done,
                    onClick = { onIntent(GroupEditorIntent.Save) }, enabled = state.canSave,
                    modifier = Modifier.semantics { contentDescription = "Save group" },
                )
            }
        }
    }
}
