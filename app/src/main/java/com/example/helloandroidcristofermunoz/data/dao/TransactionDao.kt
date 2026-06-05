package com.example.helloandroidcristofermunoz.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.helloandroidcristofermunoz.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAll(): List<Transaction>

    @Query("SELECT SUM(CASE WHEN type = 'Ingreso' THEN amount ELSE -amount END) FROM transactions")
    suspend fun getTotalBalance(): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Ingreso'")
    suspend fun getTotalIncome(): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'Gasto'")
    suspend fun getTotalExpenses(): Double?

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE title LIKE :searchQuery OR category LIKE :searchQuery ORDER BY date DESC")
    fun searchTransactions(searchQuery: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactionsByUser(userId: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun getAllTransactionsByUser(userId: Int): List<Transaction>
    
    @Query("SELECT * FROM transactions WHERE pendingSync = 1")
    suspend fun getPendingTransactions(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND monthYear = :monthYear ORDER BY date DESC")
    fun getTransactionsByUserAndMonth(userId: Int, monthYear: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?

    @Query("SELECT * FROM transactions WHERE firebaseId = :firebaseId")
    suspend fun getByFirebaseId(firebaseId: String): Transaction?

    @Query("SELECT * FROM transactions")
    suspend fun getAllOnce(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
    @Delete
    suspend fun delete(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)
    
    @Query("DELETE FROM transactions WHERE userId = :userId AND monthYear = :monthYear")
    suspend fun deleteTransactionsByUserAndMonth(userId: Int, monthYear: Int)
}