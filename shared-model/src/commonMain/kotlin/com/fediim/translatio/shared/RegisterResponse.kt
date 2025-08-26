package com.fediim.translatio.shared

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(val userId: Int, val username: String)