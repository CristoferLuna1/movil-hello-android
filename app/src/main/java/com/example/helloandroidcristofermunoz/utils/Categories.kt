package com.example.helloandroidcristofermunoz.utils

object Categories {
    val PREDEFINED_CATEGORIES = listOf(
        "Sueldo",
        "Deudas",
        "Deudas Mensuales",
        "Gastos Fijos",
        "Gastos Hormiga",
        "Compra Ropa",
        "Gasto Vehiculo",
        "Eventualidades",
        "Otro"
    )
    
    val MONTHLY_PERSISTENT_CATEGORIES = listOf(
        "Deudas Mensuales",
        "Gastos Fijos"
    )
    
    fun isMonthlyPersistent(category: String): Boolean {
        return MONTHLY_PERSISTENT_CATEGORIES.contains(category)
    }
    
    fun requiresPaymentDay(category: String): Boolean {
        return category == "Deudas Mensuales"
    }
}
