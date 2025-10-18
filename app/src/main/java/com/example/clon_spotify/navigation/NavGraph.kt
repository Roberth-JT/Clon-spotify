package com.example.clon_spotify.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.clon_spotify.player.MiniPlayer
import com.example.clon_spotify.player.PlayerViewModel
import com.example.clon_spotify.ui.screens.LoginScreen
import com.example.clon_spotify.ui.screens.RegistroScreen
import com.example.clon_spotify.viewmodel.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val startDestination = if (authViewModel.isValidAuth) "home_nav" else "login"
    val playerViewModel: PlayerViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Pantalla de login
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                navController = navController, //
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

        //  Pantalla de registro
        composable("register") {
            RegistroScreen(
                viewModel = authViewModel,
                navController = navController, // ✅ Se pasa aquí también
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
                HomeNavGraph(playerViewModel = playerViewModel)

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                ) {
                    MiniPlayer(playerViewModel = playerViewModel)
                }
            }

}
    }
}

