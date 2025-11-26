// BibliotecaScreen.kt
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.models.SongUi
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BibliotecaScreen(
    homeNavController: NavController,
    onOpenPlaylist: (String) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    var playlists by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var likedSongs by remember { mutableStateOf<List<SongUi>>(emptyList()) }
    var albumes by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var artistas by remember { mutableStateOf<List<Artista>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Estado para el filtro seleccionado
    var selectedFilter by remember { mutableStateOf("Playlists") }

    // Obtener datos en tiempo real
    LaunchedEffect(Unit) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            isLoading = false
            return@LaunchedEffect
        }

        val userDoc = firestore.collection("usuarios").document(userId)

        // Escuchar playlists del usuario
        userDoc.collection("playlists").addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                playlists = snapshot.toObjects(PlaylistUi::class.java)
            }
        }

        // Escuchar canciones "Me gusta" del usuario
        userDoc.collection("me_gusta").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) likedSongs = snapshot.toObjects(SongUi::class.java)
        }

        // Escuchar álbumes del usuario
        userDoc.collection("albumes").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) albumes = snapshot.toObjects(PlaylistUi::class.java)
        }

        //Generar lista única de artistas en base a las playlists existentes
        val allSongs = playlists.flatMap { it.songs } +
                albumes.flatMap { it.songs } +
                likedSongs

        artistas = allSongs
            .map { song -> Artista(
                id = song.artist,
                name = song.artist,
                imageUrl = song.imageUrl, // puedes cambiar por imagen distinta si lo prefieres
                followers = 0,
                genres = emptyList()
            )}
            .distinctBy { it.name }


        isLoading = false
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
                onClick = { homeNavController.navigate("search")}
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
                selected = selectedFilter == "Playlists",
                onClick = { selectedFilter = "Playlists" },
                label = { Text("Playlists", color = Color.White) }
            )
            FilterChip(
                selected = selectedFilter == "Artistas",
                onClick = { selectedFilter = "Artistas" },
                label = { Text("Artistas", color = Color.White) }
            )
            FilterChip(
                selected = selectedFilter == "Álbumes",
                onClick = { selectedFilter = "Álbumes" },
                label = { Text("Álbumes", color = Color.White) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido según el filtro seleccionado
        when (selectedFilter) {
            "Playlists" -> LibraryPlaylistsList(
                playlists = playlists,
                likedSongs = likedSongs,
                onOpenPlaylist = onOpenPlaylist
            )
            "Artistas" -> LibraryArtistsList(
                artistas = artistas,
                onArtistClick = { artistaId ->
                    // Navegar a pantalla del artista
                    homeNavController.navigate("artist/$artistaId")
                }
            )
            "Álbumes" -> LibraryAlbumsList(
                albumes = albumes,
                onAlbumClick = { albumId ->
                    onOpenPlaylist(albumId)
                }
            )
        }
    }
}

// Componente para lista de Playlists (ya lo tenías)
@Composable
fun LibraryPlaylistsList(
    playlists: List<PlaylistUi>,
    likedSongs: List<SongUi>,
    onOpenPlaylist: (String) -> Unit
) {
    val allPlaylists = remember(playlists, likedSongs) {
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
                EmptyState(message = "No tienes playlists aún")
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

// Componente para lista de Artistas
@Composable
fun LibraryArtistsList(
    artistas: List<Artista>,
    onArtistClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (artistas.isEmpty()) {
            item {
                EmptyState(message = "No tienes artistas seguidos")
            }
        } else {
            items(artistas) { artista ->
                LibraryArtistItem(
                    artista = artista,
                    onArtistClick = { onArtistClick(artista.id) }
                )
            }
        }
    }
}

// Componente para lista de Álbumes
@Composable
fun LibraryAlbumsList(
    albumes: List<PlaylistUi>,
    onAlbumClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (albumes.isEmpty()) {
            item {
                EmptyState(message = "No tienes álbumes guardados")
            }
        } else {
            items(albumes) { album ->
                LibraryAlbumItem(
                    album = album,
                    onAlbumClick = { onAlbumClick(album.id) }
                )
            }
        }
    }
}

// Item para Artista
@Composable
fun LibraryArtistItem(artista: Artista, onArtistClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        onClick = onArtistClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen circular del artista
            AsyncImage(
                model = artista.imageUrl,
                contentDescription = artista.name,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(25.dp)) // Circular
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = artista.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                Text(
                    text = "Artista",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

// Item para Álbum
@Composable
fun LibraryAlbumItem(album: PlaylistUi, onAlbumClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        onClick = onAlbumClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = album.imageUrl,
                contentDescription = album.title,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = album.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                Text(
                    text = "Álbum • ${album.description ?: ""}",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

// Item para Playlist (ya lo tenías)
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

// Estado vacío
@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.Gray,
            fontSize = 16.sp
        )
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

// Modelo de Artista (necesitas crear este data class)
data class Artista(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val followers: Int = 0,
    val genres: List<String> = emptyList()
)