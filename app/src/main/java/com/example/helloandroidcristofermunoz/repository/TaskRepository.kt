package com.example.helloandroidcristofermunoz.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.helloandroidcristofermunoz.model.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskRepository(context: Context) {
    // Paso 3: Gestionar almacenamiento con SharedPreferences [cite: 6, 165]
    private val prefs: SharedPreferences = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private var tasksInMemory: MutableList<Task> = loadTasksFromPrefs()

    // Paso 5: Funciones para agregar, actualizar, eliminar y obtener tareas [cite: 8, 147]
    fun getAllTasks(): List<Task> = tasksInMemory.toList()

    fun addTask(task: Task) {
        tasksInMemory.add(task)
        saveTasksToPrefs()
    }

    fun updateTask(updated: Task) {
        val index = tasksInMemory.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            tasksInMemory[index] = updated
            saveTasksToPrefs()
        }
    }

    fun deleteTask(taskId: Int) {
        tasksInMemory.removeAll { it.id == taskId }
        saveTasksToPrefs()
    }

    // Paso 4: Implementar serialización y deserialización en JSON (uso de Gson) [cite: 7, 148, 149]
    private fun loadTasksFromPrefs(): MutableList<Task> {
        val json = prefs.getString("task_list", null) ?: return mutableListOf()
        val type = object : TypeToken<List<Task>>() {}.type
        return try {
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun saveTasksToPrefs() {
        val json = gson.toJson(tasksInMemory)
        prefs.edit().putString("task_list", json).apply()
    }
}