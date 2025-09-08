package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clon_spotify.R


@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
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
            var email by remember { mutableStateOf("") }

            // Logo Spotify
            Image(
                painter = painterResource(id = R.drawable.spotify_logo),
                contentDescription = "Logo Spotify",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            // Título
            Text(
                text = "Inicia sesión en Spotify",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                lineHeight = 28.sp
            )

            // Botones sociales (Google, Facebook, Apple, Teléfono)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SocialLoginButton (
                    iconRes = R.drawable.google_logo,
                    text = "Continuar con Google",
                    onClick = onLoginClick
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
                SocialLoginButton (
                    iconRes = null,
                    text = "Iniciar sesión con número de teléfono",
                    onClick = { /* TODO */ }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Campo de texto (email/usuario)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
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


            // Botón Continuar
            Button(
                onClick = { onLoginClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Continuar", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            //Texto: No tienes cuenta
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "¿No tienes cuenta?",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
                TextButton(onClick = { onRegisterClick() }) {
                    Text(
                        text = "Suscríbete a Spotify",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
