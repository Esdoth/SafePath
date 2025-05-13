package com.example.safepath

import android.app.Activity
import android.content.res.Configuration
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

// Extensión para configurar dinámicamente la barra de estado
fun Activity.setupStatusBar() {
    // Verifica si el sistema está en modo oscuro
    val isDarkMode = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    WindowCompat.getInsetsController(window, window.decorView).apply {
        // Texto CLARO (blanco) en modo OSCURO | Texto OSCURO (negro) en modo CLARO
        isAppearanceLightStatusBars = !isDarkMode
        isAppearanceLightNavigationBars = !isDarkMode
    }
}