package com.mkulikov.taskb.feature.task.repository

import com.mkulikov.taskb.feature.task.model.Task
import kotlinx.coroutines.flow.Flow

internal interface ITaskRepository {
    val tasks: Flow<List<Task>>
    suspend fun getById(id: Int): Task?
    suspend fun save(task: Task): Boolean
    suspend fun setCompleted(id: Int, completed: Boolean)
    suspend fun delete(id: Int)
}
