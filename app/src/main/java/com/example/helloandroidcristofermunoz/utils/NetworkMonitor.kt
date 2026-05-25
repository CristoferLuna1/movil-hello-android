package com.example.helloandroidcristofermunoz.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

object NetworkMonitor {

    fun start(context: Context) {

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                NetworkState.update(true)
            }

            override fun onLost(network: Network) {
                NetworkState.update(false)
            }
        }

        connectivityManager.registerNetworkCallback(request, callback)
    }
}