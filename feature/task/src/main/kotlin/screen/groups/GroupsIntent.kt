package com.mkulikov.taskb.feature.task.screen.groups

internal sealed interface GroupsIntent {
    data class Search(val query: String) : GroupsIntent
    data class Filter(val filter: GroupFilter) : GroupsIntent
    data class Sort(val sort: GroupSort) : GroupsIntent
    data class OpenGroup(val id: Int) : GroupsIntent
    data object NewGroup : GroupsIntent
    data object CloseEditor : GroupsIntent
    data object RetryLoad : GroupsIntent
}
