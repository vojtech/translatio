package com.fediim.translatio.shared

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val role: String,
) {
    val isAdmin: Boolean
        get() = role.equals("admin", ignoreCase = true)
}

@Serializable
data class OauthUser(
    val provider: String,
    val oauthId: String,
    val user: User
)

@Serializable
data class PasswordUser(
    val password: String,
    val user: User
)