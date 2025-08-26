package com.fediim.translatio

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fediim.translatio.locales.DashboardScreen
import com.fediim.translatio.login.LoginScreen
import com.fediim.translatio.navigation.Screen
import com.fediim.translatio.register.RegisterScreen

@Composable
fun TranslatioApp(onNavHostReady: suspend (NavController) -> Unit = {}) {
    MaterialTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = Screen.Login
        ) {
            composable<Screen.Login> { LoginScreen(navController) }
            composable<Screen.Register> { RegisterScreen(navController) }
            composable<Screen.Locales> { DashboardScreen(navController) }
            composable<Screen.Localisation> { com.fediim.translatio.localisation.LocalisationScreen() }
        }

        LaunchedEffect(navController) {
            onNavHostReady(navController)
        }
    }


}
