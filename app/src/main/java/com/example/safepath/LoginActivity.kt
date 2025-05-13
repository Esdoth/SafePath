package com.example.safepath

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView // Import necesario para TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.safepath.databinding.ActivityLoginBinding
import android.widget.Toast // Importa la clase Toast si quieres usarla

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Verificar si el usuario ya ha iniciado sesión (comentado para forzar login)
        /*
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("authToken", null)

        if (authToken != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        */

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmailAddress.text.toString()
            val password = binding.editTextPassword.text.toString()
            attemptLogin(email, password)
        }

        // Hacer el TextView de registro clickable
        binding.textViewRegistro.isClickable = true
        binding.textViewRegistro.setOnClickListener {
            // Crear un Intent para iniciar la actividad de registro
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun attemptLogin(email: String, password: String) {
        // Simulación de inicio de sesión exitoso
        if (email == "test@example.com" && password == "password") {
            val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
            // sharedPreferences.edit().putString("authToken", "fake_token").apply() // Comentado para forzar login
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Mostrar mensaje de error de inicio de sesión fallido en el TextView
            binding.textViewError.text = "Credenciales incorrectas"
            // Opcionalmente, podrías limpiar los campos si lo deseas:
            // binding.editTextEmailAddress.text.clear()
            // binding.editTextPassword.text.clear()
        }
    }

    private fun simulateLoginFailure() {
        // Aquí puedes implementar el comportamiento que desees para un fallo de login
        Toast.makeText(this, "Simulando fallo de inicio de sesión", Toast.LENGTH_LONG).show()
        // Opcionalmente, podrías limpiar los campos de email y contraseña:
        // binding.editTextEmailAddress.text.clear()
        // binding.editTextPassword.text.clear()
        // O mostrar un mensaje en un TextView si lo agregas al layout.
    }
}