package com.example.spotrapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// used for the history screen, logs any event (add, delete,edit, search etc)
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: HistoryAction,
    val itemId: Int? = null,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)