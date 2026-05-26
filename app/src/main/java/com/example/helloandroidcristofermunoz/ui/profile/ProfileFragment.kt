package com.example.helloandroidcristofermunoz.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.databinding.FragmentProfileBinding

class ProfileFragment :
    Fragment(R.layout.fragment_profile) {

    private var _binding:
            FragmentProfileBinding? = null

    private val binding get() = _binding!!

    private val viewModel:
            ProfileViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        _binding =
            FragmentProfileBinding.bind(view)

        setupListeners()
        observeData()
    }

    private fun setupListeners() {
        binding.btnEditProfile.setOnClickListener {
            // TODO: Implementar edición de perfil
        }

        binding.btnChangePassword.setOnClickListener {
            // TODO: Implementar cambio de contraseña
        }

        binding.btnSavingsSettings.setOnClickListener {
            // TODO: Implementar configuración de ahorro
        }

        binding.btnNotifications.setOnClickListener {
            // TODO: Implementar configuración de notificaciones
        }

        binding.btnHistory.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToHistoryFragment()
            findNavController().navigate(action)
        }

        binding.fabChangeImage.setOnClickListener {
            // TODO: Implementar cambio de imagen de perfil
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1000)
        }

        binding.btnLogout.setOnClickListener {
            // TODO: Implementar cierre de sesión
            requireActivity().finish()
        }
    }

    private fun observeData() {

        viewModel.name.observe(viewLifecycleOwner) {

            binding.txtName.text = it
        }

        viewModel.email.observe(viewLifecycleOwner) {

            binding.txtEmail.text = it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}