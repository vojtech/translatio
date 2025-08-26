package com.fediim.translatio.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    @SerialName("login") data object Login : Screen
    @Serializable
    @SerialName("register") data object Register : Screen
    @Serializable
    @SerialName("locales") data object Locales : Screen
    @Serializable
    @SerialName("localisation") data object Localisation : Screen
    data object Up : Screen
}


@Composable
fun NavigationHandler(navHostController: NavHostController, event: SharedFlow<NavigationUiEvent>) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(Unit) {
        scope.launch {
            event.collect { event ->
                when (event) {
                    is NavigationUiEvent.NavigateToScreen -> when (event.destination) {
                        Screen.Up -> navHostController.navigateUp()
                        else -> navHostController.navigate(event.destination)
                    }

                    is NavigationUiEvent.NavigateToUri -> uriHandler.openUri(event.uri)
                }
            }
        }
    }
}