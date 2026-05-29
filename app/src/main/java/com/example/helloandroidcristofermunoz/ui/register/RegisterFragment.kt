package com.example.helloandroidcristofermunoz.ui.register

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.repository.AuthRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy {
        AuthRepository()
    }

    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentRegisterBinding.bind(view)

        setupUI()
        observe()
    }

    private fun setupUI() {

        binding.btnRegister.setOnClickListener {

            val name = binding.edtName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val confirmPassword = binding.edtConfirmPassword.text.toString().trim()

            viewModel.validateAndRegister(name, email, password, confirmPassword)
        }

        binding.txtLogin.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun observe() {

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility =
                if (loading) View.VISIBLE else View.GONE
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show()

                viewModel.resetSuccessState()

                findNavController().navigate(R.id.loginFragment)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()

                viewModel.resetErrorState()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}