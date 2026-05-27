package com.example.helloandroidcristofermunoz.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_plans")
data class SavingsPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    // Meta total de ahorro (objetivo general)
    val totalGoal: Double,
    
    // Meta de ahorro mensual base (calculada automáticamente)
    val monthlyGoal: Double,
    
    // Monto máximo disponible (ej. 2.500.000)
    val maxAmount: Double,
    
    // Monto actual ahorrado en el mes
    val currentSaved: Double = 0.0,
    
    // Déficit acumulado de meses anteriores (para compensación)
    val accumulatedDeficit: Double = 0.0,
    
    // Número total de cuotas (para planes multimonth)
    val totalMonths: Int = 1,
    
    // Cuotas pagadas
    val paidInstallments: Int = 0,
    
    // Mes y año del plan (formato: YYYYMM)
    val monthYear: Int,
    
    // ID del usuario
    val userId: Int,
    
    // Si está activo
    val isActive: Boolean = true,
    
    val createdAt: Long = System.currentTimeMillis()
)
