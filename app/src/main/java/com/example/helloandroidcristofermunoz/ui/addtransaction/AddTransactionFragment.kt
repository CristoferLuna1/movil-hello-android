package com.example.helloandroidcristofermunoz.ui.addtransaction

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentAddTransactionBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import com.example.helloandroidcristofermunoz.utils.NotificationHelper

class AddTransactionFragment : Fragment(R.layout.fragment_add_transaction) {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddTransactionViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
        val repository = TransactionRepository(dao)
        AddTransactionViewModelFactory(repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAddTransactionBinding.bind(view)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {

        binding.btnSave.setOnClickListener {

            val title = binding.edtTitle.text.toString().trim()
            val amount = binding.edtAmount.text.toString().trim()
            val category = binding.edtCategory.text.toString().trim()

            val selectedTypeId = binding.radioGroupType.checkedRadioButtonId
            val selectedRadioButton = view?.findViewById<RadioButton>(selectedTypeId)

            val type =
                if (selectedRadioButton?.id == R.id.rbIncome)
                    "Ingreso"
                else
                    "Gasto"

            // Si es un gasto, verificar el presupuesto disponible
            if (type == "Gasto") {
                checkBudgetBeforeSaving(title, amount, category, type)
            } else {
                viewModel.validateAndSaveTransaction(
                    title,
                    amount,
                    category,
                    type
                )
            }
        }
    }

    private fun checkBudgetBeforeSaving(title: String, amount: String, category: String, type: String) {
        val amountValue = amount.toDoubleOrNull() ?: 0.0

        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            // Obtener meta de ahorro
            val savingsGoalDao = AppDatabase.getDatabase(requireContext()).savingsGoalDao()
            val savingsGoal = savingsGoalDao.getGoalForMonth(currentMonth, currentYear)

            if (savingsGoal == null) {
                // No hay meta configurada, guardar directamente
                viewModel.validateAndSaveTransaction(title, amount, category, type)
                return@launch
            }

            // Obtener transacciones del mes
            val transactionDao = AppDatabase.getDatabase(requireContext()).transactionDao()
            val allTransactions = transactionDao.getAll()

            // Filtrar transacciones del mes actual
            val monthlyTransactions = allTransactions.filter { transaction ->
                val txCalendar = Calendar.getInstance()
                txCalendar.timeInMillis = transaction.date
                txCalendar.get(Calendar.MONTH) == currentMonth && txCalendar.get(Calendar.YEAR) == currentYear
            }

            // Calcular ingresos y gastos actuales
            val monthlyIncome = monthlyTransactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
            val monthlyExpenses = monthlyTransactions.filter { it.type == "Gasto" }.sumOf { it.amount }

            // Calcular presupuesto disponible
            val availableBudget = monthlyIncome - savingsGoal.amount
            val remainingBudget = availableBudget - monthlyExpenses - amountValue

            val formattedRemaining = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(remainingBudget)
            val formattedAmount = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(amountValue)

            if (remainingBudget >= 0) {
                // Está dentro del presupuesto
                AlertDialog.Builder(requireContext())
                    .setTitle("Presupuesto Disponible")
                    .setMessage("Puedes gastar este monto. Te quedarán $formattedRemaining disponibles sin tocar tu ahorro.")
                    .setPositiveButton("Guardar") { _, _ ->
                        viewModel.validateAndSaveTransaction(title, amount, category, type)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                // Está superando el presupuesto
                val overspending = Math.abs(remainingBudget)
                val formattedOverspending = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(overspending)

                AlertDialog.Builder(requireContext())
                    .setTitle("¡Alerta!")
                    .setMessage("Este gasto de $formattedAmount superará tu presupuesto máximo por $formattedOverspending. Estarás usando parte de tu ahorro.\n\n¿Deseas continuar?")
                    .setPositiveButton("Guardar de todas formas") { _, _ ->
                        viewModel.validateAndSaveTransaction(title, amount, category, type)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    private fun observeViewModel() {

        viewModel.titleError.observe(viewLifecycleOwner) { binding.tilTitle.error = it }

        viewModel.amountError.observe(viewLifecycleOwner) { binding.tilAmount.error = it }

        viewModel.categoryError.observe(viewLifecycleOwner) { binding.tilCategory.error = it }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {

                Toast.makeText(
                    requireContext(),
                    "Transacción guardada exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                NotificationHelper.showNotification(
                    requireContext(),
                    "Transacción creada",
                    "Se guardó correctamente"
                )

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
