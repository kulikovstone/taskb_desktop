package com.mkulikov.taskb.feature.task.screen.group_editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.task.component.GroupButton
import com.mkulikov.taskb.feature.task.component.GroupColorPicker
import com.mkulikov.taskb.feature.task.component.GroupLoadError
import com.mkulikov.taskb.feature.task.component.GroupLoading
import com.mkulikov.taskb.feature.task.component.GroupStatistics
import com.mkulikov.taskb.feature.task.component.GroupTextField
import com.mkulikov.taskb.ui.icon.TaskBIcons
import com.mkulikov.taskb.ui.theme.TaskBTheme

@Composable
internal fun GroupEditorForm(
    state: GroupEditorState,
    onIntent: (GroupEditorIntent) -> Unit,
    modifier: Modifier,
    autoFocusName: Boolean = false,
) {
    when {
        state.isLoading -> GroupLoading(modifier)
        state.loadError != null -> GroupLoadError(state.loadError, { onIntent(GroupEditorIntent.RetryLoad) }, modifier)
        else -> Column(modifier.verticalScroll(rememberScrollState()).padding(top = 24.dp)) {
            val enabled = !state.isBusy && !state.isFinished
            val colors = TaskBTheme.colors
            if (!state.isNew) {
                GroupStatistics(requireNotNull(state.original).tasks)
                Spacer(Modifier.height(24.dp))
            }
            Text("Group name", modifier = Modifier.height(18.dp), style = TaskBTheme.typography.label, color = colors.text)
            Spacer(Modifier.height(8.dp))
            GroupTextField(
                value = state.name, onValueChange = { onIntent(GroupEditorIntent.NameChanged(it)) }, description = "Group name",
                placeholder = "Enter group name", enabled = enabled, modifier = Modifier.fillMaxWidth(),
                autoFocus = autoFocusName,
                onDone = { onIntent(GroupEditorIntent.Save) },
            )
            Spacer(Modifier.height(24.dp))
            GroupColorPicker(state.color, { onIntent(GroupEditorIntent.ColorChanged(it)) }, enabled)
            Spacer(Modifier.height(48.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!state.isNew) {
                    GroupButton("Delete", TaskBIcons.Delete, { onIntent(GroupEditorIntent.PrepareDelete) }, enabled = enabled, destructive = true)
                }
                GroupButton(
                    label = if (state.isNew) "Create group" else "Save changes", icon = if (state.isNew) TaskBIcons.Add else TaskBIcons.Done,
                    onClick = { onIntent(GroupEditorIntent.Save) }, enabled = state.canSave,
                    modifier = Modifier.semantics { contentDescription = "Save group" },
                )
            }
            if (state.isSaving || state.isDeleting) Text(if (state.isDeleting) "Deleting…" else "Saving…", style = TaskBTheme.typography.small, color = colors.muted)
        }
    }
}
