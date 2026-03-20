package com.example.helloandroidcristofermunoz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloandroidcristofermunoz.model.User
import com.example.helloandroidcristofermunoz.databinding.FragmentUserListBinding
import com.example.helloandroidcristofermunoz.viewmodel.UserViewModel



class UserListFragment : Fragment() {
    
    // ViewBinding para referenciar vistas
    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!
    
    // ViewModel compartido a nivel de Activity
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
        // Observar cambios en la lista de usuarios
        viewModel.users.observe(viewLifecycleOwner) { userList ->
            // Por ahora solo mostrar el contador
            binding.textViewUserCount.text = "Total usuarios: ${userList.size}"
            
            // Mostrar lista en un TextView temporal
            val userNames = userList.joinToString("\n") { "${it.name} - ${it.email}" }
            binding.textViewUserList.text = userNames
        }
        
        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonAddUser.setOnClickListener {
            // Por ahora solo mostrar Toast
            Toast.makeText(context, "Agregar usuario próximamente", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Evitar memory leaks
    }
}
