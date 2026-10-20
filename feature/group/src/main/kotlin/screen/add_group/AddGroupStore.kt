package com.mkulikov.taskb.feature.group.screen.add_group

import com.mkulikov.taskb.core.mvi.CoroutineStore
import com.mkulikov.taskb.feature.group.model.GroupColor
import com.mkulikov.taskb.feature.group.repository.IGroupRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class AddGroupStore(
    private val repository: IGroupRepository,
    initialColor: GroupColor = GroupColor.entries.random(),
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineStore<AddGroupIntent, AddGroupState, AddGroupMessage>(
    initialState = AddGroupState(color = initialColor),
    reducer = ::reduceAddGroup,
    context = dispatcher,
) {
    override fun handleIntent(intent: AddGroupIntent) {
        val current = state.value
        if (current.isFinished) return
        when (intent) {
            is AddGroupIntent.NameChanged -> if (!current.isSaving) reduce(AddGroupMessage.NameChanged(intent.name))
            is AddGroupIntent.ColorChanged -> if (!current.isSaving) reduce(AddGroupMessage.ColorChanged(intent.color))
            AddGroupIntent.Save -> {
                if (!current.canSave) return
                reduce(AddGroupMessage.Saving)
                launchEffect {
                    try {
                        AddGroupMessage.SaveResult(repository.add(current.normalizedName, current.color))
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Exception) {
                        AddGroupMessage.SaveFailed
                    }
                }
            }
            AddGroupIntent.DismissDialog -> if (!current.isSaving) reduce(AddGroupMessage.DismissDialog)
        }
    }
}
