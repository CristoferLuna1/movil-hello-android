package com.example.helloandroidcristofermunoz.data.repository

import com.example.helloandroidcristofermunoz.data.dao.TransactionDao
import com.example.helloandroidcristofermunoz.data.model.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun getTotalBalance(): Double = transactionDao.getTotalBalance()
    suspend fun getTotalIncome(): Double = transactionDao.getTotalIncome()
    suspend fun getTotalExpenses(): Double = transactionDao.getTotalExpenses()

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startDate, endDate)

    fun searchTransactions(searchQuery: String): Flow<List<Transaction>> =
        transactionDao.searchTransactions(searchQuery)

    suspend fun insert(transaction: Transaction) = transactionDao.insert(transaction)
    suspend fun update(transaction: Transaction) = transactionDao.update(transaction)
    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)
}
