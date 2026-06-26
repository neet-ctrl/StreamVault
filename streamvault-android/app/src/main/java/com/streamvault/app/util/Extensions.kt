package com.streamvault.app.util

import android.content.Context
import android.os.Build
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.toTmdbImageUrl(size: String = "w500"): String =
    "https://image.tmdb.org/t/p/$size$this"

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
    val kb = this / 1024
    val mb = kb / 1024
    val gb = mb / 1024
    return when {
        gb > 0 -> "%.1f GB".format(gb.toFloat())
        mb > 0 -> "%.1f MB".format(mb.toFloat())
        kb > 0 -> "$kb KB"
        else -> "$this B"
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

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun String.extractYear(): String = this.take(4)

fun qualityFromTitle(title: String): String {
    return when {
        title.contains("4K", ignoreCase = true) || title.contains("2160p", ignoreCase = true) -> "4K"
        title.contains("1080p", ignoreCase = true) -> "1080p"
        title.contains("720p", ignoreCase = true) -> "720p"
        title.contains("480p", ignoreCase = true) -> "480p"
        else -> "HD"
    }
}
