package com.example.spotrapp.di

// initializes the app with hilt so it has dependency injections
import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SpotrApplication : Application()
