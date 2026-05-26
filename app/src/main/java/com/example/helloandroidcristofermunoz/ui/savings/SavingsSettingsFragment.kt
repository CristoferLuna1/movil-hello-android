package com.example.helloandroidcristofermunoz.ui.savings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.databinding.FragmentSavingsSettingsBinding
import com.example.helloandroidcristofermunoz.utils.AmountFormatter

class SavingsSettingsFragment : Fragment(R.layout.fragment_savings_settings) {

    private var _binding: FragmentSavingsSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavingsSettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSavingsSettingsBinding.bind(view)

        setupListeners()
        observeData()
        loadCurrentSettings()
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val monthlyGoal = binding.edtMonthlyGoal.text.toString().trim()
            val maxAmount = binding.edtMaxAmount.text.toString().trim()
            viewModel.saveSettings(monthlyGoal, maxAmount)
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
                binding.tilMonthlyGoal.error = if (it.contains("mensual")) it else null
                binding.tilMaxAmount.error = if (it.contains("máximo")) it else null
                viewModel.resetErrorState()
            }
        }
    }

    private fun loadCurrentSettings() {
        viewModel.loadCurrentSettings()
        viewModel.currentSettings.observe(viewLifecycleOwner) { settings ->
            settings?.let {
                binding.edtMonthlyGoal.setText(AmountFormatter.format(it.monthlyGoal))
                binding.edtMaxAmount.setText(AmountFormatter.format(it.maxAmount))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
