package com.example.clon_spotify.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.clon_spotify.ui.components.HomeBottomBar
import com.example.clon_spotify.ui.components.MiniPlayer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDrawerScreen(
    navController: NavController,
    onOpenPlaylist: (playlistId: String) -> Unit = { id -> navController.navigate("playlist/$id") }
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val displayName = currentUser?.displayName ?: currentUser?.email ?: "Usuario"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.background(Color(0xFF0B0B0B))) {
                DrawerHeader(displayName = displayName, photoUrl = currentUser?.photoUrl?.toString())
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
                DrawerItem(label = "Novedades") { /* placeholder */ }
                DrawerItem(label = "Contenido reciente") { /* placeholder */ }
                DrawerItem(label = "Configuración y privacidad") { /* placeholder */ }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray)
                DrawerItem(label = "Cerrar sesión", icon = Icons.Default.Logout) {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Inicio",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
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
                HomeBottomBar(navController = navController, onCreate = { /* handled in content with state */ })
            }
        ) { padding ->
            HomeContent(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                navController = navController,
                onOpenPlaylist = onOpenPlaylist
            )
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                MiniPlayer()
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