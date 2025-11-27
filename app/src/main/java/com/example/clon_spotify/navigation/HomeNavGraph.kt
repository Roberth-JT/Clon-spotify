package com.example.clon_spotify.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.clon_spotify.ui.screens.ArtistSongsScreen
import com.example.clon_spotify.ui.screens.BibliotecaScreen
import com.example.clon_spotify.ui.screens.CreatePlaylistDialog
import com.example.clon_spotify.ui.screens.HomeDrawerScreen
import com.example.clon_spotify.ui.screens.MessagesScreen
import com.example.clon_spotify.ui.screens.PerfilUsuarioScreen
import com.example.clon_spotify.ui.screens.PlaylistScreen
import com.example.clon_spotify.ui.screens.PublicPlaylistScreen
import com.example.clon_spotify.ui.screens.SearchScreen
import com.example.clon_spotify.ui.screens.SelectFriendsScreen

@Composable
fun HomeNavGraph(playerViewModel: PlayerViewModel, mainNavController: NavController) {
    val homeNavController = rememberNavController()
    var showCreateDialog by remember { mutableStateOf(false) } //creacion de dialogo de playlist

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                // MiniPlayer global
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    color = Color(0xFF181818)
                ) {
                    MiniPlayer(playerViewModel = playerViewModel)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // HomeBottomBar global
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

            composable("search") { //buscador
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

            // RUTA PARA CANCIONES DEL ARTISTA
            composable(
                route = "artist_songs/{artistName}",
                arguments = listOf(navArgument("artistName") { type = NavType.StringType })
            ) { backStackEntry ->
                val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
                ArtistSongsScreen(
                    artistName = artistName,
                    navController = homeNavController,
                    playerViewModel = playerViewModel
                )
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

            // RUTA PERFIL DE USUARIO
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

            composable(
                route = "playlist_public/{playlistId}/{ownerId}",
                arguments = listOf(
                    navArgument("playlistId") { type = NavType.StringType },
                    navArgument("ownerId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                val ownerId = backStackEntry.arguments?.getString("ownerId") ?: ""

                PublicPlaylistScreen(
                    playlistId = playlistId,
                    ownerId = ownerId,
                    navController = homeNavController,
                    playerViewModel = playerViewModel
                )
            }
        }

        // Mostrar diálogo de creación si se activa
        if (showCreateDialog) {
            showCreateDialog = false
            homeNavController.navigate("create_playlist")
        }
    }
}