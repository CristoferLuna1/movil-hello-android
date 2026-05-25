package com.example.helloandroidcristofermunoz.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.databinding.FragmentProfileBinding

class ProfileFragment :
    Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        observeData()
        setupClickListeners()
    }

    private fun observeData() {
        viewModel.name.observe(viewLifecycleOwner) {
            binding.txtName.text = it
        }

        viewModel.email.observe(viewLifecycleOwner) {
            binding.txtEmail.text = it
        }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Editar perfil - Próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.optionNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "Configuración de notificaciones - Próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.optionTheme.setOnClickListener {
            Toast.makeText(requireContext(), "Cambiar tema - Próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.optionLanguage.setOnClickListener {
            Toast.makeText(requireContext(), "Cambiar idioma - Próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Cerrar sesión - Próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
