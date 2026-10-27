package com.mkulikov.taskb.feature.task.api.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ITaskDao {
    @Query("SELECT * FROM tasks ORDER BY id")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): TaskEntity?

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity): Int

    @Query("UPDATE tasks SET name = :name, dueDate = :dueDate, groupId = :groupId WHERE id = :id")
    suspend fun updateDetails(id: Int, name: String, dueDate: Long?, groupId: Int?): Int

    @Query("UPDATE tasks SET completed = :completed WHERE id = :id")
    suspend fun setCompleted(id: Int, completed: Boolean)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Int)
}
