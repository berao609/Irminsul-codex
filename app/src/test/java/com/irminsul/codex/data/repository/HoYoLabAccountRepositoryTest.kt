package com.irminsul.codex.data.repository

import com.irminsul.codex.domain.model.AccountExport
import com.irminsul.codex.domain.model.AccountSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class HoYoLabAccountRepositoryTest {
    private val validExport = """
        {
          "export_format_version": 1,
          "exported_at": "2026-11-25T08:00:00Z",
          "uid": "800000001",
          "server": "os_asia",
          "export_status": {
            "overall_status": "complete",
            "sections": {}
          }
        }
    """.trimIndent()

    @Test
    fun mapsSuccessfulExportIntoDomainSnapshotAndForwardsProgress() = runBlocking {
        val progressMessages = mutableListOf<String>()
        var receivedUid: String? = null
        var receivedCookie: String? = null
        val repository = HoYoLabAccountRepository(
            exportJson = { uid, cookie, onProgress ->
                receivedUid = uid
                receivedCookie = cookie
                onProgress("Fetching account data")
                Result.success(validExport)
            }
        )

        val result = repository.fetchAccountExport(
            uid = "800000001",
            cookie = "test-cookie",
            onProgress = progressMessages::add
        )

        assertTrue(result.isSuccess)
        val accountExport = result.getOrThrow()
        val snapshot = accountExport.snapshot
        assertEquals(validExport, accountExport.rawJson)
        assertEquals("800000001", snapshot.identity.uid)
        assertEquals("os_asia", snapshot.identity.server)
        assertEquals("800000001", receivedUid)
        assertEquals("test-cookie", receivedCookie)
        assertEquals(listOf("Fetching account data"), progressMessages)
    }

    @Test
    fun propagatesExportFailureWithoutAttemptingToMap() = runBlocking {
        var mapperCalled = false
        val failure = IllegalStateException("network unavailable")
        val repository = HoYoLabAccountRepository(
            exportJson = { _, _, _ -> Result.failure(failure) },
            mapExport = {
                mapperCalled = true
                error("Mapper must not run for a failed export.")
            }
        )

        val result = repository.fetchAccountExport("800000001", "test-cookie")

        assertTrue(result.isFailure)
        assertSame(failure, result.exceptionOrNull())
        assertFalse(mapperCalled)
    }

    @Test
    fun convertsMalformedExportJsonIntoRepositoryFailure() = runBlocking {
        val repository = HoYoLabAccountRepository(
            exportJson = { _, _, _ -> Result.success("{not-json") }
        )

        val result = repository.fetchAccountExport("800000001", "test-cookie")

        assertTrue(result.isFailure)
    }

    @Test
    fun preservesCoroutineCancellation() = runBlocking {
        val repository = HoYoLabAccountRepository(
            exportJson = { _, _, _ -> throw CancellationException("cancelled") }
        )

        try {
            repository.fetchAccountExport("800000001", "test-cookie")
            fail("Expected coroutine cancellation to propagate.")
        } catch (cancelled: CancellationException) {
            assertEquals("cancelled", cancelled.message)
        }
    }
}
