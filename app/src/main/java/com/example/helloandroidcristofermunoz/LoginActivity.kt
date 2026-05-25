package com.example.helloandroidcristofermunoz

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.model.User
import com.example.helloandroidcristofermunoz.data.repository.UserRepository
import com.example.helloandroidcristofermunoz.databinding.ActivityLoginBinding
import com.example.helloandroidcristofermunoz.ui.login.LoginViewModel
import com.example.helloandroidcristofermunoz.ui.login.LoginViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDatabase()
        setupViewModel()
        setupClickListeners()
        createTestUserIfNeeded()
        checkLoggedInUser()
    }

    private fun setupDatabase() {
        val database = AppDatabase.getDatabase(this)
        userRepository = UserRepository(database.userDao())
    }

    private fun setupViewModel() {
        val factory = LoginViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()
            viewModel.validateAndLogin(email, password)
        }

        binding.txtRegister.setOnClickListener {
            Toast.makeText(this, "Registro - Próximamente", Toast.LENGTH_SHORT).show()
        }

        viewModel.isSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                navigateToMain()
                viewModel.resetSuccessState()
            }
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.resetErrorState()
            }
        }

        viewModel.emailError.observe(this) { error ->
            binding.tilEmail.error = error
        }

        viewModel.passwordError.observe(this) { error ->
            binding.tilPassword.error = error
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
            binding.btnLogin.isEnabled = !isLoading
        }
    }

    private fun createTestUserIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            val users = userRepository.getAllUsers()
            if (users.isEmpty()) {
                // Crear usuario de prueba
                val testUser = User(
                    name = "Usuario Prueba",
                    email = "test@test.com",
                    password = "123456",
                    isLoggedIn = false
                )
                userRepository.register(testUser)
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Usuario de prueba creado: test@test.com / 123456", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkLoggedInUser() {
        CoroutineScope(Dispatchers.IO).launch {
            val loggedInUser = userRepository.getLoggedInUser()
            if (loggedInUser != null) {
                navigateToMain()
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
