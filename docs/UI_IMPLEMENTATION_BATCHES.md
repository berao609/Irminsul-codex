# UI Implementation Batches

**Repository:** `berao609/Irminsul-codex`  
**Current completed batch:** Batch 7 - World Exploration  
**Roadmap status:** Batch 8 inserted; later approved work renumbered.

This document is the active sequence for the UI Revamp implementation. Completed batches retain their historical numbers. The new data-retrieval work is inserted after Batch 7; later planned work moves forward by one number without changing its approved scope.

## Completed

- **Batches 1-7:** Completed. See the corresponding UI Revamp entries in `docs/PROGRESS_MILESTONES.md`. Batch 7 (World Exploration) was merged in PR #15, merge commit `c699d3c3636eda2cb50fbfbb73d7d00a7af70360`, and user-tested.

## Upcoming

### Batch 8 - Multi-Channel Data Fetching

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

This is the former Batch 8. Its scope and acceptance intent remain unchanged; only its number moves.

- Reconstruct the character and weapon roster/detail experiences with high Battle Chronicles fidelity.
- Preserve character-first presentation, relevant character/weapon information, and established native Android interaction.
- Include the previously planned roster and detail refinements, including weapon rarity and weapon-type filters where they are useful and supported by the source data.
- Do not reintroduce controls the user has explicitly rejected, such as an unnecessary character rarity filter or redundant sorting controls.
- Use source-backed data and existing approved game assets; do not invent missing values or artwork.
- Preserve the existing app architecture and verify the result with tests, CI, and physical-device testing.

## Numbering and change control

- Batch 7 remains completed and unchanged.
- Multi-Channel Data Fetching is the new Batch 8.
- Character and Weapon Fidelity is now Batch 9; its scope is not otherwise changed.
- No further numbered UI batch is currently defined in this roadmap. Add later batches only when their scope is explicitly agreed.
- Update this document when a batch is approved, started, completed, or materially re-scoped. Record implementation and verification evidence in `docs/PROGRESS_MILESTONES.md`.
