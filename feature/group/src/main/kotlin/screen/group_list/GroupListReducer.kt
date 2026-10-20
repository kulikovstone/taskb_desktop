package com.mkulikov.taskb.feature.group.screen.group_list

import com.mkulikov.taskb.feature.group.model.Group

internal sealed interface GroupListMessage {
    data object Loading : GroupListMessage
    data class Loaded(val groups: List<Group>) : GroupListMessage
    data object Failed : GroupListMessage
}

internal fun reduceGroupList(state: GroupListState, message: GroupListMessage): GroupListState = when (message) {
    GroupListMessage.Loading -> state.copy(isLoading = true, error = null)
    is GroupListMessage.Loaded -> state.copy(isLoading = false, groups = message.groups, error = null)
    GroupListMessage.Failed -> state.copy(isLoading = false, error = "Couldn't load groups. Please try again.")
}
