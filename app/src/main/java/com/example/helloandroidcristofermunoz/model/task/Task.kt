package com.example.helloandroidcristofermunoz.model.task

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val hasReminder: Boolean,
    val reminderTime: Long = 0L
)
