package com.lamontlabs.spark.progress

import com.lamontlabs.spark.data.ReflectionEntity
import java.time.LocalDate

data class ReflectionProgress(
    val totalReflections: Int,
    val activeDays: Int,
    val currentGentleStreak: Int,
    val longestGentleStreak: Int
)

/**
 * One reflection makes a day active. A single missed calendar day may be bridged
 * between two active days. Never claim a skipped day was an active day.
 * Current streak is zero if last reflection is more than one day before today.
 */
object ProgressCalculator {
    fun calculate(reflections: List<ReflectionEntity>, today: LocalDate): ReflectionProgress {
        val dates = reflections.mapNotNull {
            runCatching { LocalDate.parse(it.reflectionDate) }.getOrNull()
        }.filter { !it.isAfter(today) }.distinct().sorted()
        if (dates.isEmpty()) return ReflectionProgress(reflections.size, 0, 0, 0)

        var run = 1
        var longest = 1
        var missed = 0
        var current = 0
        for (i in 1 until dates.size) {
            val gap = java.time.temporal.ChronoUnit.DAYS.between(dates[i - 1], dates[i])
            if (gap == 1L) {
                run += 1
            } else if (gap == 2L && missed == 0) {
                missed += 1
                run += 1
            } else {
                longest = maxOf(longest, run)
                run = 1
                missed = 0
            }
        }
        longest = maxOf(longest, run)
        val daysSince = java.time.temporal.ChronoUnit.DAYS.between(dates.last(), today)
        if (daysSince <= 1L || (daysSince == 2L && missed == 0)) current = run
        return ReflectionProgress(reflections.size, dates.size, current, longest)
    }
}
