package com.example.safepath.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.safepath.databinding.FragmentMisPuntosBinding
import com.example.safepath.ui.componentes.LocationCard
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class DashboardFragment : Fragment() {
    private var _binding: FragmentMisPuntosBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMisPuntosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                var points by remember { mutableStateOf<List<PointData>>(emptyList()) }
                val currentUser = auth.currentUser
                val userEmail = currentUser?.email ?: ""

                // Función para recargar los puntos
                fun loadPoints() {
                    if (userEmail.isNotEmpty()) {
                        db.collection("points")
                            .whereEqualTo("user", userEmail)
                            .get()
                            .addOnSuccessListener { documents ->
                                val mappedPoints = documents.map { doc ->
                                    PointData(
                                        id = doc.id,
                                        location = doc.getGeoPoint("location") ?: GeoPoint(0.0, 0.0),
                                        type = doc.getString("type") ?: "Desconocido",
                                        user = doc.getString("user") ?: userEmail
                                    )
                                }
                                points = mappedPoints
                            }
                    }
                }

                // Cargar puntos inicialmente
                LaunchedEffect(userEmail) {
                    loadPoints()
                }

                // Manejar eliminación de puntos
                fun handleDeletePoint(pointId: String) {
                    db.collection("points").document(pointId)
                        .delete()
                        .addOnSuccessListener {
                            loadPoints() // Recargar la lista después de eliminar
                            Snackbar.make(binding.root, "Punto eliminado", Snackbar.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Snackbar.make(binding.root, "Error al eliminar: ${e.message}", Snackbar.LENGTH_SHORT).show()
                        }
                }

                MaterialTheme {
                    if (userEmail.isEmpty()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("No hay usuario autenticado")
                        }
                    } else {
                        PointsList(
                            points = points,
                            onDeletePoint = { pointId -> handleDeletePoint(pointId) }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@Composable
fun PointsList(
    points: List<PointData>,
    onDeletePoint: (String) -> Unit
) {
    if (points.isEmpty()) {
        Column(Modifier.padding(16.dp)) {
            Text("No hay puntos registrados")
        }
    } else {
        LazyColumn {
            items(points) { point ->
                LocationCard(
                    latitude = point.location.latitude,
                    longitude = point.location.longitude,
                    type = point.type,
                    pointId = point.id,
                    onDeleteSuccess = { onDeletePoint(point.id) }
                )
            }
        }
    }
}

data class PointData(
    val id: String,
    val location: GeoPoint,
    val type: String,
    val user: String
)