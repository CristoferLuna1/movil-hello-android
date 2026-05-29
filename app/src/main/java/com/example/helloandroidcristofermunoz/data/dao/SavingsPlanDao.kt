package com.example.helloandroidcristofermunoz.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.helloandroidcristofermunoz.data.model.SavingsPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsPlanDao {
    
    @Query("SELECT * FROM savings_plans")
    fun getAllSavingsPlans(): Flow<List<SavingsPlan>>
    
    @Query("SELECT * FROM savings_plans WHERE id = :id")
    suspend fun getSavingsPlanById(id: Int): SavingsPlan?
    
    @Query("SELECT * FROM savings_plans WHERE userId = :userId AND monthYear = :monthYear AND isActive = 1 LIMIT 1")
    suspend fun getActiveSavingsPlanForMonth(userId: Int, monthYear: Int): SavingsPlan?
    
    @Query("SELECT * FROM savings_plans WHERE userId = :userId ORDER BY monthYear DESC")
    fun getSavingsPlansByUser(userId: Int): Flow<List<SavingsPlan>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savingsPlan: SavingsPlan): Long
    
    @Update
    suspend fun update(savingsPlan: SavingsPlan)
    
    @Delete
    suspend fun delete(savingsPlan: SavingsPlan)
    
    @Query("""Select * FROM savings_plans WHERE monthYear = :monthYear LIMIT 1""")
    suspend fun getGoalForMonth( monthYear: Int ): SavingsPlan?

    @Query("DELETE FROM savings_plans WHERE id = :id")
    suspend fun deleteById(id: Int)
}
