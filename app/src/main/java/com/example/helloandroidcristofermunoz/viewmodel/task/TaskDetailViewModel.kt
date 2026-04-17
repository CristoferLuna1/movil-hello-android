package com.example.helloandroidcristofermunoz.viewmodel.task

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.model.task.Task
import com.example.helloandroidcristofermunoz.repository.task.TaskRepository
import com.example.helloandroidcristofermunoz.utils.AlarmUtils
import kotlinx.coroutines.launch

class TaskDetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = TaskRepository(application)
    
    private val _task = MutableLiveData<Task?>()
    val task: LiveData<Task?> = _task
    
    private val _taskSaved = MutableLiveData<Boolean>()
    val taskSaved: LiveData<Boolean> = _taskSaved
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadTask(taskId: Int) {
        viewModelScope.launch {
            try {
                _task.value = repository.getTaskById(taskId)
            } catch (e: Exception) {
                _error.value = "Error al cargar la tarea: ${e.message}"
            }
        }
    }

    fun createTask(title: String, description: String, hasReminder: Boolean, reminderTime: Long = 0L) {
        viewModelScope.launch {
            try {
                val task = repository.createTask(title, description, hasReminder, reminderTime)
                
                // Programar recordatorio si está activado
                if (hasReminder && reminderTime > 0) {
                    AlarmUtils.scheduleTaskReminder(
                        getApplication(),
                        task.id,
                        task.title,
                        task.description,
                        reminderTime
                    )
                }
                
                _taskSaved.value = true
            } catch (e: Exception) {
                _error.value = "Error al crear la tarea: ${e.message}"
            }
        }
    }

    fun updateTask(taskId: Int, title: String, description: String, hasReminder: Boolean, reminderTime: Long = 0L) {
        viewModelScope.launch {
            try {
                val existingTask = repository.getTaskById(taskId)
                existingTask?.let {
                    // Cancelar recordatorio anterior si existía
                    if (it.hasReminder) {
                        AlarmUtils.cancelTaskReminder(getApplication(), taskId)
                    }
                    
                    val updatedTask = it.copy(
                        title = title,
                        description = description,
                        hasReminder = hasReminder,
                        reminderTime = reminderTime
                    )
                    
                    repository.updateTask(updatedTask)
                    
                    // Programar nuevo recordatorio si está activado
                    if (hasReminder && reminderTime > 0) {
                        AlarmUtils.scheduleTaskReminder(
                            getApplication(),
                            taskId,
                            title,
                            description,
                            reminderTime
                        )
                    }
                    
                    _taskSaved.value = true
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar la tarea: ${e.message}"
            }
        }
    }
    
    /** Resetea el estado de taskSaved */
    fun resetTaskSaved() {
        _taskSaved.value = false
    }
}
