package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.NavController
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.models.SongUi
import com.example.clon_spotify.player.PlayerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MusicScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel,
    onOpenPlaylist: (String) -> Unit
) {
    val context = LocalContext.current
    var playlists by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var mixes by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
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
            // Listeners en tiempo real
            userDoc.collection("playlists")
                .addSnapshotListener { snapshot, _ ->
                    playlists = snapshot?.toObjects(PlaylistUi::class.java) ?: emptyList()
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
            // Manejar error
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
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Tu música",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Tus me gusta
        item {
            val tusMeGusta = PlaylistUi(
                id = "tus_me_gusta",
                title = "Tus me gusta",
                description = if (likedSongs.isEmpty()) "Canciones que marcarás con ❤️"
                else "${likedSongs.size} canciones guardadas",
                imageUrl = "https://misc.scdn.co/liked-songs/liked-songs-640.png",
                songs = likedSongs
            )

            WidePlaylistCard(
                playlist = tusMeGusta,
                onClick = { onOpenPlaylist(tusMeGusta.id) },
                playerViewModel = playerViewModel,
                context = context,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Tus playlists
        if (playlists.isNotEmpty()) {
            item {
                Text(
                    "Tus playlists",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(playlists) { playlist ->
                WidePlaylistCard(
                    playlist = playlist,
                    onClick = { onOpenPlaylist(playlist.id) },
                    playerViewModel = playerViewModel,
                    context = context,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Álbumes
        if (albumes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Álbumes que te gustan",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(albumes) { album ->
                WidePlaylistCard(
                    playlist = album,
                    onClick = { onOpenPlaylist(album.id) },
                    playerViewModel = playerViewModel,
                    context = context,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Mixes
        if (mixes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Tus mixes",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(mixes) { mix ->
                WidePlaylistCard(
                    playlist = mix,
                    onClick = { onOpenPlaylist(mix.id) },
                    playerViewModel = playerViewModel,
                    context = context,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}