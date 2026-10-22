package com.mkulikov.taskb.feature.task.screen.groups

import com.mkulikov.taskb.feature.task.model.Group

internal data class GroupsState(
    val groups: List<Group> = emptyList(),
    val query: String = "",
    val filter: GroupFilter = GroupFilter.All,
    val sort: GroupSort = GroupSort.Name,
    val reverseSort: Boolean = false,
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val editor: GroupEditorTarget? = null,
) {
    val withTasksCount: Int get() = groups.count { it.tasks.planned > 0 }
    val emptyCount: Int get() = groups.size - withTasksCount
    val visibleGroups: List<Group> get() {
        val matching = groups.filter {
            it.name.contains(query.trim(), ignoreCase = true) && when (filter) {
                GroupFilter.All -> true
                GroupFilter.WithTasks -> it.tasks.planned > 0
                GroupFilter.Empty -> it.tasks.planned == 0
            }
        }
        val byName = compareBy<Group> { it.name.lowercase(java.util.Locale.ROOT) }.thenBy { it.id }
        val comparator = when (sort) {
            GroupSort.Name -> byName
            GroupSort.Tasks -> compareByDescending<Group> { it.tasks.planned }.then(byName)
        }
        return matching.sortedWith(if (reverseSort) comparator.reversed() else comparator)
    }
    val summary: String get() = if (query.isBlank() && filter == GroupFilter.All) {
        "${groups.size} ${if (groups.size == 1) "group" else "groups"}"
    } else "${visibleGroups.size} of ${groups.size} groups"
}

internal enum class GroupFilter { All, WithTasks, Empty }
internal enum class GroupSort { Name, Tasks }

internal sealed interface GroupEditorTarget {
    data object New : GroupEditorTarget
    data class Edit(val id: Int) : GroupEditorTarget
}
