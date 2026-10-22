package com.mkulikov.taskb.feature.task.screen.group_editor

import androidx.compose.runtime.Composable
import com.mkulikov.taskb.ui.dialog.TaskBConfirmationDialog
import com.mkulikov.taskb.ui.dialog.TaskBMessageDialog

@Composable
internal fun GroupEditorDialogs(state: GroupEditorState, onIntent: (GroupEditorIntent) -> Unit) {
    val dismiss = { onIntent(GroupEditorIntent.DismissDialog) }
    when (val dialog = state.dialog) {
        GroupEditorDialog.NameConflict -> TaskBMessageDialog("Name conflict", "A group with this name already exists.", dismiss)
        GroupEditorDialog.ConfirmDelete -> TaskBConfirmationDialog(
            title = "Delete group",
            message = "The group will be deleted. Its tasks will be kept.",
            confirmText = "Delete", onConfirm = { onIntent(GroupEditorIntent.ConfirmDelete) }, onDismiss = dismiss,
        )
        is GroupEditorDialog.Error -> TaskBMessageDialog("Group error", dialog.message, dismiss)
        null -> Unit
    }
}
