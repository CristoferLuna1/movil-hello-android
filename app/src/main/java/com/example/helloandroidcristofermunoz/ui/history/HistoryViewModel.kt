package com.example.helloandroidcristofermunoz.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    fun loadTransactionsForMonth(userId: Int, monthYear: Int) {
        viewModelScope.launch {
            try {
                repository.allTransactions.collect { transactionList ->
                    val filteredTransactions = transactionList.filter { 
                        it.userId == userId && 
                        it.monthYear == monthYear 
                    }
                    _transactions.value = filteredTransactions
                }
            } catch (e: Exception) {
                _transactions.value = emptyList()
            }
        }
    }
}
