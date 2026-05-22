package com.example.helloandroidcristofermunoz.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.databinding.ItemTransactionBinding

class TransactionAdapter(
    private val transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.txtTitle.text = transaction.title
            binding.txtCategory.text = transaction.category
            
            val amountText = if (transaction.type == "income") {
                "+ $${transaction.amount}"
            } else {
                "- $${transaction.amount}"
            }
            binding.txtAmount.text = amountText
            
            // Color según tipo de transacción
            val color = if (transaction.type == "income") {
                Color.parseColor("#4CAF50") // Verde para ingresos
            } else {
                Color.parseColor("#F44336") // Rojo para gastos
            }
            binding.txtAmount.setTextColor(color)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(transactions[position])
    }
}
