package com.fediim.translatio

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()
actual fun platformBaseUrl(): String = "http://localhost:8080"

actual fun platformHttpClient(): HttpClient = HttpClient(CIO) {
    followRedirects = false
}
