package com.example.helloandroidcristofermunoz.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
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

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.updateProfileImage(it.toString())
            binding.imgProfile.setImageURI(it)
        }
    }

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
            val action = ProfileFragmentDirections.actionProfileFragmentToSavingsSettingsFragment()
            findNavController().navigate(action)
        }

        binding.btnNotifications.setOnClickListener {
            // TODO: Implementar configuración de notificaciones
        }

        binding.btnHistory.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToHistoryFragment()
            findNavController().navigate(action)
        }

        binding.fabChangeImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            val action = ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
            findNavController().navigate(action)
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