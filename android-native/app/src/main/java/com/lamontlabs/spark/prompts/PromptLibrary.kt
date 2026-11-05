package com.lamontlabs.spark.prompts

import java.time.LocalDate

enum class PromptAudience { SHARED, TEEN, ADULT }

data class ReflectionPrompt(
    val id: String,
    val kind: String,
    val audience: PromptAudience,
    val text: String
)

/** Offline starter content. Stable IDs allow reflections to retain provenance. */
object PromptLibrary {
    val all: List<ReflectionPrompt> = listOf(
        ReflectionPrompt("gratitude-shared-001", "GRATITUDE", PromptAudience.SHARED, "What small moment made today a little better?"),
        ReflectionPrompt("gratitude-shared-002", "GRATITUDE", PromptAudience.SHARED, "Who helped you feel supported recently?"),
        ReflectionPrompt("gratitude-shared-003", "GRATITUDE", PromptAudience.SHARED, "What everyday comfort did you appreciate?"),
        ReflectionPrompt("gratitude-teen-001", "GRATITUDE", PromptAudience.TEEN, "What part of your day felt like you could be yourself?"),
        ReflectionPrompt("gratitude-adult-001", "GRATITUDE", PromptAudience.ADULT, "What ordinary moment would you like to remember?"),
        ReflectionPrompt("win-shared-001", "WIN", PromptAudience.SHARED, "What is one thing you followed through on today?"),
        ReflectionPrompt("win-shared-002", "WIN", PromptAudience.SHARED, "When did you try, even if it was difficult?"),
        ReflectionPrompt("win-shared-003", "WIN", PromptAudience.SHARED, "What small step moved you forward?"),
        ReflectionPrompt("win-teen-001", "WIN", PromptAudience.TEEN, "What did you learn about yourself today?"),
        ReflectionPrompt("win-adult-001", "WIN", PromptAudience.ADULT, "What effort deserves recognition, even if nobody noticed?")
    )

    init {
        require(all.map { it.id }.distinct().size == all.size) { "Prompt IDs must be unique" }
        require(all.all { it.kind == "GRATITUDE" || it.kind == "WIN" })
    }

    fun forKind(kind: String, audience: PromptAudience = PromptAudience.SHARED): List<ReflectionPrompt> =
        all.filter { it.kind == kind && (it.audience == PromptAudience.SHARED || it.audience == audience) }

    fun daily(kind: String, date: LocalDate, audience: PromptAudience = PromptAudience.SHARED): ReflectionPrompt? {
        val prompts = forKind(kind, audience)
        if (prompts.isEmpty()) return null
        return prompts[Math.floorMod(date.toEpochDay(), prompts.size.toLong()).toInt()]
    }

    fun byId(id: String): ReflectionPrompt? = all.firstOrNull { it.id == id }
}
