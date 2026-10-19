package com.mkulikov.taskb.feature.hello.screen.hello

import com.mkulikov.taskb.feature.settings.api.IAppSettings
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
class HelloStoreTest {
    @Test
    fun `successful save publishes progress and completion`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val settings = FakeSettings { gate.await() }
        HelloStore(settings, "Initial error", StandardTestDispatcher(testScheduler)).use { store ->
            assertEquals("Initial error", store.state.value.error)
            store.accept(HelloIntent.Continue)
            runCurrent()
            assertTrue(store.state.value.isContinuing)
            assertEquals(null, store.state.value.error)
            assertFalse(store.state.value.isCompleted)

            gate.complete(Unit)
            advanceUntilIdle()
            assertTrue(store.state.value.isCompleted)
            assertFalse(store.state.value.isContinuing)
            assertEquals(1, settings.calls)
        }
    }

    @Test
    fun `failed save allows an explicit retry`() = runTest {
        var fail = true
        val settings = FakeSettings { if (fail) error("Storage failure") }
        HelloStore(settings, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            store.accept(HelloIntent.Continue)
            advanceUntilIdle()
            assertFalse(store.state.value.isContinuing)
            assertFalse(store.state.value.isCompleted)
            assertEquals("Не удалось сохранить настройки. Попробуйте ещё раз.", store.state.value.error)

            fail = false
            store.accept(HelloIntent.Continue)
            advanceUntilIdle()
            assertEquals(2, settings.calls)
            assertTrue(store.state.value.isCompleted)
            assertEquals(null, store.state.value.error)
        }
    }

    @Test
    fun `repeated intents do not duplicate an active or completed save`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val settings = FakeSettings { gate.await() }
        HelloStore(settings, dispatcher = StandardTestDispatcher(testScheduler)).use { store ->
            repeat(10) { store.accept(HelloIntent.Continue) }
            runCurrent()
            repeat(10) { store.accept(HelloIntent.Continue) }
            runCurrent()
            assertEquals(1, settings.calls)

            gate.complete(Unit)
            advanceUntilIdle()
            store.accept(HelloIntent.Continue)
            advanceUntilIdle()
            assertEquals(1, settings.calls)
        }
    }

    @Test
    fun `closing store cancels saving and stops processing intents`() = runTest {
        var cancelled = false
        val settings = FakeSettings {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val store = HelloStore(settings, dispatcher = StandardTestDispatcher(testScheduler))
        try {
            store.accept(HelloIntent.Continue)
            runCurrent()
            assertEquals(1, settings.calls)
            store.close()
            runCurrent()
            assertTrue(cancelled)
            assertFalse(store.state.value.isCompleted)

            store.accept(HelloIntent.Continue)
            advanceUntilIdle()
            assertEquals(1, settings.calls)
        } finally {
            store.close()
        }
    }

    private class FakeSettings(private val save: suspend () -> Unit) : IAppSettings {
        var calls = 0

        override suspend fun hasCompletedWelcome() = false
        override suspend fun completeWelcome() {
            calls++
            save()
        }
    }
}
