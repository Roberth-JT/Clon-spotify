package com.example.clon_spotify.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    friendsViewModel: FriendsViewModel = viewModel(),
) {
    val allUsers by friendsViewModel.allUsers.collectAsState()
    val selectedUsers by friendsViewModel.selectedUsers.collectAsState()

    LaunchedEffect(Unit) {
        friendsViewModel.loadAllUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar amigos", color = Color.White, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
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
                                    onSuccess = { onBackClick() },
                                    onError = { /* puedes mostrar Snackbar aquí */ }
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
                            friendsViewModel = friendsViewModel,
                            isSelected = selectedUsers.contains(user.uid),
                            onToggleSelection = { friendsViewModel.toggleUserSelection(user.uid) },
                            navController = navController
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
    friendsViewModel: FriendsViewModel,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    var isSeguidos by remember { mutableStateOf(false) }

    // Verificar si ya lo sigue
    LaunchedEffect(user.uid) {
        friendsViewModel.isSeguidos(user.uid) { isSeguidos = it }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818)),
        onClick = onToggleSelection
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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

            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 Botones de acción (ahora en columna)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Botón Seguir / Dejar de seguir
                Button(
                    onClick = {
                        if (!isSeguidos) {
                            friendsViewModel.Seguidos(
                                user,
                                onSuccess = {
                                    isSeguidos = true
                                    Toast.makeText(context, "Ahora sigues a ${user.nombre}", Toast.LENGTH_SHORT).show()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            friendsViewModel.dejarDeSeguir(
                                userId = user.uid,
                                onSuccess = {
                                    isSeguidos = false
                                    Toast.makeText(context, "Dejaste de seguir a ${user.nombre}", Toast.LENGTH_SHORT).show()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSeguidos) Color.Gray else Color(0xFF1ED760)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isSeguidos) "Dejar de seguir" else "Seguir",
                        color = if (isSeguidos) Color.White else Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 🔹 Botón Ir al perfil completo
                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Viendo el perfil de ${user.nombre}", Toast.LENGTH_SHORT).show()
                        navController.navigate("perfil/${user.uid}")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ir al perfil completo")
                }
            }
        }
    }
}
