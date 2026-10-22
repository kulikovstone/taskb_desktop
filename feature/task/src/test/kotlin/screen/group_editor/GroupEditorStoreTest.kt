package com.mkulikov.taskb.feature.task.screen.group_editor

import com.mkulikov.taskb.feature.task.model.GroupColor
import com.mkulikov.taskb.feature.task.model.GroupTaskSummary
import com.mkulikov.taskb.feature.task.repository.GroupWriteResult
import com.mkulikov.taskb.feature.task.testing.FakeGroupRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GroupEditorStoreTest {
    @Test
    fun `creation validates trims and prevents double submissions and editing during a write`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val repository = FakeGroupRepository().apply { onWrite = { gate.await() } }
        GroupEditorStore(repository, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            store.accept(GroupEditorIntent.NameChanged("  "))
            store.accept(GroupEditorIntent.Save)
            runCurrent()
            assertEquals(0, repository.writes)
            store.accept(GroupEditorIntent.NameChanged(" Work "))
            store.accept(GroupEditorIntent.ColorChanged(GroupColor.Green))
            repeat(4) { store.accept(GroupEditorIntent.Save) }
            runCurrent()
            assertEquals(1, repository.writes)
            store.accept(GroupEditorIntent.NameChanged("Lost edit"))
            runCurrent()
            assertEquals(" Work ", store.state.value.name)
            gate.complete(Unit)
            advanceUntilIdle()
            assertEquals("Work", repository.lastWrite?.name)
            assertEquals(GroupColor.Green, repository.lastWrite?.color)
            assertTrue(store.state.value.isFinished)
        }
    }

    @Test
    fun `conflict and failed save preserve edit draft and permit retry`() = runTest {
        val repository = FakeGroupRepository()
        GroupEditorStore(repository, 7, StandardTestDispatcher(testScheduler)).use { store ->
            advanceUntilIdle()
            assertFalse(store.state.value.canSave)
            store.accept(GroupEditorIntent.NameChanged("Personal"))
            repository.result = GroupWriteResult.NameConflict
            store.accept(GroupEditorIntent.Save)
            advanceUntilIdle()
            assertEquals(GroupEditorDialog.NameConflict, store.state.value.dialog)
            store.accept(GroupEditorIntent.DismissDialog)
            repository.onWrite = { error("Disk full") }
            store.accept(GroupEditorIntent.Save)
            advanceUntilIdle()
            assertTrue(store.state.value.dialog is GroupEditorDialog.Error)
            assertEquals("Personal", store.state.value.name)
            store.accept(GroupEditorIntent.DismissDialog)
            repository.onWrite = {}
            repository.result = GroupWriteResult.Saved
            store.accept(GroupEditorIntent.Save)
            advanceUntilIdle()
            assertTrue(store.state.value.isFinished)
        }
    }

    @Test
    fun `deletion requires confirmation and duplicate confirmations delete once for empty and populated groups`() = runTest {
        for (hasTasks in listOf(false, true)) {
            val repository = FakeGroupRepository().apply {
                if (hasTasks) group = requireNotNull(group).copy(tasks = GroupTaskSummary(completed = 1))
            }
            GroupEditorStore(repository, 7, StandardTestDispatcher(testScheduler)).use { store ->
                advanceUntilIdle()
                store.accept(GroupEditorIntent.ConfirmDelete)
                advanceUntilIdle()
                assertEquals(0, repository.deletions)
                store.accept(GroupEditorIntent.PrepareDelete)
                advanceUntilIdle()
                assertEquals(GroupEditorDialog.ConfirmDelete, store.state.value.dialog)
                store.accept(GroupEditorIntent.DismissDialog)
                store.accept(GroupEditorIntent.ConfirmDelete)
                advanceUntilIdle()
                assertEquals(0, repository.deletions)
                store.accept(GroupEditorIntent.PrepareDelete)
                repeat(3) { store.accept(GroupEditorIntent.ConfirmDelete) }
                advanceUntilIdle()
                assertEquals(1, repository.deletions)
            }
        }
    }

    @Test
    fun `closing cancels writes and missing groups can never be saved`() = runTest {
        var cancelled = false
        val repository = FakeGroupRepository().apply {
            onWrite = { try { awaitCancellation() } finally { cancelled = true } }
        }
        val store = GroupEditorStore(repository, dispatcher = StandardTestDispatcher(testScheduler))
        store.accept(GroupEditorIntent.NameChanged("Work"))
        store.accept(GroupEditorIntent.Save)
        runCurrent()
        store.close()
        advanceUntilIdle()
        assertTrue(cancelled)
        repository.group = null
        GroupEditorStore(repository, 7, StandardTestDispatcher(testScheduler)).use { missing ->
            advanceUntilIdle()
            assertFalse(missing.state.value.canSave)
            assertEquals("This group no longer exists.", missing.state.value.loadError)
        }
    }
}
