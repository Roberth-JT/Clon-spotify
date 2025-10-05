package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.clon_spotify.models.SongUi
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    val firestore = FirebaseFirestore.getInstance()
    var query by remember { mutableStateOf("") }
    var songs by remember { mutableStateOf<List<SongUi>>(emptyList()) }
    var filteredSongs by remember { mutableStateOf<List<SongUi>>(emptyList()) }
    var selectedSong by remember { mutableStateOf<SongUi?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔹 Cargar canciones demo a Firestore si no existen
    LaunchedEffect(Unit) {
        val collection = firestore.collection("songs")
        collection.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                val demoSongs = demoSongList()
                demoSongs.forEach { song ->
                    collection.document(song.id).set(song)
                }
                songs = demoSongs
            } else {
                songs = snapshot.documents.mapNotNull { it.toObject(SongUi::class.java) }
            }
            filteredSongs = songs
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFF0B0B0B))
    ) {
        // 🔍 Barra de búsqueda
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                filteredSongs = if (query.isBlank()) {
                    songs
                } else {
                    songs.filter { song ->
                        song.title.contains(query, ignoreCase = true) ||
                                song.artist.contains(query, ignoreCase = true)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar canción o artista...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1DB954),
                cursorColor = Color(0xFF1DB954),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1DB954))
                }
            }

            filteredSongs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron resultados 😔", color = Color.Gray)
                }
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredSongs) { song ->
                        SongItem(song = song, onAddToPlaylist = { selectedSong = song })
                    }
                }
            }
        }

        // 🎵 Diálogo para agregar canción a playlist
        if (selectedSong != null) {
            AddToPlaylistDialog(
                song = selectedSong!!,
                onDismiss = { selectedSong = null }
            )
        }
    }
}

@Composable
fun SongItem(song: SongUi, onAddToPlaylist: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.imageUrl,
            contentDescription = song.title,
            modifier = Modifier
                .size(60.dp)
                .padding(end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(song.artist, color = Color.Gray)
        }
        IconButton(onClick = onAddToPlaylist) {
            Icon(Icons.Default.Add, contentDescription = "Agregar a playlist", tint = Color(0xFF1DB954))
        }
    }
}

// 🎶 Lista de canciones demo
fun demoSongList(): List<SongUi> = listOf(
    SongUi("1", "Shape of You", "Ed Sheeran", "https://i.scdn.co/image/ab67616d0000b2734b43db8b4c0c59df3e4dc68a"),
    SongUi("2", "Blinding Lights", "The Weeknd", "https://i.scdn.co/image/ab67616d0000b273f5df2f2a2c318ca42c6e3c4a"),
    SongUi("3", "Levitating", "Dua Lipa", "https://i.scdn.co/image/ab67616d0000b2739dc1dbf049d181d58bfa2762"),
    SongUi("4", "As It Was", "Harry Styles", "https://i.scdn.co/image/ab67616d0000b273c1d7b64b17e08f2bcd5a44b3"),
    SongUi("5", "Dance Monkey", "Tones and I", "https://i.scdn.co/image/ab67616d0000b273baf7f35a5c1b1db5dc76c9e3"),
    SongUi("6", "Stay", "The Kid LAROI & Justin Bieber", "https://i.scdn.co/image/ab67616d0000b2737b6e7b64a847bc51b9d6c79a"),
    SongUi("7", "Peaches", "Justin Bieber", "https://i.scdn.co/image/ab67616d0000b273e2d2e8d829c829b15c4f8d73"),
    SongUi("8", "Someone Like You", "Adele", "https://i.scdn.co/image/ab67616d0000b273d39c4b5a1cb9cb2a02b8b19a"),
    SongUi("9", "Happier Than Ever", "Billie Eilish", "https://i.scdn.co/image/ab67616d0000b273f6a9d3e48ceefc52c8dfc5e2"),
    SongUi("10", "Senorita", "Shawn Mendes & Camila Cabello", "https://i.scdn.co/image/ab67616d0000b273d1b1ff3d53e4f96c8e8a9411"),
    SongUi("11", "Perfect", "Ed Sheeran", "https://i.scdn.co/image/ab67616d0000b273c2b15a6465bb2a8b246c8b9a"),
    SongUi("12", "Don't Start Now", "Dua Lipa", "https://i.scdn.co/image/ab67616d0000b273f99c2f0ff0b3dce4e1d0d4b3"),
    SongUi("13", "Bad Guy", "Billie Eilish", "https://i.scdn.co/image/ab67616d0000b273c5c6a64cfb9e449c7f19b12e"),
    SongUi("14", "Watermelon Sugar", "Harry Styles", "https://i.scdn.co/image/ab67616d0000b273c99b85a1cb3c5b4a2f0b1b0d"),
    SongUi("15", "Flowers", "Miley Cyrus", "https://i.scdn.co/image/ab67616d0000b27326ad5e90a1d17e8123f6c9be")
)