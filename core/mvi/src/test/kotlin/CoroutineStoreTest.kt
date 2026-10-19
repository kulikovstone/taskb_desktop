package com.mkulikov.taskb.core.mvi

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineStoreTest {
    @Test
    fun `flow updates use the reducer and subscription ends with the store`() = runTest {
        val updates = MutableSharedFlow<Int>()
        val store = object : CoroutineStore<Unit, Int, Int>(
            initialState = 0,
            reducer = Int::plus,
            context = StandardTestDispatcher(testScheduler),
        ) {
            override fun handleIntent(intent: Unit) {
                collectEffect(updates)
            }
        }
        try {
            store.accept(Unit)
            runCurrent()
            assertEquals(1, updates.subscriptionCount.value)
            updates.emit(2)
            runCurrent()
            updates.emit(3)
            runCurrent()
            assertEquals(5, store.state.value)

            store.close()
            advanceUntilIdle()
            assertEquals(0, updates.subscriptionCount.value)
            updates.emit(100)
            advanceUntilIdle()
            assertEquals(5, store.state.value)
        } finally {
            store.close()
        }
    }

    @Test
    fun `effects do not block intents and reduce against the latest state`() = runTest {
        val gate = CompletableDeferred<Unit>()
        CounterStore(StandardTestDispatcher(testScheduler)).use { store ->
            store.accept(Intent.Add(1))
            store.accept(Intent.AddLater(100, gate))
            store.accept(Intent.Add(2))
            store.accept(Intent.Add(3))
            runCurrent()
            assertEquals(6, store.state.value)

            gate.complete(Unit)
            advanceUntilIdle()
            assertEquals(106, store.state.value)
        }
    }

    @Test
    fun `concurrent producers do not lose state updates`() = runTest {
        CounterStore(StandardTestDispatcher(testScheduler)).use { store ->
            coroutineScope {
                repeat(8) {
                    launch(Dispatchers.Default) {
                        repeat(200) { store.accept(Intent.Add(1)) }
                    }
                }
            }
            advanceUntilIdle()
            assertEquals(1600, store.state.value)
        }
    }

    @Test
    fun `close cancels all effects and discards queued and future intents`() = runTest {
        var cancellations = 0
        val store = CounterStore(StandardTestDispatcher(testScheduler)) { cancellations++ }
        try {
            repeat(2) { store.accept(Intent.Wait) }
            runCurrent()
            store.accept(Intent.Add(10))
            store.close()
            store.close()
            store.accept(Intent.Add(100))
            advanceUntilIdle()
            assertEquals(2, cancellations)
            assertEquals(0, store.state.value)
        } finally {
            store.close()
        }
    }

    @Test
    fun `close before first dispatch prevents intent processing`() = runTest {
        val store = CounterStore(StandardTestDispatcher(testScheduler))
        store.accept(Intent.Add(1))
        store.close()
        advanceUntilIdle()
        assertEquals(0, store.state.value)
    }

    @Test
    fun `store has an owned job and respects parent cancellation`() = runTest {
        val parent = Job()
        val context = StandardTestDispatcher(testScheduler) + parent
        val sibling = CoroutineScope(context).launch { awaitCancellation() }
        var cancellations = 0
        val first = CounterStore(context) { cancellations++ }
        val second = CounterStore(context) { cancellations++ }
        try {
            first.accept(Intent.Wait)
            second.accept(Intent.Wait)
            runCurrent()
            first.close()
            runCurrent()
            assertEquals(1, cancellations)
            assertTrue(parent.isActive)
            assertTrue(sibling.isActive)

            parent.cancel()
            advanceUntilIdle()
            assertEquals(2, cancellations)
            assertFalse(sibling.isActive)
            second.accept(Intent.Add(1))
            advanceUntilIdle()
            assertEquals(0, second.state.value)
        } finally {
            first.close()
            second.close()
            parent.cancel()
        }
    }

    @Test
    fun `unhandled failures stop the store and cancel sibling effects`() = runTest {
        FailurePoint.entries.forEach { point ->
            val failures = mutableListOf<Throwable>()
            var cancelled = false
            val context = StandardTestDispatcher(testScheduler) +
                CoroutineExceptionHandler { _, failure -> failures.add(failure) }
            val store = object : CoroutineStore<Intent, Int, Int>(
                initialState = 0,
                reducer = { state, message ->
                    if (message == -1) error("Reducer failure")
                    state + message
                },
                context = context,
            ) {
                override fun handleIntent(intent: Intent) {
                    when (intent) {
                        Intent.Wait -> launchEffect {
                            try {
                                awaitCancellation()
                            } finally {
                                cancelled = true
                            }
                        }
                        Intent.Crash -> when (point) {
                            FailurePoint.Intent -> error("Intent failure")
                            FailurePoint.Reducer -> reduce(-1)
                            FailurePoint.Effect -> launchEffect { error("Effect failure") }
                        }
                        is Intent.Add -> reduce(intent.amount)
                        is Intent.AddLater -> error("Not used in this test")
                    }
                }
            }
            store.use {
                store.accept(Intent.Wait)
                runCurrent()
                store.accept(Intent.Crash)
                advanceUntilIdle()
                assertEquals(1, failures.size, point.name)
                assertTrue(cancelled, point.name)
                store.accept(Intent.Add(1))
                advanceUntilIdle()
                assertEquals(0, store.state.value, point.name)
            }
        }
    }

    private class CounterStore(
        context: CoroutineContext,
        private val onCancelled: () -> Unit = {},
    ) : CoroutineStore<Intent, Int, Int>(
        initialState = 0,
        reducer = Int::plus,
        context = context,
    ) {
        override fun handleIntent(intent: Intent) {
            when (intent) {
                is Intent.Add -> reduce(intent.amount)
                is Intent.AddLater -> launchEffect {
                    intent.gate.await()
                    intent.amount
                }
                Intent.Wait -> launchEffect {
                    try {
                        awaitCancellation()
                    } finally {
                        onCancelled()
                    }
                }
                Intent.Crash -> error("Not used in this test")
            }
        }
    }

    private sealed interface Intent {
        data class Add(val amount: Int) : Intent
        data class AddLater(val amount: Int, val gate: Deferred<Unit>) : Intent
        data object Wait : Intent
        data object Crash : Intent
    }

    private enum class FailurePoint { Intent, Reducer, Effect }
}
