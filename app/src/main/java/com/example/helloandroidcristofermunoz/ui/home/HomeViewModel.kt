package com.example.helloandroidcristofermunoz.ui.home

import androidx.lifecycle.*
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance

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

        viewModelScope.launch {

            try {

                repository.allTransactions.collect { transactionList ->

                    _transactions.value = transactionList
                    _isEmpty.value = transactionList.isEmpty()
                    _isLoading.value = false
                }

                loadBalance()

            } catch (e: Exception) {

                _isLoading.value = false
                _isError.value = true
            }
        }
    }

    private fun loadBalance() {
        viewModelScope.launch {
            try {
                val totalBalance = repository.getTotalBalance()
                _balance.value = totalBalance
            } catch (e: Exception) {
                _balance.value = 0.0
            }
        }
    }

    fun retryLoad() {
        loadTransactions()
    }
}
