package com.example.helloandroidcristofermunoz.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentHomeBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutEmptyStateBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutErrorStateBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutLoadingStateBinding
import java.text.NumberFormat
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupStates()
        setupRecycler()
        observeData()
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
                    val formattedAmount = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(transaction.amount)
                    Toast.makeText(
                        requireContext(),
                        "${transaction.title} - $formattedAmount - ${transaction.type}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
            binding.recyclerTransactions.adapter = adapter

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
