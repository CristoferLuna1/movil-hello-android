package com.example.helloandroidcristofermunoz.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val month: Int,
    val year: Int
)
