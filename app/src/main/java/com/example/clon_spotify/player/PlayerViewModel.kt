package com.example.clon_spotify.player


import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
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

    val trackList = mutableListOf<SongUi>()
    private var currentIndex = -1

    //verificar si la cancion esta en faovritos variables:
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()


    fun playSong(song: SongUi, context: Context) {
        viewModelScope.launch {
            try {
                if (exoPlayer == null) {
                    exoPlayer = ExoPlayer.Builder(context).build()
                }

                exoPlayer?.apply {
                    setMediaItem(MediaItem.fromUri(song.audioUrl))
                    prepare()
                    play()

                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            _isPlaying.value = state == Player.STATE_READY && playWhenReady
                        }
                    })
                    checkIfFavorite()

                }

                _currentSong.value = song
                _isPlaying.value = true
                _errorMessage.value = null

                // Actualiza índice actual
                currentIndex = trackList.indexOfFirst { it.id == song.id }

            } catch (e: Exception) {
                _errorMessage.value = "Error al reproducir: ${e.localizedMessage}"
                _isPlaying.value = false
            }
        }
    }
    fun addCurrentSongToFavorites(context: Context) {
        val song = _currentSong.value ?: return
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("me_gusta")
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

    //funcion que verifica si las canciones esta en me gusta :
fun checkIfFavorite() {
    val song = _currentSong.value ?: return
    FirebaseFirestore.getInstance()
        .collection("me_gusta")
        .document(song.id)
        .get()
        .addOnSuccessListener { document ->
            _isFavorite.value = document.exists()
        }
        .addOnFailureListener {
            _isFavorite.value = false
        }
}


    fun playNext(context: Context) {
        if (trackList.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % trackList.size
            val nextSong = trackList[currentIndex]
            playSong(nextSong, context)
        }
    }

    fun playPrevious(context: Context) {
        if (trackList.isNotEmpty()) {
            currentIndex = if (currentIndex - 1 < 0) trackList.size - 1 else currentIndex - 1
            val prevSong = trackList[currentIndex]
            playSong(prevSong, context)
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            } else {
                it.play()
                _isPlaying.value = true
            }
        }
    }
    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0L
    fun getDuration(): Long = exoPlayer?.duration ?: 0L

    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }



    override fun onCleared() {
        super.onCleared()
        exoPlayer?.release()
        exoPlayer = null
    }
}
