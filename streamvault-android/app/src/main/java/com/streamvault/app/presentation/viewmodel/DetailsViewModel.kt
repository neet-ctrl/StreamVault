package com.streamvault.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.domain.model.*
import com.streamvault.app.domain.repository.LocalRepository
import com.streamvault.app.domain.repository.TmdbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailsUiState(
    val isLoading: Boolean = true,
    val movieDetails: MovieDetails? = null,
    val tvDetails: TvDetails? = null,
    val isFavorite: Boolean = false,
    val watchHistory: WatchHistory? = null,
    val error: String? = null,
    val mediaType: MediaType = MediaType.MOVIE
)

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val tmdbRepository: TmdbRepository,
    private val localRepository: LocalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private var currentMovieId: Int = -1

    fun loadMovieDetails(movieId: Int) {
        if (currentMovieId == movieId) return
        currentMovieId = movieId
        viewModelScope.launch {
            _uiState.value = DetailsUiState(isLoading = true, mediaType = MediaType.MOVIE)
            tmdbRepository.getMovieDetails(movieId).fold(
                onSuccess = { details ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        movieDetails = details
                    )
                    checkFavoriteStatus(movieId)
                    loadHistory(movieId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun loadTvDetails(tvId: Int) {
        if (currentMovieId == tvId) return
        currentMovieId = tvId
        viewModelScope.launch {
            _uiState.value = DetailsUiState(isLoading = true, mediaType = MediaType.TV)
            tmdbRepository.getTvDetails(tvId).fold(
                onSuccess = { details ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tvDetails = details
                    )
                    checkFavoriteStatus(tvId)
                    loadHistory(tvId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    private fun checkFavoriteStatus(movieId: Int) {
        viewModelScope.launch {
            localRepository.isFavorite(movieId).collect { isFav ->
                _uiState.value = _uiState.value.copy(isFavorite = isFav)
            }
        }
    }

    private fun loadHistory(movieId: Int) {
        viewModelScope.launch {
            val history = localRepository.getHistoryForMovie(movieId)
            _uiState.value = _uiState.value.copy(watchHistory = history)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val state = _uiState.value
            val movieId = currentMovieId
            if (state.isFavorite) {
                localRepository.removeFavorite(movieId)
            } else {
                val title = state.movieDetails?.movie?.title ?: state.tvDetails?.tvShow?.name ?: ""
                val poster = state.movieDetails?.movie?.posterPath ?: state.tvDetails?.tvShow?.posterPath
                val rating = state.movieDetails?.movie?.voteAverage ?: state.tvDetails?.tvShow?.voteAverage ?: 0.0
                val mediaType = if (state.tvDetails != null) "tv" else "movie"
                localRepository.addFavorite(
                    Favorite(
                        id = "${mediaType}_${movieId}",
                        movieId = movieId,
                        title = title,
                        posterPath = poster,
                        mediaType = mediaType,
                        rating = rating
                    )
                )
            }
        }
    }
}
