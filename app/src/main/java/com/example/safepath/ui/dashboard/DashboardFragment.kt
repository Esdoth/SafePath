package com.example.safepath.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.safepath.databinding.FragmentMisPuntosBinding
import com.example.safepath.ui.componentes.LocationCard

class DashboardFragment : Fragment() {
    private var _binding: FragmentMisPuntosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMisPuntosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.apply {
            setContent {
                MaterialTheme {
                    LocationCard(
                        latitude = 19.4326,  // Ejemplo: CDMX
                        longitude = -99.1332
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}