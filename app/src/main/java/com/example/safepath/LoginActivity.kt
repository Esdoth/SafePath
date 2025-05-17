package com.example.safepath

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView // Import necesario para TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.safepath.auth.CognitoAuthManager
import com.example.safepath.databinding.ActivityLoginBinding
import android.widget.Toast // Importa la clase Toast si quieres usarla
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authManager: CognitoAuthManager
    private lateinit var authLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa Firebase Auth
        auth = Firebase.auth

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar AuthManager
        authManager = CognitoAuthManager(this)

        authLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            result.data?.let { intent ->
                handleAuthResult(intent)
            } ?: run {
                binding.textViewError.text = "Error: No se recibieron datos de autenticación"
            }
        }

        lifecycleScope.launch {
            authManager.initialize()

            // Verificar si ya está autenticado
            if (authManager.authState.value.isAuthenticated) {
                navigateToMainActivity()
            }
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmailAddress.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                attemptLoginWithCognito(email, password)
            } else {
                binding.textViewError.text = "Por favor ingresa email y contraseña"
            }
        }

        binding.textViewRegistro.isClickable = true
        binding.textViewRegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun attemptLogin(email: String, password: String) {
        // Validación básica de campos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa email y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener instancia de Firebase Auth
        val auth = Firebase.auth

        // Mostrar progreso (si tienes un ProgressBar en tu layout con este ID)
        binding.progressBar?.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Ocultar progreso
                binding.progressBar?.visibility = View.GONE

                if (task.isSuccessful) {
                    // Login exitoso
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Cierra la Activity de login
                } else {
                    // Mostrar error
                    val errorMessage = when {
                        task.exception is FirebaseAuthInvalidUserException -> "Usuario no registrado"
                        task.exception is FirebaseAuthInvalidCredentialsException -> "Credenciales incorrectas"
                        else -> "Error al iniciar sesión: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Cierra la Activity de login para que no se pueda volver atrás
    }

    private fun saveLoginState() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    private fun simulateLoginFailure() {
        // Aquí puedes implementar el comportamiento que desees para un fallo de login
        Toast.makeText(this, "Simulando fallo de inicio de sesión", Toast.LENGTH_LONG).show()
        // Opcionalmente, podrías limpiar los campos de email y contraseña:
        binding.editTextEmailAddress.text.clear()
        binding.editTextPassword.text.clear()
        // O mostrar un mensaje en un TextView si lo agregas al layout.
    }
}