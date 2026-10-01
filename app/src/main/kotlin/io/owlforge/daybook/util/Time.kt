package io.owlforge.daybook.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
private val dateFmt = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())

val zone: ZoneId get() = ZoneId.systemDefault()

fun fmtTime(ms: Long): String = Instant.ofEpochMilli(ms).atZone(zone).format(timeFmt)
fun fmtMinutes(min: Int): String = LocalTime.of(min / 60, min % 60).format(timeFmt)
fun fmtHm(h: Int, m: Int): String = LocalTime.of(h, m).format(timeFmt)

fun fmtDate(d: LocalDate): String {
    val today = LocalDate.now()
    return when (d) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        today.plusDays(1) -> "Tomorrow"
        else -> d.format(dateFmt)
    }
}

fun fmtDuration(ms: Long): String {
    val mins = ms / 60_000
    val h = mins / 60
    val m = mins % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
}

fun fmtCountdown(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
}

/** Midnight (local) of today + [offset] days, in epoch millis. */
fun dayStart(offset: Int): Long =
    LocalDate.now().plusDays(offset.toLong()).atStartOfDay(zone).toInstant().toEpochMilli()

/** Epoch millis for [minuteOfDay] on today + [offset] days. */
fun millisOn(offset: Int, minuteOfDay: Int): Long =
    LocalDate.now().plusDays(offset.toLong())
        .atTime(minuteOfDay / 60, minuteOfDay % 60)
        .atZone(zone).toInstant().toEpochMilli()

/** Next future occurrence of hour:minute. */
fun nextOccurrence(hour: Int, minute: Int): Long {
    val now = ZonedDateTime.now(zone)
    var t = now.toLocalDate().atTime(hour, minute).atZone(zone)
    if (!t.isAfter(now)) t = t.plusDays(1)
    return t.toInstant().toEpochMilli()
}

fun greeting(): String {
    val h = LocalTime.now().hour
    return when {
        h < 5 -> "Still up?"
        h < 12 -> "Good morning"
        h < 17 -> "Good afternoon"
        h < 21 -> "Good evening"
        else -> "Good night"
    }
}
