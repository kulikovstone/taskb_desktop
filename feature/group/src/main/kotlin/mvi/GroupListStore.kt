package com.mkulikov.taskb.feature.group.mvi

import com.mkulikov.taskb.core.mvi.CoroutineStore
import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.feature.group.repository.GroupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal data class GroupListState(
    val isLoading: Boolean = true,
    val groups: List<Group> = emptyList(),
    val error: String? = null,
)

internal sealed interface GroupListIntent {
    data object Retry : GroupListIntent
}

internal sealed interface GroupListMessage {
    data object Loading : GroupListMessage
    data class Loaded(val groups: List<Group>) : GroupListMessage
    data object Failed : GroupListMessage
}

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

private fun reduceGroupList(state: GroupListState, message: GroupListMessage): GroupListState = when (message) {
    GroupListMessage.Loading -> state.copy(isLoading = true, error = null)
    is GroupListMessage.Loaded -> state.copy(isLoading = false, groups = message.groups, error = null)
    GroupListMessage.Failed -> state.copy(isLoading = false, error = "Couldn't load groups. Please try again.")
}
