package com.example.helloandroidcristofermunoz.data

import android.content.Context
import androidx.room.Database   
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.helloandroidcristofermunoz.data.dao.TransactionDao
import com.example.helloandroidcristofermunoz.data.model.Transaction

@Database(entities = [Transaction::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}