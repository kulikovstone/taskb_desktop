package com.mkulikov.taskb.core.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "application_settings")
internal data class ApplicationSetting(
    @PrimaryKey val key: String,
    val value: String
)
