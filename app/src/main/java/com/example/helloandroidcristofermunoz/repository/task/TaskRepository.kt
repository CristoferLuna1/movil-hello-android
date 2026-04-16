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
    
    // Lista en memoria (copia de trabajo)
    private var tasksInMemory: MutableList<Task> = loadTasksFromPrefs()
    private var nextId: Int = tasksInMemory.maxOfOrNull { it.id }?.plus(1) ?: 1

    /** Devuelve una copia inmutable de la lista de tareas */
    fun getAllTasks(): List<Task> = tasksInMemory.toList()

    /** Agrega una nueva tarea y persiste el cambio */
    fun addTask(task: Task) {
        tasksInMemory.add(task)
        saveTasksToPrefs()
    }

    /** Crea una nueva tarea con ID automático */
    fun createTask(title: String, description: String, hasReminder: Boolean): Task {
        val task = Task(nextId++, title, description, hasReminder)
        addTask(task)
        return task
    }

    /** Actualiza una tarea existente por id (si existe) y persiste el cambio */
    fun updateTask(updated: Task) {
        val index = tasksInMemory.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            tasksInMemory[index] = updated
            saveTasksToPrefs()
        }
    }

    /** Elimina una tarea por id y persiste el cambio */
    fun deleteTask(taskId: Int) {
        tasksInMemory.removeAll { it.id == taskId }
        saveTasksToPrefs()
    }

    /** Obtiene una tarea por su ID */
    fun getTaskById(taskId: Int): Task? {
        return tasksInMemory.find { it.id == taskId }
    }

    // ==========================
    // Sección de persistencia
    // ==========================

    /** Carga la lista de tareas desde SharedPreferences (JSON -> List<Task>) */
    private fun loadTasksFromPrefs(): MutableList<Task> {
        val json = prefs.getString(KEY_TASK_LIST, null) ?: return mutableListOf()
        
        // Tipo genérico List<Task> para Gson
        val type = object : TypeToken<List<Task>>() {}.type
        return try {
            val list: List<Task> = gson.fromJson(json, type)
            list.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }

    /** Guarda la lista de tareas actual en SharedPreferences (List<Task> -> JSON) */
    private fun saveTasksToPrefs() {
        val editor = prefs.edit()
        val json = gson.toJson(tasksInMemory)
        editor.putString(KEY_TASK_LIST, json)
        editor.apply()
    }
}
