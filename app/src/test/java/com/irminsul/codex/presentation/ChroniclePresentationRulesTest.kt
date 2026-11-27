package com.irminsul.codex.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChroniclePresentationRulesTest {
    @Test fun hidesTechnicalFieldNamesButKeepsAccountDetails() {
        listOf("id", "entry_id", "avatar_url", "game_head_icon", "image_path", "source").forEach {
            assertFalse("$it should be hidden", ChroniclePresentationRules.shouldDisplayField(it))
        }
        listOf("role", "nickname", "region", "level", "exploration_percentage", "achievement_number").forEach {
            assertTrue("$it should remain visible", ChroniclePresentationRules.shouldDisplayField(it))
        }
    }

    @Test fun hidesRemoteUrlsAndArtworkFilenames() {
        listOf(
            "https://act-webstatic.hoyoverse.com/game/portrait.png",
            "http://example.com/avatar.jpg",
            "//static.example.com/icon.webp",
            "avatar_10000002.png"
        ).forEach {
            assertFalse("$it should be hidden", ChroniclePresentationRules.shouldDisplayValue(it))
        }
        assertTrue(ChroniclePresentationRules.shouldDisplayValue("Raiden Shogun"))
        assertTrue(ChroniclePresentationRules.shouldDisplayValue("Asia"))
        assertTrue(ChroniclePresentationRules.shouldDisplayValue("90"))
        assertTrue(ChroniclePresentationRules.shouldDisplayValue(""))
    }
}
