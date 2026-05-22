package com.example.helloandroidcristofermunoz.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val amount: Double,
    val type: String, // Ejemplo: "Ingreso" o "Gasto"
    val date: Long
)