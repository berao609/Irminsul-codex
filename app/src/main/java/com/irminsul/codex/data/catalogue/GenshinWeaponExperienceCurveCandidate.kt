package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.model.KnowledgePermissionState
import com.irminsul.codex.domain.model.KnowledgeReviewState
import com.irminsul.codex.domain.model.KnowledgeSourceReference

/*
 * Adapted level-total tables:
 * - 5-Star Weapons: https://genshin-impact.fandom.com/wiki/Weapon_EXP/5-Star_Weapons
 * - 4-Star Weapons: https://genshin-impact.fandom.com/wiki/Weapon_EXP/4-Star_Weapons
 *
 * Publisher: Genshin Impact Wiki contributors.
 * Source text license: CC BY-SA 3.0
 * https://creativecommons.org/licenses/by-sa/3.0/
 * Changes: transcribed the Total EXP column into level-indexed Long lists.
 * This candidate data file must retain CC BY-SA 3.0 attribution/share-alike
 * terms and is not relicensed under the repository's MIT license.
 */

data class WeaponExperienceCurveCandidate(
    val rarity: Int,
    val cumulativeExperienceByLevel: List<Long>,
    val sourceReference: KnowledgeSourceReference,
    val sourceLicense: String,
    val sourceLicenseUrl: String,
    val retrievedOn: String,
    val reviewState: KnowledgeReviewState = KnowledgeReviewState.CANDIDATE,
    val permissionState: KnowledgePermissionState = KnowledgePermissionState.PENDING_REVIEW
)

/** Shared candidate curves. Only 4-star and 5-star weapons are supported. */
object GenshinWeaponExperienceCurveCandidate {
    private const val SOURCE_ID = "genshin-impact-wiki-weapon-exp"
    private const val LICENSE = "CC BY-SA 3.0"
    private const val LICENSE_URL = "https://creativecommons.org/licenses/by-sa/3.0/"

    val fourStar = WeaponExperienceCurveCandidate(
        rarity = 4,
        cumulativeExperienceByLevel = listOf(
            0L, 400L, 1_025L, 1_925L, 3_125L, 4_675L, 6_625L, 8_975L, 11_775L, 15_075L,
            18_875L, 23_225L, 28_150L, 33_675L, 39_825L, 46_625L, 54_125L, 62_325L, 71_275L, 81_000L,
            91_500L, 103_400L, 116_175L, 129_875L, 144_525L, 160_150L, 176_775L, 194_425L, 213_125L, 232_900L,
            253_800L, 275_825L, 299_025L, 323_400L, 349_000L, 375_825L, 403_925L, 433_325L, 464_050L, 496_125L,
            529_550L, 566_125L, 604_200L, 643_800L, 684_950L, 727_675L, 772_000L, 817_950L, 865_550L, 914_850L,
            965_850L, 1_021_225L, 1_078_450L, 1_137_550L, 1_198_575L, 1_261_525L, 1_326_450L, 1_393_350L, 1_462_275L, 1_533_250L,
            1_606_300L, 1_685_200L, 1_766_325L, 1_849_725L, 1_935_425L, 2_023_450L, 2_113_825L, 2_206_575L, 2_301_725L, 2_399_300L,
            2_499_350L, 2_607_025L, 2_717_350L, 2_830_350L, 2_946_050L, 3_064_475L, 3_185_675L, 3_309_675L, 3_436_500L, 3_566_175L,
            3_698_750L, 3_855_225L, 4_031_100L, 4_228_700L, 4_450_675L, 4_699_975L, 4_979_925L, 5_294_175L, 5_646_875L, 6_042_650L
        ),
        sourceReference = KnowledgeSourceReference(
            sourceId = SOURCE_ID,
            canonicalUrl = "https://genshin-impact.fandom.com/wiki/Weapon_EXP/4-Star_Weapons",
            publisherLabel = "Genshin Impact Wiki contributors"
        ),
        sourceLicense = LICENSE,
        sourceLicenseUrl = LICENSE_URL,
        retrievedOn = "2026-11-27"
    )

    val fiveStar = WeaponExperienceCurveCandidate(
        rarity = 5,
        cumulativeExperienceByLevel = listOf(
            0L, 600L, 1_550L, 2_900L, 4_700L, 7_025L, 9_950L, 13_475L, 17_675L, 22_625L,
            28_325L, 34_850L, 42_250L, 50_550L, 59_775L, 69_975L, 81_225L, 93_525L, 106_950L, 121_550L,
            137_300L, 155_150L, 174_325L, 194_875L, 216_850L, 240_300L, 265_250L, 291_725L, 319_775L, 349_450L,
            380_800L, 413_850L, 448_650L, 485_225L, 523_625L, 563_875L, 606_025L, 650_125L, 696_225L, 744_350L,
            794_500L, 849_375L, 906_500L, 965_900L, 1_027_625L, 1_091_725L, 1_158_225L, 1_227_150L, 1_298_550L, 1_372_500L,
            1_449_000L, 1_532_075L, 1_617_925L, 1_706_575L, 1_798_125L, 1_892_550L, 1_989_950L, 2_090_300L, 2_193_700L, 2_300_175L,
            2_409_750L, 2_528_100L, 2_649_800L, 2_774_900L, 2_903_450L, 3_035_500L, 3_171_075L, 3_310_200L, 3_452_925L, 3_599_300L,
            3_749_375L, 3_910_900L, 4_076_400L, 4_245_900L, 4_419_450L, 4_597_100L, 4_778_900L, 4_964_900L, 5_155_150L, 5_349_675L,
            5_548_550L, 5_783_275L, 6_047_100L, 6_343_500L, 6_676_475L, 7_050_425L, 7_470_350L, 7_941_725L, 8_470_775L, 9_064_450L
        ),
        sourceReference = KnowledgeSourceReference(
            sourceId = SOURCE_ID,
            canonicalUrl = "https://genshin-impact.fandom.com/wiki/Weapon_EXP/5-Star_Weapons",
            publisherLabel = "Genshin Impact Wiki contributors"
        ),
        sourceLicense = LICENSE,
        sourceLicenseUrl = LICENSE_URL,
        retrievedOn = "2026-11-27"
    )
}
