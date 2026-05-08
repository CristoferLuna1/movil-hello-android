package com.example.helloandroidcristofermunoz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.db.AppDatabase
import com.example.helloandroidcristofermunoz.model.Movie
import com.example.helloandroidcristofermunoz.repository.MovieRepository
import kotlinx.coroutines.launch

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MovieRepository =
        MovieRepository(AppDatabase.getInstance(application).movieDao())

    val movies: LiveData<List<Movie>> = repository.getMovies()

    fun getMovie(id: Int): LiveData<Movie?> = repository.getMovie(id)

    fun insert(movie: Movie) {
        viewModelScope.launch {
            repository.insert(movie)
        }
    }

    fun update(movie: Movie) {
        viewModelScope.launch {
            repository.update(movie)
        }
    }

    fun delete(movie: Movie) {
        viewModelScope.launch {
            repository.delete(movie)
        }
    }
}
