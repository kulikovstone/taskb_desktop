package com.mkulikov.taskb.feature.group.screen.edit_group

import com.mkulikov.taskb.feature.group.model.GroupColor

internal sealed interface EditGroupIntent {
    data class NameChanged(val name: String) : EditGroupIntent
    data class ColorChanged(val color: GroupColor) : EditGroupIntent
    data object Save : EditGroupIntent
    data object PrepareDelete : EditGroupIntent
    data object ConfirmDelete : EditGroupIntent
    data object DismissDialog : EditGroupIntent
    data object RetryLoad : EditGroupIntent
}
