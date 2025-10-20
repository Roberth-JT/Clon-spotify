package com.example.clon_spotify.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.models.SongUi
import com.example.clon_spotify.player.MiniPlayer
import com.example.clon_spotify.player.PlayerViewModel
import com.example.clon_spotify.viewmodel.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistId: String?,
    playerViewModel: PlayerViewModel
) {

    val firestore = FirebaseFirestore.getInstance()
    val viewModel: HomeViewModel = viewModel()
    val context = LocalContext.current

    var playlist by remember { mutableStateOf<PlaylistUi?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<SongUi?>(null) }

    // 🔹 Cargar la playlist desde Firestore
    LaunchedEffect(playlistId) {
        if (playlistId == null) return@LaunchedEffect

        val collections = listOf("playlists", "mixes", "recomendados", "albumes")

        if (playlistId == "tus_me_gusta") {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val snapshot = firestore.collection("usuarios")
                    .document(userId)
                    .collection("me_gusta")
                    .get()
                    .await()

                val likedSongs = snapshot.toObjects(SongUi::class.java)
                playlist = PlaylistUi(
                    id = "tus_me_gusta",
                    title = "Tus me gusta",
                    description = "Canciones que marcaste con ❤️",
                    imageUrl = "https://misc.scdn.co/liked-songs/liked-songs-640.png",
                    songs = likedSongs
                )
            }

        } else {
            for (col in collections) {
                val usuariosId = FirebaseAuth.getInstance().currentUser?.uid
                if (usuariosId != null) {
                    val doc = firestore.collection("usuarios")
                        .document(usuariosId)
                        .collection(col)
                        .document(playlistId)
                        .get()
                        .await()

                    if (doc.exists()) {
                        playlist = doc.toObject(PlaylistUi::class.java)
                        break // salimos del for si la encontramos
                    }
                }
            }

        }
    }

    // Pantalla de carga
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

    // BottomSheet de opciones
    if (showBottomSheet && selectedSong != null) {
        SongOptionsBottomSheet(
            song = selectedSong!!,
            playlist = playlist!!,
            onDismiss = { showBottomSheet = false },
            onSongDeleted = { deletedSong ->
                playlist = playlist!!.copy(
                    songs = playlist!!.songs.filter { it.id != deletedSong.id }
                )
            }
        )
    }

    // 🎧 UI principal
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist!!.title, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color(0xFF0B0B0B),

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
                    val song = playlist!!.songs[idx]

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                playerViewModel.playSong(song, context)
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
                            Text(song.artist, color = Color.LightGray)
                        }

                        IconButton(onClick = {
                            selectedSong = song
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
fun SongOptionsBottomSheet(
    song: SongUi,
    playlist: PlaylistUi,
    onDismiss: () -> Unit,
    onSongDeleted: (SongUi) -> Unit
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    var isSaving by remember { mutableStateOf(false) }

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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
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

            // ❤️ Agregar a Me gusta
            OptionItem(
                iconUrl = "https://misc.scdn.co/liked-songs/liked-songs-640.png",
                label = if (isSaving) "Guardando..." else "Agregar a Tus me gusta",
                onClick = {
                    if (!isSaving) {
                        isSaving = true
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@OptionItem
                        val songRef = firestore.collection("usuarios")
                            .document(userId)
                            .collection("me_gusta")
                            .document(song.id)

                        songRef.get().addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                Toast.makeText(context, "Ya está en tus me gusta 💜", Toast.LENGTH_SHORT).show()
                                isSaving = false
                            } else {
                                songRef.set(song)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Agregado a Tus me gusta 💜", Toast.LENGTH_SHORT).show()
                                        isSaving = false
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error al agregar 😢", Toast.LENGTH_SHORT).show()
                                        isSaving = false
                                    }
                            }
                        }
                    }
                }
            )

            // 📤 Compartir
            OptionItem("https://cdn-icons-png.flaticon.com/512/786/786205.png", "Compartir")

            // ➕ Agregar a otra playlist
            OptionItem("https://cdn-icons-png.flaticon.com/512/1828/1828817.png", "Agregar a otra playlist")

            // ❌ Eliminar (soporta playlists o tus me gusta)
            OptionItem(
                iconUrl = "https://cdn-icons-png.flaticon.com/512/1828/1828843.png",
                label = "Eliminar de esta playlist",
                onClick = {
                    if (playlist.id == "tus_me_gusta") {
                        // Eliminar de me gusta
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@OptionItem
                        firestore.collection("usuarios")
                            .document(userId)
                            .collection("me_gusta")
                            .document(song.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Eliminado de Tus me gusta 💔", Toast.LENGTH_SHORT).show()
                                onSongDeleted(song)
                                onDismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Eliminar de playlist normal
                        val usuariosId = FirebaseAuth.getInstance().currentUser?.uid

                        firestore.collection("usuarios")
                            .document(usuariosId ?: "")
                            .collection("playlists")
                            .document(playlist.id)
                            .update("songs", playlist.songs.filter { it.id != song.id })

                            .addOnSuccessListener {
                                Toast.makeText(context, "Canción eliminada ❌", Toast.LENGTH_SHORT).show()
                                onSongDeleted(song)
                                onDismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            )

            Divider(color = Color.DarkGray, thickness = 0.7.dp)
            OptionItem("https://cdn-icons-png.flaticon.com/512/565/565547.png", "Ir al álbum")
            OptionItem("https://cdn-icons-png.flaticon.com/512/747/747376.png", "Ir al artista")
        }
    }
}

@Composable
fun OptionItem(iconUrl: String, label: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .let { if (onClick != null) it.clickable { onClick() } else it },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = iconUrl,
            contentDescription = label,
            modifier = Modifier
                .size(40.dp)
                .padding(end = 16.dp)
        )
        Text(label, color = Color.White, fontSize = 17.sp)
    }
}
