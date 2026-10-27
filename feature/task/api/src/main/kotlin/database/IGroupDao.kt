package com.mkulikov.taskb.feature.task.api.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IGroupDao {
    @Query("SELECT * FROM `groups` ORDER BY id")
    fun observeGroups(): Flow<List<GroupEntity>>

    @Query("""
        SELECT g.*, COUNT(CASE WHEN t.completed = 0 THEN 1 END) AS planned,
            COUNT(CASE WHEN t.completed = 1 THEN 1 END) AS completed
        FROM `groups` g LEFT JOIN tasks t ON t.groupId = g.id GROUP BY g.id ORDER BY g.id
    """)
    fun observeGroupsWithTaskCounts(): Flow<List<GroupWithTaskCounts>>

    @Query("""
        SELECT g.*, COUNT(CASE WHEN t.completed = 0 THEN 1 END) AS planned,
            COUNT(CASE WHEN t.completed = 1 THEN 1 END) AS completed
        FROM `groups` g LEFT JOIN tasks t ON t.groupId = g.id WHERE g.id = :id GROUP BY g.id
    """)
    suspend fun getWithTaskCounts(id: Int): GroupWithTaskCounts?

    @Query("SELECT * FROM `groups` WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(group: GroupEntity): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(group: GroupEntity): Int

    @Query("DELETE FROM `groups` WHERE id = :id")
    suspend fun deleteById(id: Int)
}
