package com.example.clon_spotify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class User(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val imageUrl: String = ""
)

class FriendsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    private val _filteredUsers = MutableStateFlow<List<User>>(emptyList())
    val filteredUsers: StateFlow<List<User>> = _filteredUsers

    private val _selectedUsers = MutableStateFlow<Set<String>>(emptySet())
    val selectedUsers: StateFlow<Set<String>> = _selectedUsers

    init {
        loadAllUsers()
    }

    /** 🔹 Carga todos los usuarios en tiempo real desde Firestore */
    fun loadAllUsers() {
        val currentUserId = auth.currentUser?.uid
        db.collection("usuarios")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                val users = snapshots.documents.mapNotNull { doc ->
                    val uid = doc.id
                    val nombre = doc.getString("nombre") ?: ""
                    val email = doc.getString("email") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""

                    if (uid != currentUserId) {
                        User(uid, nombre, email, imageUrl)
                    } else null
                }

                _allUsers.value = users
                _filteredUsers.value = users // Por defecto muestra todo
            }
    }

    /** 🔹 Filtra usuarios por nombre o correo */
    fun filterUsers(query: String) {
        viewModelScope.launch {
            val lowerQuery = query.trim().lowercase()
            _filteredUsers.value = if (lowerQuery.isEmpty()) {
                _allUsers.value
            } else {
                _allUsers.value.filter {
                    it.nombre.lowercase().contains(lowerQuery) ||
                            it.email.lowercase().contains(lowerQuery)
                }
            }
        }
    }

    /** 🔹 Seguir a un usuario */
    fun Seguidos(user: User, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return onError("Usuario no autenticado")

        val followRef = db.collection("usuarios")
            .document(currentUserId)
            .collection("seguidos")
            .document(user.uid)

        val followData = mapOf(
            "uid" to user.uid,
            "nombre" to user.nombre,
            "email" to user.email,
            "imageUrl" to user.imageUrl
        )

        followRef.set(followData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al seguir usuario") }
    }

    /** 🔹 Verifica si el usuario ya es seguido */
    fun isSeguidos(userId: String, callback: (Boolean) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return callback(false)
        db.collection("usuarios")
            .document(currentUserId)
            .collection("seguidos")
            .document(userId)
            .get()
            .addOnSuccessListener { snapshot -> callback(snapshot.exists()) }
            .addOnFailureListener { callback(false) }
    }

    /** 🔹 Dejar de seguir a un usuario */
    fun dejarDeSeguir(userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return onError("Usuario no autenticado")

        db.collection("usuarios")
            .document(currentUserId)
            .collection("seguidos")
            .document(userId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al dejar de seguir usuario") }
    }

    /** 🔹 Selección múltiple (invitaciones, etc.) */
    fun toggleUserSelection(userId: String) {
        _selectedUsers.value = if (_selectedUsers.value.contains(userId)) {
            _selectedUsers.value - userId
        } else {
            _selectedUsers.value + userId
        }
    }

    fun clearSelection() {
        _selectedUsers.value = emptySet()
    }

    fun sendInvitations(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                onSuccess()
                clearSelection()
            } catch (e: Exception) {
                onError(e.message ?: "Error al enviar invitaciones")
            }
        }
    }
}
