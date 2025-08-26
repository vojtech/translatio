package com.fediim.translatio

class JsPlatform : Platform {
    override val name: String = "web-js"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun platformBaseUrl(): String = "" // relative to origin, e.g., /api

actual fun platformHttpClient(): HttpClient = HttpClient(Js) {
    followRedirects = false
}
