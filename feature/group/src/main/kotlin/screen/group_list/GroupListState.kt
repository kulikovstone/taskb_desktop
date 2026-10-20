package com.mkulikov.taskb.feature.group.screen.group_list

import com.mkulikov.taskb.feature.group.model.Group

internal data class GroupListState(
    val isLoading: Boolean = true,
    val groups: List<Group> = emptyList(),
    val error: String? = null,
)
