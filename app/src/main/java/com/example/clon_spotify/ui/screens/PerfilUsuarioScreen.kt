package com.example.clon_spotify.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.clon_spotify.viewmodel.FriendsViewModel
import com.example.clon_spotify.viewmodel.PerfilUsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen(
    userId: String,
    navController: NavController,
    viewModel: PerfilUsuarioViewModel = viewModel(),
    friendsViewModel: FriendsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    var isSeguido by remember { mutableStateOf(false) }

    // 🔹 Cargar perfil del usuario y verificar si ya lo sigue
    LaunchedEffect(userId) {
        viewModel.cargarPerfil(userId)
        friendsViewModel.isSeguidos(userId) { seguido ->
            isSeguido = seguido
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            userProfile?.let { user ->
                // Datos básicos del usuario
                Text(text = user.nombre, style = MaterialTheme.typography.headlineSmall)
                Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 Botón Seguir / Dejar de seguir
                Button(
                    onClick = {
                        if (!isSeguido) {
                            friendsViewModel.Seguidos(
                                user,
                                onSuccess = {
                                    isSeguido = true
                                    Toast.makeText(
                                        context,
                                        "Ahora sigues a ${user.nombre}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            friendsViewModel.dejarDeSeguir(
                                userId = user.uid,
                                onSuccess = {
                                    isSeguido = false
                                    Toast.makeText(
                                        context,
                                        "Dejaste de seguir a ${user.nombre}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSeguido)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (isSeguido) "Dejar de seguir" else "Seguir",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 🔹 Playlists del usuario
                Text("Playlists creadas", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (playlists.isEmpty()) {
                    Text("Este usuario aún no tiene playlists")
                } else {
                    LazyColumn {
                        items(playlists) { playlist ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(playlist.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        playlist.description ?: "",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (playlist.songs.isEmpty()) {
                                        Text("No hay canciones en esta playlist")
                                    } else {
                                        playlist.songs.forEach { song ->
                                            Text("🎵 ${song.title} - ${song.artist}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
