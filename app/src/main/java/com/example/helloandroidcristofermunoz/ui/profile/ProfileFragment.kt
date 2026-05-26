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
            findNavController().navigate(R.id.editProfileFragment)
        }

        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.changePasswordFragment)
        }

        binding.btnSavingsSettings.setOnClickListener {
            findNavController().navigate(R.id.savingsSettingsFragment)
        }

        binding.btnNotifications.setOnClickListener {
            // TODO: Implementar configuración de notificaciones
        }

        binding.btnHistory.setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }

        binding.fabChangeImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.loginFragment)
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