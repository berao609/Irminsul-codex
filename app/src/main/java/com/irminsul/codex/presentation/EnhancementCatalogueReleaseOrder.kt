package com.irminsul.codex.presentation

import com.irminsul.codex.data.catalogue.EnhancementCatalogueEntry
import com.irminsul.codex.data.catalogue.EnhancementSubjectType

/**
 * First-release dates for the 4-star and 5-star weapon catalogue.
 * Source: GachaTracker Genshin weapon database (release dates), checked 2026-11-29.
 * Unknown dates are deliberately sorted last rather than guessed from item IDs.
 */
private val weaponReleaseDateByName: Map<String, String> = buildMap {
    fun add(date: String, names: String) {
        names.split("|").forEach { put(it.trim().lowercase(), date) }
    }
    add("2020-09-27", "Amos' Bow|Aquila Favonia|Lost Prayer to the Sacred Winds|Primordial Jade Winged-Spear|Skyward Atlas|Skyward Blade|Skyward Harp|Skyward Pride|Skyward Spine|Wolf's Gravestone|Blackcliff Pole|Compound Bow|Crescent Pike|Deathmatch|Dragon's Bane|Eye of Perception|Favonius Codex|Favonius Greatsword|Favonius Lance|Favonius Sword|Favonius Warbow|Iron Sting|Lion's Roar|Mappa Mare|Prototype Amber|Prototype Archaic|Prototype Crescent|Prototype Rancour|Prototype Starglitter|Rainslasher|Royal Bow|Royal Greatsword|Royal Grimoire|Royal Longsword|Rust|Sacrificial Bow|Sacrificial Fragments|Sacrificial Greatsword|Sacrificial Sword|Serpent Spine|Solar Pearl|Sword of Descension|The Bell|The Black Sword|The Flute|The Stringless|The Viridescent Hunt|The Widsith|Whiteblind")
    add("2020-11-10", "Memory of Dust")
    add("2020-10-31", "Blackcliff Agate|Blackcliff Longsword|Blackcliff Slasher|Blackcliff Warbow")
    add("2020-11-30", "The Unforged|Vortex Vanquisher|Royal Spear")
    add("2020-12-22", "Summit Shaper|Dragonspine Spear|Frostbearer|Snow-Tombed Starsilver")
    add("2021-02-02", "Primordial Jade Cutter")
    add("2021-02-22", "Staff of Homa|Lithic Blade|Lithic Spear")
    add("2021-03-16", "Elegy for the End|The Alley Flash|Wine and Song")
    add("2021-03-18", "Windblume Ode")
    add("2021-04-05", "Alley Hunter")
    add("2021-05-17", "Song of Broken Pines")
    add("2021-06-08", "Mitternachts Waltz")
    add("2021-06-16", "Dodoco Tales")
    add("2021-06-28", "Freedom-Sworn")
    add("2021-07-20", "Mistsplitter Reforged|Amenoma Kageuchi|Hakushin Ring|Hamayumi|Katsuragikiri Nagamasa|Kitain Cross Spear")
    add("2021-08-09", "Thundering Pulse")
    add("2021-08-31", "Engulfing Lightning|The Catch|Predator")
    add("2021-09-20", "Everlasting Moonglow")
    add("2021-09-26", "Luxurious Sea-Lord")
    add("2021-10-12", "Polar Star|Akuoumaru")
    add("2021-11-01", "Mouun's Moon|Wavebreaker's Fin")
    add("2021-11-24", "Cinnabar Spindle")
    add("2021-12-13", "Redhorn Stonethresher")
    add("2022-01-04", "Calamity Queller")
    add("2022-02-15", "Kagura's Verity|Oathsworn Eye")
    add("2022-03-29", "Haran Geppaku Futsu")
    add("2022-05-30", "Aqua Simulacra|Fading Twilight")
    add("2022-07-12", "Kagotsurube Isshin|Prized Isshin Blade (Awakened)|Prized Isshin Blade (Shattered)")
    add("2022-08-23", "Hunter's Path|End of the Line|Forest Regalia|Fruit of Fulfillment|King's Squire|Moonpiercer|Sapwood Blade")
    add("2022-09-27", "Staff of the Scarlet Sands|Makhaira Aquamarine")
    add("2022-09-28", "Missive Windspear")
    add("2022-10-13", "Key of Khaj-Nisut|Wandering Evenstar|Xiphos' Moonlight")
    add("2022-11-01", "A Thousand Floating Dreams")
    add("2022-12-06", "Tulaytullah's Remembrance")
    add("2022-12-14", "Toukabou Shigure")
    add("2023-01-17", "Light of Foliar Incision")
    add("2023-02-28", "Beacon of the Reed Sea")
    add("2023-03-02", "Mailed Flower")
    add("2023-05-01", "Jadefall's Splendor")
    add("2023-05-24", "Ibis Piercer")
    add("2023-08-15", "The First Great Magic|Ballad of the Fjords|Finale of the Deep|Fleuve Cendre Ferryman|Flowing Purity|Rightful Reward|Scion of the Blazing Sun|Song of Stillness|Talking Stick|Tidal Shadow|Wolf-Fang|Sacrificial Jade")
    add("2023-09-26", "Tome of the Eternal Flow|Portable Power Saw|The Dockhand's Assistant")
    add("2023-10-11", "Ballad of the Boundless Blue")
    add("2023-10-16", "Cashflow Supervision|Prospector's Drill|Range Gauge")
    add("2023-11-07", "Splendor of Tranquil Waters|Sword of Narzissenkreuz|Sword of Narzissenkreuz (Quest)")
    add("2023-12-19", "Verdict")
    add("2023-12-20", "Ultimate Overlord's Mega Magic Sword")
    add("2024-01-30", "Crane's Echoing Call")
    add("2024-03-12", "Uraku Misugiri")
    add("2024-03-13", "Dialogues of the Desert Sages")
    add("2024-04-23", "Crimson Moon's Semblance")
    add("2024-06-04", "Absolution")
    add("2024-06-06", "Cloudforged")
    add("2024-06-24", "Silvershower Heartstrings")
    add("2024-08-05", "Lumidouce Elegy")
    add("2024-08-27", "Surf's Up|Ash-Graven Drinking Horn|Chain Breaker|Earth Shaker|Flute of Ezpitzal|Footprint of the Rainbow|Ring of Yaxche")
    add("2024-09-16", "Fang of the Mountain King")
    add("2024-10-08", "Peak Patrol Song|Mountain-Bracing Bolt|Sturdy Bone")
    add("2024-10-28", "Fruitful Hook")
    add("2024-11-19", "Astral Vulture's Crimson Plumage|Flower-Wreathed Feathers|Waveriding Whirl")
    add("2024-11-27", "Calamity of Eshu")
    add("2024-12-31", "A Thousand Blazing Suns|Starcaller's Watch")
    add("2025-02-11", "Sunny Morning Sleep-In")
    add("2025-03-25", "Vivid Notions")
    add("2025-05-06", "Symphonist of Scents")
    add("2025-05-08", "Sequence of Solitude")
    add("2025-06-17", "Azurelight")
    add("2025-07-29", "Fractured Halo|Flame-Forged Insight")
    add("2025-09-09", "Nightweaver's Looking Glass|Blackmarrow Lantern|Master Key|Moonweaver's Dawn|Prospector's Shovel|Serenity's Call|Snare Hook")
    add("2025-09-11", "Etherlight Spindlelute")
    add("2025-09-29", "Bloodsoaked Ruins")
    add("2025-10-21", "Reliquary of Truth|Dawning Frost|Sacrificer's Staff")
    add("2025-12-02", "Athame Artis|The Daybreak Chronicles")
    add("2025-12-18", "Rainbow Serpent's Rain Bow")
    add("2026-01-13", "Nocturne's Curtain Call")
    add("2026-02-24", "Gest of the Mighty Wolf")
    add("2026-04-07", "Golden Frostbound Oath")
    add("2026-05-19", "Angelos' Heptades")
    add("2026-06-08", "Disaster and Remorse")
    add("2026-06-30", "A Teaspoon of Transcendence")
    add("2026-08-11", "Exaiphanes Blade|Whitelake Frostfeather|Blade of Atonement|Clash of Kings|Covenant of Frost and Snow|Echoes of the Heart|Emberwell|Forged by the Golden Melody|Frostbreath|Heretic's Molten Blade|Jade Vista|Song of the Vigil")
    add("2026-09-22", "Beyond the Chrysalis|Breezeborne Refrain|Hymn of the Maelstrom|New Bough|Winter's Heavy Heart")
    add("2026-09-23", "Silver Light")
}

fun sortEnhancementCatalogueByRelease(entries: List<EnhancementCatalogueEntry>): List<EnhancementCatalogueEntry> =
    entries.sortedWith(
        compareBy<EnhancementCatalogueEntry> { entry ->
            when (entry.subjectType) {
                EnhancementSubjectType.CHARACTER -> characterReleaseIndex(entry.name) == null
                EnhancementSubjectType.WEAPON -> weaponReleaseDateByName[entry.name.trim().lowercase()] == null
            }
        }.thenByDescending { entry ->
            when (entry.subjectType) {
                EnhancementSubjectType.CHARACTER -> characterReleaseIndex(entry.name) ?: Int.MIN_VALUE
                EnhancementSubjectType.WEAPON -> weaponReleaseDateByName[entry.name.trim().lowercase()]
                    ?.replace("-", "")?.toIntOrNull() ?: Int.MIN_VALUE
            }
        }.thenBy { it.name.lowercase() }.thenBy { it.id }
    )
