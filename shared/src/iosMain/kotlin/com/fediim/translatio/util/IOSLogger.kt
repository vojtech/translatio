package com.fediim.translatio.util

import platform.Foundation.NSLog

class IOSLogger : Logger {
    override fun log(tag: String, lazyMessage: () -> String) {
        NSLog("[$tag] ${lazyMessage()}")
    }
}