package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.models.SongUi

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    onOpenPlaylist: (String) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    var playlists by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var mixes by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔹 Cargar datos desde Firestore
    LaunchedEffect(true) {
        val playlistsRef = firestore.collection("playlists")
        val mixesRef = firestore.collection("mixes")

        // Si las colecciones no existen, las creamos con los datos base
        if (playlistsRef.get().await().isEmpty) {
            samplePlaylists().forEach { playlistsRef.document(it.id).set(it) }
        }
        if (mixesRef.get().await().isEmpty) {
            sampleMixes().forEach { mixesRef.document(it.id).set(it) }
        }

        // Obtener datos
        playlists = playlistsRef.get().await().documents.mapNotNull { it.toObject(PlaylistUi::class.java) }
        mixes = mixesRef.get().await().documents.mapNotNull { it.toObject(PlaylistUi::class.java) }

        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF1DB954))
        }
        return
    }

    Column(modifier = modifier.padding(12.dp)) {
        val chips = listOf("Todas", "Música", "Podcasts")
        var selectedChip by remember { mutableStateOf("Todas") }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(chips) { chip ->
                val isSelected = chip == selectedChip
                Button(
                    onClick = { selectedChip = chip },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF1DB954) else Color(0xFF404040)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(chip, color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text("Tus me gusta", color = Color.White, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            playlists.take(3).forEach { p ->
                SmallPlaylistCard(p) { onOpenPlaylist(p.id) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionCarousel(title = "Recomendados", items = mixes, onOpenPlaylist = onOpenPlaylist)
        Spacer(modifier = Modifier.height(12.dp))
        SectionCarousel(title = "Viernes de lanzamientos", items = playlists, onOpenPlaylist = onOpenPlaylist)
        Spacer(modifier = Modifier.height(12.dp))
        SectionCarousel(title = "Fiesta", items = mixes, onOpenPlaylist = onOpenPlaylist)
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun SmallPlaylistCard(playlist: PlaylistUi, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = playlist.imageUrl,
            contentDescription = playlist.title,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            playlist.title,
            color = Color.White,
            maxLines = 1,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SectionCarousel(title: String, items: List<PlaylistUi>, onOpenPlaylist: (String) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { p ->
                Column(
                    modifier = Modifier
                        .width(180.dp)
                        .clickable { onOpenPlaylist(p.id) }
                ) {
                    AsyncImage(
                        model = p.imageUrl,
                        contentDescription = p.title,
                        modifier = Modifier
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(p.title, color = Color.White, maxLines = 1, fontWeight = FontWeight.SemiBold)
                    p.description?.let { Text(it, color = Color.LightGray, maxLines = 1) }
                }
            }
        }
    }
}



/** 🎶 Datos de ejemplo (solo usados si Firestore está vacío) **/
fun samplePlaylists(): List<PlaylistUi> {
    val songs = listOf(
        SongUi("s1", "Ojitos Lindos", "Bad Bunny, Bomba Estéreo", "https://i1.sndcdn.com/artworks-9nl6NjbMrGyHyaga-Vb7ZKQ-t500x500.jpg"),
        SongUi("s2", "TQG", "Karol G, Shakira", "https://images.genius.com/dadaa2bfb41d2b90a917756b05930def.1000x1000x1.png"),
        SongUi("s3", "La Bachata", "Manuel Turizo", "https://images.genius.com/e12c2bf5c7e92522fb3718a15d79758f.1000x1000x1.png")
    )

    return listOf(
        PlaylistUi("p1", "Mix Favoritos", "Creado para ti", "https://i.scdn.co/image/ab67706f000000035c5c4b9abca6d79953e7b40a", songs),
        PlaylistUi("p2", "Tus me gusta", "Canciones que marcaste con ❤️", "https://misc.scdn.co/liked-songs/liked-songs-640.png", songs),
        PlaylistUi("p3", "Top Colombia", "Los éxitos más escuchados del país", "https://charts-images.scdn.co/assets/locale_en/regional/daily/region_co_default.jpg", songs),
        PlaylistUi("p4", "Reggaeton Mix", "Bad Bunny, Feid, Mora y más", "https://i.scdn.co/image/ab67706f000000033ebaf0e1cbf7e8f22f5457a7", songs),
        PlaylistUi("p5", "Pop en Español", "Sebastián Yatra, Morat, Reik y más", "https://i.scdn.co/image/ab67706f000000039d9f1eaf03c0d29c31786c4b", songs)
    )
}

fun sampleMixes(): List<PlaylistUi> {
    return listOf(
        PlaylistUi("m1", "Viernes de lanzamientos", "Lo más nuevo de tus artistas favoritos", "https://i.scdn.co/image/ab67706f000000035a9f0c9c73f83e8f17de74b8"),
        PlaylistUi("m2", "Fiesta Latina", "Puro perreo, reggaetón y ritmo 🔥", "https://i.scdn.co/image/ab67706f000000038efb0f9eb23e10e5b6239bcb"),
        PlaylistUi("m3", "Lo-Fi Beats", "Para estudiar o relajarte ☕", "https://i.scdn.co/image/ab67706f000000037d0de1b1dc653f2697b9c65f"),
        PlaylistUi("m4", "Workout Hits", "Motivación total para el gym 💪", "https://i.scdn.co/image/ab67706f000000035418b6d058bd1f93afdfdd58"),
        PlaylistUi("m5", "Clásicos del 2000", "Rihanna, Maroon 5, Coldplay y más", "https://i.scdn.co/image/ab67706f00000003a943f25575cf50f72b2b7e4a")
    )
}
