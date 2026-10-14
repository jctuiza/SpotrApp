package com.example.spotrapp.data.local

// converts history action to and from strings

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