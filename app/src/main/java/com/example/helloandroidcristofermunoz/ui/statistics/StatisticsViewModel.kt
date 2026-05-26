package com.example.helloandroidcristofermunoz.ui.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.AppDatabase
import kotlinx.coroutines.launch

class StatisticsViewModel : ViewModel() {

    private val _income =
        MutableLiveData<Double>()

    val income: LiveData<Double>
        get() = _income

    private val _expenses =
        MutableLiveData<Double>()

    val expenses: LiveData<Double>
        get() = _expenses

    init {
        // No cargar datos en init, esperar a que se establezca el context
    }

    fun loadStatistics() {
        viewModelScope.launch {
            try {
                val transactionDao = AppDatabase.getDatabase(context).transactionDao()
                val userDao = AppDatabase.getDatabase(context).userDao()
                val currentUser = userDao.getLoggedInUser()

                if (currentUser != null) {
                    val transactions = transactionDao.getAllTransactionsByUser(currentUser.id)

                    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
                    val expenses = transactions.filter { it.type == "expense" }.sumOf { it.amount }

                    _income.value = income
                    _expenses.value = expenses
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun setContext(context: android.content.Context) {
        this.context = context
    }

    private lateinit var context: android.content.Context
}