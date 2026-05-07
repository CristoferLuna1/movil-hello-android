package com.example.helloandroidcristofermunoz.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MovieViewModel : ViewModel() {

    private val _movies = MutableLiveData<List<String>>()

    val movies: LiveData<List<String>> = _movies

    init {
        loadMovies()
    }

    private fun loadMovies() {
        _movies.value = listOf(
            "Avatar",
            "Avengers",
            "Batman",
            "Spiderman"
        )
    }
}