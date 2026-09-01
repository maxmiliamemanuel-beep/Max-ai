package com.mkuuai.android

import android.app.Application
import timber.log.Timber

class MkuuAIApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        Timber.d("MKUU AI Application initialized")
    }
}
