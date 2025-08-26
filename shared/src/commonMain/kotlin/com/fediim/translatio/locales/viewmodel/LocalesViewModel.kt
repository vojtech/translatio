package com.fediim.translatio.locales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fediim.translatio.locales.state.LocalesUiState
import com.fediim.translatio.navigation.NavigationUiEvent
import com.fediim.translatio.domain.usecase.AddLocalesUseCase
import com.fediim.translatio.domain.usecase.GetLocalesUseCase
import com.fediim.translatio.domain.usecase.LogoutUseCase
import com.fediim.translatio.navigation.Screen
import com.fediim.translatio.shared.LocaleRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocalesViewModel(
    val addLocalesUseCase: AddLocalesUseCase,
    val getLocalesUseCase: GetLocalesUseCase,
    val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _events = MutableSharedFlow<NavigationUiEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    var locales: StateFlow<LocalesUiState> = getLocalesUseCase().map { result ->
        result.fold(
            onSuccess = { LocalesUiState.Initial(it) },
            onFailure = { LocalesUiState.Error(it.message ?: "Unknown error") }
        )
    }.onStart { emit(LocalesUiState.Loading) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LocalesUiState.Initial(emptyList())
        )


    fun addLocale(locale: LocaleRequest) {
        viewModelScope.launch {
            addLocalesUseCase(locale)
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase().onSuccess {
                _events.emit(NavigationUiEvent.NavigateToScreen(Screen.Login))
            }
        }
    }
}