package com.lamontlabs.spark.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.UUID

class ReflectionRepository(private val dao: ReflectionDao) {
    fun observeAll(): Flow<List<ReflectionEntity>> = dao.observeAll()

    fun observeByDate(date: LocalDate): Flow<List<ReflectionEntity>> =
        dao.observeByDate(date.toString())

    suspend fun create(
        kind: String,
        body: String,
        date: LocalDate,
        mood: String? = null,
        photoId: String? = null,
        promptId: String? = null,
        nowUtcMillis: Long = System.currentTimeMillis()
    ): String {
        val id = UUID.randomUUID().toString()
        dao.insert(
            ReflectionEntity(
                id = id,
                kind = kind,
                body = body.trim(),
                reflectionDate = date.toString(),
                createdAtUtc = nowUtcMillis,
                updatedAtUtc = nowUtcMillis,
                mood = mood,
                photoId = photoId,
                promptId = promptId
            )
        )
        return id
    }

    suspend fun findById(id: String): ReflectionEntity? = dao.findById(id)
}
