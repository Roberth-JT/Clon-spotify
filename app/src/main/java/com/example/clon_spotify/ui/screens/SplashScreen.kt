package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.clon_spotify.R

@Composable
fun SplashScreen(navController: NavController) {
    // ⏱ Estado de temporizador
    LaunchedEffect(Unit) {
        delay(4000L) // 4 segundos
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true } // elimina splash del backstack
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo de Spotify
            Image(
                painter = painterResource(id = R.drawable.spotify_logo), // reemplaza con tu logo
                contentDescription = "Logo Spotify",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Texto de bienvenida
            Text(
                text = "Bienvenido a Spotify",
                color = Color.White,
                fontSize = 24.sp,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}