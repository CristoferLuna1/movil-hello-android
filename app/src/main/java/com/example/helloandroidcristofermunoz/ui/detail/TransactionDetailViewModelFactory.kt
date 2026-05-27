package com.example.helloandroidcristofermunoz.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository

class TransactionDetailViewModelFactory(
    private val repository: TransactionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransactionDetailViewModel(repository) as T
    }
}