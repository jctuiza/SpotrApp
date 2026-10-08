package com.example.spotrapp.data.local

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