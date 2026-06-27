package com.streamvault.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.domain.model.Movie
import com.streamvault.app.domain.model.MediaType
import com.streamvault.app.domain.repository.CinemetaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val cinemetaRepository: CinemetaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(400)
                search(query)
            }
        } else if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(results = emptyList(), hasSearched = false)
        }
    }

    fun search(query: String = _uiState.value.query) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, hasSearched = true)

            // Search movies and series in parallel, merge and de-duplicate by id
            val moviesDeferred = async { cinemetaRepository.searchMovies(query) }
            val seriesDeferred = async { cinemetaRepository.searchSeries(query) }

            val movies = moviesDeferred.await().getOrDefault(emptyList())
            val series = seriesDeferred.await().getOrDefault(emptyList())
                .map { it.toMovie() }   // convert TvShow → Movie for unified list

            val combined = (movies + series)
                .distinctBy { it.id }
                .sortedByDescending { it.voteAverage }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                results   = combined
            )
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.value = SearchUiState()
    }
}

// Thin adapter so TvShow can appear alongside Movie in the unified search list
private fun com.streamvault.app.domain.model.TvShow.toMovie() = Movie(
    id           = id,
    title        = name,
    overview     = overview,
    posterPath   = posterPath,
    backdropPath = backdropPath,
    releaseDate  = firstAirDate,
    voteAverage  = voteAverage,
    voteCount    = voteCount,
    genres       = genres,
    imdbId       = imdbId,
    mediaType    = MediaType.TV
)
