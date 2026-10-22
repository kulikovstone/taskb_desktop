package com.mkulikov.taskb.feature.task.screen.groups

import com.mkulikov.taskb.feature.task.model.Group
import com.mkulikov.taskb.feature.task.model.GroupColor
import com.mkulikov.taskb.feature.task.model.GroupTaskSummary
import com.mkulikov.taskb.feature.task.testing.FakeGroupRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class GroupsStoreTest {
    private val sample = listOf(
        Group(1, "Work", GroupColor.Blue, GroupTaskSummary(planned = 15, completed = 9)),
        Group(2, "Ideas", GroupColor.Amber, GroupTaskSummary(completed = 4)),
        Group(3, "Health", GroupColor.Teal, GroupTaskSummary(planned = 6)),
        Group(4, "Reading", GroupColor.Indigo),
    )

    @Test
    fun `search filters and sort use planned totals and keep completed-only groups empty`() = runTest {
        val repository = FakeGroupRepository().apply { values.value = sample }
        GroupsStore(repository, StandardTestDispatcher(testScheduler)).use { store ->
            advanceUntilIdle()
            assertEquals(listOf("Health", "Ideas", "Reading", "Work"), store.state.value.visibleGroups.map { it.name })
            assertEquals(2, store.state.value.withTasksCount)
            assertEquals(2, store.state.value.emptyCount)
            store.accept(GroupsIntent.Filter(GroupFilter.Empty))
            advanceUntilIdle()
            assertEquals(listOf("Ideas", "Reading"), store.state.value.visibleGroups.map { it.name })
            store.accept(GroupsIntent.Search("  IDEA "))
            advanceUntilIdle()
            assertEquals(listOf("Ideas"), store.state.value.visibleGroups.map { it.name })
            assertEquals("1 of 4 groups", store.state.value.summary)
            store.accept(GroupsIntent.Search(""))
            store.accept(GroupsIntent.Filter(GroupFilter.All))
            store.accept(GroupsIntent.Sort(GroupSort.Tasks))
            advanceUntilIdle()
            assertEquals(listOf("Work", "Health", "Ideas", "Reading"), store.state.value.visibleGroups.map { it.name })
            store.accept(GroupsIntent.Sort(GroupSort.Tasks))
            advanceUntilIdle()
            assertEquals(listOf("Reading", "Ideas", "Health", "Work"), store.state.value.visibleGroups.map { it.name })
        }
        advanceUntilIdle()
        assertEquals(0, repository.subscriptions)
    }

    @Test
    fun `failed observation can retry and editor selection survives live list updates`() = runTest {
        val repository = FakeGroupRepository().apply { failRead = true }
        GroupsStore(repository, StandardTestDispatcher(testScheduler)).use { store ->
            advanceUntilIdle()
            assertNotNull(store.state.value.loadError)
            repository.failRead = false
            repository.values.value = sample
            store.accept(GroupsIntent.RetryLoad)
            advanceUntilIdle()
            assertNull(store.state.value.loadError)
            store.accept(GroupsIntent.OpenGroup(1))
            advanceUntilIdle()
            repository.values.value = sample.dropLast(1)
            advanceUntilIdle()
            assertEquals(GroupEditorTarget.Edit(1), store.state.value.editor)
            assertEquals(3, store.state.value.groups.size)
            store.accept(GroupsIntent.CloseEditor)
            advanceUntilIdle()
            assertNull(store.state.value.editor)
        }
    }
}
