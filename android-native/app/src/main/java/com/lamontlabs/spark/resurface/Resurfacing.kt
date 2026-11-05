package com.lamontlabs.spark.resurface

import com.lamontlabs.spark.data.ReflectionEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * A quiet, deterministic reminder from the user's own journal.
 * No randomness, network, notifications, or hidden engagement scoring.
 */
object Resurfacing {
    fun select(reflections: List<ReflectionEntity>, today: LocalDate): ReflectionEntity? {
        val eligible = reflections.filter { reflection ->
            val date = runCatching { LocalDate.parse(reflection.reflectionDate) }.getOrNull()
            date != null &&
                ChronoUnit.DAYS.between(date, today) >= 7 &&
                (reflection.body.isNotBlank() || reflection.photoId != null)
        }.sortedWith(
            compareByDescending<ReflectionEntity> { it.favorite }
                .thenByDescending { it.reflectionDate }
                .thenBy { it.id }
        )
        if (eligible.isEmpty()) return null
        val index = Math.floorMod(today.toEpochDay(), eligible.size.toLong()).toInt()
        return eligible[index]
    }
}
