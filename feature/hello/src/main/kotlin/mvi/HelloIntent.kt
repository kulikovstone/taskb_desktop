package com.mkulikov.taskb.feature.hello.mvi

internal sealed interface HelloIntent {
    data object Continue : HelloIntent
}
