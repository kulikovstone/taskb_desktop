package com.mkulikov.taskb.feature.task.screen.group_editor

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
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
        GroupEditorForm(state, onIntent, modifier = Modifier.weight(1f), autoFocusName = state.isNew)
    }
}
