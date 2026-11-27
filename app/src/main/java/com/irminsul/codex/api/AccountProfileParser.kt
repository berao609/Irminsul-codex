package com.irminsul.codex.api

import org.json.JSONObject

internal object AccountProfileParser {
    fun parse(uid: String, card: JSONObject, avatarUrl: String?): AccountProfile {
        return AccountProfile(
            nickname = card.optString("nickname").takeIf { it.isNotBlank() },
            uid = uid.trim(),
            serverName = card.optString("region_name").takeIf { it.isNotBlank() }
                ?: card.optString("region").takeIf { it.isNotBlank() },
            avatarUrl = avatarUrl?.takeIf { it.startsWith("https://") }
        )
    }
}
