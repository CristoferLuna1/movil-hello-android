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

        viewModel.setContext(requireContext())
        setupToolbar()
        setupListeners()
        observeData()
        loadCurrentSettings()
    }

    private fun setupToolbar() {
        binding.toolbarSavingsSettings.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupListeners() {
        binding.radioGroupSavingsType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbMonthly -> {
                    binding.tilMonths.visibility = View.GONE
                    binding.txtCalculatedSavings.visibility = View.GONE
                    binding.edtMonthlyGoal.hint = "Meta mensual de ahorro"
                }
                R.id.rbMultiMonth -> {
                    binding.tilMonths.visibility = View.VISIBLE
                    binding.txtCalculatedSavings.visibility = View.VISIBLE
                    binding.edtMonthlyGoal.hint = "Meta total de ahorro"
                    calculateSavings()
                }
            }
        }

        binding.edtMonthlyGoal.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.rbMultiMonth.isChecked) {
                    calculateSavings()
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.edtMonths.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.rbMultiMonth.isChecked) {
                    calculateSavings()
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.btnSave.setOnClickListener {
            val monthlyGoal = binding.edtMonthlyGoal.text.toString().trim()
            val maxAmount = binding.edtMaxAmount.text.toString().trim()
            val months = binding.edtMonths.text.toString().trim()
            val isMultiMonth = binding.rbMultiMonth.isChecked
            viewModel.saveSettings(monthlyGoal, maxAmount, months, isMultiMonth)
        }
    }

    private fun calculateSavings() {
        val totalGoal = binding.edtMonthlyGoal.text.toString().trim()
        val months = binding.edtMonths.text.toString().trim()

        if (totalGoal.isNotEmpty() && months.isNotEmpty()) {
            val total = AmountFormatter.parse(totalGoal)
            val monthsCount = months.toIntOrNull() ?: 1

            if (monthsCount > 0) {
                val monthlySavings = total / monthsCount
                binding.txtCalculatedSavings.text = "Debes ahorrar: $${AmountFormatter.format(monthlySavings)}/mes"
            }
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
