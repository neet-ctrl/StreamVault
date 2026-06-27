package com.streamvault.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.domain.model.*
import com.streamvault.app.domain.repository.CinemetaRepository
import com.streamvault.app.domain.repository.LocalRepository
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
    private val cinemetaRepository: CinemetaRepository,
    private val localRepository: LocalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    /** IMDB ID, e.g. "tt1234567" */
    private var currentImdbId: String = ""

    fun loadMovieDetails(imdbId: String) {
        if (currentImdbId == imdbId) return
        currentImdbId = imdbId
        viewModelScope.launch {
            _uiState.value = DetailsUiState(isLoading = true, mediaType = MediaType.MOVIE)
            cinemetaRepository.getMovieMeta(imdbId).fold(
                onSuccess = { details ->
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        movieDetails = details
                    )
                    checkFavoriteStatus(imdbId)
                    loadHistory(imdbId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = error.message
                    )
                }
            )
        }
    }

    fun loadTvDetails(imdbId: String) {
        if (currentImdbId == imdbId) return
        currentImdbId = imdbId
        viewModelScope.launch {
            _uiState.value = DetailsUiState(isLoading = true, mediaType = MediaType.TV)
            cinemetaRepository.getSeriesMeta(imdbId).fold(
                onSuccess = { details ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tvDetails = details
                    )
                    checkFavoriteStatus(imdbId)
                    loadHistory(imdbId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = error.message
                    )
                }
            )
        }
    }

    private fun checkFavoriteStatus(imdbId: String) {
        viewModelScope.launch {
            localRepository.isFavorite(imdbId).collect { isFav ->
                _uiState.value = _uiState.value.copy(isFavorite = isFav)
            }
        }
    }

    private fun loadHistory(imdbId: String) {
        viewModelScope.launch {
            val history = localRepository.getHistoryForMovie(imdbId)
            _uiState.value = _uiState.value.copy(watchHistory = history)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val state   = _uiState.value
            val imdbId  = currentImdbId
            if (state.isFavorite) {
                localRepository.removeFavorite(imdbId)
            } else {
                val title     = state.movieDetails?.movie?.title ?: state.tvDetails?.tvShow?.name ?: ""
                val poster    = state.movieDetails?.movie?.posterPath ?: state.tvDetails?.tvShow?.posterPath
                val rating    = state.movieDetails?.movie?.voteAverage ?: state.tvDetails?.tvShow?.voteAverage ?: 0.0
                val mediaType = if (state.tvDetails != null) "tv" else "movie"
                localRepository.addFavorite(
                    Favorite(
                        id         = "${mediaType}_${imdbId}",
                        movieId    = imdbId,
                        title      = title,
                        posterPath = poster,
                        mediaType  = mediaType,
                        rating     = rating
                    )
                )
            }
        }
    }
}
