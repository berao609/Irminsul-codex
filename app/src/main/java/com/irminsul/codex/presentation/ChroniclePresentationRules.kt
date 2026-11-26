package com.irminsul.codex.presentation

/**
 * Keeps source-only implementation details out of the user-facing Account Chronicle.
 * The original StructuredData remains untouched for analysis and JSON export.
 */
object ChroniclePresentationRules {
    private val technicalKeyFragments = listOf(
        "url", "icon", "image", "filepath", "filename", "sourcepath", "entryid"
    )

    fun shouldDisplayField(rawKey: String): Boolean {
        val key = rawKey.lowercase().replace(Regex("[^a-z0-9]"), "")
        if (key.isBlank()) return false
        if (key == "id" || key.endsWith("id")) return false
        return technicalKeyFragments.none(key::contains)
    }

    fun shouldDisplayValue(rawValue: String): Boolean {
        val value = rawValue.trim()
        if (value.isEmpty()) return true
        if (value.startsWith("https://", ignoreCase = true) ||
            value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("//") ||
            value.startsWith("data:", ignoreCase = true)
        ) return false
        return !value.matches(
            Regex("(?i).+\\.(png|jpe?g|webp|gif|svg|avif)(\\?.*)?")
        )
    }
}
