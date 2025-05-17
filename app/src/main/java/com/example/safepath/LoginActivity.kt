package com.example.safepath

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.safepath.auth.CognitoAuthManager
import com.example.safepath.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authManager: CognitoAuthManager
    private lateinit var authLauncher: ActivityResultLauncher<Intent>

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

    private fun attemptLoginWithCognito(email: String, password: String) {
        // Aquí puedes implementar el flujo de autenticación con Cognito
        // Por ahora, usaremos el flujo OAuth con AppAuth
        startAuthFlow()
    }

    private fun startAuthFlow() {
        lifecycleScope.launch {
            try {
                val authRequest = authManager.getAuthorizationRequest()
                val authIntent = authManager.authService.getAuthorizationRequestIntent(authRequest)
                authLauncher.launch(authIntent)
            } catch (ex: Exception) {
                binding.textViewError.text = "Error al iniciar autenticación: ${ex.message}"
            }
        }
    }

    private fun handleAuthResult(data: Intent) {  // Ahora recibe un Intent no-nulo
        try {
            val response = AuthorizationResponse.fromIntent(data)
            val exception = AuthorizationException.fromIntent(data)

            when {
                exception != null -> {
                    binding.textViewError.text = "Error de autenticación: ${exception.errorDescription}"
                    Log.e("Auth", "AuthorizationException", exception)
                }
                response != null -> {
                    lifecycleScope.launch {
                        val state = authManager.handleAuthorizationResponse(response)
                        if (state.isAuthenticated) {
                            navigateToMainActivity()
                        } else {
                            val errorMsg = state.error ?: "Error desconocido durante la autenticación"
                            binding.textViewError.text = errorMsg
                            Log.e("Auth", errorMsg)
                        }
                    }
                }
                else -> {
                    binding.textViewError.text = "Respuesta de autenticación inválida"
                    Log.e("Auth", "Both response and exception were null")
                }
            }
        } catch (ex: Exception) {
            binding.textViewError.text = "Error procesando respuesta: ${ex.message}"
            Log.e("Auth", "Exception in handleAuthResult", ex)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        authManager.dispose()
    }
}