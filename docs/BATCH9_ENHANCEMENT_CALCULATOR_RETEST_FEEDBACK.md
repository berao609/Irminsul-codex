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
