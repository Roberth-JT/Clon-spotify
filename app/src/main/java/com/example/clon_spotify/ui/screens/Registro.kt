package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistroScreen(
    onRegisterClick: (String) -> Unit = {},
    onBackToLoginClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Título
            Text(
                text = "Regístrate para empezar a escuchar contenido",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 40.dp, bottom = 32.dp)
                    .fillMaxWidth()
            )
            Text(
                text = "Dirección de correo electrónico",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp) // 👈 casi sin separación
            )
            //Campo Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("nombre@dominio.com", color = Color.Gray) },
                label = { Text("Dirección de correo electrónico") },
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

            Spacer(modifier = Modifier.height(10.dp))

            // Texto: usar número de teléfono
            TextButton(onClick = { /* TODO */ }) {
                Text(
                    "Usa un número de teléfono en su lugar.",
                    color = Color.White
                )
            }
            // Botón continuar con correo
            Button(
                onClick = { onRegisterClick(email) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Siguiente", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Separador "o"
            Text(
                "            O       ",
                color = Color.White,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            //Botón continuar con google
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Iniciar sesión con Google", color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Botón iniciar con Apple
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) {
                Text("Iniciar sesión con Apple", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            //Texto y botón centrados
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Ya tienes una cuenta?",
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                TextButton(onClick = { onBackToLoginClick() }) {
                    Text("Iniciar sesión", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

        }
    }
}
