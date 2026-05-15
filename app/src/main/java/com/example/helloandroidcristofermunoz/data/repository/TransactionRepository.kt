package com.example.helloandroidcristofermunoz.data.repository

import com.example.helloandroidcristofermunoz.data.model.Transaction

class TransactionRepository {

    fun getTransactions(): List<Transaction> {

        return listOf(

            Transaction(
                "Salario",
                "Ingresos",
                2500000.0,
                "income"
            ),

            Transaction(
                "Mercado",
                "Comida",
                120000.0,
                "expense"
            ),

            Transaction(
                "Netflix",
                "Entretenimiento",
                38000.0,
                "expense"
            )
        )
    }
}