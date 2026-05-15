package com.example.helloandroidcristofermunoz.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository

class HomeViewModel : ViewModel() {

    private val repository = TransactionRepository()

    private val _transactions =
        MutableLiveData<List<Transaction>>()

    val transactions: LiveData<List<Transaction>>
        get() = _transactions

    init {
        loadTransactions()
    }

    private fun loadTransactions() {

        _transactions.value =
            repository.getTransactions()
    }
}