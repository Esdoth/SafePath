package com.example.safepath.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LocationCard(
    latitude: Double,
    longitude: Double,
    type: String,
    pointId: String,
    onDeleteSuccess: () -> Unit
) {
    val db = Firebase.firestore
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialogo de confirmación
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                deletePoint(db, pointId, coroutineScope, onDeleteSuccess, context)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado con botón de eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍Punto",
                    fontSize = 18.sp,
                    color = Color(0xFF0D47A1),
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar punto",
                        tint = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Información del punto
            PointInfoRow(label = "Latitud:", value = "%.4f".format(latitude))
            Spacer(modifier = Modifier.height(8.dp))
            PointInfoRow(label = "Longitud:", value = "%.4f".format(longitude))
            PointInfoRow(label = "Tipo:", value = type)
        }
    }
}

@Composable
private fun PointInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value, color = Color.Gray)
    }
}

private fun deletePoint(
    db: FirebaseFirestore,
    pointId: String,
    coroutineScope: CoroutineScope,
    onSuccess: () -> Unit,
    context: android.content.Context
) {
    coroutineScope.launch {
        db.collection("points").document(pointId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                android.widget.Toast.makeText(
                    context,
                    "Error al eliminar: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar eliminación") },
        text = { Text("¿Estás seguro de que quieres eliminar este punto?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview
@Composable
fun PreviewLocationCard() {
    LocationCard(
        latitude = 40.7128,
        longitude = -74.0060,
        type = "bache",
        pointId = "testId123",
        onDeleteSuccess = {}
    )
}