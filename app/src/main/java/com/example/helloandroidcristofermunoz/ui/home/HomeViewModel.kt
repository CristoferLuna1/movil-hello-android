package com.example.helloandroidcristofermunoz.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar

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

    private val currentUserId = 1 // TODO: obtener usuario actual

    init {
        loadTransactions()
    }

    private fun loadTransactions() {

        _isLoading.value = true
        _isError.value = false

        viewModelScope.launch {

            try {

                // Sincronizar con Firebase/API
                repository.syncTransactions()

                repository.allTransactions.collect { transactionList ->

                    val calendar = Calendar.getInstance()

                    val currentMonthYear =
                        calendar.get(Calendar.YEAR) * 100 +
                                (calendar.get(Calendar.MONTH) + 1)

                    // Filtrar por usuario y mes actual
                    val filteredTransactions = transactionList.filter {

                        it.userId == currentUserId &&
                                it.monthYear == currentMonthYear
                    }

                    _transactions.value = filteredTransactions
                    _isEmpty.value = filteredTransactions.isEmpty()
                    _isLoading.value = false

                    calculateBalance(filteredTransactions)
                }

            } catch (e: Exception) {

                Log.e("HomeViewModel", "Error loading transactions: ${e.message}")

                _isLoading.value = false
                _isError.value = true
            }
        }
    }

    private fun calculateBalance(transactions: List<Transaction>) {

        var total = 0.0

        transactions.forEach { transaction ->

            if (
                transaction.type.equals("Ingreso", true) ||
                transaction.type.equals("income", true)
            ) {

                total += transaction.amount

            } else {

                total -= transaction.amount
            }
        }

        _balance.value = total
    }

    fun filterByDateRange(startDate: Long, endDate: Long) {

        _isLoading.value = true
        _isError.value = false

        viewModelScope.launch {

            try {

                repository
                    .getTransactionsByDateRange(startDate, endDate)
                    .collect { transactionList ->

                        _transactions.value = transactionList
                        _isEmpty.value = transactionList.isEmpty()
                        _isLoading.value = false

                        calculateBalance(transactionList)
                    }

            } catch (e: Exception) {

                Log.e("HomeViewModel", "Error filtering: ${e.message}")

                _isLoading.value = false
                _isError.value = true
            }
        }
    }

    fun searchTransactions(query: String) {

        _isLoading.value = true
        _isError.value = false

        viewModelScope.launch {

            try {

                val searchPattern = "%$query%"

                repository
                    .searchTransactions(searchPattern)
                    .collect { transactionList ->

                        _transactions.value = transactionList
                        _isEmpty.value = transactionList.isEmpty()
                        _isLoading.value = false

                        calculateBalance(transactionList)
                    }

            } catch (e: Exception) {

                Log.e("HomeViewModel", "Error searching: ${e.message}")

                _isLoading.value = false
                _isError.value = true
            }
        }
    }

    fun clearFilters() {
        loadTransactions()
    }

    fun retryLoad() {
        loadTransactions()
    }

    fun deleteTransaction(transaction: Transaction) {

        viewModelScope.launch {

            try {

                repository.delete(transaction)

            } catch (e: Exception) {

                Log.e(
                    "HomeViewModel",
                    "Error deleting transaction: ${e.message}"
                )
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {

        viewModelScope.launch {

            try {

                repository.update(transaction)

            } catch (e: Exception) {

                Log.e(
                    "HomeViewModel",
                    "Error updating transaction: ${e.message}"
                )
            }
        }
    }

    // TEST API
    fun testApi() {

        viewModelScope.launch {

            try {

                repository.syncTransactions()

                Log.d("API_TEST", "Sincronización exitosa")

            } catch (e: Exception) {

                Log.e("API_TEST", e.message.toString())
            }
        }
    }
}