package com.example.helloandroidcristofermunoz.ui.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

        titleView = view.findViewById(R.id.textViewTitle)
        titleEdit = view.findViewById(R.id.editTextTitle)
        descriptionEdit = view.findViewById(R.id.editTextDescription)
        reminderSwitch = view.findViewById(R.id.switchReminder)
        saveButton = view.findViewById(R.id.buttonSave)
        cancelButton = view.findViewById(R.id.buttonCancel)

        dateTimeLayout = view.findViewById(R.id.linearLayoutDateTime)
        dateButton = view.findViewById(R.id.buttonSelectDate)
        timeButton = view.findViewById(R.id.buttonSelectTime)
        dateTimeText = view.findViewById(R.id.textViewDateTime)

        viewModel = ViewModelProvider(
            this,
            TaskDetailViewModelFactory(requireActivity().application)
        )[TaskDetailViewModel::class.java]

        setupClickListeners()
        setupObservers()

        val taskId = arguments?.getInt("taskId") ?: -1
        if (taskId != -1) {
            viewModel.loadTask(taskId)
            titleEdit.setText(arguments?.getString("taskTitle") ?: "")
            descriptionEdit.setText(arguments?.getString("taskDescription") ?: "")
            reminderSwitch.isChecked = arguments?.getBoolean("taskHasReminder") ?: false
        } else {
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

        reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            dateTimeLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        dateButton.setOnClickListener { showDatePicker() }
        timeButton.setOnClickListener { showTimePicker() }
    }

    private fun setupObservers() {

        viewModel.taskSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {

                Toast.makeText(context, "Tarea guardada exitosamente", Toast.LENGTH_SHORT).show()

                // 🔥 FIX IMPORTANTE: PRIMERO navegar, luego marcar refresh
                findNavController().navigateUp()

                findNavController().previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("refresh_tasks", true)

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

        var reminderTime = 0L

        if (hasReminder) {

            if (selectedYear == 0 || selectedDay == 0) {
                Toast.makeText(context, "Debe seleccionar fecha y hora", Toast.LENGTH_SHORT).show()
                return
            }

            val calendar = Calendar.getInstance()
            calendar.set(
                selectedYear,
                selectedMonth,
                selectedDay,
                selectedHour,
                selectedMinute,
                0
            )

            reminderTime = calendar.timeInMillis

            if (reminderTime <= System.currentTimeMillis()) {
                Toast.makeText(context, "Debe ser una fecha futura", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val taskId = arguments?.getInt("taskId") ?: -1

        if (taskId != -1) {
            viewModel.updateTask(taskId, title, description, hasReminder, reminderTime)
        } else {
            viewModel.createTask(title, description, hasReminder, reminderTime)
        }
    }

    private fun showDatePicker() {

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                selectedYear = y
                selectedMonth = m
                selectedDay = d
                updateDateTimeText()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }.show()
    }

    private fun showTimePicker() {

        val calendar = Calendar.getInstance()

        TimePickerDialog(
            requireContext(),
            { _, h, min ->
                selectedHour = h
                selectedMinute = min
                updateDateTimeText()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateTimeText() {

        val dateText = if (selectedYear > 0)
            "$selectedDay/${selectedMonth + 1}/$selectedYear"
        else
            "No seleccionada"

        val timeText = if (selectedHour >= 0)
            String.format("%02d:%02d", selectedHour, selectedMinute)
        else
            "No seleccionada"

        dateTimeText.text = "Fecha: $dateText | Hora: $timeText"
    }
}