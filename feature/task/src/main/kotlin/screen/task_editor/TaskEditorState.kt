package com.mkulikov.taskb.feature.task.screen.task_editor

import com.mkulikov.taskb.feature.task.model.Task
import java.time.LocalDate

internal data class TaskEditorState(
    val isNew: Boolean = true,
    val original: Task? = null,
    val name: String = "",
    val dueDate: LocalDate? = null,
    val groupId: Int? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isFinished: Boolean = false,
    val loadError: String? = null,
    val dialog: TaskEditorDialog? = null,
) {
    val isBusy get() = isLoading || isSaving
    val normalizedName get() = name.trim()
    val canSave get() = !isBusy && loadError == null && dialog == null && normalizedName.isNotEmpty() &&
        (isNew || original?.let { it.name != normalizedName || it.dueDate != dueDate || it.groupId != groupId } == true)
}
internal sealed interface TaskEditorDialog {
    data object ConfirmDelete : TaskEditorDialog
    data object DatePicker : TaskEditorDialog
    data class Error(val message: String) : TaskEditorDialog
}
