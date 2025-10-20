package com.example.clon_spotify.viewmodel

import androidx.lifecycle.ViewModel
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.models.SongUi
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PerfilUsuarioViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _playlists = MutableStateFlow<List<PlaylistUi>>(emptyList())
    val playlists: StateFlow<List<PlaylistUi>> = _playlists

    /** 🔹 Cargar datos del perfil y sus playlists */
    fun cargarPerfil(userId: String) {
        // Cargar datos del usuario
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { doc ->
                val user = User(
                    uid = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    email = doc.getString("email") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
                _userProfile.value = user
            }

        // Cargar playlists del usuario seguido
        db.collection("usuarios")
            .document(userId)
            .collection("playlists")
            .get()
            .addOnSuccessListener { result ->
                val listaPlaylists = mutableListOf<PlaylistUi>()

                result.forEach { playlistDoc ->
                    val playlistId = playlistDoc.id
                    val title = playlistDoc.getString("title") ?: ""
                    val description = playlistDoc.getString("description")
                    val imageUrl = playlistDoc.getString("imageUrl") ?: ""

                    // Cargar canciones de cada playlist
                    db.collection("usuarios")
                        .document(userId)
                        .collection("playlists")
                        .document(playlistId)
                        .collection("songs")
                        .get()
                        .addOnSuccessListener { songsResult ->
                            val songs = songsResult.mapNotNull { songDoc ->
                                SongUi(
                                    id = songDoc.id,
                                    title = songDoc.getString("title") ?: "",
                                    artist = songDoc.getString("artist") ?: "",
                                    imageUrl = songDoc.getString("imageUrl") ?: "",
                                    audioUrl = songDoc.getString("audioUrl") ?: ""
                                )
                            }

                            val playlist = PlaylistUi(
                                id = playlistId,
                                title = title,
                                description = description,
                                imageUrl = imageUrl,
                                songs = songs
                            )

                            listaPlaylists.add(playlist)
                            _playlists.value = listaPlaylists.toList()
                        }
                }
            }
    }
}
