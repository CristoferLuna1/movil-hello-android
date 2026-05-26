package com.example.helloandroidcristofermunoz.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs

import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentTransactionDetailBinding
import com.example.helloandroidcristofermunoz.utils.AmountFormatter

class TransactionDetailFragment : Fragment() {

    private var _binding: FragmentTransactionDetailBinding? = null
    private val binding get() = _binding!!

    private val args: TransactionDetailFragmentArgs by navArgs()

    private val viewModel: TransactionDetailViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
        val repository = TransactionRepository(dao)
        TransactionDetailViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadTransaction(args.transactionId)

        observeData()
    }

    private fun observeData() {
        viewModel.transaction.observe(viewLifecycleOwner) { transaction ->

            if (transaction != null) {
                binding.txtTitle.text = transaction.title
                binding.txtAmount.text = "$${AmountFormatter.format(transaction.amount)}"
                binding.txtCategory.text = transaction.category
                binding.txtType.text = transaction.type
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}