package com.example.helloandroidcristofermunoz

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.helloandroidcristofermunoz.data.repository.AuthRepository
import com.example.helloandroidcristofermunoz.databinding.ActivityRegisterBinding
import com.example.helloandroidcristofermunoz.ui.register.RegisterViewModel
import com.example.helloandroidcristofermunoz.ui.register.RegisterViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRepository()
        setupViewModel()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRepository() {
        authRepository = AuthRepository()
    }

    private fun setupViewModel() {
        val factory = RegisterViewModelFactory(authRepository)
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
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {

        viewModel.isSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, LoginActivity::class.java))
                finish()

                viewModel.resetSuccessState()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.resetErrorState()
            }
        }

        viewModel.nameError.observe(this) { binding.tilName.error = it }
        viewModel.emailError.observe(this) { binding.tilEmail.error = it }
        viewModel.passwordError.observe(this) { binding.tilPassword.error = it }
        viewModel.confirmPasswordError.observe(this) { binding.tilConfirmPassword.error = it }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility =
                if (loading) android.view.View.VISIBLE else android.view.View.GONE

            binding.btnRegister.isEnabled = !loading
        }
    }
}