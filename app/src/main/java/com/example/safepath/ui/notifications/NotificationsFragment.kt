package com.example.safepath.ui.notifications // Manteniendo el paquete por ahora

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.safepath.LoginActivity
import com.example.safepath.databinding.FragmentCuentaBinding // Asegúrate de que esta importación sea correcta

class NotificationsFragment : Fragment() { // Manteniendo el nombre de la clase por ahora

    private var _binding: FragmentCuentaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel = // Manteniendo el ViewModel por ahora
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentCuentaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Obtener referencia al botón de cerrar sesión
        val cerrarSesionButton: Button = binding.buttonCerrarSesion
        cerrarSesionButton.setOnClickListener {
            // Lógica para cerrar sesión
            cerrarSesion()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Aquí puedes realizar cualquier inicialización adicional de la vista
    }

    private fun cerrarSesion() {
        // Eliminar cualquier información de sesión persistente (ejemplo: authToken en SharedPreferences)
        val sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("authToken").apply()

        // Redirigir al usuario a la pantalla de inicio de sesión
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish() // Opcional: finalizar la actividad actual
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}