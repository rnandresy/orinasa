package com.orinasa.app.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val locFr = Locale.FRENCH

    fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", locFr).format(Date())

    fun now(): String =
        SimpleDateFormat("HH:mm", locFr).format(Date())

    fun nowTimestamp(): Long = System.currentTimeMillis()

    fun formatDate(date: String): String = runCatching {
        val sdf = SimpleDateFormat("yyyy-MM-dd", locFr)
        val out = SimpleDateFormat("dd MMM yyyy", locFr)
        out.format(sdf.parse(date)!!)
    }.getOrElse { date }

    fun formatDateShort(date: String): String = runCatching {
        val sdf = SimpleDateFormat("yyyy-MM-dd", locFr)
        val out = SimpleDateFormat("dd/MM/yy", locFr)
        out.format(sdf.parse(date)!!)
    }.getOrElse { date }

    fun formatDateLong(date: String): String = runCatching {
        val sdf = SimpleDateFormat("yyyy-MM-dd", locFr)
        val out = SimpleDateFormat("EEEE dd MMMM yyyy", locFr)
        out.format(sdf.parse(date)!!).replaceFirstChar { it.uppercase() }
    }.getOrElse { date }

    fun formatTimestamp(ts: Long): String = runCatching {
        SimpleDateFormat("dd MMM · HH:mm", locFr).format(Date(ts))
    }.getOrElse { "" }

    fun formatTime(time: String): String = time // HH:mm déjà formaté

    fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

    fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun monthName(month: Int, year: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.YEAR, year)
        return SimpleDateFormat("MMMM yyyy", locFr)
            .format(cal.time)
            .replaceFirstChar { it.uppercase() }
    }

    fun dayOfWeek(): Int {
        // 1=Lundi ... 7=Dimanche
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY    -> 1
            Calendar.TUESDAY   -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY  -> 4
            Calendar.FRIDAY    -> 5
            Calendar.SATURDAY  -> 6
            Calendar.SUNDAY    -> 7
            else               -> 1
        }
    }

    fun minutesBetween(start: String, end: String): Int = runCatching {
        val sdf = SimpleDateFormat("HH:mm", locFr)
        val s   = sdf.parse(start)!!
        val e   = sdf.parse(end)!!
        ((e.time - s.time) / 60000).toInt()
    }.getOrElse { 0 }

    fun isLate(clockIn: String, scheduledStart: String, toleranceMinutes: Int): Boolean {
        val diff = minutesBetween(scheduledStart, clockIn)
        return diff > toleranceMinutes
    }

    fun lateMinutes(clockIn: String, scheduledStart: String): Int =
        (minutesBetween(scheduledStart, clockIn)).coerceAtLeast(0)

    fun daysBetween(start: String, end: String): Int = runCatching {
        val sdf = SimpleDateFormat("yyyy-MM-dd", locFr)
        val s   = sdf.parse(start)!!
        val e   = sdf.parse(end)!!
        val diff = (e.time - s.time) / (1000 * 60 * 60 * 24)
        (diff + 1).toInt() // inclusif
    }.getOrElse { 0 }

    fun generateInviteCode(): String =
        (100000..999999).random().toString()

    fun formatAriary(amount: Double): String {
        val formatted = String.format("%,.0f", amount)
        return "Ar $formatted"
    }
}