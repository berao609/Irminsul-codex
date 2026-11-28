package com.irminsul.codex.data.catalogue

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BundledEnhancementCatalogueRepositoryTest {
    @Test
    fun characterCatalogueIncludesAllFourAndFiveStarEntriesOnly() {
        val chunk = JSONObject(
            """{"schemaVersion":1,"gameVersion":"7.2","sourceCommit":"8e5badd60b5d894284ccc927a56c9683529d2592","characters":[{"slug":"four","character":{"id":1001,"name":"Four Star","rarity":4}},{"slug":"five","character":{"id":1002,"name":"Five Star","rarity":5}},{"slug":"three","character":{"id":1003,"name":"Three Star","rarity":3}}]}"""
        )

        val result = BundledEnhancementCatalogueRepository.parseCharacters(listOf(chunk))

        assertEquals(listOf("1002", "1001"), result.map { it.id })
        assertTrue(result.all { it.rarity == 4 || it.rarity == 5 })
        assertTrue(result.all { it.subjectType == EnhancementSubjectType.CHARACTER })
    }

    @Test
    fun characterCatalogueRejectsDuplicateIdsAcrossChunks() {
        val record = """{"slug":"same","character":{"id":1001,"name":"Duplicate","rarity":4}}"""
        val first = characterChunk(record)
        val second = characterChunk(record)

        val failure = runCatching {
            BundledEnhancementCatalogueRepository.parseCharacters(listOf(first, second))
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
    }

    @Test
    fun weaponCatalogueIncludesAllFourAndFiveStarEntriesOnly() {
        val root = JSONObject(
            """{"data":{"English":{"weapons":{"bow":{"id":15501,"name":"Four Star Bow","rarity":4,"weaponText":"Bow"},"sword":{"id":15502,"name":"Five Star Sword","rarity":5,"weaponText":"Sword"},"three":{"id":13101,"name":"Three Star Weapon","rarity":3,"weaponText":"Claymore"}}}}}"""
        )

        val result = BundledEnhancementCatalogueRepository.parseWeapons(root)

        assertEquals(listOf("15502", "15501"), result.map { it.id })
        assertEquals(listOf("Sword", "Bow"), result.map { it.detail })
        assertTrue(result.all { it.rarity == 4 || it.rarity == 5 })
        assertTrue(result.all { it.subjectType == EnhancementSubjectType.WEAPON })
    }

    private fun characterChunk(record: String) = JSONObject(
        """{"schemaVersion":1,"gameVersion":"7.2","sourceCommit":"8e5badd60b5d894284ccc927a56c9683529d2592","characters":[$record]}"""
    )
}
