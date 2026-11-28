package com.irminsul.codex.presentation

internal enum class VisualsDestination(val title: String) {
    OVERVIEW("Battle Chronicles"),
    CHARACTER_ROSTER("All Characters"),
    CHARACTER_DETAILS("Character Details"),
    WEAPON_ROSTER("Weapon Roster"),
    WORLD_EXPLORATION("World Exploration"),
    SPIRAL_ABYSS("Spiral Abyss"),
    IMAGINARIUM_THEATER("Imaginarium Theater"),
    STYGIAN_ONSLAUGHT("Stygian Onslaught"),
    ACCOUNT_ANALYSIS("Account Analysis")
}

internal object VisualsNavigationStack {
    fun navigate(
        current: List<VisualsDestination>,
        destination: VisualsDestination
    ): List<VisualsDestination> =
        if (current.lastOrNull() == destination) current else current + destination

    fun back(current: List<VisualsDestination>): List<VisualsDestination> =
        if (current.size > 1) current.dropLast(1) else current
}
