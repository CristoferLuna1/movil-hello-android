package com.example.helloandroidcristofermunoz.ui.addtransaction

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.databinding.FragmentAddTransactionBinding

class AddTransactionFragment : Fragment(R.layout.fragment_add_transaction) {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddTransactionBinding.bind(view)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val title = binding.edtTitle.text.toString()
            val amount = binding.edtAmount.text.toString()
            val category = binding.edtCategory.text.toString()

            val selectedTypeId = binding.radioGroupType.checkedRadioButtonId
            val selectedRadioButton = view?.findViewById<RadioButton>(selectedTypeId)
            val type = if (selectedRadioButton?.id == R.id.rbIncome) "income" else "expense"

            viewModel.validateAndSaveTransaction(title, amount, category, type)
        }
    }

    private fun observeViewModel() {
        viewModel.titleError.observe(viewLifecycleOwner) { error ->
            binding.tilTitle.error = error
        }

        viewModel.amountError.observe(viewLifecycleOwner) { error ->
            binding.tilAmount.error = error
        }

        viewModel.categoryError.observe(viewLifecycleOwner) { error ->
            binding.tilCategory.error = error
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Transacción guardada exitosamente", Toast.LENGTH_SHORT).show()
                clearForm()
                viewModel.resetSuccessState()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.resetErrorState()
            }
        }
    }

    private fun clearForm() {
        binding.edtTitle.text?.clear()
        binding.edtAmount.text?.clear()
        binding.edtCategory.text?.clear()
        binding.rbIncome.isChecked = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
