package com.example.helloandroidcristofermunoz.ui.home

// import para api
//
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: TransactionRepository) : ViewModel() {

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

        viewModelScope.launch {
            repository.syncTransactions()

            repository.allTransactions.collect { list ->
                _transactions.value = list
                _isEmpty.value = list.isEmpty()
                _isLoading.value = false

                calculateBalance(list)
            }
        }
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

                    // Recalcular balance cada vez que cambian las transacciones
                    calculateBalance(transactionList)
                }
            } catch (e: Exception) {

                _isLoading.value = false
                _isError.value = true
            }
        }
    }

    private fun calculateBalance(transactions: List<Transaction>) {
        var total = 0.0
        transactions.forEach { transaction ->
            if (transaction.type == "Ingreso") {
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
                repository.getTransactionsByDateRange(startDate, endDate).collect { transactionList
                    ->
                    _transactions.value = transactionList
                    _isEmpty.value = transactionList.isEmpty()
                    _isLoading.value = false
                    calculateBalance(transactionList)
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _isError.value = true
            }
        }
    }
    private fun observeTransactions() {
        viewModelScope.launch {
            repository.allTransactions.collect { list ->
                _transactions.value = list
                _isEmpty.value = list.isEmpty()
                _isLoading.value = false
                calculateBalance(list)
            }
        }
    }
    private fun syncAndLoad() {
        viewModelScope.launch {
            _isLoading.value = true

            repository.syncTransactions()
        }
    }

    fun searchTransactions(query: String) {
        _isLoading.value = true
        _isError.value = false

        viewModelScope.launch {
            try {
                val searchPattern = "%$query%"
                repository.searchTransactions(searchPattern).collect { transactionList ->
                    _transactions.value = transactionList
                    _isEmpty.value = transactionList.isEmpty()
                    _isLoading.value = false
                    calculateBalance(transactionList)
                }
            } catch (e: Exception) {
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
                Log.e("HomeViewModel", "Error deleting transaction: ${e.message}")
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {

        viewModelScope.launch { repository.update(transaction) }
    }
    // test api

    fun testApi() {

        viewModelScope.launch {
            try {} catch (e: Exception) {

                Log.e("API_TEST", e.message.toString())
            }
        }
    }
}
