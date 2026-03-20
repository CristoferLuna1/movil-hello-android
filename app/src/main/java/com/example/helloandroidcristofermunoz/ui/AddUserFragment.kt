package com.example.helloandroidcristofermunoz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.helloandroidcristofermunoz.MainActivity
import com.example.helloandroidcristofermunoz.databinding.FragmentAddUserBinding
import com.example.helloandroidcristofermunoz.viewmodel.UserViewModel

class AddUserFragment : Fragment() {

    private var _binding: FragmentAddUserBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSaveUser.setOnClickListener {
            val nombre = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val edadText = binding.editTextAge.text.toString().trim()

            when {
                nombre.isEmpty() -> {
                    binding.editTextName.error = "Ingrese el nombre"
                    return@setOnClickListener
                }
                email.isEmpty() -> {
                    binding.editTextEmail.error = "Ingrese el correo"
                    return@setOnClickListener
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    binding.editTextEmail.error = "Correo inválido"
                    return@setOnClickListener
                }
                edadText.isEmpty() -> {
                    binding.editTextAge.error = "Ingrese la edad"
                    return@setOnClickListener
                }
                edadText.toIntOrNull() == null -> {
                    binding.editTextAge.error = "Edad inválida"
                    return@setOnClickListener
                }
            }

            val edad = edadText.toInt()
            
            // Agregar usuario - esto actualizará automáticamente el LiveData
            viewModel.addUser(nombre, email, edad)
            
            Toast.makeText(requireContext(), "Usuario agregado correctamente", Toast.LENGTH_SHORT).show()
            
            // Cerrar el formulario
            (activity as? MainActivity)?.hideAddUserFragment()
        }

        binding.buttonCancel.setOnClickListener {
            (activity as? MainActivity)?.hideAddUserFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}