package com.example.clon_spotify.ui.screens


import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.clon_spotify.models.PlaylistUi
import com.example.clon_spotify.viewmodel.FriendsViewModel
import com.example.clon_spotify.viewmodel.PerfilUsuarioViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioScreen(
    userId: String,
    navController: NavController,
    viewModel: PerfilUsuarioViewModel = viewModel(),
    friendsViewModel: FriendsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    var playlists by remember { mutableStateOf<List<PlaylistUi>>(emptyList()) }
    var isSeguido by remember { mutableStateOf(false) }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isOwnProfile = userId == currentUserId
    val firestore = FirebaseFirestore.getInstance()

    // 🔹 Cargar perfil y playlists según el contexto
    LaunchedEffect(userId) {
        viewModel.cargarPerfil(userId)

        if (isOwnProfile) {
            firestore.collection("usuarios")
                .document(userId)
                .collection("playlists")
                .addSnapshotListener { snapshot, _ ->
                    playlists = snapshot?.toObjects(PlaylistUi::class.java) ?: emptyList()
                }
        } else {
            friendsViewModel.isSeguidos(userId) { seguido ->
                isSeguido = seguido
            }

            firestore.collection("usuarios")
                .document(userId)
                .collection("playlists")
                .whereEqualTo("isPublic", true)
                .addSnapshotListener { snapshot, _ ->
                    playlists = snapshot?.toObjects(PlaylistUi::class.java) ?: emptyList()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de usuario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            userProfile?.let { user ->
                Text(text = user.nombre, style = MaterialTheme.typography.headlineSmall)
                Text(text = user.email, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                if (!isOwnProfile) {
                    Button(
                        onClick = {
                            if (!isSeguido) {
                                friendsViewModel.Seguidos(
                                    user,
                                    onSuccess = {
                                        isSeguido = true
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
                                        isSeguido = false
                                        playlists = emptyList()
                                        Toast.makeText(context, "Dejaste de seguir a ${user.nombre}", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSeguido)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isSeguido) "Dejar de seguir" else "Seguir")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isOwnProfile || isSeguido) {
                    Text(
                        text = if (isOwnProfile) "Tus playlists" else "Playlists públicas",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (playlists.isEmpty()) {
                        Text(
                            text = if (isOwnProfile)
                                "Aún no tienes playlists creadas"
                            else
                                "Este usuario no tiene playlists públicas disponibles"
                        )
                    } else {
                        LazyColumn {
                            items(playlists) { playlist ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            navController.navigate("playlist/${playlist.id}")
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(playlist.title, style = MaterialTheme.typography.titleMedium)
                                        Text(playlist.description ?: "", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sigue a este usuario para ver sus playlists públicas 👀",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } ?: Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}