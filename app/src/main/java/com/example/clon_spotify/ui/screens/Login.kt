package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},     //   para navegar a Home
    onRegisterClick: () -> Unit = {}   //  para navegar a Registro
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

            // Título
            Text(
                text = "Inicia sesión en Spotify",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 24.dp),
                lineHeight = 28.sp
            )

            // Botón: Continuar con Google
            Button(
                onClick = { onLoginClick() },  // ✅ Este ya funcionaba
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Continuar con Google", color = Color.White, fontWeight = FontWeight.Bold)
            }

            //Botón: Iniciar con Facebook
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Iniciar sesión con Facebook", color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Botón: Iniciar con Apple
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Iniciar sesión con Apple", color = Color.White, fontWeight = FontWeight.Bold)
            }

            //  Botón: Iniciar con número de teléfono
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Iniciar sesión con número de teléfono", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Texto encima del input
            Text(
                text = "Correo electrónico o nombre de usuario",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 1.dp)
            )

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

            //  Botón: Continuar (el que no funcionaba antes)
            Button(
                onClick = { onLoginClick() },  // ✅ Corregido aquí
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Continuar", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            // Link para ir a registro
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
