package com.streamvault.app.util

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class MagnetInfo(
    val infoHash: String,
    val displayName: String?,
    val trackers: List<String>,
    val fileSize: Long?,
    val magnetUrl: String
)

object MagnetParser {

    fun parse(magnetUrl: String): MagnetInfo? {
        if (!magnetUrl.startsWith("magnet:?")) return null
        return try {
            val params = magnetUrl.removePrefix("magnet:?")
                .split("&")
                .mapNotNull { param ->
                    val idx = param.indexOf('=')
                    if (idx > 0) param.take(idx) to decode(param.drop(idx + 1)) else null
                }
                .groupBy({ it.first }, { it.second })

            val xtValues = params["xt"] ?: emptyList()
            val infoHash = xtValues
                .firstOrNull { it.startsWith("urn:btih:") }
                ?.removePrefix("urn:btih:")
                ?.uppercase()
                ?: return null

            MagnetInfo(
                infoHash = infoHash,
                displayName = params["dn"]?.firstOrNull(),
                trackers = params["tr"] ?: emptyList(),
                fileSize = params["xl"]?.firstOrNull()?.toLongOrNull(),
                magnetUrl = magnetUrl
            )
        } catch (e: Exception) {
            null
        }
    }

    fun buildMagnet(infoHash: String, name: String? = null, trackers: List<String> = defaultTrackers): String {
        val sb = StringBuilder("magnet:?xt=urn:btih:${infoHash.uppercase()}")
        name?.let { sb.append("&dn=${encode(it)}") }
        trackers.forEach { sb.append("&tr=${encode(it)}") }
        return sb.toString()
    }

    fun extractInfoHash(magnetOrHash: String): String? {
        return when {
            magnetOrHash.startsWith("magnet:?") -> parse(magnetOrHash)?.infoHash
            magnetOrHash.length == 40 && magnetOrHash.all { it.isLetterOrDigit() } -> magnetOrHash.uppercase()
            magnetOrHash.length == 32 && magnetOrHash.all { it.isLetterOrDigit() } -> magnetOrHash.uppercase() // base32
            else -> null
        }
    }

    fun isValidMagnet(url: String): Boolean =
        url.startsWith("magnet:?") && parse(url) != null

    private fun decode(s: String): String =
        try { URLDecoder.decode(s, StandardCharsets.UTF_8.name()) } catch (_: Exception) { s }

    private fun encode(s: String): String =
        try { java.net.URLEncoder.encode(s, StandardCharsets.UTF_8.name()) } catch (_: Exception) { s }

    val defaultTrackers = listOf(
        "udp://tracker.opentrackr.org:1337/announce",
        "udp://tracker.openbittorrent.com:6969/announce",
        "udp://open.demonii.com:1337/announce",
        "udp://tracker.torrent.eu.org:451/announce",
        "udp://tracker.tiny-vps.com:6969/announce",
        "udp://explodie.org:6969/announce",
        "http://tracker.gbitt.info:80/announce",
        "udp://tracker.coppersurfer.tk:6969/announce"
    )
}
