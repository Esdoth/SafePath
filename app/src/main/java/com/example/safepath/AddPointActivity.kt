package com.example.safepath

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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

class AddPointActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var buttonRegreso: Button
    private lateinit var testViewtitleAddPoint: TextView
    private lateinit var textViewTitleDescAddPoint: TextView
    private lateinit var textViewDescLayoutAddPoint: TextView
    private lateinit var buttonAgregarPunto: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var editTextSearchAddPoint: EditText
    private lateinit var placesClient: com.google.android.libraries.places.api.net.PlacesClient
    private var selectedLatLng: LatLng? = null
    private var statusBarHeight = 0
    private lateinit var mMap: GoogleMap // Google Map object
    private var isMapReady = false
    private var pendingLocationUpdate: LatLng? = null

    private val autocompleteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    result.data?.let { intent ->
                        val place = Autocomplete.getPlaceFromIntent(intent)
                        updateLocation(place)
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    result.data?.let { intent ->
                        val status = Autocomplete.getStatusFromIntent(intent)
                        showError(status.statusMessage ?: "Error desconocido")
                    }
                }
                RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_point)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        buttonRegreso = findViewById(R.id.buttonRegreso)
        testViewtitleAddPoint = findViewById(R.id.testViewtitleAddPoint)
        textViewTitleDescAddPoint = findViewById(R.id.textViewTitleDescAddPoint)
        textViewDescLayoutAddPoint = findViewById(R.id.textViewDescLayoutAddPoint)
        buttonAgregarPunto = findViewById(R.id.buttonAgregarPunto)
        editTextSearchAddPoint = findViewById(R.id.editTextSearchAddPoint)

        // Initialize Places API
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)

        // Get status bar height
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }

        // Setup the search field
        setupSearchField()

        // Setup the map
        setupMap()

        // Configure OnClickListener for the Agregar button
        buttonAgregarPunto.setOnClickListener {
            guardarPuntoEnFirestore()
        }

        // Configure OnClickListener for the back button
        buttonRegreso.setOnClickListener {
            finish()
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    private fun setupSearchField() {
        editTextSearchAddPoint.apply {
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
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .setCountry("MX")  // Restrict to Mexico
                .build(this)

            // Add top margin to avoid overlap
            intent.putExtra(
                "android.provider.extra.INITIAL_INTENTS",
                Intent().apply {
                    putExtra("offset", Point(0, statusBarHeight + 50))
                }
            )
            autocompleteLauncher.launch(intent)
        } catch (e: Exception) {
            showError("Error launching autocomplete: ${e.message}")
        }
    }

    private fun updateLocation(place: Place) {
        selectedLatLng = place.latLng
        editTextSearchAddPoint.setText(place.name ?: place.address)

        // Update the map if it's ready
        if (isMapReady) {
            showLocationOnMap(selectedLatLng)
        } else {
            pendingLocationUpdate = selectedLatLng;
        }
    }

    private fun showLocationOnMap(latLng: LatLng?) {
        latLng?.let {
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(it).title("Ubicación Seleccionada"))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isMapReady = true

        //show initial location
        val defaultLocation = LatLng(20.704657028467377, -100.44353167532229)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))

        if(pendingLocationUpdate != null){
            showLocationOnMap(pendingLocationUpdate)
            pendingLocationUpdate = null
        }
    }

    private fun guardarPuntoEnFirestore() {
        val title = "Título de prueba" // etTitle.text.toString().trim()
        val description = "Descripción de prueba" // etDescription.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa título y descripción", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedLatLng == null) {
            Toast.makeText(this, "Por favor, selecciona una ubicación", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a map with the point data
        val punto = hashMapOf(
            "title" to title,
            "description" to description,
            "location" to com.google.firebase.firestore.GeoPoint(
                selectedLatLng!!.latitude,
                selectedLatLng!!.longitude
            ),
            "type" to "peligroso"
        )

        // Add the point to the "points" collection in Firestore
        firestore.collection("points")
            .add(punto)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(
                    this,
                    "Punto guardado con ID: ${documentReference.id}",
                    Toast.LENGTH_SHORT
                ).show()
                finish() // Close the activity after saving
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar punto: ${e.localizedMessage}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

