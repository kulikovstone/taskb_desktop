package com.mkulikov.taskb.feature.task.screen.group_editor

import com.mkulikov.taskb.feature.task.model.Group
import com.mkulikov.taskb.feature.task.model.GroupColor
import com.mkulikov.taskb.feature.task.repository.GroupWriteResult

internal sealed interface GroupEditorMessage {
    data class NameChanged(val name: String) : GroupEditorMessage
    data class ColorChanged(val color: GroupColor) : GroupEditorMessage
    data object Loading : GroupEditorMessage
    data class Loaded(val group: Group?) : GroupEditorMessage
    data object LoadFailed : GroupEditorMessage
    data object Saving : GroupEditorMessage
    data class Saved(val result: GroupWriteResult) : GroupEditorMessage
    data object SaveFailed : GroupEditorMessage
    data object AskDelete : GroupEditorMessage
    data object Deleting : GroupEditorMessage
    data object Deleted : GroupEditorMessage
    data object DeleteFailed : GroupEditorMessage
    data object DismissDialog : GroupEditorMessage
}

internal fun reduceGroupEditor(state: GroupEditorState, message: GroupEditorMessage): GroupEditorState = when (message) {
    is GroupEditorMessage.NameChanged -> state.copy(name = message.name)
    is GroupEditorMessage.ColorChanged -> state.copy(color = message.color)
    GroupEditorMessage.Loading -> state.copy(isLoading = true, loadError = null)
    is GroupEditorMessage.Loaded -> if (message.group == null) {
        state.copy(isLoading = false, loadError = "This group no longer exists.")
    } else state.copy(isLoading = false, original = message.group, name = message.group.name, color = message.group.color)
    GroupEditorMessage.LoadFailed -> state.copy(isLoading = false, loadError = "Couldn't load this group. Please try again.")
    GroupEditorMessage.Saving -> state.copy(isSaving = true, dialog = null)
    is GroupEditorMessage.Saved -> when (message.result) {
        GroupWriteResult.Saved -> state.copy(isSaving = false, isFinished = true)
        GroupWriteResult.NameConflict -> state.copy(isSaving = false, dialog = GroupEditorDialog.NameConflict)
        GroupWriteResult.NotFound -> state.copy(isSaving = false, dialog = GroupEditorDialog.Error("This group no longer exists."))
    }
    GroupEditorMessage.SaveFailed -> state.copy(isSaving = false, dialog = GroupEditorDialog.Error("Couldn't save this group. Please try again."))
    GroupEditorMessage.AskDelete -> state.copy(dialog = GroupEditorDialog.ConfirmDelete)
    GroupEditorMessage.Deleting -> state.copy(isDeleting = true, dialog = null)
    GroupEditorMessage.Deleted -> state.copy(isDeleting = false, isFinished = true)
    GroupEditorMessage.DeleteFailed -> state.copy(isDeleting = false, dialog = GroupEditorDialog.Error("Couldn't delete this group. Please try again."))
    GroupEditorMessage.DismissDialog -> state.copy(dialog = null)
}
