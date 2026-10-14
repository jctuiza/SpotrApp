package com.example.spotrapp.data.local

// Lists the event types stored in the activity history, including item changes and searches.

// Stored by name in the database
enum class HistoryAction { ADDED, EDITED, DELETED, RETRIEVED, SEARCHED }