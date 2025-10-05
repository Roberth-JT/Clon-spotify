package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

        // 🔸 Crear o actualizar las colecciones si no existen
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

    // ✅ Scroll vertical principal
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        item {
            // 🔹 Chips superiores
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

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(playlists.take(6)) { p ->
                    SmallPlaylistCard(p) { onOpenPlaylist(p.id) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 🔹 Carruseles
        item {
            SectionCarousel("Recomendados", recomendados, onOpenPlaylist)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SectionCarousel("Viernes de lanzamientos", mixes, onOpenPlaylist)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SectionCarousel("Fiesta", mixes, onOpenPlaylist)
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// 🔸 Tarjeta pequeña
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
        Text(playlist.title, color = Color.White, maxLines = 1, fontWeight = FontWeight.Medium)
    }
}

// 🔹 Carrusel de playlists
@Composable
fun SectionCarousel(title: String, items: List<PlaylistUi>, onOpenPlaylist: (String) -> Unit) {
    Column {
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
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
    val adeleSongs = listOf(
        SongUi("s1", "Hello", "Adele", "https://img2.rtve.es/a/5760059/?h=300"),
        SongUi("s2", "Someone Like You", "Adele", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcReGAWa2LtEOvbGwwNb2GGL93VJklHmXR6chQ&s"),
        SongUi("s3", "Rolling in the Deep", "Adele", "https://cdn-images.dzcdn.net/images/cover/8bdaf37e2e7f883e84bbc3462c938293/0x1900-000000-80-0-0.jpg"),
        SongUi("s4", "Set Fire to the Rain", "Adele", "https://i.pinimg.com/736x/f2/b1/62/f2b16270f1f063d2e26bcaedd0e2b352.jpg"),
        SongUi("s5", "Easy On Me", "Adele", "https://media.vogue.mx/photos/6194ffc384e242c09f4a6865/2:3/w_2560%2Cc_limit/A_General_PC_SimonEmmett.jpg"),
        SongUi("s6", "When We Were Young", "Adele", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcReGAWa2LtEOvbGwwNb2GGL93VJklHmXR6chQ&s"),
        SongUi("s7", "Send My Love (To Your New Lover)", "Adele", "https://ichef.bbci.co.uk/ace/ws/640/cpsprodpb/d4d7/live/5aa3f190-4454-11ef-96a8-e710c6bfc866.jpg.webp"),
        SongUi("s8", "Skyfall", "Adele", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcReGAWa2LtEOvbGwwNb2GGL93VJklHmXR6chQ&s"),
        SongUi("s9", "Chasing Pavements", "Adele", "https://i.pinimg.com/736x/f2/b1/62/f2b16270f1f063d2e26bcaedd0e2b352.jpg"),
        SongUi("s10", "Make You Feel My Love", "Adele", "https://media.vogue.mx/photos/6194ffc384e242c09f4a6865/2:3/w_2560%2Cc_limit/A_General_PC_SimonEmmett.jpg"),
        SongUi("s11", "Oh My God", "Adele", "https://img2.rtve.es/a/5760059/?h=300"),
        SongUi("s12", "Love in the Dark", "Adele", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcReGAWa2LtEOvbGwwNb2GGL93VJklHmXR6chQ&s"),
        SongUi("s13", "Turning Tables", "Adele", "https://ichef.bbci.co.uk/ace/ws/640/cpsprodpb/d4d7/live/5aa3f190-4454-11ef-96a8-e710c6bfc866.jpg.webp"),
        SongUi("s14", "Water Under the Bridge", "Adele", "https://media.vogue.mx/photos/6194ffc384e242c09f4a6865/2:3/w_2560%2Cc_limit/A_General_PC_SimonEmmett.jpg"),
        SongUi("s15", "All I Ask", "Adele", "https://wallpapers.com/images/featured/adele-hmuxzhg7ivf7jm2f.jpg"),
        SongUi("s16", "I Drink Wine", "Adele", "https://ichef.bbci.co.uk/ace/ws/640/cpsprodpb/d4d7/live/5aa3f190-4454-11ef-96a8-e710c6bfc866.jpg.webp"),
        SongUi("s17", "To Be Loved", "Adele", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcReGAWa2LtEOvbGwwNb2GGL93VJklHmXR6chQ&s"),
        SongUi("s18", "My Little Love", "Adele", "https://img2.rtve.es/a/5760059/?h=300"),
        SongUi("s19", "Don’t You Remember", "Adele", "https://wallpapers.com/images/featured/adele-hmuxzhg7ivf7jm2f.jpg"),
        SongUi("s20", "Take It All", "Adele", "https://media.vogue.mx/photos/6194ffc384e242c09f4a6865/2:3/w_2560%2Cc_limit/A_General_PC_SimonEmmett.jpg")
    )



    val weekndSongs = listOf(
        SongUi("s21", "Blinding Lights", "The Weeknd", "https://e2.hespress.com/wp-content/uploads/2022/01/E_wooELVkAM6Sun-800x600.jpeg"),
        SongUi("s22", "Save Your Tears", "The Weeknd", "https://upload.wikimedia.org/wikipedia/commons/9/95/The_Weeknd_Cannes_2023.png"),
        SongUi("s23", "Starboy", "The Weeknd ft. Daft Punk", "https://upload.wikimedia.org/wikipedia/commons/9/95/The_Weeknd_Cannes_2023.png"),
        SongUi("s24", "Die For You", "The Weeknd", "https://media.pitchfork.com/photos/5ff49cfc341e3dc1cfe761a5/1:1/w_1000,h_1000,c_limit/The-Weeknd.jpeg"),
        SongUi("s25", "The Hills", "The Weeknd", "https://upload.wikimedia.org/wikipedia/commons/9/95/The_Weeknd_Cannes_2023.png"),
        SongUi("s26", "Can't Feel My Face", "The Weeknd", "https://media.gq.com.mx/photos/610492e73b54691d4e5cc00b/16:9/w_2560%2Cc_limit/weeknd-gq-cover-september-2021-10.jpg"),
        SongUi("s27", "Earned It", "The Weeknd", "https://e2.hespress.com/wp-content/uploads/2022/01/E_wooELVkAM6Sun-800x600.jpeg"),
        SongUi("s28", "I Feel It Coming", "The Weeknd ft. Daft Punk", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQCwm8g7x6Rlw2raDQtZ9mlfJ_mhf8TsABTrg&s"),
        SongUi("s29", "In The Night", "The Weeknd", "https://upload.wikimedia.org/wikipedia/commons/9/95/The_Weeknd_Cannes_2023.png"),
        SongUi("s30", "Call Out My Name", "The Weeknd", "https://media.pitchfork.com/photos/5ff49cfc341e3dc1cfe761a5/1:1/w_1000,h_1000,c_limit/The-Weeknd.jpeg"),
        SongUi("s31", "Take My Breath", "The Weeknd", "https://upload.wikimedia.org/wikipedia/en/c/c1/The_Weeknd_-_After_Hours.png"),
        SongUi("s32", "Out of Time", "The Weeknd", "https://media.gq.com.mx/photos/610492e73b54691d4e5cc00b/16:9/w_2560%2Cc_limit/weeknd-gq-cover-september-2021-10.jpg"),
        SongUi("s33", "After Hours", "The Weeknd", "https://e2.hespress.com/wp-content/uploads/2022/01/E_wooELVkAM6Sun-800x600.jpeg"),
        SongUi("s34", "Is There Someone Else?", "The Weeknd", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQCwm8g7x6Rlw2raDQtZ9mlfJ_mhf8TsABTrg&s"),
        SongUi("s35", "Heartless", "The Weeknd", "https://upload.wikimedia.org/wikipedia/commons/9/95/The_Weeknd_Cannes_2023.png"),
        SongUi("s36", "Reminder", "The Weeknd", "https://media.pitchfork.com/photos/5ff49cfc341e3dc1cfe761a5/1:1/w_1000,h_1000,c_limit/The-Weeknd.jpeg"),
        SongUi("s37", "Pray For Me", "The Weeknd & Kendrick Lamar", "https://upload.wikimedia.org/wikipedia/en/c/c1/The_Weeknd_-_After_Hours.png"),
        SongUi("s38", "Save Your Tears (Remix)", "The Weeknd & Ariana Grande", "https://media.gq.com.mx/photos/610492e73b54691d4e5cc00b/16:9/w_2560%2Cc_limit/weeknd-gq-cover-september-2021-10.jpg"),
        SongUi("s39", "Sacrifice", "The Weeknd", "https://e2.hespress.com/wp-content/uploads/2022/01/E_wooELVkAM6Sun-800x600.jpeg"),
        SongUi("s40", "Less Than Zero", "The Weeknd", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQCwm8g7x6Rlw2raDQtZ9mlfJ_mhf8TsABTrg&s")
    )




    val shakiraSongs = listOf(
        SongUi("s41", "Hips Don’t Lie", "Shakira ft. Wyclef Jean", "https://i.scdn.co/image/ab67616d0000b2733c8e6a86b9a28b8f9b22e4f5"),
        SongUi("s42", "Waka Waka (This Time for Africa)", "Shakira", "https://i.scdn.co/image/ab67616d0000b273a8a8b5cc4a6d1f9dfbb5a4d2"),
        SongUi("s43", "La Tortura", "Shakira ft. Alejandro Sanz", "https://i.scdn.co/image/ab67616d0000b27345d4143c9b4323b9c65b0f1a"),
        SongUi("s44", "Te Felicito", "Shakira, Rauw Alejandro", "https://i.scdn.co/image/ab67616d0000b2733f7657a8fce0b8a7b57b8f6a"),
        SongUi("s45", "TQG", "Shakira, Karol G", "https://i.scdn.co/image/ab67616d0000b273c79a75aee8459a49d0aaffd5"),
        SongUi("s46", "She Wolf", "Shakira", "https://i.scdn.co/image/ab67616d0000b273a47b3c6edba1a950db19d1d2"),
        SongUi("s47", "Whenever, Wherever", "Shakira", "https://i.scdn.co/image/ab67616d0000b273abdd5f5d7b9b98b9cb4cdb05"),
        SongUi("s48", "Underneath Your Clothes", "Shakira", "https://i.scdn.co/image/ab67616d0000b27388c84a1b0a6c9045b1f7324f"),
        SongUi("s49", "Empire", "Shakira", "https://i.scdn.co/image/ab67616d0000b273f65002a4ce4c2dc8c9c3f5f2"),
        SongUi("s50", "Can’t Remember to Forget You", "Shakira ft. Rihanna", "https://i.scdn.co/image/ab67616d0000b273bfa6a2271cb85f10f3b1a2c1"),
        SongUi("s51", "Beautiful Liar", "Beyoncé & Shakira", "https://i.scdn.co/image/ab67616d0000b2732f8ef16b902d2e9180c1e9a3"),
        SongUi("s52", "Chantaje", "Shakira ft. Maluma", "https://i.scdn.co/image/ab67616d0000b273b77b2a08c3e2cf8d55f9ef9d"),
        SongUi("s53", "Me Enamoré", "Shakira", "https://i.scdn.co/image/ab67616d0000b2738c776d9c0f8e4d47b7a98d7f"),
        SongUi("s54", "Monotonía", "Shakira, Ozuna", "https://i.scdn.co/image/ab67616d0000b273a8b4c6b4c785dd2a58a0a51e"),
        SongUi("s55", "Loca", "Shakira ft. El Cata", "https://i.scdn.co/image/ab67616d0000b27387f882d4a4a679c41e216cc8"),
        SongUi("s56", "Las de la Intuición", "Shakira", "https://i.scdn.co/image/ab67616d0000b273a5c86a5f56e41e2b7a3e1c47"),
        SongUi("s57", "Ciega, Sordomuda", "Shakira", "https://i.scdn.co/image/ab67616d0000b273db4b013c17b5d6c203b8c36f"),
        SongUi("s58", "Ojos Así", "Shakira", "https://i.scdn.co/image/ab67616d0000b273e2f5f1a7a0f841a1f1e5a18a"),
        SongUi("s59", "Sale El Sol", "Shakira", "https://i.scdn.co/image/ab67616d0000b273c7bce6b2e048227fa3a3b4af"),
        SongUi("s60", "La Bicicleta", "Shakira, Carlos Vives", "https://i.scdn.co/image/ab67616d0000b273b50e967ac4b3b92f52e5790a")
    )

    return listOf(
        PlaylistUi("r1", "Adele", "Adele-Exitos", "https://cdn-images.dzcdn.net/images/cover/8bdaf37e2e7f883e84bbc3462c938293/0x1900-000000-80-0-0.jpg", adeleSongs),
        PlaylistUi("r2", "The Weeknd", "Favorite hits", "https://e2.hespress.com/wp-content/uploads/2022/01/E_wooELVkAM6Sun-800x600.jpeg", weekndSongs),
        PlaylistUi("r3", "Shakira", "Las más escuchadas", "https://i.scdn.co/image/ab67706f000000035b2a8c1e5a34137f7fa0589d", shakiraSongs)
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
