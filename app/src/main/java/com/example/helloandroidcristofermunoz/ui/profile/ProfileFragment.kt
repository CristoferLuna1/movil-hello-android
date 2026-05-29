package com.example.helloandroidcristofermunoz.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.helloandroidcristofermunoz.LoginActivity
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.UserRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentProfileBinding

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).userDao()
        val repository = UserRepository(dao)
        ProfileViewModelFactory(repository)
    }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                viewModel.updateProfileImage(it.toString())
                binding.imgProfile.setImageURI(it)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        viewModel.loadUserProfile()
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
            Toast.makeText(requireContext(), "Notificaciones - próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.btnHistory.setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }

        binding.fabChangeImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnLogout.setOnClickListener {
            val isLoggedIn = viewModel.isLoggedIn.value == true

            if (isLoggedIn) {
                viewModel.logout()

                Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()

                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            } else {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
        }
    }

    private fun observeData() {
        viewModel.name.observe(viewLifecycleOwner) {
            binding.txtName.text = it
        }

        viewModel.email.observe(viewLifecycleOwner) {
            binding.txtEmail.text = it
        }

        viewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            binding.btnLogout.text =
                if (isLoggedIn) "Cerrar Sesión" else "Iniciar Sesión"
        }

        viewModel.isLoggingOut.observe(viewLifecycleOwner) {
            binding.btnLogout.isEnabled = !it
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}