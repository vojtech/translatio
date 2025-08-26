package com.fediim.translatio.config

import com.fediim.translatio.SERVER_PORT

class ApiConfig {

    val publicUrl = System.getenv("PUBLIC_URL") ?: "http://localhost:${SERVER_PORT}"

    // Determine which OAuth providers are enabled via env
    val githubEnabled = !System.getenv("GITHUB_CLIENT_ID").isNullOrBlank() && !System.getenv("GITHUB_CLIENT_SECRET").isNullOrBlank()
    val googleEnabled = !System.getenv("GOOGLE_CLIENT_ID").isNullOrBlank() && !System.getenv("GOOGLE_CLIENT_SECRET").isNullOrBlank()
    val microsoftEnabled = !System.getenv("MICROSOFT_CLIENT_ID").isNullOrBlank() && !System.getenv("MICROSOFT_CLIENT_SECRET").isNullOrBlank()
    val appleEnabled = !System.getenv("APPLE_CLIENT_ID").isNullOrBlank() && !System.getenv("APPLE_CLIENT_SECRET").isNullOrBlank()

}