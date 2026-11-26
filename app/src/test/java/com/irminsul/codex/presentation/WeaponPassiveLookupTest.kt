package com.irminsul.codex.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WeaponPassiveLookupTest {
    @Test
    fun readsPassiveDescriptionForEquippedRefinement() {
        val result = WeaponPassiveLookup.parse(
            """{
                "effectName":"Reckless Cinnabar",
                "r1":{"description":"R1 effect"},
                "r2":{"description":"R2 effect"},
                "r3":{"description":"R3 full passive description"}
            }""",
            3
        )

        assertNotNull(result)
        assertEquals("Reckless Cinnabar", result?.name)
        assertEquals("R3 full passive description", result?.description)
    }

    @Test
    fun acceptsWrappedGenshinDbApiResults() {
        val result = WeaponPassiveLookup.parse(
            """{"result":{"effectName":"Example","r5":{"description":"Refinement five effect"}}}""",
            5
        )

        assertEquals("Refinement five effect", result?.description)
    }

    @Test
    fun returnsNullWhenWeaponHasNoPassiveForSelectedRank() {
        assertEquals(
            null,
            WeaponPassiveLookup.parse("""{"effectName":"","r1":{}}""", 1)
        )
    }
}
