package com.mkulikov.taskb.feature.hello

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.mkulikov.taskb.feature.hello.api.HelloRoute

fun EntryProviderScope<NavKey>.helloEntry(
    initialError: String? = null,
    onContinue: suspend () -> Unit
) {
    entry<HelloRoute> {
        HelloScreen(initialError = initialError, onContinue = onContinue)
    }
}
