# Batch 9 Enhancement Calculator - Device Retest Feedback

**Date:** 2026-11-28  
**PR:** #17 - Batch 9 Character and Weapon Fidelity  
**Status:** Device acceptance rejected; corrections required before another APK test.

The user tested the calculator UI on device and found the current implementation unsatisfactory. The four-page PDF reference is `PDF_2026-11-28_17-46-29_1416.pdf` (provided in chat, not a repository asset).

## Page 1 - Character selection

- Replace text-only element filters with actual game element icons.
- Replace text-only weapon-type filters with actual game weapon-type icons.
- Keep the selection page as an image-based grid/catalogue.

## Page 2 - Weapon selection

- Apply the same grid-selection design logic and icon-based weapon-type filters as the character-selection interface.
- Keep the catalogue selection flow consistent between characters and weapons.

## Page 3 - Upgrade target input

- Use wheel picker / scroll picker controls for numeric levels. The user did **not** request sliders.
- Make input sections visually subtle and integrated into the planning card.
- Remove the visible **EXP already earned within current level** field.
- Remove the visible **Current ascension rank** field.
- Preserve calculation accuracy and disclose internal assumptions when necessary. No keyboard-triggered input controls on this interface.

## Page 4 - All material details

- Use a compact, continuous material list.
- Display correct item icons and readable names.
- Align consumed/required and still-needed quantities in clear columns.
- Avoid oversized per-material cards and excessive padding.

## Global UI refinements

- Remove or replace any active input section across the app that triggers the software keyboard. Use suitable non-keyboard controls such as wheel pickers, steppers, chips, or selection grids.
- Remove search bars from selection pages.
- Make expand/collapse affordances tiny, subdued, and centered; keep their touch target and accessibility semantics usable.

## Saved-account restoration error

On restoration of saved account analysis, the user observed:

`Could not restore the saved account analysis: A migration from 3 to 2 was required but not found. Please provide the necessary Migration path via RoomDatabase.Builder.addMigration(...) or allow for destructive migrations via one of the RoomDatabase.Builder.fallbackToDestructiveMigration* functions.`

Source inspection of the current PR branch's `IrminsulDatabase.kt` confirms schema **version 3**, entity `private_account_data`, and registered migrations `1->2` and `2->3`. The exact APK tested has not been identified, so the observed error may come from an older APK/schema state. **Do not change or downgrade the current schema based only on the error message.** Verify the installed APK/version and database state; test restoration using the latest CI artifact and a database created by the prior build. Keep non-destructive migrations and add regression coverage for supported upgrade paths.

## Acceptance status

- The previous calculator refinement is **not accepted** by physical-device testing.
- The saved-account restoration error is a blocking device-retest issue until its source is identified and resolved.
- Implement UI corrections, run relevant unit/instrumentation/CI checks, publish a fresh APK, and request a focused retest.
- CI success alone does not constitute physical-device acceptance.

## Source reference

See `docs/BATTLE_CHRONICLE_REFERENCE_ANALYSIS.md`, Section 6.6, for the original screenshot-based UI direction. This retest feedback supersedes any prior implication that the current calculator UI was accepted.


## Retest follow-up - 2026-11-28

The user tested CI #1218 and reported three additional issues:

1. The complete Enhancement Calculator was embedded inside one expandable Tools section. It must open as a dedicated screen with its own back navigation, not expand inline inside the Tools list.
2. The All Characters / Already Owned selector did not change the catalogue. Root cause found in `EnhancementCalculatorSection.kt`: the `remember(...)` key set omitted both `ownedOnly` and the account `characters` list, so the derived choices remained stale after tab/account changes.
3. The Cryo filter symbol was not visibly rendering. The filter renderer used a tiny dot as its loading/error fallback. Keep the source-backed Cryo game asset, apply a visible ice-blue tint, and show a visible snowflake fallback if the remote image cannot load.

Corrective commits:
- `3183aa665bca9e20fcf1ea4e815f7aa56dc3704f` - Tools entry now opens a dedicated calculator screen.
- `e6a15fa4d0f2ce55c37bad5406bae70a9d9b3346` - Added screen-level back navigation and moved calculator composition out of the expandable Tools section.
- `4fc418b96d1f20ea2598a5c1abee45d6e78f8987` - Added ownership/account data to catalogue-choice memoization keys and visible Cryo fallback.
- `c5274d80e993535cec54dce8d02065dc3a9b4ac9` - Applied an ice-blue tint to the Cryo source icon.

Verification pending: new CI run and physical-device retest. The Cryo asset's remote successful-load state is not independently verified; the fallback is implemented to prevent an invisible filter if loading fails. Do not mark device acceptance complete.


## Retest follow-up - 2026-11-29

The user requested three immediate refinements:

1. **Compact material requirements:** Replaced spacious material rows and dialog with a near-full-screen dense list. Each row uses a 26dp icon, one-line material name, inline owned stepper, and compact Required / Needed quantities. Inventory-unknown remains distinct from confirmed zero; setting zero remains explicit. Candidate-cost status remains visible.
2. **Full-screen nested destinations:** CodexScreen suppresses the Home / Visuals / Tools / Account bottom navigation on the dedicated Enhancement Calculator screen and all nested Visuals destinations. Main tab destinations retain the bar; expandable content does not trigger full-screen behavior.
3. **Chronological default ordering:** Character release groups now sort newest-first and include Aether/Lumine plus current Manekin/Manekina forms. Calculator character/weapon grids use a newest-first release-order sorter. Weapon first-release dates are bundled from GachaTracker's Genshin weapon database; entries without a published date are deliberately placed last rather than guessed from IDs.

Relevant implementation: CodexScreen.kt, EnhancementCalculatorSection.kt, CharacterReleaseOrder.kt, EnhancementCatalogueReleaseOrder.kt.

Tests: Updated CharacterReleaseOrderTest.kt for newest-first behavior; added EnhancementCatalogueReleaseOrderTest.kt for oldest-first weapon dates and unknown-date fallback.

Verification: CI for the latest branch head is pending. No new APK or physical-device acceptance is claimed. The user must retest the compact materials list, full-screen nested navigation, and both chronological selection grids after CI produces a fresh APK.
