package com.mkulikov.taskb.feature.task.testing

import com.mkulikov.taskb.feature.task.model.Task
import com.mkulikov.taskb.feature.task.repository.ITaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll

internal class FakeTaskRepository : ITaskRepository {
    val values = MutableStateFlow(emptyList<Task>())
    var fail = false
    var writes = 0
    var deletions = 0
    var subscriptions = 0
    var lastWrite: Task? = null
    var onWrite: suspend () -> Unit = {}
    override val tasks = flow {
        if (fail) error("Read failed")
        subscriptions++
        try { emitAll(values) } finally { subscriptions-- }
    }
    override suspend fun getById(id: Int): Task? {
        if (fail) error("Read failed")
        return values.value.find { it.id == id }
    }
    override suspend fun save(task: Task): Boolean {
        writes++
        onWrite()
        if (fail) error("Write failed")
        lastWrite = task
        if (task.id != 0 && values.value.none { it.id == task.id }) return false
        values.value = if (task.id == 0) values.value + task.copy(id = (values.value.maxOfOrNull { it.id } ?: 0) + 1)
            else values.value.map { if (it.id == task.id) task else it }
        return true
    }
    override suspend fun setCompleted(id: Int, completed: Boolean) {
        onWrite()
        if (fail) error("Write failed")
        values.value = values.value.map { if (it.id == id) it.copy(completed = completed) else it }
    }
    override suspend fun delete(id: Int) { deletions++; onWrite(); values.value = values.value.filter { it.id != id } }
}
