package com.example.clon_spotify.models

data class SongUi(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val imageUrl: String = "",
    val audioUrl: String = "",

)

data class PlaylistUi(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val imageUrl: String = "",
    val songs: List<SongUi> = emptyList(),
    val isPublic: Boolean = false // 👈 Nuevo campo

)
data class User(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val imageUrl: String = ""
)