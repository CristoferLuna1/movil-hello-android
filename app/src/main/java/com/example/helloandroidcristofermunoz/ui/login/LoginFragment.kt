package com.example.helloandroidcristofermunoz.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.databinding.FragmentLoginBinding
import com.example.helloandroidcristofermunoz.data.repository.UserRepository
import com.example.helloandroidcristofermunoz.data.AppDatabase


class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // ✔️ USANDO FACTORY CORRECTAMENTE
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(
            UserRepository(
                AppDatabase.getDatabase(requireContext()).userDao()
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        setupListeners()
        observeData()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            viewModel.validateAndLogin(email, password)
        }

        binding.txtRegister.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
    }

    private fun observeData() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.homeFragment)
                viewModel.resetSuccessState()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                binding.tilEmail.error =
                    if (it.contains("email")) it else null

                binding.tilPassword.error =
                    if (it.contains("contraseña")) it else null

                viewModel.resetErrorState()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}