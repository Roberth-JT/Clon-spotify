package com.example.clon_spotify.ui.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.clon_spotify.R
import com.example.clon_spotify.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    navController: NavController,
    onLoginSuccess: () -> Unit = {},
    onRegisterClick: () -> Unit = {}

) {
    var email by remember { mutableStateOf("") }
    var showPasswordField by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Obtener Activity para Phone Auth
    val activity = LocalContext.current as Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            // Logo Spotify
            Image(
                painter = painterResource(id = R.drawable.spotify_logo),
                contentDescription = "Logo Spotify",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Inicia sesión en Spotify",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                lineHeight = 28.sp
            )

            // Botones sociales
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SocialLoginButton(
                    iconRes = R.drawable.google_logo,
                    text = "Continuar con Google",
                    onClick = { /* TODO */ }
                )
                SocialLoginButton(
                    iconRes = R.drawable.facebook_logo,
                    text = "Iniciar sesión con Facebook",
                    onClick = { /* TODO */ }
                )
                SocialLoginButton(
                    iconRes = R.drawable.apple_logo,
                    text = "Iniciar sesión con Apple",
                    onClick = { /* TODO */ }
                )
                SocialLoginButton(
                    iconRes = null,
                    text = "Iniciar sesión con número de teléfono",
                    onClick = { navController.navigate("phone_login") }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Input de correo
            Text(
                text = "Correo electrónico o nombre de usuario",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 1.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    showPasswordField = it.isNotBlank()
                },
                label = { Text("Ingresa tu correo o usuario") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.Gray,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Campo de contraseña (solo si se escribió un correo)
            if (showPasswordField) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black,
                        unfocusedContainerColor = Color.Black,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.Gray,
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // Botón continuar (solo si se escribió contraseña)
            if (showPasswordField && password.isNotBlank()) {
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            viewModel.login(email, password) { success, error ->
                                if (success) onLoginSuccess()
                                else errorMessage = error
                            }
                        } else {
                            errorMessage = "Completa todos los campos"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Continuar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            errorMessage?.let {
                Text(it, color = Color.Red, fontSize = 14.sp)
            }

            // Texto inferior
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("¿No tienes cuenta?", color = Color.DarkGray, fontSize = 14.sp)
                TextButton(onClick = onRegisterClick) {
                    Text("Suscríbete a Spotify", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

