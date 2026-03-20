/* package com.example.helloandroidcristofermunoz

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        //declarar variables
        val textView = findViewById<TextView>(R.id.textView) //usamos para llamar un elemento de la interfaz
        val botton = findViewById<Button>(R.id.btnSaludar)
        // variable contador
        var contador = 0

        //asiganar accion al boton
        botton.setOnClickListener {
            contador ++
            textView.text = "Has hecho clic $contador veces" //usamos $para llamar una variable en una cadena de texto
            //Mostrar mensaje emergente
            Toast.makeText(
                this,
                "Boton presionado",
                Toast.LENGTH_SHORT
            ).show()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
} */


package com.example.helloandroidcristofermunoz

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.helloandroidcristofermunoz.databinding.ActivityMainBinding
import com.example.helloandroidcristofermunoz.ui.AddUserFragment
import com.example.helloandroidcristofermunoz.ui.UserListFragment
import com.example.helloandroidcristofermunoz.viewmodel.UserViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    // ViewModel a nivel de Activity (compartido con fragments)
    private val viewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Cargar el fragment principal (lista de usuarios)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container_main, UserListFragment())
                .commit()
        }
    }
    
    fun showAddUserFragment() {
        // Mostrar el overlay y agregar el fragment
        binding.containerOverlay.visibility = View.VISIBLE
        
        supportFragmentManager.beginTransaction()
            .add(R.id.container_overlay, AddUserFragment())
            .addToBackStack(null)
            .commit()
    }
    
    fun hideAddUserFragment() {
        // Ocultar el overlay y remover el fragment
        supportFragmentManager.popBackStack()
        binding.containerOverlay.visibility = View.GONE
    }
}
