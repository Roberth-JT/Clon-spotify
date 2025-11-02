package com.example.clon_spotify.player

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.clon_spotify.models.SongUi
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Enum para los modos de repetición
enum class RepeatMode {
    OFF,       // No repetir
    ALL,       // Repetir toda la playlist
    ONE        // Repetir una canción
}

class PlayerViewModel : ViewModel() {

    private var exoPlayer: ExoPlayer? = null

    private val _currentSong = MutableStateFlow<SongUi?>(null)
    val currentSong: StateFlow<SongUi?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    // Estado para el modo de repetición
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    // Playlist actual y estado
    private val _currentPlaylist = MutableStateFlow<List<SongUi>>(emptyList())
    val currentPlaylist: StateFlow<List<SongUi>> = _currentPlaylist.asStateFlow()

    private var currentIndex = -1
    private var currentPlaylistId: String? = null

    /**
     * Asigna la lista de reproducción actual
     */
    fun setPlaylist(tracks: List<SongUi>, trackStartIndex: Int = 0, playlistId: String? = null) {
        _currentPlaylist.value = tracks.toList() // Crear una copia
        currentIndex = trackStartIndex.coerceIn(0, tracks.size - 1)
        currentPlaylistId = playlistId

        // Si hay tracks, establecer la canción actual
        if (tracks.isNotEmpty() && trackStartIndex < tracks.size) {
            _currentSong.value = tracks[trackStartIndex]
        }
    }

    /**
     * Reproduce una canción específica dentro de una playlist
     */
    fun playSongInPlaylist(song: SongUi, playlist: List<SongUi>, context: Context) {
        // Primero establecer la playlist completa
        setPlaylist(playlist)
        // Luego reproducir la canción específica
        playSong(song, context)
    }

    /**
     * Reproduce una canción específica dentro de la playlist actual
     */
    fun playSong(song: SongUi, context: Context) {
        viewModelScope.launch {
            try {
                // Verificar si la canción está en la playlist actual
                val playlist = _currentPlaylist.value
                val songIndex = playlist.indexOfFirst { it.id == song.id }

                if (songIndex == -1) {
                    _errorMessage.value = "La canción no está en la playlist actual"
                    return@launch
                }

                if (exoPlayer == null) {
                    exoPlayer = ExoPlayer.Builder(context).build()
                }

                exoPlayer?.apply {
                    stop()
                    clearMediaItems()
                    setMediaItem(MediaItem.fromUri(song.audioUrl))
                    prepare()
                    playWhenReady = true
                    play()

                    // Configurar el modo de repetición actual en el ExoPlayer
                    updateExoPlayerRepeatMode()

                    removeListener(listener)
                    addListener(listener)
                }

                _currentSong.value = song
                currentIndex = songIndex
                _isPlaying.value = true
                _errorMessage.value = null
                checkIfFavorite()

            } catch (e: Exception) {
                _errorMessage.value = "Error al reproducir: ${e.localizedMessage}"
                _isPlaying.value = false
            }
        }
    }

    /**
     * Reproduce una canción por su índice en la playlist actual
     */
    fun playSongByIndex(index: Int, context: Context) {
        val playlist = _currentPlaylist.value
        if (index in playlist.indices) {
            playSong(playlist[index], context)
        }
    }

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            _isPlaying.value = state == Player.STATE_READY && (exoPlayer?.playWhenReady ?: false)

            // Cuando termina la reproducción (STATE_ENDED)
            if (state == Player.STATE_ENDED) {
                handleEndOfTrack()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _errorMessage.value = "Player error: ${error.message}"
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            _isPlaying.value = playWhenReady
        }
    }

    /**
     * Maneja el fin de una canción según el modo de repetición
     */
    private fun handleEndOfTrack() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                // Repetir la misma canción - ExoPlayer ya lo hace automáticamente
                exoPlayer?.seekTo(0)
                exoPlayer?.play()
            }
            RepeatMode.ALL -> {
                // Pasar a la siguiente canción automáticamente en la misma playlist
                val context = android.app.Application().applicationContext
                playNext(context)
            }
            RepeatMode.OFF -> {
                // Comportamiento normal - pasar a la siguiente si hay más canciones
                val playlist = _currentPlaylist.value
                if (currentIndex < playlist.size - 1) {
                    val context = android.app.Application().applicationContext
                    playNext(context)
                }
            }
        }
    }

    /**
     * Actualiza el modo de repetición en el ExoPlayer
     */
    private fun updateExoPlayerRepeatMode() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            }
            RepeatMode.ALL -> {
                exoPlayer?.repeatMode = Player.REPEAT_MODE_ALL
            }
            RepeatMode.OFF -> {
                exoPlayer?.repeatMode = Player.REPEAT_MODE_OFF
            }
        }
    }

    /**
     * Cambia el modo de repetición
     */
    fun toggleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }

        // Actualizar el modo en el ExoPlayer
        updateExoPlayerRepeatMode()
    }

    fun pause() {
        exoPlayer?.pause()
        _isPlaying.value = false
    }

    fun resume() {
        exoPlayer?.play()
        _isPlaying.value = true
    }

    fun togglePlayPause() {
        if (exoPlayer?.isPlaying == true) pause() else resume()
    }

    fun playNext(context: Context) {
        val playlist = _currentPlaylist.value
        if (playlist.isNotEmpty()) {
            when (_repeatMode.value) {
                RepeatMode.ONE -> {
                    // En modo repetir una, simplemente reiniciamos la misma canción
                    exoPlayer?.seekTo(0)
                    exoPlayer?.play()
                }
                else -> {
                    // Modo OFF o ALL: pasar a la siguiente canción en la MISMA playlist
                    val nextIndex = (currentIndex + 1) % playlist.size

                    // Si estamos en modo OFF y es la última canción, no hacer nada
                    if (_repeatMode.value == RepeatMode.OFF && nextIndex == 0) {
                        // Última canción en modo OFF - detener reproducción
                        pause()
                    } else {
                        // Reproducir la siguiente canción en la misma playlist
                        playSongByIndex(nextIndex, context)
                    }
                }
            }
        }
    }

    fun playPrevious(context: Context) {
        val playlist = _currentPlaylist.value
        if (playlist.isNotEmpty()) {
            when (_repeatMode.value) {
                RepeatMode.ONE -> {
                    // En modo repetir una, simplemente reiniciamos la misma canción
                    exoPlayer?.seekTo(0)
                    exoPlayer?.play()
                }
                else -> {
                    // Modo OFF o ALL: pasar a la anterior canción en la MISMA playlist
                    val previousIndex = if (currentIndex - 1 < 0) playlist.size - 1 else currentIndex - 1
                    playSongByIndex(previousIndex, context)
                }
            }
        }
    }

    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0L
    fun getDuration(): Long = exoPlayer?.duration ?: 0L
    fun seekTo(position: Long) = exoPlayer?.seekTo(position)

    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    fun addCurrentSongToFavorites(context: Context) {
        val song = _currentSong.value ?: return
        val usuariosId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (usuariosId == null) {
            Toast.makeText(context, "Error: usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(usuariosId)
            .collection("me_gusta")
            .document(song.id)
            .set(song)
            .addOnSuccessListener {
                Toast.makeText(context, "Agregada a tus Me Gusta 💚", Toast.LENGTH_SHORT).show()
                _isFavorite.value = true
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al agregar", Toast.LENGTH_SHORT).show()
            }
    }

    fun checkIfFavorite() {
        val song = _currentSong.value ?: return
        val usuariosId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(usuariosId)
            .collection("me_gusta")
            .document(song.id)
            .get()
            .addOnSuccessListener { doc ->
                _isFavorite.value = doc.exists()
            }
            .addOnFailureListener {
                _isFavorite.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer?.removeListener(listener)
        exoPlayer?.release()
        exoPlayer = null
    }
}