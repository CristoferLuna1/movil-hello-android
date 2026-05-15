package com.example.helloandroidcristofermunoz.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

        observeData()
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