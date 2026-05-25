package com.example.helloandroidcristofermunoz.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.example.helloandroidcristofermunoz.utils.NetworkState

class NetworkReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (context == null) return

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork

        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val isConnected =
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        // 🔥 ACTUALIZAR ESTADO GLOBAL (AQUÍ SÍ VA BIEN)
        NetworkState.update(isConnected)

        if (isConnected) {
            Toast.makeText(
                context,
                "Internet conectado",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                "Sin conexión",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}