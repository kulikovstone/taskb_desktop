package com.mkulikov.taskb.feature.task.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.mkulikov.taskb.core.navigation.IAppNavigator
import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.feature.task.TaskScreen
import com.mkulikov.taskb.feature.task.api.TaskRoute

internal class TaskNavEntry : INavEntry {
    override fun register(scope: EntryProviderScope<NavKey>, navigator: IAppNavigator) {
        scope.entry<TaskRoute> { TaskScreen() }
    }
}
