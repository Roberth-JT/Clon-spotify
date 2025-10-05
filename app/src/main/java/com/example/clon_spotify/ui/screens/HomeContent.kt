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
import com.google.firebase.firestore.SetOptions
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
    var recomendados by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔹 Cargar o actualizar datos desde Firestore
    LaunchedEffect(true) {
        val playlistsRef = firestore.collection("playlists")
        val mixesRef = firestore.collection("mixes")
        val recomRef = firestore.collection("recomendados")

        // 🔸 Crear o actualizar las colecciones
        samplePlaylists().forEach { playlistsRef.document(it.id).set(it, SetOptions.merge()) }
        sampleMixes().forEach { mixesRef.document(it.id).set(it, SetOptions.merge()) }
        sampleRecomendados().forEach { recomRef.document(it.id).set(it, SetOptions.merge()) }

        // 🔸 Obtener datos
        playlists = playlistsRef.get().await().documents.mapNotNull { it.toObject(PlaylistUi::class.java) }
        mixes = mixesRef.get().await().documents.mapNotNull { it.toObject(PlaylistUi::class.java) }
        recomendados = recomRef.get().await().documents.mapNotNull { it.toObject(PlaylistUi::class.java) }

        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

        // 🔸 Tus me gusta
        Text("Tus me gusta", color = Color.White, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            playlists.take(3).forEach { p -> SmallPlaylistCard(p) { onOpenPlaylist(p.id) } }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔸 Recomendados
        SectionCarousel(title = "Recomendados", items = recomendados, onOpenPlaylist = onOpenPlaylist)

        Spacer(modifier = Modifier.height(12.dp))

        // 🔸 Viernes de lanzamientos (MIXES)
        SectionCarousel(title = "Viernes de lanzamientos", items = mixes, onOpenPlaylist = onOpenPlaylist)

        Spacer(modifier = Modifier.height(12.dp))
        SectionCarousel(title = "Fiesta", items = mixes, onOpenPlaylist = onOpenPlaylist)
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun SmallPlaylistCard(playlist: PlaylistUi, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(120.dp).clickable { onClick() }
    ) {
        AsyncImage(
            model = playlist.imageUrl,
            contentDescription = playlist.title,
            modifier = Modifier.size(120.dp).clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(playlist.title, color = Color.White, maxLines = 1, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SectionCarousel(title: String, items: List<PlaylistUi>, onOpenPlaylist: (String) -> Unit) {
    Column {
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { p ->
                Column(
                    modifier = Modifier.width(180.dp).clickable { onOpenPlaylist(p.id) }
                ) {
                    AsyncImage(
                        model = p.imageUrl,
                        contentDescription = p.title,
                        modifier = Modifier.height(180.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(p.title, color = Color.White, maxLines = 1, fontWeight = FontWeight.SemiBold)
                    p.description?.let { Text(it, color = Color.LightGray, maxLines = 1) }
                }
            }
        }
    }
}

/** 🎧 Playlists base **/
fun samplePlaylists(): List<PlaylistUi> {
    val mixFavoritosSongs = listOf(
        SongUi("s1", "Easy On Me", "Adele", "https://i.scdn.co/image/ab67616d0000b2730b1f5d905e76a24d03a9c7b7"),
        SongUi("s2", "Talking to the Moon", "Bruno Mars", "https://i.scdn.co/image/ab67616d0000b2734b4c4e084bcb9f6ce0d93e5b")
    )

    val tusMeGustaSongs = listOf(
        SongUi("s3", "Someone Like You", "Adele", "https://i.scdn.co/image/ab67616d0000b273b9e6248f08b9b5684a6d2b0a"),
        SongUi("s4", "When I Was Your Man", "Bruno Mars", "https://i.scdn.co/image/ab67616d0000b273c64820a1e83d20c7e1d16b7c"),
        SongUi("s5", "Flowers", "Miley Cyrus", "https://i.scdn.co/image/ab67616d0000b2733bbcd66c6d99c07c94a5e2e3")
    )

    val topColombiaSongs = listOf(
        SongUi("s6", "Provenza", "Karol G", "https://i.scdn.co/image/ab67616d0000b2733f7657a8fce0b8a7b57b8f6a"),
        SongUi("s7", "TQG", "Karol G, Shakira", "https://i.scdn.co/image/ab67616d0000b273c79a75aee8459a49d0aaffd5"),
        SongUi("s8", "La Bachata", "Manuel Turizo", "https://i.scdn.co/image/ab67616d0000b273e12c2bf5c7e92522fb3718a1")
    )

    return listOf(
        PlaylistUi("p1", "Mix Favoritos", "Tus canciones más escuchadas", "https://i.scdn.co/image/ab67706f000000035c5c4b9abca6d79953e7b40a", mixFavoritosSongs),
        PlaylistUi("p2", "Tus me gusta", "Canciones que marcaste con ❤️", "https://misc.scdn.co/liked-songs/liked-songs-640.png", tusMeGustaSongs),
        PlaylistUi("p3", "Top Colombia", "Los éxitos más escuchados del país", "https://charts-images.scdn.co/assets/locale_en/regional/daily/region_co_default.jpg", topColombiaSongs)
    )
}

/** 🔹 Recomendados **/
fun sampleRecomendados(): List<PlaylistUi> {
    val popActualSongs = listOf(
        SongUi("s1", "Dance The Night", "Dua Lipa", "https://i.scdn.co/image/ab67616d0000b273d0a9b22b54e982c1e5efcc3f"),
        SongUi("s2", "As It Was", "Harry Styles", "https://i.scdn.co/image/ab67616d0000b273a64e6e4e79ac9d580f1f0c87"),
        SongUi("s3", "Kill Bill", "SZA", "https://i.scdn.co/image/ab67616d0000b273aa8b3d889a1e0da3bbdbb9a4")
    )

    val indieCoolSongs = listOf(
        SongUi("s4", "Sunflower", "Post Malone, Swae Lee", "https://i.scdn.co/image/ab67616d0000b27335b6b6e75a8dfb8a9d35d8a7"),
        SongUi("s5", "Electric Feel", "MGMT", "https://i.scdn.co/image/ab67616d0000b273cb3a8899b7c76087b7a9a31f"),
        SongUi("s6", "Lost in Yesterday", "Tame Impala", "https://i.scdn.co/image/ab67616d0000b273214d6937a3c88f6a17cb78c1")
    )

    val baladas2025Songs = listOf(
        SongUi("s7", "The Night We Met", "Lord Huron", "https://i.scdn.co/image/ab67616d0000b2739adbded6bcbf25a4d7a10f62"),
        SongUi("s8", "Let Her Go", "Passenger", "https://i.scdn.co/image/ab67616d0000b273179f5a4d31c660cead1ab6cc"),
        SongUi("s9", "When I Look At You", "Miley Cyrus", "https://i.scdn.co/image/ab67616d0000b2730c91cf5bb2a7a88a1b67e89f")
    )

    return listOf(
        PlaylistUi("r1", "Pop Actual", "Los hits más nuevos del pop", "https://i.scdn.co/image/ab67706f00000003a4978e4b2b88bcb66f4ce3d8", popActualSongs),
        PlaylistUi("r2", "Indie Cool", "Sonidos frescos y diferentes", "https://i.scdn.co/image/ab67706f000000035a7c1ed9c25b785cd71f4e9f", indieCoolSongs),
        PlaylistUi("r3", "Baladas 2025", "Música para el corazón", "https://i.scdn.co/image/ab67706f000000035b2a8c1e5a34137f7fa0589d", baladas2025Songs)
    )
}

/** 🔸 Mixes **/
fun sampleMixes(): List<PlaylistUi> {
    val workoutSongs = listOf(
        SongUi("s1", "Stronger", "Kanye West", "https://i.scdn.co/image/ab67616d0000b2736e9f6dc09bcb4fbbf4f7f0e7"),
        SongUi("s2", "Can’t Hold Us", "Macklemore & Ryan Lewis", "https://i.scdn.co/image/ab67616d0000b27370f7263d9b3ab785ca5bb3c2"),
        SongUi("s3", "Don’t Start Now", "Dua Lipa", "https://i.scdn.co/image/ab67616d0000b273bb3f7fd9f848b61aa317e9b2")
    )

    val fiestaSongs = listOf(
        SongUi("s4", "Despacito", "Luis Fonsi ft. Daddy Yankee", "https://i.scdn.co/image/ab67616d0000b27306a8c2a4e799a1d2abddc92b"),
        SongUi("s5", "Taki Taki", "DJ Snake ft. Selena Gomez", "https://i.scdn.co/image/ab67616d0000b273c5db8fdf4dcfb9e8d7d94a1a"),
        SongUi("s6", "Dákiti", "Bad Bunny ft. Jhay Cortez", "https://i.scdn.co/image/ab67616d0000b273caa43159f6a6a69c8e3f747e")
    )

    val loFiSongs = listOf(
        SongUi("s7", "Dreaming", "mell-ø", "https://i.scdn.co/image/ab67616d0000b27377a4b1ed0a0cb065a0b51667"),
        SongUi("s8", "Snowman", "WYS", "https://i.scdn.co/image/ab67616d0000b273b71c4e03f4cfa40fa1d89ec3"),
        SongUi("s9", "Blue Channel", "Mondo Loops", "https://i.scdn.co/image/ab67616d0000b27341a4f1a29286e916fcf77d4b")
    )

    return listOf(
        PlaylistUi("m1", "Workout Hits", "Motivación total 💪", "https://i.scdn.co/image/ab67706f000000035418b6d058bd1f93afdfdd58", workoutSongs),
        PlaylistUi("m2", "Fiesta Pop", "Ritmos para bailar 🎉", "https://i.scdn.co/image/ab67706f000000038efb0f9eb23e10e5b6239bcb", fiestaSongs),
        PlaylistUi("m3", "Lo-Fi Beats", "Relájate y estudia ☕", "https://i.scdn.co/image/ab67706f000000037d0de1b1dc653f2697b9c65f", loFiSongs)
    )
}
