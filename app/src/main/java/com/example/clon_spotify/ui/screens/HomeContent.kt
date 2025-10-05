package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.clon_spotify.R

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    onOpenPlaylist: (String) -> Unit
) {
    // sample data
    val playlists = remember { samplePlaylists() }
    val mixes = remember { sampleMixes() }

    Column(modifier = modifier.padding(12.dp)) {
        // Chips row
        val chips = listOf("Todas", "Música", "Podcasts")
        var selectedChip by remember { mutableStateOf("Todas") }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(chips) { chip ->
                val isSelected = chip == selectedChip
                Button(
                    onClick = { selectedChip = chip },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFF7B1FA2) else Color(0xFF1DB954)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(chip, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Tus me gusta (fixed card)
        Text("Tus me gusta", color = Color.White, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            playlists.take(3).forEach { p ->
                SmallPlaylistCard(p) { onOpenPlaylist(p.id) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recomendados (carousel)
        SectionCarousel(title = "Recomendados", items = mixes, onOpenPlaylist = onOpenPlaylist)

        Spacer(modifier = Modifier.height(12.dp))

        SectionCarousel(title = "Viernes de lanzamientos", items = playlists, onOpenPlaylist = onOpenPlaylist)

        Spacer(modifier = Modifier.height(12.dp))

        SectionCarousel(title = "Fiesta", items = mixes, onOpenPlaylist = onOpenPlaylist)

        Spacer(modifier = Modifier.height(80.dp)) // espacio para mini-player
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
        Text(playlist.title, color = Color.White, maxLines = 1, fontWeight = FontWeight.Medium)
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
                Column(modifier = Modifier.width(180.dp).clickable { onOpenPlaylist(p.id) }) {
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

/** Sample data - same helpers as previous file **/
 fun samplePlaylists(): List<PlaylistUi> {
    val songs = listOf(
        SongUi("s1", "Volví a nacer", "Carlos Vives", "https://i.scdn.co/image/ab67616d0000b27317cb72f4c671f0b86e8a9d22"),
        SongUi("s2", "Hello", "Adele", "https://i.scdn.co/image/ab67616d0000b273abcdef1234567890")
    )
    return listOf(
        PlaylistUi("p1","Mix Favoritos","Creado para ti","https://i.scdn.co/image/ab67616d0000b27317cb72f4c671f0b86e8a9d22", songs),
        PlaylistUi("p2","Los me gusta","Tus canciones favoritas","https://cdn-icons-png.flaticon.com/512/833/833472.png", songs),
        PlaylistUi("p3","Para ti","Sugerencias", "https://i.scdn.co/image/ab67616d0000b273f1d24b706bc3f4042fdb4b5a", songs)
    )
}

 fun sampleMixes(): List<PlaylistUi> {
    return listOf(
        PlaylistUi("m1","Viernes Pop","Lanzamientos", "https://i.scdn.co/image/ab67616d0000b273b1a4d97f18dfe9a73d3a50f7"),
        PlaylistUi("m2","Fiesta 2025","Dance & Hits","https://i.scdn.co/image/ab67616d0000b273b2b2b2b2b2b2b2b2"),
        PlaylistUi("m3","80s Vibes","Clásicos", "https://i.scdn.co/image/ab67616d0000b273c3c3c3c3c3c3c3c3")
    )
}
