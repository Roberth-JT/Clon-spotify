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

// 🎶 Lista de canciones demo actualizada
fun demoSongList(): List<SongUi> = listOf(

    // 🔹 ADELE
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
    SongUi("s20", "Take It All", "Adele", "https://media.vogue.mx/photos/6194ffc384e242c09f4a6865/2:3/w_2560%2Cc_limit/A_General_PC_SimonEmmett.jpg"),

    // 🔹 THE WEEKND
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
    SongUi("s40", "Less Than Zero", "The Weeknd", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQCwm8g7x6Rlw2raDQtZ9mlfJ_mhf8TsABTrg&s"),

    // 🔹 SHAKIRA
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