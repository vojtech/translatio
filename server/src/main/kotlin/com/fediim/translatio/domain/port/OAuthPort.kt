package com.fediim.translatio.domain.port

import com.fediim.translatio.shared.OauthUser

interface OAuthPort {
    suspend fun fetchUser(provider: String, accessToken: String): OauthUser
}
