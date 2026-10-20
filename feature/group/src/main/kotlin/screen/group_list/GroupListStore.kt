package com.mkulikov.taskb.feature.group.screen.group_list

import com.mkulikov.taskb.core.mvi.CoroutineStore
import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.feature.group.repository.GroupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal class GroupListStore(
    private val repository: GroupRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineStore<GroupListIntent, GroupListState, GroupListMessage>(
    initialState = GroupListState(),
    reducer = ::reduceGroupList,
    context = dispatcher,
) {
    private var observation: Job? = null

    init { accept(GroupListIntent.Retry) }

    override fun handleIntent(intent: GroupListIntent) {
        when (intent) {
            GroupListIntent.Retry -> {
                if (observation?.isActive == true) return
                reduce(GroupListMessage.Loading)
                observation = collectEffect(
                    repository.groups
                        .map<List<Group>, GroupListMessage> { GroupListMessage.Loaded(it) }
                        .catch { emit(GroupListMessage.Failed) },
                )
            }
        }
    }
}
