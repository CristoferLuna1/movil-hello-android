package com.example.helloandroidcristofermunoz.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditProfileBinding.bind(view)

        viewModel.setContext(requireContext())
        setupToolbar()
        setupListeners()
        observeData()
        loadCurrentProfile()
    }

    private fun setupToolbar() {
        binding.toolbarEditProfile.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            viewModel.updateProfile(name, email)
        }
    }

    private fun observeData() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                findNavController().navigateUp()
                viewModel.resetSuccessState()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                binding.tilName.error = if (it.contains("nombre")) it else null
                binding.tilEmail.error = if (it.contains("email")) it else null
                viewModel.resetErrorState()
            }
        }
    }

    private fun loadCurrentProfile() {
        viewModel.loadCurrentProfile()
        viewModel.currentProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.edtName.setText(it.name)
                binding.edtEmail.setText(it.email)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
