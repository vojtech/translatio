package com.fediim.translatio

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun platformBaseUrl(): String = "" // relative to origin, e.g., /api

actual fun platformHttpClient(): HttpClient = HttpClient(Js) {
    followRedirects = false
}
