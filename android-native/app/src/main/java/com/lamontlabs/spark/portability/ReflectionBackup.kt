package com.lamontlabs.spark.portability

import com.lamontlabs.spark.data.ReflectionDao
import com.lamontlabs.spark.data.ReflectionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

/**
 * Portable text metadata only (photo bytes are not included).
 * JSON is plaintext and must be handled as sensitive personal data.
 */
class ReflectionBackup(private val dao: ReflectionDao) {
    suspend fun exportJson(): String {
        val rows = dao.snapshotAll()
        require(rows.none { it.body.isBlank() }) { "Photo-only reflections cannot be exported without photo backup support" }
        require(rows.size <= 10_000) { "Too many reflections for this backup format" }
        val items = JSONArray()
        for (r in rows) {
            items.put(JSONObject().apply {
                put("id", r.id)
                put("kind", r.kind)
                put("body", r.body)
                put("reflection_date", r.reflectionDate)
                put("created_at_utc", r.createdAtUtc)
                put("updated_at_utc", r.updatedAtUtc)
                put("mood", r.mood ?: JSONObject.NULL)
                put("favorite", r.favorite)
                put("prompt_id", r.promptId ?: JSONObject.NULL)
                put("photo_id", r.photoId ?: JSONObject.NULL)
                put("schema_version", r.schemaVersion)
            })
        }
        val json = JSONObject().put("format", "spark-reflections").put("version", 1)
            .put("photo_bytes_included", false).put("reflections", items).toString(2)
        require(json.toByteArray(Charsets.UTF_8).size <= 5_000_000) { "Backup exceeds 5 MB" }
        return json
    }

    suspend fun importJson(text: String): ImportResult {
        require(text.toByteArray(Charsets.UTF_8).size <= 5_000_000) { "Backup exceeds 5 MB" }
        val root = JSONObject(text)
        require(root.getString("format") == "spark-reflections" && root.getInt("version") == 1) {
            "Unsupported backup format"
        }
        require(root.has("photo_bytes_included") && !root.getBoolean("photo_bytes_included")) { "Unsupported photo archive" }
        val array = root.getJSONArray("reflections")
        require(array.length() <= 10_000) { "Too many reflections" }
        val seen = HashSet<String>()
        val rows = (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val id = item.getString("id")
            UUID.fromString(id)
            require(seen.add(id)) { "Duplicate reflection ID in backup" }
            val date = item.getString("reflection_date")
            require(LocalDate.parse(date).toString() == date) { "Invalid reflection date" }
            // Never create broken photo references: a text-only import drops photo metadata.
            require(item.has("photo_id") && (item.isNull("photo_id") || item.getString("photo_id").isNotBlank())) { "Invalid photo metadata" }
            val body = item.getString("body")
            require(body.isNotBlank()) { "Photo-only entries cannot be restored without photo bytes" }
            ReflectionEntity(
                id = id,
                kind = item.getString("kind"),
                body = body,
                reflectionDate = date,
                createdAtUtc = item.getLong("created_at_utc"),
                updatedAtUtc = item.getLong("updated_at_utc"),
                mood = item.optString("mood").takeUnless { it.isBlank() || it == "null" },
                favorite = item.getBoolean("favorite"),
                photoId = null,
                promptId = item.optString("prompt_id").takeUnless { it.isBlank() || it == "null" },
                schemaVersion = item.getInt("schema_version")
            )
        }
        val ids = dao.insertIfAbsent(rows)
        return ImportResult(imported = ids.count { it != -1L }, skippedExisting = ids.count { it == -1L })
    }
}

data class ImportResult(val imported: Int, val skippedExisting: Int)
