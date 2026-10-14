package com.example.spotrapp.di

// Initializes the Android application with Hilt so dependency injection is available throughout the app.

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SpotrApplication : Application()
