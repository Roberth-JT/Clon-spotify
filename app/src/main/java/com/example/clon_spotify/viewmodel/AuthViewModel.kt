package com.example.clon_spotify.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class AuthViewModel : ViewModel() {
    var userId by mutableStateOf("")
    var isValidAuth by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var authMessage by mutableStateOf("")

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    // 🔹 Registro con Firestore
    fun register(email: String, password: String, nombre: String, onResult: (Boolean, String?) -> Unit) {
        isLoading = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: ""
                val userMap = mapOf(
                    "uid" to uid,
                    "nombre" to nombre,
                    "email" to email
                )
                firestore.collection("usuarios").document(uid).set(userMap)
                    .addOnSuccessListener {
                        authMessage = ""
                        isLoading = false
                        isValidAuth = true
                        userId = uid
                        onResult(true, null)
                    }
                    .addOnFailureListener { error ->
                        authMessage = "Error guardando datos: ${error.localizedMessage}"
                        isLoading = false
                        onResult(false, authMessage)
                    }
            }
            .addOnFailureListener { error ->
                Log.e("SPOTIFY_APP", "Error al registrar", error)
                authMessage = error.localizedMessage ?: "Error al registrar"
                isLoading = false
                isValidAuth = false
                onResult(false, authMessage)
            }
    }

    // 🔹 Login con FirebaseAuth
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        isLoading = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                authMessage = ""
                isLoading = false
                isValidAuth = true
                userId = result.user?.uid ?: ""
                onResult(true, null)
            }
            .addOnFailureListener { error ->
                Log.e("SPOTIFY_APP", "Error al iniciar sesión", error)
                authMessage = "Usuario y/o contraseña incorrecto"
                isLoading = false
                isValidAuth = false
                onResult(false, authMessage)
            }
    }

    // 🔹 Cerrar sesión
    fun logout() {
        auth.signOut()
        isValidAuth = false
        userId = ""
    }
}
