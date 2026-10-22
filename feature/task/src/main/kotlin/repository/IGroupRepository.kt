package com.mkulikov.taskb.feature.task.repository

import com.mkulikov.taskb.feature.task.model.Group
import com.mkulikov.taskb.feature.task.model.GroupColor
import kotlinx.coroutines.flow.Flow

internal interface IGroupRepository {
    val groups: Flow<List<Group>>
    suspend fun getById(id: Int): Group?
    suspend fun add(name: String, color: GroupColor): GroupWriteResult
    suspend fun update(group: Group): GroupWriteResult
    suspend fun delete(id: Int)
}
