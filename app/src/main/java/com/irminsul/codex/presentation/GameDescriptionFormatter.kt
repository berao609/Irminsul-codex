package com.irminsul.codex.presentation

/**
 * Converts HoYoLAB/Genshin description markup into readable plain text.
 * Link labels remain visible; only their surrounding metadata tokens are removed.
 */
internal fun formatGameDescription(raw: String): String {
    return raw
        .replace("\\r\\n", "\\n")
        .replace("\\n", "\n")
        .replace(Regex("<color=[^>]*>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("</color>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<[^>]+>"), "")
        .replace(Regex("""\\{LINK#[^}]+\\}"""), "")
        .replace(Regex("""\\{/LINK\\}"""), "")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}
