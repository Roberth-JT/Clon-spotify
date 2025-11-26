package com.example.clon_spotify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.ui.screens.sampleMixes
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _playlists = MutableStateFlow<List<PlaylistUi>>(emptyList())
    //val playlists: StateFlow<List<PlaylistUi>> = _playlists

    private val _mixes = MutableStateFlow<List<PlaylistUi>>(emptyList())
    //val mixes: StateFlow<List<PlaylistUi>> = _mixes

    init {
        ensureDatabaseInitialized()
        fetchPlaylists()
        fetchMixes()
    }

    private fun ensureDatabaseInitialized() {
        viewModelScope.launch {
            //val playlistsRef = firestore.collection("playlists")
            val mixesRef = firestore.collection("mixes")
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
}
