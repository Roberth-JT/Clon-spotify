package com.example.clon_spotify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PlaylistUi(
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    val imageUrl: String = "",
    val songs: List<SongUi> = emptyList()
)

data class SongUi(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val imageUrl: String = ""
)

class HomeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _playlists = MutableStateFlow<List<PlaylistUi>>(emptyList())
    val playlists: StateFlow<List<PlaylistUi>> = _playlists

    private val _mixes = MutableStateFlow<List<PlaylistUi>>(emptyList())
    val mixes: StateFlow<List<PlaylistUi>> = _mixes

    init {
        ensureDatabaseInitialized()
        fetchPlaylists()
        fetchMixes()
    }

    private fun ensureDatabaseInitialized() {
        viewModelScope.launch {
            val playlistsRef = firestore.collection("playlists")
            val mixesRef = firestore.collection("mixes")

            playlistsRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    // Si la colección no existe, la creamos con tus datos por defecto
                    val defaultPlaylists = samplePlaylists()
                    defaultPlaylists.forEach { playlistsRef.document(it.id).set(it) }
                }
            }

            mixesRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    val defaultMixes = sampleMixes()
                    defaultMixes.forEach { mixesRef.document(it.id).set(it) }
                }
            }
        }
    }

    private fun fetchPlaylists() {
        viewModelScope.launch {
            firestore.collection("playlists")
                .addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PlaylistUi::class.java)
                    }
                    _playlists.value = list
                }
        }
    }

    private fun fetchMixes() {
        viewModelScope.launch {
            firestore.collection("mixes")
                .addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PlaylistUi::class.java)
                    }
                    _mixes.value = list
                }
        }
    }

    /** Tus datos base (los mismos del HomeContent) **/
    private fun samplePlaylists(): List<PlaylistUi> = listOf(
        PlaylistUi(
            id = "p1",
            title = "Mix Favoritos",
            description = "Creado para ti",
            imageUrl = "https://i.scdn.co/image/ab67706f000000035c5c4b9abca6d79953e7b40a"
        ),
        PlaylistUi(
            id = "p2",
            title = "Tus me gusta",
            description = "Canciones que marcaste con ❤️",
            imageUrl = "https://misc.scdn.co/liked-songs/liked-songs-640.png"
        ),
        PlaylistUi(
            id = "p3",
            title = "Top Colombia",
            description = "Los éxitos más escuchados del país",
            imageUrl = "https://charts-images.scdn.co/assets/locale_en/regional/daily/region_co_default.jpg"
        ),
        PlaylistUi(
            id = "p4",
            title = "Reggaeton Mix",
            description = "Bad Bunny, Feid, Mora y más",
            imageUrl = "https://i.scdn.co/image/ab67706f000000033ebaf0e1cbf7e8f22f5457a7"
        ),
        PlaylistUi(
            id = "p5",
            title = "Pop en Español",
            description = "Sebastián Yatra, Morat, Reik y más",
            imageUrl = "https://i.scdn.co/image/ab67706f000000039d9f1eaf03c0d29c31786c4b"
        )
    )

    private fun sampleMixes(): List<PlaylistUi> = listOf(
        PlaylistUi(
            id = "m1",
            title = "Viernes de lanzamientos",
            description = "Lo más nuevo de tus artistas favoritos",
            imageUrl = "https://i.scdn.co/image/ab67706f000000035a9f0c9c73f83e8f17de74b8"
        ),
        PlaylistUi(
            id = "m2",
            title = "Fiesta Latina",
            description = "Puro perreo, reggaetón y ritmo 🔥",
            imageUrl = "https://i.scdn.co/image/ab67706f000000038efb0f9eb23e10e5b6239bcb"
        ),
        PlaylistUi(
            id = "m3",
            title = "Lo-Fi Beats",
            description = "Para estudiar o relajarte ☕",
            imageUrl = "https://i.scdn.co/image/ab67706f000000037d0de1b1dc653f2697b9c65f"
        ),
        PlaylistUi(
            id = "m4",
            title = "Workout Hits",
            description = "Motivación total para el gym 💪",
            imageUrl = "https://i.scdn.co/image/ab67706f000000035418b6d058bd1f93afdfdd58"
        ),
        PlaylistUi(
            id = "m5",
            title = "Clásicos del 2000",
            description = "Rihanna, Maroon 5, Coldplay y más",
            imageUrl = "https://i.scdn.co/image/ab67706f00000003a943f25575cf50f72b2b7e4a"
        )
    )
}
