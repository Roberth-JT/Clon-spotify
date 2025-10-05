package com.example.clon_spotify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.clon_spotify.navigation.NavGraph
import com.example.clon_spotify.ui.theme.ClonspotifyTheme
import com.example.clon_spotify.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ClonspotifyTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    // Llamada correcta al NavGraph con parámetros
                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel
                    )

                }
            }
        }
    }
}
