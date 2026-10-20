package com.mkulikov.taskb.feature.group.screen.edit_group

import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.feature.group.model.GroupColor

internal data class EditGroupState(
    val original: Group? = null,
    val name: String = "",
    val color: GroupColor = GroupColor.Transparent,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isFinished: Boolean = false,
    val loadError: String? = null,
    val dialog: EditGroupDialog? = null,
) {
    val isBusy: Boolean get() = isLoading || isSaving || isDeleting
    val normalizedName: String get() = name.trim()
    val canSave: Boolean get() = !isBusy && !isFinished && loadError == null && normalizedName.isNotEmpty() &&
        original?.let { it.name != normalizedName || it.color != color } == true
}

internal sealed interface EditGroupDialog {
    data object NameConflict : EditGroupDialog
    data object ConfirmDelete : EditGroupDialog
    data class Error(val message: String) : EditGroupDialog
}

internal sealed interface EditGroupIntent {
    data class NameChanged(val name: String) : EditGroupIntent
    data class ColorChanged(val color: GroupColor) : EditGroupIntent
    data object Save : EditGroupIntent
    data object PrepareDelete : EditGroupIntent
    data object ConfirmDelete : EditGroupIntent
    data object DismissDialog : EditGroupIntent
    data object RetryLoad : EditGroupIntent
}
