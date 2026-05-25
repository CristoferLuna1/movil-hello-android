package com.example.helloandroidcristofermunoz.ui.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _income = MutableLiveData<Double>()
    val income: LiveData<Double> = _income

    private val _expenses = MutableLiveData<Double>()
    val expenses: LiveData<Double> = _expenses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        _isLoading.value = true
        _isError.value = false

        viewModelScope.launch {
            try {
                repository.allTransactions.collect { transactions ->
                    calculateStatistics(transactions)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _isError.value = true
                _income.value = 0.0
                _expenses.value = 0.0
            }
        }
    }

    private fun calculateStatistics(transactions: List<Transaction>) {
        var totalIncome = 0.0
        var totalExpenses = 0.0

        transactions.forEach { transaction ->
            if (transaction.type == "Ingreso") {
                totalIncome += transaction.amount
            } else if (transaction.type == "Gasto") {
                totalExpenses += transaction.amount
            }
        }

        _income.value = totalIncome
        _expenses.value = totalExpenses
    }

    fun retryLoad() {
        loadStatistics()
    }
}
