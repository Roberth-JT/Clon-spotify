package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.models.SongUi
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AddToPlaylistDialog(
    song: SongUi,
    onDismiss: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    var playlists by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var selectedPlaylist by remember { mutableStateOf<PlaylistUi?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

    // 🔹 Cargar playlists del usuario (por ahora todas)
    LaunchedEffect(Unit) {
        firestore.collection("playlists")
            .get()
            .addOnSuccessListener { snapshot ->
                playlists = snapshot.documents.mapNotNull { it.toObject(PlaylistUi::class.java) }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121212),
        title = { Text("Agregar a playlist", color = Color.White) },
        text = {
            Column {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1DB954)
                    )
                } else if (playlists.isEmpty()) {
                    Text(
                        "No tienes playlists creadas 😔",
                        color = Color.Gray
                    )
                } else {
                    playlists.forEach { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedPlaylist = playlist
                                },
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                playlist.title, // ✅ se usa title, no name
                                color = if (selectedPlaylist == playlist) Color(0xFF1DB954) else Color.White
                            )
                            if (selectedPlaylist == playlist) {
                                Text("✔", color = Color(0xFF1DB954))
                            }
                        }
                    }
                }

                if (message != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(message!!, color = Color(0xFF1DB954))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedPlaylist?.let { playlist ->
                        val playlistRef = firestore.collection("playlists").document(playlist.id)

                        // 🔹 Agregar la canción a la lista de songs del documento playlist
                        playlistRef.get()
                            .addOnSuccessListener { snapshot ->
                                val current = snapshot.toObject(PlaylistUi::class.java)
                                val updatedSongs = (current?.songs ?: emptyList()) + song

                                playlistRef.update("songs", updatedSongs)
                                    .addOnSuccessListener {
                                        message = "✅ Canción agregada a '${playlist.title}'"
                                    }
                                    .addOnFailureListener {
                                        message = "❌ Error al agregar la canción"
                                    }
                            }
                    }
                },
                enabled = selectedPlaylist != null,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
            ) {
                Text("Agregar", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}