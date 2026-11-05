package com.lamontlabs.spark.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * UTC timestamps are epoch milliseconds. reflectionDate is the local YYYY-MM-DD
 * date explicitly assigned when the reflection is created, not inferred from UTC.
 */
@Entity(
    tableName = "reflections",
    indices = [Index(value = ["reflection_date"]), Index(value = ["kind", "reflection_date"])]
)
data class ReflectionEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val body: String,
    @ColumnInfo(name = "reflection_date") val reflectionDate: String,
    @ColumnInfo(name = "created_at_utc") val createdAtUtc: Long,
    @ColumnInfo(name = "updated_at_utc") val updatedAtUtc: Long,
    val mood: String? = null,
    val favorite: Boolean = false,
    @ColumnInfo(name = "photo_id") val photoId: String? = null,
    @ColumnInfo(name = "prompt_id") val promptId: String? = null,
    @ColumnInfo(name = "schema_version") val schemaVersion: Int = 1
) {
    init {
        require(id.isNotBlank()) { "Reflection ID is required" }
        require(kind == "GRATITUDE" || kind == "WIN") { "Unsupported reflection kind" }
        require(body.isNotBlank() || !photoId.isNullOrBlank()) { "Text or photo required" }
        require(Regex("\\d{4}-\\d{2}-\\d{2}").matches(reflectionDate)) { "Expected YYYY-MM-DD" }
        require(createdAtUtc >= 0 && updatedAtUtc >= createdAtUtc) { "Invalid timestamps" }
        require(schemaVersion == 1) { "Unsupported schema version" }
    }
}
