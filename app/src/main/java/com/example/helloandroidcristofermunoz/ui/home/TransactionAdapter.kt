package com.example.helloandroidcristofermunoz.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.databinding.ItemTransactionBinding

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

            val amountText = if (transaction.type == "income") {
                "+ $${transaction.amount}"
            } else {
                "- $${transaction.amount}"
            }

            binding.txtAmount.text = amountText

            val color = if (transaction.type == "income") {
                Color.parseColor("#4CAF50")
            } else {
                Color.parseColor("#F44336")
            }

            binding.txtAmount.setTextColor(color)

            binding.root.setOnClickListener {
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