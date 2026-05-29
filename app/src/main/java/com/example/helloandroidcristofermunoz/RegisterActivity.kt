package com.example.helloandroidcristofermunoz

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.UserRepository
import com.example.helloandroidcristofermunoz.databinding.ActivityRegisterBinding
import com.example.helloandroidcristofermunoz.ui.register.RegisterViewModel
import com.example.helloandroidcristofermunoz.ui.register.RegisterViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatabase()
        setupViewModel()
        setupClickListeners()
    }

    private fun setupDatabase() {
        val database = AppDatabase.getDatabase(this)
        userRepository = UserRepository(database.userDao())
    }

    private fun setupViewModel() {
        val factory = RegisterViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.edtName.text.toString()
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPassword = binding.edtConfirmPassword.text.toString()
            viewModel.validateAndRegister(name, email, password, confirmPassword)
        }

        binding.txtLogin.setOnClickListener {
            // Navegar a LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewModel.isSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Registro exitoso. Inicia sesión.", Toast.LENGTH_SHORT).show()
                // Navegar a LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                viewModel.resetSuccessState()
            }
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.resetErrorState()
            }
        }

        viewModel.nameError.observe(this) { error ->
            binding.tilName.error = error
        }

        viewModel.emailError.observe(this) { error ->
            binding.tilEmail.error = error
        }

        viewModel.passwordError.observe(this) { error ->
            binding.tilPassword.error = error
        }

        viewModel.confirmPasswordError.observe(this) { error ->
            binding.tilConfirmPassword.error = error
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
            binding.btnRegister.isEnabled = !isLoading
        }
    }
}
