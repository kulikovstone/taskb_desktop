package com.mkulikov.taskb.feature.task.screen.group_editor

import com.mkulikov.taskb.core.mvi.CoroutineStore
import com.mkulikov.taskb.feature.task.model.GroupColor
import com.mkulikov.taskb.feature.task.repository.IGroupRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class GroupEditorStore(
    private val repository: IGroupRepository,
    private val groupId: Int? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineStore<GroupEditorIntent, GroupEditorState, GroupEditorMessage>(
    GroupEditorState(
        isNew = groupId == null,
        color = if (groupId == null) GroupColor.entries.random() else GroupColor.Transparent,
    ), ::reduceGroupEditor, dispatcher,
) {
    private var loadRequested = false

    init { if (groupId != null) accept(GroupEditorIntent.RetryLoad) }

    override fun handleIntent(intent: GroupEditorIntent) {
        val current = state.value
        if (current.isFinished) return
        when (intent) {
            is GroupEditorIntent.NameChanged -> if (!current.isBusy && current.loadError == null && current.dialog == null) {
                reduce(GroupEditorMessage.NameChanged(intent.name))
            }
            is GroupEditorIntent.ColorChanged -> if (!current.isBusy && current.loadError == null && current.dialog == null) {
                reduce(GroupEditorMessage.ColorChanged(intent.color))
            }
            GroupEditorIntent.Save -> {
                if (!current.canSave || current.dialog != null) return
                reduce(GroupEditorMessage.Saving)
                launchEffect {
                    recover(GroupEditorMessage.SaveFailed) {
                        val result = if (current.isNew) repository.add(current.normalizedName, current.color)
                        else repository.update(requireNotNull(current.original).copy(name = current.normalizedName, color = current.color))
                        GroupEditorMessage.Saved(result)
                    }
                }
            }
            GroupEditorIntent.PrepareDelete -> if (current.original != null && !current.isBusy && current.dialog == null) {
                reduce(GroupEditorMessage.AskDelete)
            }
            GroupEditorIntent.ConfirmDelete -> {
                if (current.dialog != GroupEditorDialog.ConfirmDelete || current.isBusy || groupId == null) return
                reduce(GroupEditorMessage.Deleting)
                launchEffect {
                    recover(GroupEditorMessage.DeleteFailed) {
                        repository.delete(groupId)
                        GroupEditorMessage.Deleted
                    }
                }
            }
            GroupEditorIntent.DismissDialog -> if (!current.isBusy) reduce(GroupEditorMessage.DismissDialog)
            GroupEditorIntent.RetryLoad -> {
                if (groupId == null || current.original != null || current.isSaving || current.isDeleting || current.isLoading && loadRequested) return
                loadRequested = true
                reduce(GroupEditorMessage.Loading)
                launchEffect { recover(GroupEditorMessage.LoadFailed) { GroupEditorMessage.Loaded(repository.getById(groupId)) } }
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
