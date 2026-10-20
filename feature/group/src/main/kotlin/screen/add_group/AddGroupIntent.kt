package com.mkulikov.taskb.feature.group.screen.add_group

import com.mkulikov.taskb.feature.group.model.GroupColor

internal sealed interface AddGroupIntent {
    data class NameChanged(val name: String) : AddGroupIntent
    data class ColorChanged(val color: GroupColor) : AddGroupIntent
    data object Save : AddGroupIntent
    data object DismissDialog : AddGroupIntent
}
