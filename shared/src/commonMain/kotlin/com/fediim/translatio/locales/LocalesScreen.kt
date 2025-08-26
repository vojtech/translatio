package com.fediim.translatio.locales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.fediim.translatio.locales.state.LocalesUiState
import com.fediim.translatio.locales.viewmodel.LocalesViewModel
import com.fediim.translatio.navigation.NavigationHandler
import com.fediim.translatio.shared.LocaleRequest
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    navHostController: NavHostController,
    viewModel: LocalesViewModel = koinViewModel()
) {
    NavigationHandler(navHostController, viewModel.events)

    val state = viewModel.locales.collectAsStateWithLifecycle()

    when (val uiState = state.value) {
        is LocalesUiState.Initial -> InitialDashboardScreen(uiState, { language, country ->
            viewModel.addLocale(LocaleRequest(language.trim(), country.trim()))
        })

        is LocalesUiState.Error -> {
            Text("Error: ${uiState.message}")
        }

        LocalesUiState.Loading -> {
            Text("Loading...")
        }
    }
}

@Composable
private fun InitialDashboardScreen(state: LocalesUiState.Initial, onAddLocaleClicked: (String, String) -> Unit) {
    var language by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        Text("Add locale", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = language, onValueChange = { language = it }, label = { Text("Language") })
            OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country (optional)") })
            Button(onClick = {
                onAddLocaleClicked(language, country)
                language = ""
                country = ""
            }) { Text("Add") }
        }
        Text("Locales", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(state.data) {
                val localeStr = it.language + (it.country?.let { r -> "-" + r } ?: "")
                Text("- " + localeStr)
            }
        }
    }
}
