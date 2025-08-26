package com.fediim.translatio

import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun platformBaseUrl(): String = "http://localhost:8080"

actual fun platformHttpClient(): HttpClient = HttpClient(CIO) {
    followRedirects = false
}
