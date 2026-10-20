package com.mkulikov.taskb.feature.group.screen.group_list

internal sealed interface GroupListIntent {
    data object Retry : GroupListIntent
}
