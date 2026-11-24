package com.irminsul.codex.api

import org.json.JSONObject

internal data class ExportSectionResult(
    val data: Any?,
    val status: String,
    val message: String? = null
) {
    companion object {
        fun success(data: Any) = ExportSectionResult(data, "success")
        fun partial(data: Any, message: String) = ExportSectionResult(data, "partial", message)
        fun failed(errorEnvelope: JSONObject, message: String) =
            ExportSectionResult(errorEnvelope, "failed", message)
        fun skipped(message: String) = ExportSectionResult(null, "skipped", message)
    }
}

internal object AccountExportAssembler {

    fun assemble(
        uid: String,
        server: String,
        exportedAt: String,
        sections: Map<String, ExportSectionResult>
    ): JSONObject {
        val root = JSONObject()
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
            sectionStatuses.put(name, status)
        }

        val hasFailure = sections.values.any {
            it.status == "failed" || it.status == "partial"
        }
        val hasUsableSection = sections.values.any {
            it.status == "success" || (it.status == "partial" && it.data != null)
        }
        val overallStatus = when {
            !hasFailure -> "complete"
            hasUsableSection -> "partial"
            else -> "failed"
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
