package com.example.helloandroidcristofermunoz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.viewmodel.MovieViewModel

class MovieDetailFragment : Fragment() {

    private lateinit var viewModel: MovieViewModel
    private var currentMovieId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]

        val args = MovieDetailFragmentArgs.fromBundle(requireArguments())
        currentMovieId = args.movieId

        val tvMovieTitle = view.findViewById<TextView>(R.id.tvMovieTitle)
        val tvMovieDetails = view.findViewById<TextView>(R.id.tvMovieDetails)
        val tvMovieWatched = view.findViewById<TextView>(R.id.tvMovieWatched)
        val btnToggleWatched = view.findViewById<Button>(R.id.btnToggleWatched)
        val btnEditMovie = view.findViewById<Button>(R.id.btnEditMovie)

        viewModel.getMovie(currentMovieId).observe(viewLifecycleOwner) { movie ->
            if (movie != null) {
                tvMovieTitle.text = movie.title
                tvMovieDetails.text = "${movie.genre} • ${movie.year} • Rating: ${movie.rating}"
                tvMovieWatched.text = if (movie.watched) getString(R.string.watched) else getString(R.string.not_watched)
                btnToggleWatched.text = if (movie.watched) getString(R.string.mark_as_not_watched) else getString(R.string.mark_as_watched)

                btnToggleWatched.setOnClickListener {
                    viewModel.update(movie.copy(watched = !movie.watched))
                }

                btnEditMovie.setOnClickListener {
                    val action = MovieDetailFragmentDirections.actionDetailToEdit(movie.id)
                    findNavController().navigate(action)
                }
            } else {
                tvMovieTitle.text = getString(R.string.movie_not_found)
                tvMovieDetails.text = ""
                tvMovieWatched.text = ""
                btnToggleWatched.isEnabled = false
                btnEditMovie.isEnabled = false
            }
        }
    }
}
