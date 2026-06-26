package com.streamvault.app.domain.model

object StreamRanker {

    private val QUALITY_SCORES = mapOf(
        "4K" to 100,
        "1080p" to 80,
        "720p" to 60,
        "480p" to 40,
        "HD" to 55,
        "SD" to 30
    )

    private val ADDON_RELIABILITY = mapOf(
        "torrentio" to 10,
        "knightcrawler" to 9,
        "mediafusion" to 9,
        "comet" to 8,
        "jackettio" to 7,
        "cinemeta" to 5
    )

    fun rank(streams: List<Stream>, preferredLanguage: Language = Language.ENGLISH): List<Stream> {
        val deduped = deduplicateByHash(streams)
        return deduped.sortedByDescending { score(it, preferredLanguage) }
    }

    fun score(stream: Stream, preferredLanguage: Language = Language.ENGLISH): Double {
        var score = 0.0

        // Quality score (0-100)
        score += (QUALITY_SCORES[stream.quality] ?: 50).toDouble()

        // Seed score — logarithmic scale (0-50)
        val seeds = stream.seeds ?: 0
        if (seeds > 0) {
            score += (Math.log10((seeds + 1).toDouble()) * 15).coerceAtMost(50.0)
        }

        // Size score — prefer larger files for quality (0-20)
        val sizeGb = (stream.size ?: 0L).toDouble() / (1024 * 1024 * 1024)
        score += when {
            sizeGb >= 20 -> 20.0  // 4K BDRip
            sizeGb >= 8 -> 18.0   // 1080p BluRay
            sizeGb >= 4 -> 15.0   // 1080p WEB-DL
            sizeGb >= 1.5 -> 12.0 // 720p
            sizeGb >= 0.7 -> 8.0  // 480p
            sizeGb > 0 -> 5.0
            else -> 0.0
        }

        // Addon reliability bonus (0-10)
        score += ADDON_RELIABILITY[stream.addonId] ?: 5

        // Language boost (0-25)
        val lang = detectLanguage(stream)
        score += when {
            lang == preferredLanguage -> 25.0
            lang == Language.MULTI -> 15.0
            lang == Language.UNKNOWN -> 5.0
            else -> 0.0
        }

        // Torrent health bonus
        if (stream.isTorrent && seeds >= 10) score += 5.0
        if (stream.isTorrent && seeds >= 50) score += 5.0
        if (stream.isTorrent && seeds >= 200) score += 5.0

        // Direct stream slight penalty (may not always work)
        if (!stream.isTorrent && stream.url.isNotEmpty()) score += 10.0

        return score
    }

    fun deduplicateByHash(streams: List<Stream>): List<Stream> {
        val seen = mutableSetOf<String>()
        return streams.filter { stream ->
            val key = stream.infoHash?.uppercase()
                ?: "${stream.url}_${stream.quality}"
            seen.add(key)
        }
    }

    fun filterByQuality(streams: List<Stream>, quality: String?): List<Stream> {
        if (quality == null) return streams
        return streams.filter { it.quality == quality }
    }

    fun filterByLanguage(streams: List<Stream>, language: Language): List<Stream> {
        if (language == Language.ALL) return streams
        return streams.filter {
            val lang = detectLanguage(it)
            lang == language || lang == Language.MULTI || lang == Language.UNKNOWN
        }
    }

    fun detectLanguage(stream: Stream): Language {
        val text = stream.title.uppercase()
        return when {
            text.contains("HINDI") || text.contains("HIN") -> Language.HINDI
            text.contains("TAMIL") || text.contains("TAM") -> Language.TAMIL
            text.contains("TELUGU") || text.contains("TEL") -> Language.TELUGU
            text.contains("MALAYALAM") || text.contains("MAL") -> Language.MALAYALAM
            text.contains("KANNADA") || text.contains("KAN") -> Language.KANNADA
            text.contains("MULTI") || text.contains("DUAL") || text.contains("TRI") -> Language.MULTI
            text.contains("JAPANESE") || text.contains("JPN") -> Language.JAPANESE
            text.contains("KOREAN") || text.contains("KOR") -> Language.KOREAN
            text.contains("SPANISH") || text.contains("SPA") -> Language.SPANISH
            text.contains("FRENCH") || text.contains("FRE") -> Language.FRENCH
            else -> Language.UNKNOWN
        }
    }

    fun sortBySeeders(streams: List<Stream>): List<Stream> =
        streams.sortedByDescending { it.seeds ?: -1 }

    fun sortByQuality(streams: List<Stream>): List<Stream> =
        streams.sortedByDescending { QUALITY_SCORES[it.quality] ?: 0 }

    fun sortBySize(streams: List<Stream>): List<Stream> =
        streams.sortedByDescending { it.size ?: 0L }

    fun getPriorityAddons(language: Language): List<String> = when (language) {
        Language.HINDI, Language.TAMIL, Language.TELUGU,
        Language.MALAYALAM, Language.KANNADA ->
            listOf("mediafusion", "torrentio", "jackettio", "knightcrawler")
        Language.JAPANESE, Language.KOREAN ->
            listOf("knightcrawler", "torrentio", "jackettio")
        else ->
            listOf("torrentio", "knightcrawler", "jackettio", "comet")
    }
}

enum class Language {
    ENGLISH, HINDI, TAMIL, TELUGU, MALAYALAM, KANNADA,
    JAPANESE, KOREAN, SPANISH, FRENCH, MULTI, UNKNOWN, ALL
}
