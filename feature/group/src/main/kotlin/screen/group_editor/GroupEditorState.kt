package com.mkulikov.taskb.feature.group.screen.group_editor

import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.feature.group.model.GroupColor

internal data class GroupEditorState(
    val groupId: Int? = null,
    val original: Group? = null,
    val name: String = "",
    val color: GroupColor = GroupColor.Transparent,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val isFinished: Boolean = false,
    val loadError: String? = null,
    val dialog: GroupEditorDialog? = null,
) {
    val isEditing: Boolean get() = groupId != null
    val isBusy: Boolean get() = isLoading || isSaving || isDeleting
    val normalizedName: String get() = name.trim()
    val canSave: Boolean get() = !isBusy && !isFinished && loadError == null && normalizedName.isNotEmpty() &&
        (!isEditing || original?.let { it.name != normalizedName || it.color != color } == true)
}

internal sealed interface GroupEditorDialog {
    data object NameConflict : GroupEditorDialog
    data object ConfirmDelete : GroupEditorDialog
    data class Error(val message: String) : GroupEditorDialog
}

internal sealed interface GroupEditorIntent {
    data class NameChanged(val name: String) : GroupEditorIntent
    data class ColorChanged(val color: GroupColor) : GroupEditorIntent
    data object Save : GroupEditorIntent
    data object PrepareDelete : GroupEditorIntent
    data object ConfirmDelete : GroupEditorIntent
    data object DismissDialog : GroupEditorIntent
    data object RetryLoad : GroupEditorIntent
}
