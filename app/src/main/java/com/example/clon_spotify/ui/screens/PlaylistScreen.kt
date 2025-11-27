package com.example.clon_spotify.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.models.SongUi
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
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var playlist by remember { mutableStateOf<PlaylistUi?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedSong by remember { mutableStateOf<SongUi?>(null) }

    //  Cargar playlist desde Firestore
    LaunchedEffect(playlistId) {
        if (playlistId == null || userId == null) return@LaunchedEffect

        val collections = listOf("playlists", "mixes", "recomendados", "albumes")

        if (playlistId == "tus_me_gusta") {
            val snapshot = firestore.collection("usuarios")
                .document(userId)
                .collection("me_gusta")
                .get()
                .await()

            playlist = PlaylistUi(
                id = "tus_me_gusta",
                title = "Tus me gusta",
                description = "Canciones que marcaste con ❤️",
                imageUrl = "https://misc.scdn.co/liked-songs/liked-songs-640.png",
                songs = snapshot.toObjects(SongUi::class.java)
            )
        } else {
            for (col in collections) {
                val doc = firestore.collection("usuarios")
                    .document(userId)
                    .collection(col)
                    .document(playlistId)
                    .get()
                    .await()

                if (doc.exists()) {
                    val loaded = doc.toObject(PlaylistUi::class.java)
                    // Aseguramos usar solo el campo correcto
                    playlist = loaded?.copy(
                        isPublic = loaded.isPublic || doc.getBoolean("public") == true
                    )
                    break
                }
            }
        }
    }

    if (playlist == null) {
        // Pantalla de carga
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

    // Interfaz principal
    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(playlist!!.title, color = Color.White) },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
//            )
//        },
        //containerColor = Color(0xFF0B0B0B),
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
                    .height(200.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
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

            // Switch Pública / Privada
            if (userId != null && playlistId != "tus_me_gusta") {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (playlist!!.isPublic) "Pública 🌍" else "Privada 🔒",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Switch(
                        checked = playlist!!.isPublic,
                        onCheckedChange = { newValue ->
                            val ref = firestore.collection("usuarios")
                                .document(userId)
                                .collection("playlists")
                                .document(playlist!!.id)

                            // Actualizamos ambos campos para evitar conflictos
                            ref.update(
                                mapOf(
                                    "isPublic" to newValue,
                                    "public" to newValue
                                )
                            ).addOnSuccessListener {
                                playlist = playlist!!.copy(isPublic = newValue)
                                Toast.makeText(
                                    context,
                                    if (newValue) "Playlist marcada como Pública 🌍"
                                    else "Playlist ahora es Privada 🔒",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.addOnFailureListener {
                                Toast.makeText(context, "No tienes permiso para cambiar el estado de esta playlist", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF1DB954),
                            uncheckedThumbColor = Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Canciones", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            // Lista de canciones
            LazyColumn {
                items(playlist!!.songs.size) { idx ->
                    val song = playlist!!.songs[idx]

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                //  Usar playSongInPlaylist en lugar de playSong
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
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF181818),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF181818))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Info de la canción
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

            //  Agregar a me gusta
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
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Error al agregar 😢", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnCompleteListener { isSaving = false }
                            }
                        }
                    }
                }
            )
            OptionItem(
                iconUrl = "https://cdn-icons-png.flaticon.com/512/1828/1828817.png",
                label = "Agregar a otra playlist",
                onClick = {
                    showAddToPlaylistDialog = true
                }
            )
            if (showAddToPlaylistDialog) {
                AddToPlaylistDialog(
                    song = song,
                    onDismiss = { showAddToPlaylistDialog = false }
                )
            }

            //  Eliminar canción
            OptionItem(
                iconUrl = "https://cdn-icons-png.flaticon.com/512/1828/1828843.png",
                label = "Eliminar de esta playlist",
                onClick = {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@OptionItem

                    if (playlist.id == "tus_me_gusta") {
                        // 🔥 Eliminar desde me_gusta
                        firestore.collection("usuarios")
                            .document(userId)
                            .collection("me_gusta")
                            .document(song.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Quitado de Tus me gusta 💜", Toast.LENGTH_SHORT).show()
                                onSongDeleted(song)
                                onDismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error al eliminar 😢", Toast.LENGTH_SHORT).show()
                            }
                        return@OptionItem
                    }

                    // 🔥 Eliminar desde playlist normal
                    val ref = firestore.collection("usuarios")
                        .document(userId)
                        .collection("playlists")
                        .document(playlist.id)

                    val nuevaLista = playlist.songs.filter { it.id != song.id }

                    ref.update("songs", nuevaLista)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Canción eliminada", Toast.LENGTH_SHORT).show()
                            onSongDeleted(song)
                            onDismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "No tienes permiso para eliminar", Toast.LENGTH_SHORT).show()
                        }
                }
            )
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