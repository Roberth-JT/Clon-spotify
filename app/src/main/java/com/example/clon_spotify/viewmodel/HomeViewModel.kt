package com.example.clon_spotify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clon_spotify.models.PlaylistUi
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
    private fun samplePlaylists(): List<com.example.clon_spotify.viewmodel.PlaylistUi> {
        // 🎧 Mix Favoritos
        val mixFavoritosSongs = listOf(
            SongUi("s1", "Levitating", "Dua Lipa", "https://i.scdn.co/image/ab67616d0000b273c00b0a50b1ab57f1a00b8e55"),
            SongUi("s2", "Save Your Tears", "The Weeknd", "https://i.scdn.co/image/ab67616d0000b2738d9d99e5e7c5f4f1cb2a09ab"),
            SongUi("s3", "As It Was", "Harry Styles", "https://i.scdn.co/image/ab67616d0000b273e1c9a12e1a029cfe22b3a6e1")
        )

        // ❤️ Tus me gusta
        val tusMeGustaSongs = listOf(
            SongUi("s1", "Someone Like You", "Adele", "https://i.scdn.co/image/ab67616d0000b273ce0e5e0db8e0a4e244ad0b9c"),
            SongUi("s2", "Just The Way You Are", "Bruno Mars", "https://i.scdn.co/image/ab67616d0000b2734b4c4e084bcb9f6ce0d93e5b"),
            SongUi("s3", "Wrecking Ball", "Miley Cyrus", "https://i.scdn.co/image/ab67616d0000b273f35a1982c8f3cb0f2b0a0fcb")
        )

        // 🇨🇴 Top Colombia
        val topColombiaSongs = listOf(
            SongUi("s1", "Yandel 150", "Yandel, Feid", "https://i.scdn.co/image/ab67616d0000b273b35c2cbf7c2c6e9d5ed8c4e3"),
            SongUi("s2", "Feliz Cumpleaños Ferxxo", "Feid", "https://i.scdn.co/image/ab67616d0000b273a9dd4d1d1e5f48e1ab3d6a64"),
            SongUi("s3", "La Bachata", "Manuel Turizo", "https://i.scdn.co/image/ab67616d0000b2734ed4f9f6a98b0249a2f6c6c8")
        )

        return listOf(
            PlaylistUi(
                id = "p1",
                title = "Mix Favoritos",
                description = "Tus canciones más escuchadas",
                imageUrl = "https://i.scdn.co/image/ab67706f000000035c5c4b9abca6d79953e7b40a",
                songs = mixFavoritosSongs
            ),
            PlaylistUi(
                id = "p2",
                title = "Tus me gusta",
                description = "Canciones que marcaste con ❤️",
                imageUrl = "https://misc.scdn.co/liked-songs/liked-songs-640.png",
                songs = tusMeGustaSongs
            ),
            PlaylistUi(
                id = "p3",
                title = "Top Colombia",
                description = "Los éxitos más escuchados del país",
                imageUrl = "https://charts-images.scdn.co/assets/locale_en/regional/daily/region_co_default.jpg",
                songs = topColombiaSongs
            )
        )
    }



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

    private fun sampleRecomendados(): List<PlaylistUi> = listOf(
        PlaylistUi(
            id = "r1",
            title = "Pop Actual",
            description = "Los hits más nuevos del pop",
            imageUrl = "https://i.scdn.co/image/ab67706f00000003a4978e4b2b88bcb66f4ce3d8"
        ),
        PlaylistUi(
            id = "r2",
            title = "Indie Cool",
            description = "Sonidos frescos y diferentes",
            imageUrl = "https://i.scdn.co/image/ab67706f000000035a7c1ed9c25b785cd71f4e9f"
        )
    )
}
