package com.lamontlabs.spark.portability

import java.io.InputStream
import java.security.MessageDigest

/** Pure JVM validation used by the archive reader and regression tests. */
internal object ArchiveIntegrity {
    fun validateVersion(version: Int, hasChecksums: Boolean) {
        require(version == 1 || version == 2) { "Unsupported photo archive version" }
        require((version == 2) == hasChecksums) { "Inconsistent archive version metadata" }
    }

    fun validatePhotoReferences(
        referenced: Set<String>,
        staged: Set<String>,
        checksums: Map<String, String>?
    ) {
        require(referenced == staged) { "Photo entries do not match manifest" }
        if (checksums != null) {
            require(referenced == checksums.keys) { "Photo checksum list does not match manifest" }
            require(checksums.values.all { Regex("[0-9a-f]{64}").matches(it) }) { "Invalid photo checksum" }
        }
    }

    fun sha256(input: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
