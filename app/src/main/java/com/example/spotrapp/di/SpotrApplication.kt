package com.example.spotrapp.di

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// registered in manifest (android:name=".di.SpotrApplication")
@HiltAndroidApp
class SpotrApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
}