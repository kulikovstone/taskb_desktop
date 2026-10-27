package com.mkulikov.taskb.feature.task.screen.task_editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.mkulikov.taskb.ui.dialog.TaskBConfirmationDialog
import com.mkulikov.taskb.ui.dialog.TaskBMessageDialog
import java.time.LocalDate

@Composable
internal fun TaskEditorDialogs(state: TaskEditorState, today: LocalDate, onIntent: (TaskEditorIntent) -> Unit, availableHeight: Dp) {
    when (val dialog = state.dialog) {
        TaskEditorDialog.ConfirmDelete -> TaskBConfirmationDialog("Delete task", "Delete \"${state.normalizedName}\"?", "Delete",
            { onIntent(TaskEditorIntent.ConfirmDelete) }, { onIntent(TaskEditorIntent.DismissDialog) })
        is TaskEditorDialog.Error -> TaskBMessageDialog("Task", dialog.message) { onIntent(TaskEditorIntent.DismissDialog) }
        TaskEditorDialog.DatePicker -> TaskDatePicker(state.dueDate, today, { onIntent(TaskEditorIntent.DateChanged(it)) }, { onIntent(TaskEditorIntent.DismissDialog) }, availableHeight)
        null -> Unit
    }
}
