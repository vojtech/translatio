package com.fediim.translatio

import android.app.Application
import com.fediim.translatio.util.AndroidLogger

class TranslatioApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initApp(AndroidLogger())
    }
}