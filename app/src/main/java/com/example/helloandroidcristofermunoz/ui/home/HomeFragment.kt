package com.example.helloandroidcristofermunoz.ui.home

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.DialogTransactionDetailBinding
import com.example.helloandroidcristofermunoz.databinding.FragmentHomeBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutEmptyStateBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutErrorStateBinding
import com.example.helloandroidcristofermunoz.databinding.LayoutLoadingStateBinding
import com.example.helloandroidcristofermunoz.ui.addtransaction.AddTransactionViewModel
import com.example.helloandroidcristofermunoz.ui.addtransaction.AddTransactionViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    private var emptyStateBinding: LayoutEmptyStateBinding? = null
    private var loadingStateBinding: LayoutLoadingStateBinding? = null
    private var errorStateBinding: LayoutErrorStateBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupStates()
        setupRecycler()
        setupSearchAndFilter()
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

        binding.recyclerTransactions.layoutManager =
            LinearLayoutManager(requireContext())
    }

    private fun setupSearchAndFilter() {
        // Búsqueda
        binding.edtSearch.setOnEditorActionListener { _, _, _ ->
            val query = binding.edtSearch.text.toString()
            if (query.isNotEmpty()) {
                viewModel.searchTransactions(query)
            } else {
                viewModel.clearFilters()
            }
            true
        }

        // Filtro por fecha
        binding.btnFilter.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val startDatePicker = DatePickerDialog(
            requireContext(),
            { _, startYear, startMonth, startDay ->
                val startCalendar = Calendar.getInstance()
                startCalendar.set(startYear, startMonth, startDay, 0, 0, 0)
                val startDate = startCalendar.timeInMillis

                // Mostrar segundo date picker para fecha final
                val endDatePicker = DatePickerDialog(
                    requireContext(),
                    { _, endYear, endMonth, endDay ->
                        val endCalendar = Calendar.getInstance()
                        endCalendar.set(endYear, endMonth, endDay, 23, 59, 59)
                        val endDate = endCalendar.timeInMillis

                        viewModel.filterByDateRange(startDate, endDate)
                    },
                    year, month, day
                )
                endDatePicker.show()
            },
            year, month, day
        )
        startDatePicker.show()
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
                    showTransactionDetailDialog(transaction)
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

    private fun showTransactionDetailDialog(transaction: com.example.helloandroidcristofermunoz.data.model.Transaction) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_transaction_detail)

        val binding = DialogTransactionDetailBinding.bind(dialog.findViewById<View>(android.R.id.content))

        // Mostrar datos
        binding.txtDetailTitle.text = transaction.title
        binding.txtDetailCategory.text = transaction.category
        binding.txtDetailType.text = transaction.type

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))
        val date = Date(transaction.date)
        binding.txtDetailDate.text = dateFormat.format(date)

        val formattedAmount = NumberFormat.getCurrencyInstance(Locale("es", "CO")).format(transaction.amount)
        binding.txtDetailAmount.text = formattedAmount

        // Color según tipo
        val color = if (transaction.type == "Ingreso") {
            Color.parseColor("#4CAF50")
        } else {
            Color.parseColor("#F44336")
        }
        binding.txtDetailAmount.setTextColor(color)

        // Botón editar
        binding.btnEdit.setOnClickListener {
            dialog.dismiss()
            showEditDialog(transaction)
        }

        // Botón eliminar
        binding.btnDelete.setOnClickListener {
            dialog.dismiss()
            showDeleteDialog(transaction)
        }

        dialog.show()
    }

    private fun showEditDialog(transaction: com.example.helloandroidcristofermunoz.data.model.Transaction) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_add_transaction)

        // Prellenar datos
        val edtTitle = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtTitle)
        val edtAmount = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtAmount)
        val edtCategory = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtCategory)
        val radioGroupType = dialog.findViewById<android.widget.RadioGroup>(R.id.radioGroupType)
        val btnSave = dialog.findViewById<android.widget.Button>(R.id.btnSave)

        edtTitle.setText(transaction.title)
        edtAmount.setText(transaction.amount.toString())
        edtCategory.setText(transaction.category)

        if (transaction.type == "Ingreso") {
            radioGroupType.check(R.id.rbIncome)
        } else {
            radioGroupType.check(R.id.rbExpense)
        }

        // Setup ViewModel para edición
        val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
        val repository = TransactionRepository(dao)
        val factory = AddTransactionViewModelFactory(repository)
        val editViewModel = ViewModelProvider(this, factory)[AddTransactionViewModel::class.java]

        editViewModel.setEditingTransaction(transaction)

        btnSave.setOnClickListener {
            val title = edtTitle.text.toString().trim()
            val amount = edtAmount.text.toString().trim()
            val category = edtCategory.text.toString().trim()

            val selectedTypeId = radioGroupType.checkedRadioButtonId
            val selectedRadioButton = dialog.findViewById<RadioButton>(selectedTypeId)
            val type = if (selectedRadioButton?.id == R.id.rbIncome) "Ingreso" else "Gasto"

            editViewModel.validateAndSaveTransaction(title, amount, category, type)
        }

        editViewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(requireContext(), "Transacción actualizada", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                editViewModel.resetSuccessState()
                viewModel.retryLoad()
            }
        }

        editViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                editViewModel.resetErrorState()
            }
        }

        dialog.show()
    }

    private fun showDeleteDialog(transaction: com.example.helloandroidcristofermunoz.data.model.Transaction) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar transacción")
            .setMessage("¿Estás seguro de eliminar ${transaction.title}?")
            .setPositiveButton("Eliminar") { _, _ ->
                // Setup ViewModel para eliminación
                val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
                val repository = TransactionRepository(dao)
                val factory = AddTransactionViewModelFactory(repository)
                val deleteViewModel = ViewModelProvider(this, factory)[AddTransactionViewModel::class.java]

                deleteViewModel.deleteTransaction(transaction)

                deleteViewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
                    if (isSuccess) {
                        Toast.makeText(requireContext(), "Transacción eliminada", Toast.LENGTH_SHORT).show()
                        deleteViewModel.resetSuccessState()
                        viewModel.retryLoad()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
