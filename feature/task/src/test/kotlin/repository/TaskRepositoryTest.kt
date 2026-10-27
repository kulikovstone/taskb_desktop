package com.mkulikov.taskb.feature.task.repository

import com.mkulikov.taskb.core.database.AppDatabase
import com.mkulikov.taskb.feature.task.api.database.GroupEntity
import com.mkulikov.taskb.feature.task.model.GroupTaskSummary
import com.mkulikov.taskb.feature.task.model.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.LocalDate
import kotlin.test.*

class TaskRepositoryTest {
    @TempDir lateinit var directory: Path
    @Test
    fun `tasks persist with dates groups and completion and details save preserves newer completion`() = runBlocking {
        val file = directory.resolve("tasks.db")
        val date = LocalDate.of(2028, 2, 29)
        val database = AppDatabase.open(file)
        try {
            val group = database.groupDao().insert(GroupEntity(name = "Work", color = 0)).toInt()
            val repository = TaskRepository(lazy { database.taskDao() })
            assertTrue(repository.save(Task(name = " Task ", dueDate = date, groupId = group)))
            val task = repository.tasks.first().single()
            assertEquals("Task", task.name)
            assertEquals(date, task.dueDate)
            val groups = GroupRepository(lazy { database.groupDao() })
            assertEquals(GroupTaskSummary(1, 0), groups.groups.first().single().tasks)
            repository.setCompleted(task.id, true)
            assertTrue(repository.save(task.copy(name = "Renamed")))
            assertTrue(assertNotNull(repository.getById(task.id)).completed)
            assertEquals(GroupTaskSummary(0, 1), groups.getById(group)?.tasks)
            assertFalse(repository.save(task.copy(id = 999)))
            assertFailsWith<IllegalArgumentException> { repository.save(Task(name = "  ")) }
        } finally { database.close() }
        val reopened = AppDatabase.open(file)
        try {
            val repository = TaskRepository(lazy { reopened.taskDao() })
            val task = repository.tasks.first().single()
            assertEquals("Renamed", task.name)
            assertEquals(date, task.dueDate)
            assertTrue(task.completed)
            reopened.groupDao().deleteById(requireNotNull(task.groupId))
            assertNull(assertNotNull(repository.getById(task.id)).groupId)
            repository.delete(task.id)
            assertTrue(repository.tasks.first().isEmpty())
        } finally { reopened.close() }
    }
}
