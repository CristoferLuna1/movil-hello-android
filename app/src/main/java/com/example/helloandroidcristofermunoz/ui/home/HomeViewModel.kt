package com.example.helloandroidcristofermunoz.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository

class HomeViewModel : ViewModel() {

    private val repository = TransactionRepository()

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        _isLoading.value = true
        _isError.value = false

        try {
            // Simular carga de datos
            Thread.sleep(500)
            
            val transactionList = repository.getTransactions()
            _transactions.value = transactionList
            _isEmpty.value = transactionList.isEmpty()
            _isLoading.value = false
        } catch (e: Exception) {
            _isLoading.value = false
            _isError.value = true
        }
    }

    fun retryLoad() {
        loadTransactions()
    }
}
