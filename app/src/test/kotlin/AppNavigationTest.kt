package com.mkulikov.taskb

import androidx.navigation3.runtime.NavKey
import com.mkulikov.taskb.feature.hello.api.HelloRoute
import com.mkulikov.taskb.feature.home.api.HomeRoute
import com.mkulikov.taskb.feature.task.api.TaskRoute
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AppNavigationTest {
    @Test
    fun `first launch starts with hello`() {
        assertEquals(listOf<NavKey>(HelloRoute), AppNavigation(hasCompletedWelcome = false).backStack.toList())
    }

    @Test
    fun `returning user starts with home`() {
        assertEquals(listOf<NavKey>(HomeRoute), AppNavigation(hasCompletedWelcome = true).backStack.toList())
    }

    @Test
    fun `completed hello is removed from navigation history`() {
        val navigation = AppNavigation(hasCompletedWelcome = false)
        navigation.completeHello()
        navigation.back()
        assertEquals(listOf<NavKey>(HomeRoute), navigation.backStack.toList())
    }

    @Test
    fun `back returns to home and keeps the root screen`() {
        val navigation = AppNavigation(hasCompletedWelcome = true)
        navigation.backStack.add(TaskRoute)
        navigation.back()
        navigation.back()
        assertEquals(listOf<NavKey>(HomeRoute), navigation.backStack.toList())
    }
}
