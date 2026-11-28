package com.irminsul.codex.data.catalogue

/**
 * Calculates account-wide remaining material quantities.
 *
 * A null owned quantity means inventory is unknown/not entered; zero is an
 * explicit inventory value and must not be inferred from missing preferences.
 */
object EnhancementMaterialInventoryCalculator {
    fun remainingQuantity(requiredQuantity: Long, ownedQuantity: Long?): Long? {
        require(requiredQuantity >= 0L) { "Required quantity cannot be negative." }
        require(ownedQuantity == null || ownedQuantity >= 0L) {
            "Owned quantity cannot be negative."
        }
        return ownedQuantity?.let { (requiredQuantity - it).coerceAtLeast(0L) }
    }
}
