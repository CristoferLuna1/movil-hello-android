package com.example.helloandroidcristofermunoz.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository

class AddTransactionViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {

            @Suppress("UNCHECKED_CAST")
            return AddTransactionViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}