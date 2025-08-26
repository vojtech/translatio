package com.fediim.translatio.shared

import kotlinx.serialization.Serializable

@Serializable
data class LocaleRequest(val language: String, val country: String)