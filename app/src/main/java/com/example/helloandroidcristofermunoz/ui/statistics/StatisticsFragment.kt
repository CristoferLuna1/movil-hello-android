package com.example.helloandroidcristofermunoz.ui.statistics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlin.collections.listOf

class StatisticsFragment :
    Fragment(R.layout.fragment_statistics) {

    private var _binding: FragmentStatisticsBinding? = null

    private val binding get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentStatisticsBinding.bind(view)

        viewModel.setContext(requireContext())
        viewModel.loadStatistics()
        observeData()
    }

    private fun observeData() {

        viewModel.income.observe(viewLifecycleOwner) { income ->

            binding.txtIncome.text = "$ $income"

            val expenses =
                viewModel.expenses.value ?: 0.0

            setupChart(
                income.toFloat(),
                expenses.toFloat()
            )
        }

        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->

            binding.txtExpenses.text = "$ $expenses"

            val income =
                viewModel.income.value ?: 0.0

            setupChart(
                income.toFloat(),
                expenses.toFloat()
            )
        }
    }

    private fun setupChart(
        income: Float,
        expenses: Float
    ) {

        val entries = arrayListOf(

            PieEntry(
                income,
                "Ingresos"
            ),

            PieEntry(
                expenses,
                "Gastos"
            )
        )

        val dataSet =
            PieDataSet(entries, "Finanzas")

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

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}