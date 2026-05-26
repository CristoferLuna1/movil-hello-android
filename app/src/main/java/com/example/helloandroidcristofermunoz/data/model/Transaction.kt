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

    // Categoría personalizada cuando se selecciona "Otro"
    val customCategory: String? = null,

    // "Ingreso" o "Gasto"
    val type: String,

    val date: Long,

    // Día del mes para pago de deudas mensuales (1-31)
    val paymentDay: Int? = null,

    // Fecha de fin para deudas mensuales
    val endDate: Long? = null,

    // Si es persistente mensual (gastos fijos y deudas mensuales)
    val isMonthlyPersistent: Boolean = false,

    // Mes y año de la transacción (formato: YYYYMM)
    val monthYear: Int,

    // ID del usuario que creó la transacción
    val userId: Int
)