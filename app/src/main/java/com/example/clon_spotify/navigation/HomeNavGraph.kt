package com.example.clon_spotify.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.clon_spotify.ui.screens.HomeDrawerScreen
import com.example.clon_spotify.ui.screens.PlaylistScreen

@Composable
fun HomeNavGraph() {
    //  Se crea su propio NavController, independiente del principal
    val homeNavController = rememberNavController()

    NavHost(
        navController = homeNavController,
        startDestination = "home_drawer"
    ) {
        composable("home_drawer") {
            HomeDrawerScreen(navController = homeNavController)
        }

        composable("playlist/{playlistId}") { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")
            PlaylistScreen(playlistId = playlistId)
        }
    }
}