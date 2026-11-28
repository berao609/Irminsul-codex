package com.irminsul.codex.presentation

import com.irminsul.codex.domain.model.CharacterSnapshot

/**
 * Stable first-release ordering for playable characters, oldest release first.
 * Characters released in the same update are alphabetized; unknown names remain at the end.
 */
private val releaseGroups = listOf(
    listOf("Amber", "Barbara", "Beidou", "Bennett", "Chongyun", "Diluc", "Fischl", "Jean", "Kaeya", "Keqing", "Klee", "Lisa", "Mona", "Ningguang", "Noelle", "Qiqi", "Razor", "Sucrose", "Traveler", "Venti", "Xiangling", "Xingqiu"),
    listOf("Diona", "Tartaglia", "Xinyan", "Zhongli"),
    listOf("Albedo", "Ganyu"),
    listOf("Hu Tao", "Xiao"),
    listOf("Rosaria"),
    listOf("Eula", "Yanfei"),
    listOf("Kaedehara Kazuha"),
    listOf("Kamisato Ayaka", "Sayu", "Yoimiya"),
    listOf("Aloy", "Kujou Sara", "Raiden Shogun", "Sangonomiya Kokomi"),
    listOf("Thoma"),
    listOf("Arataki Itto", "Gorou"),
    listOf("Shenhe", "Yun Jin"),
    listOf("Yae Miko"),
    listOf("Kamisato Ayato"),
    listOf("Kuki Shinobu", "Yelan"),
    listOf("Shikanoin Heizou"),
    listOf("Collei", "Dori", "Tighnari"),
    listOf("Candace", "Cyno", "Nilou"),
    listOf("Layla", "Nahida"),
    listOf("Faruzan", "Wanderer"),
    listOf("Alhaitham", "Yaoyao"),
    listOf("Dehya", "Mika"),
    listOf("Baizhu", "Kaveh"),
    listOf("Kirara"),
    listOf("Freminet", "Lynette", "Lyney"),
    listOf("Neuvillette", "Wriothesley"),
    listOf("Charlotte", "Furina"),
    listOf("Chevreuse", "Navia"),
    listOf("Gaming", "Xianyun"),
    listOf("Chiori"),
    listOf("Arlecchino"),
    listOf("Clorinde", "Sethos", "Sigewinne"),
    listOf("Emilie"),
    listOf("Kachina", "Kinich", "Mualani"),
    listOf("Xilonen"),
    listOf("Chasca", "Ororon"),
    listOf("Citlali", "Lan Yan", "Mavuika"),
    listOf("Yumemizuki Mizuki"),
    listOf("Iansan", "Varesa"),
    listOf("Escoffier", "Ifa"),
    listOf("Dahlia", "Skirk"),
    listOf("Ineffa"),
    listOf("Aino", "Flins", "Lauma"),
    listOf("Nefer"),
    listOf("Durin", "Jahoda"),
    listOf("Columbina", "Illuga", "Zibai"),
    listOf("Varka"),
    listOf("Linnea"),
    listOf("Lohen", "Nicole", "Prune"),
    listOf("Sandrone"),
    listOf("Alyosha", "Odette"),
    listOf("Vesna", "Vodyanitsa"),
    listOf("Mitya", "Valeriy")
)

private val releaseRankByName = releaseGroups
    .flatMapIndexed { releaseIndex, names -> names.map { it.lowercase() to releaseIndex } }
    .toMap()

fun sortCharactersByRelease(characters: List<CharacterSnapshot>): List<CharacterSnapshot> =
    characters.sortedWith(
        compareBy<CharacterSnapshot> { character ->
            releaseRankByName[character.name?.trim()?.lowercase()] ?: Int.MAX_VALUE
        }.thenBy { it.name?.lowercase().orEmpty() }
            .thenBy { it.id }
    )
