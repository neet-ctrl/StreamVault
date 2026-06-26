package com.streamvault.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.domain.model.Download
import com.streamvault.app.domain.model.Favorite
import com.streamvault.app.domain.model.WatchHistory
import com.streamvault.app.domain.repository.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val favorites: List<Favorite> = emptyList(),
    val history: List<WatchHistory> = emptyList(),
    val downloads: List<Download> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val localRepository: LocalRepository
) : ViewModel() {

    val uiState: StateFlow<LibraryUiState> = combine(
        localRepository.getFavorites(),
        localRepository.getWatchHistory(),
        localRepository.getDownloads()
    ) { favs, history, downloads ->
        LibraryUiState(
            favorites = favs,
            history = history,
            downloads = downloads,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LibraryUiState()
    )

    fun removeFavorite(movieId: Int) {
        viewModelScope.launch { localRepository.removeFavorite(movieId) }
    }

    fun removeFromHistory(movieId: Int) {
        viewModelScope.launch { localRepository.removeFromHistory(movieId) }
    }

    fun clearHistory() {
        viewModelScope.launch { localRepository.clearHistory() }
    }
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val localRepository: LocalRepository
) : ViewModel() {

    val favorites: StateFlow<List<Favorite>> = localRepository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFavorite(movieId: Int) {
        viewModelScope.launch { localRepository.removeFavorite(movieId) }
    }
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val localRepository: LocalRepository
) : ViewModel() {

    val history: StateFlow<List<WatchHistory>> = localRepository.getWatchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeItem(movieId: Int) {
        viewModelScope.launch { localRepository.removeFromHistory(movieId) }
    }

    fun clearAll() {
        viewModelScope.launch { localRepository.clearHistory() }
    }
}

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val localRepository: LocalRepository
) : ViewModel() {

    val downloads: StateFlow<List<Download>> = localRepository.getDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteDownload(id: String) {
        viewModelScope.launch { localRepository.deleteDownload(id) }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val streamRepository: com.streamvault.app.domain.repository.StreamRepository
) : ViewModel() {

    val addons = streamRepository.getAllAddons()

    fun toggleAddon(addonId: String, enabled: Boolean) {
        viewModelScope.launch { streamRepository.toggleAddon(addonId, enabled) }
    }
}
