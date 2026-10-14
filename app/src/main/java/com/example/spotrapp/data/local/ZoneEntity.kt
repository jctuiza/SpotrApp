package com.example.spotrapp.data.local

/**
 * Represents a storage zone in the database; zone names are unique so items can be organized consistently.
 */

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "zones",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class ZoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)