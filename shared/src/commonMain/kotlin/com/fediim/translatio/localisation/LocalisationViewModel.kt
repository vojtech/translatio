package com.fediim.translatio.localisation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fediim.translatio.Api
import com.fediim.translatio.shared.InternalLocale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LocalisationViewModel(private val api: Api): ViewModel() {
    private val _locales = MutableStateFlow<List<InternalLocale>>(emptyList())
    val locales: StateFlow<List<InternalLocale>> = _locales

    init { refreshLocales() }

    fun refreshLocales() {
        viewModelScope.launch {
            runCatching { api.locales() }.onSuccess { _locales.value = it }.onFailure { _locales.value = emptyList() }
        }
    }

    suspend fun createKey(key: String, description: String?): Int? = api.createStringKey(key, description)

    suspend fun submitTranslation(stringId: Int, localeId: Int, value: String) = api.submitTranslation(stringId, localeId, value)
}
