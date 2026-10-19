package com.mkulikov.taskb.feature.group.repository

import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.feature.group.model.GroupColor
import kotlinx.coroutines.flow.Flow

internal interface GroupRepository {
    val groups: Flow<List<Group>>
    suspend fun getById(id: Int): Group?
    suspend fun add(name: String, color: GroupColor): GroupWriteResult
    suspend fun update(group: Group): GroupWriteResult
    suspend fun delete(id: Int)
}

internal enum class GroupWriteResult { Saved, NameConflict, NotFound }
