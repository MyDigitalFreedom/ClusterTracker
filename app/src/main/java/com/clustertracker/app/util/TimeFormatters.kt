package com.clustertracker.app.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeFormatters {
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")

    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    fun formatTime(instant: Instant): String =
        instant.atZone(ZoneId.systemDefault()).format(timeFormatter)

    fun formatDateTime(instant: Instant): String =
        instant.atZone(ZoneId.systemDefault()).format(dateTimeFormatter)

    fun formatDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    fun formatDurationShort(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    fun formatRelativeMinutes(from: Instant, to: Instant): String {
        val minutes = Duration.between(from, to).toMinutes()
        return "+${minutes}m"
    }
}
