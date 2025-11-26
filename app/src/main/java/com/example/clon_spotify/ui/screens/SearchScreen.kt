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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.clon_spotify.models.SongUi
import com.example.clon_spotify.player.PlayerViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(playerViewModel: PlayerViewModel) {
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var songs by remember { mutableStateOf<List<SongUi>>(emptyList()) }
    var filteredSongs by remember { mutableStateOf<List<SongUi>>(emptyList()) }
    var selectedSong by remember { mutableStateOf<SongUi?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar canciones demo a Firestore si no existen
    LaunchedEffect(Unit) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            try {
                val userSongsCollection = firestore
                    .collection("usuarios")
                    .document(userId)
                    .collection("songs")

                val snapshot = userSongsCollection.get().await()

                if (snapshot.isEmpty) {
                    val demoSongs = demoSongList()
                    demoSongs.forEach { song ->
                        userSongsCollection.document(song.id).set(song).await()
                    }
                    songs = demoSongs
                } else {
                    songs = snapshot.documents.mapNotNull { it.toObject(SongUi::class.java) }
                }

                filteredSongs = songs

                // CORRECCIÓN: Usar setPlaylist en lugar de acceder directamente a trackList
                playerViewModel.setPlaylist(songs)

            } catch (e: Exception) {
                Toast.makeText(context, "Error cargando canciones: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }

        } else {
            isLoading = false
            Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFF0B0B0B))
    ) {
        //  Barra de búsqueda
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
                    Text("No se encontraron resultados ", color = Color.Gray)
                }
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredSongs) { song ->
                        SongItem(song = song,
                            onAddToPlaylist = { selectedSong = song },
                            onPlayClick = {
                                // CORRECCIÓN: Usar playSongInPlaylist en lugar de playSong
                                playerViewModel.playSongInPlaylist(song, songs, context)
                                Toast.makeText(context, "Reproduciendo: ${song.title}", Toast.LENGTH_SHORT).show()
                            } )
                    }
                }
            }
        }

        if (selectedSong != null) {
            AddToPlaylistDialog(
                song = selectedSong!!,
                onDismiss = { selectedSong = null }
            )
        }
    }
}

@Composable
fun SongItem(song: SongUi, onAddToPlaylist: () -> Unit, onPlayClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayClick() }
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

fun demoSongList(): List<SongUi> = listOf(
    SongUi(
        id = "s1",
        title = "Someone Like You",
        artist = "Adele",
        imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcReGAWa2LtEOvbGwwNb2GGL93VJklHmXR6chQ&s",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/Someonelikeyou.mp3"
    ),
    SongUi(
        id = "s2",
        title = "Blinding Lights",
        artist = "The Weeknd",
        imageUrl = "https://media.gq.com.mx/photos/610492e73b54691d4e5cc00b/16:9/w_2560%2Cc_limit/weeknd-gq-cover-september-2021-10.jpg",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/BlindingLights.mp3"
    ),
    SongUi(
        id = "s3",
        title = "24K Magic",
        artist = "Bruno Mars",
        imageUrl = "https://i.scdn.co/image/ab67616d0000b273232711f7d66a1e19e89e28c5",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/24kMagic.mp3"
    ),
    SongUi(
        id = "s4",
        title = "Turning Tables",
        artist = "Adele",
        imageUrl = "https://ichef.bbci.co.uk/ace/ws/640/cpsprodpb/d4d7/live/5aa3f190-4454-11ef-96a8-e710c6bfc866.jpg.webp",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/TurningTables.mp3"
    ),
    SongUi(
        id = "s5",
        title = "The Hills",
        artist = "The Weeknd",
        imageUrl = "https://i.scdn.co/image/ab67616d00001e027fcead687e99583072cc217b",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/TheHills.mp3"
    ),
    SongUi(
        id = "s6",
        title = "Flowers",
        artist = "Miley Cyrus",
        imageUrl = "https://i1.sndcdn.com/artworks-YOSTbh90ESawTlzu-s9fROg-t500x500.jpg",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/Flowers.mp3"
    ),
    SongUi(
        id = "s7",
        title = "La Incondicional",
        artist = "Luis Miguel",
        imageUrl = "https://i.ytimg.com/vi/wOjzo02Tmck/hq720.jpg?sqp=-oaymwEhCK4FEIIDSFryq4qpAxMIARUAAAAAGAElAADIQj0AgKJD&rs=AOn4CLCjimJjGE7FfMmJDqlZzf7Sk6P0Rg",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/LaIncondicional.mp3"
    ),
    SongUi(
        id = "s8",
        title = "Chantaje",
        artist = "Shakira ft. Maluma",
        imageUrl = "https://media.gq.com.mx/photos/67169a3da507995d05846d85/16:9/w_2128,h_1197,c_limit/Shakira%20posando%20para%20GQ.jpg",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/Chantaje.mp3"
    ),
    SongUi(
        id = "s9",
        title = "Besos en Guerra",
        artist = "Morat ft. Juanes",
        imageUrl = "https://www.agendapop.cl/wp-content/uploads/2018/01/Morat-2.jpg",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/BesosEnGuerra.mp3"
    ),
    SongUi(
        id = "s10",
        title = "La Bachata",
        artist = "Manuel Turizo",
        imageUrl = "https://i.scdn.co/image/ab67616d0000b273e12c2bf5c7e92522fb3718a1",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/LaBachata.mp3"
    ),
    SongUi(
        id = "s11",
        title = "TQG",
        artist = "Shakira, Karol G",
        imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSWc3k8iv1Mk6VwIekrV6qoSHuvbWaNeA18eg&s",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/TQG.mp3"
    ),
    SongUi(
        id = "s12",
        title = "Porfa No Te Vayas",
        artist = "Morat & Beret",
        imageUrl = "https://i.scdn.co/image/ab67616d0000b2734cac4c4431908529b744ec9b",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/PorfaNoTeVayas.mp3"
    ),
    SongUi(
        id = "s13",
        title = "Hello",
        artist = "Adele",
        imageUrl = "https://img2.rtve.es/a/5760059/?h=300",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/Hello.mp3"
    ),
    SongUi(
        id = "s14",
        title = "Me Enamoré",
        artist = "Shakira",
        imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT16xN0UpFtx9X_KMm8l2aDGbi2ZGd0PQcUIQ1x-STqKlkrkar66wXTn_FPbCfDqh3UKX2IwNMIuEVw-cT3P3CMiFiOVD1DZk757pfK3oI",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/MeEnamore.mp3"
    ),
    SongUi(
        id = "s15",
        title = "Shape of You",
        artist = "Ed Sheeran",
        imageUrl = "https://i.ytimg.com/vi/JGwWNGJdvx8/sddefault.jpg",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/ShapeOfYou.mp3"
    ),
    SongUi(
        id = "s16",
        title = "Perfect",
        artist = "Ed Sheeran",
        imageUrl = "https://i.ytimg.com/vi/cNGjD0VG4R8/sddefault.jpg",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/Perfect.mp3"
    ),
    SongUi(
        id = "s17",
        title = "Cuando Pienses en Volver",
        artist = "Pedro Suárez Vértiz",
        imageUrl = "https://i.ytimg.com/vi/fOBdcuX1FyQ/maxresdefault.jpg",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/CuandoPiensesEnVolver.mp3"
    ),
    SongUi(
        id = "s18",
        title = "Si No Te Hubieras Ido",
        artist = "Marco Antonio Solís",
        imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRALCk7BfwNGiy8BsrP9_sP2k1EBreq4oLLGg&s",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/SiNoTeHubierasIdo.mp3"
    ),
    SongUi(
        id = "s19",
        title = "¡Corre!",
        artist = "Jesse & Joy",
        imageUrl = "https://i.ytimg.com/vi/2LYU0nmdrik/mqdefault.jpg",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/Corre.mp3"
    ),
    SongUi(
        id = "s20",
        title = "Wrecking Ball",
        artist = "Miley Cyrus",
        imageUrl = "https://upload.wikimedia.org/wikipedia/en/0/06/Miley_Cyrus_-_Wrecking_Ball.jpg",
        audioUrl = "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/WreckingBall.mp3"
    )


)