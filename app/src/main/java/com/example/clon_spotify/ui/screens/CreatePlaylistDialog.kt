package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistDialog(
    navController: NavController
) {
    var playlistTitle by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(true) }

    val firestore = FirebaseFirestore.getInstance()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                navController.popBackStack()
            },
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

                        showDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Text("Crear", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    navController.popBackStack()
                }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}