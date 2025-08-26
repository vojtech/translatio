package com.fediim.translatio.feature.auth

import com.fediim.translatio.config.ApiConfig
import com.fediim.translatio.feature.auth.apple.appleOauth
import com.fediim.translatio.feature.auth.github.githubOauth
import com.fediim.translatio.feature.auth.google.googleOauth
import com.fediim.translatio.feature.auth.microsoft.microsoftOauth
import com.fediim.translatio.shared.UserSession
import io.ktor.client.HttpClient
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.session
import io.ktor.server.response.respondRedirect

fun Application.installAuthentication(httpClient: HttpClient, apiConfig: ApiConfig) {
    install(Authentication) {
        session<UserSession>("auth-session") {
            validate { it }
            challenge {
                call.respondRedirect("/login")
            }

            googleOauth(httpClient, apiConfig)
            githubOauth(httpClient, apiConfig)
            microsoftOauth(httpClient, apiConfig)
            appleOauth(httpClient, apiConfig)
        }
    }
}