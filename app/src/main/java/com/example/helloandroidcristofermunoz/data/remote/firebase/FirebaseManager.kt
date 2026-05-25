package com.example.helloandroidcristofermunoz.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {

    val database: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}