package com.example.clon_spotify.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.models.SongUi
import com.example.clon_spotify.player.PlayerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ArtistSongsScreen(
    artistName: String,
    navController: NavController,
    playerViewModel: PlayerViewModel
) {
    val firestore = FirebaseFirestore.getInstance()
    var cancionesDelArtista by remember { mutableStateOf<List<SongUi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Obtener todas las canciones de este artista
    LaunchedEffect(artistName) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            isLoading = false
            return@LaunchedEffect
        }

        val userDoc = firestore.collection("usuarios").document(userId)

        try {
            // Obtener todas las colecciones
            val playlistsSnapshot = userDoc.collection("playlists").get().await()
            val albumesSnapshot = userDoc.collection("albumes").get().await()
            val mixesSnapshot = userDoc.collection("mixes").get().await()
            val recomendadosSnapshot = userDoc.collection("recomendados").get().await()
            val meGustaSnapshot = userDoc.collection("me_gusta").get().await()
            val todasLasCanciones = playlistsSnapshot.documents.flatMap { doc ->
                doc.toObject(PlaylistUi::class.java)?.songs ?: emptyList()
            } + albumesSnapshot.documents.flatMap { doc ->
                doc.toObject(PlaylistUi::class.java)?.songs ?: emptyList()
            } + mixesSnapshot.documents.flatMap { doc ->
                doc.toObject(PlaylistUi::class.java)?.songs ?: emptyList()
            } + recomendadosSnapshot.documents.flatMap { doc ->
                doc.toObject(PlaylistUi::class.java)?.songs ?: emptyList()
            } + meGustaSnapshot.toObjects(SongUi::class.java)

            cancionesDelArtista = todasLasCanciones
                .filter { it.artist == artistName }
                .distinctBy { it.id } // Evitar duplicados

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF1DB954))
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Botón de regreso
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Información del artista
                val imagenArtista = cancionesDelArtista.firstOrNull()?.imageUrl ?: ""
                AsyncImage(
                    model = imagenArtista,
                    contentDescription = artistName,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = artistName,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${cancionesDelArtista.size} ${if (cancionesDelArtista.size == 1) "canción" else "canciones"}",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botón de reproducción de todas las canciones
                Button(
                    onClick = {
                        if (cancionesDelArtista.isNotEmpty()) {
                            // Reproducir toda la lista del artista
                            playerViewModel.playSongInPlaylist(
                                cancionesDelArtista.first(),
                                cancionesDelArtista,
                                context
                            )
                            Toast.makeText(
                                context,
                                "Reproduciendo canciones de $artistName",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1DB954)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Reproducir",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reproducir todas", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Canciones",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Lista de canciones del artista
        if (cancionesDelArtista.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron canciones de $artistName",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            items(cancionesDelArtista) { cancion ->
                SongItem(
                    song = cancion,
                    onSongClick = {
                        // Reproducir la canción dentro del contexto de la lista del artista
                        playerViewModel.playSongInPlaylist(cancion, cancionesDelArtista, context)
                        Toast.makeText(
                            context,
                            "Reproduciendo: ${cancion.title}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    playerViewModel = playerViewModel
                )
                Divider(color = Color(0xFF282828), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun SongItem(
    song: SongUi,
    onSongClick: () -> Unit,
    playerViewModel: PlayerViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSongClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.imageUrl,
            contentDescription = song.title,
            modifier = Modifier.size(50.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Text(
                text = song.artist,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Reproducir",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}