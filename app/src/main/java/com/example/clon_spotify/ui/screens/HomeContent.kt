package com.example.clon_spotify.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.models.SongUi
import com.example.clon_spotify.player.PlayerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * HomeContent: ahora la tarjeta NAVIGATES (onOpenPlaylist) en lugar de reproducir al clickear la card.
 * La reproducción pasa a PlaylistScreen donde aparece la lista y se puede reproducir cada canción.
 */
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    onOpenPlaylist: (String) -> Unit // este callback lo llamas para navegar a la pantalla de la playlist
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    var playlists by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var mixes by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var recomendados by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var albumes by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var likedSongs by remember { mutableStateOf<List<SongUi>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            isLoading = false
            return@LaunchedEffect
        }

        val userDoc = firestore.collection("usuarios").document(userId)

        try {
            // --- Crear subcolecciones iniciales solo si están vacías ---
            suspend fun ensureCollectionExists(
                name: String,
                sampleData: List<PlaylistUi>
            ) {
                val colRef = userDoc.collection(name)
                val snapshot = colRef.get().await()
                if (snapshot.isEmpty) {
                    sampleData.forEach { playlist ->
                        colRef.document(playlist.id).set(playlist).await()
                    }
                }
            }

            // Inicializamos subcolecciones del usuario si no existen
            ensureCollectionExists("recomendados", sampleRecomendados())
            ensureCollectionExists("mixes", sampleMixes())
            ensureCollectionExists("albumes", sampleAlbumes())

            // --- Listeners en tiempo real ---
            userDoc.collection("playlists")
                .addSnapshotListener { snapshot, _ ->
                    playlists = snapshot?.toObjects(PlaylistUi::class.java) ?: emptyList()
                }

            userDoc.collection("recomendados")
                .addSnapshotListener { snapshot, _ ->
                    recomendados = snapshot?.toObjects(PlaylistUi::class.java) ?: emptyList()
                }

            userDoc.collection("mixes")
                .addSnapshotListener { snapshot, _ ->
                    mixes = snapshot?.toObjects(PlaylistUi::class.java) ?: emptyList()
                }

            userDoc.collection("albumes")
                .addSnapshotListener { snapshot, _ ->
                    albumes = snapshot?.toObjects(PlaylistUi::class.java) ?: emptyList()
                }

            userDoc.collection("me_gusta")
                .addSnapshotListener { snapshot, _ ->
                    likedSongs = snapshot?.toObjects(SongUi::class.java) ?: emptyList()
                }

        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
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
                        Text(chip, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (playlists.isNotEmpty()) {
                GridPlaylists(playlists, onOpenPlaylist, playerViewModel, context)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Tus me gusta", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            val tusMeGusta = PlaylistUi(
                id = "tus_me_gusta",
                title = "Tus me gusta",
                description = if (likedSongs.isEmpty()) "Canciones que marcarás con ❤️"
                else "${likedSongs.size} canciones guardadas",
                imageUrl = "https://misc.scdn.co/liked-songs/liked-songs-640.png",
                songs = likedSongs
            )

            // Mostrar mini lista de me gusta (pulsar abre la playlist)
            WidePlaylistCard(
                playlist = tusMeGusta,
                onClick = { onOpenPlaylist(tusMeGusta.id) },
                playerViewModel = playerViewModel,
                context = context,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (recomendados.isNotEmpty()) {
            item {
                SectionCarousel("Recomendados", recomendados, onOpenPlaylist, playerViewModel, context)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (mixes.isNotEmpty()) {
            item {
                SectionCarousel("Tus mixes más escuchados", mixes, onOpenPlaylist, playerViewModel, context)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (albumes.isNotEmpty()) {
            item {
                SectionCarousel("Álbumes con canciones que te gustan", albumes, onOpenPlaylist, playerViewModel, context)
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

/* --- Reutilizables dentro del Home (sin reproducción automática) --- */

@Composable
fun GridPlaylists(
    playlists: List<PlaylistUi>,
    onOpenPlaylist: (String) -> Unit,
    playerViewModel: PlayerViewModel,
    context: Context
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        playlists.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowItems.forEach { playlist ->
                    WidePlaylistCard(
                        playlist = playlist,
                        onClick = { onOpenPlaylist(playlist.id) }, // navegar a detalle
                        playerViewModel = playerViewModel,
                        context = context,
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
    playerViewModel: PlayerViewModel,
    context: Context,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2A2A2A))
            .clickable { onClick() } // ahora solo navega
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
fun SectionCarousel(
    title: String,
    items: List<PlaylistUi>,
    onOpenPlaylist: (String) -> Unit,
    playerViewModel: PlayerViewModel,
    context: Context
) {
    Column {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { p ->
                Column(
                    modifier = Modifier
                        .width(180.dp)
                        .clickable { onOpenPlaylist(p.id) } // navegar
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
// RECOMENDADOS
fun sampleRecomendados(): List<PlaylistUi> {
    val adeleSongs = listOf(
        SongUi("s1", "Someone Like You", "Adele", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcReGAWa2LtEOvbGwwNb2GGL93VJklHmXR6chQ&s", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/Someonelikeyou.mp3"),
        SongUi("s13", "Hello", "Adele", "https://img2.rtve.es/a/5760059/?h=300", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/Hello.mp3"),
        SongUi("s4", "Turning Tables", "Adele", "https://ichef.bbci.co.uk/ace/ws/640/cpsprodpb/d4d7/live/5aa3f190-4454-11ef-96a8-e710c6bfc866.jpg.webp", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/TurningTables.mp3")
    )

    val weekndSongs = listOf(
        SongUi("s2", "Blinding Lights", "The Weeknd", "https://media.gq.com.mx/photos/610492e73b54691d4e5cc00b/16:9/w_2560%2Cc_limit/weeknd-gq-cover-september-2021-10.jpg", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/BlindingLights.mp3"),
        SongUi("s5", "The Hills", "The Weeknd", "https://i.scdn.co/image/ab67616d00001e027fcead687e99583072cc217b", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/TheHills.mp3")
    )

    val shakiraSongs = listOf(
        SongUi("s8", "Chantaje", "Shakira ft. Maluma", "https://media.gq.com.mx/photos/67169a3da507995d05846d85/16:9/w_2128,h_1197,c_limit/Shakira%20posando%20para%20GQ.jpg", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/Chantaje.mp3"),
        SongUi("s11", "TQG", "Shakira, Karol G", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSWc3k8iv1Mk6VwIekrV6qoSHuvbWaNeA18eg&s", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/TQG.mp3"),
        SongUi("s14", "Me Enamoré", "Shakira", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT16xN0UpFtx9X_KMm8l2aDGbi2ZGd0PQcUIQ1x-STqKlkrkar66wXTn_FPbCfDqh3UKX2IwNMIuEVw-cT3P3CMiFiOVD1DZk757pfK3oI", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/MeEnamore.mp3")
    )

    return listOf(
        PlaylistUi("r1", "Adele", "Adele - Éxitos", "https://cdn-images.dzcdn.net/images/cover/8bdaf37e2e7f883e84bbc3462c938293/0x1900-000000-80-0-0.jpg", adeleSongs),
        PlaylistUi("r2", "The Weeknd", "Favorite hits", "https://e2.hespress.com/wp-content/uploads/2022/01/E_wooELVkAM6Sun-800x600.jpeg", weekndSongs),
        PlaylistUi("r3", "Shakira", "Las más escuchadas", "https://i.scdn.co/image/ab6761610000e5eb2528c726e5ddb90a7197e527", shakiraSongs)
    )
}

// MIXES
fun sampleMixes(): List<PlaylistUi> {
    val moratSongs = listOf(
        SongUi("s9", "Besos en Guerra", "Morat ft. Juanes", "https://www.agendapop.cl/wp-content/uploads/2018/01/Morat-2.jpg", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/BesosEnGuerra.mp3"),
        SongUi("s12", "Porfa No Te Vayas", "Morat & Beret", "https://i.scdn.co/image/ab67616d0000b2734cac4c4431908529b744ec9b", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/PorfaNoTeVayas.mp3")
    )

    val brunoSongs = listOf(
        SongUi("s3", "24K Magic", "Bruno Mars", "https://i.scdn.co/image/ab67616d0000b273232711f7d66a1e19e89e28c5", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/24kMagic.mp3")
    )

    val edSongs = listOf(
        SongUi("s15", "Shape of You", "Ed Sheeran", "https://i.ytimg.com/vi/JGwWNGJdvx8/sddefault.jpg", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/ShapeOfYou.mp3"),
        SongUi("s16", "Perfect", "Ed Sheeran", "https://i.ytimg.com/vi/cNGjD0VG4R8/sddefault.jpg", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/Perfect.mp3")
    )

    return listOf(
        PlaylistUi("m1", "Lo Mejor de Morat", "Pop y emociones que llegan al corazón 💙", "https://image-cdn-ak.spotifycdn.com/image/ab67706c0000da84cf4599e4d4bf377a0aef5ab8", moratSongs),
        PlaylistUi("m2", "Bruno Mars Hits", "Ritmo, funk y pura energía 🎶", "https://i.scdn.co/image/ab6761610000e5ebc36dd9eb55fb0db4911f25dd", brunoSongs),
        PlaylistUi("m3", "Ed Sheeran Essentials", "Relájate con las mejores baladas acústicas ☕", "https://cdn.unitycms.io/images/EdB8-yRQardB00_YpJVj43.jpg", edSongs)
    )
}

// ÁLBUMES
fun sampleAlbumes(): List<PlaylistUi> {
    val marcoAlbum = listOf(
        SongUi("s18", "Si No Te Hubieras Ido", "Marco Antonio Solís", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRALCk7BfwNGiy8BsrP9_sP2k1EBreq4oLLGg&s", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/SiNoTeHubierasIdo.mp3")
    )

    val jjAlbum = listOf(
        SongUi("s19", "¡Corre!", "Jesse & Joy", "https://i.ytimg.com/vi/2LYU0nmdrik/mqdefault.jpg", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/Corre.mp3")
    )

    val mileyAlbum = listOf(
        SongUi("s6", "Flowers", "Miley Cyrus", "https://i1.sndcdn.com/artworks-YOSTbh90ESawTlzu-s9fROg-t500x500.jpg", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/Flowers.mp3"),
        SongUi("s20", "Wrecking Ball", "Miley Cyrus", "https://upload.wikimedia.org/wikipedia/en/0/06/Miley_Cyrus_-_Wrecking_Ball.jpg", "https://raw.githubusercontent.com/Roberth-JT/clon-spotify-audios/main/WreckingBall.mp3")
    )

    return listOf(
        PlaylistUi("a1", "Marco Antonio Solís", "Romance y nostalgia", "https://iscale.iheart.com/catalog/artist/39469", marcoAlbum),
        PlaylistUi("a2", "Jesse & Joy", "Amor y armonía", "https://i.scdn.co/image/ab67616d0000b2731f6379010c486d0658e644f5", jjAlbum),
        PlaylistUi("a3", "Miley Cyrus", "Fuerza y libertad", "https://www.hollywoodreporter.com/wp-content/uploads/2025/10/GettyImages-1472464614-e1759444822734.jpg?w=2000&h=1126&crop=1&resize=1440%2C810", mileyAlbum)
    )
}