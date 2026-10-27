package com.mkulikov.taskb.feature.task.screen.task

import com.mkulikov.taskb.feature.task.model.Group
import com.mkulikov.taskb.feature.task.model.GroupColor
import com.mkulikov.taskb.feature.task.model.Task
import com.mkulikov.taskb.feature.task.testing.FakeGroupRepository
import com.mkulikov.taskb.feature.task.testing.FakeTaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class TaskStoreTest {
    private val today = LocalDate.of(2026, 10, 25)
    private val tasks = listOf(
        Task(1, "Overdue", today.minusDays(1), 1), Task(2, "Today", today, 1),
        Task(3, "Tomorrow", today.plusDays(1)), Task(4, "Later", today.plusDays(2)),
        Task(5, "Someday"), Task(6, "Completed", today, 1, true),
    )
    @Test
    fun `date filters exclude completed tasks and combine with group and case insensitive search`() {
        val state = TaskState(tasks = tasks, today = today)
        val expected = mapOf(TaskFilter.Scheduled to listOf(1, 2, 3, 4), TaskFilter.Overdue to listOf(1),
            TaskFilter.Today to listOf(2), TaskFilter.Tomorrow to listOf(3), TaskFilter.OtherDates to listOf(4),
            TaskFilter.NoDate to listOf(5), TaskFilter.Completed to listOf(6))
        expected.forEach { (filter, ids) -> assertEquals(ids, state.copy(filter = filter).visibleTasks.map { it.id }, filter.label) }
        assertEquals(listOf(1, 2, 6), state.copy(groupFilter = TaskGroupFilter.Selected(1)).visibleTasks.map { it.id })
        assertEquals(listOf(3, 4, 5), state.copy(groupFilter = TaskGroupFilter.NoGroup).visibleTasks.map { it.id })
        assertEquals(listOf(2), state.copy(groupFilter = TaskGroupFilter.Selected(1), query = " TODAY ", filter = TaskFilter.Scheduled).visibleTasks.map { it.id })
        assertEquals(6, state.copy(sort = TaskSort.Name, reverseSort = true).visibleTasks.last().id)
        assertEquals(listOf(1, 2), state.copy(filter = TaskFilter.Overdue, today = today.plusDays(1)).visibleTasks.map { it.id })
    }
    @Test
    fun `store retries loading applies filters updates completion and releases subscriptions`() = runTest {
        val repository = FakeTaskRepository().apply { fail = true; values.value = this@TaskStoreTest.tasks }
        val groups = FakeGroupRepository().apply { values.value = listOf(Group(1, "Work", GroupColor.Blue)) }
        val store = TaskStore(repository, groups, StandardTestDispatcher(testScheduler))
        runCurrent()
        assertNotNull(store.state.value.loadError)
        repository.fail = false
        store.accept(TaskIntent.RetryLoad)
        runCurrent()
        assertEquals(6, store.state.value.tasks.size)
        store.accept(TaskIntent.Choose(TaskSelection.Filters))
        store.accept(TaskIntent.Filter(TaskFilter.Completed))
        store.accept(TaskIntent.ToggleCompleted(1))
        runCurrent()
        assertNull(store.state.value.selection)
        assertEquals(2, store.state.value.visibleTasks.size)
        store.accept(TaskIntent.FilterGroup(TaskGroupFilter.Selected(1)))
        runCurrent()
        groups.values.value = emptyList()
        runCurrent()
        assertEquals(TaskGroupFilter.All, store.state.value.groupFilter)
        repository.fail = true
        store.accept(TaskIntent.ToggleCompleted(2))
        runCurrent()
        assertNotNull(store.state.value.error)
        assertTrue(store.state.value.pendingCompletion.isEmpty())
        store.close()
        runCurrent()
        assertEquals(0, repository.subscriptions)
        assertEquals(0, groups.subscriptions)
    }
}
