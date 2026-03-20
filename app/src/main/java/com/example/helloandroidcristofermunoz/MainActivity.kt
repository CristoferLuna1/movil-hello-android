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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.helloandroidcristofermunoz.viewmodel.UserViewModel
import com.example.helloandroidcristofermunoz.R


class MainActivity : AppCompatActivity() {
    
    // ViewModel a nivel de Activity (compartido con fragments)
    private val viewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // El Fragment se carga automáticamente desde el XML
        // El ViewModel está disponible para todos los fragments
    }
}
