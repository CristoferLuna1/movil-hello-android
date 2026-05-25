package com.example.helloandroidcristofermunoz.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.helloandroidcristofermunoz.LoginActivity
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.UserRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentProfileBinding

class ProfileFragment :
    Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        val dao = AppDatabase
            .getDatabase(requireContext())
            .userDao()

        val repository = UserRepository(dao)

        ProfileViewModelFactory(repository)
    }

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

        viewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (!isLoggedIn) {
                // Si no está logueado, mostrar opción de iniciar sesión
                binding.btnLogout.text = "Iniciar Sesión"
            } else {
                binding.btnLogout.text = "Cerrar Sesión"
            }
        }

        viewModel.isLoggingOut.observe(viewLifecycleOwner) { isLoggingOut ->
            binding.btnLogout.isEnabled = !isLoggingOut
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
            val isLoggedIn = viewModel.isLoggedIn.value == true
            if (isLoggedIn) {
                // Cerrar sesión
                viewModel.logout()
                Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
                // Navegar a LoginActivity
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                // Iniciar sesión
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
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
