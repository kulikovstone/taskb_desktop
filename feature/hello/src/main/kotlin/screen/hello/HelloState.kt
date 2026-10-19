package com.mkulikov.taskb.feature.hello.screen.hello

internal data class HelloState(
    val isContinuing: Boolean = false,
    val error: String? = null,
    val isCompleted: Boolean = false,
)
