package com.example.helloandroidcristofermunoz.ui.home

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.model.SavingsGoal
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentHomeBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutEmptyStateBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutErrorStateBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutLoadingStateBinding
import com.example.helloandroidcristofermunoz.ui.addtransaction.AddTransactionViewModel
import com.example.helloandroidcristofermunoz.ui.addtransaction.AddTransactionViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
        val repository = TransactionRepository(dao)
        HomeViewModelFactory(repository)
    }

    private var emptyStateBinding: LayoutEmptyStateBinding? = null
    private var loadingStateBinding: LayoutLoadingStateBinding? = null
    private var errorStateBinding: LayoutErrorStateBinding? = null

    private var currentSavingsGoal: SavingsGoal? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupStates()
        setupRecycler()
        observeData()
        setupSavingsGoal()
    }

    private fun setupStates() {
        emptyStateBinding = LayoutEmptyStateBinding.inflate(
            LayoutInflater.from(requireContext()),
            binding.root as ViewGroup,
            false
        )

        loadingStateBinding = LayoutLoadingStateBinding.inflate(
            LayoutInflater.from(requireContext()),
            binding.root as ViewGroup,
            false
        )

        errorStateBinding = LayoutErrorStateBinding.inflate(
            LayoutInflater.from(requireContext()),
            binding.root as ViewGroup,
            false
        )

        errorStateBinding?.btnRetry?.setOnClickListener {
            viewModel.retryLoad()
        }
    }

    private fun setupRecycler() {
        binding.recyclerTransactions.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeData() {
        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            val formattedBalance = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(balance)
            binding.txtBalance.text = formattedBalance
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val adapter = TransactionAdapter(
                transactions,
                onItemClick = { transaction ->
                    showTransactionDetailDialog(transaction)
                }
            )
            binding.recyclerTransactions.adapter = adapter

            if (transactions.isNotEmpty()) {
                showContentState()
            }

            updateSavingsProgress(transactions)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                showLoadingState()
            }
        }

        viewModel.isError.observe(viewLifecycleOwner) { isError ->
            if (isError) {
                showErrorState()
            }
        }

        viewModel.isEmpty.observe(viewLifecycleOwner) { isEmpty ->
            if (isEmpty) {
                showEmptyState()
            }
        }
    }

    private fun setupSavingsGoal() {
        binding.btnSetGoal.setOnClickListener {
            showSetGoalDialog()
        }

        loadCurrentGoal()
    }

    private fun loadCurrentGoal() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).savingsGoalDao()
            currentSavingsGoal = dao.getGoalForMonth(currentMonth, currentYear)

            updateGoalUI()
        }
    }

    private fun updateGoalUI() {
        if (currentSavingsGoal == null) {
            binding.txtGoalAmount.text = "Sin meta configurada"
            binding.txtAvailableBudget.text = "Configura tu meta de ahorro"
            binding.txtSpent.text = ""
            binding.progressSavings.progress = 0
            binding.txtProgressText.text = ""
            return
        }

        val formattedGoal = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(currentSavingsGoal!!.amount)
        binding.txtGoalAmount.text = "Meta: $formattedGoal"
    }

    private fun updateSavingsProgress(transactions: List<com.example.helloandroidcristofermunoz.data.model.Transaction>) {
        if (currentSavingsGoal == null) return

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Filtrar transacciones del mes actual
        val monthlyTransactions = transactions.filter { transaction ->
            val txCalendar = Calendar.getInstance()
            txCalendar.timeInMillis = transaction.date
            txCalendar.get(Calendar.MONTH) == currentMonth && txCalendar.get(Calendar.YEAR) == currentYear
        }

        // Calcular ingresos y gastos del mes
        val monthlyIncome = monthlyTransactions.filter { it.type == "Ingreso" }.sumOf { it.amount }
        val monthlyExpenses = monthlyTransactions.filter { it.type == "Gasto" }.sumOf { it.amount }

        // Calcular presupuesto disponible (ingresos - meta de ahorro)
        val availableBudget = monthlyIncome - currentSavingsGoal!!.amount

        // Calcular porcentaje gastado
        val spentPercentage = if (availableBudget > 0) {
            (monthlyExpenses / availableBudget * 100).toInt()
        } else {
            100
        }

        // Actualizar UI
        val formattedBudget = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(availableBudget)
        val formattedSpent = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(monthlyExpenses)

        binding.txtAvailableBudget.text = "Presupuesto disponible: $formattedBudget"
        binding.txtSpent.text = "Gastado: $formattedSpent"
        binding.progressSavings.progress = spentPercentage.coerceAtMost(100)
        binding.txtProgressText.text = "$spentPercentage% del presupuesto gastado"

        // Cambiar color según progreso
        when {
            spentPercentage >= 90 -> {
                binding.progressSavings.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))
                binding.txtAvailableBudget.setTextColor(Color.parseColor("#F44336"))
                binding.txtProgressText.text = "$spentPercentage% - ¡Cuidado! Estás cerca de tu meta de ahorro"
            }
            spentPercentage >= 70 -> {
                binding.progressSavings.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))
                binding.txtAvailableBudget.setTextColor(Color.parseColor("#FF9800"))
            }
            else -> {
                binding.progressSavings.progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                binding.txtAvailableBudget.setTextColor(Color.parseColor("#4CAF50"))
            }
        }
    }

    private fun showSetGoalDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_set_goal, null)
        val edtGoalAmount = dialogView.findViewById<EditText>(R.id.edtGoalAmount)

        if (currentSavingsGoal != null) {
            edtGoalAmount.setText(currentSavingsGoal!!.amount.toString())
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Configurar Meta de Ahorro")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val amountStr = edtGoalAmount.text.toString().trim()
                if (amountStr.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    saveSavingsGoal(amount)
                } else {
                    Toast.makeText(requireContext(), "Ingresa un monto válido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveSavingsGoal(amount: Double) {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).savingsGoalDao()

            if (currentSavingsGoal == null) {
                val newGoal = SavingsGoal(
                    amount = amount,
                    month = currentMonth,
                    year = currentYear
                )
                dao.insert(newGoal)
            } else {
                val updatedGoal = currentSavingsGoal!!.copy(amount = amount)
                dao.update(updatedGoal)
            }

            loadCurrentGoal()
            Toast.makeText(requireContext(), "Meta de ahorro guardada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTransactionDetailDialog(transaction: com.example.helloandroidcristofermunoz.data.model.Transaction) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_transaction_detail_improved)

        val txtDetailTitle = dialog.findViewById<TextView>(R.id.txtDetailTitle)
        val txtDetailCategory = dialog.findViewById<TextView>(R.id.txtDetailCategory)
        val txtDetailType = dialog.findViewById<TextView>(R.id.txtDetailType)
        val txtDetailDate = dialog.findViewById<TextView>(R.id.txtDetailDate)
        val txtDetailAmount = dialog.findViewById<TextView>(R.id.txtDetailAmount)
        val btnEdit = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEdit)
        val btnDelete = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDelete)

        txtDetailTitle.text = transaction.title
        txtDetailCategory.text = transaction.category
        txtDetailType.text = transaction.type

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))
        val date = Date(transaction.date)
        txtDetailDate.text = dateFormat.format(date)

        val formattedAmount = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(transaction.amount)
        txtDetailAmount.text = formattedAmount

        if (transaction.type == "Ingreso") {
            txtDetailAmount.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            txtDetailAmount.setTextColor(Color.parseColor("#FFFFFF"))
        }

        btnEdit.setOnClickListener {
            dialog.dismiss()
            showEditDialog(transaction)
        }

        btnDelete.setOnClickListener {
            dialog.dismiss()
            showDeleteDialog(transaction)
        }

        dialog.show()
    }

    private fun showEditDialog(transaction: com.example.helloandroidcristofermunoz.data.model.Transaction) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_add_transaction, null)
        
        val edtTitle = dialogView.findViewById<EditText>(R.id.edtTitle)
        val edtAmount = dialogView.findViewById<EditText>(R.id.edtAmount)
        val edtCategory = dialogView.findViewById<EditText>(R.id.edtCategory)
        val radioGroupType = dialogView.findViewById<RadioGroup>(R.id.radioGroupType)

        edtTitle.setText(transaction.title)
        edtAmount.setText(transaction.amount.toString())
        edtCategory.setText(transaction.category)

        if (transaction.type == "Ingreso") {
            radioGroupType.check(R.id.rbIncome)
        } else {
            radioGroupType.check(R.id.rbExpense)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Editar Transacción")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val title = edtTitle.text.toString().trim()
                val amountStr = edtAmount.text.toString().trim()
                val category = edtCategory.text.toString().trim()

                val selectedTypeId = radioGroupType.checkedRadioButtonId
                val selectedRadioButton = dialogView.findViewById<RadioButton>(selectedTypeId)
                val type = if (selectedRadioButton?.id == R.id.rbIncome) "Ingreso" else "Gasto"

                if (title.isNotEmpty() && amountStr.isNotEmpty() && category.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    
                    val updatedTransaction = transaction.copy(
                        title = title,
                        amount = amount,
                        category = category,
                        type = type
                    )

                    val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
                    val repository = TransactionRepository(dao)
                    val factory = AddTransactionViewModelFactory(repository)
                    val editViewModel = ViewModelProvider(this, factory)[AddTransactionViewModel::class.java]

                    editViewModel.updateTransaction(updatedTransaction)

                    editViewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
                        if (isSuccess) {
                            Toast.makeText(requireContext(), "Transacción actualizada", Toast.LENGTH_SHORT).show()
                            editViewModel.resetSuccessState()
                            viewModel.retryLoad()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteDialog(transaction: com.example.helloandroidcristofermunoz.data.model.Transaction) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar transacción")
            .setMessage("¿Estás seguro de eliminar ${transaction.title}?")
            .setPositiveButton("Eliminar") { _, _ ->
                val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
                val repository = TransactionRepository(dao)
                val factory = AddTransactionViewModelFactory(repository)
                val deleteViewModel = ViewModelProvider(this, factory)[AddTransactionViewModel::class.java]

                deleteViewModel.deleteTransaction(transaction)

                deleteViewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
                    if (isSuccess) {
                        Toast.makeText(requireContext(), "Transacción eliminada", Toast.LENGTH_SHORT).show()
                        deleteViewModel.resetSuccessState()
                        viewModel.retryLoad()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLoadingState() {
        binding.recyclerTransactions.visibility = View.GONE
        loadingStateBinding?.root?.visibility = View.VISIBLE
        emptyStateBinding?.root?.visibility = View.GONE
        errorStateBinding?.root?.visibility = View.GONE
    }

    private fun showErrorState() {
        binding.recyclerTransactions.visibility = View.GONE
        loadingStateBinding?.root?.visibility = View.GONE
        emptyStateBinding?.root?.visibility = View.GONE
        errorStateBinding?.root?.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        binding.recyclerTransactions.visibility = View.GONE
        loadingStateBinding?.root?.visibility = View.GONE
        emptyStateBinding?.root?.visibility = View.VISIBLE
        errorStateBinding?.root?.visibility = View.GONE
    }

    private fun showContentState() {
        binding.recyclerTransactions.visibility = View.VISIBLE
        loadingStateBinding?.root?.visibility = View.GONE
        emptyStateBinding?.root?.visibility = View.GONE
        errorStateBinding?.root?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        emptyStateBinding = null
        loadingStateBinding = null
        errorStateBinding = null
    }
}
