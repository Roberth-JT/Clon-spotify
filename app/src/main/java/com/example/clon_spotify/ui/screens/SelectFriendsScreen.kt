package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.clon_spotify.viewmodel.FriendsViewModel
import com.example.clon_spotify.viewmodel.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFriendsScreen(
    navController: NavController,
    onBackClick: () -> Unit,
    friendsViewModel: FriendsViewModel = viewModel()
) {
    val allUsers by friendsViewModel.allUsers.collectAsState()
    val selectedUsers by friendsViewModel.selectedUsers.collectAsState()

    // Cargar usuarios cuando la pantalla se abre
    LaunchedEffect(Unit) {
        friendsViewModel.loadAllUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Seleccionar amigos", color = Color.White, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        bottomBar = {
            if (selectedUsers.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    color = Color(0xFF181818),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${selectedUsers.size} amigos seleccionados",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )

                        Button(
                            onClick = {
                                friendsViewModel.sendInvitations(
                                    onSuccess = {
                                        // Volver a la pantalla anterior después de enviar
                                        onBackClick()
                                    },
                                    onError = { error ->
                                        // Manejar error (podrías mostrar un snackbar)
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1ED760))
                        ) {
                            Text("Enviar invitaciones", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (allUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay usuarios disponibles", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(allUsers) { user ->
                        UserItem(
                            user = user,
                            isSelected = selectedUsers.contains(user.uid),
                            onToggleSelection = { friendsViewModel.toggleUserSelection(user.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
        onClick = onToggleSelection
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar del usuario
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF535353)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Usuario",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = user.nombre,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = user.email,
                        color = Color(0xFFB3B3B3),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Checkbox de selección
            Surface(
                shape = CircleShape,
                color = if (isSelected) Color(0xFF1ED760) else Color.Transparent,
                border = if (!isSelected) CardDefaults.outlinedCardBorder() else null
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = if (isSelected) "Seleccionado" else "No seleccionado",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(4.dp),
                    tint = if (isSelected) Color.Black else Color.Transparent
                )
            }
        }
    }
}