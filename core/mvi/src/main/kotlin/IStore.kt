package com.mkulikov.taskb.core.mvi

import kotlinx.coroutines.flow.StateFlow

interface IStore<in Intent : Any, out State : Any> : AutoCloseable {
    val state: StateFlow<State>
    fun accept(intent: Intent)
}
