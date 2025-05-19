package com.example.safepath.ui.notifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView // Import necesario para TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.safepath.LoginActivity
import com.example.safepath.databinding.FragmentCuentaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class NotificationsFragment : Fragment() {

    private var _binding: FragmentCuentaBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var textHelloUser: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentCuentaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Obtener referencia al TextView
        textHelloUser = binding.textHelloUser

        // Obtener referencia al botón de cerrar sesión
        val cerrarSesionButton: Button = binding.buttonCerrarSesion
        cerrarSesionButton.setOnClickListener {
            cerrarSesion()
        }

        // Actualizar el saludo con el correo electrónico
        updateGreeting()

        return root
    }

    private fun updateGreeting() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val email = currentUser.email
            if (!email.isNullOrEmpty()) {
                textHelloUser.text = "Correo asociado\n$email"
            } else {
                textHelloUser.text = "Correo asociado\nUsuario sin correo" // Mensaje por si el email es nulo o vacío (raro)
            }
        } else {
            textHelloUser.text = "Correo asociado" // Mensaje si no hay usuario autenticado (esto no debería ocurrir aquí)
        }
    }

    private fun cerrarSesion() {
        auth.signOut()
        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("authToken").apply()
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}