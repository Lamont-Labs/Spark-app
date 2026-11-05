package com.lamontlabs.spark.media

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Imported photos live in app-private filesDir. No broad media permissions.
 * Never trust a source filename or persist a third-party content URI.
 */
class PrivatePhotoStore(context: Context) {
    private val directory = File(context.applicationContext.filesDir, "reflection-photos")
    private val resolver = context.applicationContext.contentResolver

    fun importPhoto(uri: Uri): String {
        if (!directory.exists() && !directory.mkdirs()) {
            throw IllegalStateException("Unable to create private media directory")
        }
        val id = UUID.randomUUID().toString()
        val target = File(directory, "$id.jpg")
        try {
            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= 15L * 1024 * 1024) { "Photo exceeds 15 MB limit" }
                        output.write(buffer, 0, count)
                    }
                    require(total > 0) { "Empty photo" }
                    output.fd.sync()
                }
            } ?: throw IllegalArgumentException("Unable to open selected photo")
            return id
        } catch (exception: Exception) {
            target.delete()
            throw exception
        }
    }

    /** Restore destination restricted to the private photo directory. */
    fun newPhotoFile(id: String): File {
        UUID.fromString(id)
        require(!directory.exists() || directory.isDirectory) { "Invalid photo directory" }
        require(directory.exists() || directory.mkdirs()) { "Cannot create photo directory" }
        val target = File(directory, "$id.jpg")
        require(!target.exists()) { "Photo already exists" }
        return target
    }

    fun fileFor(id: String): File? {
        if (!Regex("[0-9a-fA-F-]{36}").matches(id)) return null
        return File(directory, "$id.jpg").takeIf { it.isFile }
    }

    fun delete(id: String): Boolean {
        if (!Regex("[0-9a-fA-F-]{36}").matches(id)) return false
        return !File(directory, "$id.jpg").exists() || File(directory, "$id.jpg").delete()
    }
}
