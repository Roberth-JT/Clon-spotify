package com.example.clon_spotify.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clon_spotify.viewmodel.PhoneAuthViewModel

@Composable
fun PhoneLoginScreen(
    viewModel: PhoneAuthViewModel,
    navController: NavController
) {
    var phoneNumber by remember { mutableStateOf("") }
    val activity = LocalContext.current as Activity

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

            Text("Inicia sesión con tu número de teléfono", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Número de teléfono (+51...)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { if (phoneNumber.isNotBlank()) {
                    if (phoneNumber == "+51902250258") {
                        // Número de prueba
                        viewModel.verificationId = "TEST_VERIFICATION_ID"
                        viewModel.isCodeSent = true
                        viewModel.authMessage = "Número de prueba detectado, ingresa el OTP: 123456"
                    } else {
                        // Número real
                        viewModel.sendVerificationCode(phoneNumber, activity)
                    }
                }
                },
                modifier = Modifier.fillMaxWidth()

            ) {
                Text("Enviar OTP")
            }

            if (viewModel.authMessage.isNotBlank()) {
                Text(viewModel.authMessage, color = MaterialTheme.colorScheme.error)
            }

            // Navegar a la pantalla OTP cuando se envió el código
            LaunchedEffect(viewModel.isCodeSent) {
                if (viewModel.isCodeSent) {
                    navController.navigate("verify_otp")
                }
            }
        }
    }
}
