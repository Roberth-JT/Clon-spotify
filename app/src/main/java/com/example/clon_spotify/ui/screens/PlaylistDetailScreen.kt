package com.example.clon_spotify.ui.screens
/*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.clon_spotify.models.SongUi
import com.example.clon_spotify.player.PlayerViewModel


@Composable
fun PlaylistDetailScreen(navController: NavHostController, playlistId: String, playerViewModel: PlayerViewModel) {
    // obtén canciones de Firestore según playlistId (tú ya tienes lógica)
    val songs: List<SongUi> = remember { */
/* cargar desde Firestore y mapear a SongUi *//*
 emptyList() }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // imagen grande, título etc.
        LazyColumn {
            items(songs.size) { index ->
                val song = songs[index]
                SongCard(song = song, playerViewModel = playerViewModel)
            }
        }

        Button(onClick = { playerViewModel.playPlaylist(songs, startIndex = 0) }) {
            Text("Reproducir toda")
        }
    }
}
*/
