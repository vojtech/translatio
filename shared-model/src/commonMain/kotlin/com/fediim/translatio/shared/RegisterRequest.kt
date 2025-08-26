package com.fediim.translatio.shared

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String
)
