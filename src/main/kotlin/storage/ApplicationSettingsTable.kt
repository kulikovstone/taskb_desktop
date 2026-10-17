package com.mkulikov.taskb.storage

import org.jetbrains.exposed.v1.core.Table

internal object ApplicationSettingsTable : Table("application_settings") {
    val key = varchar("key", 128)
    val value = text("value")

    override val primaryKey = PrimaryKey(key)
}
