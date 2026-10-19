package com.mkulikov.taskb

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.mkulikov.taskb.core.navigation.rememberEntryResourceNavEntryDecorator
import com.mkulikov.taskb.core.navigation.rememberNavEntryResource
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame

@OptIn(ExperimentalTestApi::class)
class NavEntryResourcesUiTest {
    @Test
    fun `covered entry keeps its resource and popped entry gets a fresh one`() = runComposeUiTest {
        val stack = mutableStateListOf<NavKey>(Route(1))
        val resources = mutableMapOf<Int, MutableList<Resource>>()
        val composed = mutableSetOf<Int>()
        setContent { TestHost(stack, resources, composed) }
        onNodeWithText("Screen 1").assertIsDisplayed()
        val first = runOnIdle { resources.getValue(1).single() }

        runOnIdle { stack.add(Route(2)) }
        waitForIdle()
        onNodeWithText("Screen 2").assertIsDisplayed()
        val second = runOnIdle {
            assertFalse(1 in composed)
            assertEquals(0, first.closes)
            resources.getValue(2).single()
        }

        runOnIdle { stack.removeLast() }
        waitForIdle()
        onNodeWithText("Screen 1").assertIsDisplayed()
        runOnIdle {
            assertEquals(1, second.closes)
            assertEquals(0, first.closes)
            assertSame(first, resources.getValue(1).single())
            stack.add(Route(2))
        }
        waitForIdle()
        runOnIdle {
            assertEquals(2, resources.getValue(2).size)
            assertNotSame(second, resources.getValue(2).last())
            assertEquals(0, resources.getValue(2).last().closes)
        }
    }

    @Test
    fun `removing a covered entry closes only its resource`() = runComposeUiTest {
        val stack = mutableStateListOf<NavKey>(Route(1))
        val resources = mutableMapOf<Int, MutableList<Resource>>()
        setContent { TestHost(stack, resources, mutableSetOf()) }
        runOnIdle { stack.add(Route(2)) }
        waitForIdle()
        runOnIdle { stack.removeFirst() }
        waitForIdle()
        onNodeWithText("Screen 2").assertIsDisplayed()
        runOnIdle {
            assertEquals(1, resources.getValue(1).single().closes)
            assertEquals(0, resources.getValue(2).single().closes)
        }
    }

    @Test
    fun `removing navigation host closes visible and covered resources`() = runComposeUiTest {
        val stack = mutableStateListOf<NavKey>(Route(1))
        val resources = mutableMapOf<Int, MutableList<Resource>>()
        var showHost by mutableStateOf(true)
        setContent { if (showHost) TestHost(stack, resources, mutableSetOf()) }
        runOnIdle { stack.add(Route(2)) }
        waitForIdle()
        runOnIdle { showHost = false }
        waitForIdle()
        runOnIdle {
            assertEquals(1, resources.getValue(1).single().closes)
            assertEquals(1, resources.getValue(2).single().closes)
        }
    }

    @Composable
    private fun TestHost(
        stack: List<NavKey>,
        resources: MutableMap<Int, MutableList<Resource>>,
        composed: MutableSet<Int>,
    ) {
        MaterialTheme {
            NavDisplay(
                backStack = stack,
                onBack = {},
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberEntryResourceNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<Route> { route ->
                        rememberNavEntryResource(Resource::class) {
                            Resource().also { resources.getOrPut(route.id) { mutableListOf() }.add(it) }
                        }
                        DisposableEffect(route) {
                            composed.add(route.id)
                            onDispose { composed.remove(route.id) }
                        }
                        Text("Screen ${route.id}")
                    }
                },
            )
        }
    }

    private data class Route(val id: Int) : NavKey
    private class Resource : AutoCloseable {
        var closes = 0
        override fun close() { closes++ }
    }
}
