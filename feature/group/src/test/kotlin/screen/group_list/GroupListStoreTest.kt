package com.mkulikov.taskb.feature.group.screen.group_list

import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.feature.group.model.GroupColor
import com.mkulikov.taskb.feature.group.testing.FakeGroupRepository
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroupListStoreTest {
    @Test
    fun `list observes repository updates retries failures and releases its subscription`() = runTest {
        val repository = FakeGroupRepository()
        repository.failRead = true
        val store = GroupListStore(repository, StandardTestDispatcher(testScheduler))
        try {
            advanceUntilIdle()
            assertFalse(store.state.value.isLoading)
            assertNotNull(store.state.value.error)
            repository.failRead = false
            store.accept(GroupListIntent.Retry)
            advanceUntilIdle()
            assertNull(store.state.value.error)
            assertEquals(1, repository.subscriptions)
            repository.values.value = listOf(Group(1, "Personal", GroupColor.Pink))
            advanceUntilIdle()
            assertEquals("Personal", store.state.value.groups.single().name)
            store.close()
            runCurrent()
            assertEquals(0, repository.subscriptions)
        } finally { store.close() }
    }
}
