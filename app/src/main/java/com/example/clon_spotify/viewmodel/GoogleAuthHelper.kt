package com.example.clon_spotify.viewmodel


import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.clon_spotify.R

object GoogleAuthHelper {

    fun signInWithGoogle(
        context: Context,
        onResult: (success: Boolean, userId: String?, errorMessage: String?) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            credentialManager.getCredentialAsync(
                context,
                request,
                null,
                context.mainExecutor,
                object : androidx.credentials.CredentialManagerCallback<
                        androidx.credentials.GetCredentialResponse,
                        androidx.credentials.exceptions.GetCredentialException
                        > {
                    override fun onResult(result: androidx.credentials.GetCredentialResponse) {
                        val credential = result.credential
                        if (credential is CustomCredential &&
                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                            val idToken = googleCred.idToken
                            if (!idToken.isNullOrBlank()) {
                                val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                                auth.signInWithCredential(firebaseCred)
                                    .addOnSuccessListener { firebaseResult ->
                                        val uid = firebaseResult.user?.uid
                                        onResult(true, uid, null)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("GoogleAuthHelper", "Firebase signin failed", e)
                                        onResult(false, null, "Error de Firebase: ${e.localizedMessage}")
                                    }
                            } else {
                                onResult(false, null, "Id token inválido")
                            }
                        } else {
                            onResult(false, null, "Credencial tipo inválido")
                        }
                    }

                    override fun onError(e: androidx.credentials.exceptions.GetCredentialException) {
                        Log.e("GoogleAuthHelper", "Error credentialManager", e)
                        onResult(false, null, "Error obteniendo credencial: ${e.localizedMessage}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("GoogleAuthHelper", "Error inesperado en signInWithGoogle", e)
            onResult(false, null, "Error inesperado: ${e.localizedMessage}")
        }
    }
}