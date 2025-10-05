package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.background
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
    var albumes by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        val playlistsRef = firestore.collection("playlists")
        val mixesRef = firestore.collection("mixes")
        val recomRef = firestore.collection("recomendados")
        val albumRef = firestore.collection("albumes")

        // Actualizar Firestore con listas base
        samplePlaylists().forEach { playlistsRef.document(it.id).set(it, SetOptions.merge()) }
        sampleMixes().forEach { mixesRef.document(it.id).set(it, SetOptions.merge()) }
        sampleRecomendados().forEach { recomRef.document(it.id).set(it, SetOptions.merge()) }
        sampleAlbumes().forEach { albumRef.document(it.id).set(it, SetOptions.merge()) }

        playlists = playlistsRef.get().await().documents.mapNotNull { it.toObject(PlaylistUi::class.java) }
        mixes = mixesRef.get().await().documents.mapNotNull { it.toObject(PlaylistUi::class.java) }
        recomendados = recomRef.get().await().documents.mapNotNull { it.toObject(PlaylistUi::class.java) }
        albumes = albumRef.get().await().documents.mapNotNull { it.toObject(PlaylistUi::class.java) }

        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1DB954))
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        item {
            val chips = listOf("Todas", "Música", "Podcasts")
            var selectedChip by remember { mutableStateOf("Todas") }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(chips) { chip ->
                    val isSelected = chip == selectedChip
                    Button(
                        onClick = { selectedChip = chip },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF1DB954) else Color(0xFF2A2A2A)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(chip, color = Color.Black, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 🔹 Bloque de "Tus me gusta" con cuadrícula
            Text("Tus me gusta", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            GridPlaylists(playlists.take(4), onOpenPlaylist)

            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SectionCarousel("Recomendados", recomendados, onOpenPlaylist)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SectionCarousel("Tus mixes más escuchados", mixes, onOpenPlaylist)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SectionCarousel("Álbumes con canciones que te gustan", albumes, onOpenPlaylist)
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/* --- 🎧 ESTILO DE SPOTIFY --- */

@Composable
fun GridPlaylists(playlists: List<PlaylistUi>, onOpenPlaylist: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        playlists.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { playlist ->
                    WidePlaylistCard(
                        playlist = playlist,
                        onClick = { onOpenPlaylist(playlist.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun WidePlaylistCard(
    playlist: PlaylistUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2A2A2A))
            .clickable { onClick() }
            .height(65.dp)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = playlist.imageUrl,
            contentDescription = playlist.title,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                playlist.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                playlist.description ?: "",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SectionCarousel(title: String, items: List<PlaylistUi>, onOpenPlaylist: (String) -> Unit) {
    Column {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
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
        PlaylistUi("p1", "Mix Favoritos", "Tus canciones más escuchadas", "https://www.ukeysoft.com/wp-content/uploads/2023/03/spotify-cover-art.png", mixFavoritosSongs),
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
        SongUi("s22", "Save Your Tears", "The Weeknd", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSREKFYMQnFXjnohq8Wm7MYMS4GL8hFjr7d8w&s"),
        SongUi("s23", "Starboy", "The Weeknd ft. Daft Punk", "https://i.pinimg.com/736x/92/bf/72/92bf723e530b1a23a8a61f2bb9a455c2.jpg"),
        SongUi("s24", "Die For You", "The Weeknd", "https://media.pitchfork.com/photos/5ff49cfc341e3dc1cfe761a5/1:1/w_1000,h_1000,c_limit/The-Weeknd.jpeg"),
        SongUi("s25", "The Hills", "The Weeknd", "https://i.scdn.co/image/ab67616d00001e027fcead687e99583072cc217b"),
        SongUi("s26", "Can't Feel My Face", "The Weeknd", "https://media.gq.com.mx/photos/610492e73b54691d4e5cc00b/16:9/w_2560%2Cc_limit/weeknd-gq-cover-september-2021-10.jpg"),
        SongUi("s27", "Earned It", "The Weeknd", "https://e2.hespress.com/wp-content/uploads/2022/01/E_wooELVkAM6Sun-800x600.jpeg"),
        SongUi("s28", "I Feel It Coming", "The Weeknd ft. Daft Punk", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQCwm8g7x6Rlw2raDQtZ9mlfJ_mhf8TsABTrg&s"),
        SongUi("s29", "In The Night", "The Weeknd", "https://images.genius.com/222ab83bb6fb98b7a562fce2b5cf8173.300x300x1.jpg"),
        SongUi("s30", "Call Out My Name", "The Weeknd", "https://media.pitchfork.com/photos/5ff49cfc341e3dc1cfe761a5/1:1/w_1000,h_1000,c_limit/The-Weeknd.jpeg"),
        SongUi("s31", "Take My Breath", "The Weeknd", "https://i1.sndcdn.com/artworks-RapCTPAQ0nGTcJJV-vHI1NA-t500x500.jpg"),
        SongUi("s32", "Out of Time", "The Weeknd", "https://media.gq.com.mx/photos/610492e73b54691d4e5cc00b/16:9/w_2560%2Cc_limit/weeknd-gq-cover-september-2021-10.jpg"),
        SongUi("s33", "After Hours", "The Weeknd", "https://e2.hespress.com/wp-content/uploads/2022/01/E_wooELVkAM6Sun-800x600.jpeg"),
        SongUi("s34", "Is There Someone Else?", "The Weeknd", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQCwm8g7x6Rlw2raDQtZ9mlfJ_mhf8TsABTrg&s"),
        SongUi("s35", "Heartless", "The Weeknd", "https://i1.sndcdn.com/artworks-BTpKz5WqRtz01bq2-f1A2xw-t500x500.jpg"),
        SongUi("s36", "Reminder", "The Weeknd", "https://media.pitchfork.com/photos/5ff49cfc341e3dc1cfe761a5/1:1/w_1000,h_1000,c_limit/The-Weeknd.jpeg"),
        SongUi("s37", "Pray For Me", "The Weeknd & Kendrick Lamar", "https://i1.sndcdn.com/artworks-000522382710-rge1c6-t500x500.jpg"),
        SongUi("s38", "Save Your Tears (Remix)", "The Weeknd & Ariana Grande", "https://media.gq.com.mx/photos/610492e73b54691d4e5cc00b/16:9/w_2560%2Cc_limit/weeknd-gq-cover-september-2021-10.jpg"),
        SongUi("s39", "Sacrifice", "The Weeknd", "https://e2.hespress.com/wp-content/uploads/2022/01/E_wooELVkAM6Sun-800x600.jpeg"),
        SongUi("s40", "Less Than Zero", "The Weeknd", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQCwm8g7x6Rlw2raDQtZ9mlfJ_mhf8TsABTrg&s")
    )


    val shakiraSongs = listOf(
        SongUi("s41", "Hips Don’t Lie", "Shakira ft. Wyclef Jean", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT16xN0UpFtx9X_KMm8l2aDGbi2ZGd0PQcUIQ1x-STqKlkrkar66wXTn_FPbCfDqh3UKX2IwNMIuEVw-cT3P3CMiFiOVD1DZk757pfK3oI"),
        SongUi("s42", "Waka Waka (This Time for Africa)", "Shakira", "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcSm5G45-lF3jQn0nCjrM2tRVu6qvEkrIw7wjn5UqDtcVq1_BWIvkduXVbI4yPPMNQlHQDtBLOUz9lgLm2uLPbed4EPsCbBZoSRT8b_IgbQ"),
        SongUi("s43", "La Tortura", "Shakira ft. Alejandro Sanz", "https://media.revistagq.com/photos/6716472ea96b2923704673bf/4:3/w_3888,h_2916,c_limit/GQ_MARCAS%20DE%20AGUA-SHAKIRA2.jpg"),
        SongUi("s44", "Te Felicito", "Shakira, Rauw Alejandro", "https://depor.com/resizer/HQgjD5K6SDwjYkurI7FLcucvR00=/580x330/smart/filters:format(jpeg):quality(75)/cloudfront-us-east-1.images.arcpublishing.com/elcomercio/H42EXSOSQVCQ7HWEAIFVJNKH2Y.jpg"),
        SongUi("s45", "TQG", "Shakira, Karol G", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSWc3k8iv1Mk6VwIekrV6qoSHuvbWaNeA18eg&s"),
        SongUi("s46", "She Wolf", "Shakira", "https://media.gq.com.mx/photos/67169a3da507995d05846d85/16:9/w_2128,h_1197,c_limit/Shakira%20posando%20para%20GQ.jpg"),
        SongUi("s47", "Whenever, Wherever", "Shakira", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT16xN0UpFtx9X_KMm8l2aDGbi2ZGd0PQcUIQ1x-STqKlkrkar66wXTn_FPbCfDqh3UKX2IwNMIuEVw-cT3P3CMiFiOVD1DZk757pfK3oI"),
        SongUi("s48", "Underneath Your Clothes", "Shakira", "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcSm5G45-lF3jQn0nCjrM2tRVu6qvEkrIw7wjn5UqDtcVq1_BWIvkduXVbI4yPPMNQlHQDtBLOUz9lgLm2uLPbed4EPsCbBZoSRT8b_IgbQ"),
        SongUi("s49", "Empire", "Shakira", "https://www.billboard.com/wp-content/uploads/media/shakira-new-cover-art-650-430.jpg?w=650"),
        SongUi("s50", "Can’t Remember to Forget You", "Shakira ft. Rihanna", "https://depor.com/resizer/HQgjD5K6SDwjYkurI7FLcucvR00=/580x330/smart/filters:format(jpeg):quality(75)/cloudfront-us-east-1.images.arcpublishing.com/elcomercio/H42EXSOSQVCQ7HWEAIFVJNKH2Y.jpg"),
        SongUi("s51", "Beautiful Liar", "Beyoncé & Shakira", "https://media.revistagq.com/photos/6716472ea96b2923704673bf/4:3/w_3888,h_2916,c_limit/GQ_MARCAS%20DE%20AGUA-SHAKIRA2.jpg"),
        SongUi("s52", "Chantaje", "Shakira ft. Maluma", "https://media.gq.com.mx/photos/67169a3da507995d05846d85/16:9/w_2128,h_1197,c_limit/Shakira%20posando%20para%20GQ.jpg"),
        SongUi("s53", "Me Enamoré", "Shakira", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT16xN0UpFtx9X_KMm8l2aDGbi2ZGd0PQcUIQ1x-STqKlkrkar66wXTn_FPbCfDqh3UKX2IwNMIuEVw-cT3P3CMiFiOVD1DZk757pfK3oI"),
        SongUi("s54", "Monotonía", "Shakira, Ozuna", "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcSm5G45-lF3jQn0nCjrM2tRVu6qvEkrIw7wjn5UqDtcVq1_BWIvkduXVbI4yPPMNQlHQDtBLOUz9lgLm2uLPbed4EPsCbBZoSRT8b_IgbQ"),
        SongUi("s55", "Loca", "Shakira ft. El Cata", "https://elfarandi.com/wp-content/uploads/2018/04/Shakira-Ft.-El-Cata-Loca-HD.mp4_snapshot_01.34_2010.10.17_16.08.54.jpg"),
        SongUi("s56", "Las de la Intuición", "Shakira", "https://depor.com/resizer/HQgjD5K6SDwjYkurI7FLcucvR00=/580x330/smart/filters:format(jpeg):quality(75)/cloudfront-us-east-1.images.arcpublishing.com/elcomercio/H42EXSOSQVCQ7HWEAIFVJNKH2Y.jpg"),
        SongUi("s57", "Ciega, Sordomuda", "Shakira", "https://media.revistagq.com/photos/6716472ea96b2923704673bf/4:3/w_3888,h_2916,c_limit/GQ_MARCAS%20DE%20AGUA-SHAKIRA2.jpg"),
        SongUi("s58", "Ojos Así", "Shakira", "https://media.gq.com.mx/photos/67169a3da507995d05846d85/16:9/w_2128,h_1197,c_limit/Shakira%20posando%20para%20GQ.jpg"),
        SongUi("s59", "Sale El Sol", "Shakira", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT16xN0UpFtx9X_KMm8l2aDGbi2ZGd0PQcUIQ1x-STqKlkrkar66wXTn_FPbCfDqh3UKX2IwNMIuEVw-cT3P3CMiFiOVD1DZk757pfK3oI"),
        SongUi("s60", "La Bicicleta", "Shakira, Carlos Vives", "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcSm5G45-lF3jQn0nCjrM2tRVu6qvEkrIw7wjn5UqDtcVq1_BWIvkduXVbI4yPPMNQlHQDtBLOUz9lgLm2uLPbed4EPsCbBZoSRT8b_IgbQ")
    )


    return listOf(
        PlaylistUi("r1", "Adele", "Adele-Exitos", "https://cdn-images.dzcdn.net/images/cover/8bdaf37e2e7f883e84bbc3462c938293/0x1900-000000-80-0-0.jpg", adeleSongs),
        PlaylistUi("r2", "The Weeknd", "Favorite hits", "https://e2.hespress.com/wp-content/uploads/2022/01/E_wooELVkAM6Sun-800x600.jpeg", weekndSongs),
        PlaylistUi("r3", "Shakira", "Las más escuchadas", "https://i.scdn.co/image/ab6761610000e5eb2528c726e5ddb90a7197e527", shakiraSongs)
    )
}

/** 🔸 Mixes **/
fun sampleMixes(): List<PlaylistUi> {
    val moratSongs = listOf(
        SongUi("s1", "Besos en Guerra", "Morat ft. Juanes", "https://www.agendapop.cl/wp-content/uploads/2018/01/Morat-2.jpg"),
        SongUi("s2", "Cómo Te Atreves", "Morat", "https://i.ytimg.com/vi/_gm5piKnrS4/maxresdefault.jpg"),
        SongUi("s3", "Cuando Nadie Ve", "Morat", "https://i.ytimg.com/vi/szeA9tvItJY/maxresdefault.jpg"),
        SongUi("s4", "La Bella y la Bestia", "Morat & Reik", "https://cdn.milenio.com/uploads/media/2020/06/11/reik-morat-unen-cancion-bella.jpg"),
        SongUi("s5", "Amor Con Hielo", "Morat", "https://i.ytimg.com/vi/1P5eDa_Kn2M/maxresdefault.jpg"),
        SongUi("s6", "No Termino", "Morat", "https://i.scdn.co/image/ab67616d0000b27351358468b00831d9eb74ac51"),
        SongUi("s7", "Porfa No Te Vayas", "Morat & Beret", "https://i.scdn.co/image/ab67616d0000b2734cac4c4431908529b744ec9b"),
        SongUi("s8", "Sobre El Amor y Sus Efectos Secundarios", "Morat", "https://i.scdn.co/image/ab67616d0000b2739eb71b7fe146a474ff2b607e"),
        SongUi("s9", "No Se Va", "Morat", "https://i.ytimg.com/vi/xp82mniDmmg/maxresdefault.jpg"),
        SongUi("s10", "Llamada Perdida", "Morat", "https://i.ytimg.com/vi/4IzYE_e5POg/maxresdefault.jpg")
    )


    val brunoSongs = listOf(
        SongUi("s11", "Uptown Funk", "Mark Ronson ft. Bruno Mars", "https://i.ytimg.com/vi/OPf0YbXqDm0/maxresdefault.jpg"),
        SongUi("s12", "24K Magic", "Bruno Mars", "https://i.scdn.co/image/ab67616d0000b273232711f7d66a1e19e89e28c5"),
        SongUi("s13", "Treasure", "Bruno Mars", "https://i1.sndcdn.com/artworks-cnH0XFWI67ZFBZuU-Zob8WA-t500x500.jpg"),
        SongUi("s14", "Locked Out of Heaven", "Bruno Mars", "https://i1.sndcdn.com/artworks-000042824893-xcujcq-t1080x1080.jpg"),
        SongUi("s15", "Just the Way You Are", "Bruno Mars", "https://i.ytimg.com/vi/LjhCEhWiKXk/maxresdefault.jpg"),
        SongUi("s16", "That’s What I Like", "Bruno Mars", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQRrOfft-OAHxbV-s4xo1DGJoVdmw0RNBmmDA&s"),
        SongUi("s17", "Finesse (Remix)", "Bruno Mars ft. Cardi B", "https://i1.sndcdn.com/artworks-000286025327-f78pyo-t500x500.jpg"),
        SongUi("s18", "Leave The Door Open", "Bruno Mars, Anderson .Paak, Silk Sonic", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTMWS6nkA4uACWx2bc0qVtmAMv6Q9QA3sjp3Q&s"),
        SongUi("s19", "Versace on the Floor", "Bruno Mars", "https://images.genius.com/0b9ae95ddf929d84d51544fd1ba9a1a5.1000x1000x1.png"),
        SongUi("s20", "Grenade", "Bruno Mars", "https://images.genius.com/9d49d79aa4e35f36e2b751d149f9132b.425x425x1.png")
    )


    val edSongs = listOf(
        SongUi("s21", "Shape of You", "Ed Sheeran", "https://i.ytimg.com/vi/JGwWNGJdvx8/sddefault.jpg"),
        SongUi("s22", "Perfect", "Ed Sheeran", "https://i.ytimg.com/vi/cNGjD0VG4R8/sddefault.jpg"),
        SongUi("s23", "Photograph", "Ed Sheeran", "https://i.scdn.co/image/ab67616d0000b273407981084d79d283e24d428e"),
        SongUi("s24", "Thinking Out Loud", "Ed Sheeran", "https://i.ytimg.com/vi/XMPgVZtADtQ/maxresdefault.jpg"),
        SongUi("s25", "Bad Habits", "Ed Sheeran", "https://i.ytimg.com/vi/1KZaWYdltHc/hq720.jpg?sqp=-oaymwEhCK4FEIIDSFryq4qpAxMIARUAAAAAGAElAADIQj0AgKJD&rs=AOn4CLAolt_lU6_1OwhpbzMlKwMtZu4ODA"),
        SongUi("s26", "Eyes Closed", "Ed Sheeran", "https://akamai.sscdn.co/uploadfile/letras/fotos/5/d/4/5/5d45f3920ea283e2bad27b626d20d9e2.jpg"),
        SongUi("s27", "Shivers", "Ed Sheeran", "https://i1.sndcdn.com/artworks-rVSO2PnxKETyFEyL-IwFgXQ-t500x500.jpg"),
        SongUi("s28", "Castle on the Hill", "Ed Sheeran", "https://i.ytimg.com/vi/K0ibBPhiaG0/sddefault.jpg"),
        SongUi("s29", "Overpass Graffiti", "Ed Sheeran", "https://i.ytimg.com/vi/0qTQR92UuUA/maxresdefault.jpg"),
        SongUi("s30", "Lego House", "Ed Sheeran", "https://i.ytimg.com/vi/c4BLVznuWnU/hq720.jpg?sqp=-oaymwEhCK4FEIIDSFryq4qpAxMIARUAAAAAGAElAADIQj0AgKJD&rs=AOn4CLBwUPAmIFr5QCHI8YjVxIRePe__FA")
    )

    val luisMiguelSongs = listOf(
        SongUi("s31", "La Incondicional", "Luis Miguel", "https://i.ytimg.com/vi/wOjzo02Tmck/hq720.jpg?sqp=-oaymwEhCK4FEIIDSFryq4qpAxMIARUAAAAAGAElAADIQj0AgKJD&rs=AOn4CLCjimJjGE7FfMmJDqlZzf7Sk6P0Rg"),
        SongUi("s32", "Ahora Te Puedes Marchar", "Luis Miguel", "https://i.scdn.co/image/ab67616d0000b2736d2d141c6f14e161ca551971"),
        SongUi("s33", "Culpable o No (Miénteme Como Siempre)", "Luis Miguel", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTC95Eur_brvcwu2gxFQ1n7a0C7LcAbytrkAg&s"),
        SongUi("s34", "Tengo Todo Excepto a Ti", "Luis Miguel", "https://i.scdn.co/image/ab67616d0000b273f2237e4348c77d9ccd1ebd3a"),
        SongUi("s35", "Entrégate", "Luis Miguel", "https://i.ytimg.com/vi/kogZZsZzG-E/maxresdefault.jpg"),
        SongUi("s36", "Suave", "Luis Miguel", "https://i.ytimg.com/vi/3IRBm9DjBdQ/hq720.jpg?sqp=-oaymwEhCK4FEIIDSFryq4qpAxMIARUAAAAAGAElAADIQj0AgKJD&rs=AOn4CLBmp7NnETP1heqyq6-qYXFXrWuIQQ"),
        SongUi("s37", "Por Debajo de la Mesa", "Luis Miguel", "https://i.ytimg.com/vi/giAE7Yz7gHI/hq720.jpg?sqp=-oaymwEhCK4FEIIDSFryq4qpAxMIARUAAAAAGAElAADIQj0AgKJD&rs=AOn4CLD9NkRpIrR8FRxWiT6Shs5w54zmYg"),
        SongUi("s38", "Fría Como el Viento", "Luis Miguel", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRLx4J8fujqIB_ZcesEh2m-RppPj3RbN0YteA&s"),
        SongUi("s39", "Cuando Calienta el Sol", "Luis Miguel", "https://i.scdn.co/image/ab67616d00001e027144526743136029a4d61aca"),
        SongUi("s40", "La Bikina", "Luis Miguel", "https://i.ytimg.com/vi/NCvJwzDQTBM/hqdefault.jpg")
    )
    val pedroSongs = listOf(
        SongUi("s41", "Cuando Pienses en Volver", "Pedro Suárez Vértiz", "https://i.ytimg.com/vi/fOBdcuX1FyQ/maxresdefault.jpg"),
        SongUi("s42", "Los Globos del Cielo", "Pedro Suárez Vértiz", "https://i.ytimg.com/vi/IGmyLM1OUnE/maxresdefault.jpg"),
        SongUi("s43", "Me Estoy Enamorando", "Pedro Suárez Vértiz", "https://i.ytimg.com/vi/fB01FVQ5BMU/maxresdefault.jpg"),
        SongUi("s44", "No Pensé que Era Amor", "Pedro Suárez Vértiz", "https://i.ytimg.com/vi/B5x_DUCZOso/maxresdefault.jpg"),
        SongUi("s45", "Degeneración Actual", "Pedro Suárez Vértiz", "https://i.ytimg.com/vi/Nd7pt3ktNFI/maxresdefault.jpg"),
        SongUi("s46", "Cuéntame", "Pedro Suárez Vértiz", "https://i1.sndcdn.com/artworks-000027806645-wly1t8-t500x500.jpg"),
        SongUi("s47", "Globo de Gas", "Pedro Suárez Vértiz", "https://i.ytimg.com/vi/eMAT524yBw4/hq720.jpg?sqp=-oaymwE7CK4FEIIDSFryq4qpAy0IARUAAAAAGAElAADIQj0AgKJD8AEB-AH-CYAC0AWKAgwIABABGEIgWShyMA8=&rs=AOn4CLAKi1hn6WN2TTHI4FCpvJG6mxIuzw"),
        SongUi("s48", "Lo Olvidé", "Pedro Suárez Vértiz", "https://i1.sndcdn.com/artworks-000203133120-hqwxho-t500x500.jpg"),
        SongUi("s49", "Mi Auto Era una Rana", "Pedro Suárez Vértiz", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRFAO8X4JqBXnFXTg_QYNbUb_yOJK5FD9xzgg&s"),
        SongUi("s50", "Póntelo en la lengua", "Pedro Suárez Vértiz", "https://i.scdn.co/image/ab67616d0000b27359dc0cdec0ebc5fde5d73fcf")
    )


    return listOf(
        PlaylistUi("m1", "Lo Mejor de Morat", "Pop y emociones que llegan al corazón 💙", "https://image-cdn-ak.spotifycdn.com/image/ab67706c0000da84cf4599e4d4bf377a0aef5ab8", moratSongs),
        PlaylistUi("m2", "Bruno Mars Hits", "Ritmo, funk y pura energía 🎶", "https://i.scdn.co/image/ab6761610000e5ebc36dd9eb55fb0db4911f25dd", brunoSongs),
        PlaylistUi("m3", "Ed Sheeran Essentials", "Relájate con las mejores baladas acústicas ☕", "https://cdn.unitycms.io/images/EdB8-yRQardB00_YpJVj43.jpg", edSongs),
        PlaylistUi("m4", "Románticos Eternos", "Baladas para el corazón ❤️", "https://mosaic.scdn.co/640/ab67616d00001e027144526743136029a4d61acaab67616d00001e02780268564c65ca302786e6ffab67616d00001e02bda5c1e56bf06c3c7fc173f7ab67616d00001e02f2237e4348c77d9ccd1ebd3a", luisMiguelSongs),
        PlaylistUi("m5", "Rock Peruano Legendario", "Pedro Suárez Vértiz y clásicos nacionales 🎸", "https://radiomaranon.org.pe/wp-content/uploads/2023/12/pedrito.png", pedroSongs)
    )

}

/** 🔹 Álbumes con canciones **/
fun sampleAlbumes(): List<PlaylistUi> {
    val marcoAlbum = listOf(
        SongUi("s1", "Si No Te Hubieras Ido", "Marco Antonio Solís", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRALCk7BfwNGiy8BsrP9_sP2k1EBreq4oLLGg&s"),
        SongUi("s2", "Tu Cárcel", "Marco Antonio Solís", "https://i.ytimg.com/vi/uq5utpNtbNM/maxresdefault.jpg"),
        SongUi("s3", "Más Que Tu Amigo", "Marco Antonio Solís", "https://i.ytimg.com/vi/sZ9j9ngOLnQ/maxresdefault.jpg"),
        SongUi("s4", "Dónde Estará Mi Primavera", "Marco Antonio Solís", "https://www.filmaffinity.com/es/film794732.html"),
        SongUi("s5", "A Dónde Vamos a Parar", "Marco Antonio Solís", "https://i.ytimg.com/vi/Vho48iVcMas/maxresdefault.jpg"),
        SongUi("s6", "El Perdedor", "Marco Antonio Solís", "https://i.ytimg.com/vi/CjC2NYfhGWs/hqdefault.jpg"),
        SongUi("s7", "Mi Eterno Amor Secreto", "Marco Antonio Solís", "https://i.ytimg.com/vi/JZEl-p3FBbA/maxresdefault.jpg"),
        SongUi("s8", "Vivir Sin Ti", "Marco Antonio Solís", "https://i.ytimg.com/vi/58MA2kHcWqA/hq720.jpg?sqp=-oaymwEhCK4FEIIDSFryq4qpAxMIARUAAAAAGAElAADIQj0AgKJD&rs=AOn4CLDiIQmCLhl77YMgxO5P0tIVArdOyg"),
        SongUi("s9", "Te Amo Mamá", "Marco Antonio Solís", "https://i.ytimg.com/vi/STQtVaAO3pQ/maxresdefault.jpg"),
        SongUi("s10", "Antes de Que Te Vayas", "Marco Antonio Solís", "https://i.ytimg.com/vi/ePNdHrqG3d8/maxresdefault.jpg")
    )

    val jjAlbum = listOf(
        SongUi("s11", "¡Corre!", "Jesse & Joy", "https://i.ytimg.com/vi/2LYU0nmdrik/mqdefault.jpg"),
        SongUi("s12", "Ecos de Amor", "Jesse & Joy", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSOm3AC3_31zPuXFvLVFGgSuhvF-OtBKDNPXw&s"),
        SongUi("s13", "Dueles", "Jesse & Joy", "https://www.lahiguera.net/musicalia/artistas/jesse_&_joy/disco/7161/tema/13066/jesse_&_joy_dueles-portada.jpg"),
        SongUi("s14", "Espacio Sideral", "Jesse & Joy", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQC_J8ka5N_n-2J_XiwYrBhGvah8zIW9tr9Zw&s"),
        SongUi("s15", "Me Soltaste", "Jesse & Joy", "https://cdn-images.dzcdn.net/images/cover/434dfd13f8118b6c60f289e909efeeaa/1900x1900-000000-80-0-0.jpg"),
        SongUi("s16", "La de la Mala Suerte", "Jesse & Joy", "https://cdn-images.dzcdn.net/images/cover/74f5396edc03d5262ecb7b7a2958a7b9/0x1900-000000-80-0-0.jpg"),
        SongUi("s17", "¿Con Quién Se Queda el Perro?", "Jesse & Joy", "https://images.genius.com/d6583b05c670a0c4ea3dd946eb38da89.1000x1000x1.jpg"),
        SongUi("s18", "Chocolate", "Jesse & Joy", "https://cdn-images.dzcdn.net/images/cover/b3b0b56864444fe2a9dc4ac4d83fa55c/0x1900-000000-80-0-0.jpg"),
        SongUi("s19", "3 A.M. (feat. Gente de Zona)", "Jesse & Joy, Gente de Zona", "https://cdn-images.dzcdn.net/images/cover/3b0b90aac81fa6ba7e605deba507e51d/0x1900-000000-80-0-0.jpg"),
        SongUi("s20", "Llorar (feat. Mario Domm)", "Jesse & Joy, Mario Domm", "https://cdn-images.dzcdn.net/images/cover/19eab344cf9574ee0f7b56ea34f59682/1900x1900-000000-81-0-0.jpg")
    )

    val mileyAlbum = listOf(
        SongUi("s21", "Flowers", "Miley Cyrus", "https://i1.sndcdn.com/artworks-YOSTbh90ESawTlzu-s9fROg-t500x500.jpg"),
        SongUi("s22", "Wrecking Ball", "Miley Cyrus", "https://upload.wikimedia.org/wikipedia/en/0/06/Miley_Cyrus_-_Wrecking_Ball.jpg"),
        SongUi("s23", "Midnight Sky", "Miley Cyrus", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTKrn5hIen34o_V2r4I3Xqp7p2lpM88miV23g&s"),
        SongUi("s24", "Party in the U.S.A.", "Miley Cyrus", "https://magicallightshows.com/cdn/shop/products/Miley.jpg?v=1592700666"),
        SongUi("s25", "The Climb", "Miley Cyrus", "https://upload.wikimedia.org/wikipedia/en/1/1c/Miley_Cyrus_-_The_Climb.png"),
        SongUi("s26", "Angels Like You", "Miley Cyrus", "https://i.scdn.co/image/ab67616d0000b2738cffb7c6c40759eaf8a5a142"),
        SongUi("s27", "Malibu", "Miley Cyrus", "https://upload.wikimedia.org/wikipedia/en/e/e5/Malibu_%28Official_Single_Cover%29_by_Miley_Cyrus.png"),
        SongUi("s28", "Adore You", "Miley Cyrus", "https://m.soundcloud.com/jordaninstrumental/adore-you-miley-cyrus"),
        SongUi("s29", "We Can’t Stop", "Miley Cyrus", "https://i.ytimg.com/vi/LrUvu1mlWco/maxresdefault.jpg"),
        SongUi("s30", "Used To Be Young", "Miley Cyrus", "https://www.imdb.com/es/title/tt28768948/")
    )

    return listOf(
        PlaylistUi("a1", "Marco Antonio Solis", "Romance, nostalgia, pasión", "https://iscale.iheart.com/catalog/artist/39469", marcoAlbum),
        PlaylistUi("a2", "Jesse & Joy", "Amor, armonía, ternura", "https://i.scdn.co/image/ab67616d0000b2731f6379010c486d0658e644f5", jjAlbum),
        PlaylistUi("a3", "Miley Cyrus", "Fuerza, libertad, rebeldía", "https://www.hollywoodreporter.com/wp-content/uploads/2025/10/GettyImages-1472464614-e1759444822734.jpg?w=2000&h=1126&crop=1&resize=1440%2C810", mileyAlbum)
    )
}

