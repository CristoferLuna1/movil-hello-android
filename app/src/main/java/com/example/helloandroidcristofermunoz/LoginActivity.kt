package com.example.helloandroidcristofermunoz

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.helloandroidcristofermunoz.data.repository.AuthRepository
import com.example.helloandroidcristofermunoz.databinding.ActivityLoginBinding
import com.example.helloandroidcristofermunoz.ui.login.LoginViewModel
import com.example.helloandroidcristofermunoz.ui.login.LoginViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔥 Si ya hay sesión iniciada -> entra directo
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {

            Log.d("AUTH_FLOW", "USER ALREADY LOGGED -> ${user.email}")

            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setupViewModel()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViewModel() {

        val factory = LoginViewModelFactory(
            AuthRepository()
        )

        viewModel =
            ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }

    private fun setupClickListeners() {

        binding.btnLogin.setOnClickListener {

            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            Log.d("AUTH_FLOW", "LOGIN BUTTON CLICKED")
            Log.d("AUTH_FLOW", "email=$email")

            viewModel.validateAndLogin(email, password)
        }

        binding.txtRegister.setOnClickListener {

            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }
    }

    private fun observeViewModel() {

        viewModel.isSuccess.observe(this) { success ->

            if (success) {

                Log.d("AUTH_FLOW", "NAVIGATING TO MAIN")

                val intent = Intent(this, MainActivity::class.java)

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { error ->

            error?.let {

                Log.e("AUTH_FLOW", "LOGIN ERROR -> $it")

                Toast.makeText(
                    this,
                    it,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.emailError.observe(this) { error ->

            binding.tilEmail.error = error
        }

        viewModel.passwordError.observe(this) { error ->

            binding.tilPassword.error = error
        }

        viewModel.isLoading.observe(this) { isLoading ->

            binding.progressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE

            binding.btnLogin.isEnabled = !isLoading
        }
    }
}