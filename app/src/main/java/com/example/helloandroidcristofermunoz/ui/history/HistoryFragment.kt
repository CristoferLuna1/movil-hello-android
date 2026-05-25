package com.example.helloandroidcristofermunoz.ui.history

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        setupRecycler()
        observeData()
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
