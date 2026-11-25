package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class StatPropertyCatalogTest {
    private fun stat(type: Int? = null, name: String? = null) =
        StatValueSnapshot(type, name, null, null, null, null, null)

    @Test fun resolvesCanonicalSourcePropertyNames() {
        val result = StatPropertyCatalog.classify(1, "selectedProperties[0]", stat(3, "FIGHT_PROP_HP_PERCENT"))
        assertEquals(StatClassificationStatus.CLASSIFIED, result.status)
        assertEquals("FIGHT_PROP_HP_PERCENT", result.definition?.canonicalId)
        assertEquals(StatValueKind.PERCENTAGE, result.definition?.valueKind)
        assertEquals(StatPropertyCategory.CORE_ATTRIBUTE, result.definition?.category)
    }

    @Test fun resolvesKnownNumericFightPropertyTypeWhenNameIsMissing() {
        val result = StatPropertyCatalog.classify(1, "selectedProperties[0]", stat(23))
        assertEquals(StatClassificationStatus.CLASSIFIED, result.status)
        assertEquals("FIGHT_PROP_CHARGE_EFFICIENCY", result.definition?.canonicalId)
        assertEquals(StatClassificationSource.PROPERTY_TYPE_ID, result.resolvedBy)
    }

    @Test fun sourceNameAndNumericTypeMustAgree() {
        val result = StatPropertyCatalog.classify(1, "selectedProperties[0]", stat(3, "FIGHT_PROP_ATTACK_PERCENT"))
        assertEquals(StatClassificationStatus.CONFLICT, result.status)
        assertNull(result.definition)
    }

    @Test fun unknownFuturePropertyIsPreservedAsUnknown() {
        val result = StatPropertyCatalog.classify(1, "selectedProperties[0]", stat(987654, "FIGHT_PROP_FUTURE_STAT"))
        assertEquals(StatClassificationStatus.UNKNOWN, result.status)
        assertNull(result.definition)
        assertEquals("FIGHT_PROP_FUTURE_STAT", result.sourceStatName)
    }

    @Test fun knownSourceAliasesResolveToCanonicalIdentifiers() {
        val result = StatPropertyCatalog.classify(1, "stat", stat(null, "CRIT RATE"))
        assertEquals(StatClassificationStatus.CLASSIFIED, result.status)
        assertEquals("FIGHT_PROP_CRITICAL", result.definition?.canonicalId)
    }

    @Test fun elementalBonusAndResistanceHaveDifferentCategories() {
        val bonus = StatPropertyCatalog.classify(1, "bonus", stat(40))
        val resistance = StatPropertyCatalog.classify(1, "res", stat(50))
        assertEquals(StatPropertyCategory.ELEMENTAL, bonus.definition?.category)
        assertEquals(StatPropertyCategory.DEFENSIVE, resistance.definition?.category)
        assertEquals("Pyro", bonus.definition?.element)
        assertEquals("Pyro", resistance.definition?.element)
    }
}
