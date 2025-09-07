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
@Composable
fun BottomNavigationBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = Color.DarkGray
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("biblioteca", color = Color.White)
        }
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
    LazyRow(
        modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(5) { index ->
            Surface(
                color = Color.DarkGray,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(140.dp, 60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Playlist $index", color = Color.White)
                }
            }
        }
    }
}

// Creado para ti
@Composable
fun CreatedForYou() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Creado para ti", color = Color.White, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(2) { index ->
                Column {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(Color.Gray)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Mix diario ${index + 4}", color = Color.White)
                    Text("Artistas varios", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        }
    }
}

// Recientes
@Composable
fun RecentsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Recientes", color = Color.White, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(4) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Playlist", color = Color.White)
                }
            }
        }
    }
}

// Más de lo que te gusta
@Composable
fun MoreOfWhatYouLike() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Más de lo que te gusta", color = Color.White, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(3) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sugerencia", color = Color.White)
                }
            }
        }
    }
}
