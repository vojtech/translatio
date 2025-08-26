package com.fediim.translatio.feature.auth.google

import com.fediim.translatio.config.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.oauth

fun AuthenticationConfig.googleOauth(httpClient: HttpClient, apiConfig: ApiConfig) {
    System.getenv("GOOGLE_CLIENT_ID")?.takeIf { it.isNotBlank() }?.let { clientId ->
        val secret = System.getenv("GOOGLE_CLIENT_SECRET") ?: ""
        if (secret.isNotBlank()) {
            oauth("google") {
                urlProvider = { "${apiConfig.publicUrl}/oauth/google/callback" }
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "google",
                        authorizeUrl = "https://accounts.google.com/o/oauth2/v2/auth",
                        accessTokenUrl = "https://oauth2.googleapis.com/token",
                        requestMethod = HttpMethod.Post,
                        clientId = clientId,
                        clientSecret = secret,
                        defaultScopes = listOf("openid", "email", "profile")
                    )
                }
                client = httpClient
            }
        }
    }
}