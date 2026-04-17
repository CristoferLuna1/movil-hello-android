package com.example.helloandroidcristofermunoz.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.viewmodel.task.TaskDetailViewModel
import com.example.helloandroidcristofermunoz.viewmodel.task.TaskDetailViewModelFactory

class TaskDetailFragment : Fragment() {
    
    private lateinit var viewModel: TaskDetailViewModel
    
    private lateinit var titleView: TextView
    private lateinit var titleEdit: EditText
    private lateinit var descriptionEdit: EditText
    private lateinit var reminderSwitch: Switch
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_task_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicializar vistas
        titleView = view.findViewById(R.id.textViewTitle)
        titleEdit = view.findViewById(R.id.editTextTitle)
        descriptionEdit = view.findViewById(R.id.editTextDescription)
        reminderSwitch = view.findViewById(R.id.switchReminder)
        saveButton = view.findViewById(R.id.buttonSave)
        cancelButton = view.findViewById(R.id.buttonCancel)
        
        viewModel = ViewModelProvider(this, TaskDetailViewModelFactory(requireActivity().application))[TaskDetailViewModel::class.java]
        
        setupClickListeners()
        setupObservers()
        
        // Cargar tarea si estamos en modo edición
        val taskId = arguments?.getInt("taskId") ?: -1
        if (taskId != -1) {
            viewModel.loadTask(taskId)
            titleEdit.setText(arguments?.getString("taskTitle") ?: "")
            descriptionEdit.setText(arguments?.getString("taskDescription") ?: "")
            reminderSwitch.isChecked = arguments?.getBoolean("taskHasReminder") ?: false
        } else {
            // Modo creación
            titleView.text = "Nueva Tarea"
        }
    }

    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveTask()
        }
        
        cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.taskSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                Toast.makeText(context, "Tarea guardada exitosamente", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTask() {
        val title = titleEdit.text.toString().trim()
        val description = descriptionEdit.text.toString().trim()
        val hasReminder = reminderSwitch.isChecked

        if (title.isEmpty()) {
            Toast.makeText(context, "El título es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val taskId = arguments?.getInt("taskId") ?: -1
        if (taskId != -1) {
            // Modo edición
            viewModel.updateTask(taskId, title, description, hasReminder)
        } else {
            // Modo creación
            viewModel.createTask(title, description, hasReminder)
        }
    }
}
