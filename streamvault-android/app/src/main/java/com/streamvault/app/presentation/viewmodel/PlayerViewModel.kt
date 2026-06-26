package com.streamvault.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.domain.model.Stream
import com.streamvault.app.domain.model.WatchHistory
import com.streamvault.app.domain.repository.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val stream: Stream? = null,
    val movieId: Int = -1,
    val title: String = "",
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val isControlsVisible: Boolean = true,
    val isLocked: Boolean = false,
    val isMuted: Boolean = false,
    val volume: Float = 1.0f,
    val brightness: Float = 0.5f,
    val sleepTimerMinutes: Int? = null,
    val error: String? = null
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var controlsHideJob: kotlinx.coroutines.Job? = null

    fun setStream(stream: Stream, movieId: Int, title: String) {
        _uiState.value = _uiState.value.copy(
            stream = stream,
            movieId = movieId,
            title = title
        )
    }

    fun onPlayPause() {
        _uiState.value = _uiState.value.copy(isPlaying = !_uiState.value.isPlaying)
        scheduleHideControls()
    }

    fun onPositionChanged(positionMs: Long) {
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
        saveProgress(positionMs)
    }

    fun onDurationChanged(durationMs: Long) {
        _uiState.value = _uiState.value.copy(durationMs = durationMs)
    }

    fun onBufferingChanged(isBuffering: Boolean) {
        _uiState.value = _uiState.value.copy(isBuffering = isBuffering)
    }

    fun showControls() {
        _uiState.value = _uiState.value.copy(isControlsVisible = true)
        scheduleHideControls()
    }

    fun toggleControls() {
        if (_uiState.value.isLocked) return
        val visible = !_uiState.value.isControlsVisible
        _uiState.value = _uiState.value.copy(isControlsVisible = visible)
        if (visible) scheduleHideControls()
    }

    fun toggleLock() {
        _uiState.value = _uiState.value.copy(isLocked = !_uiState.value.isLocked)
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun setSleepTimer(minutes: Int?) {
        _uiState.value = _uiState.value.copy(sleepTimerMinutes = minutes)
        if (minutes != null) {
            viewModelScope.launch {
                delay(minutes * 60 * 1000L)
                _uiState.value = _uiState.value.copy(isPlaying = false, sleepTimerMinutes = null)
            }
        }
    }

    private fun scheduleHideControls() {
        controlsHideJob?.cancel()
        controlsHideJob = viewModelScope.launch {
            delay(3000)
            if (_uiState.value.isPlaying) {
                _uiState.value = _uiState.value.copy(isControlsVisible = false)
            }
        }
    }

    private fun saveProgress(positionMs: Long) {
        val state = _uiState.value
        if (state.movieId > 0 && state.durationMs > 0) {
            viewModelScope.launch {
                localRepository.updateProgress(state.movieId, positionMs, state.durationMs)
            }
        }
    }

    fun saveToHistory() {
        val state = _uiState.value
        viewModelScope.launch {
            localRepository.addToHistory(
                WatchHistory(
                    id = "movie_${state.movieId}_${System.currentTimeMillis()}",
                    movieId = state.movieId,
                    title = state.title,
                    posterPath = null,
                    mediaType = "movie",
                    progressMs = state.currentPositionMs,
                    durationMs = state.durationMs
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveToHistory()
    }
}
