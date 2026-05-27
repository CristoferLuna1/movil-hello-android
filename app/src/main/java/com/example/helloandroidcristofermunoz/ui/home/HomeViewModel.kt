package com.example.helloandroidcristofermunoz.ui.home

import androidx.lifecycle.*
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val currentUserId = 1 // TODO: Obtener del usuario actual

    init {
        loadTransactions()
    }

    private fun loadTransactions() {

        _isLoading.value = true
        _isError.value = false

        viewModelScope.launch {

            try {

                repository.allTransactions.collect { transactionList ->

                    val calendar = Calendar.getInstance()
                    val currentMonthYear = calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)

                    val filteredTransactions = transactionList.filter { 
                        it.userId == currentUserId && 
                        it.monthYear == currentMonthYear 
                    }

                    _transactions.value = filteredTransactions
                    _isEmpty.value = filteredTransactions.isEmpty()
                    _isLoading.value = false
                }

            } catch (e: Exception) {

                _isLoading.value = false
                _isError.value = true
            }
        }
    }

    fun retryLoad() {
        loadTransactions()
    }
}