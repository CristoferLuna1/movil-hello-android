package com.example.helloandroidcristofermunoz.ui.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentHistoryBinding
import com.example.helloandroidcristofermunoz.ui.home.TransactionAdapter
import com.example.helloandroidcristofermunoz.utils.AmountFormatter
import java.util.Calendar

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
        val repository = TransactionRepository(dao)
        HistoryViewModelFactory(repository)
    }

    private val currentUserId = 1 // TODO: Obtener del usuario actual

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        setupToolbar()
        setupMonthSelector()
        setupRecycler()
        observeData()
    }

    private fun setupToolbar() {
        binding.toolbarHistory.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupMonthSelector() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        val months = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        val years = (currentYear downTo currentYear - 12).toList()

        val monthAdapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            months
        )
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter
        binding.spinnerMonth.setSelection(currentMonth - 1)

        val yearAdapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            years.map { it.toString() }
        )
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter

        binding.spinnerMonth.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadMonthData()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        binding.spinnerYear.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadMonthData()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun loadMonthData() {
        val month = binding.spinnerMonth.selectedItemPosition + 1
        val year = binding.spinnerYear.selectedItem.toString().toInt()
        val monthYear = year * 100 + month

        viewModel.loadTransactionsForMonth(currentUserId, monthYear)
    }

    private fun setupRecycler() {
        binding.recyclerTransactions.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeData() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val adapter = TransactionAdapter(transactions) { transaction ->
                // Navegar a detalles si es necesario
            }
            binding.recyclerTransactions.adapter = adapter

            // Calcular balance del mes
            val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
            val expense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
            val balance = income - expense

            binding.txtIncome.text = "Ingresos: $${AmountFormatter.format(income)}"
            binding.txtExpense.text = "Gastos: $${AmountFormatter.format(expense)}"
            binding.txtBalance.text = "Balance: $${AmountFormatter.format(balance)}"

            if (transactions.isEmpty()) {
                binding.recyclerTransactions.visibility = View.GONE
                binding.txtEmpty.visibility = View.VISIBLE
            } else {
                binding.recyclerTransactions.visibility = View.VISIBLE
                binding.txtEmpty.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
