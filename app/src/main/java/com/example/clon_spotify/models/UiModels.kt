package com.example.clon_spotify.models

data class SongUi(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val imageUrl: String = ""
)

data class PlaylistUi(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val imageUrl: String = "",
    val songs: List<SongUi> = emptyList()
)
