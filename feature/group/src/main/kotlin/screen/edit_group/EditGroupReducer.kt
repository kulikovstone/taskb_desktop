package com.mkulikov.taskb.feature.group.screen.edit_group

import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.feature.group.model.GroupColor
import com.mkulikov.taskb.feature.group.repository.GroupWriteResult

internal sealed interface EditGroupMessage {
    data class NameChanged(val name: String) : EditGroupMessage
    data class ColorChanged(val color: GroupColor) : EditGroupMessage
    data object Loading : EditGroupMessage
    data class Loaded(val group: Group?) : EditGroupMessage
    data object LoadFailed : EditGroupMessage
    data object Saving : EditGroupMessage
    data class SaveResult(val result: GroupWriteResult) : EditGroupMessage
    data object SaveFailed : EditGroupMessage
    data object AskDelete : EditGroupMessage
    data object Deleting : EditGroupMessage
    data object Deleted : EditGroupMessage
    data object DeleteFailed : EditGroupMessage
    data object DismissDialog : EditGroupMessage
}

internal fun reduceEditGroup(state: EditGroupState, message: EditGroupMessage): EditGroupState = when (message) {
    is EditGroupMessage.NameChanged -> state.copy(name = message.name)
    is EditGroupMessage.ColorChanged -> state.copy(color = message.color)
    EditGroupMessage.Loading -> state.copy(isLoading = true, loadError = null)
    is EditGroupMessage.Loaded -> if (message.group == null) {
        state.copy(isLoading = false, loadError = "This group no longer exists.")
    } else {
        state.copy(isLoading = false, original = message.group, name = message.group.name, color = message.group.color)
    }
    EditGroupMessage.LoadFailed -> state.copy(isLoading = false, loadError = "Couldn't load this group. Please try again.")
    EditGroupMessage.Saving -> state.copy(isSaving = true, dialog = null)
    is EditGroupMessage.SaveResult -> when (message.result) {
        GroupWriteResult.Saved -> state.copy(isSaving = false, isFinished = true)
        GroupWriteResult.NameConflict -> state.copy(isSaving = false, dialog = EditGroupDialog.NameConflict)
        GroupWriteResult.NotFound -> state.copy(isSaving = false, dialog = EditGroupDialog.Error("This group no longer exists."))
    }
    EditGroupMessage.SaveFailed -> state.copy(isSaving = false, dialog = EditGroupDialog.Error("Couldn't save this group. Please try again."))
    EditGroupMessage.AskDelete -> state.copy(dialog = EditGroupDialog.ConfirmDelete)
    EditGroupMessage.Deleting -> state.copy(isDeleting = true, dialog = null)
    EditGroupMessage.Deleted -> state.copy(isDeleting = false, isFinished = true)
    EditGroupMessage.DeleteFailed -> state.copy(isDeleting = false, dialog = EditGroupDialog.Error("Couldn't delete this group. Please try again."))
    EditGroupMessage.DismissDialog -> state.copy(dialog = null)
}
