package com.example.clon_spotify.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class User(
    val uid: String="",
    val nombre: String="",
    val email: String="",
    val imageUrl: String = ""


)

class FriendsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    private val _selectedUsers = MutableStateFlow<Set<String>>(emptySet())
    val selectedUsers: StateFlow<Set<String>> = _selectedUsers

    fun loadAllUsers() {
        viewModelScope.launch {
            try {
                db.collection("usuarios")
                    .get()
                    .addOnSuccessListener { result ->
                        val users = mutableListOf<User>()
                        val currentUserId = auth.currentUser?.uid

                        for (document in result) {
                            val user = User(
                                uid = document.id,
                                nombre = document.getString("nombre") ?: "",
                                email = document.getString("email") ?: ""
                            )
                            // Excluir al usuario actual de la lista
                            if (user.uid != currentUserId) {
                                users.add(user)
                            }
                        }
                        _allUsers.value = users
                    }
                    .addOnFailureListener { exception ->
                        // Manejar error
                    }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

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
                // Aquí puedes implementar el envío real de invitaciones
                // Por ahora solo simulamos el éxito
                onSuccess()
                clearSelection()
            } catch (e: Exception) {
                onError(e.message ?: "Error al enviar invitaciones")
            }
        }
    }
    /** 🔹 Función para seguir a otro usuario */
    fun Seguidos(user: User, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return onError("Usuario no autenticado")

        val followRef = db.collection("usuarios")
            .document(currentUserId)
            .collection("seguidos")
            .document(user.uid)

        val followData = mapOf(
            "uid" to user.uid,
            "nombre" to user.nombre,
            "email" to user.email
        )

        followRef.set(followData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al seguir usuario") }
    }

    /** 🔹 Verificar si ya lo sigue (opcional) */
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
    /** 🔹 Función para dejar de seguir a un usuario */
    fun dejarDeSeguir(userId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: return onError("Usuario no autenticado")

        val followRef = db.collection("usuarios")
            .document(currentUserId)
            .collection("seguidos")
            .document(userId)

        followRef.delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al dejar de seguir usuario") }
    }

}