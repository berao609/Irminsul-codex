package com.irminsul.codex.presentation

import com.irminsul.codex.domain.model.CharacterSnapshot
import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.WeaponSnapshot

internal data class WeaponRosterEntry(
    val weapon: WeaponSnapshot,
    val equippedBy: List<String>
)

internal object WeaponRosterRules {
    fun entries(characters: List<CharacterSnapshot>): List<WeaponRosterEntry> {
        val equipped = characters.mapNotNull { character ->
            val weapon = when (val value = character.weapon) {
                is DataValue.Available -> value.value
                is DataValue.Stale -> value.value
                else -> null
            } ?: return@mapNotNull null
            weapon to (character.name ?: "Character #${character.id}")
        }
        return equipped.groupBy({ it.first.id }, { it })
            .values
            .map { matches ->
                val first = matches.first()
                WeaponRosterEntry(
                    weapon = first.first,
                    equippedBy = matches.map { it.second }.distinct()
                )
            }
    }

    fun filter(
        entries: List<WeaponRosterEntry>,
        rarity: Int?,
        weaponType: String?
    ): List<WeaponRosterEntry> = entries.filter { entry ->
        (rarity == null || entry.weapon.rarity == rarity) &&
            (weaponType == null || (entry.weapon.typeName?.takeIf(String::isNotBlank) ?: "Unknown") == weaponType)
    }
}
