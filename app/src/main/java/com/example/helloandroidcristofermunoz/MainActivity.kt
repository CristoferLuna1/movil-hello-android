package com.example.helloandroidcristofermunoz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.repository.UserRepository
import com.example.helloandroidcristofermunoz.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup database
        val database = AppDatabase.getDatabase(this)
        userRepository = UserRepository(database.userDao())

        // Check authentication
        checkAuthentication()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun checkAuthentication() {
        lifecycleScope.launch {
            val loggedInUser = userRepository.getLoggedInUser()
            if (loggedInUser == null) {
                // No user logged in, redirect to login
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupNavigation() {
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)

        val navController = navHost
            ?.findNavController()

        if (navController != null) {
            binding.bottomNavigation.setupWithNavController(navController)
        }
    }
}
