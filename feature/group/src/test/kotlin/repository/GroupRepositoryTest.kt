package com.mkulikov.taskb.feature.group.repository

import com.mkulikov.taskb.core.database.AppDatabase
import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.feature.group.model.GroupColor
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class GroupRepositoryTest {
    @TempDir lateinit var directory: Path

    @Test
    fun `flow publishes creation editing and deletion`() = runBlocking {
        val database = AppDatabase.open(directory.resolve("groups.db"))
        try {
            val repository = GroupRepository(lazy { database.groupDao() })
            val updates = Channel<List<Group>>(Channel.UNLIMITED)
            val observer = launch { repository.groups.collect { updates.send(it) } }
            try {
                assertEquals(emptyList(), withTimeout(5_000) { updates.receive() })
                assertEquals(GroupWriteResult.Saved, repository.add(" Work ", GroupColor.Blue))
                val group = withTimeout(5_000) { updates.receive() }.single()
                assertEquals("Work", group.name)
                assertEquals(GroupColor.Blue, group.color)
                assertEquals(GroupWriteResult.Saved, repository.update(group.copy(name = " Home ", color = GroupColor.Red)))
                val edited = withTimeout(5_000) { updates.receive() }.single()
                assertEquals(group.id, edited.id)
                assertEquals("Home", edited.name)
                assertEquals(GroupColor.Red, edited.color)
                repository.delete(group.id)
                assertEquals(emptyList(), withTimeout(5_000) { updates.receive() })
                assertNull(repository.getById(group.id))
            } finally { observer.cancelAndJoin() }
        } finally { database.close() }
    }

    @Test
    fun `names and colors survive reopening`() = runBlocking {
        val file = directory.resolve("groups.db")
        val database = AppDatabase.open(file)
        try {
            val repository = GroupRepository(lazy { database.groupDao() })
            repository.add("Personal", GroupColor.LightGreen)
            repository.add("Without a color", GroupColor.Transparent)
        } finally { database.close() }
        val reopened = AppDatabase.open(file)
        try {
            val groups = GroupRepository(lazy { reopened.groupDao() }).groups.first()
            assertEquals(listOf("Personal", "Without a color"), groups.map { it.name })
            assertEquals(listOf(GroupColor.LightGreen, GroupColor.Transparent), groups.map { it.color })
        } finally { reopened.close() }
    }

    @Test
    fun `case insensitive uniqueness protects inserts and updates`() = runBlocking {
        val database = AppDatabase.open(directory.resolve("groups.db"))
        try {
            val repository = GroupRepository(lazy { database.groupDao() })
            assertEquals(GroupWriteResult.Saved, repository.add("Work", GroupColor.Blue))
            assertEquals(GroupWriteResult.NameConflict, repository.add(" work ", GroupColor.Red))
            repository.add("Home", GroupColor.Green)
            val groups = repository.groups.first()
            val home = groups.last()
            assertEquals(GroupWriteResult.NameConflict, repository.update(home.copy(name = "WORK", color = GroupColor.Red)))
            assertEquals(home, repository.getById(home.id))
            val work = groups.first()
            assertEquals(GroupWriteResult.Saved, repository.update(work.copy(name = "work")))
            assertEquals("work", assertNotNull(repository.getById(work.id)).name)
            assertEquals(GroupWriteResult.NotFound, repository.update(Group(999, "Missing", GroupColor.Blue)))
            assertFailsWith<IllegalArgumentException> { repository.add("  ", GroupColor.Red) }
            assertEquals(2, repository.groups.first().size)
        } finally { database.close() }
    }

    @Test
    fun `concurrent inserts cannot create duplicate groups`() = runBlocking {
        val database = AppDatabase.open(directory.resolve("groups.db"))
        try {
            val repository = GroupRepository(lazy { database.groupDao() })
            val results = coroutineScope {
                List(12) { async { repository.add("Work", GroupColor.Blue) } }.awaitAll()
            }
            assertEquals(1, results.count { it == GroupWriteResult.Saved })
            assertEquals(11, results.count { it == GroupWriteResult.NameConflict })
            assertEquals(1, repository.groups.first().size)
        } finally { database.close() }
    }
}
