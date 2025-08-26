package com.fediim.translatio.navigation

sealed interface NavigationUiEvent {
    data class NavigateToScreen(val destination: Screen) : NavigationUiEvent
    data class NavigateToUri(val uri: String) : NavigationUiEvent
}