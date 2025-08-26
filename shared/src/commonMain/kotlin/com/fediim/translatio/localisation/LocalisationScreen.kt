package com.fediim.translatio.localisation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LocalisationScreen(modifier: Modifier = Modifier, viewModel: LocalisationViewModel = koinViewModel()) {
    val locales by viewModel.locales.collectAsState()

    if (locales.isEmpty()) {
        Text("Loading locales...")
        return
    }

    LocalisationEditor(
        locales = locales,
        onCreateKey = { key, description -> viewModel.createKey(key, description) },
        onSubmitTranslation = { stringId, localeId, value -> viewModel.submitTranslation(stringId, localeId, value) },
        modifier = modifier
    )
}
