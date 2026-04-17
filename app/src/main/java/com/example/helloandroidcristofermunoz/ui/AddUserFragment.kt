package com.example.helloandroidcristofermunoz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val ageText = binding.editTextAge.text.toString().trim()

            when {
                name.isEmpty() -> {
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
                ageText.isEmpty() -> {
                    binding.editTextAge.error = "Ingrese la edad"
                    return@setOnClickListener
                }
                ageText.toIntOrNull() == null -> {
                    binding.editTextAge.error = "Edad inválida"
                    return@setOnClickListener
                }
            }

            val age = ageText.toInt()
            viewModel.addUser(name, email, age)
            Toast.makeText(requireContext(), "Usuario agregado correctamente", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}