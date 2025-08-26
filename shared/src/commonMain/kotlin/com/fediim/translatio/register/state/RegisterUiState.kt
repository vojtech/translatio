package com.fediim.translatio.register.state

sealed class RegisterUiState {
    data class Initial(
        val firstName: String = "",
        val lastName: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isValid: Boolean = true,
    ) : RegisterUiState()

    data object Loading : RegisterUiState()
    data object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}