package com.example.helloandroidcristofermunoz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.helloandroidcristofermunoz.MainActivity
import com.example.helloandroidcristofermunoz.databinding.FragmentUserListBinding
import com.example.helloandroidcristofermunoz.viewmodel.UserViewModel

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.users.observe(viewLifecycleOwner) { userList ->
            binding.textViewUserCount.text = "Total usuarios: ${userList.size}"

            // CORREGIDO: it.name → it.nombre (según tu modelo User)
            val userNames = userList.joinToString("\n") { "${it.name} - ${it.email}" }
            binding.textViewUserList.text = userNames.ifEmpty { "No hay usuarios registrados" }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.buttonAddUser.setOnClickListener {
            // CORREGIDO: Llamar a MainActivity para mostrar el formulario
            (activity as? MainActivity)?.showAddUserFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}