package com.mkulikov.taskb.feature.settings.api

interface IAppSettings {
    suspend fun hasCompletedWelcome(): Boolean
    suspend fun completeWelcome()
}
