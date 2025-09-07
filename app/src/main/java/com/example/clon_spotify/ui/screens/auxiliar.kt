package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.clon_spotify.R
@Composable
fun SocialLoginButton(
    iconRes: Int?,
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, Color.White),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp), // Espacio fijo para alinear íconos
                contentAlignment = Alignment.Center
            ) {
                if (iconRes != null) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = text,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
@Composable
fun LoginButtons(onLoginClick: () -> Unit, iconRes: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SocialLoginButton(
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
            iconRes = iconRes,
            text = "Iniciar sesión con Apple",
            onClick = { /* TODO */ }
        )
        SocialLoginButton(
            iconRes = null,
            text = "Iniciar sesión con número de teléfono",
            onClick = { /* TODO */ }
        )
    }
}

