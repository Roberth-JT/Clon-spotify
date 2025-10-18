package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.clon_spotify.player.MiniPlayer
import com.example.clon_spotify.player.PlayerViewModel
import com.example.clon_spotify.ui.components.HomeBottomBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDrawerScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    onOpenPlaylist: (playlistId: String) -> Unit = { id ->
        navController.navigate("playlist/$id")
    }
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val displayName = currentUser?.displayName ?: currentUser?.email ?: "Usuario"

    var showCreateDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.background(Color(0xFF0B0B0B))) {
                DrawerHeader(displayName = displayName, photoUrl = currentUser?.photoUrl?.toString())

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
                DrawerItem("Novedades") { /* acción */ }
                DrawerItem("Contenido reciente") { /* acción */ }
                DrawerItem("Configuración y privacidad") { /* acción */ }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
                DrawerItem("Cerrar sesión", icon = Icons.Default.Logout) {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Inicio", color = Color.White, fontWeight = FontWeight.SemiBold)
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("search") }) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            },
            containerColor = Color(0xFF0B0B0B),
            bottomBar = {
                // 🔹 MiniPlayer arriba + espacio + barra inferior
                Column {
                    // MiniPlayer con elevación y fondo
                    Surface(
                        tonalElevation = 8.dp,
                        shadowElevation = 12.dp,
                        color = Color(0xFF181818)
                    ) {
                        MiniPlayer(playerViewModel = playerViewModel)
                    }

                    // Espaciado entre el MiniPlayer y la barra inferior
                    Spacer(modifier = Modifier.height(8.dp))

                    // Barra inferior que nunca se oculta
                    HomeBottomBar(
                        navController = navController,
                        onCreateClick = { showCreateDialog = true }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                HomeContent(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    onOpenPlaylist = onOpenPlaylist,
                    playerViewModel = playerViewModel
                )
            }

            if (showCreateDialog) {
                showCreateDialog = false
                navController.navigate("create_playlist")
            }
        }
    }
}

@Composable
private fun DrawerHeader(displayName: String, photoUrl: String?) {
    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
        ) {
            photoUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Foto usuario",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = displayName, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let { Icon(it, contentDescription = label, tint = Color.White) }
        if (icon != null) Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, color = Color.White)
    }
}
