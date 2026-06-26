package com.streamvault.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.domain.model.StreamAddon
import com.streamvault.app.domain.model.SubtitlePreference
import com.streamvault.app.domain.model.UserSettings
import com.streamvault.app.domain.repository.LocalRepository
import com.streamvault.app.domain.repository.StreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val subtitlePrefs: SubtitlePreference = SubtitlePreference(),
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val streamRepository: StreamRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val addons: List<StreamAddon> get() = streamRepository.getAllAddons()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val subtitlePrefs = localRepository.getSubtitlePreferences()
            _uiState.value = _uiState.value.copy(subtitlePrefs = subtitlePrefs)
        }
    }

    fun toggleAddon(addonId: String, enabled: Boolean) {
        viewModelScope.launch {
            streamRepository.toggleAddon(addonId, enabled)
        }
    }

    fun setPreferredQuality(quality: String) {
        _uiState.value = _uiState.value.copy(
            settings = _uiState.value.settings.copy(preferredQuality = quality)
        )
    }

    fun setAutoPlayNext(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            settings = _uiState.value.settings.copy(autoPlayNext = enabled)
        )
    }

    fun setHardwareAcceleration(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            settings = _uiState.value.settings.copy(hardwareAcceleration = enabled)
        )
    }

    fun setUseExternalPlayer(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            settings = _uiState.value.settings.copy(useExternalPlayer = enabled)
        )
    }

    fun setPreBufferSize(mb: Int) {
        _uiState.value = _uiState.value.copy(
            settings = _uiState.value.settings.copy(preBufferSizeMb = mb)
        )
    }

    fun setMaxConnections(max: Int) {
        _uiState.value = _uiState.value.copy(
            settings = _uiState.value.settings.copy(maxConnections = max)
        )
    }

    fun setTorrentTimeout(seconds: Int) {
        _uiState.value = _uiState.value.copy(
            settings = _uiState.value.settings.copy(torrentTimeoutSeconds = seconds)
        )
    }

    fun setSubtitleLanguage(lang: String, code: String) {
        val prefs = _uiState.value.subtitlePrefs.copy(language = lang, languageCode = code)
        _uiState.value = _uiState.value.copy(subtitlePrefs = prefs)
        viewModelScope.launch { localRepository.saveSubtitlePreferences(prefs) }
    }

    fun setSubtitleSize(scale: Float) {
        val prefs = _uiState.value.subtitlePrefs.copy(sizeScale = scale)
        _uiState.value = _uiState.value.copy(subtitlePrefs = prefs)
        viewModelScope.launch { localRepository.saveSubtitlePreferences(prefs) }
    }

    fun setSubtitleBold(enabled: Boolean) {
        val prefs = _uiState.value.subtitlePrefs.copy(boldEnabled = enabled)
        _uiState.value = _uiState.value.copy(subtitlePrefs = prefs)
        viewModelScope.launch { localRepository.saveSubtitlePreferences(prefs) }
    }

    fun setSubtitleOutline(enabled: Boolean) {
        val prefs = _uiState.value.subtitlePrefs.copy(outlineEnabled = enabled)
        _uiState.value = _uiState.value.copy(subtitlePrefs = prefs)
        viewModelScope.launch { localRepository.saveSubtitlePreferences(prefs) }
    }

    fun clearHistory() {
        viewModelScope.launch { localRepository.clearHistory() }
    }

    fun clearSubtitleCache() {
        // Cache clearing done through SubtitleManager
    }
}
