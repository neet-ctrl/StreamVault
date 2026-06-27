package com.streamvault.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.domain.model.Movie
import com.streamvault.app.domain.model.TvShow
import com.streamvault.app.domain.repository.CinemetaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val featured: List<Movie> = emptyList(),
    val trendingMovies: List<Movie> = emptyList(),
    val popularMovies: List<Movie> = emptyList(),
    val popularTvShows: List<TvShow> = emptyList(),
    val topRatedMovies: List<Movie> = emptyList(),
    val nowPlaying: List<Movie> = emptyList(),
    val upcoming: List<Movie> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cinemetaRepository: CinemetaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Fetch top movies and top series from Cinemeta in parallel
                val moviesDeferred = async { cinemetaRepository.getTopMovies() }
                val seriesDeferred = async { cinemetaRepository.getTopSeries() }

                val movies = moviesDeferred.await().getOrDefault(emptyList())
                val series = seriesDeferred.await().getOrDefault(emptyList())

                _uiState.value = HomeUiState(
                    isLoading      = false,
                    featured       = movies.take(5),
                    trendingMovies = movies,
                    popularMovies  = movies,
                    popularTvShows = series,
                    topRatedMovies = movies.sortedByDescending { it.voteAverage },
                    nowPlaying     = movies.take(10),
                    upcoming       = series.take(10)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = e.message ?: "Failed to load content"
                )
            }
        }
    }

    fun retry() = loadHome()
}
