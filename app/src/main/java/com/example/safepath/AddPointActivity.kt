package com.example.safepath

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore

class AddPointActivity : AppCompatActivity() {

    private lateinit var buttonRegreso: Button
    private lateinit var testViewtitleAddPoint: TextView
    private lateinit var textViewTitleDescAddPoint: TextView
    private lateinit var textViewDescLayoutAddPoint: TextView
    private lateinit var buttonAgregarPunto: Button
    private lateinit var firestore: FirebaseFirestore

    // Coordenadas quemadas de ejemplo.  En una app real, esto vendría de un mapa o un selector de ubicación.
    private val puntoFijo = LatLng(20.704657028467377, -100.44353167532229)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_point)

        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance()

        // Inicializar vistas con los IDs correctos
        buttonRegreso = findViewById(R.id.buttonRegreso)
        testViewtitleAddPoint = findViewById(R.id.testViewtitleAddPoint)
        textViewTitleDescAddPoint = findViewById(R.id.textViewTitleDescAddPoint)
        textViewDescLayoutAddPoint = findViewById(R.id.textViewDescLayoutAddPoint)
        buttonAgregarPunto = findViewById(R.id.buttonAgregarPunto)

        // Configurar OnClickListener para el botón Agregar
        buttonAgregarPunto.setOnClickListener {
            guardarPuntoEnFirestore()
        }

        //Configurar OnClickListener para el boton de regreso
        buttonRegreso.setOnClickListener{
            finish()
        }
    }

    private fun guardarPuntoEnFirestore() {
        // Como no hay campos de entrada en el XML que proporcionaste, estos darán error.
        // Debes agregar EditTexts a tu layout para que esto funcione.
        // Por ahora, usaré valores quemados para que el resto del código se ejecute.

        val title = "Título de prueba" //etTitle.text.toString().trim()
        val description = "Descripción de prueba" //etDescription.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa título y descripción", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear un mapa con los datos del punto
        val punto = hashMapOf(
            "title" to title,
            "description" to description,
            "location" to com.google.firebase.firestore.GeoPoint(puntoFijo.latitude, puntoFijo.longitude), // Usar el punto fijo
            "type" to "peligroso" // Puedes cambiar esto, o añadir un campo para el tipo.
        )

        // Añadir el punto a la colección "points" en Firestore
        firestore.collection("points")
            .add(punto)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Punto guardado con ID: ${documentReference.id}", Toast.LENGTH_SHORT).show()
                finish() // Cerrar la actividad después de guardar
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar punto: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }
}
