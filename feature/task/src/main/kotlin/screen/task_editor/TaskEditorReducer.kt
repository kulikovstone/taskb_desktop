package com.mkulikov.taskb.feature.task.screen.task_editor

import com.mkulikov.taskb.feature.task.model.Task
import java.time.LocalDate

internal sealed interface TaskEditorMessage {
    data class Loaded(val task: Task?) : TaskEditorMessage
    data class Name(val value: String) : TaskEditorMessage
    data class Date(val value: LocalDate?) : TaskEditorMessage
    data class Group(val value: Int?) : TaskEditorMessage
    data class Dialog(val value: TaskEditorDialog?) : TaskEditorMessage
    data object Loading : TaskEditorMessage
    data object LoadFailed : TaskEditorMessage
    data object Saving : TaskEditorMessage
    data object Finished : TaskEditorMessage
    data class Failed(val message: String) : TaskEditorMessage
}
internal fun reduceTaskEditor(state: TaskEditorState, message: TaskEditorMessage): TaskEditorState = when (message) {
    is TaskEditorMessage.Loaded -> if (message.task == null) state.copy(isLoading = false, loadError = "This task no longer exists.")
        else state.copy(isLoading = false, original = message.task, name = message.task.name, dueDate = message.task.dueDate, groupId = message.task.groupId)
    is TaskEditorMessage.Name -> state.copy(name = message.value)
    is TaskEditorMessage.Date -> state.copy(dueDate = message.value, dialog = null)
    is TaskEditorMessage.Group -> state.copy(groupId = message.value)
    is TaskEditorMessage.Dialog -> state.copy(dialog = message.value)
    TaskEditorMessage.Loading -> state.copy(isLoading = true, loadError = null)
    TaskEditorMessage.LoadFailed -> state.copy(isLoading = false, loadError = "Couldn't load this task. Please try again.")
    TaskEditorMessage.Saving -> state.copy(isSaving = true, dialog = null)
    TaskEditorMessage.Finished -> state.copy(isSaving = false, isFinished = true)
    is TaskEditorMessage.Failed -> state.copy(isSaving = false, dialog = TaskEditorDialog.Error(message.message))
}
