package com.example.helloandroidcristofermunoz

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.helloandroidcristofermunoz.databinding.ActivityMainBinding
import com.example.helloandroidcristofermunoz.viewmodel.UserViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // El Navigation se maneja automáticamente con el FragmentContainerView
        // No necesitamos configurar ActionBar porque usamos NoActionBar
    }
}