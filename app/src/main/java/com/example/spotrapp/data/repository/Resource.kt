package com.example.spotrapp.data.repository

// Wraps asynchronous operation results as loading, success, or error states for the UI to display.

// wrapper for async ops
sealed class Resource<T> {
    class Loading<T>(val data: T? = null) : Resource<T>()
    class Success<T>(val data: T) : Resource<T>()
    class Error<T>(val message: String, val data: T? = null) : Resource<T>()
}