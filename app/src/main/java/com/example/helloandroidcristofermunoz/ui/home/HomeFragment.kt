package com.example.helloandroidcristofermunoz.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentHomeBinding.bind(view)

        setupRecycler()

        observeData()
    }

    private fun setupRecycler() {

        binding.recyclerTransactions.layoutManager =
            LinearLayoutManager(requireContext())
    }

    private fun observeData() {

        viewModel.transactions.observe(viewLifecycleOwner) {

            val adapter = TransactionAdapter(it)

            binding.recyclerTransactions.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}