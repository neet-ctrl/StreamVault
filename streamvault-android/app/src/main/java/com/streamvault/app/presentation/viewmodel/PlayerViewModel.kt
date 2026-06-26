package com.streamvault.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.domain.model.*
import com.streamvault.app.domain.repository.LocalRepository
import com.streamvault.app.engine.SubtitleManager
import com.streamvault.app.engine.TorrentEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
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
    val sleepTimerRemainingMs: Long = 0L,
    val error: String? = null,
    // Subtitle state
    val subtitles: List<Subtitle> = emptyList(),
    val activeSubtitleIndex: Int = -1,
    val subtitleSearchResults: List<SubtitleSearchResult> = emptyList(),
    val isSubtitlePanelOpen: Boolean = false,
    val subtitleDelayMs: Long = 0L,
    val subtitlePrefs: SubtitlePreference = SubtitlePreference(),
    // Audio tracks
    val audioTracks: List<AudioTrack> = emptyList(),
    val activeAudioTrackId: Int = -1,
    val isAudioPanelOpen: Boolean = false,
    // Torrent status
    val torrentStatus: TorrentStatus? = null,
    val streamUrl: String? = null,
    val isStreamReady: Boolean = false,
    // Reconnect / health
    val reconnectAttempt: Int = 0,
    val streamHealth: StreamHealth = StreamHealth.GOOD,
    // Next episode
    val showNextEpisode: Boolean = false,
    val nextEpisodeCountdown: Int = 0,
    // Skip intro
    val showSkipIntro: Boolean = false,
    val introEndMs: Long = 0L,
    // PiP
    val isPipMode: Boolean = false,
    // Quality switch
    val availableQualities: List<String> = emptyList(),
    val isQualityPanelOpen: Boolean = false
)

enum class StreamHealth { GOOD, POOR, RECONNECTING, FAILED }

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val localRepository: LocalRepository,
    private val torrentEngine: TorrentEngine,
    private val subtitleManager: SubtitleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var controlsHideJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var nextEpisodeJob: Job? = null
    private var progressSaveJob: Job? = null
    private var healthMonitorJob: Job? = null

    fun setStream(stream: Stream, movieId: Int, title: String) {
        _uiState.value = _uiState.value.copy(
            stream = stream,
            movieId = movieId,
            title = title,
            subtitles = stream.subtitles
        )
        loadSavedPosition(movieId, stream)
        loadSubtitlePreferences()
        startHealthMonitor()
    }

    private fun loadSavedPosition(movieId: Int, stream: Stream) {
        viewModelScope.launch {
            val pos = localRepository.getPlaybackPositionByMovieId(movieId)
            if (pos != null && pos.isStarted && !pos.isFinished) {
                _uiState.value = _uiState.value.copy(currentPositionMs = pos.positionMs)
                Timber.d("PlayerViewModel: Resuming from ${pos.positionMs}ms")
            }
        }
    }

    private fun loadSubtitlePreferences() {
        viewModelScope.launch {
            val prefs = localRepository.getSubtitlePreferences()
            _uiState.value = _uiState.value.copy(
                subtitlePrefs = prefs,
                subtitleDelayMs = prefs.delayMs
            )
        }
    }

    fun prepareTorrentStream(stream: Stream) {
        if (!stream.isTorrent || stream.infoHash == null) {
            _uiState.value = _uiState.value.copy(streamUrl = stream.url, isStreamReady = true)
            return
        }
        viewModelScope.launch {
            val magnetUrl = if (stream.url.startsWith("magnet:")) stream.url
            else com.streamvault.app.util.MagnetParser.buildMagnet(stream.infoHash)

            val localUrl = torrentEngine.startStream(
                magnetUrl = magnetUrl,
                fileIdx = stream.fileIdx ?: 0,
                onProgress = { status ->
                    _uiState.value = _uiState.value.copy(torrentStatus = status)
                }
            )
            _uiState.value = _uiState.value.copy(
                streamUrl = localUrl ?: stream.url,
                isStreamReady = true,
                torrentStatus = torrentEngine.getStatusFor(stream.infoHash)
            )
        }
    }

    // ─────────────── Playback Controls ───────────────

    fun onPlayPause(isPlaying: Boolean) {
        _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        if (isPlaying) scheduleHideControls()
    }

    fun onPositionChanged(positionMs: Long) {
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
        checkSkipIntro(positionMs)
        checkNextEpisode(positionMs)
        scheduleProgressSave(positionMs)
    }

    fun onDurationChanged(durationMs: Long) {
        _uiState.value = _uiState.value.copy(durationMs = durationMs)
    }

    fun onBufferingChanged(isBuffering: Boolean) {
        _uiState.value = _uiState.value.copy(isBuffering = isBuffering)
        if (isBuffering) checkStreamHealth()
    }

    fun onError(message: String) {
        _uiState.value = _uiState.value.copy(error = message, streamHealth = StreamHealth.FAILED)
        attemptReconnect()
    }

    fun toggleControls() {
        if (_uiState.value.isLocked) return
        val visible = !_uiState.value.isControlsVisible
        _uiState.value = _uiState.value.copy(isControlsVisible = visible)
        if (visible) scheduleHideControls()
    }

    fun showControls() {
        _uiState.value = _uiState.value.copy(isControlsVisible = true)
        scheduleHideControls()
    }

    fun toggleLock() {
        val locked = !_uiState.value.isLocked
        _uiState.value = _uiState.value.copy(isLocked = locked, isControlsVisible = true)
        if (!locked) scheduleHideControls()
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun setVolume(volume: Float) {
        _uiState.value = _uiState.value.copy(volume = volume.coerceIn(0f, 1f), isMuted = volume <= 0f)
    }

    fun setBrightness(brightness: Float) {
        _uiState.value = _uiState.value.copy(brightness = brightness.coerceIn(0f, 1f))
    }

    fun toggleMute() {
        val muted = !_uiState.value.isMuted
        _uiState.value = _uiState.value.copy(isMuted = muted)
    }

    // ─────────────── Sleep Timer ───────────────

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        if (minutes == null) {
            _uiState.value = _uiState.value.copy(sleepTimerMinutes = null, sleepTimerRemainingMs = 0)
            return
        }
        val endMs = System.currentTimeMillis() + minutes * 60_000L
        _uiState.value = _uiState.value.copy(sleepTimerMinutes = minutes, sleepTimerRemainingMs = minutes * 60_000L)
        sleepTimerJob = viewModelScope.launch {
            while (true) {
                val remaining = endMs - System.currentTimeMillis()
                if (remaining <= 0) {
                    _uiState.value = _uiState.value.copy(isPlaying = false, sleepTimerMinutes = null, sleepTimerRemainingMs = 0)
                    break
                }
                _uiState.value = _uiState.value.copy(sleepTimerRemainingMs = remaining)
                delay(1000)
            }
        }
    }

    // ─────────────── Subtitles ───────────────

    fun toggleSubtitlePanel() {
        _uiState.value = _uiState.value.copy(
            isSubtitlePanelOpen = !_uiState.value.isSubtitlePanelOpen,
            isAudioPanelOpen = false
        )
    }

    fun selectSubtitle(index: Int) {
        _uiState.value = _uiState.value.copy(activeSubtitleIndex = index, isSubtitlePanelOpen = false)
    }

    fun disableSubtitles() {
        _uiState.value = _uiState.value.copy(activeSubtitleIndex = -1, isSubtitlePanelOpen = false)
    }

    fun setSubtitleDelay(delayMs: Long) {
        _uiState.value = _uiState.value.copy(subtitleDelayMs = delayMs)
        viewModelScope.launch { localRepository.updateSubtitleDelay(delayMs) }
    }

    fun searchSubtitles(imdbId: String, langCode: String) {
        viewModelScope.launch {
            val results = subtitleManager.searchByImdbId(imdbId, langCode)
            _uiState.value = _uiState.value.copy(subtitleSearchResults = results)
        }
    }

    fun downloadAndSelectSubtitle(result: SubtitleSearchResult) {
        viewModelScope.launch {
            val path = subtitleManager.downloadSubtitle(result.url, result.id) ?: return@launch
            val sub = Subtitle(
                id = result.id,
                lang = result.lang,
                langCode = result.langCode,
                url = path,
                isExternal = true
            )
            val subs = _uiState.value.subtitles.toMutableList()
            subs.add(sub)
            _uiState.value = _uiState.value.copy(
                subtitles = subs,
                activeSubtitleIndex = subs.size - 1,
                isSubtitlePanelOpen = false
            )
        }
    }

    // ─────────────── Audio ───────────────

    fun setAudioTracks(tracks: List<AudioTrack>) {
        _uiState.value = _uiState.value.copy(audioTracks = tracks)
    }

    fun selectAudioTrack(trackId: Int) {
        _uiState.value = _uiState.value.copy(activeAudioTrackId = trackId, isAudioPanelOpen = false)
    }

    fun toggleAudioPanel() {
        _uiState.value = _uiState.value.copy(
            isAudioPanelOpen = !_uiState.value.isAudioPanelOpen,
            isSubtitlePanelOpen = false
        )
    }

    // ─────────────── PiP ───────────────

    fun onPipModeChanged(inPip: Boolean) {
        _uiState.value = _uiState.value.copy(
            isPipMode = inPip,
            isControlsVisible = if (inPip) false else _uiState.value.isControlsVisible
        )
    }

    // ─────────────── Quality ───────────────

    fun toggleQualityPanel() {
        _uiState.value = _uiState.value.copy(isQualityPanelOpen = !_uiState.value.isQualityPanelOpen)
    }

    // ─────────────── Health & Reconnect ───────────────

    private fun startHealthMonitor() {
        healthMonitorJob?.cancel()
        healthMonitorJob = viewModelScope.launch {
            var bufferingSeconds = 0
            while (true) {
                delay(1000)
                if (_uiState.value.isBuffering) {
                    bufferingSeconds++
                    val health = when {
                        bufferingSeconds > 30 -> StreamHealth.FAILED
                        bufferingSeconds > 10 -> StreamHealth.POOR
                        bufferingSeconds > 3 -> StreamHealth.RECONNECTING
                        else -> StreamHealth.GOOD
                    }
                    _uiState.value = _uiState.value.copy(streamHealth = health)
                } else {
                    bufferingSeconds = 0
                    if (_uiState.value.streamHealth != StreamHealth.GOOD) {
                        _uiState.value = _uiState.value.copy(streamHealth = StreamHealth.GOOD)
                    }
                }
            }
        }
    }

    private fun checkStreamHealth() {
        if (_uiState.value.streamHealth == StreamHealth.FAILED) {
            attemptReconnect()
        }
    }

    private fun attemptReconnect() {
        val attempt = _uiState.value.reconnectAttempt
        if (attempt >= 3) {
            _uiState.value = _uiState.value.copy(streamHealth = StreamHealth.FAILED)
            return
        }
        viewModelScope.launch {
            delay(2000L * (attempt + 1))
            _uiState.value = _uiState.value.copy(
                reconnectAttempt = attempt + 1,
                streamHealth = StreamHealth.RECONNECTING,
                error = null
            )
        }
    }

    // ─────────────── Skip Intro / Next Episode ───────────────

    private fun checkSkipIntro(positionMs: Long) {
        // Show skip intro if within first 5 minutes and there's a known intro end
        val introEnd = _uiState.value.introEndMs
        if (introEnd > 0 && positionMs in 1000..introEnd) {
            if (!_uiState.value.showSkipIntro) {
                _uiState.value = _uiState.value.copy(showSkipIntro = true)
            }
        } else if (_uiState.value.showSkipIntro) {
            _uiState.value = _uiState.value.copy(showSkipIntro = false)
        }
    }

    fun skipIntro() {
        val introEnd = _uiState.value.introEndMs
        if (introEnd > 0) {
            _uiState.value = _uiState.value.copy(showSkipIntro = false)
            // ExoPlayer seekTo handled by screen
        }
    }

    private fun checkNextEpisode(positionMs: Long) {
        val duration = _uiState.value.durationMs
        if (duration > 0 && (duration - positionMs) < 30_000L && duration > 60_000L) {
            if (!_uiState.value.showNextEpisode) {
                startNextEpisodeCountdown()
            }
        }
    }

    private fun startNextEpisodeCountdown() {
        if (_uiState.value.stream == null) return
        nextEpisodeJob?.cancel()
        _uiState.value = _uiState.value.copy(showNextEpisode = true, nextEpisodeCountdown = 10)
        nextEpisodeJob = viewModelScope.launch {
            for (i in 9 downTo 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(nextEpisodeCountdown = i)
            }
        }
    }

    fun cancelNextEpisode() {
        nextEpisodeJob?.cancel()
        _uiState.value = _uiState.value.copy(showNextEpisode = false)
    }

    // ─────────────── Progress Saving ───────────────

    private fun scheduleProgressSave(positionMs: Long) {
        progressSaveJob?.cancel()
        progressSaveJob = viewModelScope.launch {
            delay(5000)
            savePlaybackPosition(positionMs)
        }
    }

    private fun savePlaybackPosition(positionMs: Long) {
        val state = _uiState.value
        if (state.movieId <= 0 || state.durationMs <= 0) return
        viewModelScope.launch {
            localRepository.savePlaybackPosition(
                PlaybackPosition(
                    contentId = "movie_${state.movieId}",
                    movieId = state.movieId,
                    title = state.title,
                    posterPath = null,
                    mediaType = "movie",
                    positionMs = positionMs,
                    durationMs = state.durationMs
                )
            )
        }
    }

    fun saveToHistory() {
        val state = _uiState.value
        if (state.movieId <= 0) return
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
            savePlaybackPosition(state.currentPositionMs)
        }
    }

    private fun scheduleHideControls() {
        controlsHideJob?.cancel()
        controlsHideJob = viewModelScope.launch {
            delay(4000)
            if (_uiState.value.isPlaying) {
                _uiState.value = _uiState.value.copy(isControlsVisible = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveToHistory()
        healthMonitorJob?.cancel()
        sleepTimerJob?.cancel()
        nextEpisodeJob?.cancel()
        progressSaveJob?.cancel()
    }
}
