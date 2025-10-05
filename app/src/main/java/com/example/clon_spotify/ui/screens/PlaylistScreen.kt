package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.models.SongUi



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(playlistId: String?) {
    val firestore = FirebaseFirestore.getInstance()

    var playlist by remember { mutableStateOf<PlaylistUi?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<SongUi?>(null) }

    // 🔸 Carga de playlist desde Firestore
    LaunchedEffect(playlistId) {
        if (playlistId != null) {
            val doc = firestore.collection("playlists").document(playlistId).get().await()
            playlist = doc.toObject(PlaylistUi::class.java)
        }
    }

    // ⏳ Loading State
    if (playlist == null) {
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

    // 🔽 Opciones inferiores
    if (showBottomSheet && selectedSong != null) {
        SongOptionsBottomSheet(
            song = selectedSong!!,
            onDismiss = { showBottomSheet = false }
        )
    }

    // 🎧 Pantalla principal
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist!!.title, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color(0xFF0B0B0B)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
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

            Text(
                playlist!!.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )

            playlist!!.description?.let { Text(it, color = Color.LightGray) }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Canciones", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(playlist!!.songs.size) { idx ->
                    val s = playlist!!.songs[idx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(s.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = s.title,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.title, color = Color.White)
                            Text(s.artist, color = Color.LightGray)
                        }

                        IconButton(onClick = {
                            selectedSong = s
                            showBottomSheet = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsBottomSheet(song: SongUi, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF181818),
        dragHandle = { Box(modifier = Modifier.height(10.dp)) },
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF181818))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                AsyncImage(
                    model = song.imageUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(55.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(song.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(song.artist, color = Color.Gray, fontSize = 13.sp)
                }
            }

            Divider(color = Color.DarkGray, thickness = 0.7.dp)
            Spacer(modifier = Modifier.height(8.dp))

            OptionItem("https://cdn-icons-png.flaticon.com/512/786/786205.png", "Compartir")
            OptionItem("https://cdn-icons-png.flaticon.com/512/1828/1828817.png", "Agregar a otra playlist")
            OptionItem("https://cdn-icons-png.flaticon.com/512/1828/1828843.png", "Eliminar de esta playlist")

            Divider(color = Color.DarkGray, thickness = 0.7.dp)

            OptionItem("https://cdn-icons-png.flaticon.com/512/565/565547.png", "Ir al álbum")
            OptionItem("https://cdn-icons-png.flaticon.com/512/747/747376.png", "Ir al artista")

            Divider(color = Color.DarkGray, thickness = 0.7.dp)

            OptionItem("https://cdn-icons-png.flaticon.com/512/552/552721.png", "Ir a radio de la canción")
            OptionItem("https://cdn-icons-png.flaticon.com/512/1250/1250615.png", "Ver los créditos de la canción")
            OptionItem("https://cdn-icons-png.flaticon.com/512/992/992703.png", "Mostrar código de Spotify")

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun OptionItem(iconUrl: String, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = iconUrl,
            contentDescription = label,
            modifier = Modifier
                .size(40.dp)
                .padding(end = 16.dp)
        )
        Text(
            label,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
