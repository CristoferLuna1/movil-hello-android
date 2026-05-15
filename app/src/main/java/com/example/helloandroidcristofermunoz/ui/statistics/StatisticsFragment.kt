package com.example.helloandroidcristofermunoz.ui.statistics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.databinding.FragmentStatisticsBinding

class StatisticsFragment :
    Fragment(R.layout.fragment_statistics) {

    private var _binding:
            FragmentStatisticsBinding? = null

    private val binding get() = _binding!!

    private val viewModel:
            StatisticsViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        _binding =
            FragmentStatisticsBinding.bind(view)

        observeData()
    }

    private fun observeData() {

        viewModel.income.observe(viewLifecycleOwner) {

            binding.txtIncome.text = "$ $it"
        }

        viewModel.expenses.observe(viewLifecycleOwner) {

            binding.txtExpenses.text = "$ $it"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}