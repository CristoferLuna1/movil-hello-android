package com.example.helloandroidcristofermunoz.ui.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class StatisticsViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _income = MutableLiveData<Double>()
    val income: LiveData<Double> = _income

    private val _expenses = MutableLiveData<Double>()
    val expenses: LiveData<Double> = _expenses

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance

    private val _categoryExpenses = MutableLiveData<Map<String, Double>>()
    val categoryExpenses: LiveData<Map<String, Double>> = _categoryExpenses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    fun loadStatistics(userId: Int) {
        viewModelScope.launch {
            repository.getTransactionsByUser(userId)
                .catch {
                    _isLoading.value = false
                    _isError.value = true
                }
                .collectLatest { transactions ->

                    _isLoading.value = true
                    _isError.value = false

                    val calendar = Calendar.getInstance()
                    val currentMonthYear =
                        calendar.get(Calendar.YEAR) * 100 +
                                (calendar.get(Calendar.MONTH) + 1)

                    val currentMonthTransactions =
                        transactions.filter { it.monthYear == currentMonthYear }

                    val totalIncome =
                        currentMonthTransactions
                            .filter { it.type == "Ingreso" }
                            .sumOf { it.amount }

                    val totalExpenses =
                        currentMonthTransactions
                            .filter { it.type == "Gasto" }
                            .sumOf { it.amount }

                    val totalBalance = totalIncome - totalExpenses

                    val expensesByCategory =
                        currentMonthTransactions
                            .filter { it.type == "Gasto" }
                            .groupBy { it.category }
                            .mapValues { entry ->
                                entry.value.sumOf { it.amount }
                            }

                    _income.value = totalIncome
                    _expenses.value = totalExpenses
                    _balance.value = totalBalance
                    _categoryExpenses.value = expensesByCategory

                    _isLoading.value = false
                }
        }
    }
}