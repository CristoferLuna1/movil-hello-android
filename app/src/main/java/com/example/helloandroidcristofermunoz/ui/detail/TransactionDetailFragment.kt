package com.example.helloandroidcristofermunoz.ui.detail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.databinding.FragmentTransactionDetailBinding
import com.example.helloandroidcristofermunoz.utils.AmountFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.util.Log

class TransactionDetailFragment : Fragment() {

    private var _binding: FragmentTransactionDetailBinding? = null
    private val binding
        get() = _binding!!

    private val args: TransactionDetailFragmentArgs by navArgs()

    private val viewModel: TransactionDetailViewModel by viewModels {
        val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
        val savingsDao = AppDatabase.getDatabase(requireContext()).savingsPlanDao()
        val repository = TransactionRepository(dao, savingsDao)
        TransactionDetailViewModelFactory(repository)
    }

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))

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
        setupListeners()
        viewModel.deleteDone.observe(viewLifecycleOwner) {
            Log.d("DELETE_CRISTOFER", "OBSERVADOR EJECUTADO = $it")
            if (it == true) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setupListeners() {
        binding.btnEdit.setOnClickListener {
            val action =
                    TransactionDetailFragmentDirections
                            .actionTransactionDetailFragmentToAddTransactionFragment(
                                    args.transactionId
                            )
            findNavController().navigate(action)
        }

        binding.btnDelete.setOnClickListener { showDeleteConfirmationDialog() }
    }
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
                .setTitle("Eliminar transacción")
                .setMessage("¿Seguro?")
                .setPositiveButton("Eliminar") { _, _ ->
                    viewModel.transaction.value?.let { transaction ->
                        viewModel.deleteTransaction(transaction)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
    }

    private fun observeData() {
        viewModel.transaction.observe(viewLifecycleOwner) { transaction ->
            if (transaction != null) {
                binding.txtTitle.text = transaction.title
                binding.txtAmount.text = "$${AmountFormatter.format(transaction.amount)}"
                binding.txtCategory.text = transaction.category
                binding.txtType.text = if (transaction.type == "income") "Ingreso" else "Gasto"

                // Mostrar información de deuda mensual si aplica
                if (transaction.isMonthlyPersistent && transaction.category == "Deudas Mensuales") {
                    binding.layoutDebtInfo.visibility = View.VISIBLE

                    transaction.paymentDay?.let {
                        binding.txtPaymentDay.text = "Día de pago: $it de cada mes"
                    }

                    transaction.endDate?.let {
                        binding.txtEndDate.text = "Fecha fin: ${dateFormat.format(it)}"

                        // Calcular cuotas
                        val startDate = transaction.date
                        val endDate = it
                        val monthsBetween = calculateMonthsBetween(startDate, endDate)
                        val currentMonth = getCurrentInstallment(startDate)

                        binding.txtInstallmentInfo.text = "Cuota $currentMonth de $monthsBetween"
                    }
                } else {
                    binding.layoutDebtInfo.visibility = View.GONE
                }
            }
        }
    }

    private fun calculateMonthsBetween(startDate: Long, endDate: Long): Int {
        val start = Calendar.getInstance().apply { timeInMillis = startDate }
        val end = Calendar.getInstance().apply { timeInMillis = endDate }

        val months =
                (end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 +
                        (end.get(Calendar.MONTH) - start.get(Calendar.MONTH)) +
                        1
        return months
    }

    private fun getCurrentInstallment(startDate: Long): Int {
        val start = Calendar.getInstance().apply { timeInMillis = startDate }
        val current = Calendar.getInstance()

        val months =
                (current.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12 +
                        (current.get(Calendar.MONTH) - start.get(Calendar.MONTH)) +
                        1
        return months.coerceAtLeast(1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
