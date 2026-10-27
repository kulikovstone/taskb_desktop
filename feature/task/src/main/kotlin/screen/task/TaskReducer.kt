package com.mkulikov.taskb.feature.task.screen.task

import com.mkulikov.taskb.feature.task.model.Group
import com.mkulikov.taskb.feature.task.model.Task
import java.time.LocalDate

internal sealed interface TaskMessage {
    data class Loaded(val tasks: List<Task>, val groups: List<Group>) : TaskMessage
    data object Loading : TaskMessage
    data object LoadFailed : TaskMessage
    data class Query(val value: String) : TaskMessage
    data class GroupQuery(val value: String) : TaskMessage
    data class Filter(val value: TaskFilter?) : TaskMessage
    data class GroupFilter(val value: TaskGroupFilter) : TaskMessage
    data class Sort(val value: TaskSort) : TaskMessage
    data class Editor(val value: TaskEditorTarget?) : TaskMessage
    data class Selection(val value: TaskSelection?) : TaskMessage
    data class Completing(val id: Int) : TaskMessage
    data class Completed(val id: Int, val failed: Boolean) : TaskMessage
    data class Today(val value: LocalDate) : TaskMessage
    data object DismissError : TaskMessage
}

internal fun reduceTask(state: TaskState, message: TaskMessage): TaskState = when (message) {
    is TaskMessage.Loaded -> state.copy(
        tasks = message.tasks, groups = message.groups, isLoading = false, loadError = null,
        groupFilter = state.groupFilter.takeUnless { filter -> filter is TaskGroupFilter.Selected && message.groups.none { it.id == filter.id } } ?: TaskGroupFilter.All,
    )
    TaskMessage.Loading -> state.copy(isLoading = true, loadError = null)
    TaskMessage.LoadFailed -> state.copy(isLoading = false, loadError = "Couldn't load tasks. Please try again.")
    is TaskMessage.Query -> state.copy(query = message.value)
    is TaskMessage.GroupQuery -> state.copy(groupQuery = message.value)
    is TaskMessage.Filter -> state.copy(filter = message.value, selection = null)
    is TaskMessage.GroupFilter -> state.copy(groupFilter = message.value, selection = null)
    is TaskMessage.Sort -> state.copy(sort = message.value, reverseSort = state.sort == message.value && !state.reverseSort)
    is TaskMessage.Editor -> state.copy(editor = message.value)
    is TaskMessage.Selection -> state.copy(selection = message.value, groupQuery = "")
    is TaskMessage.Completing -> state.copy(pendingCompletion = state.pendingCompletion + message.id)
    is TaskMessage.Completed -> state.copy(pendingCompletion = state.pendingCompletion - message.id,
        error = if (message.failed) "Couldn't update this task. Please try again." else state.error)
    is TaskMessage.Today -> state.copy(today = message.value)
    TaskMessage.DismissError -> state.copy(error = null)
}
