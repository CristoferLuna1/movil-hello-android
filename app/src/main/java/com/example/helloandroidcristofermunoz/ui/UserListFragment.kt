package com.example.helloandroidcristofermunoz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.helloandroidcristofermunoz.R
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
            
            if (userList.isEmpty()) {
                binding.textViewUserList.text = "No hay usuarios registrados"
            } else {
                val userListText = userList.joinToString("\n") { user ->
                    "• ${user.name} (${user.age} años) - ${user.email}"
                }
                binding.textViewUserList.text = userListText
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.buttonAddUser.setOnClickListener {
            findNavController().navigate(R.id.action_list_to_add)
        }
        
        // Hacer click en la lista para ver detalle
        binding.textViewUserList.setOnClickListener {
            val currentUser = viewModel.users.value?.firstOrNull()
            currentUser?.let { user ->
                val bundle = Bundle().apply {
                    putInt("userId", user.id)
                }
                findNavController().navigate(R.id.action_list_to_detail, bundle)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}