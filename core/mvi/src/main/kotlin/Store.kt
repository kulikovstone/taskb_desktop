package com.mkulikov.taskb.core.mvi

import kotlinx.coroutines.flow.StateFlow

interface Store<in Intent : Any, out State : Any> : AutoCloseable {
    val state: StateFlow<State>
    fun accept(intent: Intent)
}
