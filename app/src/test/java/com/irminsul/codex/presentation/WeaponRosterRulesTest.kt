package com.irminsul.codex.presentation

import com.irminsul.codex.domain.model.CharacterSnapshot
import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.WeaponSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class WeaponRosterRulesTest {
    @Test
    fun deduplicatesEquippedWeaponsAndRetainsEquippedCharacterNames() {
        val sharedWeapon = weapon(id = 10, name = "Shared Blade", rarity = 4, typeName = "Sword")
        val characters = listOf(
            character(id = 1, name = "Amber", weapon = DataValue.Available(sharedWeapon, null)),
            character(id = 2, name = "Kaeya", weapon = DataValue.Stale(sharedWeapon, java.time.Instant.EPOCH)),
            character(id = 3, name = "Lisa", weapon = DataValue.Missing)
        )

        val result = WeaponRosterRules.entries(characters)

        assertEquals(1, result.size)
        assertEquals(10, result.single().weapon.id)
        assertEquals(listOf("Amber", "Kaeya"), result.single().equippedBy)
    }

    @Test
    fun filtersByRarityAndWeaponTypeWithoutChangingSourceEntries() {
        val entries = WeaponRosterRules.entries(
            listOf(
                character(1, "Amber", DataValue.Available(weapon(10, "Bow", 4, "Bow"), null)),
                character(2, "Kaeya", DataValue.Available(weapon(11, "Sword", 5, "Sword"), null)),
                character(3, "Lisa", DataValue.Available(weapon(12, "Catalyst", 4, "Catalyst"), null))
            )
        )

        assertEquals(listOf(10, 12), WeaponRosterRules.filter(entries, rarity = 4, weaponType = null).map { it.weapon.id })
        assertEquals(listOf(11), WeaponRosterRules.filter(entries, rarity = null, weaponType = "Sword").map { it.weapon.id })
        assertEquals(3, entries.size)
    }

    private fun character(id: Int, name: String, weapon: DataValue<WeaponSnapshot>) =
        CharacterSnapshot(
            id = id,
            name = name,
            level = 1,
            rarity = 4,
            element = null,
            friendshipLevel = null,
            activeConstellationCount = null,
            weapon = weapon
        )

    private fun weapon(id: Int, name: String, rarity: Int, typeName: String) =
        WeaponSnapshot(
            id = id,
            name = name,
            icon = null,
            type = null,
            rarity = rarity,
            level = 1,
            ascensionLevel = 0,
            typeName = typeName,
            description = null,
            refinementLevel = 1,
            mainProperty = null,
            subProperty = null
        )
}
