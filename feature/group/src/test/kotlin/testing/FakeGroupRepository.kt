package com.mkulikov.taskb.feature.group.testing

import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.feature.group.model.GroupColor
import com.mkulikov.taskb.feature.group.repository.GroupRepository
import com.mkulikov.taskb.feature.group.repository.GroupWriteResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

internal class FakeGroupRepository : GroupRepository {
    val values = MutableStateFlow(emptyList<Group>())
    var group: Group? = Group(7, "Work", GroupColor.Blue)
    var failRead = false
    var result = GroupWriteResult.Saved
    var writes = 0
    var deletions = 0
    var lastWrite: Group? = null
    var subscriptions = 0
    var onWrite: suspend () -> Unit = {}
    override val groups = flow {
        if (failRead) error("Storage unavailable")
        subscriptions++
        try { emitAll(values) } finally { subscriptions-- }
    }
    override suspend fun getById(id: Int): Group? {
        if (failRead) error("Storage unavailable")
        return group
    }
    override suspend fun add(name: String, color: GroupColor): GroupWriteResult = update(Group(0, name, color))
    override suspend fun update(group: Group): GroupWriteResult {
        writes++
        onWrite()
        lastWrite = group
        return result
    }
    override suspend fun delete(id: Int) { deletions++ }
}
