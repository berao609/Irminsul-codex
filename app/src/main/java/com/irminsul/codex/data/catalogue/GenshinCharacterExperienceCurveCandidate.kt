package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.model.KnowledgePermissionState
import com.irminsul.codex.domain.model.KnowledgeReviewState
import com.irminsul.codex.domain.model.KnowledgeSourceReference

/*
 * The cumulative Character EXP values below are adapted from:
 * "Character EXP", Genshin Impact Wiki contributors,
 * https://genshin-impact.fandom.com/wiki/Character_EXP
 *
 * The source page's text is licensed CC BY-SA 3.0:
 * https://creativecommons.org/licenses/by-sa/3.0/
 * Attribution: Genshin Impact Wiki contributors, "Character EXP".
 * Changes: extracted the Total EXP column into a level-indexed LongArray.
 *
 * This dataset file is separately licensed CC BY-SA 3.0. It is not relicensed
 * under the repository's MIT license. No game artwork or icons are included.
 */

data class CharacterExperienceCurveCandidate(
    val curveId: String,
    val cumulativeExperienceByLevel: List<Long>,
    val sourceReference: KnowledgeSourceReference,
    val sourceLicense: String,
    val sourceLicenseUrl: String,
    val retrievedOn: String,
    val reviewState: KnowledgeReviewState = KnowledgeReviewState.CANDIDATE,
    val permissionState: KnowledgePermissionState = KnowledgePermissionState.PENDING_REVIEW
)

/**
 * Shared level curve for all playable characters. Index zero represents level 1.
 * This remains candidate data until the project completes its source/license review.
 */
object GenshinCharacterExperienceCurveCandidate {
    val current = CharacterExperienceCurveCandidate(
        curveId = "genshin-character-exp-curve-v1",
        cumulativeExperienceByLevel = listOf(
            0L, 1_000L, 2_325L, 4_025L, 6_175L, 8_800L, 11_950L, 15_675L, 20_025L, 25_025L,
            30_725L, 37_175L, 44_400L, 52_450L, 61_375L, 71_200L, 81_950L, 93_675L, 106_400L, 120_175L,
            135_050L, 151_850L, 169_850L, 189_100L, 209_650L, 231_525L, 254_775L, 279_425L, 305_525L, 333_100L,
            362_200L, 392_850L, 425_100L, 458_975L, 494_525L, 531_775L, 570_750L, 611_500L, 654_075L, 698_500L,
            744_800L, 795_425L, 848_125L, 902_900L, 959_800L, 1_018_875L, 1_080_150L, 1_143_675L, 1_209_475L, 1_277_600L,
            1_348_075L, 1_424_575L, 1_503_625L, 1_585_275L, 1_669_550L, 1_756_500L, 1_846_150L, 1_938_550L, 2_033_725L, 2_131_725L,
            2_232_600L, 2_341_550L, 2_453_600L, 2_568_775L, 2_687_100L, 2_808_625L, 2_933_400L, 3_061_475L, 3_192_875L, 3_327_650L,
            3_465_825L, 3_614_525L, 3_766_900L, 3_922_975L, 4_082_800L, 4_246_400L, 4_413_825L, 4_585_125L, 4_760_350L, 4_939_525L,
            5_122_700L, 5_338_925L, 5_581_950L, 5_855_050L, 6_161_850L, 6_506_450L, 6_893_400L, 7_327_825L, 7_815_450L, 8_362_650L
        ),
        sourceReference = KnowledgeSourceReference(
            sourceId = "genshin-impact-wiki-character-exp",
            canonicalUrl = "https://genshin-impact.fandom.com/wiki/Character_EXP",
            publisherLabel = "Genshin Impact Wiki contributors"
        ),
        sourceLicense = "CC BY-SA 3.0",
        sourceLicenseUrl = "https://creativecommons.org/licenses/by-sa/3.0/",
        retrievedOn = "2026-11-27"
    )
}
