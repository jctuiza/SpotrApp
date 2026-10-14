package com.example.spotrapp.data.repository

// wrapper for async ops
sealed class Resource<T> {
    class Loading<T>(val data: T? = null) : Resource<T>()
    class Success<T>(val data: T) : Resource<T>()
    class Error<T>(val message: String, val data: T? = null) : Resource<T>()
}