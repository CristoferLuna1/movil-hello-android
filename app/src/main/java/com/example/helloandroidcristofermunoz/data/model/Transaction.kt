package com.example.helloandroidcristofermunoz.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Antes title
    val title: String,

    val amount: Double,

    // Antes category
    val category: String,

    // "Ingreso" o "Gasto"
    val type: String,

    val date: Long
)