package com.example.helloandroidcristofermunoz.ui.history

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentHistoryBinding
import com.example.helloandroidcristofermunoz.ui.home.TransactionAdapter
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
        val repository = TransactionRepository(dao)
        HistoryViewModelFactory(repository)
    }

    private val monthNames = arrayOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        setupMonthSelector()
        setupRecycler()
        observeData()
    }

    private fun setupMonthSelector() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        // Mostrar últimos 12 meses
        for (i in 11 downTo 0) {
            val monthCalendar = Calendar.getInstance()
            monthCalendar.add(Calendar.MONTH, -i)

            val month = monthCalendar.get(Calendar.MONTH)
            val year = monthCalendar.get(Calendar.YEAR)

            val monthButton = TextView(requireContext()).apply {
                text = monthNames[month]
                textSize = 16f
                setPadding(24, 16, 24, 16)
                isClickable = true
                isFocusable = true

                if (month == currentMonth && year == currentYear) {
                    setTextColor(Color.parseColor("#2196F3"))
                } else {
                    setTextColor(Color.parseColor("#666"))
                }

                setOnClickListener {
                    viewModel.selectMonth(month, year)
                    updateMonthSelectorUI(month, year)
                }
            }

            binding.monthSelector.addView(monthButton)
        }
    }

    private fun updateMonthSelectorUI(selectedMonth: Int, selectedYear: Int) {
        for (i in 0 until binding.monthSelector.childCount) {
            val button = binding.monthSelector.getChildAt(i) as TextView
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -(11 - i))
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            if (month == selectedMonth && year == selectedYear) {
                button.setTextColor(Color.parseColor("#2196F3"))
            } else {
                button.setTextColor(Color.parseColor("#666"))
            }
        }
    }

    private fun setupRecycler() {
        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeData() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val adapter = TransactionAdapter(
                transactions,
                onItemClick = { transaction ->
                    // Callback vacío para evitar crash
                }
            )
            binding.recyclerHistory.adapter = adapter
        }

        viewModel.monthIncome.observe(viewLifecycleOwner) { income ->
            val formatted = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(income)
            binding.txtMonthIncome.text = formatted
        }

        viewModel.monthExpenses.observe(viewLifecycleOwner) { expenses ->
            val formatted = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(expenses)
            binding.txtMonthExpenses.text = formatted
        }

        viewModel.monthBalance.observe(viewLifecycleOwner) { balance ->
            val formatted = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(balance)
            binding.txtMonthBalance.text = formatted

            // Color según balance positivo o negativo
            binding.txtMonthBalance.setTextColor(
                if (balance >= 0) Color.parseColor("#212121") else Color.parseColor("#F44336")
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
