package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun PlaylistScreen(playlistId: String?) {
    val playlist = samplePlaylists().find { it.id == playlistId } ?: samplePlaylists().first()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(playlist.title, color = Color.White) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black))
        },
        containerColor = Color(0xFF0B0B0B)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            AsyncImage(model = playlist.imageUrl, contentDescription = playlist.title, modifier = Modifier.height(220.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)))
            Spacer(modifier = Modifier.height(12.dp))
            Text(playlist.title, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            playlist.description?.let { Text(it, color = Color.LightGray) }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Canciones", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(playlist.songs.size) { idx ->
                    val s = playlist.songs[idx]
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(model = s.imageUrl, contentDescription = s.title, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.title, color = Color.White)
                            Text(s.artist, color = Color.LightGray)
                        }
                        IconButton(onClick = { /* like - later */ }) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = "Like", tint = Color(0xFF1DB954))
                        }
                    }
                }
            }
        }
    }
}
