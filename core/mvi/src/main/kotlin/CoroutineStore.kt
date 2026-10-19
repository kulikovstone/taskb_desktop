package com.mkulikov.taskb.core.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Processes intents and effect results serially. Each instance owns its coroutine scope.
 * Recoverable failures should be converted to messages by the feature; an unhandled failure
 * cancels the store and its remaining work.
 */
abstract class CoroutineStore<Intent : Any, State : Any, Message : Any>(
    initialState: State,
    private val reducer: (State, Message) -> State,
    context: CoroutineContext = Dispatchers.Default,
) : Store<Intent, State> {
    private val job = Job(context[Job])
    private val scope = CoroutineScope(context + job)
    private val events = Channel<Event<Intent, Message>>(Channel.UNLIMITED)
    private val mutableState = MutableStateFlow(initialState)
    final override val state: StateFlow<State> = mutableState.asStateFlow()

    init {
        job.invokeOnCompletion { events.cancel() }
        scope.launch {
            try {
                for (event in events) {
                    ensureActive()
                    when (event) {
                        is Event.Input -> handleIntent(event.intent)
                        is Event.Result -> reduce(event.message)
                    }
                }
            } finally {
                close()
            }
        }
    }

    final override fun accept(intent: Intent) {
        events.trySend(Event.Input(intent))
    }

    protected abstract fun handleIntent(intent: Intent)

    /** Applies a message immediately from handleIntent, before the next intent is processed. */
    protected fun reduce(message: Message) {
        scope.ensureActive()
        mutableState.value = reducer(mutableState.value, message)
    }

    /** Runs asynchronous work and queues its result for the reducer. */
    protected fun launchEffect(block: suspend () -> Message): Job = collectEffect(flow { emit(block()) })

    /** Collects a stream, such as repository updates, until it completes or the store closes. */
    protected fun collectEffect(messages: Flow<Message>): Job = scope.launch {
        messages.collect { message ->
            events.send(Event.Result(message))
        }
    }

    final override fun close() {
        scope.cancel()
        events.cancel()
    }

    private sealed interface Event<out I, out M> {
        data class Input<I>(val intent: I) : Event<I, Nothing>
        data class Result<M>(val message: M) : Event<Nothing, M>
    }
}
