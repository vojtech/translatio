package com.fediim.translatio.login

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fediim.translatio.common.LoginButton
import com.fediim.translatio.common.OAuthButton
import com.fediim.translatio.common.RegisterButton
import com.fediim.translatio.login.viewmodel.LoginProvider
import com.fediim.translatio.login.viewmodel.LoginViewModel
import com.fediim.translatio.navigation.NavigationHandler
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    navHostController: NavHostController,
    loginViewModel: LoginViewModel = koinViewModel()
) {
    NavigationHandler(navHostController, loginViewModel.events)
    val focusManager = LocalFocusManager.current

    LazyColumn(Modifier.padding(16.dp)) {
        item { Text("Login", style = MaterialTheme.typography.titleLarge) }
        item {
            OutlinedTextField(
                value = loginViewModel.username.value,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }),
                onValueChange = { loginViewModel.username.value = it },
                label = { Text("Username") })
        }
        item {
            OutlinedTextField(
                value = loginViewModel.password.value,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                }),
                onValueChange = { loginViewModel.password.value = it },
                label = { Text("Password") })
        }
        errorMessage(loginViewModel.error.value)
        item { LoginButton { loginViewModel.login() } }
        item { RegisterButton { loginViewModel.register() } }
        item { Text("Or sign in with:") }
        item { OAuthButton("GitHub") { loginViewModel.onLoginProviderClicked(LoginProvider.Github) } }
        item { OAuthButton("Google") { loginViewModel.onLoginProviderClicked(LoginProvider.Google) } }
        item { OAuthButton("Microsoft") { loginViewModel.onLoginProviderClicked(LoginProvider.Microsoft) } }
        item { OAuthButton("Apple") { loginViewModel.onLoginProviderClicked(LoginProvider.Apple) } }
    }
}

private fun LazyListScope.errorMessage(error: String?) {
    error?.let {
        item { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
