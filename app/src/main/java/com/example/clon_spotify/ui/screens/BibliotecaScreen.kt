package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.models.SongUi
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BibliotecaScreen(
    onOpenPlaylist: (String) -> Unit,
    homeNavController: NavHostController
)
{
    val firestore = FirebaseFirestore.getInstance()
    var playlists by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var likedSongs by remember { mutableStateOf<List<SongUi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Obtener datos en tiempo real igual que en HomeContent
    LaunchedEffect(Unit) {
        // Escuchar playlists del usuario
        firestore.collection("playlists").addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                playlists = snapshot.toObjects(PlaylistUi::class.java)
            }
        }

        // Escuchar canciones me gusta
        firestore.collection("me_gusta").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) likedSongs = snapshot.toObjects(SongUi::class.java)
        }

        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1DB954))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tu biblioteca",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { /* Navegar a búsqueda en biblioteca */ }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar en biblioteca",
                    tint = Color.White
                )
            }
        }

        // Filtros
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text("Playlists", color = Color.White) }
            )
            FilterChip(
                selected = false,
                onClick = { },
                label = { Text("Artistas", color = Color.White) }
            )
            FilterChip(
                selected = false,
                onClick = { },
                label = { Text("Álbumes", color = Color.White) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de playlists
        LibraryPlaylistsList(
            playlists = playlists,
            likedSongs = likedSongs,
            onOpenPlaylist = onOpenPlaylist
        )
    }
}

@Composable
fun LibraryPlaylistsList(
    playlists: List<PlaylistUi>,
    likedSongs: List<SongUi>,
    onOpenPlaylist: (String) -> Unit
) {
    val allPlaylists = remember(playlists, likedSongs) {
        // Crear playlist de "Tus me gusta" si hay canciones
        val tusMeGusta = if (likedSongs.isNotEmpty()) {
            listOf(
                PlaylistUi(
                    id = "tus_me_gusta",
                    title = "Tus me gusta",
                    description = "${likedSongs.size} canciones guardadas",
                    imageUrl = "https://misc.scdn.co/liked-songs/liked-songs-640.png",
                    songs = likedSongs
                )
            )
        } else {
            emptyList()
        }

        tusMeGusta + playlists
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (allPlaylists.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes playlists aún",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            items(allPlaylists) { playlist ->
                LibraryPlaylistItem(
                    playlist = playlist,
                    onPlaylistClick = { onOpenPlaylist(playlist.id) }
                )
            }
        }
    }
}

@Composable
fun LibraryPlaylistItem(playlist: PlaylistUi, onPlaylistClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        onClick = onPlaylistClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de la playlist
            AsyncImage(
                model = playlist.imageUrl,
                contentDescription = playlist.title,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playlist.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                Text(
                    text = if (playlist.id == "tus_me_gusta") {
                        "Playlist • ${playlist.songs.size} canciones"
                    } else {
                        "Playlist • ${playlist.description ?: ""}"
                    },
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    val containerColor = if (selected) Color(0xFF1DB954) else Color(0xFF333333)

    AssistChip(
        onClick = onClick,
        label = label,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = Color.White
        )
    )
}