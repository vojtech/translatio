package com.fediim.translatio

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.fediim.translatio.util.JvmLogger

fun main() {
    initApp(JvmLogger())

    System.setProperty("apple.awt.application.appearance", "system")

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Translatio",
            alwaysOnTop = true,
            state = rememberWindowState(width = 600.dp, height = 800.dp),
        ) {
            App()
        }
    }
}