package com.mkulikov.taskb.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavEntryDecorator

/** Keeps a resource alive while its Navigation 3 content key remains in the back stack. */
@Composable
fun <T : AutoCloseable> rememberNavEntryResource(key: Any, factory: () -> T): T {
    val resources = LocalNavEntryResources.current
    return remember(resources, key) { resources.getOrCreate(key, factory) }
}

@Composable
fun <T : Any> rememberEntryResourceNavEntryDecorator(): NavEntryDecorator<T> {
    val owner = remember { NavEntryResourceOwner() }
    return remember(owner) {
        NavEntryDecorator(
            onPop = owner::remove,
            decorate = { entry ->
                CompositionLocalProvider(LocalNavEntryResources provides owner.resourcesFor(entry.contentKey)) {
                    entry.Content()
                }
            },
        )
    }
}

private val LocalNavEntryResources = staticCompositionLocalOf<NavEntryResources> {
    error("NavEntry resources require rememberEntryResourceNavEntryDecorator")
}

internal class NavEntryResources : AutoCloseable {
    private val resources = mutableMapOf<Any, AutoCloseable>()
    private var closed = false

    @Suppress("UNCHECKED_CAST")
    fun <T : AutoCloseable> getOrCreate(key: Any, factory: () -> T): T {
        check(!closed) { "NavEntry resources are already closed" }
        return resources.getOrPut(key, factory) as T
    }

    override fun close() {
        if (closed) return
        closed = true
        val toClose = resources.values.toList()
        resources.clear()
        closeAll(toClose)
    }
}

internal class NavEntryResourceOwner : RememberObserver, AutoCloseable {
    private val entries = mutableMapOf<Any, NavEntryResources>()
    private var closed = false

    fun resourcesFor(contentKey: Any): NavEntryResources {
        check(!closed) { "NavEntry resource owner is already closed" }
        return entries.getOrPut(contentKey) { NavEntryResources() }
    }

    fun remove(contentKey: Any) {
        entries.remove(contentKey)?.close()
    }

    override fun close() {
        if (closed) return
        closed = true
        val toClose = entries.values.toList()
        entries.clear()
        closeAll(toClose)
    }

    override fun onRemembered() = Unit
    override fun onForgotten() = close()
    override fun onAbandoned() = close()
}

private fun closeAll(resources: List<AutoCloseable>) {
    var failure: Throwable? = null
    resources.asReversed().forEach { resource ->
        try {
            resource.close()
        } catch (exception: Throwable) {
            val previousFailure = failure
            if (previousFailure == null) failure = exception else previousFailure.addSuppressed(exception)
        }
    }
    failure?.let { throw it }
}
