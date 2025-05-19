package com.example.safepath.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.safepath.databinding.FragmentMisPuntosBinding
import com.example.safepath.ui.componentes.LocationCard
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

                // Observador de Firestore filtrado por usuario
                LaunchedEffect(userEmail) {
                    if (userEmail.isNotEmpty()) {
                        db.collection("points")
                            .whereEqualTo("user", userEmail) // Filtra por el email del usuario
                            .get()
                            .addOnSuccessListener { documents ->
                                val mappedPoints = documents.map { doc ->
                                    PointData(
                                        location = doc.getGeoPoint("location") ?: GeoPoint(0.0, 0.0),
                                        type = doc.getString("type") ?: "Desconocido",
                                        user = doc.getString("user") ?: ""
                                    )
                                }
                                points = mappedPoints
                            }
                    }
                }

                MaterialTheme {
                    if (userEmail.isEmpty()) {
                        // Muestra un mensaje si no hay usuario autenticado
                        Column {
                            Text("No hay usuario autenticado")
                        }
                    } else {
                        PointsList(points = points)
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
fun PointsList(points: List<PointData>) {
    if (points.isEmpty()) {
        Column {
            Text("No hay puntos registrados")
        }
    } else {
        LazyColumn {
            items(points) { point ->
                LocationCard(
                    latitude = point.location.latitude,
                    longitude = point.location.longitude,
                    type = point.type
                )
            }
        }
    }
}

data class PointData(
    val location: GeoPoint,
    val type: String,
    val user: String = ""
)