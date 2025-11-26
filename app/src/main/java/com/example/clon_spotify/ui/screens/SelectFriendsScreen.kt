package com.example.clon_spotify.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
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
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(Unit) {
        friendsViewModel.loadAllUsers()
    }

    // Filtrado de usuarios local (por nombre o email)
    val filteredUsers = remember(searchQuery.text, allUsers) {
        if (searchQuery.text.isBlank()) {
            emptyList() // No mostrar nada si no hay búsqueda
        } else {
            allUsers.filter { user ->
                user.nombre.contains(searchQuery.text, ignoreCase = true) ||
                        user.email.contains(searchQuery.text, ignoreCase = true)
            }
        }
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
                                    onError = { /* mostrar Snackbar si quieres */ }
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
            // 🔹 Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("¿A quién quieres buscar hoy?", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1ED760),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            if (searchQuery.text.isBlank()) {
                // Si no hay búsqueda activa, mostrar un mensaje tipo Spotify
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Escribe el nombre o correo del usuario que deseas buscar",
                        color = Color.Gray
                    )
                }
            } else if (filteredUsers.isEmpty()) {
                // Si no hay coincidencias
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontraron resultados", color = Color.Gray)
                }
            } else {
                // Mostrar resultados filtrados
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(filteredUsers) { user ->
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
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
