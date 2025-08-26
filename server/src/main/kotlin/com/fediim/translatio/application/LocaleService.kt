package com.fediim.translatio.application

import com.fediim.translatio.domain.port.LocalesRepository
import com.fediim.translatio.shared.InternalLocale
import com.fediim.translatio.shared.LocaleRequest

class LocaleService(private val repo: LocalesRepository) {
    fun list(): List<InternalLocale> = repo.list()
    fun add(locale: LocaleRequest): Int = repo.upsert(locale)
}
