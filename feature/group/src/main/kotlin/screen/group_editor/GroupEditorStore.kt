package com.mkulikov.taskb.feature.group.screen.group_editor

import com.mkulikov.taskb.core.mvi.CoroutineStore
import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.feature.group.model.GroupColor
import com.mkulikov.taskb.feature.group.repository.GroupRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal data class GroupEditorArgs(val groupId: Int?)

internal class GroupEditorStore(
    private val repository: GroupRepository,
    groupId: Int? = null,
    initialColor: GroupColor = GroupColor.entries.random(),
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineStore<GroupEditorIntent, GroupEditorState, GroupEditorMessage>(
    initialState = GroupEditorState(groupId = groupId, color = initialColor, isLoading = groupId != null),
    reducer = ::reduceGroupEditor,
    context = dispatcher,
) {
    private var loadRequested = false

    init { if (groupId != null) accept(GroupEditorIntent.RetryLoad) }

    override fun handleIntent(intent: GroupEditorIntent) {
        val current = state.value
        if (current.isFinished) return
        when (intent) {
            is GroupEditorIntent.NameChanged -> if (!current.isBusy && current.loadError == null) {
                reduce(GroupEditorMessage.NameChanged(intent.name))
            }
            is GroupEditorIntent.ColorChanged -> if (!current.isBusy && current.loadError == null) {
                reduce(GroupEditorMessage.ColorChanged(intent.color))
            }
            GroupEditorIntent.Save -> {
                if (!current.canSave) return
                reduce(GroupEditorMessage.Saving)
                launchEffect {
                    recover(GroupEditorMessage.SaveFailed) {
                        val result = if (current.groupId == null) {
                            repository.add(current.normalizedName, current.color)
                        } else {
                            repository.update(Group(current.groupId, current.normalizedName, current.color))
                        }
                        GroupEditorMessage.SaveResult(result)
                    }
                }
            }
            GroupEditorIntent.PrepareDelete -> if (current.original != null && !current.isBusy) {
                reduce(GroupEditorMessage.AskDelete)
            }
            GroupEditorIntent.ConfirmDelete -> {
                if (current.dialog != GroupEditorDialog.ConfirmDelete || current.isBusy || current.groupId == null) return
                reduce(GroupEditorMessage.Deleting)
                launchEffect {
                    recover(GroupEditorMessage.DeleteFailed) {
                        repository.delete(current.groupId)
                        GroupEditorMessage.Deleted
                    }
                }
            }
            GroupEditorIntent.DismissDialog -> if (!current.isBusy) reduce(GroupEditorMessage.DismissDialog)
            GroupEditorIntent.RetryLoad -> {
                val id = current.groupId ?: return
                if (current.original != null || current.isSaving || current.isDeleting || current.isLoading && loadRequested) return
                loadRequested = true
                reduce(GroupEditorMessage.Loading)
                launchEffect { recover(GroupEditorMessage.LoadFailed) { GroupEditorMessage.Loaded(repository.getById(id)) } }
            }
        }
    }

    private suspend fun recover(failure: GroupEditorMessage, block: suspend () -> GroupEditorMessage): GroupEditorMessage = try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        failure
    }
}
