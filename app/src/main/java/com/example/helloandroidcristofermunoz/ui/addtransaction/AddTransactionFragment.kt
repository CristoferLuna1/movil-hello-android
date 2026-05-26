package com.example.helloandroidcristofermunoz.ui.addtransaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentAddTransactionBinding
import com.example.helloandroidcristofermunoz.utils.AmountFormatter
import com.example.helloandroidcristofermunoz.utils.Categories
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionFragment : Fragment(R.layout.fragment_add_transaction) {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by viewModels {

        val dao = AppDatabase
            .getDatabase(requireContext())
            .transactionDao()

        val repository = TransactionRepository(dao)

        AddTransactionViewModelFactory(repository)
    }

    private var selectedCategory: String = ""
    private var selectedPaymentDay: Int? = null
    private var selectedEndDate: Long? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAddTransactionBinding.bind(view)

        setupCategorySpinner()
        setupAmountFormatter()
        setupDatePicker()
        setupListeners()
        observeViewModel()
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Categories.PREDEFINED_CATEGORIES
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = Categories.PREDEFINED_CATEGORIES[position]
                handleCategorySelection(selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun handleCategorySelection(category: String) {
        // Mostrar campo de categoría personalizada si selecciona "Otro"
        if (category == "Otro") {
            binding.tilCustomCategory.visibility = View.VISIBLE
        } else {
            binding.tilCustomCategory.visibility = View.GONE
            binding.edtCustomCategory.text?.clear()
        }

        // Mostrar campos de deuda mensual si selecciona "Deudas Mensuales"
        if (category == "Deudas Mensuales") {
            binding.tilPaymentDay.visibility = View.VISIBLE
            binding.tilEndDate.visibility = View.VISIBLE
        } else {
            binding.tilPaymentDay.visibility = View.GONE
            binding.tilEndDate.visibility = View.GONE
            binding.edtPaymentDay.text?.clear()
            binding.edtEndDate.text?.clear()
            selectedEndDate = null
        }
    }

    private fun setupAmountFormatter() {
        binding.edtAmount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s != null && s.isNotEmpty()) {
                    val text = s.toString()
                    if (!text.contains(".") && !text.contains(",")) {
                        val formatted = AmountFormatter.format(text.toDouble())
                        binding.edtAmount.removeTextChangedListener(this)
                        binding.edtAmount.setText(formatted)
                        binding.edtAmount.setSelection(formatted.length)
                        binding.edtAmount.addTextChangedListener(this)
                    }
                }
            }
        })
    }

    private fun setupDatePicker() {
        binding.edtPaymentDay.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    calendar.set(selectedYear, selectedMonth, selectedDay)
                    selectedPaymentDay = selectedDay
                    binding.edtPaymentDay.setText("$selectedDay de ${getMonthName(selectedMonth)} de $selectedYear")
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        binding.edtEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    calendar.set(selectedYear, selectedMonth, selectedDay)
                    selectedEndDate = calendar.timeInMillis
                    binding.edtEndDate.setText(dateFormat.format(calendar.time))
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    private fun getMonthName(month: Int): String {
        val months = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        return months[month]
    }

    private fun setupListeners() {

        binding.btnSave.setOnClickListener {

            val title = binding.edtTitle.text.toString().trim()
            val amount = binding.edtAmount.text.toString().trim()
            val customCategory = binding.edtCustomCategory.text.toString().trim()
            val paymentDay = if (selectedPaymentDay != null) selectedPaymentDay.toString() else ""

            val selectedTypeId =
                binding.radioGroupType.checkedRadioButtonId

            val selectedRadioButton =
                view?.findViewById<RadioButton>(selectedTypeId)

            val type =
                if (selectedRadioButton?.id == R.id.rbIncome)
                    "income"
                else
                    "expense"

            val category = if (selectedCategory == "Otro") {
                customCategory.ifEmpty { "Otro" }
            } else {
                selectedCategory
            }

            viewModel.validateAndSaveTransaction(
                title,
                amount,
                category,
                type,
                paymentDay,
                selectedEndDate,
                Categories.isMonthlyPersistent(selectedCategory)
            )
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

            binding.tilCustomCategory.error = error
        }

        viewModel.paymentDayError.observe(viewLifecycleOwner) { error ->

            binding.tilPaymentDay.error = error
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->

            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE

            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->

            if (isSuccess) {

                Toast.makeText(
                    requireContext(),
                    "Transacción guardada exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                clearForm()

                viewModel.resetSuccessState()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->

            errorMessage?.let {

                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.resetErrorState()
            }
        }
    }

    private fun clearForm() {

        binding.edtTitle.text?.clear()
        binding.edtAmount.text?.clear()
        binding.edtCustomCategory.text?.clear()
        binding.edtPaymentDay.text?.clear()
        binding.edtEndDate.text?.clear()

        binding.spinnerCategory.setSelection(0)
        selectedCategory = Categories.PREDEFINED_CATEGORIES[0]
        selectedPaymentDay = null
        selectedEndDate = null

        handleCategorySelection(selectedCategory)

        binding.rbIncome.isChecked = true
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}