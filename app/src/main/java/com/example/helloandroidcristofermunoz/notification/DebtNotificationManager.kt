package com.example.helloandroidcristofermunoz.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.helloandroidcristofermunoz.MainActivity
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.data.dao.TransactionDao
import com.example.helloandroidcristofermunoz.data.model.Transaction
import kotlinx.coroutines.flow.first
import java.util.Calendar

class DebtNotificationManager(
    private val context: Context,
    private val transactionDao: TransactionDao
) {
    
    private val channelId = "debt_payment_channel"
    private val channelName = "Pagos de Deudas"
    private val notificationId = 1001
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para recordatorios de pago de deudas"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    suspend fun checkAndSendDebtNotifications(userId: Int) {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonthYear = calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)
        
        val transactions = transactionDao.getTransactionsByUserAndMonth(userId, currentMonthYear).first()
        val debtTransactions = transactions.filter { 
            it.category == "Deudas Mensuales" && 
            it.paymentDay != null 
        }
        
        debtTransactions.forEach { transaction ->
            val paymentDay = transaction.paymentDay!!
            val daysUntilPayment = calculateDaysUntilPayment(currentDay, paymentDay, calendar)
            
            if (daysUntilPayment in 1..7) {
                sendDebtNotification(transaction, daysUntilPayment)
            }
        }
    }
    
    private fun calculateDaysUntilPayment(currentDay: Int, paymentDay: Int, calendar: Calendar): Int {
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        return if (paymentDay >= currentDay) {
            paymentDay - currentDay
        } else {
            (daysInMonth - currentDay) + paymentDay
        }
    }
    
    private fun sendDebtNotification(transaction: Transaction, daysUntilPayment: Int) {
        val message = when (daysUntilPayment) {
            0 -> "¡Hoy es el día de pago para: ${transaction.title}!"
            1 -> "Mañana es el día de pago para: ${transaction.title}"
            else -> "Faltan $daysUntilPayment días para el pago de: ${transaction.title}"
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recordatorio de Pago")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId + transaction.id, notification)
    }
}
