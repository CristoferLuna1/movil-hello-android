package com.example.helloandroidcristofermunoz.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.adapter.TaskAdapter
import com.example.helloandroidcristofermunoz.model.task.Task
import com.example.helloandroidcristofermunoz.viewmodel.task.TaskListViewModel
import com.example.helloandroidcristofermunoz.viewmodel.task.TaskListViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TaskListFragment : Fragment() {

    private lateinit var viewModel: TaskListViewModel
    private lateinit var taskAdapter: TaskAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewTasks)
        emptyView = view.findViewById(R.id.textViewEmpty)
        fabAdd = view.findViewById(R.id.fabAddTask)

        viewModel = ViewModelProvider(
            this,
            TaskListViewModelFactory(requireActivity().application)
        )[TaskListViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        // 🔥 CARGA INICIAL (OBLIGATORIO)
        viewModel.loadTasks()

        // 🔥 REFRESH DESDE DETAIL
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("refresh_tasks")
            ?.observe(viewLifecycleOwner) { shouldRefresh ->

                if (shouldRefresh == true) {
                    viewModel.loadTasks()

                    findNavController().currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("refresh_tasks", false)
                }
            }
    }

    private fun setupRecyclerView() {

        taskAdapter = TaskAdapter(
            onTaskClick = { task ->

                val bundle = Bundle().apply {
                    putInt("taskId", task.id)
                    putString("taskTitle", task.title)
                    putString("taskDescription", task.description)
                    putBoolean("taskHasReminder", task.hasReminder)
                }

                findNavController().navigate(
                    R.id.action_taskListFragment_to_taskDetailFragment,
                    bundle
                )
            },
            onDeleteClick = { task ->
                deleteTask(task)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }
    }

    private fun setupObservers() {

        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->

            taskAdapter.submitList(tasks)

            if (tasks.isEmpty()) {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {

        fabAdd.setOnClickListener {

            val bundle = Bundle().apply {
                putInt("taskId", -1)
                putString("taskTitle", "")
                putString("taskDescription", "")
                putBoolean("taskHasReminder", false)
            }

            findNavController().navigate(
                R.id.action_taskListFragment_to_taskDetailFragment,
                bundle
            )
        }
    }

    private fun deleteTask(task: Task) {

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Tarea")
            .setMessage("¿Eliminar \"${task.title}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteTask(task.id)
                Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}