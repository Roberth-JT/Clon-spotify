package com.example.clon_spotify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowRow
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.clon_spotify.R



//  Pantalla principal
@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        containerColor = Color.Black,
        bottomBar = { BottomNavigationBar() } //  Implementa barra inferior
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
        ) {
            item { TopFilters() }
            item { QuickAccessPlaylists() }
            item { CreatedForYou() }
            item { RecentsSection() }
            item { MoreOfWhatYouLike() }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
//barra inferior flotante
@Composable
fun BottomNavigationBar() {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = { /* nav inicio */ },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* nav búsqueda */ },
            icon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            label = { Text("Buscar") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* nav biblioteca */ },
            icon = { Icon(Icons.Default.List, contentDescription = "Tu biblioteca") },
            label = { Text("Tu biblioteca") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* nav crear */ },
            icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
            label = { Text("Crear") }
        )
    }
}


//  Filtros superiores
@Composable
fun TopFilters() {
    var expandedFilter by remember { mutableStateOf<String?>(null) }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        mainAxisSpacing = 10.dp,
        crossAxisSpacing = 10.dp
    ) {
        val filters = listOf("Todas", "Música", "Podcasts")

        filters.forEach { label ->
            if (label == "Todas") {
                Button(
                    onClick = { expandedFilter = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(label)
                }
            } else {
                Row {
                    Button(
                        onClick = {
                            expandedFilter = if (expandedFilter == label) null else label
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(topStart = 15.dp, bottomStart = 15.dp)
                    ) {
                        Text(label)
                    }

                    if (expandedFilter == label) {
                        Button(
                            onClick = {
                                // Acción al presionar "Siguiendo"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(topEnd = 15.dp, bottomEnd = 15.dp)
                        ) {
                            Text("Siguiendo")
                        }
                    }
                }
            }
        }
    }
}

// Accesos rápidos
@Composable
fun QuickAccessPlaylists() {
    val playlists = listOf(
        Pair(R.drawable.adele, "ADELE - ÉXITOS"),
        Pair(R.drawable.luism, "Luis Miguel - Lo Mejor"),
        Pair(R.drawable.morat, "Morat Todas Las Canciones"),
        Pair(R.drawable.rock_clasico, "Rock Clásico"),
        Pair(R.drawable.jesse, "Jesse & Joy"),
        Pair(R.drawable.music_2000, "Lo mejor de los 2000"),
        Pair(R.drawable.pedrosv, "Pedro Suárez Vértiz")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        playlists.chunked(2).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                rowItems.forEach { (imageRes, playlistName) ->
                    Surface(
                        color = Color.DarkGray,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f) // tarjetas iguales
                            .height(70.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = playlistName,
                                modifier = Modifier
                                    .size(70.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = playlistName,
                                color = Color.White,
                                fontSize = 15.sp,
                                maxLines = 2
                            )
                        }
                    }
                }

                // una sola tarjeta
                if (rowItems.size == 1) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .height(70.dp)
                    )
                }
            }
        }
    }
}



// Creado para ti
@Composable
fun CreatedForYou() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Creado para ti",
            color = Color.White,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(listOf(R.drawable.luis_mix, R.drawable.bruno_mix)) { imageRes ->
                Column {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(140.dp)
                            .background(Color.Gray) // opcional, para placeholder
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Mix diario", color = Color.White)
                    Text("Artistas variados", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }
    }
}

// Recientes
@Composable
fun RecentsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Recientes",
            color = Color.White,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Lista de imágenes recientes
            items(
                listOf(
                    R.drawable.adele,
                    R.drawable.morat,
                    R.drawable.luism,
                    R.drawable.pedrosv
                )
            ) { imageRes ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.DarkGray)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Playlist", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}


// Más de lo que te gusta
@Composable
fun MoreOfWhatYouLike() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Más de lo que te gusta",
            color = Color.White,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Lista de imágenes sugeridas (coloca las tuyas en drawable)
            items(
                listOf(
                    R.drawable.morat,
                    R.drawable.adele_mas,
                    R.drawable.luis_mix
                )
            ) { imageRes ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(140.dp)
                            .background(Color.DarkGray) // opcional como placeholder
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sugerencia",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Artistas varios",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

