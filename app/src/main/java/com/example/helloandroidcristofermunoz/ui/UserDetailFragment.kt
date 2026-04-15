package com.example.helloandroidcristofermunoz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.helloandroidcristofermunoz.databinding.FragmentUserDetailBinding
import com.example.helloandroidcristofermunoz.viewmodel.UserViewModel

class UserDetailFragment : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by activityViewModels()
    
    // Recibir argumentos con Safe Args - ¡Type safe!
    private val args: UserDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener el userId de forma segura con Safe Args
        val userId = args.userId
        
        // Buscar el usuario por ID
        val user = viewModel.users.value?.find { it.id == userId }
        
        if (user != null) {
            binding.textViewUserName.text = user.name
            binding.textViewUserEmail.text = user.email
            binding.textViewUserAge.text = "${user.age} años"
        } else {
            Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
        
        binding.buttonDelete.setOnClickListener {
            viewModel.deleteUser(userId)
            Toast.makeText(requireContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}