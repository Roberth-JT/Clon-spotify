package com.example.clon_spotify.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.clon_spotify.ui.screens.HomeScreen
import com.example.clon_spotify.ui.screens.LoginScreen
import com.example.clon_spotify.ui.screens.RegistroScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { navController.navigate("home") },
                onRegisterClick = { navController.navigate("registro") }
            )
        }
        composable("registro") {
            RegistroScreen(
                onRegisterClick = { email ->
                    // Aquí manejaremos la lógica de registro con el email
                    // Por ahora solo navega al Home
                    navController.navigate("home")
                },
                onBackToLoginClick = {
                    navController.popBackStack() // Vuelve al login
                }
            )
        }
        composable("home") {
            HomeScreen(navController)
        }
    }
}
