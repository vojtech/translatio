package com.fediim.translatio.feature.auth.apple

import com.fediim.translatio.config.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.oauth

fun AuthenticationConfig.appleOauth(httpClient: HttpClient, apiConfig: ApiConfig) {
    System.getenv("APPLE_CLIENT_ID")?.takeIf { it.isNotBlank() }?.let { clientId ->
        val secret = System.getenv("APPLE_CLIENT_SECRET") ?: ""
        if (secret.isNotBlank()) {
            oauth("apple") {
                urlProvider = { "${apiConfig.publicUrl}/oauth/apple/callback" }
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "apple",
                        authorizeUrl = "https://appleid.apple.com/auth/authorize",
                        accessTokenUrl = "https://appleid.apple.com/auth/token",
                        requestMethod = HttpMethod.Post,
                        clientId = clientId,
                        clientSecret = secret,
                        defaultScopes = listOf("name", "email")
                    )
                }
                client = httpClient
            }
        }
    }
}