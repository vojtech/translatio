package com.fediim.translatio.shared

import kotlinx.serialization.Serializable

@Serializable
data class UserRecord(
    val id: Int,
    val username: String,
    val passwordHash: String,
    val role: String,
)