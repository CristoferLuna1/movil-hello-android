package com.example.helloandroidcristofermunoz.data.repository

import com.example.helloandroidcristofermunoz.data.dao.TransactionDao
import com.example.helloandroidcristofermunoz.data.dao.SavingsPlanDao
import com.example.helloandroidcristofermunoz.data.model.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao, private val savingsPlanDao: SavingsPlanDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun insert(transaction: Transaction) = transactionDao.insert(transaction)
    suspend fun update(transaction: Transaction) = transactionDao.update(transaction)
    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)
    suspend fun deleteTransaction(id: Int) = transactionDao.deleteById(id)
    suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)
    }
    
    suspend fun decrementSavingsInstallments(userId: Int, monthYear: Int, amount: Double) {
        val currentPlan = savingsPlanDao.getActiveSavingsPlanForMonth(userId, monthYear)
        currentPlan?.let {
            if (it.paidInstallments > 0) {
                val updatedPlan = it.copy(
                    currentSaved = maxOf(it.currentSaved - amount, 0.0),
                    paidInstallments = it.paidInstallments - 1
                )
                savingsPlanDao.update(updatedPlan)
            }
        }
    }
}