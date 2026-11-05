package com.lamontlabs.spark.portability

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class ArchiveIntegrityTest {
    private fun rejects(block: () -> Unit) {
        try { block(); fail("Expected validation rejection") }
        catch (_: IllegalArgumentException) { }
    }

    @Test fun legacyV1WithoutChecksumsIsAccepted() {
        ArchiveIntegrity.validateVersion(1, false)
    }

    @Test fun v2WithChecksumsIsAccepted() {
        ArchiveIntegrity.validateVersion(2, true)
    }

    @Test fun v1WithChecksumsIsRejected() {
        rejects { ArchiveIntegrity.validateVersion(1, true) }
    }

    @Test fun v2WithoutChecksumsIsRejected() {
        rejects { ArchiveIntegrity.validateVersion(2, false) }
    }

    @Test fun unknownVersionIsRejected() {
        rejects { ArchiveIntegrity.validateVersion(3, true) }
    }

    @Test fun missingPhotoIsRejected() {
        rejects { ArchiveIntegrity.validatePhotoReferences(setOf("a"), emptySet(), null) }
    }

    @Test fun extraPhotoIsRejected() {
        rejects { ArchiveIntegrity.validatePhotoReferences(emptySet(), setOf("a"), null) }
    }

    @Test fun missingChecksumIsRejected() {
        rejects { ArchiveIntegrity.validatePhotoReferences(setOf("a"), setOf("a"), emptyMap()) }
    }

    @Test fun malformedChecksumIsRejected() {
        rejects { ArchiveIntegrity.validatePhotoReferences(setOf("a"), setOf("a"), mapOf("a" to "bad")) }
    }

    @Test fun validChecksumMapIsAccepted() {
        ArchiveIntegrity.validatePhotoReferences(setOf("a"), setOf("a"), mapOf("a" to "a".repeat(64)))
    }

    @Test fun sha256MatchesKnownVector() {
        val digest = "abc".byteInputStream().use { ArchiveIntegrity.sha256(it) }
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", digest)
    }

    @Test fun changedBytesChangeDigest() {
        val a = "photo".byteInputStream().use { ArchiveIntegrity.sha256(it) }
        val b = "Photo".byteInputStream().use { ArchiveIntegrity.sha256(it) }
        org.junit.Assert.assertNotEquals(a, b)
    }
}
