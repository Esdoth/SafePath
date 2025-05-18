package com.example.safepath.ui.componentes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LocationCard(latitude: Double, longitude: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD) // Fondo azul claro
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📍Punto",
                fontSize = 18.sp,
                color = Color(0xFF0D47A1),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Latitud:",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "%.4f".format(latitude),
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Longitud:",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "%.4f".format(longitude),
                    color = Color.Gray
                )
            }
        }
    }
}

// Ejemplo de uso:
@Preview
@Composable
fun PreviewLocationCard() {
    LocationCard(
        latitude = 40.7128,
        longitude = -74.0060
    )
}