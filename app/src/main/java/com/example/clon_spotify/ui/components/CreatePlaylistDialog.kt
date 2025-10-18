package com.example.clon_spotify.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clon_spotify.models.PlaylistUi
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@Composable
fun HomeBottomBar(
    navController: NavController
) {
    // 🔹 Estado para mostrar o cerrar el diálogo
    var showDialog by remember { mutableStateOf(false) }

    // 🔹 Barra inferior con las secciones
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
            onClick = { showDialog = true }, // ✅ ahora abre el diálogo directamente
            icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
            label = { Text("Crear") },
            alwaysShowLabel = false
        )
    }

    // 🔹 Mostrar diálogo si el usuario toca el botón "+"
    if (showDialog) {
        CreatePlaylistDialog(
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit
) {
    var playlistTitle by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }

    val firestore = FirebaseFirestore.getInstance()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212),
        title = {
            Text(
                "Crear nueva playlist",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = playlistTitle,
                    onValueChange = { playlistTitle = it },
                    label = { Text("Nombre de la playlist", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = playlistDescription,
                    onValueChange = { playlistDescription = it },
                    label = { Text("Descripción (opcional)", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (playlistTitle.isNotBlank()) {
                        val playlistId = UUID.randomUUID().toString()
                        val newPlaylist = PlaylistUi(
                            id = playlistId,
                            title = playlistTitle,
                            description = playlistDescription,
                            imageUrl = "",
                            songs = emptyList()
                        )

                        firestore.collection("playlists")
                            .document(playlistId)
                            .set(newPlaylist)
                            .addOnSuccessListener {
                                println("✅ Playlist guardada en Firestore")
                            }
                            .addOnFailureListener {
                                println("❌ Error al guardar playlist: ${it.message}")
                            }
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
            ) {
                Text("Crear", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}
