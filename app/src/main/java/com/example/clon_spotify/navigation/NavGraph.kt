package com.example.clon_spotify.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.clon_spotify.ui.screens.*
import com.example.clon_spotify.viewmodel.AuthViewModel
import com.example.clon_spotify.viewmodel.PhoneAuthViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    // ViewModels
    val authViewModel: AuthViewModel = viewModel()
    val phoneAuthViewModel: PhoneAuthViewModel = viewModel()
    val activity = LocalContext.current as Activity

    NavHost(
        navController = navController,
        startDestination = "splash" //  Cambiado a splash
    ) {
        //  Pantalla Splash
        composable("splash") {
            SplashScreen(navController = navController)
        }

        // Pantalla de Login (Email/Password)
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                navController = navController,
                onLoginSuccess = { navController.navigate("home") },
                onRegisterClick = { navController.navigate("registro") },
            )
        }

        // Pantalla de Registro
        composable("registro") {
            RegistroScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { navController.navigate("home") },
                onBackToLoginClick = { navController.popBackStack() }
            )
        }

        // Login con Teléfono
        composable("phone_login") {
            PhoneLoginScreen(
                viewModel = phoneAuthViewModel,
                navController = navController
            )
        }

        // Verificación OTP
        composable("verify_otp") {
            VerifyOtpScreen(
                viewModel = phoneAuthViewModel,
                navController = navController
            )
        }

        // Home
        composable("home") {
            HomeScreen(
                navController = navController,
                phoneAuthViewModel = phoneAuthViewModel
            )
        }
    }
}




