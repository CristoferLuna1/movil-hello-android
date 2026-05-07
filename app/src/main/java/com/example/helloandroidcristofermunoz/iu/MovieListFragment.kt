package com.example.helloandroidcristofermunoz.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.helloandroidcristofermunoz.R
import com.example.helloandroidcristofermunoz.viewmodel.MovieViewModel

class MovieListFragment : Fragment() {

    private lateinit var viewModel: MovieViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_movie_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvMovies)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]

        viewModel.movies.observe(viewLifecycleOwner) { movies ->

            recyclerView.adapter = MovieAdapter(movies) { selectedMovie ->

                val bundle = Bundle()

                bundle.putString("movieTitle", selectedMovie)

                findNavController().navigate(
                    R.id.action_list_to_detail,
                    bundle
                )
            }
        }
    }
}