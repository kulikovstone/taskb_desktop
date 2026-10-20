package com.mkulikov.taskb.feature.group.screen.edit_group

import com.mkulikov.taskb.core.mvi.CoroutineStore
import com.mkulikov.taskb.feature.group.repository.GroupRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class EditGroupStore(
    private val repository: GroupRepository,
    private val groupId: Int,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineStore<EditGroupIntent, EditGroupState, EditGroupMessage>(
    initialState = EditGroupState(),
    reducer = ::reduceEditGroup,
    context = dispatcher,
) {
    private var loadRequested = false

    init { accept(EditGroupIntent.RetryLoad) }

    override fun handleIntent(intent: EditGroupIntent) {
        val current = state.value
        if (current.isFinished) return
        when (intent) {
            is EditGroupIntent.NameChanged -> if (!current.isBusy && current.loadError == null) {
                reduce(EditGroupMessage.NameChanged(intent.name))
            }
            is EditGroupIntent.ColorChanged -> if (!current.isBusy && current.loadError == null) {
                reduce(EditGroupMessage.ColorChanged(intent.color))
            }
            EditGroupIntent.Save -> {
                if (!current.canSave) return
                val original = current.original ?: return
                reduce(EditGroupMessage.Saving)
                launchEffect {
                    recover(EditGroupMessage.SaveFailed) {
                        EditGroupMessage.SaveResult(repository.update(original.copy(name = current.normalizedName, color = current.color)))
                    }
                }
            }
            EditGroupIntent.PrepareDelete -> if (current.original != null && !current.isBusy) {
                reduce(EditGroupMessage.AskDelete)
            }
            EditGroupIntent.ConfirmDelete -> {
                if (current.dialog != EditGroupDialog.ConfirmDelete || current.isBusy) return
                reduce(EditGroupMessage.Deleting)
                launchEffect {
                    recover(EditGroupMessage.DeleteFailed) {
                        repository.delete(groupId)
                        EditGroupMessage.Deleted
                    }
                }
            }
            EditGroupIntent.DismissDialog -> if (!current.isBusy) reduce(EditGroupMessage.DismissDialog)
            EditGroupIntent.RetryLoad -> {
                if (current.original != null || current.isSaving || current.isDeleting || current.isLoading && loadRequested) return
                loadRequested = true
                reduce(EditGroupMessage.Loading)
                launchEffect { recover(EditGroupMessage.LoadFailed) { EditGroupMessage.Loaded(repository.getById(groupId)) } }
            }
        }
    }

    private suspend fun recover(failure: EditGroupMessage, block: suspend () -> EditGroupMessage): EditGroupMessage = try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (_: Exception) {
        failure
    }
}
