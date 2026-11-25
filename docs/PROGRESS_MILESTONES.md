# Irminsul Codex Progress Milestones

This file is the durable, evidence-based record of completed and in-progress development milestones. It records implementation, verification, and remaining work separately. A planned feature is not considered complete merely because it appears in the roadmap or specification.

For each milestone, distinguish:
- **Implemented:** Source changes exist in the repository.
- **CI-verified:** The relevant automated workflow completed successfully.
- **Device-verified:** Runtime behavior was observed on the target device and reported by the user.
- **Committed:** The relevant changes are present in Git history.
- **Remaining:** Work or verification still outstanding.

---

## Milestone 001 - Daily Note Data Reliability

**Date recorded:** 2026-11-25  
**Development phase:** Phase 0 - Stabilize the Existing Application  
**Milestone scope:** First reliability increment for Daily Note parsing and presentation  
**Status:** Implemented and CI-verified; the user also reported that the app still works on-device.

### Objective

Prevent missing, null, or malformed Daily Note values from being silently interpreted as valid zero/false values. Keep existing application workflows operational while introducing isolated parsing and automated tests.

### Work implemented

1. Added `DailyNoteParser.kt` to separate Daily Note JSON parsing from the HTTP request implementation.
2. Changed Daily Note numeric and boolean properties to nullable values so unavailable source data remains distinguishable from real values.
3. Updated the Daily Note fetch path to reject responses without an account data object and delegate field parsing to the parser.
4. Updated the UI to display an em dash for unavailable values and to avoid presenting unavailable Resin progress or recovery information as real measurements.
5. Added JVM unit-test dependencies and parser tests covering:
   - Valid Daily Note values.
   - Actual zero and false values.
   - Missing and explicit null values.
   - Malformed field types.
6. Updated the Debug CI workflow to run unit tests before assembling the Debug APK.

### Files changed

- `app/src/main/java/com/irminsul/codex/api/DailyNoteParser.kt` (new)
- `app/src/main/java/com/irminsul/codex/api/HoYoLabClient.kt`
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/test/java/com/irminsul/codex/api/DailyNoteParserTest.kt` (new)
- `.github/workflows/build.yml`

### Verification evidence

**Repository and commit**
- Branch: `main`
- CI-triggering commit: `d692a8c3f69cb445ed770abfdaa7402a66cccdd3`
- Commit message: `Run unit tests before building debug APK`
- [Commit on GitHub](https://github.com/berao609/Irminsul-codex/commit/d692a8c3f69cb445ed770abfdaa7402a66cccdd3)

**GitHub Actions**
- Workflow: Build Debug APK
- Run: [#56 - Run unit tests before building debug APK](https://github.com/berao609/Irminsul-codex/actions/runs/45444201077)
- Run result: **Success**
- Unit-test step: **Success**
- Debug APK build step: **Success**
- Debug APK artifact upload step: **Success**
- Completed: 2026-11-24 17:05 UTC

The successful CI run establishes that the configured unit-test task and Debug APK build completed successfully for the recorded commit.

**Device confirmation**
- On 2026-11-25, the user reported that the updated app still works.
- The conversation did not separately enumerate every tested screen or workflow. Do not interpret this statement as proof that every login, synchronization, export, missing-data, and malformed-response scenario was individually tested on-device.

### Outcome

The Daily Note parser now preserves the distinction between valid zero/false values and unavailable values. The UI has explicit unavailable states. Automated parser tests and the Debug APK build passed in CI. The user also reported that the updated app continues to work.

This is a completed implementation and CI milestone. It does **not** mean all Phase 0 stabilization work is complete.

### Remaining work

- Perform targeted device checks for Daily Note values that are missing, null, zero, false, or malformed, where practical.
- Confirm the exact existing user workflows tested on-device, including login, Daily Note synchronization, and JSON export.
- Continue Phase 0 reliability work in separately scoped increments, including consistent API error handling, credential security review, export behavior review, and reproducible build improvements.
- Do not begin the next milestone until its scope and acceptance criteria are defined.

### Next development checkpoint

Continue with the next smallest Phase 0 reliability task after confirming the intended device test coverage for this milestone. Preserve the current working login, synchronization, and export behavior.

---


## Milestone 002 - HoYoLAB HTTP Response Handling

**Date recorded:** 2026-11-25  
**Development phase:** Phase 0 - Stabilize the Existing Application  
**Milestone scope:** Normalize HTTP response handling for HoYoLAB GET and POST requests  
**Status:** Implemented and CI-verified; device verification remains pending.

### Objective

Make HTTP-level failures, empty responses, and malformed JSON explicit and consistent without confusing them with successful API responses. Preserve HoYoLAB API-level error codes and messages for existing endpoint handlers.

### Work implemented

1. Added `HoYoLabResponseParser.kt` as a pure response-normalization component.
2. Updated both GET and POST request paths to use the same parser.
3. Converted empty response bodies and malformed JSON into explicit error response envelopes.
4. Ensured non-2xx HTTP responses cannot be treated as successful solely because their JSON body contains `retcode: 0`.
5. Preserved nonzero HoYoLAB API error codes and useful server messages, while adding the HTTP status.
6. Limited server-provided error message length to avoid excessively large error text.
7. Added unit tests for successful responses, API-level errors, HTTP failures, empty bodies, malformed JSON, and long messages.

### Files changed

- `app/src/main/java/com/irminsul/codex/api/HoYoLabClient.kt`
- `app/src/main/java/com/irminsul/codex/api/HoYoLabResponseParser.kt` (new)
- `app/src/test/java/com/irminsul/codex/api/HoYoLabResponseParserTest.kt` (new)

### Verification evidence

**Repository and commit**
- Branch: `main`
- Source and test commit: `a9687fa0054730d8275faf38722c366d7ea7564f`
- Commit message: `test: cover HoYoLAB response errors`
- [Commit on GitHub](https://github.com/berao609/Irminsul-codex/commit/a9687fa0054730d8275faf38722c366d7ea7564f)

**GitHub Actions**
- Workflow: Build Debug APK
- Run: [#59 - test: cover HoYoLAB response errors](https://github.com/berao609/Irminsul-codex/actions/runs/45446200587)
- Unit-test step: **Success**
- Debug APK build step: **Success**
- Debug APK artifact upload step: **Success**
- Run result: **Success**, including all job cleanup steps.

### Outcome

HTTP status failures are now represented as explicit error envelopes. Empty and malformed responses no longer cause an uncontextualized JSON parsing failure at the response boundary. Existing endpoint handlers can continue to interpret HoYoLAB API-level `retcode` values, and export sections can retain normalized HTTP error details.

### Remaining work

- Install the latest Debug APK and confirm login, Daily Note synchronization, and JSON export still behave correctly.
- Verify error messages for expired/invalid credentials and network/server failures when safe test conditions are available.
- This milestone does not yet isolate every export section from transport exceptions. Partial-export resilience remains a separate task.
- This milestone does not implement typed domain-wide error states or a repository layer.

### Next development checkpoint

After device verification, proceed to the next Phase 0 task: make account JSON export resilient to individual section failures so one failed request does not discard successful data from unrelated sections.

---

## Milestone 003 - Partial Account Export Resilience

**Date recorded:** 2026-11-25  
**Development phase:** Phase 0 - Stabilize the Existing Application  
**Milestone scope:** Preserve successful account sections when other export requests fail  
**Status:** Implemented and CI-verified; device verification remains pending.

### Objective

Prevent one failed HoYoLAB endpoint or character-detail batch from discarding unrelated successful account data. Make incomplete exports clearly identifiable to both the user and downstream data consumers.

### Work implemented

1. Added `AccountExportAssembler.kt` to assemble export metadata, section payloads, and per-section status information.
2. Wrapped each independent account-data request so transport exceptions become section-level error responses instead of cancelling the other concurrent requests.
3. Kept coroutine cancellation distinct from ordinary request failures inside the request wrapper.
4. Preserved successful sections even when profile, Daily Note, character list, Abyss, Theater, or account-card retrieval fails.
5. Added an `export_status` object to the JSON export containing:
   - An overall status: `complete`, `partial`, or `failed`.
   - A status for each exported section: `success`, `partial`, `failed`, or `skipped`.
   - A useful message for failed, partial, or skipped sections.
6. Isolated character-detail batch failures. Available character details remain in the export, while missing batches are identified in the character section's status message.
7. Preserved the existing account section names and payload shapes where data is available.
8. Updated the UI to report complete, partial, or unsuccessful export status instead of always describing the result as a full export.
9. Added unit tests for complete exports, skipped optional sections, isolated section failures, partial character-detail results, and fully failed exports.

### Files changed

- `app/src/main/java/com/irminsul/codex/api/HoYoLabClient.kt`
- `app/src/main/java/com/irminsul/codex/api/AccountExportAssembler.kt` (new)
- `app/src/test/java/com/irminsul/codex/api/AccountExportAssemblerTest.kt` (new)
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`

### Verification evidence

**Repository and commit**
- Branch: `main`
- Final CI-triggering commit: `ab45853b9660494e3a399d6121a87311632f6009`
- Commit message: `feat: show partial export status to users`
- [Commit on GitHub](https://github.com/berao609/Irminsul-codex/commit/ab45853b9660494e3a399d6121a87311632f6009)

**GitHub Actions**
- Workflow: Build Debug APK
- Run: [#63 - feat: show partial export status to users](https://github.com/berao609/Irminsul-codex/actions/runs/45447616109)
- Run result: **Success**
- Unit-test step: **Success**
- Debug APK build step: **Success**
- Debug APK artifact upload step: **Success**
- Workflow cleanup: **Success**

### Outcome

The export process now treats account sections independently. An unsuccessful endpoint is represented in its own section and in `export_status`, while unrelated successful data remains available. Character-detail batch failures are surfaced rather than silently ignored. The UI distinguishes complete and partial exports.

### Remaining work

- Install the latest Debug APK and perform a real account export on-device.
- Inspect the resulting JSON and confirm `export_status` and the per-section results are understandable and correctly match the retrieved data.
- Do not deliberately invalidate credentials or disrupt the account merely to force a failure. A safe network interruption or controlled test can be used later if needed.
- Verify whether downstream consumers of the export need a schema/version field before the format is relied upon by future intelligence features.

### Next development checkpoint

After device verification, continue Phase 0 with a focused review of export data integrity and filtering. In particular, determine which fields removed by `stripBloat()` are truly disposable versus potentially useful to account intelligence, and define a deliberate export schema before expanding the data model.

## Milestone 004 - Account Export Data Integrity and Field Preservation

**Date recorded:** 2026-11-25  
**Development phase:** Phase 0 - Stabilize the Existing Application  
**Milestone scope:** Preserve useful source fields in selected account-data exports and identify the export format version  
**Status:** Implemented, CI-verified, and device-verified with field-availability qualifications.

### Objective

Prevent the account exporter from silently discarding potentially useful source information. Preserve the fields returned by the selected HoYoLAB account-data sections so future account analytics and intelligence features can use data that the current UI does not display.

### Work implemented

1. Replaced the broad `stripBloat()` filtering behavior with `AccountExportDataPreserver`.
2. Preserved nested source fields, descriptions, effects, media URLs, property maps, skill-affix data, explicit nulls, and array ordering.
3. Retained the existing readable `stat_name` enhancement when a `property_type` can be mapped, without removing the original `property_type`.
4. Added `export_format_version: 1` to every assembled account export.
5. Added automated tests for source-field preservation, explicit nulls, nested arrays, stat-name enrichment, and export format versioning.
6. Updated the README to describe the versioned export and clarify that it remains a selected-section export, not a complete raw API archive or complete account inventory.

### Files changed

- `app/src/main/java/com/irminsul/codex/api/AccountExportDataPreserver.kt` (new)
- `app/src/main/java/com/irminsul/codex/api/HoYoLabClient.kt`
- `app/src/main/java/com/irminsul/codex/api/AccountExportAssembler.kt`
- `app/src/test/java/com/irminsul/codex/api/AccountExportDataPreserverTest.kt` (new)
- `app/src/test/java/com/irminsul/codex/api/AccountExportAssemblerTest.kt`
- `README.md`

### Verification evidence

**Repository and commit**
- Branch: `main`
- Final implementation/test commit: `19c229aa964cf4a6384696066a8172fc88c0771a`
- README clarification commit: `f9228cadc0400c331a9a98ebf924f3bc48699ffd`
- [Implementation/test commit](https://github.com/berao609/Irminsul-codex/commit/19c229aa964cf4a6384696066a8172fc88c0771a)
- [README update](https://github.com/berao609/Irminsul-codex/commit/f9228cadc0400c331a9a98ebf924f3bc48699ffd)

**GitHub Actions**
- Workflow: Build Debug APK
- Run: [#73 - test: assert account export format version](https://github.com/berao609/Irminsul-codex/actions/runs/45452746428)
- Run result: **Success**
- Unit-test step: **Success**
- Debug APK build step: **Success**
- Debug APK artifact upload step: **Success**
- Artifact: `irminsul-codex-debug-v1.0.0-build2`
- Artifact ID: `14946929273`
- SHA-256: `cf652bfa7f7d7b8a91f2db6bb2661889db850fcac00cc49f4aba68d9a5aa63db`
- Artifact expiry: 2026-12-08 17:50 UTC

### Physical-device export inspection

On 2026-11-25, the user generated a fresh export from the Android app and inspected `irminsul-m4-check.json` locally in Termux.

- JSON parsing: **Success**
- File size: **2,098,772 bytes**
- `export_format_version`: **1**
- `export_status.overall_status`: **complete**
- All seven reported section statuses: **success**
- Field scan found: 774 `effect` keys, 1,823 URL/image-related keys, 3,580 `property_type` keys, 3,580 `stat_name` keys, 430 `skill_affix` keys, and 165 explicit null values.
- The scan found zero `description` and zero `property_map` keys. Whether descriptions were returned by the source is unknown. The current retrieval code reads `data.property_map` from character-detail responses into an in-memory lookup used to add `stat_name`, but does not include that raw map object in the exported character section. Therefore, raw `property_map` preservation is not confirmed by this device export and is a specific item for the export-coverage review.
- The user did not provide the previous export's file size, so a before/after file-size comparison was not performed.
- This inspection confirms the generated export's structure and the presence of the listed fields. It is not a byte-for-byte comparison against raw HoYoLAB responses and does not establish complete account-data coverage.

### Outcome

The selected account sections are no longer aggressively filtered by a denylist of field names and URL patterns. The exporter retains the source fields it receives and adds an explicit format version. This improves the export's usefulness for future parsing and intelligence work without claiming that all possible HoYoLAB data is retrieved.

### Remaining work

- No `description` keys appeared in the exported JSON; whether the source responses contained descriptions is unknown. The raw character-detail `property_map` is currently consumed for `stat_name` enrichment rather than exported as a raw map. Existing automated tests cover preservation behavior for supplied source fields, but this device export does not verify raw-map retention.
- A before/after file-size comparison was not performed because the previous export size was not supplied.
- Continue to treat the export as selected account sections, not a complete raw API archive or complete account inventory.
- Consider a deliberate retention policy before introducing raw-response archival or long-term local storage.

### Next development checkpoint

Reassess the export schema and retrieval coverage against the actual account data before adding new endpoints or beginning account intelligence calculations. Identify which useful account domains are already represented, which are absent, and which require additional retrieval sources.

---
---
## Milestone record rules

1. Add a new numbered section for each meaningful milestone; do not silently rewrite historical results.
2. Record the exact branch, commit SHA, workflow run, and relevant evidence when available.
3. Separate automated CI results from physical-device observations.
4. State what was not tested or remains uncertain.
5. Update a milestone's status only when new evidence supports the change.

## Milestone 005 - Automatic Debug Build Versioning

**Date recorded:** 2026-11-25  
**Development phase:** Phase 0 - Stabilize the Existing Application  
**Milestone scope:** Give every new CI Debug build a higher Android version code while keeping official Release versioning independent  
**Status:** Implemented, CI-verified, and physical-device update verified.

### Objective

Prevent Android from rejecting consecutive CI Debug APKs because they reuse the same Android `versionCode`.

### Work implemented

1. Updated `app/build.gradle.kts` to accept optional `APP_VERSION_CODE` and `APP_VERSION_NAME` environment overrides. When absent, Gradle continues to use the values in `version.properties`.
2. Updated `.github/workflows/build.yml` to calculate each Debug build's version code as `VERSION_CODE + GITHUB_RUN_NUMBER`.
3. Assigned each Debug build a semantic prerelease version name in the form `VERSION_NAME-dev.GITHUB_RUN_NUMBER`.
4. Added validation for positive run numbers and Android's maximum supported version code.
5. Kept official Release builds on the exact `VERSION_CODE` and `VERSION_NAME` values in `version.properties`.
6. Updated `README.md`, `RELEASE_PROCESS.md`, and `version.properties` comments to explain the separate Debug and Release version channels.

### Expected behavior

- Each new Debug workflow run receives a higher version code and can update an installation signed with the persistent Debug key.
- Re-running the same workflow run retains its version code. A new workflow run is required for a higher one.
- Debug and Release use different signing certificates, so switching between those channels still requires a one-time uninstall/reinstall.
- Official Release version codes remain manually controlled and increase only when preparing an official release.

### Verification

**Repository and implementation**
- Branch: `main`
- Gradle override support commit: `6349a79daa009952cdbd46271d100125a6b11d2d`
- Debug workflow versioning commit: `9c1f5a172ba656ed87f6ab0da36703e33d77c88b`
- Documentation and milestone commits are recorded in the repository history.

**GitHub Actions**
- Workflow: Build Debug APK
- Run: [#84 - Record automatic Debug versioning milestone](https://github.com/berao609/Irminsul-codex/actions/runs/45458818484)
- Run ID: `45458818484`
- Run result: **Success**
- Persistent Debug signing configuration: **Success**
- Unique Debug version calculation: **Success**
- Unit tests: **Success**
- Debug APK build: **Success**
- APK signature verification: **Success**
- Artifact upload: **Success**
- Temporary keystore cleanup: **Success**
- CI run commit: `df182c7770012be1cd58094f0f5ac2fd7ba85124`

**Generated Debug artifact**
- Artifact: `irminsul-codex-debug-v1.0.0-dev.84-vc86`
- Artifact ID: `14951772344`
- Android version name: `1.0.0-dev.84`
- Android version code: `86`
- Size: `8,179,215` bytes
- SHA-256: `6232bab2f3028b70b1b9d3607e199b38dceeda8812f9992ddb94e6409ec77e18`
- Expiry: 2026-12-08 18:23:48 UTC

**Physical-device verification**
- The owner reports that the persistent-signing Debug APK migration was installed successfully before this milestone.
- On 2026-11-25, the owner confirmed that a subsequent higher-version Debug APK installed over the existing app using Android's normal update flow.
- The owner confirmed that the update function works. No uninstall/reinstall was needed for this update.
- This confirms the practical in-place update path for consecutive CI Debug builds signed with the persistent Debug key and assigned increasing version codes.
- The conversation did not separately record a detailed audit of every stored app-data field after the update; do not overstate that as a complete data-integrity test.


## Milestone 006 - Account Export Retrieval Reliability and Provenance

**Date recorded:** 2026-11-25  
**Development phase:** Phase 0 - Stabilize the Existing Application  
**Milestone scope:** Implement Orders 1-5 from the account-data export reliability audit  
**Status:** Implemented, CI-verified, and device-export verified with explicit scope qualifications.

### Objective

Make the existing selected-section account export more reliable and auditable without claiming complete account inventory coverage.

### Work implemented

1. Preserve coroutine cancellation through outer result-handling boundaries in UID lookup, Daily Note retrieval, and account export.
2. Validate response envelopes and endpoint-specific container shapes before marking export sections successful. Empty objects and empty arrays remain valid when the expected container shape is present.
3. Define required sections explicitly. A skipped optional section does not invalidate completeness; failed/partial sections or missing required sections produce a partial export when usable data remains. An export with no usable section data is failed.
4. Preserve the original character roster and raw character-detail response batches, including raw property maps, alongside the enriched character list. Stop removing the profile's source avatar list.
5. Add per-section source, retrieval timestamp, and last-successful-retrieval timestamp where this single export can establish one. A failed section does not invent a historical last-success timestamp.

### Files changed

- `app/src/main/java/com/irminsul/codex/api/HoYoLabClient.kt`
- `app/src/main/java/com/irminsul/codex/api/AccountExportAssembler.kt`
- `app/src/main/java/com/irminsul/codex/api/HoYoLabExportResponseValidator.kt` (new)
- `app/src/test/java/com/irminsul/codex/api/AccountExportAssemblerTest.kt`
- `app/src/test/java/com/irminsul/codex/api/AccountExportDataPreserverTest.kt`
- `app/src/test/java/com/irminsul/codex/api/HoYoLabExportResponseValidatorTest.kt` (new)
- `README.md`

### Verification evidence

**Repository and CI**
- Implementation commits: `d82ef3e068797707dd77bf1c8594aef91b751278` and `ddf230bbd6d1a686237c25479c962dfa98e69e2a`.
- Build Debug APK workflow: [Run #94](https://github.com/berao609/Irminsul-codex/actions/runs/45463930525).
- Run result: **Success**.
- The run completed for commit `ddf230bbd6d1a686237c25479c962dfa98e69e2a`.

**Physical-device export verification**
- Export file: `irminsul-orders-1-5-check.json`.
- JSON parsing: **Success**; `export_format_version=1`; overall status: `complete`.
- File size: 4,258,474 bytes (previous M4 export: 2,098,772 bytes).
- Character roster source: 67 entries.
- Character detail source: 9 batches, 67 raw detail entries.
- Raw property maps: present and nonempty in all 9 batches, 333 entries total.
- All 9 section statuses: `success`.
- All 9 sections had populated source identifiers and valid `retrieved_at` and `last_successful_retrieval_at` timestamps.

**Qualifications**
- This verifies the observed account export and the fields available in that response; it is not a byte-for-byte comparison with raw HoYoLAB network responses.
- The file-size increase is measured, but size alone is not a preservation guarantee.
- The metadata inspection verified timestamp parseability and presence, not historical freshness across separate exports.
- The export remains selected-section account data, not a complete raw API archive or complete inventory.

## Milestone 007 - Unified Account Domain Model Foundation

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Introduce the initial pure Kotlin account snapshot model and map the existing versioned export into it.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Work implemented

1. Added domain types for account identity, snapshot metadata, section metadata/status, and explicit data availability states.
2. Added typed Daily Note and character snapshot models for the first normalized account sections.
3. Added an export mapper that converts the existing JSON export into the domain model without mutating or replacing the raw export.
4. Preserved missing, null, malformed, and valid zero/false distinctions in mapped Daily Note values.
5. Added mapper tests for identity, timestamp, zero/false, missing fields, character mapping, and invalid identity.

### Files added

- `app/src/main/java/com/irminsul/codex/domain/model/AccountSnapshot.kt`
- `app/src/main/java/com/irminsul/codex/data/mapper/AccountExportMapper.kt`
- `app/src/test/java/com/irminsul/codex/data/mapper/AccountExportMapperTest.kt`

### Verification

- Source commits: `d121a02c7859a41d348ba97c59d61fd22c82f8fe`, `504110bb23348c698cacc5fc74385e6d9f059cbf`, `3a9c24ad1a988f0b661c9e916f6da3d2c77d14aa`.
- Unit tests and Debug build: **Success**, GitHub Actions [Run #99](https://github.com/berao609/Irminsul-codex/actions/runs/45518448031), run ID `45518448031`, commit `83cdb9ae3c457890a31533f9e8f92b95a98b6ca7`.
- CI completed successfully, including unit tests, Debug APK build, signature verification, and artifact upload.
- Device verification: not yet required for this in-memory model foundation.
- No Room database, UI integration, export schema change, or release was included.

## Milestone 008 - Unified Account Model: Profile and Endgame Sections

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Extend the export mapper to include profile/exploration, both Spiral Abyss periods, and Imaginarium Theater without coupling the domain model to Android JSON classes.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Work implemented

1. Added a JSON-independent structured domain value tree supporting objects, arrays, strings, numbers, booleans, and explicit nulls.
2. Mapped `profile_and_exploration`, `spiral_abyss_current`, `spiral_abyss_previous`, and `imaginarium_theater` into the unified account snapshot.
3. Preserved nested values and explicit nulls without making assumptions about undocumented or changing endpoint field names.
4. Preserved section availability semantics: failed sections map to `Failed`, skipped sections to `Unavailable`, absent sections to `Missing`, and present sections to `Available`.
5. Added tests for nested objects/arrays, numeric and null values, and failed/skipped/missing section states.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/model/AccountSnapshot.kt`
- `app/src/main/java/com/irminsul/codex/data/mapper/AccountExportMapper.kt`
- `app/src/test/java/com/irminsul/codex/data/mapper/AccountExportMapperTest.kt`

### Verification

- Source commits: `ec700ec0918b48176f8aea4dd5fa24f6d1112091`, `def8842229d8230d09fb752c7fcc841ca6b02d80`, `f41299e36b6b1331caeaf51bbd99ef6e6887c4ac`.
- Unit tests and Debug build: **Success**, GitHub Actions [Run #104](https://github.com/berao609/Irminsul-codex/actions/runs/45518956906), run ID `45518956906`, commit `c7866f9f5556074b9a85c9a1420f7ba0aa0c6cc6`.
- Unit tests, Debug APK build, persistent Debug signature verification, artifact upload, and temporary keystore cleanup: **Success**.
- Device verification: not yet required for this mapper expansion.
- No raw export schema change, Room persistence, UI integration, or release was included.

## Milestone 009 - Unified Account Model: Account Card and Raw Character Sources

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Map account-card data, the original character roster, and raw character-detail batches into the domain snapshot while retaining their nested source structure.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Work implemented

1. Extended `AccountSnapshot` with account-card, character-roster-source, and character-detail-source values.
2. Reused the JSON-independent `StructuredData` tree so nested source fields remain represented without binding the domain layer to Android JSON classes or unstable endpoint schemas.
3. Mapped the original character roster and raw detail batches, including nested weapon/relic examples and property-map data.
4. Preserved section status semantics for failed, skipped, and absent source sections.
5. Added tests for nested raw character source mapping, equipment-shaped data, property maps, and unavailable/failed/missing states.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/model/AccountSnapshot.kt`
- `app/src/main/java/com/irminsul/codex/data/mapper/AccountExportMapper.kt`
- `app/src/test/java/com/irminsul/codex/data/mapper/AccountExportMapperTest.kt`

### Verification

- Source commits: `923796c24954336413482b683a5d922ed47eb002`, `9f5329107e92cee651de5695d7120749bccf3eb2`, `6077657c5f255352c0d8a6d0dc9c43e222c74b38`.
- Unit tests and Debug build: **Success**, GitHub Actions [Run #109](https://github.com/berao609/Irminsul-codex/actions/runs/45519407733), run ID `45519407733`, commit `d05af627821d3e4e0e69456bf8d787cd6fcf8a2b`.
- Unit tests, Debug APK build, persistent Debug signature verification, artifact upload, and temporary keystore cleanup: **Success**.
- Device verification: not required for this in-memory mapper increment.
- No raw export schema change, Room persistence, UI integration, or release was included.


## Milestone 010 - Typed Character and Equipment Domain Models

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Normalize observed character, weapon, artifact, constellation, skill, and combat-stat fields from the existing selected-section export.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Evidence used for the model

The owner inspected the device-generated `irminsul-orders-1-5-check.json` export in Termux:
- 67 character roster entries and 67 raw character-detail entries across 9 batches.
- The normalized `characters` records contain `base`, `weapon`, `relics`, `constellations`, `skills`, and property arrays.
- Weapon fields include ID/name/icon/type/rarity/level/promotion/type name/description/refinement and main/sub properties.
- Artifact fields include ID/name/icon/slot/rarity/level/set/main property/sub-property list.
- Constellation fields include active/enhancement flags and effects.
- Skill fields include skill ID/type/level/name/description/unlock/enhancement fields and affix/index arrays.
- Numeric stat fields are serialized as strings in the export; artifact property entries use `value` and `times`.

### Work implemented

1. Added typed weapon, artifact, stat, constellation, and character-skill domain models.
2. Expanded `CharacterSnapshot` with equipped weapon, artifact list, constellations, skills, and selected/base/extra/element property lists.
3. Extended the export mapper to accept the observed nested `base` character object while retaining compatibility with the earlier flat test representation.
4. Mapped nullable identifiers and fields without inventing zero values. Malformed or absent equipment identifiers do not create placeholder equipment.
5. Kept the raw export and raw character-detail source representation intact; no export schema change or Room persistence was introduced.
6. Added tests for representative nested character/equipment mapping, stat values, zero roll counts, and incomplete equipment.

### Verification

**Repository and commits**
- Branch: `main`
- Typed model, mapper, tests, and initial milestone record: `870a858efcc73ebb06fe1c3768bcdbab37b1b188`.
- Nullable weapon mapping correction: `ed59b95a5e089fce14f500c019ca42ddce0e51a9`.
- Daily Note metadata nullability correction: `aa03efbdbb3c94c9ee173eede67e0b2b045d4bf6`.

**GitHub Actions**
- Workflow: Build Debug APK
- Run: [#113](https://github.com/berao609/Irminsul-codex/actions/runs/45521170785)
- Run ID: `45521170785`
- CI-triggering commit: `aa03efbdbb3c94c9ee173eede67e0b2b045d4bf6`
- Overall result: **Success**
- Unit tests: **Success**
- Debug APK build: **Success**
- Persistent Debug signature verification: **Success**
- Artifact upload: **Success**
- Temporary Debug keystore cleanup: **Success**

Run #111 and #112 exposed Kotlin compilation errors in the initial implementation and the first correction. Those errors were corrected in the commits above; Run #113 passed on the final source commit.

- Device verification: not required for this in-memory mapper increment.
- No raw export schema change, Room persistence, UI integration, or release was included.

### Qualifications

- The normalized fields are limited to those observed in the supplied export structure.
- Artifact set metadata, skill affix payloads, and enhancement index arrays remain available as `StructuredData` rather than being prematurely interpreted.
- The raw source sections remain the preservation path for fields not yet modeled.
- This milestone does not implement artifact scoring, build evaluation, weapon investment analysis, or account recommendations.



## Milestone 011 - Character-to-Equipment Relationship Integrity

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Preserve equipment availability states and validate character, weapon, artifact, slot, and stat-property relationships in the normalized in-memory model.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Objective

Ensure normalized character records do not confuse missing equipment data with a valid empty equipment collection, and expose structural inconsistencies without silently discarding the remaining valid records.

### Work implemented

1. Changed the typed weapon and artifact collection fields to explicit `DataValue` states:
   - Missing or explicit null source fields remain `Missing`.
   - A valid empty artifact array remains `Available(emptyList())`.
   - Wrong container shapes and weapons without valid identifiers become `Failed`.
2. Kept each weapon nested in its source character mapping, with tests covering multiple characters and distinct weapon IDs.
3. Added artifact integrity diagnostics for invalid source entries, duplicate artifact IDs, duplicate slots, invalid slot values, unoccupied slots among the five standard positions, and malformed sub-property entries.
4. Kept valid artifact records even when another artifact entry is malformed; the original raw source remains available in the export source sections.
5. Added character collection diagnostics for malformed entries and duplicate character IDs while retaining valid character records.
6. Treats a malformed top-level character container as failed rather than silently missing.

### Verification

**Repository and source**
- Branch: `main`
- Implementation, tests, and initial milestone record: `d7529dd889ecb99dc209801a7c421ef52e5bcbf9`.

**GitHub Actions**
- Workflow: Build Debug APK
- Run: [#115](https://github.com/berao609/Irminsul-codex/actions/runs/45522097294)
- Run ID: `45522097294`
- CI-triggering source commit: `d7529dd889ecb99dc209801a7c421ef52e5bcbf9`
- Overall result: **Success**
- Unit tests: **Success**
- Debug APK build: **Success**
- Persistent Debug signature verification: **Success**
- Artifact upload: **Success**
- Temporary Debug keystore cleanup: **Success**

**Generated Debug artifact**
- Artifact: `irminsul-codex-debug-v1.0.0-dev.115-vc117`
- Artifact ID: `14980741647`
- Size: `8,225,380` bytes
- Expiry: 2026-12-09 00:58 UTC

- Device verification: not required for this in-memory domain-model and mapper increment.
- No export schema change, Room persistence, UI integration, or release is included in this milestone.

### Qualifications

- Slot positions 1 through 5 are treated as the five standard artifact slots. Unoccupied slots are reported, not automatically treated as errors, because a character may legitimately have fewer than five equipped artifacts.
- Duplicate IDs or slots are surfaced as integrity diagnostics; records are not silently deduplicated.
- Stat values remain in their original string representation. This milestone does not parse, score, or infer artifact rolls.
- These checks validate the supplied export structure, not the completeness or correctness of HoYoLAB's upstream data.


## Milestone 012 - Character Build Data Validation

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Add deterministic validation for character progression, weapon properties, artifact stats, and character stat collections.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Objective

Produce a machine-readable validation report for normalized character builds so later investment and comparison features can distinguish structurally valid data from missing, malformed, or out-of-range values.

### Work implemented

1. Added a pure domain validation component with no JSON or Android dependencies.
2. Validates known progression ranges for character level, rarity, friendship, constellation count, weapon level, rarity, ascension, refinement, artifact level, rarity, and slot.
3. Validates stat property identifiers, numeric stat strings (including percentage-form values), and nonnegative roll counts.
4. Checks artifact main-stat presence, duplicate substat property types, main-stat/substat collisions, and excessive substat counts.
5. Surfaces existing M11 equipment-integrity diagnostics in the character build report.
6. Attaches the report to the unified account snapshot while preserving missing, unavailable, failed, and stale character-section states.
7. Validation reports issues without mutating or normalizing the source values.

### Verification

**Repository and source**
- Branch: `main`
- Implementation, tests, and initial milestone record: `d999eeb4591cff36344051b67c67dd329882102b`.

**GitHub Actions**
- Workflow: Build Debug APK
- Run: [#117](https://github.com/berao609/Irminsul-codex/actions/runs/45522906104)
- Run ID: `45522906104`
- CI-triggering source commit: `d999eeb4591cff36344051b67c67dd329882102b`
- Overall result: **Success**
- Unit tests: **Success**
- Debug APK build: **Success**
- Persistent Debug signature verification: **Success**
- Artifact upload: **Success**
- Temporary Debug keystore cleanup: **Success**

**Generated Debug artifact**
- Artifact: `irminsul-codex-debug-v1.0.0-dev.117-vc119`
- Artifact ID: `14980169357`
- Size: `8,239,325` bytes
- Expiry: 2026-12-09 01:05 UTC

- Device verification: not required for this in-memory domain validation increment.
- No investment scoring, recommendations, export schema change, Room persistence, UI integration, or release is included.

### Qualifications

- The validator checks the supplied export representation, not whether HoYoLAB returned every possible field or whether an upstream value is authoritative.
- Known game progression bounds are explicit validation rules; missing values are warnings rather than invented defaults.
- Numeric stat text is checked for parseability only. This milestone does not recompute displayed totals, infer artifact roll quality, or calculate build scores.
- Empty artifact arrays are accepted as valid source data. Unoccupied slots are not automatically treated as errors.
- A clean report means no detected validation errors, not proof that the build is optimal or complete.

