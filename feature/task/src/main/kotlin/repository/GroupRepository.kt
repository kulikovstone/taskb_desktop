package com.mkulikov.taskb.feature.task.repository

import androidx.compose.ui.graphics.toArgb
import com.mkulikov.taskb.feature.task.api.database.GroupEntity
import com.mkulikov.taskb.feature.task.api.database.IGroupDao
import com.mkulikov.taskb.feature.task.model.Group
import com.mkulikov.taskb.feature.task.model.GroupColor
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class GroupRepository(private val dao: Lazy<IGroupDao>) : IGroupRepository {
    override val groups = flow {
        emitAll(dao.value.observeGroups().map { entities -> entities.map { it.toGroup() } })
    }

    override suspend fun getById(id: Int): Group? = dao.value.getById(id)?.toGroup()

    override suspend fun add(name: String, color: GroupColor): GroupWriteResult {
        val normalized = name.trim().also { require(it.isNotEmpty()) }
        return if (dao.value.insert(GroupEntity(name = normalized, color = color.color.toArgb())) == -1L) {
            GroupWriteResult.NameConflict
        } else GroupWriteResult.Saved
    }

    override suspend fun update(group: Group): GroupWriteResult {
        val normalized = group.name.trim().also { require(it.isNotEmpty()) }
        val updated = dao.value.update(GroupEntity(group.id, normalized, group.color.color.toArgb()))
        return when {
            updated > 0 -> GroupWriteResult.Saved
            dao.value.getById(group.id) == null -> GroupWriteResult.NotFound
            else -> GroupWriteResult.NameConflict
        }
    }

    override suspend fun delete(id: Int) = dao.value.deleteById(id)

    private fun GroupEntity.toGroup() = Group(id, name, GroupColor.fromArgb(color))
}
