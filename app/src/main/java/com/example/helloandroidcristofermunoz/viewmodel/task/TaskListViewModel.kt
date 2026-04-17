package com.example.helloandroidcristofermunoz.viewmodel.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.model.task.Task
import com.example.helloandroidcristofermunoz.repository.task.TaskRepository
import kotlinx.coroutines.launch

class TaskListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = TaskRepository(application)
    
    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadTasks() {
        viewModelScope.launch {
            try {
                _tasks.value = repository.getAllTasks()
            } catch (e: Exception) {
                _error.value = "Error al cargar las tareas: ${e.message}"
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteTask(taskId)
                loadTasks() // Recargar la lista
            } catch (e: Exception) {
                _error.value = "Error al eliminar la tarea: ${e.message}"
            }
        }
    }
}
