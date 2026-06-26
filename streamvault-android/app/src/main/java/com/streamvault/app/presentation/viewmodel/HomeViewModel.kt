package com.streamvault.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.domain.model.Movie
import com.streamvault.app.domain.model.TvShow
import com.streamvault.app.domain.repository.TmdbRepository
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
    private val tmdbRepository: TmdbRepository
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
                val trendingDeferred = async { tmdbRepository.getTrendingMovies() }
                val popularMoviesDeferred = async { tmdbRepository.getPopularMovies() }
                val popularTvDeferred = async { tmdbRepository.getPopularTv() }
                val topRatedDeferred = async { tmdbRepository.getTopRatedMovies() }
                val nowPlayingDeferred = async { tmdbRepository.getNowPlayingMovies() }
                val upcomingDeferred = async { tmdbRepository.getUpcomingMovies() }

                val trending = trendingDeferred.await().getOrDefault(emptyList())
                val popular = popularMoviesDeferred.await().getOrDefault(emptyList())
                val popularTv = popularTvDeferred.await().getOrDefault(emptyList())
                val topRated = topRatedDeferred.await().getOrDefault(emptyList())
                val nowPlaying = nowPlayingDeferred.await().getOrDefault(emptyList())
                val upcoming = upcomingDeferred.await().getOrDefault(emptyList())

                _uiState.value = HomeUiState(
                    isLoading = false,
                    featured = trending.take(5),
                    trendingMovies = trending,
                    popularMovies = popular,
                    popularTvShows = popularTv,
                    topRatedMovies = topRated,
                    nowPlaying = nowPlaying,
                    upcoming = upcoming
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load content"
                )
            }
        }
    }

    fun retry() = loadHome()
}
