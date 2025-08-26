package com.fediim.translatio.util

import android.util.Log

class AndroidLogger : Logger {
    override fun log(tag: String, lazyMessage: () -> String) {
        Log.w(tag, lazyMessage())
    }
}
