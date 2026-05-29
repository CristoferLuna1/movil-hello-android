package com.example.helloandroidcristofermunoz.ui.statistics

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentStatisticsBinding
import com.example.helloandroidcristofermunoz.utils.AmountFormatter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.NumberFormat
import java.util.Locale
import kotlin.collections.listOf

class StatisticsFragment :
    Fragment(R.layout.fragment_statistics) {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels {
        val dao = AppDatabase
            .getDatabase(requireContext())
            .transactionDao()

        val savingsDao = AppDatabase
            .getDatabase(requireContext())
            .savingsPlanDao()

        val repository = TransactionRepository(dao, savingsDao)

        StatisticsViewModelFactory(repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentStatisticsBinding.bind(view)

        val userId = 1 // 👈 TEMPORAL (cámbialo por sesión real)

        viewModel.loadStatistics(userId)
        observeData()
    }

    private fun observeData() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("StatisticsFragment", "Loading: $isLoading")
        }

        viewModel.isError.observe(viewLifecycleOwner) { isError ->
            Log.d("StatisticsFragment", "Error: $isError")
            if (isError) {
                binding.txtIncome.text = "Error"
                binding.txtExpenses.text = "Error"
            }
        }

        viewModel.income.observe(viewLifecycleOwner) { income ->
            val formattedIncome = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(income)
            binding.txtIncome.text = formattedIncome

            val expenses = viewModel.expenses.value ?: 0.0

        }

        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            binding.txtExpenses.text = "$${AmountFormatter.format(expenses)}"
            updateChart()
        }

        viewModel.balance.observe(viewLifecycleOwner) { balance ->
            binding.txtBalance.text = "Balance: $${AmountFormatter.format(balance)}"
        }

        viewModel.categoryExpenses.observe(viewLifecycleOwner) { categoryExpenses ->
            updateCategoryChart(categoryExpenses)
        }
    }

    private fun updateChart() {
        val income = viewModel.income.value?.toFloat() ?: 0f
        val expenses = viewModel.expenses.value?.toFloat() ?: 0f

        val entries = arrayListOf(
            PieEntry(income, "Ingresos"),
            PieEntry(expenses, "Gastos")
        )

        val dataSet = PieDataSet(entries, "Finanzas")
        dataSet.valueTextSize = 14f
        dataSet.colors = listOf(
            android.graphics.Color.parseColor("#4CAF50"),
            android.graphics.Color.parseColor("#F44336")
        )

        val data = PieData(dataSet)
        binding.pieChart.data = data
        binding.pieChart.description.isEnabled = false
        binding.pieChart.centerText = "Resumen"
        binding.pieChart.setEntryLabelTextSize(14f)
        binding.pieChart.animateY(1000)
        binding.pieChart.invalidate()
    }

    private fun updateCategoryChart(categoryExpenses: Map<String, Double>) {
        val entries = categoryExpenses.map { (category, amount) ->
            PieEntry(amount.toFloat(), category)
        }

        if (entries.isNotEmpty()) {
            val dataSet = PieDataSet(entries, "Gastos por Categoría")
            dataSet.valueTextSize = 12f
            dataSet.colors = listOf(
                android.graphics.Color.parseColor("#F44336"),
                android.graphics.Color.parseColor("#FF9800"),
                android.graphics.Color.parseColor("#FFC107"),
                android.graphics.Color.parseColor("#9C27B0"),
                android.graphics.Color.parseColor("#2196F3")
            )

            val data = PieData(dataSet)
            binding.categoryPieChart.data = data
            binding.categoryPieChart.description.isEnabled = false
            binding.categoryPieChart.centerText = "Categorías"
            binding.categoryPieChart.setEntryLabelTextSize(12f)
            binding.categoryPieChart.animateY(1000)
            binding.categoryPieChart.invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
