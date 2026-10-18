package com.mkulikov.taskb.feature.settings.api.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "application_settings")
data class ApplicationSetting(
    @PrimaryKey val key: String,
    val value: String
)
