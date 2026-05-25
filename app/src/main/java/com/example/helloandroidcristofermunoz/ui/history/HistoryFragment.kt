package com.example.helloandroidcristofermunoz.ui.history

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentHistoryBinding
import com.example.helloandroidcristofermunoz.ui.home.TransactionAdapter
import java.text.NumberFormat
import java.text.SimpleDateFormat
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

        val monthText = "${monthNames[currentMonth]} $currentYear"
        binding.monthSelector.removeAllViews()

        val monthButton = android.widget.Button(requireContext()).apply {
            text = monthText
            textSize = 16f
            setPadding(32, 16, 32, 16)
            setBackgroundColor(Color.parseColor("#2196F3"))
            setTextColor(Color.WHITE)
            isAllCaps = false

            setOnClickListener {
                showMonthYearPicker()
            }
        }

        binding.monthSelector.addView(monthButton)
    }

    private fun showMonthYearPicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, _ ->
                viewModel.selectMonth(selectedMonth, selectedYear)
                updateMonthSelectorUI(selectedMonth, selectedYear)
            },
            year, month, 1
        )

        // Configurar para mostrar solo mes y año
        try {
            val datePicker = datePickerDialog.datePicker
            val dayField = datePicker.findViewById<View>(
                resources.getIdentifier("day", "id", "android")
            )
            if (dayField != null) {
                dayField.visibility = View.GONE
            }
        } catch (e: Exception) {
            // Si no se puede ocultar el día, se muestra normal
        }

        datePickerDialog.show()
    }

    private fun updateMonthSelectorUI(selectedMonth: Int, selectedYear: Int) {
        val monthText = "${monthNames[selectedMonth]} $selectedYear"
        val button = binding.monthSelector.getChildAt(0) as android.widget.Button
        button.text = monthText
    }

    private fun setupRecycler() {
        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeData() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            val adapter = TransactionAdapter(
                transactions,
                onItemClick = { }
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
