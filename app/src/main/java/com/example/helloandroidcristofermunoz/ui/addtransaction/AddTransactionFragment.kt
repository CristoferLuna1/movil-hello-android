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
import androidx.navigation.fragment.navArgs
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

    private val args: AddTransactionFragmentArgs by navArgs()

    private val viewModel: AddTransactionViewModel by viewModels {

        val dao = AppDatabase
            .getDatabase(requireContext())
            .transactionDao()

        val savingsDao = AppDatabase
            .getDatabase(requireContext())
            .savingsPlanDao()

        val repository = TransactionRepository(dao, savingsDao)

        AddTransactionViewModelFactory(repository)
    }

    private var selectedCategory: String = ""
    private var selectedPaymentDay: Int? = null
    private var selectedEndDate: Long? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
    private var isEditMode = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAddTransactionBinding.bind(view)

        isEditMode = args.transactionId != -1

        setupCategorySpinner()
        setupAmountFormatter()
        setupDatePicker()
        setupListeners()
        observeViewModel()

        if (isEditMode) {
            loadTransactionForEdit()
        }
    }

    private fun loadTransactionForEdit() {
        viewModel.loadTransaction(args.transactionId)
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
                    // Eliminar todos los puntos y comas para obtener el número limpio
                    val cleanText = text.replace(".", "").replace(",", "")
                    if (cleanText.isNotEmpty() && cleanText != "0") {
                        try {
                            val number = cleanText.toDouble()
                            if (number > 0) {
                                val formatted = AmountFormatter.format(number)
                                binding.edtAmount.removeTextChangedListener(this)
                                binding.edtAmount.setText(formatted)
                                binding.edtAmount.setSelection(formatted.length)
                                binding.edtAmount.addTextChangedListener(this)
                            }
                        } catch (e: Exception) {
                            // Si hay error al convertir, no hacer nada
                        }
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
                Categories.isMonthlyPersistent(selectedCategory),
                if (isEditMode) args.transactionId else -1
            )
        }
    }

    private fun observeViewModel() {

        viewModel.transactionToEdit.observe(viewLifecycleOwner) { transaction ->
            transaction?.let {
                // Llenar el formulario con los datos de la transacción
                binding.edtTitle.setText(it.title)
                binding.edtAmount.setText(AmountFormatter.format(it.amount))
                
                // Seleccionar categoría
                val categoryIndex = Categories.PREDEFINED_CATEGORIES.indexOf(it.category)
                if (categoryIndex >= 0) {
                    binding.spinnerCategory.setSelection(categoryIndex)
                    selectedCategory = it.category
                }
                
                // Seleccionar tipo
                if (it.type == "income") {
                    binding.rbIncome.isChecked = true
                } else {
                    binding.rbExpense.isChecked = true
                }
                
                // Día de pago
                it.paymentDay?.let { day ->
                    selectedPaymentDay = day
                    binding.edtPaymentDay.setText("$day de cada mes")
                }
                
                // Fecha fin
                it.endDate?.let { endDate ->
                    selectedEndDate = endDate
                    binding.edtEndDate.setText(dateFormat.format(java.util.Date(endDate)))
                }
            }
        }

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
                    if (isEditMode) "Transacción actualizada exitosamente" else "Transacción guardada exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                if (isEditMode) {
                    requireActivity().onBackPressed()
                } else {
                    clearForm()
                }

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