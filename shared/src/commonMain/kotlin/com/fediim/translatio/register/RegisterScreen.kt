package com.fediim.translatio.register

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.fediim.translatio.navigation.NavigationHandler
import com.fediim.translatio.register.state.RegisterUiState
import com.fediim.translatio.register.viewmodel.RegisterFiled
import com.fediim.translatio.register.viewmodel.RegisterViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    navHostController: NavHostController,
    viewModel: RegisterViewModel = koinViewModel()
) {
    NavigationHandler(navHostController, viewModel.events)

    val state = viewModel.uiState.collectAsState()

    when (val uiState = state.value) {
        is RegisterUiState.Error -> RegisterFormScreen(viewModel, uiState)
        is RegisterUiState.Initial -> RegisterFormScreen(viewModel, uiState)
        RegisterUiState.Loading -> RegisterFormScreen(viewModel, RegisterUiState.Loading)
        RegisterUiState.Success -> RegisterFormScreen(viewModel, RegisterUiState.Success)
    }
}

@Composable
private fun RegisterFormScreen(viewModel: RegisterViewModel, state: RegisterUiState.Initial) {
    LazyColumn(Modifier.padding(16.dp)) {
        item { Text("Register", style = MaterialTheme.typography.titleLarge) }
        item {
            OutlinedTextField(
                value = state.firstName,
                onValueChange = { viewModel.updateField(RegisterFiled.firstName, it) },
                label = { Text("First name") })
        }
        item {
            OutlinedTextField(
                value = state.lastName,
                onValueChange = { viewModel.updateField(RegisterFiled.lastName, it) },
                label = { Text("Last name") })
        }
        item {
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.updateField(RegisterFiled.email, it) },
                label = { Text("Email") })
        }
        item {
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.updateField(RegisterFiled.password, it) },
                label = { Text("Password") })
        }
        item {
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { viewModel.updateField(RegisterFiled.confirmPassword, it) },
                label = { Text("Password") })
        }
        errorMessage(if (state.isValid) null else "Please fill all fields")
        item {
            Button(onClick = {
                viewModel.submit()
            }) { Text("Create account") }
        }
        item {
            Button(onClick = {
                viewModel.navigateUp()
            }) { Text("Create account") }
        }
    }
}

@Composable
private fun RegisterFormScreen(viewModel: RegisterViewModel, state: RegisterUiState.Success) {
}

@Composable
private fun RegisterFormScreen(viewModel: RegisterViewModel, state: RegisterUiState.Error) {
}

@Composable
private fun RegisterFormScreen(viewModel: RegisterViewModel, state: RegisterUiState.Loading) {
}

private fun LazyListScope.errorMessage(error: String?) {
    error?.let {
        item { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

