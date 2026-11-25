package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.*

/**
 * Canonical stat semantics for known Genshin FightProp identifiers.
 *
 * Unknown identifiers are intentionally left unresolved. Source stat names take
 * precedence only when they are recognized; if a recognized name and recognized
 * numeric property type disagree, the result is a conflict rather than a guess.
 */
object StatPropertyCatalog {
    private fun definition(
        id: String,
        name: String,
        category: StatPropertyCategory,
        kind: StatValueKind,
        element: String? = null
    ) = StatPropertyDefinition(id, name, category, kind, element)

    private val byName: Map<String, StatPropertyDefinition> = listOf(
        definition("FIGHT_PROP_BASE_HP", "Base HP", StatPropertyCategory.CORE_ATTRIBUTE, StatValueKind.FLAT),
        definition("FIGHT_PROP_HP", "HP", StatPropertyCategory.CORE_ATTRIBUTE, StatValueKind.FLAT),
        definition("FIGHT_PROP_HP_PERCENT", "HP%", StatPropertyCategory.CORE_ATTRIBUTE, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_BASE_ATTACK", "Base ATK", StatPropertyCategory.CORE_ATTRIBUTE, StatValueKind.FLAT),
        definition("FIGHT_PROP_ATTACK", "ATK", StatPropertyCategory.CORE_ATTRIBUTE, StatValueKind.FLAT),
        definition("FIGHT_PROP_ATTACK_PERCENT", "ATK%", StatPropertyCategory.CORE_ATTRIBUTE, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_BASE_DEFENSE", "Base DEF", StatPropertyCategory.CORE_ATTRIBUTE, StatValueKind.FLAT),
        definition("FIGHT_PROP_DEFENSE", "DEF", StatPropertyCategory.CORE_ATTRIBUTE, StatValueKind.FLAT),
        definition("FIGHT_PROP_DEFENSE_PERCENT", "DEF%", StatPropertyCategory.CORE_ATTRIBUTE, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_BASE_SPEED", "Base Movement Speed", StatPropertyCategory.UTILITY, StatValueKind.FLAT),
        definition("FIGHT_PROP_SPEED_PERCENT", "Movement Speed%", StatPropertyCategory.UTILITY, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_CRITICAL", "CRIT Rate", StatPropertyCategory.OFFENSIVE, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_ANTI_CRITICAL", "CRIT Rate Resistance", StatPropertyCategory.DEFENSIVE, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_CRITICAL_HURT", "CRIT DMG", StatPropertyCategory.OFFENSIVE, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_CHARGE_EFFICIENCY", "Energy Recharge", StatPropertyCategory.ENERGY, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_ADD_HURT", "All DMG Bonus", StatPropertyCategory.OFFENSIVE, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_SUB_HURT", "All DMG Resistance", StatPropertyCategory.DEFENSIVE, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_HEAL_ADD", "Healing Bonus", StatPropertyCategory.HEALING, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_HEALED_ADD", "Incoming Healing Bonus", StatPropertyCategory.HEALING, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_ELEMENT_MASTERY", "Elemental Mastery", StatPropertyCategory.OFFENSIVE, StatValueKind.FLAT),
        definition("FIGHT_PROP_PHYSICAL_SUB_HURT", "Physical RES", StatPropertyCategory.DEFENSIVE, StatValueKind.PERCENTAGE, "Physical"),
        definition("FIGHT_PROP_PHYSICAL_ADD_HURT", "Physical DMG Bonus", StatPropertyCategory.OFFENSIVE, StatValueKind.PERCENTAGE, "Physical"),
        definition("FIGHT_PROP_DEFENCE_IGNORE_RATIO", "DEF Ignore", StatPropertyCategory.OFFENSIVE, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_DEFENCE_IGNORE_DELTA", "Flat DEF Ignore", StatPropertyCategory.OFFENSIVE, StatValueKind.FLAT),
        definition("FIGHT_PROP_FIRE_ADD_HURT", "Pyro DMG Bonus", StatPropertyCategory.ELEMENTAL, StatValueKind.PERCENTAGE, "Pyro"),
        definition("FIGHT_PROP_ELEC_ADD_HURT", "Electro DMG Bonus", StatPropertyCategory.ELEMENTAL, StatValueKind.PERCENTAGE, "Electro"),
        definition("FIGHT_PROP_WATER_ADD_HURT", "Hydro DMG Bonus", StatPropertyCategory.ELEMENTAL, StatValueKind.PERCENTAGE, "Hydro"),
        definition("FIGHT_PROP_GRASS_ADD_HURT", "Dendro DMG Bonus", StatPropertyCategory.ELEMENTAL, StatValueKind.PERCENTAGE, "Dendro"),
        definition("FIGHT_PROP_WIND_ADD_HURT", "Anemo DMG Bonus", StatPropertyCategory.ELEMENTAL, StatValueKind.PERCENTAGE, "Anemo"),
        definition("FIGHT_PROP_ROCK_ADD_HURT", "Geo DMG Bonus", StatPropertyCategory.ELEMENTAL, StatValueKind.PERCENTAGE, "Geo"),
        definition("FIGHT_PROP_ICE_ADD_HURT", "Cryo DMG Bonus", StatPropertyCategory.ELEMENTAL, StatValueKind.PERCENTAGE, "Cryo"),
        definition("FIGHT_PROP_FIRE_SUB_HURT", "Pyro RES", StatPropertyCategory.DEFENSIVE, StatValueKind.PERCENTAGE, "Pyro"),
        definition("FIGHT_PROP_ELEC_SUB_HURT", "Electro RES", StatPropertyCategory.DEFENSIVE, StatValueKind.PERCENTAGE, "Electro"),
        definition("FIGHT_PROP_WATER_SUB_HURT", "Hydro RES", StatPropertyCategory.DEFENSIVE, StatValueKind.PERCENTAGE, "Hydro"),
        definition("FIGHT_PROP_GRASS_SUB_HURT", "Dendro RES", StatPropertyCategory.DEFENSIVE, StatValueKind.PERCENTAGE, "Dendro"),
        definition("FIGHT_PROP_WIND_SUB_HURT", "Anemo RES", StatPropertyCategory.DEFENSIVE, StatValueKind.PERCENTAGE, "Anemo"),
        definition("FIGHT_PROP_ROCK_SUB_HURT", "Geo RES", StatPropertyCategory.DEFENSIVE, StatValueKind.PERCENTAGE, "Geo"),
        definition("FIGHT_PROP_ICE_SUB_HURT", "Cryo RES", StatPropertyCategory.DEFENSIVE, StatValueKind.PERCENTAGE, "Cryo"),
        definition("FIGHT_PROP_HIT_HEAD_ADD_HURT", "Weak Point DMG Bonus", StatPropertyCategory.OFFENSIVE, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_EFFECT_HIT", "Effect Hit Rate", StatPropertyCategory.CONTROL, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_EFFECT_RESIST", "Effect RES", StatPropertyCategory.CONTROL, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_FREEZE_RESIST", "Freeze RES", StatPropertyCategory.CONTROL, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_DIZZY_RESIST", "Stagger RES", StatPropertyCategory.CONTROL, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_FREEZE_SHORTEN", "Freeze Duration Reduction", StatPropertyCategory.CONTROL, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_DIZZY_SHORTEN", "Stagger Duration Reduction", StatPropertyCategory.CONTROL, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_SKILL_CD_MINUS_RATIO", "Skill Cooldown Reduction", StatPropertyCategory.UTILITY, StatValueKind.PERCENTAGE),
        definition("FIGHT_PROP_SHIELD_COST_MINUS_RATIO", "Shield Damage Reduction", StatPropertyCategory.DEFENSIVE, StatValueKind.PERCENTAGE)
    ).associateBy { it.canonicalId }

    // FightPropType numeric values used by the public game-data model.
    private val byType: Map<Int, StatPropertyDefinition> = mapOf(
        1 to "FIGHT_PROP_BASE_HP", 2 to "FIGHT_PROP_HP", 3 to "FIGHT_PROP_HP_PERCENT",
        4 to "FIGHT_PROP_BASE_ATTACK", 5 to "FIGHT_PROP_ATTACK", 6 to "FIGHT_PROP_ATTACK_PERCENT",
        7 to "FIGHT_PROP_BASE_DEFENSE", 8 to "FIGHT_PROP_DEFENSE", 9 to "FIGHT_PROP_DEFENSE_PERCENT",
        10 to "FIGHT_PROP_BASE_SPEED", 11 to "FIGHT_PROP_SPEED_PERCENT",
        20 to "FIGHT_PROP_CRITICAL", 21 to "FIGHT_PROP_ANTI_CRITICAL", 22 to "FIGHT_PROP_CRITICAL_HURT",
        23 to "FIGHT_PROP_CHARGE_EFFICIENCY", 24 to "FIGHT_PROP_ADD_HURT", 25 to "FIGHT_PROP_SUB_HURT",
        26 to "FIGHT_PROP_HEAL_ADD", 27 to "FIGHT_PROP_HEALED_ADD", 28 to "FIGHT_PROP_ELEMENT_MASTERY",
        29 to "FIGHT_PROP_PHYSICAL_SUB_HURT", 30 to "FIGHT_PROP_PHYSICAL_ADD_HURT",
        31 to "FIGHT_PROP_DEFENCE_IGNORE_RATIO", 32 to "FIGHT_PROP_DEFENCE_IGNORE_DELTA",
        40 to "FIGHT_PROP_FIRE_ADD_HURT", 41 to "FIGHT_PROP_ELEC_ADD_HURT",
        42 to "FIGHT_PROP_WATER_ADD_HURT", 43 to "FIGHT_PROP_GRASS_ADD_HURT",
        44 to "FIGHT_PROP_WIND_ADD_HURT", 45 to "FIGHT_PROP_ROCK_ADD_HURT", 46 to "FIGHT_PROP_ICE_ADD_HURT",
        47 to "FIGHT_PROP_HIT_HEAD_ADD_HURT",
        50 to "FIGHT_PROP_FIRE_SUB_HURT", 51 to "FIGHT_PROP_ELEC_SUB_HURT",
        52 to "FIGHT_PROP_WATER_SUB_HURT", 53 to "FIGHT_PROP_GRASS_SUB_HURT",
        54 to "FIGHT_PROP_WIND_SUB_HURT", 55 to "FIGHT_PROP_ROCK_SUB_HURT", 56 to "FIGHT_PROP_ICE_SUB_HURT",
        60 to "FIGHT_PROP_EFFECT_HIT", 61 to "FIGHT_PROP_EFFECT_RESIST",
        62 to "FIGHT_PROP_FREEZE_RESIST", 64 to "FIGHT_PROP_DIZZY_RESIST",
        65 to "FIGHT_PROP_FREEZE_SHORTEN", 67 to "FIGHT_PROP_DIZZY_SHORTEN",
        80 to "FIGHT_PROP_SKILL_CD_MINUS_RATIO", 81 to "FIGHT_PROP_SHIELD_COST_MINUS_RATIO"
    ).mapNotNull { (type, key) -> byName[key]?.let { type to it } }.toMap()

    private val aliases = mapOf(
        "HP" to "FIGHT_PROP_HP",
        "HP%" to "FIGHT_PROP_HP_PERCENT",
        "ATK" to "FIGHT_PROP_ATTACK",
        "ATK%" to "FIGHT_PROP_ATTACK_PERCENT",
        "DEF" to "FIGHT_PROP_DEFENSE",
        "DEF%" to "FIGHT_PROP_DEFENSE_PERCENT",
        "CRIT RATE" to "FIGHT_PROP_CRITICAL",
        "CRIT DMG" to "FIGHT_PROP_CRITICAL_HURT",
        "ER" to "FIGHT_PROP_CHARGE_EFFICIENCY",
        "EM" to "FIGHT_PROP_ELEMENT_MASTERY"
    )

    fun classify(characterId: Int, fieldPath: String, stat: StatValueSnapshot): StatPropertyClassification {
        val rawName = stat.statName?.trim()?.uppercase()
        val normalizedName = rawName?.let { aliases[it] ?: it }
        val bySourceName = normalizedName?.let(byName::get)
        val byPropertyType = stat.propertyType?.let(byType::get)

        return when {
            bySourceName != null && byPropertyType != null && bySourceName.canonicalId != byPropertyType.canonicalId ->
                StatPropertyClassification(characterId, fieldPath, stat.propertyType, stat.statName, null,
                    StatClassificationStatus.CONFLICT, StatClassificationSource.NONE)
            bySourceName != null ->
                StatPropertyClassification(characterId, fieldPath, stat.propertyType, stat.statName, bySourceName,
                    StatClassificationStatus.CLASSIFIED, StatClassificationSource.SOURCE_NAME)
            normalizedName != null && byPropertyType != null ->
                StatPropertyClassification(characterId, fieldPath, stat.propertyType, stat.statName, null,
                    StatClassificationStatus.CONFLICT, StatClassificationSource.NONE)
            byPropertyType != null ->
                StatPropertyClassification(characterId, fieldPath, stat.propertyType, stat.statName, byPropertyType,
                    StatClassificationStatus.CLASSIFIED, StatClassificationSource.PROPERTY_TYPE_ID)
            else ->
                StatPropertyClassification(characterId, fieldPath, stat.propertyType, stat.statName, null,
                    StatClassificationStatus.UNKNOWN, StatClassificationSource.NONE)
        }
    }
}
