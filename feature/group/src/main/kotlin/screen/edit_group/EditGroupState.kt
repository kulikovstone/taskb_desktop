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
