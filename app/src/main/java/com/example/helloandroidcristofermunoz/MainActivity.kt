package com.example.helloandroidcristofermunoz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.databinding.ActivityMainBinding
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.notification.DebtNotificationManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)

        val navController = navHost
            ?.findNavController()

        if (navController != null) {
            binding.bottomNavigation.setupWithNavController(navController)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.loginFragment, R.id.registerFragment -> {
                        binding.bottomNavigation.visibility = android.view.View.GONE
                    }
                    else -> {
                        binding.bottomNavigation.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }

        // Verificar notificaciones de deudas al iniciar la app
        checkDebtNotifications()
    }

    private fun checkDebtNotifications() {
        lifecycleScope.launch {
            try {
                val userDao = AppDatabase.getDatabase(this@MainActivity).userDao()
                val currentUser = userDao.getLoggedInUser()
                currentUser?.let { user ->
                    val transactionDao = AppDatabase.getDatabase(this@MainActivity).transactionDao()
                    val notificationManager = DebtNotificationManager(this@MainActivity, transactionDao)
                    notificationManager.checkAndSendDebtNotifications(user.id)
                }
            } catch (e: Exception) {
                // Manejar error silenciosamente
            }
        }
    }
}