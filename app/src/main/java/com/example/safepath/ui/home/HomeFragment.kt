package com.example.safepath.ui.home

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.safepath.R
import com.example.safepath.databinding.FragmentHomeBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mMap: GoogleMap
    private var isMapReady = false
    private lateinit var placesClient: com.google.android.libraries.places.api.net.PlacesClient
    private var pendingLocationUpdate: Pair<LatLng, String?>? = null
    private var statusBarHeight = 0
    private val firestore = FirebaseFirestore.getInstance()
    private val markers = mutableListOf<Marker>()

    private val autocompleteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                result.data?.let { intent ->
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    updateLocationOnMap(place)
                }
            }
            AutocompleteActivity.RESULT_ERROR -> {
                result.data?.let { intent ->
                    val status = Autocomplete.getStatusFromIntent(intent)
                    showError(status.statusMessage ?: "Error desconocido")
                }
            }
            AppCompatActivity.RESULT_CANCELED -> {
                // Búsqueda cancelada por el usuario
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener altura de la status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            insets
        }

        initializePlacesApi()
        setupMap()
        setupSearchField()
    }

    private fun initializePlacesApi() {
        if (!Places.isInitialized()) {
            context?.applicationContext?.let {
                Places.initialize(it, getString(R.string.google_maps_key))
            }
        }
        placesClient = Places.createClient(requireContext())
    }

    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun setupSearchField() {
        binding.editTextSearch.apply {
            setOnClickListener { launchPlaceAutocomplete() }
            isFocusable = false
            isClickable = true
            isFocusableInTouchMode = false
        }
    }

    private fun launchPlaceAutocomplete() {
        try {
            val fields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )

            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN,
                fields
            )
                .setCountry("MX")
                .build(requireContext())

            // Añadir margen superior para evitar solapamiento
            intent.putExtra(
                "android.provider.extra.INITIAL_INTENTS",
                Intent().apply {
                    putExtra("offset", Point(0, statusBarHeight + 50)) // Margen adicional de 50px
                }
            )

            autocompleteLauncher.launch(intent)
        } catch (e: Exception) {
            showError("Error al iniciar la búsqueda: ${e.localizedMessage}")
        }
    }

    private fun updateLocationOnMap(place: Place) {
        place.latLng?.let { latLng ->
            binding.editTextSearch.setText(place.name ?: place.address)

            if (isMapReady) {
                showLocationOnMap(latLng, place.name)
            } else {
                pendingLocationUpdate = Pair(latLng, place.name)
            }
        } ?: showError("Ubicación no disponible")
    }

    private fun showLocationOnMap(latLng: LatLng, title: String?) {
        try {
            mMap.clear()
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(title ?: "Ubicación seleccionada")
            )
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        } catch (e: Exception) {
            showError("Error al mostrar ubicación en el mapa")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isMapReady = true

        val defaultLocation = LatLng(20.704657028467377, -100.44353167532229)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        // Cargar puntos de Firestore
        loadPointsFromFirestore()

        pendingLocationUpdate?.let { (latLng, name) ->
            showLocationOnMap(latLng, name)
            pendingLocationUpdate = null
        }
    }

    private fun loadPointsFromFirestore() {
        lifecycleScope.launch {
            firestore.collection("points")
                .get()
                .addOnSuccessListener { documents ->
                    clearAllMarkers()

                    for (document in documents) {
                        val location = document.getGeoPoint("location")
                        val type = document.getString("type") ?: "unknown"
                        val title = document.getString("title") ?: "Desperfecto"

                        location?.let { geoPoint ->
                            val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
                            addMarkerToMap(latLng, title, type)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    showError("Error al cargar puntos: ${e.localizedMessage}")
                }
        }
    }

    private fun addMarkerToMap(latLng: LatLng, title: String, type: String) {
        val markerColor = when (type.toLowerCase()) {
            "peligroso" -> BitmapDescriptorFactory.HUE_RED
            "seguro" -> BitmapDescriptorFactory.HUE_GREEN
            "advertencia" -> BitmapDescriptorFactory.HUE_ORANGE
            else -> BitmapDescriptorFactory.HUE_BLUE
        }

        val marker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet("Tipo: $type")
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
        )

        marker?.let { markers.add(it) }
    }

    private fun clearAllMarkers() {
        markers.forEach { it.remove() }
        markers.clear()
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}