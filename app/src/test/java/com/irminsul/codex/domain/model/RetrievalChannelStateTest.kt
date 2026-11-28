package com.irminsul.codex.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class RetrievalChannelStateTest {
    @Test
    fun failedRefreshRetainsLastSuccessfulValueAndMarksItStale() {
        val retrievedAt = Instant.parse("2026-11-27T10:00:00Z")
        val failedAt = Instant.parse("2026-11-28T10:00:00Z")
        val successful = RetrievalChannelState("cached-profile").success("cached-profile", retrievedAt)
        val failed = successful.loading().failure("Network unavailable", failedAt)
        assertEquals("cached-profile", failed.value)
        assertEquals(RetrievalChannelState.Status.STALE, failed.status)
        assertEquals(retrievedAt, failed.lastSuccessfulRetrievedAt)
        assertEquals(failedAt, failed.retrievedAt)
        assertEquals("Network unavailable", failed.message)
    }

    @Test
    fun failedFirstRetrievalHasNoFabricatedValue() {
        val failedAt = Instant.parse("2026-11-28T10:00:00Z")
        val failed = RetrievalChannelState<String>().failure("Unavailable", failedAt)
        assertNull(failed.value)
        assertEquals(RetrievalChannelState.Status.FAILED, failed.status)
        assertNull(failed.lastSuccessfulRetrievedAt)
    }
}
