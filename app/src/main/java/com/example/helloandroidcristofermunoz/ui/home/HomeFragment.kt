package com.example.helloandroidcristofermunoz.ui.home

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.example.helloandroidcristofermunoz.utils.NetworkMonitor
import com.example.helloandroidcristofermunoz.utils.NetworkState
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

        NetworkMonitor.start(requireContext())

        NetworkState.isConnected.observe(viewLifecycleOwner) { connected ->
            binding.txtNetworkStatus.text =
                if (connected) "🟢 Conectado a Internet"
                else "🔴 Sin conexión"
        }

        setupStates()
        setupRecycler()
        observeData()
        setupSavingsGoal()
    }

    private fun setupStates() {
        emptyStateBinding = LayoutEmptyStateBinding.inflate(layoutInflater, binding.root as ViewGroup, false)
        loadingStateBinding = LayoutLoadingStateBinding.inflate(layoutInflater, binding.root as ViewGroup, false)
        errorStateBinding = LayoutErrorStateBinding.inflate(layoutInflater, binding.root as ViewGroup, false)

        errorStateBinding?.btnRetry?.setOnClickListener {
            viewModel.retryLoad()
        }
    }

    private fun setupRecycler() {
        binding.recyclerTransactions.layoutManager =
            LinearLayoutManager(requireContext())
    }

    private fun observeData() {
        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            val formatted = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(balance)
            binding.txtBalance.text = formatted
        }

        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            binding.recyclerTransactions.adapter =
                TransactionAdapter(transactions) { transaction ->
                    showTransactionDetailDialog(transaction)
                }

            if (transactions.isNotEmpty()) showContentState()

            updateSavingsProgress(transactions)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { if (it) showLoadingState() }
        viewModel.isError.observe(viewLifecycleOwner) { if (it) showErrorState() }
        viewModel.isEmpty.observe(viewLifecycleOwner) { if (it) showEmptyState() }
    }

    private fun setupSavingsGoal() {
        binding.btnSetGoal.setOnClickListener { showSetGoalDialog() }
        loadCurrentGoal()
    }

    private fun loadCurrentGoal() {
        val calendar = Calendar.getInstance()

        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).savingsGoalDao()
            currentSavingsGoal =
                dao.getGoalForMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))

            updateGoalUI()
        }
    }

    private fun updateGoalUI() {
        val goal = currentSavingsGoal

        if (goal == null) {
            binding.txtGoalAmount.text = "Sin meta configurada"
            binding.markerLimit.visibility = View.GONE
            return
        }

        val formatted = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(goal.amount)
        binding.txtGoalAmount.text = "Meta: $formatted"
        binding.markerLimit.visibility = View.VISIBLE
    }

    private fun updateSavingsProgress(transactions: List<com.example.helloandroidcristofermunoz.data.model.Transaction>) {
        val goal = currentSavingsGoal ?: return

        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val monthly = transactions.filter {
            val c = Calendar.getInstance()
            c.timeInMillis = it.date
            c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year
        }

        val income = monthly.filter { it.type == "Ingreso" }.sumOf { it.amount }
        val expenses = monthly.filter { it.type == "Gasto" }.sumOf { it.amount }

        val available = income - goal.amount
        val spentPercent = if (available > 0) (expenses / available * 100).toInt() else 100

        binding.txtAvailableBudget.text =
            "Presupuesto: ${NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(available)}"

        binding.txtSpent.text =
            "Gastado: ${NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(expenses)}"

        binding.progressSavings.progress = spentPercent.coerceAtMost(100)
    }

    private fun showSetGoalDialog() {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_set_goal, null)
        val edt = view.findViewById<EditText>(R.id.edtGoalAmount)

        currentSavingsGoal?.let {
            edt.setText(it.amount.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Meta de ahorro")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val amount = edt.text.toString().toDoubleOrNull() ?: return@setPositiveButton
                saveGoal(amount)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveGoal(amount: Double) {
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).savingsGoalDao()
            val calendar = Calendar.getInstance()

            val goal = currentSavingsGoal

            if (goal == null) {
                dao.insert(
                    SavingsGoal(
                        amount = amount,
                        month = calendar.get(Calendar.MONTH),
                        year = calendar.get(Calendar.YEAR)
                    )
                )
            } else {
                dao.update(goal.copy(amount = amount))
            }

            loadCurrentGoal()
        }
    }

    private fun showTransactionDetailDialog(transaction: com.example.helloandroidcristofermunoz.data.model.Transaction) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_transaction_detail_improved)

        dialog.findViewById<TextView>(R.id.txtDetailTitle).text = transaction.title
        dialog.findViewById<TextView>(R.id.txtDetailCategory).text = transaction.category
        dialog.findViewById<TextView>(R.id.txtDetailType).text = transaction.type

        dialog.show()
    }

    private fun showEditDialog(transaction: com.example.helloandroidcristofermunoz.data.model.Transaction) {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_add_transaction, null)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupType)

        val type = when (radioGroup.checkedRadioButtonId) {
            R.id.rbIncome -> "Ingreso"
            R.id.rbExpense -> "Gasto"
            else -> "Gasto"
        }

        // Aquí ya puedes actualizar con ViewModel si quieres
    }

    private fun showDeleteDialog(transaction: com.example.helloandroidcristofermunoz.data.model.Transaction) {
        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
            dao.delete(transaction)
            viewModel.retryLoad()
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