package com.example.helloandroidcristofermunoz.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.helloandroidcristofermunoz.data.model.SavingsGoal

@Dao
interface SavingsGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoal)

    @Update
    suspend fun update(goal: SavingsGoal)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM savings_goals WHERE month = :month AND year = :year LIMIT 1")
    suspend fun getGoalForMonth(month: Int, year: Int): SavingsGoal?

    @Query("SELECT * FROM savings_goals ORDER BY year DESC, month DESC")
    suspend fun getAllGoals(): List<SavingsGoal>
}
