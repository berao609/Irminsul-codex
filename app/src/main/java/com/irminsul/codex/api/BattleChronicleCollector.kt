package com.irminsul.codex.api

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject

/**
 * One independently fetched HoYoLAB response with its source and retrieval time.
 * The raw response remains available for the existing lossless selected-section export.
 */
internal data class FetchedExportResponse(
    val response: JSONObject,
    val source: String,
    val retrievedAt: String
)

internal data class BattleChronicleResponses(
    val profileAndExploration: FetchedExportResponse,
    val spiralAbyssCurrent: FetchedExportResponse,
    val spiralAbyssPrevious: FetchedExportResponse,
    val imaginariumTheater: FetchedExportResponse
) {
    fun toExportSections(propertyMap: Map<Int, String>): Map<String, ExportSectionResult> =
        linkedMapOf(
            "profile_and_exploration" to sectionFromResponse(
                profileAndExploration,
                "Profile and exploration",
                propertyMap
            ),
            "spiral_abyss_current" to sectionFromResponse(
                spiralAbyssCurrent,
                "Current Spiral Abyss",
                propertyMap
            ),
            "spiral_abyss_previous" to sectionFromResponse(
                spiralAbyssPrevious,
                "Previous Spiral Abyss",
                propertyMap
            ),
            "imaginarium_theater" to sectionFromResponse(
                imaginariumTheater,
                "Imaginarium Theater",
                propertyMap
            )
        )

    private fun sectionFromResponse(
        fetched: FetchedExportResponse,
        sectionName: String,
        propertyMap: Map<Int, String>
    ): ExportSectionResult {
        val validationError = HoYoLabExportResponseValidator.validationError(
            fetched.response,
            sectionName
        )
        if (validationError != null) {
            val errorData = JSONObject()
                .put("retcode", fetched.response.optInt("retcode", -1))
                .put("message", validationError)
            return ExportSectionResult.failed(
                errorData,
                validationError,
                fetched.source,
                fetched.retrievedAt
            )
        }

        val data = fetched.response.getJSONObject("data")
        return ExportSectionResult.success(
            AccountExportDataPreserver.preserve(data, propertyMap) ?: JSONObject(),
            fetched.source,
            fetched.retrievedAt
        )
    }
}

/**
 * Owns Battle Chronicle profile, exploration, and endgame requests independently
 * from account identity, Daily Note, and character collection retrieval.
 *
 * Each request returns its own response envelope. A failed endpoint therefore
 * becomes a failed section during mapping and does not discard successful siblings.
 */
internal class BattleChronicleCollector(
    private val apiBase: String,
    private val fetchSafely: suspend (source: String, url: String) -> FetchedExportResponse
) {
    suspend fun collect(uid: String, server: String): BattleChronicleResponses = coroutineScope {
        val indexDeferred = async {
            fetchSafely(
                "HoYoLAB index",
                "$apiBase/index?server=$server&role_id=$uid"
            )
        }
        val abyssCurrentDeferred = async {
            fetchSafely(
                "HoYoLAB spiralAbyss schedule 1",
                "$apiBase/spiralAbyss?server=$server&role_id=$uid&schedule_type=1"
            )
        }
        val abyssPreviousDeferred = async {
            fetchSafely(
                "HoYoLAB spiralAbyss schedule 2",
                "$apiBase/spiralAbyss?server=$server&role_id=$uid&schedule_type=2"
            )
        }
        val theaterDeferred = async {
            fetchSafely(
                "HoYoLAB role_combat",
                "$apiBase/role_combat?server=$server&role_id=$uid&need_detail=true"
            )
        }

        BattleChronicleResponses(
            profileAndExploration = indexDeferred.await(),
            spiralAbyssCurrent = abyssCurrentDeferred.await(),
            spiralAbyssPrevious = abyssPreviousDeferred.await(),
            imaginariumTheater = theaterDeferred.await()
        )
    }
}
