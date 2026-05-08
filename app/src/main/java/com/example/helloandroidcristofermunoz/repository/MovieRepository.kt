package com.example.helloandroidcristofermunoz.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.helloandroidcristofermunoz.db.MovieDao
import com.example.helloandroidcristofermunoz.db.MovieEntity
import com.example.helloandroidcristofermunoz.model.Movie

class MovieRepository(private val movieDao: MovieDao) {

    fun getMovies(): LiveData<List<Movie>> {
        val result = MediatorLiveData<List<Movie>>()
        result.addSource(movieDao.getAllMovies()) { list ->
            result.value = list.map { it.toMovie() }
        }
        return result
    }

    fun getMovie(id: Int): LiveData<Movie?> {
        val result = MediatorLiveData<Movie?>()
        result.addSource(movieDao.getMovieById(id)) { entity ->
            result.value = entity?.toMovie()
        }
        return result
    }

    suspend fun insert(movie: Movie) {
        movieDao.insert(movie.toEntity())
    }

    suspend fun update(movie: Movie) {
        movieDao.update(movie.toEntity())
    }

    suspend fun delete(movie: Movie) {
        movieDao.delete(movie.toEntity())
    }

    private fun MovieEntity.toMovie(): Movie =
        Movie(
            id = id,
            title = title,
            year = year,
            genre = genre,
            rating = rating,
            watched = watched
        )

    private fun Movie.toEntity(): MovieEntity =
        MovieEntity(
            id = id,
            title = title,
            year = year,
            genre = genre,
            rating = rating,
            watched = watched
        )
}
