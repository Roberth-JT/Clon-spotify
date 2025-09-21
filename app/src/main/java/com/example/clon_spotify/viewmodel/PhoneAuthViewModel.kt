package com.example.clon_spotify.viewmodel

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneAuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var isLoading by mutableStateOf(false)
    var authMessage by mutableStateOf("")
    var verificationId by mutableStateOf("")
    var isCodeSent by mutableStateOf(false)
    var isAuthSuccess by mutableStateOf(false)

    //  Múltiples números de prueba con su OTP
    private val testNumbers = mapOf(
        "+51902250258" to "244422",
        "+51987654321" to "123456"
    )

    //  Enviar código OTP al teléfono
    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        isLoading = true
        authMessage = ""

        // Verificar si es un número de prueba
        testNumbers[phoneNumber]?.let { otp ->
            verificationId = "TEST_VERIFICATION_ID"
            isCodeSent = true
            isLoading = false
            authMessage = "Número de prueba detectado, ingresa el OTP: $otp"
            return
        }

        // Número real, enviar SMS
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verificación completada
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    isLoading = false
                    authMessage = "Error al enviar OTP: ${e.localizedMessage}"
                    Log.e("PhoneAuth", "onVerificationFailed", e)
                }

                override fun onCodeSent(
                    verifId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    isLoading = false
                    verificationId = verifId
                    isCodeSent = true
                    Log.d("PhoneAuth", "Código enviado correctamente")
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // 🔹 Verificar OTP ingresado por el usuario
    fun verifyCode(code: String) {
        isLoading = true
        authMessage = ""

        // OTP de prueba
        if (verificationId == "TEST_VERIFICATION_ID") {
            val expectedOtp = testNumbers.values.firstOrNull { it == code }
            if (expectedOtp != null) {
                isAuthSuccess = true
                authMessage = "Autenticación exitosa (número de prueba)"
            } else {
                isAuthSuccess = false
                authMessage = "Código OTP incorrecto"
            }
            isLoading = false
            return
        }

        // OTP real
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential)
    }

    //Sign in con el credential de Firebase
    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                isLoading = false
                isAuthSuccess = true
                authMessage = "Autenticación exitosa"
            }
            .addOnFailureListener { e ->
                isLoading = false
                isAuthSuccess = false
                authMessage = "Error al verificar OTP: ${e.localizedMessage}"
                Log.e("PhoneAuth", "signInWithCredential", e)
            }
    }

    fun logout() {
        auth.signOut()
        isAuthSuccess = false
        verificationId = ""
        isCodeSent = false
    }
}
