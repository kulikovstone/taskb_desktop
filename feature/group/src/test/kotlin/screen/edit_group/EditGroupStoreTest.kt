package com.mkulikov.taskb.feature.group.screen.edit_group

import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.feature.group.model.GroupColor
import com.mkulikov.taskb.feature.group.repository.GroupWriteResult
import com.mkulikov.taskb.feature.group.testing.FakeGroupRepository
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditGroupStoreTest {
    @Test
    fun `editing preserves the draft on conflict and storage failure and prevents duplicate updates`() = runTest {
        val repository = FakeGroupRepository()
        EditGroupStore(repository, groupId = 7, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            advanceUntilIdle()
            repository.result = GroupWriteResult.NameConflict
            store.accept(EditGroupIntent.NameChanged(" Personal "))
            store.accept(EditGroupIntent.Save)
            advanceUntilIdle()
            assertEquals(EditGroupDialog.NameConflict, store.state.value.dialog)
            assertEquals(" Personal ", store.state.value.name)
            assertFalse(store.state.value.isFinished)

            repository.onWrite = { error("Disk unavailable") }
            store.accept(EditGroupIntent.DismissDialog)
            store.accept(EditGroupIntent.Save)
            advanceUntilIdle()
            assertTrue(store.state.value.dialog is EditGroupDialog.Error)
            assertFalse(store.state.value.isSaving)
            assertEquals(" Personal ", store.state.value.name)

            val gate = CompletableDeferred<Unit>()
            repository.result = GroupWriteResult.Saved
            repository.onWrite = { gate.await() }
            store.accept(EditGroupIntent.DismissDialog)
            repeat(5) { store.accept(EditGroupIntent.Save) }
            runCurrent()
            assertEquals(3, repository.writes)
            assertTrue(store.state.value.isSaving)
            store.accept(EditGroupIntent.ColorChanged(GroupColor.Red))
            store.accept(EditGroupIntent.NameChanged("Changed while saving"))
            runCurrent()
            assertEquals(" Personal ", store.state.value.name)
            assertEquals(GroupColor.Blue, store.state.value.color)
            gate.complete(Unit)
            advanceUntilIdle()
            assertEquals(Group(7, "Personal", GroupColor.Blue), repository.lastWrite)
            assertTrue(store.state.value.isFinished)
        }
    }

    @Test
    fun `editing ignores surrounding whitespace and saves color changes with the original id`() = runTest {
        val repository = FakeGroupRepository()
        EditGroupStore(repository, groupId = 7, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            advanceUntilIdle()
            assertEquals("Work", store.state.value.name)
            assertFalse(store.state.value.canSave)
            store.accept(EditGroupIntent.NameChanged(" Work "))
            advanceUntilIdle()
            assertFalse(store.state.value.canSave)
            store.accept(EditGroupIntent.ColorChanged(GroupColor.Green))
            advanceUntilIdle()
            assertTrue(store.state.value.canSave)
            store.accept(EditGroupIntent.Save)
            advanceUntilIdle()
            assertEquals(Group(7, "Work", GroupColor.Green), repository.lastWrite)
            assertTrue(store.state.value.isFinished)
        }
    }

    @Test
    fun `delete requires confirmation and cancelling preserves the group`() = runTest {
        val repository = FakeGroupRepository()
        EditGroupStore(repository, groupId = 7, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            advanceUntilIdle()
            store.accept(EditGroupIntent.ConfirmDelete)
            advanceUntilIdle()
            assertEquals(0, repository.deletions)
            store.accept(EditGroupIntent.PrepareDelete)
            advanceUntilIdle()
            assertEquals(EditGroupDialog.ConfirmDelete, store.state.value.dialog)
            store.accept(EditGroupIntent.DismissDialog)
            advanceUntilIdle()
            assertNull(store.state.value.dialog)
            assertEquals(0, repository.deletions)
            store.accept(EditGroupIntent.PrepareDelete)
            repeat(5) { store.accept(EditGroupIntent.ConfirmDelete) }
            advanceUntilIdle()
            assertEquals(1, repository.deletions)
            assertTrue(store.state.value.isFinished)
        }
    }

    @Test
    fun `load failures and missing groups do not leave a permanent spinner`() = runTest {
        val repository = FakeGroupRepository()
        repository.failRead = true
        EditGroupStore(repository, groupId = 7, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            advanceUntilIdle()
            assertFalse(store.state.value.isLoading)
            assertNotNull(store.state.value.loadError)
            assertFalse(store.state.value.canSave)
            repository.failRead = false
            store.accept(EditGroupIntent.RetryLoad)
            advanceUntilIdle()
            assertNull(store.state.value.loadError)
            assertEquals("Work", store.state.value.name)
        }
        repository.group = null
        EditGroupStore(repository, groupId = 7, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            advanceUntilIdle()
            assertFalse(store.state.value.isLoading)
            assertNotNull(store.state.value.loadError)
            assertFalse(store.state.value.canSave)
        }
    }
}
