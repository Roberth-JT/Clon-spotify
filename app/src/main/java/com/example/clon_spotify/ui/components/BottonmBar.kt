package com.example.clon_spotify.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@Composable
fun HomeBottomBar(
    navController: NavController,
    onCreateClick: () -> Unit
) {
    NavigationBar(containerColor = Color.Black) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home_drawer") }, // ← CAMBIADO A "home_drawer"
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
            onClick = onCreateClick,
            icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
            label = { Text("Crear") },
            alwaysShowLabel = false
        )
    }
}
