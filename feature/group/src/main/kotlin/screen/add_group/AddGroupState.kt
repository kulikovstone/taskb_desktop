package com.mkulikov.taskb.feature.group.screen.add_group

import com.mkulikov.taskb.feature.group.model.GroupColor

internal data class AddGroupState(
    val name: String = "",
    val color: GroupColor = GroupColor.Transparent,
    val isSaving: Boolean = false,
    val isFinished: Boolean = false,
    val dialog: AddGroupDialog? = null,
) {
    val normalizedName: String get() = name.trim()
    val canSave: Boolean get() = !isSaving && !isFinished && normalizedName.isNotEmpty()
}

internal sealed interface AddGroupDialog {
    data object NameConflict : AddGroupDialog
    data class Error(val message: String) : AddGroupDialog
}

internal sealed interface AddGroupIntent {
    data class NameChanged(val name: String) : AddGroupIntent
    data class ColorChanged(val color: GroupColor) : AddGroupIntent
    data object Save : AddGroupIntent
    data object DismissDialog : AddGroupIntent
}
