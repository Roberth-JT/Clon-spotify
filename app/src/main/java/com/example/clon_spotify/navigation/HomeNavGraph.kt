package com.example.clon_spotify.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.clon_spotify.player.PlayerViewModel
import com.example.clon_spotify.ui.screens.BibliotecaScreen
import com.example.clon_spotify.ui.screens.CreatePlaylistDialog
import com.example.clon_spotify.ui.screens.HomeDrawerScreen
import com.example.clon_spotify.ui.screens.MessagesScreen
import com.example.clon_spotify.ui.screens.PlaylistScreen
import com.example.clon_spotify.ui.screens.SearchScreen
import com.example.clon_spotify.ui.screens.SelectFriendsScreen
import com.example.clon_spotify.ui.screens.TusMeGustaScreen
import com.example.clon_spotify.ui.screens.PerfilUsuarioScreen


@Composable
fun HomeNavGraph(playerViewModel: PlayerViewModel, mainNavController: NavController) {
    val homeNavController = rememberNavController()

    NavHost(
        navController = homeNavController,
        startDestination = "home_drawer"
    ) {
        composable("home_drawer") {
            HomeDrawerScreen(
                navController = homeNavController,
                mainNavController = mainNavController,
                playerViewModel = playerViewModel,
                onOpenPlaylist = { id -> homeNavController.navigate("playlist/$id") }
            )
        }

        // Agregar esta ruta adicional para el NavigationBarItem
        composable("home_nav") {
            HomeDrawerScreen(
                navController = homeNavController,
                playerViewModel = playerViewModel,
                onOpenPlaylist = { id -> homeNavController.navigate("playlist/$id") },
                mainNavController = TODO()
            )
        }

        composable("create_playlist") {
            CreatePlaylistDialog(navController = homeNavController)
        }

        composable("search") {
            SearchScreen(playerViewModel = playerViewModel)
        }

        composable("playlist/{playlistId}") { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")
            PlaylistScreen(
                playlistId = playlistId,
                playerViewModel = playerViewModel
            )
        }

        composable("tus_me_gusta") {
            TusMeGustaScreen(playerViewModel = playerViewModel)
        }

        composable("library") {
            BibliotecaScreen(
                homeNavController = homeNavController,
                onOpenPlaylist = { playlistId ->
                    homeNavController.navigate("playlist/$playlistId") // ← También cambiar aquí si es necesario
                }
            )
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
        // ✅ RUTA CORRECTA Y UNIFICADA
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
}