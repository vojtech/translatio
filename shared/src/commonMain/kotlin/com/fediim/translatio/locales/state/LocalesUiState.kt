package com.fediim.translatio.locales.state

import com.fediim.translatio.shared.InternalLocale

sealed class LocalesUiState {
    data class Initial(val data: List<InternalLocale>) : LocalesUiState()
    data object Loading : LocalesUiState()
    data class Error(val message: String) : LocalesUiState()
}