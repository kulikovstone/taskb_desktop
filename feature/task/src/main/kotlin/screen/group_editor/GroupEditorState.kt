package com.mkulikov.taskb.feature.task.screen.group_editor

import com.mkulikov.taskb.feature.task.model.Group
import com.mkulikov.taskb.feature.task.model.GroupColor

internal data class GroupEditorState(
    val isNew: Boolean,
    val original: Group? = null,
    val name: String = "",
    val color: GroupColor = GroupColor.Grey,
    val isLoading: Boolean = !isNew,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isFinished: Boolean = false,
    val loadError: String? = null,
    val dialog: GroupEditorDialog? = null,
) {
    val isBusy: Boolean get() = isLoading || isSaving || isDeleting
    val normalizedName: String get() = name.trim()
    val canSave: Boolean get() = !isBusy && !isFinished && loadError == null && normalizedName.isNotEmpty() &&
        (isNew || original?.let { it.name != normalizedName || it.color != color } == true)
}

internal sealed interface GroupEditorDialog {
    data object NameConflict : GroupEditorDialog
    data object ConfirmDelete : GroupEditorDialog
    data class Error(val message: String) : GroupEditorDialog
}
