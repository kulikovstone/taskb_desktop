package com.mkulikov.taskb.feature.hello.screen.hello

internal sealed interface HelloIntent {
    data object Continue : HelloIntent
}
