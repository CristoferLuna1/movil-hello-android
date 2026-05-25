package com.example.helloandroidcristofermunoz.ui.home

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentHomeBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutEmptyStateBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutErrorStateBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutLoadingStateBinding
import com.example.helloandroidcristofermunoz.utils.NetworkMonitor
import com.example.helloandroidcristofermunoz.utils.NetworkState

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

        // 🚀 INICIAR MONITOR EN TIEMPO REAL
        NetworkMonitor.start(requireContext())

        // 🌐 OBSERVAR ESTADO DE RED
        NetworkState.isConnected.observe(viewLifecycleOwner) { connected ->
            binding.txtNetworkStatus.text =
                if (connected) "🟢 Conectado a Internet"
                else "🔴 Sin conexión"
        }

        setupStates()
        setupRecycler()
        observeData()
    }

    private fun setupStates() {

        emptyStateBinding = LayoutEmptyStateBinding.inflate(
            layoutInflater,
            binding.root as ViewGroup,
            false
        )

        loadingStateBinding = LayoutLoadingStateBinding.inflate(
            layoutInflater,
            binding.root as ViewGroup,
            false
        )

        errorStateBinding = LayoutErrorStateBinding.inflate(
            layoutInflater,
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

                val action =
                    HomeFragmentDirections
                        .actionHomeFragmentToTransactionDetailFragment(
                            transaction.id
                        )

                findNavController().navigate(action)
            }

            binding.recyclerTransactions.adapter = adapter

            if (transactions.isNotEmpty()) showContentState()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { if (it) showLoadingState() }
        viewModel.isError.observe(viewLifecycleOwner) { if (it) showErrorState() }
        viewModel.isEmpty.observe(viewLifecycleOwner) { if (it) showEmptyState() }
    }

    private fun showLoadingState() {
        binding.recyclerTransactions.visibility = View.GONE

        loadingStateBinding?.root?.visibility = View.VISIBLE
        emptyStateBinding?.root?.visibility = View.GONE
        errorStateBinding?.root?.visibility = View.GONE

        (binding.root as ViewGroup).addView(loadingStateBinding?.root)
    }

    private fun showErrorState() {
        binding.recyclerTransactions.visibility = View.GONE

        loadingStateBinding?.root?.visibility = View.GONE
        emptyStateBinding?.root?.visibility = View.GONE
        errorStateBinding?.root?.visibility = View.VISIBLE

        (binding.root as ViewGroup).addView(errorStateBinding?.root)
    }

    private fun showEmptyState() {
        binding.recyclerTransactions.visibility = View.GONE

        loadingStateBinding?.root?.visibility = View.GONE
        emptyStateBinding?.root?.visibility = View.VISIBLE
        errorStateBinding?.root?.visibility = View.GONE

        (binding.root as ViewGroup).addView(emptyStateBinding?.root)
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