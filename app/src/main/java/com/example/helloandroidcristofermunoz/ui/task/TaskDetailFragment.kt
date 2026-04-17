package com.example.helloandroidcristofermunoz.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.viewmodel.task.TaskDetailViewModel
import com.example.helloandroidcristofermunoz.viewmodel.task.TaskDetailViewModelFactory
import java.util.Calendar

class TaskDetailFragment : Fragment() {
    
    private lateinit var viewModel: TaskDetailViewModel
    
    private lateinit var titleView: TextView
    private lateinit var titleEdit: EditText
    private lateinit var descriptionEdit: EditText
    private lateinit var reminderSwitch: Switch
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    
    // Para selección de fecha y hora
    private lateinit var dateTimeLayout: LinearLayout
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var dateTimeText: TextView
    
    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private var selectedHour = 0
    private var selectedMinute = 0

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
        
        // Inicializar vistas de fecha y hora
        dateTimeLayout = view.findViewById(R.id.linearLayoutDateTime)
        dateButton = view.findViewById(R.id.buttonSelectDate)
        timeButton = view.findViewById(R.id.buttonSelectTime)
        dateTimeText = view.findViewById(R.id.textViewDateTime)
        
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
        
        // Listener para el switch de recordatorio
        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            dateTimeLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        // Listeners para fecha y hora
        dateButton.setOnClickListener {
            showDatePicker()
        }
        
        timeButton.setOnClickListener {
            showTimePicker()
        }
    }

    private fun setupObservers() {
        viewModel.taskSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                Toast.makeText(context, "Tarea guardada exitosamente", Toast.LENGTH_SHORT).show()
                
                // Forzar actualización del TaskListFragment antes de navegar hacia atrás
                findNavController().previousBackStackEntry?.savedStateHandle?.set("refresh_tasks", true)
                
                findNavController().navigateUp()
                // Resetear el estado para la próxima vez
                viewModel.resetTaskSaved()
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
        
        // Validar fecha y hora si el recordatorio está activado
        var reminderTime = 0L
        if (hasReminder) {
            if (selectedYear == 0 || selectedHour == 0) {
                Toast.makeText(context, "Debe seleccionar fecha y hora para el recordatorio", Toast.LENGTH_SHORT).show()
                return
            }
            
            val calendar = Calendar.getInstance()
            calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0)
            reminderTime = calendar.timeInMillis
            
            // Validar que la fecha sea futura
            if (reminderTime <= System.currentTimeMillis()) {
                Toast.makeText(context, "La fecha y hora deben ser futuras", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val taskId = arguments?.getInt("taskId") ?: -1
        if (taskId != -1) {
            // Modo edición
            viewModel.updateTask(taskId, title, description, hasReminder, reminderTime)
        } else {
            // Modo creación
            viewModel.createTask(title, description, hasReminder, reminderTime)
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                this.selectedYear = selectedYear
                this.selectedMonth = selectedMonth
                this.selectedDay = selectedDay
                updateDateTimeText()
            },
            year, month, day
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }
    
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                this.selectedHour = selectedHour
                this.selectedMinute = selectedMinute
                updateDateTimeText()
            },
            hour, minute, true
        )
        timePickerDialog.show()
    }
    
    private fun updateDateTimeText() {
        val dateText = if (selectedYear > 0) {
            "$selectedDay/${selectedMonth + 1}/$selectedYear"
        } else {
            "No seleccionada"
        }
        
        val timeText = if (selectedHour > 0) {
            String.format("%02d:%02d", selectedHour, selectedMinute)
        } else {
            "No seleccionada"
        }
        
        dateTimeText.text = "Fecha: $dateText | Hora: $timeText"
    }
}
