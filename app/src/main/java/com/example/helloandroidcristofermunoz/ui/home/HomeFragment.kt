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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {

        val dao = AppDatabase
            .getDatabase(requireContext())
            .transactionDao()

        val repository = TransactionRepository(dao)

        HomeViewModelFactory(repository)
    }

    private val savingsPlanDao: SavingsPlanDao by lazy {
        AppDatabase.getDatabase(requireContext()).savingsPlanDao()
    }

    private var emptyStateBinding: LayoutEmptyStateBinding? = null
    private var loadingStateBinding: LayoutLoadingStateBinding? = null
    private var errorStateBinding: LayoutErrorStateBinding? = null

    private val currentUserId = 1 // TODO: Obtener del usuario actual

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupStates()
        setupRecycler()
        loadSavingsPlan()
        observeData()
    }

    private fun loadSavingsPlan() {
        val calendar = Calendar.getInstance()
        val currentMonthYear = calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)

        try {
            runBlocking {
                val savingsPlan = savingsPlanDao.getActiveSavingsPlanForMonth(currentUserId, currentMonthYear)
                
                if (savingsPlan != null) {
                    val progress = if (savingsPlan.monthlyGoal > 0) {
                        ((savingsPlan.currentSaved / savingsPlan.monthlyGoal) * 100).toInt()
                    } else {
                        0
                    }
                    val availableWithoutSavings = savingsPlan.maxAmount - savingsPlan.currentSaved

                    binding.txtSavingsGoal.text = "Meta: $${AmountFormatter.format(savingsPlan.monthlyGoal)}"
                    binding.txtSavingsProgress.text = "${progress}%"
                    binding.progressSavings.progress = progress
                    binding.txtAvailableWithoutSavings.text = "Disponible sin tocar ahorro: $${AmountFormatter.format(availableWithoutSavings)}"

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
                    binding.txtSavingsGoal.text = "Sin plan de ahorro"
                    binding.txtSavingsProgress.text = "0%"
                    binding.progressSavings.progress = 0
                    binding.txtAvailableWithoutSavings.text = "Configura tu plan de ahorro"
                }
            }
        } catch (e: Exception) {
            // Manejar error silenciosamente para evitar cierre de app
            binding.txtSavingsGoal.text = "Sin plan de ahorro"
            binding.txtSavingsProgress.text = "0%"
            binding.progressSavings.progress = 0
            binding.txtAvailableWithoutSavings.text = "Configura tu plan de ahorro"
        }
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