package com.irminsul.codex.api

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountProfileParserTest {

    @Test
    fun mapsGameIdentityAndCommunityAvatar() {
        val profile = AccountProfileParser.parse(
            uid = " 800123456 ",
            card = JSONObject(
                """{"nickname":"Traveler","region":"os_asia","region_name":"Asia"}"""
            ),
            avatarUrl = "https://img-static.hoyolab.com/avatar.png"
        )

        assertEquals("Traveler", profile.nickname)
        assertEquals("800123456", profile.uid)
        assertEquals("Asia", profile.serverName)
        assertEquals("https://img-static.hoyolab.com/avatar.png", profile.avatarUrl)
    }

    @Test
    fun preservesMissingOptionalProfileFields() {
        val profile = AccountProfileParser.parse(
            uid = "800123456",
            card = JSONObject("""{"nickname":"","region":"os_asia"}"""),
            avatarUrl = "http://insecure.example/avatar.png"
        )

        assertNull(profile.nickname)
        assertEquals("os_asia", profile.serverName)
        assertNull(profile.avatarUrl)
    }
}
