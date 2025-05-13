package com.example.safepath

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.safepath.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.WindowCompat
import android.content.res.Configuration

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar la barra de estado ANTES de setContentView
        setupStatusBarTheme()

        // Asegurarse que la ActionBar no existe
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración de la navegación inferior
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Define los destinos de nivel superior
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_mis_puntos,
                R.id.navigation_cuenta
            )
        )

        // Configura la navegación con el BottomNavigationView
        navView.setupWithNavController(navController)

        // Opcional: Configuración manual de los clicks si necesitas más control
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_mis_puntos -> {
                    navController.navigate(R.id.navigation_mis_puntos)
                    true
                }
                R.id.navigation_cuenta -> {
                    navController.navigate(R.id.navigation_cuenta)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupStatusBarTheme() {
        // Determinar si el sistema está en modo oscuro
        val isDarkMode = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        // Configurar el color del texto de la barra de estado
        WindowCompat.getInsetsController(window, window.decorView).apply {
            // Texto oscuro en modo claro, texto claro en modo oscuro
            isAppearanceLightStatusBars = !isDarkMode
            isAppearanceLightNavigationBars = !isDarkMode
        }
    }

    override fun onResume() {
        super.onResume()
        // Volver a aplicar la configuración por si cambió el tema mientras la app estaba en segundo plano
        setupStatusBarTheme()
    }
}