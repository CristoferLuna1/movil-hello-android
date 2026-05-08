package com.example.helloandroidcristofermunoz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.model.Movie
import com.example.helloandroidcristofermunoz.viewmodel.MovieViewModel

class MovieEditFragment : Fragment() {

    private lateinit var viewModel: MovieViewModel
    private var movieId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_movie_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]

        val args = MovieEditFragmentArgs.fromBundle(requireArguments())
        movieId = args.movieId

        val etTitle = view.findViewById<EditText>(R.id.etMovieTitle)
        val etYear = view.findViewById<EditText>(R.id.etMovieYear)
        val etGenre = view.findViewById<EditText>(R.id.etMovieGenre)
        val etRating = view.findViewById<EditText>(R.id.etMovieRating)
        val cbWatched = view.findViewById<CheckBox>(R.id.cbMovieWatched)
        val btnSaveMovie = view.findViewById<Button>(R.id.btnSaveMovie)

        if (movieId >= 0) {
            viewModel.getMovie(movieId).observe(viewLifecycleOwner) { movie ->
                if (movie != null) {
                    etTitle.setText(movie.title)
                    etYear.setText(movie.year.toString())
                    etGenre.setText(movie.genre)
                    etRating.setText(movie.rating.toString())
                    cbWatched.isChecked = movie.watched
                }
            }
        }

        btnSaveMovie.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val year = etYear.text.toString().toIntOrNull() ?: 0
            val genre = etGenre.text.toString().trim()
            val rating = etRating.text.toString().toFloatOrNull() ?: 0f
            val watched = cbWatched.isChecked

            if (title.isEmpty() || year <= 0 || genre.isEmpty() || rating <= 0f) {
                return@setOnClickListener
            }

            val movie = Movie(
                id = if (movieId >= 0) movieId else 0,
                title = title,
                year = year,
                genre = genre,
                rating = rating,
                watched = watched
            )

            if (movieId >= 0) {
                viewModel.update(movie)
            } else {
                viewModel.insert(movie)
            }

            findNavController().navigateUp()
        }
    }
}
