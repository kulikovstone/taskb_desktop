package com.mkulikov.taskb.feature.hello.screen.hello

import com.mkulikov.taskb.core.mvi.CoroutineStore
import com.mkulikov.taskb.feature.settings.api.IAppSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class HelloStore(
    private val settings: IAppSettings,
    initialError: String? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineStore<HelloIntent, HelloState, HelloMessage>(
    initialState = HelloState(error = initialError),
    reducer = ::reduceHello,
    context = dispatcher,
) {
    override fun handleIntent(intent: HelloIntent) {
        when (intent) {
            HelloIntent.Continue -> {
                if (state.value.isContinuing || state.value.isCompleted) return
                reduce(HelloMessage.Continue)
                launchEffect {
                    try {
                        settings.completeWelcome()
                        HelloMessage.Saved
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Exception) {
                        HelloMessage.SaveFailed
                    }
                }
            }
        }
    }
}
