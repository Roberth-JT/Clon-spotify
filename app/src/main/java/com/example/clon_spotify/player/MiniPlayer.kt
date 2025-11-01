package com.example.clon_spotify.player


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun MiniPlayer(playerViewModel: PlayerViewModel) {
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val context = LocalContext.current
    val isFavorite by playerViewModel.isFavorite.collectAsState()
    val playbackPosition = remember { mutableStateOf(0L) }

    // Actualizar posición cada segundo si hay canción
    LaunchedEffect(currentSong) {
        while (currentSong != null) {
            delay(1000)
            playbackPosition.value = playerViewModel.getCurrentPosition()
        }
    }

    // Mostrar solo si hay canción
    if (currentSong != null) {
        val duration = playerViewModel.getDuration().coerceAtLeast(1L)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Fondo translúcido + degradado para un efecto tipo Spotify
                .background(Color(0xCC121212)) // negro con 80% opacidad
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,     // parte superior más suave
                            Color(0xFF121212)      // parte inferior más oscura
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(vertical = 2.dp)
        ) {

            // Barra de progreso
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Slider(
                    value = playbackPosition.value.toFloat(),
                    onValueChange = { newValue ->
                        playbackPosition.value = newValue.toLong()
                    },
                    onValueChangeFinished = {
                        playerViewModel.seekTo(playbackPosition.value)
                    },
                    valueRange = 0f..duration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF1DB954),
                        activeTrackColor = Color(0xFF1DB954),
                        inactiveTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.height(24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = playerViewModel.formatTime(playbackPosition.value),
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = playerViewModel.formatTime(duration),
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Controles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    Text(
                        text = currentSong?.title ?: "",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                    Text(
                        text = currentSong?.artist ?: "",
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { playerViewModel.playPrevious(context) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Anterior",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { playerViewModel.togglePlayPause() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            if (!isFavorite) {
                                playerViewModel.addCurrentSongToFavorites(context)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.Add,
                            contentDescription = if (isFavorite) "Ya en Me Gusta" else "Agregar a Me Gusta",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { playerViewModel.playNext(context) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Siguiente",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}