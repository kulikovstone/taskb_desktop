package com.mkulikov.taskb.feature.task.repository

import com.mkulikov.taskb.feature.task.api.database.ITaskDao
import com.mkulikov.taskb.feature.task.api.database.TaskEntity
import com.mkulikov.taskb.feature.task.model.Task
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

internal class TaskRepository(private val dao: Lazy<ITaskDao>) : ITaskRepository {
    override val tasks = flow { emitAll(dao.value.observeTasks().map { list -> list.map { it.toTask() } }) }

    override suspend fun getById(id: Int): Task? = dao.value.getById(id)?.toTask()

    override suspend fun save(task: Task): Boolean {
        val name = task.name.trim().also { require(it.isNotEmpty()) }
        val entity = TaskEntity(task.id, name, task.dueDate?.toEpochDay(), task.groupId, task.completed)
        return if (task.id == 0) dao.value.insert(entity) > 0
        else dao.value.updateDetails(task.id, name, entity.dueDate, task.groupId) > 0
    }

    override suspend fun setCompleted(id: Int, completed: Boolean) = dao.value.setCompleted(id, completed)
    override suspend fun delete(id: Int) = dao.value.deleteById(id)

    private fun TaskEntity.toTask() = Task(id, name, dueDate?.let(LocalDate::ofEpochDay), groupId, completed)
}
