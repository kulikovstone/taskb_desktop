package com.mkulikov.taskb.feature.task.screen.task

internal sealed interface TaskIntent {
    data class Search(val query: String) : TaskIntent
    data class SearchGroups(val query: String) : TaskIntent
    data class Filter(val filter: TaskFilter?) : TaskIntent
    data class FilterGroup(val filter: TaskGroupFilter) : TaskIntent
    data class Sort(val sort: TaskSort) : TaskIntent
    data class OpenTask(val id: Int) : TaskIntent
    data class ToggleCompleted(val id: Int) : TaskIntent
    data class Choose(val selection: TaskSelection) : TaskIntent
    data object CloseSelection : TaskIntent
    data object NewTask : TaskIntent
    data object CloseEditor : TaskIntent
    data object RetryLoad : TaskIntent
    data object DismissError : TaskIntent
}
