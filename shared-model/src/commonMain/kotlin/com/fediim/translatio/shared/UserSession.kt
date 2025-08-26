package com.fediim.translatio.shared

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(val userId: Int, val username: String)