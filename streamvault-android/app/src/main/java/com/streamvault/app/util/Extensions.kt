package com.streamvault.app.util

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// NOTE: toTmdbImageUrl() removed — Cinemeta supplies absolute poster/backdrop URLs.
// Use poster strings directly as Coil model arguments.

fun Int.toRuntimeString(): String {
    val hours = this / 60
    val minutes = this % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

fun Double.toRatingString(): String = String.format(Locale.US, "%.1f", this)

fun String.toFormattedDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val date = inputFormat.parse(this)
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        this
    }
}

fun Long.toFormattedSize(): String {
    if (this <= 0) return "Unknown"
    val kb = this / 1024
    val mb = kb / 1024
    val gb = mb / 1024
    return when {
        gb > 0 -> "%.2f GB".format(gb.toFloat() + (mb % 1024).toFloat() / 1024f)
        mb > 0 -> "%.1f MB".format(mb.toFloat() + (kb % 1024).toFloat() / 1024f)
        kb > 0 -> "$kb KB"
        else   -> "$this B"
    }
}

fun Long.toProgressString(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

fun Long.toSpeedString(): String = when {
    this >= 1_000_000L -> "%.1f MB/s".format(this / 1_000_000f)
    this >= 1_000L     -> "%.0f KB/s".format(this / 1_000f)
    else               -> "$this B/s"
}

fun Long.toEtaString(): String {
    if (this <= 0) return "∞"
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    return when {
        hours > 0   -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else        -> "${seconds}s"
    }
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun String.extractYear(): String = this.take(4)

fun qualityFromTitle(title: String): String = when {
    title.contains("4K", ignoreCase = true) || title.contains("2160p", ignoreCase = true) -> "4K"
    title.contains("1080p", ignoreCase = true) -> "1080p"
    title.contains("720p",  ignoreCase = true) -> "720p"
    title.contains("480p",  ignoreCase = true) -> "480p"
    title.contains("HD",    ignoreCase = true) -> "HD"
    else -> "HD"
}

fun Long.toRelativeTime(): String {
    val diff = System.currentTimeMillis() - this
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days > 7    -> SimpleDateFormat("MMM d", Locale.US).format(Date(this))
        days > 0    -> "${days}d ago"
        hours > 0   -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else        -> "Just now"
    }
}

fun Float.toBrightnessLevel(): String = when {
    this < 0.25f -> "Low"
    this < 0.75f -> "Medium"
    else         -> "High"
}

fun Float.toVolumeIcon(): String = when {
    this <= 0f  -> "🔇"
    this < 0.3f -> "🔈"
    this < 0.7f -> "🔉"
    else        -> "🔊"
}
