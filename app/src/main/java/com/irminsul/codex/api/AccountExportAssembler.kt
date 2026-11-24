package com.irminsul.codex.api

import org.json.JSONObject

internal data class ExportSectionResult(
    val data: Any?,
    val status: String,
    val message: String? = null,
    val source: String? = null,
    val retrievedAt: String? = null,
    val lastSuccessfulRetrievalAt: String? = null
) {
    companion object {
        fun success(
            data: Any,
            source: String? = null,
            retrievedAt: String? = null
        ) = ExportSectionResult(
            data = data,
            status = "success",
            source = source,
            retrievedAt = retrievedAt,
            lastSuccessfulRetrievalAt = retrievedAt
        )

        fun partial(
            data: Any,
            message: String,
            source: String? = null,
            retrievedAt: String? = null,
            lastSuccessfulRetrievalAt: String? = retrievedAt
        ) = ExportSectionResult(
            data = data,
            status = "partial",
            message = message,
            source = source,
            retrievedAt = retrievedAt,
            lastSuccessfulRetrievalAt = lastSuccessfulRetrievalAt
        )

        fun failed(
            errorEnvelope: JSONObject,
            message: String,
            source: String? = null,
            retrievedAt: String? = null
        ) = ExportSectionResult(
            data = errorEnvelope,
            status = "failed",
            message = message,
            source = source,
            retrievedAt = retrievedAt
        )

        fun skipped(message: String, source: String? = null) = ExportSectionResult(
            data = null,
            status = "skipped",
            message = message,
            source = source
        )
    }
}

internal object AccountExportAssembler {
    private val requiredSections = setOf(
        "profile_and_exploration",
        "daily_note",
        "characters",
        "spiral_abyss_current",
        "spiral_abyss_previous",
        "imaginarium_theater"
    )

    fun assemble(
        uid: String,
        server: String,
        exportedAt: String,
        sections: Map<String, ExportSectionResult>
    ): JSONObject {
        val root = JSONObject()
            .put("export_format_version", 1)
            .put("exported_at", exportedAt)
            .put("uid", uid)
            .put("server", server)

        val sectionStatuses = JSONObject()
        sections.forEach { (name, result) ->
            if (result.data != null) {
                root.put(name, result.data)
            }

            val status = JSONObject().put("status", result.status)
            result.message?.let { status.put("message", it) }
            result.source?.let { status.put("source", it) }
            result.retrievedAt?.let { status.put("retrieved_at", it) }
            result.lastSuccessfulRetrievalAt?.let {
                status.put("last_successful_retrieval_at", it)
            }
            sectionStatuses.put(name, status)
        }

        val hasProblem = sections.values.any {
            it.status == "failed" || it.status == "partial"
        }
        val missingRequiredSection = requiredSections.any { name ->
            sections[name]?.status != "success"
        }
        val hasUsableSection = sections.values.any {
            (it.status == "success" || it.status == "partial") && it.data != null
        }
        val overallStatus = when {
            !hasUsableSection -> "failed"
            hasProblem || missingRequiredSection -> "partial"
            else -> "complete"
        }

        root.put(
            "export_status",
            JSONObject()
                .put("overall_status", overallStatus)
                .put("sections", sectionStatuses)
        )
        return root
    }
}
