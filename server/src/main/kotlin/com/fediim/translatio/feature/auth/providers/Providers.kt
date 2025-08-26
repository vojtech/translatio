package com.fediim.translatio.feature.auth.providers

import com.fediim.translatio.config.ApiConfig
import io.ktor.resources.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

@Resource("auth-providers")
class Providers()

fun Route.providers() {
    val apiConfig by inject<ApiConfig>()

    get<Providers> {
        call.respond(
            mapOf(
                "github" to apiConfig.githubEnabled,
                "google" to apiConfig.googleEnabled,
                "microsoft" to apiConfig.microsoftEnabled,
                "apple" to apiConfig.appleEnabled
            )
        )
    }
}