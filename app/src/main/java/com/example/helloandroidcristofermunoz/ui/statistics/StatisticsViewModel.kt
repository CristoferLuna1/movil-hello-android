package com.example.helloandroidcristofermunoz.ui.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.AppDatabase
import kotlinx.coroutines.launch
import java.util.Calendar

class StatisticsViewModel : ViewModel() {

    private val _income = MutableLiveData<Double>()
    val income: LiveData<Double> = _income

    private val _expenses = MutableLiveData<Double>()
    val expenses: LiveData<Double> = _expenses

    private val _balance = MutableLiveData<Double>()
    val balance: LiveData<Double> = _balance

    private val _categoryExpenses = MutableLiveData<Map<String, Double>>()
    val categoryExpenses: LiveData<Map<String, Double>> = _categoryExpenses


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
                    val calendar = Calendar.getInstance()
                    val currentMonthYear = calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)

                    // Obtener transacciones del mes actual
                    val transactions = transactionDao.getAllTransactionsByUser(currentUser.id)
                        .filter { it.monthYear == currentMonthYear }

                    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
                    val expenses = transactions.filter { it.type == "expense" }.sumOf { it.amount }
                    val balance = income - expenses

                    // Calcular gastos por categoría
                    val categoryExpensesMap = transactions
                        .filter { it.type == "expense" }
                        .groupBy { it.category }
                        .mapValues { it.value.sumOf { t -> t.amount } }

                    _income.value = income
                    _expenses.value = expenses
                    _balance.value = balance
                    _categoryExpenses.value = categoryExpensesMap
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