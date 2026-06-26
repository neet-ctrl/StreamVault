package com.streamvault.app.util

object Constants {
    // TMDB
    const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    const val TMDB_IMAGE_W500 = "https://image.tmdb.org/t/p/w500"
    const val TMDB_IMAGE_W780 = "https://image.tmdb.org/t/p/w780"
    const val TMDB_IMAGE_ORIGINAL = "https://image.tmdb.org/t/p/original"
    const val TMDB_IMAGE_W185 = "https://image.tmdb.org/t/p/w185"

    // Stremio Addons
    const val TORRENTIO_URL = "https://torrentio.strem.fun"
    const val MEDIAFUSION_URL = "https://mediafusion.elfhosted.com"
    const val KNIGHTCRAWLER_URL = "https://knightcrawler.elfhosted.com"
    const val JACKETTIO_URL = "https://jackettio.elfhosted.com"
    const val COMET_URL = "https://comet.elfhosted.com"
    const val CINEMETA_URL = "https://v3-cinemeta.strem.io"

    // Addon config slugs
    const val TORRENTIO_CONFIG = "sort=qualitysize|qualityfilter=480p,scr,cam"
    const val MEDIAFUSION_CONFIG = ""
    const val KNIGHTCRAWLER_CONFIG = ""
    const val JACKETTIO_CONFIG = ""
    const val COMET_CONFIG = ""

    // Room Database
    const val DATABASE_NAME = "streamvault.db"
    const val DATABASE_VERSION = 1

    // Download
    const val DOWNLOAD_DIR = "Movies/StreamVault"
    const val NOTIFICATION_CHANNEL_ID = "streamvault_downloads"
    const val NOTIFICATION_CHANNEL_NAME = "Downloads"

    // Worker tags
    const val DOWNLOAD_WORK_TAG = "download_work"

    // Preferences keys
    const val PREF_PREFERRED_QUALITY = "preferred_quality"
    const val PREF_PREFERRED_AUDIO_LANG = "preferred_audio_lang"
    const val PREF_PREFERRED_SUB_LANG = "preferred_sub_lang"
    const val PREF_AUTO_PLAY_NEXT = "auto_play_next"
    const val PREF_SUBTITLE_SIZE = "subtitle_size"
    const val PREF_DOWNLOAD_PATH = "download_path"
    const val PREF_ACTIVE_ADDONS = "active_addons"

    // Default priorities
    val ENGLISH_ADDON_PRIORITY = listOf("torrentio", "knightcrawler", "jackettio", "comet")
    val INDIAN_ADDON_PRIORITY = listOf("mediafusion", "torrentio", "jackettio", "knightcrawler")
    val ANIME_ADDON_PRIORITY = listOf("knightcrawler", "torrentio", "jackettio")

    // Paging
    const val PAGE_SIZE = 20
    const val PREFETCH_DISTANCE = 5

    // Player
    const val SKIP_FORWARD_MS = 10_000L
    const val SKIP_BACK_MS = 10_000L
    const val HIDE_CONTROLS_DELAY_MS = 3_000L
}
