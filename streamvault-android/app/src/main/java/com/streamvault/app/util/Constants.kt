package com.streamvault.app.util

object Constants {

    // Cinemeta (no API key required — free public Stremio addon)
    const val CINEMETA_URL = "https://v3-cinemeta.strem.io"

    // Stremio Addons
    const val TORRENTIO_URL    = "https://torrentio.strem.fun"
    const val MEDIAFUSION_URL  = "https://mediafusion.elfhosted.com"
    const val KNIGHTCRAWLER_URL = "https://knightcrawler.elfhosted.com"
    const val JACKETTIO_URL    = "https://jackettio.elfhosted.com"
    const val COMET_URL        = "https://comet.elfhosted.com"

    // Addon config slugs
    const val TORRENTIO_CONFIG    = "sort=qualitysize|qualityfilter=480p,scr,cam"
    const val MEDIAFUSION_CONFIG  = ""
    const val KNIGHTCRAWLER_CONFIG = ""
    const val JACKETTIO_CONFIG    = ""
    const val COMET_CONFIG        = ""

    // Room Database
    const val DATABASE_NAME    = "streamvault.db"
    const val DATABASE_VERSION = 3   // bumped: movieId Int → String

    // Download
    const val DOWNLOAD_DIR              = "Movies/StreamVault"
    const val NOTIFICATION_CHANNEL_ID   = "streamvault_downloads"
    const val NOTIFICATION_CHANNEL_NAME = "Downloads"

    // Worker tags
    const val DOWNLOAD_WORK_TAG = "download_work"

    // Preferences keys
    const val PREF_PREFERRED_QUALITY    = "preferred_quality"
    const val PREF_PREFERRED_AUDIO_LANG = "preferred_audio_lang"
    const val PREF_PREFERRED_SUB_LANG   = "preferred_sub_lang"
    const val PREF_AUTO_PLAY_NEXT       = "auto_play_next"
    const val PREF_SUBTITLE_SIZE        = "subtitle_size"
    const val PREF_DOWNLOAD_PATH        = "download_path"
    const val PREF_ACTIVE_ADDONS        = "active_addons"
    const val PREF_HARDWARE_ACCEL       = "hardware_acceleration"
    const val PREF_EXTERNAL_PLAYER      = "external_player"
    const val PREF_PRE_BUFFER_MB        = "pre_buffer_size_mb"
    const val PREF_MAX_CONNECTIONS      = "max_connections"
    const val PREF_TORRENT_TIMEOUT      = "torrent_timeout_seconds"

    // Default addon priority per language/genre
    val ENGLISH_ADDON_PRIORITY = listOf("torrentio", "knightcrawler", "jackettio", "comet")
    val INDIAN_ADDON_PRIORITY  = listOf("mediafusion", "torrentio", "jackettio", "knightcrawler")
    val ANIME_ADDON_PRIORITY   = listOf("knightcrawler", "torrentio", "jackettio")

    // Paging
    const val PAGE_SIZE        = 20
    const val PREFETCH_DISTANCE = 5

    // Player
    const val SKIP_FORWARD_MS        = 10_000L
    const val SKIP_BACK_MS           = 10_000L
    const val HIDE_CONTROLS_DELAY_MS = 4_000L
    const val DOUBLE_TAP_SEEK_MS     = 10_000L
    const val PROGRESS_SAVE_INTERVAL_MS = 5_000L

    // Stream retry
    const val STREAM_RETRY_COUNT    = 3
    const val STREAM_RETRY_DELAY_MS = 1_000L

    // Torrent
    const val DEFAULT_PRE_BUFFER_MB    = 10
    const val DEFAULT_MAX_CONNECTIONS  = 200
    const val DEFAULT_TORRENT_TIMEOUT_S = 30
    const val MAX_TORRENT_CONNECTIONS  = 500

    // Cache expiry
    const val CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000L  // 24 hours

    // Quality options
    val QUALITY_OPTIONS = listOf("4K", "1080p", "720p", "480p", "Best Available")

    // Playback speed options
    val SPEED_OPTIONS = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)
}
