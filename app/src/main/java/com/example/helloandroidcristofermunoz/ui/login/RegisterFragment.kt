package com.example.helloandroidcristofermunoz.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.databinding.FragmentRegisterBinding
import com.example.helloandroidcristofermunoz.ui.register.RegisterViewModel
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.UserRepository

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var viewModel: RegisterViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        val dao = AppDatabase.getDatabase(requireContext()).userDao()
        val repository = UserRepository(dao)

        viewModel = RegisterViewModel(repository)
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val confirmPassword = binding.edtConfirmPassword.text.toString().trim()
            viewModel.validateAndRegister(name, email, password, confirmPassword)
        }

        binding.txtLogin.setOnClickListener { findNavController().navigate(R.id.loginFragment) }
    }

    private fun observeData() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.loginFragment)
                viewModel.resetSuccessState()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                binding.tilName.error = if (it.contains("nombre")) it else null
                binding.tilEmail.error = if (it.contains("email")) it else null
                binding.tilPassword.error = if (it.contains("contraseña")) it else null
                binding.tilConfirmPassword.error = if (it.contains("confirmar")) it else null
                viewModel.resetErrorState()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
