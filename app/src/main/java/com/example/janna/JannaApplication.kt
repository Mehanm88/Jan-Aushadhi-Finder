package com.example.janna

import android.app.Application
import timber.log.Timber

class JannaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // BuildConfig is generated at build time. 
        // Using a try-catch or explicit import if needed, but standard practice is 
        // to import com.example.janna.BuildConfig
        Timber.plant(Timber.DebugTree())
    }
}
