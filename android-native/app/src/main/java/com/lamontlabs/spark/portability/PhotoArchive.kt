package com.lamontlabs.spark.portability

import android.content.Context
import com.lamontlabs.spark.data.ReflectionDao
import com.lamontlabs.spark.data.ReflectionEntity
import com.lamontlabs.spark.media.PrivatePhotoStore
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.io.File
import java.security.MessageDigest
import java.time.LocalDate
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Portable plaintext ZIP containing manifest.json and private photo bytes.
 * Never overwrite existing reflections or existing photo files.
 */
class PhotoArchive(private val context: Context, private val dao: ReflectionDao) {
    private val photos = PrivatePhotoStore(context)
    private val maxPhoto = 15L * 1024 * 1024
    private val maxArchive = 200L * 1024 * 1024

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    suspend fun exportTo(output: OutputStream) {
        val rows = dao.snapshotAll()
        require(rows.size <= 10_000) { "Too many reflections" }
        val manifest = JSONArray()
        val photoFiles = LinkedHashMap<String, File>()
        for (row in rows) {
            row.photoId?.let { id ->
                UUID.fromString(id)
                val file = photos.fileFor(id) ?: error("Missing photo for reflection ${row.id}")
                require(file.length() in 1..maxPhoto) { "Invalid photo size" }
                photoFiles[id] = file
            }
            manifest.put(JSONObject().apply {
                put("id", row.id); put("kind", row.kind); put("body", row.body)
                put("reflection_date", row.reflectionDate)
                put("created_at_utc", row.createdAtUtc); put("updated_at_utc", row.updatedAtUtc)
                put("mood", row.mood ?: JSONObject.NULL); put("favorite", row.favorite)
                put("prompt_id", row.promptId ?: JSONObject.NULL)
                put("photo_id", row.photoId ?: JSONObject.NULL); put("schema_version", row.schemaVersion)
            })
        }
        val checksums = JSONObject()
        for ((id, file) in photoFiles) checksums.put(id, sha256(file))
        val root = JSONObject().put("format", "spark-photo-archive").put("version", 1)
            .put("reflections", manifest).put("photo_sha256", checksums)
        val bytes = root.toString().toByteArray(Charsets.UTF_8)
        require(bytes.size <= 5_000_000) { "Manifest exceeds 5 MB" }
        var total = bytes.size.toLong()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(bytes)
            zip.closeEntry()
            for ((id, file) in photoFiles) {
                total += file.length()
                require(total <= maxArchive) { "Archive exceeds 200 MB" }
                zip.putNextEntry(ZipEntry("photos/$id.jpg"))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
            zip.finish()
        }
    }

    suspend fun importFrom(input: InputStream): ImportResult {
        val staging = File(context.cacheDir, "spark-restore-${UUID.randomUUID()}")
        require(staging.mkdirs()) { "Unable to create temporary restore directory" }
        val staged = LinkedHashMap<String, File>()
        val installed = ArrayList<String>()
        try {
            var manifestBytes: ByteArray? = null
            var total = 0L
            var entries = 0
            ZipInputStream(input).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    require(++entries <= 10_001) { "Too many archive entries" }
                    require(!entry.isDirectory) { "Unexpected archive directory" }
                    val name = entry.name
                    require(name == "manifest.json" || Regex("photos/[0-9a-fA-F-]{36}\\.jpg").matches(name)) {
                        "Unexpected archive path"
                    }
                    val destination = if (name == "manifest.json") File(staging, "manifest.json")
                        else File(staging, name.substringAfter('/'))
                    require(!destination.exists()) { "Duplicate archive entry" }
                    destination.outputStream().use { out ->
                        val buffer = ByteArray(8192)
                        var entryBytes = 0L
                        while (true) {
                            val count = zip.read(buffer)
                            if (count == -1) break
                            entryBytes += count
                            total += count
                            require(total <= maxArchive) { "Archive exceeds 200 MB" }
                            require(entryBytes <= if (name == "manifest.json") 5_000_000L else maxPhoto) {
                                "Archive entry exceeds limit"
                            }
                            out.write(buffer, 0, count)
                        }
                    }
                    if (name == "manifest.json") manifestBytes = destination.readBytes()
                    else staged[name.substringAfter('/').removeSuffix(".jpg")] = destination
                    zip.closeEntry()
                }
            }
            val root = JSONObject((manifestBytes ?: error("Missing manifest")).toString(Charsets.UTF_8))
            require(root.getString("format") == "spark-photo-archive" && root.getInt("version") == 1) {
                "Unsupported photo archive"
            }
            val checksums = root.getJSONObject("photo_sha256")
            val array = root.getJSONArray("reflections")
            require(array.length() <= 10_000) { "Too many reflections" }
            val ids = HashSet<String>()
            val referenced = HashSet<String>()
            val rows = (0 until array.length()).map { i ->
                val item = array.getJSONObject(i)
                val id = item.getString("id")
                UUID.fromString(id)
                require(ids.add(id)) { "Duplicate reflection ID" }
                val date = item.getString("reflection_date")
                require(LocalDate.parse(date).toString() == date) { "Invalid date" }
                val photoId = if (item.isNull("photo_id")) null else item.getString("photo_id")
                photoId?.let { UUID.fromString(it); referenced.add(it) }
                ReflectionEntity(
                    id = id, kind = item.getString("kind"), body = item.getString("body"),
                    reflectionDate = date, createdAtUtc = item.getLong("created_at_utc"),
                    updatedAtUtc = item.getLong("updated_at_utc"),
                    mood = if (item.isNull("mood")) null else item.getString("mood"),
                    favorite = item.getBoolean("favorite"),
                    photoId = photoId,
                    promptId = if (item.isNull("prompt_id")) null else item.getString("prompt_id"),
                    schemaVersion = item.getInt("schema_version")
                )
            }
            require(staged.keys == referenced) { "Photo entries do not match manifest" }
            val checksumKeys = HashSet<String>()
            val iterator = checksums.keys()
            while (iterator.hasNext()) checksumKeys.add(iterator.next())
            require(checksumKeys == referenced) { "Photo checksum list does not match manifest" }
            for (id in referenced) {
                val expected = checksums.getString(id)
                require(Regex("[0-9a-f]{64}").matches(expected)) { "Invalid photo checksum" }
                require(sha256(staged[id] ?: error("Missing photo")) == expected) { "Corrupted photo bytes" }
            }
            val newRows = rows.filter { dao.findById(it.id) == null }
            val requiredPhotos = newRows.mapNotNull { it.photoId }.toSet()
            for (id in requiredPhotos) {
                require(photos.fileFor(id) == null) { "Photo ID already exists locally" }
            }
            for (id in requiredPhotos) {
                val source = staged[id] ?: error("Missing photo")
                val target = photos.newPhotoFile(id)
                installed.add(id)
                require(source.copyTo(target, overwrite = false).length() == source.length()) { "Photo copy failed" }
            }
            val inserted = dao.insertIfAbsent(newRows)
            val insertedIds = newRows.filterIndexed { index, _ -> inserted[index] != -1L }
                .mapNotNull { it.photoId }.toSet()
            for (id in installed) if (id !in insertedIds) photos.delete(id)
            return ImportResult(inserted.count { it != -1L }, rows.size - inserted.count { it != -1L })
        } catch (error: Exception) {
            for (id in installed) photos.delete(id)
            throw error
        } finally {
            staging.deleteRecursively()
        }
    }
}
