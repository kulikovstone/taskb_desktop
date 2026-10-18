package com.mkulikov.taskb.feature.task

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.mkulikov.taskb.feature.task.api.TaskRoute

fun EntryProviderScope<NavKey>.taskEntry() {
    entry<TaskRoute> { TaskScreen() }
}
