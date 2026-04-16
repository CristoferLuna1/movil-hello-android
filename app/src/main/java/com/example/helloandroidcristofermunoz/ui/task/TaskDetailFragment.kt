package com.example.helloandroidcristofermunoz.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.helloandroidcristofermunoz.databinding.FragmentTaskDetailBinding
import com.example.helloandroidcristofermunoz.viewmodel.task.TaskDetailViewModel

class TaskDetailFragment : Fragment() {
    
    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: TaskDetailViewModel
    private val args: TaskDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[TaskDetailViewModel::class.java]
        
        setupClickListeners()
        setupObservers()
        
        // Cargar tarea si estamos en modo edición
        val taskId = args.taskId
        if (taskId != -1) {
            viewModel.loadTask(taskId)
            binding.editTextTitle.setText(args.taskTitle)
            binding.editTextDescription.setText(args.taskDescription)
            binding.switchReminder.isChecked = args.taskHasReminder
        } else {
            // Modo creación
            binding.textViewTitle.text = "Nueva Tarea"
        }
    }

    private fun setupClickListeners() {
        binding.buttonSave.setOnClickListener {
            saveTask()
        }
        
        binding.buttonCancel.setOnClickListener {
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
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val hasReminder = binding.switchReminder.isChecked

        if (title.isEmpty()) {
            Toast.makeText(context, "El título es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val taskId = args.taskId
        if (taskId != -1) {
            // Modo edición
            viewModel.updateTask(taskId, title, description, hasReminder)
        } else {
            // Modo creación
            viewModel.createTask(title, description, hasReminder)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
