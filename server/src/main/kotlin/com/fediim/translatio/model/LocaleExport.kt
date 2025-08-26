package com.fediim.translatio.model

import kotlinx.serialization.Serializable
import java.util.Locale
import java.util.MissingResourceException

@Serializable
data class LocaleExport(
    val language: String?,
    val country: String?,
    val isO3Country: String?,
    val isO3Language: String?,
    val displayCountry: String?,
    val displayName: String?,
    val displayLanguage: String?
)

fun Locale.toExport() = LocaleExport(
    language,
    country.lowercase(),
    try {
        isO3Country
    } catch (_: MissingResourceException) {
        null
    },
    try {
        isO3Language
    } catch (_: MissingResourceException) {
        null
    },
    displayCountry,
    displayName,
    displayLanguage,
)