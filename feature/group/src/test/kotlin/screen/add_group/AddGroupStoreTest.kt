package com.mkulikov.taskb.feature.group.screen.add_group

import com.mkulikov.taskb.feature.group.model.GroupColor
import com.mkulikov.taskb.feature.group.testing.FakeGroupRepository
import com.mkulikov.taskb.feature.group.repository.GroupWriteResult
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddGroupStoreTest {
    @Test
    fun `blank names are rejected and repeated saves produce one normalized write`() = runTest {
        val repository = FakeGroupRepository()
        val gate = CompletableDeferred<Unit>()
        repository.onWrite = { gate.await() }
        AddGroupStore(repository, initialColor = GroupColor.Blue, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            store.accept(AddGroupIntent.NameChanged("   "))
            store.accept(AddGroupIntent.Save)
            runCurrent()
            assertFalse(store.state.value.canSave)
            assertEquals(0, repository.writes)
            store.accept(AddGroupIntent.NameChanged(" Work "))
            store.accept(AddGroupIntent.ColorChanged(GroupColor.Red))
            repeat(5) { store.accept(AddGroupIntent.Save) }
            runCurrent()
            assertTrue(store.state.value.isSaving)
            assertEquals(1, repository.writes)
            store.accept(AddGroupIntent.NameChanged("Changed while saving"))
            runCurrent()
            assertEquals(" Work ", store.state.value.name)
            gate.complete(Unit)
            advanceUntilIdle()
            assertEquals("Work", repository.lastWrite?.name)
            assertEquals(GroupColor.Red, repository.lastWrite?.color)
            assertTrue(store.state.value.isFinished)
            store.accept(AddGroupIntent.Save)
            advanceUntilIdle()
            assertEquals(1, repository.writes)
        }
    }

    @Test
    fun `name conflicts and storage failures keep the draft and allow retry`() = runTest {
        val repository = FakeGroupRepository()
        repository.result = GroupWriteResult.NameConflict
        AddGroupStore(repository, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            store.accept(AddGroupIntent.NameChanged("Work"))
            store.accept(AddGroupIntent.Save)
            advanceUntilIdle()
            assertEquals(AddGroupDialog.NameConflict, store.state.value.dialog)
            assertEquals("Work", store.state.value.name)
            assertFalse(store.state.value.isFinished)
            repository.result = GroupWriteResult.Saved
            repository.onWrite = { error("Disk unavailable") }
            store.accept(AddGroupIntent.DismissDialog)
            store.accept(AddGroupIntent.Save)
            advanceUntilIdle()
            assertTrue(store.state.value.dialog is AddGroupDialog.Error)
            assertFalse(store.state.value.isSaving)
            repository.onWrite = {}
            store.accept(AddGroupIntent.DismissDialog)
            store.accept(AddGroupIntent.Save)
            advanceUntilIdle()
            assertTrue(store.state.value.isFinished)
        }
    }

    @Test
    fun `closing the add screen cancels its operation`() = runTest {
        val repository = FakeGroupRepository()
        var cancelled = false
        repository.onWrite = { try { awaitCancellation() } finally { cancelled = true } }
        val store = AddGroupStore(repository, dispatcher = StandardTestDispatcher(testScheduler))
        try {
            store.accept(AddGroupIntent.NameChanged("Work"))
            store.accept(AddGroupIntent.Save)
            runCurrent()
            assertTrue(store.state.value.isSaving)
            store.close()
            runCurrent()
            assertTrue(cancelled)
            assertFalse(store.state.value.isFinished)
        } finally { store.close() }
    }
}
