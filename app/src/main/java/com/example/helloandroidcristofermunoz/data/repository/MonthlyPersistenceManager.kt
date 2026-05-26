package com.example.helloandroidcristofermunoz.data.repository

import com.example.helloandroidcristofermunoz.data.dao.TransactionDao
import com.example.helloandroidcristofermunoz.data.model.Transaction
import kotlinx.coroutines.flow.first
import java.util.Calendar

class MonthlyPersistenceManager(
    private val transactionDao: TransactionDao
) {
    
    suspend fun handleMonthTransition(userId: Int) {
        val calendar = Calendar.getInstance()
        val currentMonthYear = calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)
        
        // Obtener todas las transacciones del usuario
        val allTransactions = transactionDao.getAllTransactions().first()
        
        // Filtrar transacciones del mes anterior que son persistentes
        val previousMonthCalendar = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }
        val previousMonthYear = previousMonthCalendar.get(Calendar.YEAR) * 100 + (previousMonthCalendar.get(Calendar.MONTH) + 1)
        
        val persistentTransactions = allTransactions.filter { 
            it.userId == userId && 
            it.monthYear == previousMonthYear && 
            it.isMonthlyPersistent 
        }
        
        // Crear nuevas transacciones para el mes actual
        persistentTransactions.forEach { oldTransaction ->
            val newTransaction = Transaction(
                title = oldTransaction.title,
                amount = oldTransaction.amount,
                category = oldTransaction.category,
                customCategory = oldTransaction.customCategory,
                type = oldTransaction.type,
                date = System.currentTimeMillis(),
                paymentDay = oldTransaction.paymentDay,
                endDate = oldTransaction.endDate,
                isMonthlyPersistent = true,
                monthYear = currentMonthYear,
                userId = userId
            )
            transactionDao.insert(newTransaction)
        }
    }
    
    suspend fun shouldHandleMonthTransition(userId: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        // Solo procesar el primer día del mes
        return currentDay == 1
    }
}
