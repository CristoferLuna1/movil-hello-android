package com.example.helloandroidcristofermunoz.repository.task

import android.content.Context
import android.content.SharedPreferences
import com.example.helloandroidcristofermunoz.model.task.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "tasks_prefs"
        private const val KEY_TASK_LIST = "task_list"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    // ==========================
    // LISTA EN MEMORIA
    // ==========================
    private var tasksInMemory: MutableList<Task> = loadTasksFromPrefs()

    // 🔥 IDs únicos
    private var nextId: Int = generateNextId()

    /** ==========================
     *  OBTENER TODAS LAS TAREAS
     *  🔥 FIX IMPORTANTE AQUÍ
     *  ========================== */
    fun getAllTasks(): List<Task> {
        tasksInMemory = loadTasksFromPrefs()
        return tasksInMemory.toList()
    }

    /** Agregar tarea */
    fun addTask(task: Task) {
        tasksInMemory.add(task)
        saveTasksToPrefs()
    }

    /** Crear tarea con ID seguro */
    fun createTask(
        title: String,
        description: String,
        hasReminder: Boolean,
        reminderTime: Long = 0L
    ): Task {

        val task = Task(
            id = nextId++,
            title = title,
            description = description,
            hasReminder = hasReminder,
            reminderTime = reminderTime
        )

        addTask(task)
        return task
    }

    /** Update */
    fun updateTask(updated: Task) {
        val index = tasksInMemory.indexOfFirst { it.id == updated.id }

        if (index != -1) {
            tasksInMemory[index] = updated
            saveTasksToPrefs()
        }
    }

    /** Delete */
    fun deleteTask(taskId: Int) {
        tasksInMemory = tasksInMemory.filter { it.id != taskId }.toMutableList()
        saveTasksToPrefs()
    }

    /** Get by ID */
    fun getTaskById(taskId: Int): Task? {
        return tasksInMemory.find { it.id == taskId }
    }

    // ==========================
    // PERSISTENCIA
    // ==========================
    private fun loadTasksFromPrefs(): MutableList<Task> {
        val json = prefs.getString(KEY_TASK_LIST, null) ?: return mutableListOf()

        return try {
            val type = object : TypeToken<List<Task>>() {}.type
            gson.fromJson<List<Task>>(json, type).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun saveTasksToPrefs() {
        prefs.edit()
            .putString(KEY_TASK_LIST, gson.toJson(tasksInMemory))
            .apply()
    }

    // 🔥 asegura IDs únicos
    private fun generateNextId(): Int {
        return (tasksInMemory.maxOfOrNull { it.id } ?: 0) + 1
    }
}