package com.example.clon_spotify.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.clon_spotify.player.PlayerViewModel
import com.example.clon_spotify.ui.screens.LoginScreen
import com.example.clon_spotify.ui.screens.RegistroScreen
import com.example.clon_spotify.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val playerViewModel: PlayerViewModel = viewModel()

    // 🔹 Detecta si ya hay usuario logueado en Firebase
    val currentUser = FirebaseAuth.getInstance().currentUser

    val startDestination = if (currentUser != null) "home_nav" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination // ✅ AHORA SE USA ESTA VARIABLE
    ) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                navController = navController,
                onLoginSuccess = {
                    navController.navigate("home_nav") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegistroScreen(
                viewModel = authViewModel,
                navController = navController,
                onRegisterSuccess = {
                    navController.navigate("home_nav") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBackToLoginClick = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("home_nav") {
            Box(modifier = Modifier.fillMaxSize()) {
                HomeNavGraph(
                    playerViewModel = playerViewModel,
                    mainNavController = navController
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                ) {
                }
            }
        }
    }
}
