package com.example.clon_spotify.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.clon_spotify.ui.screens.CreatePlaylistDialog
import com.example.clon_spotify.ui.screens.HomeDrawerScreen
import com.example.clon_spotify.ui.screens.PlaylistScreen
import com.example.clon_spotify.ui.screens.SearchScreen

@Composable
fun HomeNavGraph() {
    val homeNavController = rememberNavController()

    NavHost(
        navController = homeNavController,
        startDestination = "home_drawer"
    ) {
        composable("home_drawer") {
            HomeDrawerScreen(
                navController = homeNavController,
                onOpenPlaylist = { id -> homeNavController.navigate("playlist/$id") }
            )

        }
        composable("create_playlist") {
            CreatePlaylistDialog(navController = homeNavController)
        }


        composable("search") { SearchScreen() }

        composable("playlist/{playlistId}") { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")
            PlaylistScreen(playlistId = playlistId)
        }
    }
}