package com.mkulikov.taskb.feature.task.screen.task

import com.mkulikov.taskb.feature.task.model.Group
import com.mkulikov.taskb.feature.task.model.Task
import java.time.LocalDate
import java.util.Locale

internal data class TaskState(
    val tasks: List<Task> = emptyList(),
    val groups: List<Group> = emptyList(),
    val query: String = "",
    val groupQuery: String = "",
    val groupFilter: TaskGroupFilter = TaskGroupFilter.All,
    val filter: TaskFilter? = null,
    val sort: TaskSort = TaskSort.Date,
    val reverseSort: Boolean = false,
    val today: LocalDate = LocalDate.now(),
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val editor: TaskEditorTarget? = null,
    val selection: TaskSelection? = null,
    val pendingCompletion: Set<Int> = emptySet(),
    val error: String? = null,
) {
    val visibleTasks: List<Task> get() {
        val matching = tasks.filter { task ->
            task.name.contains(query.trim(), ignoreCase = true) && when (val group = groupFilter) {
                TaskGroupFilter.All -> true
                TaskGroupFilter.NoGroup -> task.groupId == null
                is TaskGroupFilter.Selected -> task.groupId == group.id
            } && when (filter) {
                null -> true
                TaskFilter.Completed -> task.completed
                else -> !task.completed && when (filter) {
                    TaskFilter.Scheduled -> task.dueDate != null
                    TaskFilter.Overdue -> task.dueDate?.isBefore(today) == true
                    TaskFilter.Today -> task.dueDate == today
                    TaskFilter.Tomorrow -> task.dueDate == today.plusDays(1)
                    TaskFilter.OtherDates -> task.dueDate?.isAfter(today.plusDays(1)) == true
                    TaskFilter.NoDate -> task.dueDate == null
                }
            }
        }
        val names = compareBy<Task> { it.name.lowercase(Locale.ROOT) }.thenBy { it.id }
        val order = when (sort) {
            TaskSort.Name -> names
            TaskSort.Group -> compareBy<Task> { task -> groups.find { it.id == task.groupId }?.name?.lowercase(Locale.ROOT) ?: "" }.then(names)
            TaskSort.Date -> compareBy<Task> { it.dueDate == null }.thenBy { it.dueDate }.thenBy { it.id }
        }.let { if (reverseSort) it.reversed() else it }
        return matching.sortedWith(compareBy<Task> { it.completed }.then(order))
    }
    val summary: String get() = if (query.isBlank() && filter == null && groupFilter == TaskGroupFilter.All) {
        "${tasks.size} ${if (tasks.size == 1) "task" else "tasks"}"
    } else "${visibleTasks.size} of ${tasks.size} tasks"
}

internal enum class TaskFilter(val label: String) {
    Scheduled("Scheduled"), Overdue("Overdue"), Today("Today"), Tomorrow("Tomorrow"),
    OtherDates("Other dates"), NoDate("No date"), Completed("Completed"),
}
internal enum class TaskSort { Name, Group, Date }
internal sealed interface TaskGroupFilter {
    data object All : TaskGroupFilter
    data object NoGroup : TaskGroupFilter
    data class Selected(val id: Int) : TaskGroupFilter
}
internal sealed interface TaskEditorTarget {
    data object New : TaskEditorTarget
    data class Edit(val id: Int) : TaskEditorTarget
}
internal enum class TaskSelection { Groups, Filters, EditorGroup }
