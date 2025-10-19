package com.example.clon_spotify.player

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import com.example.clon_spotify.models.SongUi
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    val trackList = mutableListOf<SongUi>()
    private var currentIndex = -1
    private var currentPlaylistId: String? = null

    /**
     * Asigna la lista de reproducción actual
     */
    fun setPlaylist(tracks: List<SongUi>, trackStartIndex: Int = 0, playlistId: String? = null) {
        trackList.clear()
        trackList.addAll(tracks)
        currentIndex = trackStartIndex.coerceIn(0, trackList.size - 1)
        currentPlaylistId = playlistId
    }

    /**
     * Reproduce una canción
     */
    fun playSong(song: SongUi, context: Context) {
        viewModelScope.launch {
            try {
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

                    removeListener(listener)
                    addListener(listener)
                }

                _currentSong.value = song
                _isPlaying.value = true
                _errorMessage.value = null
                currentIndex = trackList.indexOfFirst { it.id == song.id }
                checkIfFavorite()

            } catch (e: Exception) {
                _errorMessage.value = "Error al reproducir: ${e.localizedMessage}"
                _isPlaying.value = false
            }
        }
    }

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            _isPlaying.value = state == Player.STATE_READY && (exoPlayer?.playWhenReady ?: false)
        }

        override fun onPlayerError(error: PlaybackException) {
            _errorMessage.value = "Player error: ${error.message}"
        }
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
        if (trackList.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % trackList.size
            playSong(trackList[currentIndex], context)
        }
    }

    fun playPrevious(context: Context) {
        if (trackList.isNotEmpty()) {
            currentIndex = if (currentIndex - 1 < 0) trackList.size - 1 else currentIndex - 1
            playSong(trackList[currentIndex], context)
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
