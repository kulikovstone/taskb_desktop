package com.mkulikov.taskb.core.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AppNavigatorTest {
    @Test
    fun `start destination initializes an empty stack`() {
        val navigator = AppNavigator()
        navigator.setStartDestination(FirstRoute)
        assertEquals(listOf<NavKey>(FirstRoute), navigator.backStack.toList())
    }

    @Test
    fun `recomposition does not reset existing navigation`() {
        val navigator = AppNavigator()
        navigator.setStartDestination(FirstRoute)
        navigator.navigate(SecondRoute)
        navigator.setStartDestination(FirstRoute)
        assertEquals(listOf<NavKey>(FirstRoute, SecondRoute), navigator.backStack.toList())
    }

    @Test
    fun `replaceAll removes previous navigation history`() {
        val navigator = AppNavigator()
        navigator.setStartDestination(FirstRoute)
        navigator.navigate(SecondRoute)
        navigator.replaceAll(ThirdRoute)
        navigator.back()
        assertEquals(listOf<NavKey>(ThirdRoute), navigator.backStack.toList())
    }

    @Test
    fun `back returns to previous screen and keeps the root`() {
        val navigator = AppNavigator()
        navigator.setStartDestination(FirstRoute)
        navigator.navigate(SecondRoute)
        navigator.back()
        navigator.back()
        assertEquals(listOf<NavKey>(FirstRoute), navigator.backStack.toList())
    }

    @Test
    fun `replacing a detail keeps one back step to its list`() {
        val navigator = AppNavigator()
        navigator.setStartDestination(FirstRoute)
        navigator.navigate(SecondRoute)
        navigator.replaceTop(ThirdRoute)
        assertEquals(listOf<NavKey>(FirstRoute, ThirdRoute), navigator.backStack.toList())
        navigator.back()
        assertEquals(listOf<NavKey>(FirstRoute), navigator.backStack.toList())
    }

    @Test
    fun `popUpTo can close a list and its detail together`() {
        val navigator = AppNavigator()
        navigator.setStartDestination(FirstRoute)
        navigator.navigate(SecondRoute)
        navigator.navigate(ThirdRoute)
        navigator.popUpTo(SecondRoute)
        assertEquals(listOf<NavKey>(FirstRoute, SecondRoute), navigator.backStack.toList())
        navigator.navigate(ThirdRoute)
        navigator.popUpTo(SecondRoute, inclusive = true)
        assertEquals(listOf<NavKey>(FirstRoute), navigator.backStack.toList())
    }

    @Test
    fun `popUpTo ignores missing destinations and keeps the root`() {
        val navigator = AppNavigator()
        navigator.setStartDestination(FirstRoute)
        navigator.navigate(SecondRoute)
        navigator.popUpTo(ThirdRoute, inclusive = true)
        assertEquals(listOf<NavKey>(FirstRoute, SecondRoute), navigator.backStack.toList())
        navigator.popUpTo(FirstRoute, inclusive = true)
        assertEquals(listOf<NavKey>(FirstRoute), navigator.backStack.toList())
    }

    private data object FirstRoute : NavKey
    private data object SecondRoute : NavKey
    private data object ThirdRoute : NavKey
}
