package com.example.helloandroidcristofermunoz.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoryViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    private val _monthIncome = MutableLiveData<Double>()
    val monthIncome: LiveData<Double> = _monthIncome

    private val _monthExpenses = MutableLiveData<Double>()
    val monthExpenses: LiveData<Double> = _monthExpenses

    private val _monthBalance = MutableLiveData<Double>()
    val monthBalance: LiveData<Double> = _monthBalance

    private val _selectedMonth = MutableLiveData<Int>()
    val selectedMonth: LiveData<Int> = _selectedMonth

    private val _selectedYear = MutableLiveData<Int>()
    val selectedYear: LiveData<Int> = _selectedYear

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        val calendar = Calendar.getInstance()
        _selectedMonth.value = calendar.get(Calendar.MONTH)
        _selectedYear.value = calendar.get(Calendar.YEAR)
        loadTransactionsByMonth()
    }

    fun selectMonth(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
        loadTransactionsByMonth()
    }

    private fun loadTransactionsByMonth() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val month = _selectedMonth.value ?: 0
                val year = _selectedYear.value ?: 0

                val calendar = Calendar.getInstance()
                calendar.set(year, month, 1, 0, 0, 0)
                val startOfMonth = calendar.timeInMillis

                calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                val endOfMonth = calendar.timeInMillis

                repository.getTransactionsByDateRange(startOfMonth, endOfMonth).collect { transactionList ->
                    _transactions.value = transactionList
                    calculateMonthSummary(transactionList)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    private fun calculateMonthSummary(transactions: List<Transaction>) {
        var income = 0.0
        var expenses = 0.0

        transactions.forEach { transaction ->
            if (transaction.type == "Ingreso") {
                income += transaction.amount
            } else {
                expenses += transaction.amount
            }
        }

        _monthIncome.value = income
        _monthExpenses.value = expenses
        _monthBalance.value = income - expenses
    }
}
