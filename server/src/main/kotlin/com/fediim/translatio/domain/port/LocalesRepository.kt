package com.fediim.translatio.domain.port

import com.fediim.translatio.shared.InternalLocale
import com.fediim.translatio.shared.LocaleRequest

interface LocalesRepository {
    fun list(): List<InternalLocale>
    fun upsert(locale: LocaleRequest): Int
}