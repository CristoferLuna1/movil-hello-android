package com.example.helloandroidcristofermunoz.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.databinding.ItemTransactionBinding
import com.example.helloandroidcristofermunoz.utils.AmountFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {

            binding.txtTitle.text = transaction.title
            binding.txtCategory.text = transaction.category

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))
            val date = Date(transaction.date)
            binding.txtDate.text = dateFormat.format(date)

            val amountText = if (transaction.type == "Ingreso") {
                "+ $${transaction.amount}"
            } else {
                "- $${AmountFormatter.format(transaction.amount)}"
            }

            binding.txtAmount.text = amountText

            val color = if (transaction.type == "Ingreso") {
                Color.parseColor("#4CAF50")
            } else {
                Color.parseColor("#F44336")
            }

            binding.txtAmount.setTextColor(color)

            binding.cardTransaction.setOnClickListener {
                onItemClick(transaction)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = transactions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(transactions[position])
    }
}