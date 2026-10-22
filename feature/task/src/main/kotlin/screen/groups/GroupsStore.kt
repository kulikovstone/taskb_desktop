package com.mkulikov.taskb.feature.task.screen.groups

import com.mkulikov.taskb.core.mvi.CoroutineStore
import com.mkulikov.taskb.feature.task.model.Group
import com.mkulikov.taskb.feature.task.repository.IGroupRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal class GroupsStore(
    private val repository: IGroupRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineStore<GroupsIntent, GroupsState, GroupsMessage>(GroupsState(), ::reduceGroups, dispatcher) {
    private var observation: Job? = null

    init { accept(GroupsIntent.RetryLoad) }

    override fun handleIntent(intent: GroupsIntent) {
        when (intent) {
            is GroupsIntent.Search -> reduce(GroupsMessage.QueryChanged(intent.query))
            is GroupsIntent.Filter -> reduce(GroupsMessage.FilterChanged(intent.filter))
            is GroupsIntent.Sort -> reduce(GroupsMessage.SortChanged(intent.sort))
            is GroupsIntent.OpenGroup -> if (state.value.groups.any { it.id == intent.id }) {
                reduce(GroupsMessage.EditorChanged(GroupEditorTarget.Edit(intent.id)))
            }
            GroupsIntent.NewGroup -> reduce(GroupsMessage.EditorChanged(GroupEditorTarget.New))
            GroupsIntent.CloseEditor -> reduce(GroupsMessage.EditorChanged(null))
            GroupsIntent.RetryLoad -> if (observation?.isActive != true) {
                reduce(GroupsMessage.Loading)
                observation = collectEffect(repository.groups.map<List<Group>, GroupsMessage> {
                    GroupsMessage.Loaded(it)
                }.catch {
                    if (it is CancellationException) throw it
                    emit(GroupsMessage.LoadFailed)
                })
            }
        }
    }
}
