package com.example.safepath

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import com.example.safepath.ui.componentes.LocationCard

class MyPointsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_mis_puntos)

        val composeView = findViewById<ComposeView>(R.id.composeView)
        composeView.setContent {
            MaterialTheme {
                LocationCard(
                    latitude = 19.4326,  // Ejemplo: CDMX
                    longitude = -99.1332,
                    type = "bache"
                )
            }
        }
    }
}