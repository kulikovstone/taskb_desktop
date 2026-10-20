package com.mkulikov.taskb.feature.group.screen.edit_group

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.group.component.GroupColorBlock
import com.mkulikov.taskb.feature.group.component.GroupLoadError
import com.mkulikov.taskb.feature.group.component.GroupNameField
import com.mkulikov.taskb.feature.group.component.GroupSectionTitle
import com.mkulikov.taskb.ui.button.TaskBActionButton
import com.mkulikov.taskb.ui.component.TaskBTopAppBar
import com.mkulikov.taskb.ui.dialog.TaskBConfirmationDialog
import com.mkulikov.taskb.ui.dialog.TaskBMessageDialog
import com.mkulikov.taskb.ui.icon.TaskBIcons

@Composable
internal fun EditGroupScreen(state: EditGroupState, onIntent: (EditGroupIntent) -> Unit, onBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TaskBTopAppBar(title = "Group", onBack = onBack, backEnabled = !state.isSaving && !state.isDeleting) {
                IconButton(onClick = { onIntent(EditGroupIntent.Save) }, enabled = state.canSave) {
                    Icon(TaskBIcons.Done, contentDescription = "Save group")
                }
            }
        },
    ) { padding ->
        when {
            state.isBusy -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.loadError != null -> GroupLoadError(state.loadError, { onIntent(EditGroupIntent.RetryLoad) }, Modifier.padding(padding))
            else -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(8.dp))
                GroupNameField(
                    name = state.name,
                    onNameChange = { onIntent(EditGroupIntent.NameChanged(it)) },
                    onSave = { onIntent(EditGroupIntent.Save) },
                )
                Spacer(Modifier.height(30.dp))
                GroupColorBlock(selectedColor = state.color, onSelect = { onIntent(EditGroupIntent.ColorChanged(it)) })
                Spacer(Modifier.height(30.dp))
                GroupSectionTitle("Statistics")
                Spacer(Modifier.height(10.dp))
                Text("No tasks in this group yet.", style = MaterialTheme.typography.body2)
                Spacer(Modifier.height(30.dp))
                GroupSectionTitle("Action")
                Spacer(Modifier.height(10.dp))
                TaskBActionButton(
                    label = "Delete",
                    icon = TaskBIcons.Delete,
                    onClick = { onIntent(EditGroupIntent.PrepareDelete) },
                )
                Spacer(Modifier.height(50.dp))
            }
        }
    }
    when (val dialog = state.dialog) {
        EditGroupDialog.NameConflict -> TaskBMessageDialog(
            title = "Name Conflict",
            message = "Group with this name already exists.",
            onDismiss = { onIntent(EditGroupIntent.DismissDialog) },
        )
        EditGroupDialog.ConfirmDelete -> TaskBConfirmationDialog(
            title = "Delete group",
            message = "The group will be deleted.",
            confirmText = "Delete",
            onConfirm = { onIntent(EditGroupIntent.ConfirmDelete) },
            onDismiss = { onIntent(EditGroupIntent.DismissDialog) },
        )
        is EditGroupDialog.Error -> TaskBMessageDialog(
            title = "Group error",
            message = dialog.message,
            onDismiss = { onIntent(EditGroupIntent.DismissDialog) },
        )
        null -> Unit
    }
}
