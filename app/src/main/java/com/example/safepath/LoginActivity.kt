package com.example.safepath

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView // Import necesario para TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializaciones
        auth = Firebase.auth
        sharedPreferences = getSharedPreferences("SafePathPrefs", Context.MODE_PRIVATE)

        // Verificar sesión existente
        if (isUserLoggedIn()) {
            navigateToMainActivity()
            return
        }

        setupWindowInsets()
        setupClickListeners()
    }

    private fun isUserLoggedIn(): Boolean {
        // Verificar tanto en Firebase como en preferencias
        return auth.currentUser != null && sharedPreferences.getBoolean("isLoggedIn", false)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmailAddress.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            attemptLogin(email, password)
        }

        binding.textViewRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun attemptLogin(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa email y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    onLoginSuccess()
                } else {
                    handleLoginError(task.exception)
                }
            }
    }

    private fun onLoginSuccess() {
        // Guardar estado de login
        sharedPreferences.edit().apply {
            putBoolean("isLoggedIn", true)
            putString("userEmail", auth.currentUser?.email)
            apply()
        }

        navigateToMainActivity()
    }

    private fun handleLoginError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidUserException -> "Usuario no registrado"
            is FirebaseAuthInvalidCredentialsException -> "Credenciales incorrectas"
            else -> "Error al iniciar sesión: ${exception?.localizedMessage}"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}