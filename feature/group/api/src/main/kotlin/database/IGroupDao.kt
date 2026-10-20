package com.mkulikov.taskb.feature.group.api.database

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

    @Query("SELECT * FROM `groups` WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(group: GroupEntity): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(group: GroupEntity): Int

    @Query("DELETE FROM `groups` WHERE id = :id")
    suspend fun deleteById(id: Int)
}
