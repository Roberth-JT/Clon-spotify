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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.player.PlayerViewModel
import com.example.clon_spotify.viewmodel.PerfilUsuarioViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicPlaylistScreen(
    playlistId: String,
    ownerId: String,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    viewModel: PerfilUsuarioViewModel = viewModel()
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    var playlist by remember { mutableStateOf<PlaylistUi?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    //  Cargar playlist pública desde el usuario dueño
    LaunchedEffect(playlistId, ownerId) {
        try {
            val playlistDoc = firestore.collection("usuarios")
                .document(ownerId)
                .collection("playlists")
                .document(playlistId)
                .get()
                .await()

            if (playlistDoc.exists()) {
                val loadedPlaylist = playlistDoc.toObject(PlaylistUi::class.java)
                playlist = loadedPlaylist?.copy(id = playlistId)
            } else {
                Toast.makeText(context, "Playlist no encontrada", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar playlist: ${e.message}", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
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

    if (playlist == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Playlist no disponible", color = Color.White)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist!!.title, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF0B0B0B),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // Imagen principal
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(playlist!!.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = playlist!!.title,
                modifier = Modifier
                    .height(220.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Título y descripción
            Text(
                playlist!!.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
            playlist!!.description?.let {
                Text(it, color = Color.LightGray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Canciones", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 Lista de canciones
            LazyColumn {
                items(playlist!!.songs.size) { idx ->
                    val song = playlist!!.songs[idx]

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                // Reproducir la canción en el contexto de esta playlist
                                playerViewModel.playSongInPlaylist(song, playlist!!.songs, context)
                                Toast.makeText(
                                    context,
                                    "Reproduciendo: ${song.title}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(song.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = song.title,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title, color = Color.White)
                            Text(song.artist, color = Color.LightGray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}