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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> = _isError

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    init {
        syncFirebase()

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
            } catch (e: Exception) {

                _isLoading.value = false
                _isError.value = true
            }
        }
    }

    fun retryLoad() {
        loadTransactions()
    }
    // test api

    fun testApi() {

        viewModelScope.launch {
            try {} catch (e: Exception) {

                Log.e("API_TEST", e.message.toString())
            }
        }
    }
    private fun syncFirebase() {

        viewModelScope.launch {
             repository.syncTransactions() 
        }
    }
}
