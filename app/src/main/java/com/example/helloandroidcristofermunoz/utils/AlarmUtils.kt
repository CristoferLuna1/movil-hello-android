package com.example.helloandroidcristofermunoz.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.helloandroidcristofermunoz.receiver.TaskReminderReceiver

object AlarmUtils {
    
    fun scheduleTaskReminder(
        context: Context,
        taskId: Int,
        taskTitle: String,
        taskDescription: String,
        triggerTime: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(TaskReminderReceiver.EXTRA_TASK_DESCRIPTION, taskDescription)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId, // Usar taskId como requestCode para que sea único
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Manejar caso donde no se tiene permiso para alarmas exactas
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
    
    fun cancelTaskReminder(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
}
