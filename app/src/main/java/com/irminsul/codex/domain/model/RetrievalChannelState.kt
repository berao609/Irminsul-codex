package com.irminsul.codex.domain.model

import java.time.Instant

/**
 * Per-channel retrieval state. A failed refresh retains the last successful value
 * and marks it stale instead of replacing it with an empty or failed result.
 */
data class RetrievalChannelState<T>(
    val value: T? = null,
    val status: Status = Status.NOT_REQUESTED,
    val retrievedAt: Instant? = null,
    val lastSuccessfulRetrievedAt: Instant? = null,
    val message: String? = null
) {
    enum class Status { NOT_REQUESTED, LOADING, SUCCESS, STALE, FAILED }

    fun loading(): RetrievalChannelState<T> = copy(status = Status.LOADING, message = null)

    fun success(value: T, retrievedAt: Instant): RetrievalChannelState<T> = RetrievalChannelState(
        value = value,
        status = Status.SUCCESS,
        retrievedAt = retrievedAt,
        lastSuccessfulRetrievedAt = retrievedAt
    )

    fun failure(message: String, retrievedAt: Instant): RetrievalChannelState<T> = copy(
        status = if (value == null) Status.FAILED else Status.STALE,
        retrievedAt = retrievedAt,
        message = message
    )
}
