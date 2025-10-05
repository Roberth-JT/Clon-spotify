package com.example.clon_spotify.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@Composable
fun HomeBottomBar(navController: NavController, onCreate: () -> Unit) {
    NavigationBar(containerColor = Color.Black) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home_nav") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Inicio") },
            alwaysShowLabel = false
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("search") },
            icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            label = { Text("Buscar") },
            alwaysShowLabel = false
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("library") },
            icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Biblioteca") },
            label = { Text("Biblioteca") },
            alwaysShowLabel = false
        )
        NavigationBarItem(
            selected = false,
            onClick = onCreate,
            icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
            label = { Text("Crear") },
            alwaysShowLabel = false
        )
    }
}
