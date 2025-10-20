package com.example.clon_spotify.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.clon_spotify.player.MiniPlayer
import com.example.clon_spotify.player.PlayerViewModel
import com.example.clon_spotify.ui.components.HomeBottomBar
import com.example.clon_spotify.ui.screens.*

@Composable
fun HomeNavGraph(playerViewModel: PlayerViewModel, mainNavController: NavController) {
    val homeNavController = rememberNavController()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                // 🔹 MiniPlayer global (siempre visible)
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    color = Color(0xFF181818)
                ) {
                    MiniPlayer(playerViewModel = playerViewModel)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 🔹 HomeBottomBar global
                HomeBottomBar(
                    navController = homeNavController,
                    onCreateClick = { showCreateDialog = true }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = homeNavController,
            startDestination = "home_drawer",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home_drawer") {
                HomeDrawerScreen(
                    navController = homeNavController,
                    mainNavController = mainNavController,
                    playerViewModel = playerViewModel
                )
            }

            composable("search") {
                SearchScreen(playerViewModel = playerViewModel)
            }

            composable("library") {
                BibliotecaScreen(
                    homeNavController = homeNavController,
                    onOpenPlaylist = { id -> homeNavController.navigate("playlist/$id") }
                )
            }

            composable("playlist/{playlistId}") { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId")
                PlaylistScreen(playlistId = playlistId, playerViewModel = playerViewModel)
            }

            composable("create_playlist") {
                CreatePlaylistDialog(navController = homeNavController)
            }

            composable("messages") {
                MessagesScreen(
                    navController = homeNavController,
                    onBackClick = { homeNavController.popBackStack() }
                )
            }

            composable("select_friends") {
                SelectFriendsScreen(
                    navController = homeNavController,
                    onBackClick = { homeNavController.popBackStack() }
                )
            }

            // ✅ RUTA PERFIL DE USUARIO (dentro del NavHost)
            composable(
                route = "perfil/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                PerfilUsuarioScreen(
                    userId = userId,
                    navController = homeNavController
                )
            }
        }

        // ✅ Mostrar diálogo de creación si se activa
        if (showCreateDialog) {
            showCreateDialog = false
            homeNavController.navigate("create_playlist")
        }
    }
}
