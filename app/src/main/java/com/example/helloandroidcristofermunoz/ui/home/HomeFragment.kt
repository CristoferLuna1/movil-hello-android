package com.example.helloandroidcristofermunoz.ui.home

import android.widget.Toast
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.dao.SavingsPlanDao
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentHomeBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutEmptyStateBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutErrorStateBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutLoadingStateBinding
import com.example.helloandroidcristofermunoz.utils.AmountFormatter
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {

        val dao = AppDatabase
            .getDatabase(requireContext())
            .transactionDao()

        val savingsDao = AppDatabase
            .getDatabase(requireContext())
            .savingsPlanDao()

        val repository = TransactionRepository(dao, savingsDao)

        HomeViewModelFactory(repository)
    }

    private val savingsPlanDao: SavingsPlanDao by lazy {
        AppDatabase.getDatabase(requireContext()).savingsPlanDao()
    }

    private val userDao by lazy {
        AppDatabase.getDatabase(requireContext()).userDao()
    }

    private var emptyStateBinding: LayoutEmptyStateBinding? = null
    private var loadingStateBinding: LayoutLoadingStateBinding? = null
    private var errorStateBinding: LayoutErrorStateBinding? = null

    private var currentUserId = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        loadCurrentUser()
        setupStates()
        setupRecycler()
        loadSavingsPlan()
        observeData()
    }

    private fun loadCurrentUser() {
        try {
            runBlocking {
                val currentUser = userDao.getLoggedInUser()
                currentUser?.let {
                    currentUserId = it.id
                }
            }
        } catch (e: Exception) {
            // Manejar error
        }
    }

    private fun loadSavingsPlan() {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val currentMonthYear = calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)

            try {
                val savingsPlan = savingsPlanDao.getActiveSavingsPlanForMonth(currentUserId, currentMonthYear)
                
                if (savingsPlan != null) {
                    // Calcular balance total basado en transacciones del mes
                    val transactions = viewModel.transactions.value ?: emptyList()
                    
                    // Filtrar solo transacciones del mes actual
                    val currentMonthTransactions = transactions.filter { it.monthYear == currentMonthYear }
                    
                    val income = currentMonthTransactions.filter { it.type == "income" }.sumOf { it.amount }
                    val expenses = currentMonthTransactions.filter { it.type == "expense" }.sumOf { it.amount }
                    
                    // Calcular balance total (ingresos - gastos)
                    val totalBalance = income - expenses
                    
                    // Asegurar que el balance no sea negativo
                    val safeBalance = maxOf(totalBalance, 0.0)
                    
                    // Recalcular la meta mensual basada en el balance total
                    // Si el plan es multimonth, recalculamos la meta basada en el total restante
                    val originalMonthlyGoal = savingsPlan.monthlyGoal
                    val remainingGoal = savingsPlan.totalGoal - savingsPlan.currentSaved
                    val remainingMonths = savingsPlan.totalMonths - savingsPlan.paidInstallments
                    
                    // La meta mensual recalculada es el total restante dividido por los meses restantes
                    val recalculatedMonthlyGoal = if (remainingMonths > 0) {
                        remainingGoal / remainingMonths
                    } else {
                        originalMonthlyGoal
                    }
                    
                    // La meta del mes es el mínimo entre la meta recalculada y el balance total
                    // Si el balance es menor a 10.000, no se ahorra este mes
                    val monthlyGoal = if (safeBalance < 10000) {
                        0.0
                    } else if (safeBalance < recalculatedMonthlyGoal) {
                        safeBalance
                    } else {
                        recalculatedMonthlyGoal
                    }
                    
                    // Calcular déficit
                    // Si el balance es menor a 10.000, el déficit es la meta completa
                    // Si el balance es mayor pero menor que la meta, el déficit es la diferencia
                    val deficit = if (safeBalance < 10000) {
                        recalculatedMonthlyGoal
                    } else if (safeBalance < recalculatedMonthlyGoal) {
                        recalculatedMonthlyGoal - safeBalance
                    } else {
                        0.0
                    }
                    
                    // Determinar meses de compensación: siempre 3 meses
                    val compensationMonths = 3
                    val compensationPerMonth = if (deficit > 0) deficit / compensationMonths else 0.0
                    
                    val progress = if (monthlyGoal > 0) {
                        ((savingsPlan.currentSaved / monthlyGoal) * 100).toInt()
                    } else {
                        0
                    }
                    
                    binding.txtSavingsGoal.text = "Meta este mes: $${AmountFormatter.format(monthlyGoal)}"
                    binding.txtSavingsProgress.text = "${progress}%"
                    binding.progressSavings.progress = progress
                    
                    // Mostrar información de cuotas
                    val installmentInfo = if (savingsPlan.totalMonths > 1) {
                        val nextInstallment = savingsPlan.paidInstallments + 1
                        val calendar = Calendar.getInstance()
                        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                        "Cuota $nextInstallment de ${savingsPlan.totalMonths} (Día $currentDay)"
                    } else {
                        "Ahorro mensual"
                    }
                    
                    // Calcular lo pagado y lo que falta
                    val paidAmount = savingsPlan.currentSaved
                    val remainingAmount = savingsPlan.totalGoal - savingsPlan.currentSaved
                    
                    if (deficit > 0) {
                        val deficitMessage = if (safeBalance < 10000) {
                            "No se ahorra este mes (balance < $10.000). Déficit: $${AmountFormatter.format(deficit)} - Se distribuirá en $compensationMonths cuotas ($${AmountFormatter.format(compensationPerMonth)}/cuota adicional)"
                        } else {
                            "Abonas $${AmountFormatter.format(monthlyGoal)} (balance). Faltan $${AmountFormatter.format(deficit)} - Se distribuirán en $compensationMonths cuotas ($${AmountFormatter.format(compensationPerMonth)}/cuota adicional)"
                        }
                        binding.txtAvailableWithoutSavings.text = "$installmentInfo\nPagado: $${AmountFormatter.format(paidAmount)} - Faltan: $${AmountFormatter.format(remainingAmount)}\n$deficitMessage"
                    } else {
                        binding.txtAvailableWithoutSavings.text = "$installmentInfo\nPagado: $${AmountFormatter.format(paidAmount)} - Faltan: $${AmountFormatter.format(remainingAmount)}"
                    }
                    
                    binding.cardSavings.visibility = View.VISIBLE

                    // Configurar botón de ahorro (pago de cuota)
                    binding.btnSaveSavings.setOnClickListener {
                        // Verificar si ya existe una transacción de ahorro en este mes
                        val existingSavingsTransaction = transactions.filter {
                            it.category == "Ahorro" && it.monthYear == currentMonthYear
                        }

                        if (existingSavingsTransaction.isNotEmpty()) {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Ya has realizado tu pago de cuota este mes. Solo puedes hacer un pago por mes.",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            saveSavingsTransaction(monthlyGoal, currentMonthYear, true)
                        }
                    }

                    // Configurar botón de abono adicional
                    binding.btnMakePayment.setOnClickListener {
                        showPaymentDialog(monthlyGoal, currentMonthYear)
                    }

                    // Cambiar color de la barra según progreso
                    when {
                        progress >= 90 -> binding.progressSavings.progressTintList =
                            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F44336"))
                        progress >= 70 -> binding.progressSavings.progressTintList =
                            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF9800"))
                        else -> binding.progressSavings.progressTintList =
                            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50"))
                    }
                } else {
                    binding.cardSavings.visibility = View.GONE
                }
            } catch (e: Exception) {
                // Manejar error silenciosamente para evitar cierre de app
                binding.cardSavings.visibility = View.GONE
            }
        }
    }

    private fun saveSavingsTransaction(amount: Double, monthYear: Int, isInstallment: Boolean) {
        lifecycleScope.launch {
            try {
                val transactionDao = AppDatabase.getDatabase(requireContext()).transactionDao()
                val savingsPlanDao = AppDatabase.getDatabase(requireContext()).savingsPlanDao()
                
                // Obtener el plan actual
                val currentPlan = savingsPlanDao.getActiveSavingsPlanForMonth(currentUserId, monthYear)
                
                // Crear transacción de ahorro
                val title = if (isInstallment) "Ahorro del mes" else "Abono adicional"
                val savingsTransaction = com.example.helloandroidcristofermunoz.data.model.Transaction(
                    title = title,
                    amount = amount,
                    category = "Ahorro",
                    customCategory = null,
                    type = "expense", // Ahorro se registra como gasto para reducir el balance disponible
                    date = System.currentTimeMillis(),
                    paymentDay = null,
                    endDate = null,
                    isMonthlyPersistent = false,
                    monthYear = monthYear,
                    userId = currentUserId
                )
                
                transactionDao.insert(savingsTransaction)
                
                // Actualizar plan de ahorro
                currentPlan?.let {
                    val updatedPlan = if (isInstallment) {
                        val newPaidInstallments = it.paidInstallments + 1
                        it.copy(
                            currentSaved = it.currentSaved + amount,
                            paidInstallments = newPaidInstallments
                        )
                    } else {
                        // Abono adicional: solo incrementa currentSaved, no paidInstallments
                        it.copy(
                            currentSaved = it.currentSaved + amount
                        )
                    }
                    savingsPlanDao.update(updatedPlan)
                    
                    val message = if (isInstallment) {
                        if (it.totalMonths > 1) {
                            val newPaidInstallments = it.paidInstallments + 1
                            val remainingGoal = it.totalGoal - (it.currentSaved + amount)
                            val remainingMonths = it.totalMonths - newPaidInstallments
                            val nextPayment = if (remainingMonths > 0) remainingGoal / remainingMonths else 0.0
                            "Ahorro registrado: $${AmountFormatter.format(amount)}\nCuota ${newPaidInstallments} de ${it.totalMonths}\nSiguiente pago: $${AmountFormatter.format(nextPayment)}"
                        } else {
                            "Ahorro registrado: $${AmountFormatter.format(amount)}"
                        }
                    } else {
                        "Abono registrado: $${AmountFormatter.format(amount)}\nTotal ahorrado: $${AmountFormatter.format(it.currentSaved + amount)}"
                    }
                    
                    android.widget.Toast.makeText(
                        requireContext(),
                        message,
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } ?: run {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Ahorro registrado: $${AmountFormatter.format(amount)}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                
                // Recargar datos
                viewModel.retryLoad()
            } catch (e: Exception) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Error al registrar ahorro",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showPaymentDialog(monthlyGoal: Double, monthYear: Int) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Abonar al plan de ahorro")
        
        val input = android.widget.EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "Monto a abonar"
        builder.setView(input)
        
        builder.setPositiveButton("Abonar") { _, _ ->
            val amountStr = input.text.toString()
            if (amountStr.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    saveSavingsTransaction(amount, monthYear, false)
                } else {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Ingrese un monto válido",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }
        
        builder.show()
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

        binding.recyclerTransactions.layoutManager =
            LinearLayoutManager(requireContext())
    }

    private fun observeData() {

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->

            val adapter = TransactionAdapter(transactions) { transaction ->

                val action = HomeFragmentDirections
                    .actionHomeFragmentToTransactionDetailFragment(transaction.id)

                findNavController().navigate(action)
            }

            binding.recyclerTransactions.adapter = adapter

            // Calcular balance total
            val balance = transactions.filter { it.type == "income" }
                .sumOf { it.amount } - transactions.filter { it.type == "expense" }
                .sumOf { it.amount }

            binding.txtBalance.text = "$${AmountFormatter.format(balance)}"

            if (transactions.isNotEmpty()) {
                showContentState()
            }

            // Recalcular plan de ahorro cuando cambian las transacciones
            loadSavingsPlan()
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