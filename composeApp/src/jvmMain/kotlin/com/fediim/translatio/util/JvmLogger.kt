package com.fediim.translatio.util

class JvmLogger : Logger {
    override fun log(tag: String, lazyMessage: () -> String) {
        println("[$tag] ${lazyMessage()}")
    }
}