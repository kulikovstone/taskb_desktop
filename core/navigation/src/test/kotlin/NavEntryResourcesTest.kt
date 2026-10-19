package com.mkulikov.taskb.core.navigation

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class NavEntryResourcesTest {
    @Test
    fun `resources belong to their entry and are closed exactly once`() {
        val owner = NavEntryResourceOwner()
        val first = owner.resourcesFor("first").getOrCreate("store") { Resource() }
        val second = owner.resourcesFor("second").getOrCreate("store") { Resource() }
        assertSame(first, owner.resourcesFor("first").getOrCreate("store") { Resource() })
        assertNotSame(first, second)

        owner.remove("first")
        owner.remove("first")
        assertEquals(1, first.closes)
        assertEquals(0, second.closes)
        owner.close()
        owner.close()
        assertEquals(1, first.closes)
        assertEquals(1, second.closes)
    }

    @Test
    fun `disposing or abandoning the host closes its resources`() {
        listOf<(NavEntryResourceOwner) -> Unit>({ it.onForgotten() }, { it.onAbandoned() }).forEach { dispose ->
            val owner = NavEntryResourceOwner()
            val resource = owner.resourcesFor("entry").getOrCreate("store") { Resource() }
            dispose(owner)
            assertEquals(1, resource.closes)
            assertFailsWith<IllegalStateException> { owner.resourcesFor("entry") }
        }
    }

    @Test
    fun `failing cleanup does not prevent other resources from closing`() {
        val owner = NavEntryResourceOwner()
        val first = owner.resourcesFor("first").getOrCreate("store") { Resource() }
        owner.resourcesFor("second").getOrCreate("store") {
            AutoCloseable { error("Cleanup failure") }
        }
        assertFailsWith<IllegalStateException> { owner.close() }
        assertEquals(1, first.closes)
        owner.close()
        assertEquals(1, first.closes)
    }

    private class Resource : AutoCloseable {
        var closes = 0
        override fun close() { closes++ }
    }
}
