package com.fediim.translatio.feature.auth.github

import com.fediim.translatio.config.ApiConfig
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.oauth

fun AuthenticationConfig.githubOauth(httpClient: HttpClient, apiConfig: ApiConfig) {
    System.getenv("GITHUB_CLIENT_ID")?.takeIf { it.isNotBlank() }?.let { clientId ->
        val secret = System.getenv("GITHUB_CLIENT_SECRET") ?: ""
        if (secret.isNotBlank()) {
            oauth("github") {
                urlProvider = { "${apiConfig.publicUrl}/oauth/github/callback" }
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "github",
                        authorizeUrl = "https://github.com/login/oauth/authorize",
                        accessTokenUrl = "https://github.com/login/oauth/access_token",
                        requestMethod = HttpMethod.Post,
                        clientId = clientId,
                        clientSecret = secret,
                        defaultScopes = listOf("read:user", "user:email")
                    )
                }
                client = httpClient
            }
        }
    }
}
