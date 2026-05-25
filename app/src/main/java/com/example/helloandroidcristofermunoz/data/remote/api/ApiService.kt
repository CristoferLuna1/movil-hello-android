package com.example.helloandroidcristofermunoz.data.remote.api

import com.example.helloandroidcristofermunoz.data.remote.model.RemoteTransaction
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("posts")
    suspend fun getTransactions(): Response<List<RemoteTransaction>>
}