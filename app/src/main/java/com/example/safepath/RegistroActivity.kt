package com.example.safepath

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.example.safepath.auth.CognitoHelper
import com.example.safepath.databinding.ActivityRegistroBinding
import kotlinx.coroutines.launch

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private lateinit var cognitoHelper: CognitoHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // Inicializar Cognito Helper
        cognitoHelper = CognitoHelper(this)

        // Configurar el botón de registro
        binding.button.setOnClickListener {
            registerUser()
        }

        // Configurar el texto para ir a Login
        binding.textView4.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun registerUser() {
        val username = binding.editTextName.text.toString().trim()
        val email = binding.editTextTextEmailAddress.text.toString().trim()
        val password = binding.editTextTextPassword.text.toString().trim()

        if (validateInputs(username, email, password)) {
            lifecycleScope.launch {
                try {
                    signUpWithCognito(username, email, password)
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(
                            this@RegistroActivity,
                            "Error en registro: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun validateInputs(username: String, email: String, password: String): Boolean {
        if (username.isEmpty()) {
            binding.editTextName.error = "Nombre de usuario requerido"
            return false
        }

        if (email.isEmpty()) {
            binding.editTextTextEmailAddress.error = "Email requerido"
            return false
        }

        if (password.isEmpty() || password.length < 8) {
            binding.editTextTextPassword.error = "La contraseña debe tener al menos 8 caracteres"
            return false
        }

        return true
    }

    private fun signUpWithCognito(username: String, email: String, password: String) {
        val userAttributes = CognitoUserAttributes().apply {
            addAttribute("email", email)
        }

        cognitoHelper.userPool.signUp(
            username,
            password,
            userAttributes,
            null,
            object : com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler {
                override fun onSuccess(
                    user: CognitoUser?,
                    signUpConfirmationState: Boolean,
                    cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails?
                ) {
                    runOnUiThread {
                        if (signUpConfirmationState) {
                            // Usuario confirmado automáticamente (poco común)
                            Toast.makeText(
                                this@RegistroActivity,
                                "Registro exitoso. Ya puedes iniciar sesión.",
                                Toast.LENGTH_LONG
                            ).show()
                            navigateToLogin()
                        } else {
                            // Se requiere confirmación
                            Toast.makeText(
                                this@RegistroActivity,
                                "Registro exitoso. Por favor verifica tu email.",
                                Toast.LENGTH_LONG
                            ).show()
                            navigateToVerification(username)
                        }
                    }
                }

                override fun onFailure(exception: Exception?) {
                    runOnUiThread {
                        Toast.makeText(
                            this@RegistroActivity,
                            "Error en registro: ${exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }

    private fun navigateToVerification(username: String) {
        val intent = Intent(this, VerificationActivity::class.java).apply {
            putExtra("username", username)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}