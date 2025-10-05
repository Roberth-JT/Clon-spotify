package com.example.clon_spotify.utils


import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreSeeder {
    private const val PREFS = "app_prefs"
    private const val KEY_SEEDED = "firestore_seeded"

    fun seedIfNeeded(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) {
            Log.d("Seeder", "Already seeded")
            return
        }

        val db = FirebaseFirestore.getInstance()

        // Example songs
        val songs = listOf(
            hashMapOf("id" to "s1", "titulo" to "Volví a nacer", "artista" to "Carlos Vives", "imagenUrl" to "https://i.scdn.co/image/ab67616d0000b27317cb72f4c671f0b86e8a9d22", "duracion" to "3:45"),
            hashMapOf("id" to "s2", "titulo" to "Hello", "artista" to "Adele", "imagenUrl" to "https://i.scdn.co/image/ab67616d0000b273abcdef1234567890", "duracion" to "4:55"),
            hashMapOf("id" to "s3", "titulo" to "Dance All Night", "artista" to "DJ Party", "imagenUrl" to "https://i.scdn.co/image/ab67616d0000b273b1a4d97f18dfe9a73d3a50f7", "duracion" to "3:20")
        )

        // Write songs collection
        val songsColl = db.collection("canciones")
        val batch = db.batch()
        songs.forEach { s ->
            val doc = songsColl.document(s["id"] as String)
            batch.set(doc, s)
        }

        // Example playlists
        val playlists = listOf(
            hashMapOf(
                "id" to "p1",
                "nombre" to "Mix Favoritos",
                "descripcion" to "Creado para ti",
                "imagenUrl" to "https://i.scdn.co/image/ab67616d0000b27317cb72f4c671f0b86e8a9d22",
                "creador" to "system",
                "tipo" to "sugerido",
                "canciones" to listOf("s1","s2")
            ),
            hashMapOf(
                "id" to "p2",
                "nombre" to "Los me gusta",
                "descripcion" to "Canciones marcadas como me gusta",
                "imagenUrl" to "https://cdn-icons-png.flaticon.com/512/833/833472.png",
                "creador" to "system",
                "tipo" to "meGusta",
                "canciones" to listOf("s1")
            )
        )

        val playlistsColl = db.collection("playlists")
        playlists.forEach { p ->
            val doc = playlistsColl.document(p["id"] as String)
            batch.set(doc, p)
        }

        // Example home sections
        val sections = listOf(
            hashMapOf("id" to "sec1", "titulo" to "Viernes de lanzamientos", "playlists" to listOf("p1","p2")),
            hashMapOf("id" to "sec2", "titulo" to "Fiesta", "playlists" to listOf("p1"))
        )
        val sectionsColl = db.collection("seccionesHome")
        sections.forEach { s ->
            val doc = sectionsColl.document(s["id"] as String)
            batch.set(doc, s)
        }

        // Commit batch
        batch.commit()
            .addOnSuccessListener {
                prefs.edit().putBoolean(KEY_SEEDED, true).apply()
                Log.d("Seeder", "Firestore seeded successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Seeder", "Error seeding firestore: ${e.localizedMessage}", e)
            }
    }
}
