package com.fediim.translatio.login.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fediim.translatio.Api
import com.fediim.translatio.navigation.Screen
import com.fediim.translatio.navigation.NavigationUiEvent
import com.fediim.translatio.platformBaseUrl
import com.fediim.translatio.shared.LoginRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel constructor(private val api: Api) : ViewModel() {

    private val _username: MutableState<String> = mutableStateOf("")
    val username: MutableState<String> get() = _username

    private val _password: MutableState<String> = mutableStateOf("")
    val password: MutableState<String> get() = _password

    private val _error: MutableState<String?> = mutableStateOf(null)
    val error: MutableState<String?> get() = _error

    private val _events = MutableSharedFlow<NavigationUiEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun login() {
        viewModelScope.launch {
            error.value = null
            val res = runCatching {
                api.login(
                    loginRequest = LoginRequest(
                        username = username.value.trim(),
                        password = password.value
                    )
                )
            }
            if (res.isSuccess) {
                _events.emit(NavigationUiEvent.NavigateToScreen(Screen.Localisation))
            } else {
                error.value = "Invalid credentials"
            }
        }
    }

    fun onLoginProviderClicked(loginProvider: LoginProvider) {
        viewModelScope.launch {
            val uri = when (loginProvider) {
                LoginProvider.Github -> "/oauth/github/start"
                LoginProvider.Google -> "/oauth/google/start"
                LoginProvider.Microsoft -> "/oauth/microsoft/start"
                LoginProvider.Apple -> "/oauth/apple/start"
            }
            _events.emit(NavigationUiEvent.NavigateToUri(uri))
        }
    }

    fun register() {
        viewModelScope.launch {
            _events.emit(NavigationUiEvent.NavigateToScreen(Screen.Register))
        }
    }

    private fun buildExternalUrl(path: String): String {
        val base = platformBaseUrl()
        return if (base.isBlank()) path else base.trimEnd('/') + path
    }
}

enum class LoginProvider {
    Google,
    Microsoft,
    Apple,
    Github
}