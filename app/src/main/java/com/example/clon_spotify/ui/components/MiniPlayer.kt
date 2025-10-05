package com.example.clon_spotify.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage

@Composable
fun MiniPlayer() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            AsyncImage(model = "https://i.scdn.co/image/ab67616d0000b27317cb72f4c671f0b86e8a9d22", contentDescription = "song", modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Volví a nacer", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Text("Carlos Vives", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { /* like */ }) { Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color(0xFF1DB954)) }
            IconButton(onClick = { /* play */ }) { Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White) }
        }
    }
}
