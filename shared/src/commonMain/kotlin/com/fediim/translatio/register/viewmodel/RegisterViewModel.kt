package com.fediim.translatio.register.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fediim.translatio.navigation.NavigationUiEvent
import com.fediim.translatio.navigation.Screen
import com.fediim.translatio.domain.usecase.RegisterUseCase
import com.fediim.translatio.register.state.RegisterUiState
import com.fediim.translatio.shared.RegisterRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RegisterViewModel(val registerUseCase: RegisterUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Initial())
    val uiState: StateFlow<RegisterUiState> = _uiState

    private val _events = MutableSharedFlow<NavigationUiEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun updateField(field: RegisterFiled, value: String) {
        if (_uiState.value !is RegisterUiState.Initial) return

        when (field) {
            RegisterFiled.firstName -> _uiState.update { it.asInitial().copy(firstName = value) }
            RegisterFiled.lastName -> _uiState.update { it.asInitial().copy(lastName = value) }
            RegisterFiled.email -> _uiState.update { it.asInitial().copy(email = value) }
            RegisterFiled.password -> _uiState.update { it.asInitial().copy(password = value) }
            RegisterFiled.confirmPassword -> _uiState.update { it.asInitial().copy(confirmPassword = value) }
        }
    }

    fun submit() {
        if (_uiState.value !is RegisterUiState.Initial) return

        viewModelScope.launch {
            val res = registerUseCase.invoke(uiState.value.asInitial().toRegisterRequest())
            if (res.isSuccess) {
                _events.emit(NavigationUiEvent.NavigateToScreen(Screen.Localisation))
            } else {
                _uiState.value = RegisterUiState.Error("Registration failed: ${res.exceptionOrNull()}")
            }
        }
    }

    fun navigateUp() {
        viewModelScope.launch {
            _events.emit(NavigationUiEvent.NavigateToScreen(Screen.Up))
        }
    }
}

private fun RegisterUiState.asInitial() = (this as RegisterUiState.Initial)

private fun RegisterUiState.Initial.toRegisterRequest(): RegisterRequest {
    return RegisterRequest(
        email = email.trim(),
        firstName = firstName.trim(),
        lastName = lastName.trim(),
        password = password
    )
}

enum class RegisterFiled {
    firstName, lastName, email, password, confirmPassword;
}
