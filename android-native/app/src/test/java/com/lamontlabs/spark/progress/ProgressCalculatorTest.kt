package com.lamontlabs.spark.progress

import com.lamontlabs.spark.data.ReflectionEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class ProgressCalculatorTest {
    private val today = LocalDate.of(2026, 11, 5)

    private fun reflection(date: String) = ReflectionEntity(
        id = UUID.randomUUID().toString(),
        kind = "GRATITUDE",
        body = "A small good thing",
        reflectionDate = date,
        createdAtUtc = 1L,
        updatedAtUtc = 1L
    )

    @Test fun emptyJournalHasZeroProgress() {
        assertEquals(ReflectionProgress(0, 0, 0, 0), ProgressCalculator.calculate(emptyList(), today))
    }

    @Test fun duplicateEntriesCountOneActiveDay() {
        val result = ProgressCalculator.calculate(listOf(reflection("2026-11-05"), reflection("2026-11-05")), today)
        assertEquals(2, result.totalReflections)
        assertEquals(1, result.activeDays)
        assertEquals(1, result.currentGentleStreak)
    }

    @Test fun oneMissedDayIsNotCountedAsActive() {
        val result = ProgressCalculator.calculate(listOf(reflection("2026-11-01"), reflection("2026-11-03"), reflection("2026-11-04")), today)
        assertEquals(3, result.activeDays)
        assertEquals(3, result.currentGentleStreak)
        assertEquals(3, result.longestGentleStreak)
    }

    @Test fun secondMissedDayBreaksRun() {
        val result = ProgressCalculator.calculate(listOf(reflection("2026-10-30"), reflection("2026-11-01"), reflection("2026-11-03")), today)
        assertEquals(1, result.currentGentleStreak)
        assertEquals(2, result.longestGentleStreak)
    }

    @Test fun staleRunIsNotCurrent() {
        val result = ProgressCalculator.calculate(listOf(reflection("2026-10-20"), reflection("2026-10-21")), today)
        assertEquals(0, result.currentGentleStreak)
        assertEquals(2, result.longestGentleStreak)
    }

    @Test fun futureDatesDoNotCreateActiveDays() {
        val result = ProgressCalculator.calculate(listOf(reflection("2026-11-06")), today)
        assertEquals(0, result.activeDays)
        assertEquals(0, result.currentGentleStreak)
    }

    @Test fun malformedDateDoesNotCrash() {
        val result = ProgressCalculator.calculate(listOf(reflection("2026-02-30")), today)
        assertEquals(0, result.activeDays)
    }
}
