package com.mkulikov.taskb.feature.group.screen.add_group

import com.mkulikov.taskb.feature.group.model.GroupColor
import com.mkulikov.taskb.feature.group.repository.GroupWriteResult

internal sealed interface AddGroupMessage {
    data class NameChanged(val name: String) : AddGroupMessage
    data class ColorChanged(val color: GroupColor) : AddGroupMessage
    data object Saving : AddGroupMessage
    data class SaveResult(val result: GroupWriteResult) : AddGroupMessage
    data object SaveFailed : AddGroupMessage
    data object DismissDialog : AddGroupMessage
}

internal fun reduceAddGroup(state: AddGroupState, message: AddGroupMessage): AddGroupState = when (message) {
    is AddGroupMessage.NameChanged -> state.copy(name = message.name)
    is AddGroupMessage.ColorChanged -> state.copy(color = message.color)
    AddGroupMessage.Saving -> state.copy(isSaving = true, dialog = null)
    is AddGroupMessage.SaveResult -> when (message.result) {
        GroupWriteResult.Saved -> state.copy(isSaving = false, isFinished = true)
        GroupWriteResult.NameConflict -> state.copy(isSaving = false, dialog = AddGroupDialog.NameConflict)
        GroupWriteResult.NotFound -> state.copy(isSaving = false, dialog = AddGroupDialog.Error("Couldn't save this group. Please try again."))
    }
    AddGroupMessage.SaveFailed -> state.copy(isSaving = false, dialog = AddGroupDialog.Error("Couldn't save this group. Please try again."))
    AddGroupMessage.DismissDialog -> state.copy(dialog = null)
}
