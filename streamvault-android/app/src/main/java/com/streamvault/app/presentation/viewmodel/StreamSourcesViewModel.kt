package com.streamvault.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.domain.model.Stream
import com.streamvault.app.domain.model.StreamResult
import com.streamvault.app.domain.repository.StreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StreamSourcesUiState(
    val streams: List<Stream> = emptyList(),
    val loadingAddons: Set<String> = emptySet(),
    val errorAddons: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val selectedQuality: String? = null
)

@HiltViewModel
class StreamSourcesViewModel @Inject constructor(
    private val streamRepository: StreamRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreamSourcesUiState())
    val uiState: StateFlow<StreamSourcesUiState> = _uiState.asStateFlow()

    fun loadMovieStreams(imdbId: String) {
        viewModelScope.launch {
            _uiState.value = StreamSourcesUiState(isLoading = true)
            val allStreams = mutableListOf<Stream>()
            streamRepository.getMovieStreams(imdbId).collect { result ->
                when (result) {
                    is StreamResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            loadingAddons = _uiState.value.loadingAddons + result.addonId
                        )
                    }
                    is StreamResult.Success -> {
                        allStreams.addAll(result.streams)
                        val sorted = allStreams.sortedWith(
                            compareByDescending<Stream> { qualityOrder(it.quality) }
                                .thenByDescending { it.seeds ?: 0 }
                        )
                        _uiState.value = _uiState.value.copy(
                            streams = sorted,
                            isLoading = _uiState.value.loadingAddons.isNotEmpty()
                        )
                    }
                    is StreamResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            loadingAddons = _uiState.value.loadingAddons - result.addonId,
                            errorAddons = _uiState.value.errorAddons + (result.addonId to result.message)
                        )
                    }
                }
            }
            _uiState.value = _uiState.value.copy(isLoading = false, loadingAddons = emptySet())
        }
    }

    fun loadTvStreams(imdbId: String, season: Int, episode: Int) {
        viewModelScope.launch {
            _uiState.value = StreamSourcesUiState(isLoading = true)
            val allStreams = mutableListOf<Stream>()
            streamRepository.getTvStreams(imdbId, season, episode).collect { result ->
                when (result) {
                    is StreamResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            loadingAddons = _uiState.value.loadingAddons + result.addonId
                        )
                    }
                    is StreamResult.Success -> {
                        allStreams.addAll(result.streams)
                        val sorted = allStreams.sortedWith(
                            compareByDescending<Stream> { qualityOrder(it.quality) }
                                .thenByDescending { it.seeds ?: 0 }
                        )
                        _uiState.value = _uiState.value.copy(
                            streams = sorted,
                            isLoading = _uiState.value.loadingAddons.isNotEmpty()
                        )
                    }
                    is StreamResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            loadingAddons = _uiState.value.loadingAddons - result.addonId,
                            errorAddons = _uiState.value.errorAddons + (result.addonId to result.message)
                        )
                    }
                }
            }
            _uiState.value = _uiState.value.copy(isLoading = false, loadingAddons = emptySet())
        }
    }

    private fun qualityOrder(quality: String): Int = when (quality) {
        "4K" -> 4
        "1080p" -> 3
        "720p" -> 2
        "480p" -> 1
        else -> 0
    }

    fun filterByQuality(quality: String?) {
        _uiState.value = _uiState.value.copy(selectedQuality = quality)
    }
}
