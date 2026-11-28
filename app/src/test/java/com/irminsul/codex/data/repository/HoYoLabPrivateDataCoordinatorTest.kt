package com.irminsul.codex.data.repository

import com.irminsul.codex.api.AccountProfile
import com.irminsul.codex.api.DailyNote
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

class HoYoLabPrivateDataCoordinatorTest {
    private val note = DailyNote(
        currentResin = 120, maxResin = 200, resinRecoveryTime = 28_800L,
        finishedTaskNum = 4, totalTaskNum = 4, isExtraTaskRewardReceived = true,
        remainResinDiscountNum = 3, resinDiscountNumLimit = 3,
        currentHomeCoin = 2_000, maxHomeCoin = 2_400,
        currentExpeditionNum = 5, maxExpeditionNum = 5,
        transformerObtained = true, transformerRecoveryTimeSeconds = 3_600L,
        transformerReached = false
    )
    private val profile = AccountProfile(
        nickname = "Traveler", uid = "800000001", serverName = "os_asia", avatarUrl = null
    )

    @Test
    fun retrievesPrivateChannelsConcurrentlyWithSharedSession() = runBlocking {
        val started = AtomicInteger()
        val bothStarted = CompletableDeferred<Unit>()
        val timestamp = Instant.parse("2026-11-28T01:00:00Z")
        val coordinator = HoYoLabPrivateDataCoordinator(
            fetchDailyNote = { uid, cookie ->
                assertEquals("800000001", uid)
                assertEquals("shared-cookie", cookie)
                if (started.incrementAndGet() == 2) bothStarted.complete(Unit)
                bothStarted.await()
                Result.success(note)
            },
            fetchAccountProfile = { uid, cookie ->
                assertEquals("800000001", uid)
                assertEquals("shared-cookie", cookie)
                if (started.incrementAndGet() == 2) bothStarted.complete(Unit)
                bothStarted.await()
                Result.success(profile)
            },
            clock = { timestamp }
        )
        val result = withTimeout(2_000L) { coordinator.refresh("800000001", "shared-cookie") }
        assertEquals(note, result.dailyNote.result.getOrThrow())
        assertEquals(profile, result.accountProfile.result.getOrThrow())
        assertEquals(timestamp, result.dailyNote.retrievedAt)
        assertEquals(timestamp, result.accountProfile.retrievedAt)
    }

    @Test
    fun oneChannelFailureDoesNotDiscardSuccessfulSibling() = runBlocking {
        val coordinator = HoYoLabPrivateDataCoordinator(
            fetchDailyNote = { _, _ -> Result.failure(IllegalStateException("Daily Note unavailable")) },
            fetchAccountProfile = { _, _ -> Result.success(profile) }
        )
        val result = coordinator.refresh("800000001", "cookie")
        assertEquals("Daily Note unavailable", result.dailyNote.result.exceptionOrNull()?.message)
        assertEquals(profile, result.accountProfile.result.getOrThrow())
    }

    @Test
    fun thrownOrdinaryFailureIsCapturedWithoutFailingOtherChannel() = runBlocking {
        val coordinator = HoYoLabPrivateDataCoordinator(
            fetchDailyNote = { _, _ -> throw IllegalStateException("Daily Note service offline") },
            fetchAccountProfile = { _, _ -> Result.success(profile) }
        )
        val result = coordinator.refresh("800000001", "cookie")
        assertEquals("Daily Note service offline", result.dailyNote.result.exceptionOrNull()?.message)
        assertEquals(profile, result.accountProfile.result.getOrThrow())
    }

    @Test
    fun cancellationIsNeverConvertedIntoAnOrdinaryChannelFailure() = runBlocking {
        val coordinator = HoYoLabPrivateDataCoordinator(
            fetchDailyNote = { _, _ -> throw CancellationException("refresh cancelled") },
            fetchAccountProfile = { _, _ -> Result.success(profile) }
        )
        try {
            coordinator.refresh("800000001", "cookie")
            fail("Expected cancellation to propagate.")
        } catch (cancelled: CancellationException) {
            assertEquals("refresh cancelled", cancelled.message)
        }
    }
}
