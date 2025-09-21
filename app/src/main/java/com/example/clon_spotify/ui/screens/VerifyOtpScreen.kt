package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clon_spotify.viewmodel.PhoneAuthViewModel

@Composable
fun VerifyOtpScreen(
    viewModel: PhoneAuthViewModel,
    navController: NavController
) {
    var otpCode by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Ingresa el código OTP", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("Código OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { if (otpCode.isNotBlank()) viewModel.verifyCode(otpCode) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Verificar")
            }

            if (viewModel.authMessage.isNotBlank()) {
                Text(viewModel.authMessage, color = MaterialTheme.colorScheme.error)
            }

            // Navegar a Home si se autenticó correctamente
            LaunchedEffect(viewModel.isAuthSuccess) {
                if (viewModel.isAuthSuccess) {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }
    }
}
