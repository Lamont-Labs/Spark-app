package com.lamontlabs.spark.portability

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import com.lamontlabs.spark.data.ReflectionEntity
import com.lamontlabs.spark.data.SparkDatabase
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class PhotoArchiveInstrumentedTest {
    private lateinit var context: Context
    private lateinit var db: SparkDatabase
    private lateinit var archive: PhotoArchive
    private val photos = ArrayList<String>()

    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, SparkDatabase::class.java).build()
        archive = PhotoArchive(context, db.reflectionDao())
    }

    @After fun cleanup() {
        db.close()
        photos.forEach { File(context.filesDir, "reflection-photos/$it.jpg").delete() }
    }

    private fun row(id: String = UUID.randomUUID().toString(), photoId: String? = null) =
        ReflectionEntity(id = id, kind = "GRATITUDE", body = if (photoId == null) "A good day" else "",
            reflectionDate = "2026-11-05", createdAtUtc = 1000L, updatedAtUtc = 1000L, photoId = photoId)

    private fun archiveBytes() = runBlocking {
        val output = ByteArrayOutputStream()
        archive.exportTo(output)
        output.toByteArray()
    }

    private fun rewriteManifest(bytes: ByteArray, change: (JSONObject) -> Unit): ByteArray {
        val entries = ArrayList<Pair<String, ByteArray>>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries.add(entry.name to zip.readBytes())
                zip.closeEntry()
            }
        }
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            for ((name, content) in entries) {
                zip.putNextEntry(ZipEntry(name))
                val changed = if (name == "manifest.json") {
                    val root = JSONObject(content.toString(Charsets.UTF_8))
                    change(root)
                    root.toString().toByteArray(Charsets.UTF_8)
                } else content
                zip.write(changed)
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    @Test fun textReflectionRoundTripPreservesFields() = runBlocking {
        val original = row()
        db.reflectionDao().insert(original)
        val bytes = archiveBytes()
        db.reflectionDao().deleteById(original.id)
        val result = archive.importFrom(ByteArrayInputStream(bytes))
        assertEquals(1, result.imported)
        assertEquals(original, db.reflectionDao().findById(original.id))
    }

    @Test fun photoOnlyReflectionRoundTripRestoresExactBytes() = runBlocking {
        val id = UUID.randomUUID().toString()
        photos.add(id)
        val file = File(context.filesDir, "reflection-photos/$id.jpg")
        file.parentFile!!.mkdirs()
        val content = byteArrayOf(0x7f, 0x22, 0x11, 0x44)
        file.writeBytes(content)
        val original = row(photoId = id)
        db.reflectionDao().insert(original)
        val bytes = archiveBytes()
        db.reflectionDao().deleteById(original.id)
        file.delete()
        val result = archive.importFrom(ByteArrayInputStream(bytes))
        assertEquals(1, result.imported)
        assertEquals(original, db.reflectionDao().findById(original.id))
        assertArrayEquals(content, file.readBytes())
    }

    @Test fun existingReflectionIsNotOverwritten() = runBlocking {
        val original = row()
        db.reflectionDao().insert(original)
        val bytes = archiveBytes()
        val existing = original.copy(body = "Keep my local changes")
        db.reflectionDao().deleteById(original.id)
        db.reflectionDao().insert(existing)
        val result = archive.importFrom(ByteArrayInputStream(bytes))
        assertEquals(0, result.imported)
        assertEquals(1, result.skippedExisting)
        assertEquals(existing, db.reflectionDao().findById(original.id))
    }

    @Test fun changedPhotoChecksumRejectsBeforeDatabaseWrites() = runBlocking {
        val id = UUID.randomUUID().toString()
        photos.add(id)
        val file = File(context.filesDir, "reflection-photos/$id.jpg")
        file.parentFile!!.mkdirs()
        file.writeBytes(byteArrayOf(1, 2, 3, 4))
        val original = row(photoId = id)
        db.reflectionDao().insert(original)
        val bytes = archiveBytes()
        db.reflectionDao().deleteById(original.id)
        file.delete()
        val corrupted = rewriteManifest(bytes) { root ->
            root.getJSONObject("photo_sha256").put(id, "0".repeat(64))
        }
        try {
            archive.importFrom(ByteArrayInputStream(corrupted))
            fail("Expected checksum mismatch")
        } catch (_: IllegalArgumentException) { }
        assertNull(db.reflectionDao().findById(original.id))
        assertFalse(file.exists())
    }

    @Test fun legacyVersionOneArchiveRestoresText() = runBlocking {
        val original = row()
        db.reflectionDao().insert(original)
        val bytes = archiveBytes()
        db.reflectionDao().deleteById(original.id)
        val legacy = rewriteManifest(bytes) { root ->
            root.put("version", 1)
            root.remove("photo_sha256")
        }
        val result = archive.importFrom(ByteArrayInputStream(legacy))
        assertEquals(1, result.imported)
        assertEquals(original, db.reflectionDao().findById(original.id))
    }

    @Test fun legacyVersionOneWithChecksumsIsRejected() = runBlocking {
        val original = row()
        db.reflectionDao().insert(original)
        val bytes = archiveBytes()
        db.reflectionDao().deleteById(original.id)
        val malformed = rewriteManifest(bytes) { it.put("version", 1) }
        try {
            archive.importFrom(ByteArrayInputStream(malformed))
            fail("Expected rejection")
        } catch (_: IllegalArgumentException) { }
        assertNull(db.reflectionDao().findById(original.id))
    }

    @Test fun duplicateManifestReflectionIdRejectsWholeImport() = runBlocking {
        val original = row()
        db.reflectionDao().insert(original)
        val bytes = archiveBytes()
        db.reflectionDao().deleteById(original.id)
        val duplicate = rewriteManifest(bytes) { root ->
            val items = root.getJSONArray("reflections")
            items.put(JSONObject(items.getJSONObject(0).toString()))
        }
        try {
            archive.importFrom(ByteArrayInputStream(duplicate))
            fail("Expected duplicate ID rejection")
        } catch (_: IllegalArgumentException) { }
        assertNull(db.reflectionDao().findById(original.id))
    }

    @Test fun archiveWithUnexpectedPhotoPathIsRejected() = runBlocking {
        val original = row()
        db.reflectionDao().insert(original)
        val bytes = archiveBytes()
        db.reflectionDao().deleteById(original.id)
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            ZipInputStream(ByteArrayInputStream(bytes)).use { source ->
                while (true) {
                    val entry = source.nextEntry ?: break
                    zip.putNextEntry(ZipEntry(entry.name))
                    zip.write(source.readBytes())
                    zip.closeEntry()
                    source.closeEntry()
                }
            }
            zip.putNextEntry(ZipEntry("../escape.txt"))
            zip.write(byteArrayOf(1))
            zip.closeEntry()
        }
        try {
            archive.importFrom(ByteArrayInputStream(output.toByteArray()))
            fail("Expected path rejection")
        } catch (_: IllegalArgumentException) { }
        assertNull(db.reflectionDao().findById(original.id))
    }

    @Test fun truncatedArchiveDoesNotModifyExistingReflection() = runBlocking {
        val existing = row()
        db.reflectionDao().insert(existing)
        val bytes = archiveBytes()
        val truncated = bytes.copyOf(bytes.size / 2)
        try {
            archive.importFrom(ByteArrayInputStream(truncated))
            fail("Expected incomplete ZIP rejection")
        } catch (_: Exception) { }
        assertEquals(existing, db.reflectionDao().findById(existing.id))
    }

    @Test fun invalidManifestDateRejectsImportWithoutWrites() = runBlocking {
        val original = row()
        db.reflectionDao().insert(original)
        val bytes = archiveBytes()
        db.reflectionDao().deleteById(original.id)
        val invalid = rewriteManifest(bytes) { root ->
            root.getJSONArray("reflections").getJSONObject(0)
                .put("reflection_date", "2026-02-30")
        }
        try {
            archive.importFrom(ByteArrayInputStream(invalid))
            fail("Expected invalid date rejection")
        } catch (_: Exception) { }
        assertNull(db.reflectionDao().findById(original.id))
    }

    @Test fun missingReferencedPhotoRejectsWithoutDatabaseWrites() = runBlocking {
        val id = UUID.randomUUID().toString()
        photos.add(id)
        val file = File(context.filesDir, "reflection-photos/$id.jpg")
        file.parentFile!!.mkdirs()
        file.writeBytes(byteArrayOf(1, 2, 3))
        val original = row(photoId = id)
        db.reflectionDao().insert(original)
        val bytes = archiveBytes()
        db.reflectionDao().deleteById(original.id)
        file.delete()
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            ZipInputStream(ByteArrayInputStream(bytes)).use { source ->
                while (true) {
                    val entry = source.nextEntry ?: break
                    if (entry.name == "manifest.json") {
                        zip.putNextEntry(ZipEntry(entry.name))
                        zip.write(source.readBytes())
                        zip.closeEntry()
                    }
                    source.closeEntry()
                }
            }
        }
        try {
            archive.importFrom(ByteArrayInputStream(output.toByteArray()))
            fail("Expected missing photo rejection")
        } catch (_: IllegalArgumentException) { }
        assertNull(db.reflectionDao().findById(original.id))
        assertFalse(file.exists())
    }

    @Test fun missingZipCentralDirectoryCannotRestoreNewReflection() = runBlocking {
        val original = row()
        db.reflectionDao().insert(original)
        val bytes = archiveBytes()
        db.reflectionDao().deleteById(original.id)
        val incomplete = bytes.copyOf(bytes.size - 22)
        try {
            archive.importFrom(ByteArrayInputStream(incomplete))
            fail("Expected missing central directory rejection")
        } catch (_: Exception) { }
        assertNull(db.reflectionDao().findById(original.id))
    }

    @Test fun unsupportedVersionRejectsBeforeDatabaseWrites() = runBlocking {
        val original = row()
        db.reflectionDao().insert(original)
        val bytes = archiveBytes()
        db.reflectionDao().deleteById(original.id)
        val corrupted = rewriteManifest(bytes) { it.put("version", 3) }
        try {
            archive.importFrom(ByteArrayInputStream(corrupted))
            fail("Expected unsupported archive version")
        } catch (_: IllegalArgumentException) { }
        assertNull(db.reflectionDao().findById(original.id))
    }
}
