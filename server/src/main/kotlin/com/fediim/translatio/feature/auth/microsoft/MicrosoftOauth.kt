package com.fediim.translatio.feature.auth.microsoft

import com.fediim.translatio.config.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.oauth

fun AuthenticationConfig.microsoftOauth(httpClient: HttpClient, apiConfig: ApiConfig) {
    System.getenv("MICROSOFT_CLIENT_ID")?.takeIf { it.isNotBlank() }?.let { clientId ->
        val secret = System.getenv("MICROSOFT_CLIENT_SECRET") ?: ""
        if (secret.isNotBlank()) {
            oauth("microsoft") {
                urlProvider = { "${apiConfig.publicUrl}/oauth/microsoft/callback" }
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "microsoft",
                        authorizeUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
                        accessTokenUrl = "https://login.microsoftonline.com/common/oauth2/v2.0/token",
                        requestMethod = HttpMethod.Post,
                        clientId = clientId,
                        clientSecret = secret,
                        defaultScopes = listOf("openid", "email", "profile", "https://graph.microsoft.com/User.Read")
                    )
                }
                client = httpClient
            }
        }
    }
}