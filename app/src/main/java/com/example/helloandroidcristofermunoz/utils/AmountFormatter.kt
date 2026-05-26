package com.example.helloandroidcristofermunoz.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object AmountFormatter {
    
    private val decimalFormat = DecimalFormat("#,###").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale("es", "CL"))
    }
    
    fun format(amount: Double): String {
        return decimalFormat.format(amount)
    }
    
    fun format(amount: String): String {
        return try {
            val doubleValue = amount.replace(".", "").replace(",", "").toDouble()
            decimalFormat.format(doubleValue)
        } catch (e: Exception) {
            amount
        }
    }
    
    fun parse(formattedAmount: String): Double {
        return try {
            formattedAmount.replace(".", "").replace(",", "").toDouble()
        } catch (e: Exception) {
            0.0
        }
    }
}
