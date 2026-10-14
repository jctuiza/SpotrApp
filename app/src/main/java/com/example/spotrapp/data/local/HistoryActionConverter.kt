package com.example.spotrapp.data.local

// Converts HistoryAction values to and from strings so Room can store them in SQLite.

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromHistoryAction(action: HistoryAction): String {
        return action.name
    }

    @TypeConverter
    fun toHistoryAction(value: String): HistoryAction {
        return HistoryAction.valueOf(value)
    }
}