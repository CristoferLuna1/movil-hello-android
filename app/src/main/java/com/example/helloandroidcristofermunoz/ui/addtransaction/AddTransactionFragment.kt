package com.example.helloandroidcristofermunoz.ui.addtransaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentAddTransactionBinding
import com.example.helloandroidcristofermunoz.utils.AmountFormatter
import com.example.helloandroidcristofermunoz.utils.Categories
import com.example.helloandroidcristofermunoz.utils.NotificationHelper
import kotlinx.coroutines.launch
import java.text.NumberFormat
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

    private val dateFormat =
        SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))

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

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    selectedCategory =
                        Categories.PREDEFINED_CATEGORIES[position]

                    handleCategorySelection(selectedCategory)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
    }

    private fun handleCategorySelection(category: String) {

        if (category == "Otro") {

            binding.tilCustomCategory.visibility = View.VISIBLE

        } else {

            binding.tilCustomCategory.visibility = View.GONE
            binding.edtCustomCategory.text?.clear()
        }

        if (category == "Deudas Mensuales") {

            binding.tilPaymentDay.visibility = View.VISIBLE
            binding.tilEndDate.visibility = View.VISIBLE

        } else {

            binding.tilPaymentDay.visibility = View.GONE
            binding.tilEndDate.visibility = View.GONE

            binding.edtPaymentDay.text?.clear()
            binding.edtEndDate.text?.clear()

            selectedPaymentDay = null
            selectedEndDate = null
        }
    }

    private fun setupAmountFormatter() {

        binding.edtAmount.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {}

                override fun afterTextChanged(s: Editable?) {

                    if (s != null && s.isNotEmpty()) {

                        val text = s.toString()

                        val cleanText =
                            text.replace(".", "")
                                .replace(",", "")

                        if (cleanText.isNotEmpty() && cleanText != "0") {

                            try {

                                val number = cleanText.toDouble()

                                if (number > 0) {

                                    val formatted =
                                        AmountFormatter.format(number)

                                    binding.edtAmount.removeTextChangedListener(this)

                                    binding.edtAmount.setText(formatted)

                                    binding.edtAmount.setSelection(
                                        formatted.length
                                    )

                                    binding.edtAmount.addTextChangedListener(this)
                                }

                            } catch (e: Exception) {

                            }
                        }
                    }
                }
            }
        )
    }

    private fun setupDatePicker() {

        binding.edtPaymentDay.setOnClickListener {

            val calendar = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->

                    calendar.set(year, month, day)

                    selectedPaymentDay = day

                    binding.edtPaymentDay.setText(
                        "$day de ${getMonthName(month)} de $year"
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.show()
        }

        binding.edtEndDate.setOnClickListener {

            val calendar = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->

                    calendar.set(year, month, day)

                    selectedEndDate =
                        calendar.timeInMillis

                    binding.edtEndDate.setText(
                        dateFormat.format(calendar.time)
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.show()
        }
    }

    private fun getMonthName(month: Int): String {

        val months = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril",
            "Mayo", "Junio", "Julio", "Agosto",
            "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        return months[month]
    }

    private fun setupListeners() {

        binding.btnSave.setOnClickListener {

            val title =
                binding.edtTitle.text.toString().trim()

            val amount =
                binding.edtAmount.text.toString().trim()

            val customCategory =
                binding.edtCustomCategory.text.toString().trim()

            val selectedTypeId =
                binding.radioGroupType.checkedRadioButtonId

            val selectedRadioButton =
                view?.findViewById<RadioButton>(selectedTypeId)

            val type =
                if (selectedRadioButton?.id == R.id.rbIncome)
                    "Ingreso"
                else
                    "Gasto"

            val finalCategory =
                if (selectedCategory == "Otro") {
                    customCategory.ifEmpty { "Otro" }
                } else {
                    selectedCategory
                }

            if (type == "Gasto") {

                checkBudgetBeforeSaving(
                    title,
                    amount,
                    finalCategory,
                    type
                )

            } else {

                viewModel.validateAndSaveTransaction(
                    title,
                    amount,
                    finalCategory,
                    type,
                    selectedPaymentDay,
                    selectedEndDate,
                    Categories.isMonthlyPersistent(selectedCategory),
                    if (isEditMode)
                        args.transactionId
                    else
                        -1
                )
            }
        }
    }

    private fun checkBudgetBeforeSaving(
        title: String,
        amount: String,
        category: String,
        type: String
    ) {

        val amountValue =
            amount.replace(".", "")
                .replace(",", "")
                .toDoubleOrNull() ?: 0.0

        lifecycleScope.launch {

            val calendar = Calendar.getInstance()

            val currentMonth =
                calendar.get(Calendar.MONTH)

            val currentYear =
                calendar.get(Calendar.YEAR)

            val savingsPlanDao =
                AppDatabase.getDatabase(requireContext())
                .savingsPlanDao()
            val monthYear =
                currentYear * 100 + (currentMonth + 1)

            val savingsPlan =
                savingsPlanDao.getGoalForMonth(
                monthYear
            )

            if (savingsPlan == null) {

                viewModel.validateAndSaveTransaction(
                    title,
                    amount,
                    category,
                    type,
                    selectedPaymentDay,
                    selectedEndDate,
                    Categories.isMonthlyPersistent(selectedCategory),
                    if (isEditMode)
                        args.transactionId
                    else
                        -1
                )

                return@launch
            }

            val transactionDao =
                AppDatabase.getDatabase(requireContext())
                    .transactionDao()

            val allTransactions =
                transactionDao.getAll()

            val monthlyTransactions =
                allTransactions.filter { transaction ->

                    val txCalendar =
                        Calendar.getInstance()

                    txCalendar.timeInMillis =
                        transaction.date

                    txCalendar.get(Calendar.MONTH) == currentMonth &&
                            txCalendar.get(Calendar.YEAR) == currentYear
                }

            val monthlyIncome =
                monthlyTransactions
                    .filter { it.type == "Ingreso" }
                    .sumOf { it.amount }

            val monthlyExpenses =
                monthlyTransactions
                    .filter { it.type == "Gasto" }
                    .sumOf { it.amount }

            val availableBudget =
                monthlyIncome - savingsPlan.monthlyGoal

            val remainingBudget =
                availableBudget - monthlyExpenses - amountValue

            val formattedRemaining =
                NumberFormat.getCurrencyInstance(
                    Locale("es", "CO")
                ).format(remainingBudget.toDouble())

            val formattedAmount =
                NumberFormat.getCurrencyInstance(
                    Locale("es", "CO")
                ).format(amountValue)

            if (remainingBudget >= 0) {

                AlertDialog.Builder(requireContext())
                    .setTitle("Presupuesto Disponible")
                    .setMessage(
                        "Puedes gastar este monto. " +
                                "Te quedarán $formattedRemaining " +
                                "disponibles sin tocar tu ahorro."
                    )
                    .setPositiveButton("Guardar") { _, _ ->

                        viewModel.validateAndSaveTransaction(
                            title,
                            amount,
                            category,
                            type,
                            selectedPaymentDay,
                            selectedEndDate,
                            Categories.isMonthlyPersistent(selectedCategory),
                            if (isEditMode)
                                args.transactionId
                            else
                                -1
                        )
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()

            } else {

                val overspending =
                    kotlin.math.abs(remainingBudget.toDouble())

                val formattedOverspending =
                    NumberFormat.getCurrencyInstance(
                        Locale("es", "CO")
                    ).format(overspending.toDouble())

                AlertDialog.Builder(requireContext())
                    .setTitle("¡Alerta!")
                    .setMessage(
                        "Este gasto de $formattedAmount " +
                                "superará tu presupuesto máximo por " +
                                "$formattedOverspending.\n\n" +
                                "¿Deseas continuar?"
                    )
                    .setPositiveButton("Guardar") { _, _ ->

                        viewModel.validateAndSaveTransaction(
                            title,
                            amount,
                            category,
                            type,
                            selectedPaymentDay,
                            selectedEndDate,
                            Categories.isMonthlyPersistent(selectedCategory),
                            if (isEditMode)
                                args.transactionId
                            else
                                -1
                        )
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    private fun observeViewModel() {

        viewModel.transactionToEdit.observe(viewLifecycleOwner) { transaction ->

            transaction?.let {

                binding.edtTitle.setText(it.title)

                binding.edtAmount.setText(
                    AmountFormatter.format(it.amount)
                )

                val categoryIndex =
                    Categories.PREDEFINED_CATEGORIES.indexOf(it.category)

                if (categoryIndex >= 0) {

                    binding.spinnerCategory.setSelection(categoryIndex)

                    selectedCategory = it.category
                }

                if (it.type == "Ingreso") {

                    binding.rbIncome.isChecked = true

                } else {

                    binding.rbExpense.isChecked = true
                }

                it.paymentDay?.let { day ->

                    selectedPaymentDay = day

                    binding.edtPaymentDay.setText(
                        "$day de cada mes"
                    )
                }

                it.endDate?.let { endDate ->

                    selectedEndDate = endDate

                    binding.edtEndDate.setText(
                        dateFormat.format(java.util.Date(endDate))
                    )
                }
            }
        }

        viewModel.titleError.observe(viewLifecycleOwner) {
            binding.tilTitle.error = it
        }

        viewModel.amountError.observe(viewLifecycleOwner) {
            binding.tilAmount.error = it
        }

        viewModel.categoryError.observe(viewLifecycleOwner) {
            binding.tilCategory.error = it
        }

        viewModel.paymentDayError.observe(viewLifecycleOwner) {
            binding.tilPaymentDay.error = it
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->

            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE
                else View.GONE

            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->

            if (isSuccess) {

                Toast.makeText(
                    requireContext(),
                    if (isEditMode)
                        "Transacción actualizada exitosamente"
                    else
                        "Transacción guardada exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                NotificationHelper.showNotification(
                    requireContext(),
                    "Transacción creada",
                    "Se guardó correctamente"
                )

                if (isEditMode) {

                    requireActivity().onBackPressed()

                } else {

                    clearForm()
                }

                viewModel.resetSuccessState()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {

            it?.let { error ->

                Toast.makeText(
                    requireContext(),
                    error,
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

        selectedCategory =
            Categories.PREDEFINED_CATEGORIES[0]

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