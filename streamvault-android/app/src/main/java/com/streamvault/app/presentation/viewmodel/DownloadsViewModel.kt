package com.streamvault.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.streamvault.app.domain.model.Download
import com.streamvault.app.domain.model.DownloadStatus
import com.streamvault.app.domain.model.Stream
import com.streamvault.app.domain.repository.LocalRepository
import com.streamvault.app.engine.TorrentEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val localRepository: LocalRepository,
    private val torrentEngine: TorrentEngine
) : ViewModel() {

    val downloads: StateFlow<List<Download>> = localRepository.getDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val torrentStatus = torrentEngine.status

    fun startDownload(stream: Stream, movieId: Int, title: String, posterPath: String?) {
        viewModelScope.launch {
            try {
                val downloadId = "dl_${movieId}_${System.currentTimeMillis()}"
                val download = Download(
                    id = downloadId,
                    movieId = movieId,
                    title = title,
                    posterPath = posterPath,
                    quality = stream.quality,
                    filePath = "",
                    totalSize = stream.size ?: 0L,
                    downloadedSize = 0L,
                    status = DownloadStatus.QUEUED,
                    infoHash = stream.infoHash,
                    fileIdx = stream.fileIdx
                )
                localRepository.insertDownload(download)

                if (stream.isTorrent && stream.infoHash != null) {
                    val magnetUrl = if (stream.url.startsWith("magnet:"))
                        stream.url
                    else
                        com.streamvault.app.util.MagnetParser.buildMagnet(stream.infoHash)

                    torrentEngine.startDownload(
                        magnetUrl = magnetUrl,
                        title = title,
                        fileIdx = stream.fileIdx ?: 0,
                        onProgress = { status ->
                            viewModelScope.launch {
                                localRepository.updateDownloadProgress(
                                    downloadId,
                                    status.downloadedSize,
                                    if (status.progress >= 1f) DownloadStatus.COMPLETED else DownloadStatus.DOWNLOADING
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "DownloadsViewModel: Failed to start download")
            }
        }
    }

    fun pauseDownload(download: Download) {
        download.infoHash?.let { torrentEngine.pauseDownload(it) }
        viewModelScope.launch {
            localRepository.updateDownloadProgress(download.id, download.downloadedSize, DownloadStatus.PAUSED)
        }
    }

    fun resumeDownload(download: Download) {
        download.infoHash?.let { torrentEngine.resumeDownload(it) }
        viewModelScope.launch {
            localRepository.updateDownloadProgress(download.id, download.downloadedSize, DownloadStatus.DOWNLOADING)
        }
    }

    fun deleteDownload(id: String) {
        viewModelScope.launch {
            val download = localRepository.getDownload(id)
            download?.infoHash?.let { torrentEngine.stopAndRemove(it) }
            localRepository.deleteDownload(id)
        }
    }

    fun cancelDownload(download: Download) {
        download.infoHash?.let { torrentEngine.stopAndRemove(it) }
        viewModelScope.launch {
            localRepository.updateDownloadProgress(download.id, download.downloadedSize, DownloadStatus.CANCELLED)
        }
    }

    fun getActiveDownloadCount(): Int =
        downloads.value.count { it.status == DownloadStatus.DOWNLOADING }
}
