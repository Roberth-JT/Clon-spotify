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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlaylistDialog(
    navController: NavController
) {
    var playlistTitle by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) } // 🔹 Nueva variable
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

                    // 🔒 Selector de privacidad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isPublic) "Playlist pública 🌍" else "Playlist privada 🔒",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isPublic)
                                    "Cualquier usuario que sigas podrá verla."
                                else
                                    "Solo tú podrás verla.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = isPublic,
                            onCheckedChange = { isPublic = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF1DB954),
                                uncheckedThumbColor = Color.Gray
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null && playlistTitle.isNotBlank()) {
                            val playlistId = UUID.randomUUID().toString()

                            val newPlaylist = PlaylistUi(
                                id = playlistId,
                                title = playlistTitle,
                                description = playlistDescription,
                                imageUrl = "https://cdn-icons-png.flaticon.com/512/1384/1384060.png",
                                songs = emptyList(),
                                isPublic = isPublic // 🔹 Guardamos el valor
                            )

                            firestore.collection("usuarios")
                                .document(userId)
                                .collection("playlists")
                                .document(playlistId)
                                .set(newPlaylist)
                                .addOnSuccessListener {
                                    println("✅ Playlist guardada (${if (isPublic) "pública" else "privada"})")
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