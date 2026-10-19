package com.mkulikov.taskb.feature.group.mvi

import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.feature.group.model.GroupColor
import com.mkulikov.taskb.feature.group.repository.GroupRepository
import com.mkulikov.taskb.feature.group.repository.GroupWriteResult
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GroupStoreTest {
    @Test
    fun `blank names are rejected and repeated saves produce one normalized write`() = runTest {
        val repository = FakeRepository()
        val gate = CompletableDeferred<Unit>()
        repository.onWrite = { gate.await() }
        GroupEditorStore(repository, initialColor = GroupColor.Blue, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            store.accept(GroupEditorIntent.NameChanged("   "))
            store.accept(GroupEditorIntent.Save)
            runCurrent()
            assertFalse(store.state.value.canSave)
            assertEquals(0, repository.writes)
            store.accept(GroupEditorIntent.NameChanged(" Work "))
            store.accept(GroupEditorIntent.ColorChanged(GroupColor.Red))
            repeat(5) { store.accept(GroupEditorIntent.Save) }
            runCurrent()
            assertTrue(store.state.value.isSaving)
            assertEquals(1, repository.writes)
            store.accept(GroupEditorIntent.NameChanged("Changed while saving"))
            runCurrent()
            assertEquals(" Work ", store.state.value.name)
            gate.complete(Unit)
            advanceUntilIdle()
            assertEquals("Work", repository.lastWrite?.name)
            assertEquals(GroupColor.Red, repository.lastWrite?.color)
            assertTrue(store.state.value.isFinished)
            store.accept(GroupEditorIntent.Save)
            advanceUntilIdle()
            assertEquals(1, repository.writes)
        }
    }

    @Test
    fun `name conflicts and storage failures keep the draft and allow retry`() = runTest {
        val repository = FakeRepository()
        repository.result = GroupWriteResult.NameConflict
        GroupEditorStore(repository, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            store.accept(GroupEditorIntent.NameChanged("Work"))
            store.accept(GroupEditorIntent.Save)
            advanceUntilIdle()
            assertEquals(GroupEditorDialog.NameConflict, store.state.value.dialog)
            assertEquals("Work", store.state.value.name)
            assertFalse(store.state.value.isFinished)
            repository.result = GroupWriteResult.Saved
            repository.onWrite = { error("Disk unavailable") }
            store.accept(GroupEditorIntent.DismissDialog)
            store.accept(GroupEditorIntent.Save)
            advanceUntilIdle()
            assertTrue(store.state.value.dialog is GroupEditorDialog.Error)
            assertFalse(store.state.value.isSaving)
            repository.onWrite = {}
            store.accept(GroupEditorIntent.DismissDialog)
            store.accept(GroupEditorIntent.Save)
            advanceUntilIdle()
            assertTrue(store.state.value.isFinished)
        }
    }

    @Test
    fun `editing ignores surrounding whitespace and saves color changes with the original id`() = runTest {
        val repository = FakeRepository()
        GroupEditorStore(repository, groupId = 7, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            advanceUntilIdle()
            assertEquals("Work", store.state.value.name)
            assertFalse(store.state.value.canSave)
            store.accept(GroupEditorIntent.NameChanged(" Work "))
            advanceUntilIdle()
            assertFalse(store.state.value.canSave)
            store.accept(GroupEditorIntent.ColorChanged(GroupColor.Green))
            advanceUntilIdle()
            assertTrue(store.state.value.canSave)
            store.accept(GroupEditorIntent.Save)
            advanceUntilIdle()
            assertEquals(Group(7, "Work", GroupColor.Green), repository.lastWrite)
            assertTrue(store.state.value.isFinished)
        }
    }

    @Test
    fun `delete requires confirmation and cancelling preserves the group`() = runTest {
        val repository = FakeRepository()
        GroupEditorStore(repository, groupId = 7, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            advanceUntilIdle()
            store.accept(GroupEditorIntent.ConfirmDelete)
            advanceUntilIdle()
            assertEquals(0, repository.deletions)
            store.accept(GroupEditorIntent.PrepareDelete)
            advanceUntilIdle()
            assertEquals(GroupEditorDialog.ConfirmDelete, store.state.value.dialog)
            store.accept(GroupEditorIntent.DismissDialog)
            advanceUntilIdle()
            assertNull(store.state.value.dialog)
            assertEquals(0, repository.deletions)
            store.accept(GroupEditorIntent.PrepareDelete)
            repeat(5) { store.accept(GroupEditorIntent.ConfirmDelete) }
            advanceUntilIdle()
            assertEquals(1, repository.deletions)
            assertTrue(store.state.value.isFinished)
        }
    }

    @Test
    fun `load failures and missing groups do not leave a permanent spinner`() = runTest {
        val repository = FakeRepository()
        repository.failRead = true
        GroupEditorStore(repository, groupId = 7, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            advanceUntilIdle()
            assertFalse(store.state.value.isLoading)
            assertNotNull(store.state.value.loadError)
            assertFalse(store.state.value.canSave)
            repository.failRead = false
            store.accept(GroupEditorIntent.RetryLoad)
            advanceUntilIdle()
            assertNull(store.state.value.loadError)
            assertEquals("Work", store.state.value.name)
        }
        repository.group = null
        GroupEditorStore(repository, groupId = 7, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            advanceUntilIdle()
            assertFalse(store.state.value.isLoading)
            assertNotNull(store.state.value.loadError)
            assertFalse(store.state.value.canSave)
        }
    }

    @Test
    fun `closing an editor cancels its operation`() = runTest {
        val repository = FakeRepository()
        var cancelled = false
        repository.onWrite = { try { awaitCancellation() } finally { cancelled = true } }
        val store = GroupEditorStore(repository, dispatcher = StandardTestDispatcher(testScheduler))
        try {
            store.accept(GroupEditorIntent.NameChanged("Work"))
            store.accept(GroupEditorIntent.Save)
            runCurrent()
            assertTrue(store.state.value.isSaving)
            store.close()
            runCurrent()
            assertTrue(cancelled)
            assertFalse(store.state.value.isFinished)
        } finally { store.close() }
    }

    @Test
    fun `list observes repository updates retries failures and releases its subscription`() = runTest {
        val repository = FakeRepository()
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

    private class FakeRepository : GroupRepository {
        val values = MutableStateFlow(emptyList<Group>())
        var group: Group? = Group(7, "Work", GroupColor.Blue)
        var failRead = false
        var result = GroupWriteResult.Saved
        var writes = 0
        var deletions = 0
        var lastWrite: Group? = null
        var subscriptions = 0
        var onWrite: suspend () -> Unit = {}
        override val groups = flow {
            if (failRead) error("Storage unavailable")
            subscriptions++
            try { emitAll(values) } finally { subscriptions-- }
        }
        override suspend fun getById(id: Int): Group? {
            if (failRead) error("Storage unavailable")
            return group
        }
        override suspend fun add(name: String, color: GroupColor): GroupWriteResult = update(Group(0, name, color))
        override suspend fun update(group: Group): GroupWriteResult {
            writes++
            onWrite()
            lastWrite = group
            return result
        }
        override suspend fun delete(id: Int) { deletions++ }
    }
}
