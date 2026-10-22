package com.mkulikov.taskb.feature.task.screen.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.task.component.GroupButton
import com.mkulikov.taskb.feature.task.component.GroupTextField
import com.mkulikov.taskb.feature.task.screen.group_editor.GroupEditorIntent
import com.mkulikov.taskb.feature.task.screen.group_editor.GroupEditorState
import com.mkulikov.taskb.feature.task.screen.group_editor.GroupEditorWidePanel
import com.mkulikov.taskb.ui.icon.TaskBIcons
import com.mkulikov.taskb.ui.theme.TaskBTheme

@Composable
internal fun GroupsWideScreen(
    state: GroupsState,
    onIntent: (GroupsIntent) -> Unit,
    editor: GroupEditorState?,
    onEditorIntent: (GroupEditorIntent) -> Unit,
    onBack: () -> Unit,
) {
    val canLeaveEditor = editor?.let { !it.isSaving && !it.isDeleting && it.dialog == null } ?: true
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(96.dp).padding(start = 16.dp, end = 32.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = canLeaveEditor) { Icon(TaskBIcons.Back, contentDescription = "Back", tint = MaterialTheme.colors.onBackground) }
            Spacer(Modifier.width(8.dp))
            Text("Groups", style = TaskBTheme.typography.display, color = MaterialTheme.colors.onBackground)
            Spacer(Modifier.weight(1f))
            GroupButton("New group", TaskBIcons.Add, { onIntent(GroupsIntent.NewGroup) }, enabled = canLeaveEditor)
        }
        Column(Modifier.weight(1f).padding(start = 32.dp, end = 32.dp, top = 16.dp)) {
            Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                GroupTextField(state.query, { onIntent(GroupsIntent.Search(it)) }, "Search groups", placeholder = "Search groups", search = true, modifier = Modifier.width(300.dp))
                GroupsFilters(state, onIntent)
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                GroupsDirectory(state, onIntent, compact = false, enabled = canLeaveEditor, modifier = Modifier.weight(1f).fillMaxHeight())
                if (editor != null) {
                    Box(Modifier.width(348.dp).fillMaxHeight()) {
                        GroupEditorWidePanel(editor, onEditorIntent, onClose = { if (canLeaveEditor) onIntent(GroupsIntent.CloseEditor) })
                    }
                }
            }
        }
    }
}
