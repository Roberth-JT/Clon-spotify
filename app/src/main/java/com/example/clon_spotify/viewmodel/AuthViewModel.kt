package com.example.clon_spotify.viewmodel


import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
    var userId by mutableStateOf<String?>(null)
    var isValidAuth by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var authMessage by mutableStateOf<String?>(null)

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun register(
        email: String,
        password: String,
        nombre: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        isLoading = true
        authMessage = null
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
                        isLoading = false
                        isValidAuth = true
                        userId = uid
                        onResult(true, null)
                    }
                    .addOnFailureListener { error ->
                        isLoading = false
                        authMessage = "Error guardando datos: ${error.localizedMessage}"
                        Log.e("AuthViewModel", "Error guardando user", error)
                        onResult(false, authMessage)
                    }
            }
            .addOnFailureListener { error ->
                isLoading = false
                authMessage = "Error al registrar: ${error.localizedMessage}"
                Log.e("AuthViewModel", "Error registrar", error)
                onResult(false, authMessage)
            }
    }

    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        isLoading = true
        authMessage = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                isLoading = false
                isValidAuth = true
                userId = result.user?.uid
                onResult(true, null)
            }
            .addOnFailureListener { error ->
                isLoading = false
                isValidAuth = false
                authMessage = "Usuario y/o contraseña incorrectos"
                Log.e("AuthViewModel", "Login failed", error)
                onResult(false, authMessage)
            }
    }

    fun loginWithGoogle(
        context: Context,
        onResult: (Boolean) -> Unit
    ) {
        isLoading = true
        authMessage = null
        GoogleAuthHelper.signInWithGoogle(context) { success, uid, error ->
            isLoading = false
            if (success && uid != null) {
                isValidAuth = true
                userId = uid
                onResult(true)
            } else {
                isValidAuth = false
                authMessage = error
                onResult(false)
            }
        }
    }
}
