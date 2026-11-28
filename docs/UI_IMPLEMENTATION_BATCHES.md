# UI Implementation Batches

**Repository:** `berao609/Irminsul-codex`  
**Latest completed batch:** Batch 7 - World Exploration  
**Active:** Batch 9 - Character and Weapon Fidelity  
**Pending acceptance:** Batch 8 - Multi-Channel Data Fetching (CI #1070 passed; physical-device verification pending)  
**Planned:** Batch 10 - Chronicles UI Visual Refinement

This document is the active sequence for the UI Revamp implementation. Completed batches retain their historical numbers. The new data-retrieval work is inserted after Batch 7; later planned work moves forward by one number without changing its approved scope.

## Completed

- **Batches 1-7:** Completed. See the corresponding UI Revamp entries in `docs/PROGRESS_MILESTONES.md`. Batch 7 (World Exploration) was merged in PR #15, merge commit `c699d3c3636eda2cb50fbfbb73d7d00a7af70360`, and user-tested.

## Upcoming

### Batch 8 - Multi-Channel Data Fetching

**Status:** Implementation and CI verification complete; physical-device acceptance pending. PR #16 remains open and unmerged.

**Objective:** Separate retrieved account information by purpose and lifecycle, instead of treating one JSON export as the only durable output.

**Logical channels**

1. **Portable account-data channel**
   - Collect the clean, debloated account-data JSON intended for user access.
   - Keep the data usable and understandable outside the app.
   - Let the user inspect/interact with the JSON and explicitly save or export it to external storage.
   - Preserve source values and meaningful null/missing distinctions; do not silently fabricate unavailable fields.

2. **Private app-data channel**
   - Retrieve and retain app-specific presentation data and HoYoLAB-specific elements needed by Irminsul, such as profile/showcase presentation and HoYoLAB modules.
   - Keep this data in app-private storage with appropriate protection; sensitive values must use the established secured/encrypted storage approach.
   - Do not mix app-internal implementation data into the portable account JSON unless it is deliberately part of that export contract.

3. **Asset channel**
   - Retrieve/cache visual assets such as character portraits, regional emblems, and backgrounds through the established asset-loading path.
   - Keep asset caching separate from account-data persistence and the portable JSON export.
   - Reuse the existing approved image-loading/cache path; do not introduce a parallel asset pipeline without a demonstrated need.

**Retrieval coordination and behavior**

- Reuse one authenticated HoYoLAB session and a shared retrieval coordinator; do not create separate login flows or duplicate authentication state for each channel.
- Run independent retrieval tasks for portable account sections, app-specific visuals/data, HoYoLAB modules (including Daily Note and Battle Chronicle where supported), and assets.
- Give each independently retrieved section/channel its own validation, freshness timestamp, and loading/success/partial/failure state.
- A failure in one channel must not erase valid results from another channel or invalidate an otherwise usable cached snapshot.
- Preserve partial-success semantics. Distinguish missing, null, malformed, unavailable, stale, and failed data.
- Keep network and disk work off the main thread, propagate coroutine cancellation, and preserve the existing layered architecture.
- Do not introduce a mandatory backend, transmit HoYoLAB credentials to AI providers, or claim complete account coverage where source endpoints do not provide it.

**Scope boundary:** This batch establishes the retrieval, ownership, persistence, and export boundaries needed by the UI. It does not authorize speculative endpoints, invented data, a new backend, or a wholesale rewrite of the existing account model. Inspect current retrieval and persistence code before deciding the smallest compatible implementation.

**Verification:** Add deterministic tests for independent channel success/failure, partial retrieval, stale/cached data preservation, malformed or missing source values, and export boundaries. CI and physical-device verification remain required before marking the batch complete.

### Batch 9 - Character and Weapon Fidelity

**Status:** Active on `ui-revamp/batch-9-character-weapon-fidelity`.

This is the former Batch 8. Its scope and acceptance intent remain unchanged; only its number moves.

- Refine the dedicated Character Roster and Character Details destinations against the established Battle Chronicles presentation.
- Preserve character-first presentation, newest-first release ordering, rarity-based card artwork, selected-character highlighting, source-backed character artwork, and existing detail interactions.
- Implement a dedicated Weapon Roster destination using distinct weapon records present in the retrieved character roster.
- Provide weapon rarity and weapon-type filters with clear/reset behavior and accessible selected state.
- **Data boundary:** the current Battle Chronicle model exposes equipped weapons through character records, not a complete inventory of unequipped weapons. The initial roster must disclose that limitation and must not imply complete weapon ownership.
- Do not reintroduce the rejected character rarity filter or redundant sorting controls.
- Use source-backed data and existing approved game assets; do not invent missing values, passive descriptions, or artwork.
- **Enhancement Calculator flow:** choose Character or Weapon, then navigate to a filterable, non-searchable grid containing the complete bundled 4-star/5-star catalogue regardless of account ownership; select an entry to open its dedicated planning page.
- Character planning includes current/target character level, ascension rank, and independent Normal Attack/Skill/Burst talent levels. Weapon planning includes current/target level and ascension rank.
- Replace numeric keyboard fields with vertical wheel/scroll pickers for levels/talents and plus/minus steppers for quantities. Calculate opens a large material-requirements dialog; each material has an owned-quantity stepper and a live remaining-quantity calculation. Persist owned quantities account-wide by material ID, distinguish unknown inventory from explicit zero, and do not display a remaining total until inventory is entered.
- Keep the catalogue version-pinned and source-attributed. The current bundled selection data has no complete portrait/icon mapping for all unowned items; do not fabricate artwork. Candidate cost/EXP data must remain clearly labelled and incomplete totals must be withheld.
- Preserve the existing app architecture and verify the result with deterministic tests, CI, and physical-device testing.

#### User device-test corrections - 2026-11-28

The first physical-device acceptance attempt was **unsatisfactory**. Apply the four-page reference recorded in `docs/BATTLE_CHRONICLE_REFERENCE_ANALYSIS.md` §6.6:

- Character selection: seven actual element icons, five actual weapon-type icons, dense portrait grid, no search field.
- Weapon selection: matching grid layout, rarity filters, and actual weapon-type icons; no search field.
- Planning page: subtle parchment-style input sections and vertical wheel/scroll pickers. Remove visible current-EXP-within-level and current-ascension-rank inputs. Preserve calculation correctness and disclose assumptions for unknown values.
- Material details: compact scrollable rows with correct item icons, readable names, and aligned required/consumed/still-needed quantities.
- Global UI: replace keyboard-opening text fields with appropriate non-keyboard controls; make expand/collapse affordances tiny, subtle, and centered while preserving touch accessibility.

**Room regression:** PR #16's Batch 8 persistence added schema v3 (`private_account_data`); the Batch 9 branch had schema v2. Retain schema v3 and migrate v2->3. Do not use destructive fallback. User reported saved-analysis restoration failure; physical retest is required after the migration fix.



#### Corrective implementation pass - 2026-11-28

- Added the permanent project UI standard at `docs/NATIVE_UI_STANDARD.md` and recorded the four-page PDF observations in `docs/BATTLE_CHRONICLE_REFERENCE_ANALYSIS.md` §6.7.
- Calculator selection now uses a 4-column grid, All / Already Owned tabs, icon-only element and weapon-type chips, and rarity filters for weapons. Character and weapon catalogue selection share the same flow.
- Calculator numeric values now render as compact triggers and open a `ModalBottomSheet` wheel picker with explicit Confirm / Cancel. The wheel is no longer in the resting planning layout.
- Flattened the Tools calculator entry. Removed the inline min/max level wheel controls from Character Roster and removed the selected-card stroke in favor of tonal selection.
- Flattened top-level World Exploration region containers and Spiral Abyss floor containers; child subregions retain one tonal surface level.
- **Still pending in this corrective pass:** full selection footer/multi-selection interaction parity; material owned-quantity input redesign and narrow-width validation; full app-wide modified-file audit for borders, nested surfaces, inline wheels, and keyboard-opening fields; source/compile verification and CI. These changes are not yet device-accepted.

#### Implementation progress - 2026-11-28

- Implemented the selector grids, full game-asset portraits, element/weapon icon filters, no-search selection flow, wheel pickers, subtle planning sections, compact material rows, and centered expand/collapse controls.
- Replaced active Compose text-entry fields across roster filters, materials planning, weapon planning, and account credential settings. HoYoLAB WebView sign-in remains the intentional keyboard exception.
- Existing manual custom-material rows retain quantity steppers; creating or renaming custom rows is disabled. New material plans use the Enhancement Calculator.
- Restored Room schema v3 and added the 2->3 migration. Migration instrumentation passed CI #1119/#1121. CI workflow now compares the generated schema-v3 snapshot to the checked-in snapshot.
- CI #1169 passed unit tests and Debug APK build for the code-tested source; Room instrumentation was skipped because that workflow commit changed documentation only. CI #1177 passed unit tests and build for the first visual refinement pass. CI #1179 passed for documentation updates. CI #1181 passed unit tests, Debug APK build, schema-v3 snapshot verification, signature verification, and artifact upload for latest code head `4ce56a4a6788dfff2d96d0f43f6bfd2e081cadb9`; Room instrumentation was skipped because no Room files changed. Room migration instrumentation had previously passed in #1119/#1121.
- Latest device-test APK #1181: `1.0.0-dev.1181`, version code `1183`, artifact ID `15263442218`, SHA-256 `546e56534b1bc32d76d06563438a919507e4762d67b5c1fa47a2bdd6ef8e9c00`.
- Physical-device acceptance remains pending. Retest saved-analysis restoration, both selection grids, wheel behavior, compact material details, keyboard-free controls, and Light/Dark layouts before merge.

### Batch 10 - Chronicles UI Visual Refinement

**Status:** Planned; approved by the user for work after Batch 9.

- Refine foreground surface opacity/transparency across the Chronicles UI so existing regional background artwork is more visible.
- Apply the treatment to the affected Exploration region cards first, including regions that already have backgrounds.
- Keep text, icons, progress indicators, and interactive controls readable in both Light and Dark themes.
- Preserve existing artwork, data, layout, navigation, and regions without artwork. Adding missing regional backgrounds is a separate future task.
- Review consistency across the wider Chronicles UI after the Exploration treatment is validated.
- This is a visual-surface refinement, not a global theme rewrite or background-artwork acquisition task.

## Numbering and change control

- Batch 7 remains completed and unchanged.
- Multi-Channel Data Fetching is the new Batch 8.
- Character and Weapon Fidelity is now Batch 9; its scope is not otherwise changed.
- No further numbered UI batch is currently defined in this roadmap. Add later batches only when their scope is explicitly agreed.
- Update this document when a batch is approved, started, completed, or materially re-scoped. Record implementation and verification evidence in `docs/PROGRESS_MILESTONES.md`.
