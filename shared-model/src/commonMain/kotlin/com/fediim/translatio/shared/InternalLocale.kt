package com.fediim.translatio.shared

import kotlinx.serialization.Serializable

@Serializable
data class InternalLocale(
    val id: Int,
    val language: String,
    val country: String?,
    val isDefault: Boolean,
    val editable: Boolean
)
