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
2. Record the exact branch, commit SHA, workflow run, and relevant evidence when available.3. Separate automated CI results from physical-device observations.
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
6. Attaches the report to the unified account snapshot while preserving missing, unavailable, failed, and stale character-section states.7. Validation reports issues without mutating or normalizing the source values.

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


## Milestone 013 - Deterministic Character Stat Reconciliation

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Reconcile source-reported base, additive, and final character/weapon stat values using decimal arithmetic.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Objective

Provide a deterministic report showing whether reported final stat values agree with the sum of their base and additive components, without changing the preserved source values.

### Work implemented

1. Added a pure domain reconciler using `BigDecimal`, avoiding binary floating-point arithmetic.
2. Reconciles character selected, base, extra, and element property collections, plus weapon main and sub properties when available.
3. Reports exact matches, differences within the displayed precision tolerance, mismatches, and values that cannot be checked.
4. Keeps original stat strings and reports the calculated expected final, signed difference, and tolerance separately.
5. Requires base, add, and final values to be present, parseable, and expressed in consistent units. Missing or malformed values are not replaced with zero.
6. Attaches the reconciliation report to the account snapshot while preserving missing, unavailable, failed, and stale character-section states.

### Rounding policy

- Tolerance is half of the smallest displayed unit in the reported `final` value.
- Example: `120` has tolerance `0.5`; `120.0` has tolerance `0.05`.
- This policy accounts for rounding of the reported final value only. It does not assume or compensate for rounding in the base or additive source values.
- Percentage values are compared only when all three components carry the percentage suffix.

### Verification

**Repository and source**
- Branch: `main`
- Implementation and initial milestone record: `88ef8d4a77f91ffabd671833e72a887d1231d6d0`.
- Rounding-test correction: `5c1017b6439633b32a6ce66e99491dfe333077d2`.

**GitHub Actions**
- Workflow: Build Debug APK
- Run: [#120](https://github.com/berao609/Irminsul-codex/actions/runs/45523893648)
- Run ID: `45523893648`
- CI-tested source commit: `5c1017b6439633b32a6ce66e99491dfe333077d2`
- Overall result: **Success**
- Unit tests: **Success**
- Debug APK build: **Success**
- Persistent Debug signature verification: **Success**
- Artifact upload: **Success**
- Temporary Debug keystore cleanup: **Success**

**Generated Debug artifact**
- Artifact: `irminsul-codex-debug-v1.0.0-dev.120-vc122`
- Artifact ID: `14981267234`
- Size: `8,246,105` bytes
- Expiry: 2026-12-09 01:12 UTC

- An earlier run #119 failed one rounding-boundary test because its expected tolerance did not match the documented precision rule. The test input and expected signed difference were corrected in the follow-up commit above; run #120 passed.
- Device verification: not required for this in-memory domain calculation increment.
- No investment scoring, recommendation logic, export schema change, Room persistence, UI integration, device install, or release is included.

### Qualifications

- Reconciliation checks arithmetic consistency of supplied fields; it does not establish that the upstream source values are complete or correct.
- A mismatch is a detected arithmetic difference under the documented precision policy, not proof of a game-data defect.
- Artifact main/substats expose a single `value` in the observed export shape and are not reconciled as base/add/final triplets.
- A not-checked result means the available fields do not support a safe comparison.
- The report does not mutate source JSON or normalized source strings.


## Milestone 014 - Stat Property Semantics and Classification

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Canonical classification of known character, weapon, and artifact stat properties.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Objective

Establish a deterministic stat-property catalog that distinguishes canonical identity, category, element, and flat versus percentage value semantics without rewriting source values or guessing unknown identifiers.

### Work implemented

1. Added a catalog for recognized Genshin FightProp names and the documented numeric FightPropType identifiers included in the implementation.
2. Added categories for core attributes, offensive, defensive, elemental, healing, energy, control, and utility properties.
3. Added flat and percentage value-kind metadata, plus element labels for elemental damage bonuses and resistances.
4. Added classification outcomes: classified, unknown, and conflict.
5. Source stat names and numeric property identifiers must agree when both resolve to known definitions. Conflicts remain unresolved.
6. Added an account-level classification report covering character property collections, equipped weapon properties, and equipped artifact main/substats.
7. Preserved unknown source identifiers and original numeric/string values.

### Verification

**Repository and source**
- Branch: `main`
- Initial implementation commit: `18394b2` (short SHA as recorded during implementation).
- Test expectation correction: `0fe078c645dc689203245e245b86edf6cede7dd8`.
- CI-triggering documentation commit: `c31c220190833e2336c833c8c16184b00431756e`.

**GitHub Actions**
- Workflow: Build Debug APK
- Run: [#125](https://github.com/berao609/Irminsul-codex/actions/runs/45524689622)
- Run ID: `45524689622`
- CI head commit: `c31c220190833e2336c833c8c16184b00431756e`
- Overall result: **Success**
- Unit tests: **Success**
- Debug APK build: **Success**
- Persistent Debug signature verification: **Success**
- Artifact upload: **Success**
- Temporary Debug keystore cleanup: **Success**

**Generated Debug artifact**
- Artifact: `irminsul-codex-debug-v1.0.0-dev.125-vc127`
- Artifact ID: `14980818679`
- Size: `8,259,767` bytes
- SHA-256: `0db52e507318c2305d0d61c0d6acfc2b5b48453b014f8d07194df32cf1ca1730`
- Expiry: 2026-12-09 01:19 UTC

- The earlier Run #122 exposed an incorrect test expectation for the number of classified properties (six were supplied, not five). The assertion was corrected in source commit `0fe078c645dc689203245e245b86edf6cede7dd8`; Run #125 passed.
- Device verification: not required for this in-memory classification and mapper increment.
- No stat scoring, build recommendations, damage simulation, value conversion, source JSON mutation, export schema change, Room persistence, UI integration, device install, or release is included.

### Qualifications

- The catalog is an initial bounded registry, not an exhaustive guarantee of every internal or future game property.
- Numeric identifiers are based on community-maintained game data definitions; the application retains unknown values instead of assuming a meaning.
- Flat/percentage classification describes the property semantic, not a transformation to apply to its stored value.
- The catalog does not infer whether a stat is optimal for a specific character or build.
- Some internal/derived properties are intentionally not catalogued in this increment.


## Milestone 015 - Account Snapshot Coverage and Consistency Assessment

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Derive a deterministic section-level coverage and metadata/payload consistency report from the unified account snapshot.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Objective

Give downstream account analytics a reliable way to distinguish which selected export sections contain usable payloads and detect contradictions between section metadata and the normalized payload state.

### Work implemented

1. Added explicit payload-state classifications for present, stale, missing, unavailable, and failed sections.
2. Added a deterministic assessment across the nine currently modeled export sections.
3. Detects four metadata/payload contradictions: successful status without payload, failed status with payload, skipped status with payload, and payload without section metadata.
4. Preserves section source and retrieval timestamp in the coverage result.
5. Exposes aggregate section counts and a consistency flag without modifying the original export or treating selected-section coverage as complete account coverage.
6. Added unit tests for metadata preservation, successful-but-missing payloads, untracked payloads, skipped contradictions, and distinct unavailable/failed/stale states.

### Verification

- Implementation, tests, and initial milestone record: `a8450278ecff6b57c6cdf2b6df65863d0a94ff77`.
- GitHub Actions Run #128: https://github.com/berao609/Irminsul-codex/actions/runs/45525575660 (success; unit tests, Debug APK build, persistent Debug signature verification, artifact upload, and temporary keystore cleanup all passed).
- Debug artifact: `irminsul-codex-debug-v1.0.0-dev.128-vc130` (ID `14981124189`), 8,265,438 bytes; expires 2026-12-09 01:26:10 UTC.
- Verification record commit: `0ad5f357afe0b6f5a2d757748bf844fe3a7352d5`.
- Device verification: not required for this pure in-memory domain assessment and mapper integration.

### Qualifications

- The report covers only the nine sections currently represented by AccountSnapshot; it does not imply that every Genshin account domain or inventory is retrieved.
- PRESENT means the export contains a payload object/array/value, not that every field inside it is populated or valid.
- A consistency issue identifies disagreement between export metadata and retained payload state; it does not determine which side is authoritative.
- No UI, persistence, new HoYoLAB endpoint, export schema change, investment scoring, recommendation, or AI behavior is included.


## Milestone 016 - Section Freshness and Temporal Assessment

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Add deterministic, policy-driven age assessment for the nine modeled account sections.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Objective

Expose section retrieval age and freshness without assuming every account section has the same update cadence or inventing a universal stale threshold.

### Work implemented

1. Added per-section freshness assessment with FRESH, STALE, UNKNOWN, and INVALID_TIMESTAMP outcomes.
2. Calculates section age against an explicit assessment time, defaulting to the snapshot export timestamp.
3. Accepts caller-supplied nonnegative thresholds per section. A section with no configured threshold remains UNKNOWN while still exposing its age.
4. Preserves explicit stale payload state as STALE and identifies retrieval timestamps later than the assessment time as invalid.
5. Attaches the report to the mapped AccountSnapshot without changing raw export data.
6. Added tests for configured and unconfigured thresholds, fresh/stale/missing/future timestamps, and invalid negative policy values.

### Verification

- Implementation, tests, and initial milestone record: `41e5a1e4b80b45fbf1220d6a0674184501661819`.
- Implementation CI: [Run #131](https://github.com/berao609/Irminsul-codex/actions/runs/45527293731), success for `41e5a1e4b80b45fbf1220d6a0674184501661819`. Unit tests, Debug APK build, persistent signing verification, artifact upload, and temporary keystore cleanup all passed.
- Documentation CI: [Run #132](https://github.com/berao609/Irminsul-codex/actions/runs/45527299268), success for `e65adb55ab2a5d4c97e646734941c8455eb19d8b`. All workflow steps passed.
- Run #131 Debug artifact: `irminsul-codex-debug-v1.0.0-dev.131-vc133`, artifact ID `14982213693`, size 8,270,720 bytes, expires 2026-12-09 01:40:12 UTC.
- Run #132 Debug artifact: `irminsul-codex-debug-v1.0.0-dev.132-vc134`, artifact ID `14983386316`, size 8,270,709 bytes, expires 2026-12-09 01:40:24 UTC.
- Device verification: not required for this pure in-memory domain assessment and mapper integration.

### Qualifications

- No default freshness durations are asserted; product-specific thresholds remain a policy decision.
- Age is measured from section retrieved_at metadata, falling back to the typed payload retrieval timestamp already represented by the M15 coverage report.
- An explicit DataValue.Stale state remains stale regardless of configured threshold.
- A future retrieval timestamp is flagged relative to the assessment time; it is not silently clamped or rewritten.
- This increment does not add historical persistence, UI, synchronization scheduling, export schema changes, investment scoring, recommendations, or AI behavior.

## Milestone 017 - Last Successful Retrieval Provenance

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Preserve last-successful retrieval timestamps through section coverage and use them as the freshness reference when a current payload is unavailable.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Objective

Ensure a failed or unavailable refresh does not erase the temporal context of the last successful section retrieval. Keep current retrieval-attempt time distinct from last-successful payload time.

### Work implemented

1. Extended section coverage records to retain both retrieved_at and last_successful_retrieval_at.
2. Added an explicit freshness timestamp source: current retrieval, last successful retrieval, or none.
3. Freshness assessment uses the current retrieval timestamp when a current payload is present. If no current payload is present, it prefers the last successful retrieval timestamp and retains the payload state separately.
4. Exposes the current retrieval time, last-successful retrieval time, selected reference time, payload state, and age together so a recent failed attempt cannot be mistaken for a recent successful payload.
5. Added tests for coverage metadata preservation, failed-refresh fallback, current-payload timestamp selection, and absence of any usable timestamp.

### Verification

**Implementation**
- Implementation commit: `e5b4be8c9007cc5e8ee6aeb26ae6110dcee283be`.
- The implementation commit was tested directly by [GitHub Actions Run #134](https://github.com/berao609/Irminsul-codex/actions/runs/45527887445), which completed successfully.

**Final repository state and CI**
- Documentation update commit: `3e8db49f149d4086febbc34f1583cbcf3142eb3f`.
- [GitHub Actions Run #135](https://github.com/berao609/Irminsul-codex/actions/runs/45528169763) completed successfully for that commit.
- Verified steps: unit tests, Debug APK build, persistent Debug APK signature verification, artifact upload, and temporary Debug keystore cleanup.
- Debug artifact: `irminsul-codex-debug-v1.0.0-dev.135-vc137`.
- Artifact ID: `14983622326`.
- Artifact size: 8,272,070 bytes.
- SHA-256: `ef4616d46a8b8ab9ac9f801e4c0a59d35193dfe03806c36d3c35cb6d20468e38`.
- Artifact expiry: 2026-12-09 01:47:50 UTC.
- Device verification: not required for this in-memory domain assessment.

### Qualifications

- Freshness remains policy-driven; no default thresholds are introduced.
- A freshness status describes the age of its selected timestamp, not whether the current refresh succeeded. Consumers must inspect payloadState and timestampSource together.
- The selected sections and export schema are unchanged.
- This increment does not add historical storage, retry scheduling, UI, investment scoring, recommendations, or AI behavior.

## Milestone 018 - Initial Account Repository Boundary

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Introduce a domain-facing account repository contract and a HoYoLAB-backed adapter that returns the unified AccountSnapshot.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Objective

Create the first repository boundary between application/domain consumers and the existing HoYoLAB export implementation. Keep the current network workflow and export format intact while allowing the domain model to be retrieved without exposing JSON parsing details to future consumers.

### Work implemented

1. Added the domain `AccountRepository` contract, returning `Result<AccountSnapshot>` and forwarding optional progress updates.
2. Added `HoYoLabAccountRepository` in the data layer. It delegates to the existing HoYoLAB export workflow, maps the returned JSON through `AccountExportMapper`, and exposes the normalized domain snapshot.
3. Made export and mapping functions injectable so repository behavior can be tested without network access or real account credentials.
4. Preserved export failures without invoking the mapper, converted malformed export JSON into a failed result, and retained coroutine cancellation semantics.
5. Kept existing UI and synchronization/export flows unchanged; this milestone establishes the boundary but does not yet migrate MainActivity to it.

### Verification

- Implementation commit: `9c1c890a5b07c3de083732d8129697055577cf21`.
- [GitHub Actions Run #138](https://github.com/berao609/Irminsul-codex/actions/runs/45528893954) succeeded for commit `6a32a99cfe32086a83751ba12aa79b6a0063e356`, which includes the M18 implementation and milestone record.
- Verified steps: unit tests, Debug APK build, persistent Debug APK signature verification, artifact upload, and temporary Debug keystore cleanup.
- Debug artifact: `irminsul-codex-debug-v1.0.0-dev.138-vc140`.
- Artifact ID: `14983078837`.
- Artifact size: 8,275,368 bytes.
- SHA-256: `b9fe4dde1f5afde05583e48e0b75af59465b08fe660f40bcfe9e746542762a01`.
- Artifact expiry: 2026-12-09 01:53:56 UTC.
- Physical-device verification: not required because this increment adds an unused repository adapter and does not change the app's runtime path.

### Qualifications

- The repository currently accepts UID and cookie as arguments because credential ownership remains in the existing presentation layer. This is transitional and must be replaced by a dedicated credential/session boundary before broader repository use.
- The adapter currently wraps the existing full JSON export operation; it does not introduce a new HoYoLAB endpoint or local persistence.
- MainActivity still calls HoYoLabClient directly. UI migration, ViewModel introduction, credential security, Room, historical snapshots, and release work are outside this increment.
 
## Milestone 019 - Account Export ViewModel Integration

**Date recorded:** 2026-11-25  
**Development phase:** Phase 1 - Establish the Unified Account Model  
**Milestone scope:** Route the account export action through AccountRepository and an Android ViewModel while preserving the original JSON for file saving.  
**Status:** Implemented, unit-test verified, and Debug CI verified. Save JSON device verification passed; Copy JSON was removed by product decision.

### Objective

Move full account export orchestration and normalized snapshot state out of the Compose screen. Keep the user-facing Save JSON action, progress feedback, and export status messages. Clipboard export is not required for the product. Avoid duplicate HoYoLAB requests by returning the normalized snapshot and exact original JSON from the same repository operation.

### Work implemented

1. Added domain `AccountExport`, containing the normalized `AccountSnapshot` and the exact original JSON string.
2. Updated `AccountRepository` and `HoYoLabAccountRepository` to return both representations from one export request. Mapping failures become repository failures, and `CancellationException` is rethrown.
3. Added `AccountViewModel` with observable StateFlow state for loading, progress, errors, the latest normalized snapshot, and a pending raw export for the UI to consume.
4. Connected MainActivity to the ViewModel. Save JSON uses the exact raw export string through Android's document picker. Copy JSON and all clipboard handling were removed after the accepted product decision that JSON export is a secondary file-based utility.
5. Added ViewModel tests for successful state publication, raw JSON preservation, repository failure, and blank-credential rejection. Updated repository tests for the combined export result.
6. Added AndroidX ViewModel and kotlinx-coroutines-test dependencies. The ViewModel now exposes a single file-export flow; it no longer carries a clipboard/save mode flag.
7. Kept Daily Note synchronization, login, and credential handling unchanged. This increment does not yet migrate those paths to repositories or move credentials out of the current presentation code.

### Verification

**Implementation**
- Main implementation commit: `415291683a4f7a9678a20669ec6ad2442bf4754e`.
- Corrective test commit: `68d019c85ec25e86175c0c20e1c6414ca1045e9b`.
- Initial CI Run #140 failed at unit-test compilation because three repository tests still referenced the previous `fetchAccountSnapshot` method after the contract changed to `fetchAccountExport`. The compiler output identified all three stale references.
- Those references were corrected and the missing `AccountExport` import was added in the corrective commit.

**Successful CI**
- [GitHub Actions Run #141](https://github.com/berao609/Irminsul-codex/actions/runs/45530665510) completed successfully for corrected commit `68d019c85ec25e86175c0c20e1c6414ca1045e9b`.
- Verified steps: unit tests, Debug APK build, persistent Debug APK signature verification, artifact upload, and temporary Debug keystore cleanup.
- Debug artifact: `irminsul-codex-debug-v1.0.0-dev.141-vc143`.
- Artifact ID: `14985051134`.
- Artifact size: 8,280,431 bytes.
- SHA-256: `610ef60be02b64c79e0ca678a27ae1baa4ce4ea163813854c068c49d14d31d10`.
- Artifact expiry: 2026-12-09 02:09:15 UTC.
- Physical-device verification: user confirmed Save JSON succeeds. Copy JSON was subsequently removed by explicit product decision because it duplicated the JSON file contents and was not required for the primary account-intelligence workflow.
- The clipboard-size safeguard and clipboard exception handling are therefore removed with the feature. No Copy JSON device retest is required; Save JSON remains the sole user-facing JSON export action.

### Qualifications

- The ViewModel receives UID and cookie as arguments because credential ownership remains in the existing activity/presentation flow. This is transitional and does not constitute secure credential storage.
- Account export still uses the existing HoYoLAB endpoints and selected-section JSON format.
- No Room persistence, historical snapshots, new analytics, new UI screens, or official Release changes are included.


## Product Decision 001 - Account Intelligence as the Primary Product

**Date:** 2026-11-25  
**Decision status:** Accepted  
**Scope:** Product positioning, user-facing priority, and development sequencing.

### Decision

Irminsul Codex will be developed and presented primarily as an **AI-integrated Genshin Impact account intelligence and progression analyzer**, rather than as a data extractor whose main purpose is to produce JSON for third-party readers.

### Consequences

1. The main user journey should be account connection/synchronization followed by in-app account overview, analysis, and progression guidance.
2. Account retrieval remains a foundational data-input capability, but should not dominate the product's navigation or identity.
3. Deterministic application logic remains authoritative for calculations, validation, resource requirements, and rule-based assessments.
4. The integrated BYOAI assistant should explain and help explore account intelligence using relevant account context. It must not replace deterministic calculations or invent missing account data.
5. Account data should be managed locally and made available to in-app features without requiring a JSON export step.
6. JSON export remains supported as an optional utility for third-party readers, backups, and external analysis. Existing data-preservation and versioning requirements remain applicable.
7. The Account Command Center should present insights, priorities, and relationships as the primary experience; export controls should be secondary.
8. External AI providers must never receive HoYoLAB cookies or other authentication credentials. Account context must only be transmitted under explicit user-configured provider settings and appropriate user control.

### Development priority

Prioritize, in order: reliable unified account data and local persistence; deterministic account analysis; the integrated BYOAI interface and provider boundary; the Account Command Center; then optional import/export improvements as supporting workflows. This is a priority direction, not a claim that these capabilities are already implemented.

### Scope and non-goals

This decision does not authorize removing the existing JSON exporter, discarding raw source preservation, changing the account export schema, introducing a mandatory backend, or immediately building every AI and analytics feature. It does not change the native Android, local-first, layered architecture, BYOAI, or deterministic-first principles already agreed.


## Milestone 020 - Room Account Snapshot Persistence Foundation

**Date recorded:** 2026-11-25  
**Development phase:** Phase 2 - Local Account Persistence  
**Milestone scope:** Introduce Room and automatically persist successful account exports as immutable, historical raw JSON snapshots associated with an account UID and server.  
**Status:** Implemented, unit-test and Android instrumentation-test verified, Debug CI verified, and physical-device verified (2026-11-25).

### Objective

Establish the first local persistence layer without prematurely duplicating the complete domain model into relational tables. Preserve each exact source export so future features can remap it as the domain model evolves.

### Work implemented

1. Added Room 2.6.1 with KSP 2.0.0-1.0.21 and configured Room schema export to `app/schemas`, with CI verification and a dedicated schema artifact for the initial generated version.
2. Added the initial Room database with two entities: an account identity keyed by UID and server, and an immutable account snapshot linked by a composite foreign key.
3. Each snapshot stores a generated snapshot ID, export timestamp, export format version, overall retrieval status, source origin, and the exact original JSON string. No HoYoLAB cookies or AI credentials are stored in Room.
4. Added transactional snapshot persistence. Account creation, last-updated timestamp maintenance, and snapshot insertion occur in one database transaction. Existing account creation timestamps are preserved.
5. Added a domain `AccountSnapshotStore` contract and Room implementation supporting snapshot saving and history retrieval. History is ordered newest first.
6. Connected successful ViewModel account retrieval to local persistence. A storage failure does not discard the fetched export or block the existing Save JSON flow; it is surfaced as a warning.
7. Added Android instrumentation tests for multiple snapshots, raw JSON preservation, account identity, timestamps, and history ordering. CI runs them on an API 34 emulator. Added ViewModel coverage proving that local database failure does not discard a successfully retrieved export or block file saving.
8. No destructive migration fallback was added. Database schema version starts at 1; future schema changes must introduce explicit migrations.
9. At the time of M20 verification, the GitHub-hosted runner required TCG software CPU emulation because KVM was not accessible to the workflow process. A later CI optimization, recorded after M21, corrected KVM device permissions and verified hardware acceleration on the same standard `ubuntu-latest` runner; see the CI Optimization Record below.

### Verification

**Implementation commits**
- Initial Room persistence implementation: `ab93141534891a954bff7895087ecb63eb1421ea`.
- ViewModel test wiring correction: `8dfe97fdb9759cb7c56fd39dd503562574bc3033`.
- Test dependency correction made during the abandoned Robolectric approach: `d06094a41b5a889cda449542313a6a0f1e7aa2bb`.
- Android instrumentation test migration: `821caf8e466bfae6ffe7b3139d401de8f0a7f67a`.
- CI software-emulation correction: `13a648e1938b8907aff2ef139c820a7b43c2c98d`.

**Successful CI**
- [GitHub Actions Run #152](https://github.com/berao609/Irminsul-codex/actions/runs/45538191685) completed successfully for exact commit `13a648e1938b8907aff2ef139c820a7b43c2c98d`.
- Verified steps: JVM unit tests, API 34 Android emulator startup, Room instrumentation tests, Debug APK build, Room schema snapshot generation and verification, schema artifact upload, persistent Debug APK signature verification, APK artifact upload, and temporary Debug keystore cleanup.
- The JVM unit-test step passed. The earlier Run #151 recorded 90 passing JVM tests; Run #152 reran the same unit-test task successfully.
- Room instrumentation tests completed successfully on the API 34 emulator using TCG software CPU emulation.
- Generated schema: `app/schemas/com.irminsul.codex.data.local.IrminsulDatabase/1.json`.
- Schema artifact: `irminsul-room-schema-v1-run-152`; artifact ID `14988124310`; size 985 bytes; SHA-256 `6390d484346e4843fa440bedeed6aa165e9e430f84ad82883b37f9306a9792b9`; expires 2026-12-09 03:28:27 UTC.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.152-vc154`; artifact ID `14988374157`; size 8,456,536 bytes; artifact archive SHA-256 `866fb157a96d49aecc0cc38e7914a3a5c694ba67cc35457dc68cafc7d4ce7b78`; expires 2026-12-09 03:28:29 UTC.
- APK signature verification passed using APK Signature Scheme v2; one signer was reported.
- CI emitted a non-blocking notice that Gradle 8.14.8 is out of date. This did not fail the run. Gradle version modernization is not part of M20 and should be handled separately after checking plugin and Android Gradle Plugin compatibility.
- **Physical-device verification (2026-11-25):** The user installed the Run #152 Debug APK as an update and reported that it works fine. This confirms the app's basic post-update runtime behavior on the user's Android device. The user did not separately report a specific process-kill/relaunch test or inspect database rows directly; direct persistence correctness remains supported by the passing Room instrumentation tests, not by a user-visible history screen (none exists yet).

### Qualifications

- This milestone stores the exact selected-section export JSON as the canonical persisted source. It does not claim that the existing export is a complete account inventory or raw API archive.
- The normalized `AccountSnapshot` is reconstructed from raw JSON when history is read; it is not yet duplicated into separate character, weapon, artifact, or resource tables.
- User-facing account history browsing, snapshot comparison, retention controls, encrypted credential storage, and migration beyond schema version 1 are outside this milestone.
- Room uses app-private storage but has no additional database-level encryption configured. HoYoLAB cookies remain in ordinary SharedPreferences and are not encrypted by this milestone.


## Milestone 021 - Deterministic Character Build Assessment Foundation

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Milestone scope:** Convert existing deterministic character-build validation results into prioritized, traceable, actionable domain findings.  
**Status:** Implemented, unit-test verified, and Debug CI verified (Run #155). No user-facing analysis screen is included.

### Objective

Establish the first account-intelligence analysis output without inventing build scores, relying on AI for calculations, or treating missing data as a valid empty result.

### Work implemented

1. Added a domain report that explicitly distinguishes available, stale, missing, unavailable, and failed validation inputs.
2. Added deterministic character-build findings derived from the existing `CharacterBuildValidator` report. Every finding retains its character ID, optional character name, issue code, severity, field path, source message, and a suggested next action.
3. Findings are ordered deterministically, with errors before warnings and stable character/path/code ordering.
4. Stale validation inputs remain marked stale on every derived finding.
5. An available empty validation report remains distinct from missing or unavailable validation data.
6. The analyzer does not assign a numerical build score, infer meta strength, compare against external builds, modify account data, or automatically repair anything.
7. Updated README to distinguish this domain foundation from the future user-facing account intelligence experience.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/model/CharacterBuildAnalysis.kt` (new)
- `app/src/main/java/com/irminsul/codex/domain/analysis/CharacterBuildAnalyzer.kt` (new)
- `app/src/test/java/com/irminsul/codex/domain/analysis/CharacterBuildAnalyzerTest.kt` (new)
- `README.md`

### Verification

- Implementation commit: `495488f7f4438615205a02a090c4d9da2083c79b` (`feat: add deterministic character build assessment`).
- [GitHub Actions Run #155](https://github.com/berao609/Irminsul-codex/actions/runs/45542508297) completed successfully for that exact commit.
- The successful workflow verified JVM unit tests, Debug APK assembly, persistent Debug APK signature verification, artifact upload, and temporary Debug keystore cleanup. Room instrumentation tests were not required for this domain-only change under the path filter.
- Physical-device verification: not required because this increment adds domain logic and tests without changing the runtime UI or workflow.

### Qualifications

- Findings are only as complete as the selected account data and the existing validation rules.
- This is a first deterministic assessment pipeline, not a complete character build optimizer or account-wide progression recommendation engine.
- It does not yet analyze artifact substat quality, team fit, rotation performance, enemy matchups, or user goals.
- No user-facing UI currently displays these findings. A later milestone must connect the report to the Account Command Center or a dedicated account analysis screen.



## CI Optimization Record - Post-M21

**Date recorded:** 2026-11-25  
**Scope:** Reduce Room instrumentation-test emulator startup time without using paid or larger runners.  
**Runner:** Standard GitHub-hosted `ubuntu-latest`; no paid runner tier was introduced.

### Changes and verification

1. Commit `f15e203d918301d140f994486ff030b50c5c5e04` introduced path filtering so Room instrumentation tests can be skipped for unrelated changes while remaining mandatory for relevant persistence paths and manual workflow dispatch. [Run #157](https://github.com/berao609/Irminsul-codex/actions/runs/45544452026) passed. Because the workflow file itself changed, the emulator tests correctly ran for that workflow change.
2. Commit `0b567f9e58b21c826088dbb0446e183f9bb49578` added conditional KVM detection and a TCG fallback. [Run #158](https://github.com/berao609/Irminsul-codex/actions/runs/45546433313) passed, but logs showed that KVM existed and was inaccessible to the runner process, so TCG was still used. Emulator boot took approximately 767 seconds.
3. Commit `502b3e91bf19d89b71f5209261d9973200b62e20` applied permissions to the existing `/dev/kvm` device and verified access by opening it and querying `KVM_GET_API_VERSION`. [Run #159](https://github.com/berao609/Irminsul-codex/actions/runs/45550142783) passed. Logs confirmed that KVM opened successfully with API version 12 and hardware acceleration was available.
4. On Run #159, the emulator booted in approximately 25.6 seconds, compared with approximately 767 seconds under TCG in Run #158. The Room instrumentation Gradle task completed successfully in approximately 38 seconds, with both instrumentation tests passing.

### Current CI behavior

- Keep the standard `ubuntu-latest` runner.
- Use KVM when the device is present, accessible, and passes the API-version check.
- Retain TCG as a fallback when KVM is unavailable or unusable.
- Keep Room instrumentation path filtering.
- Do not change the working KVM setup without new evidence of a problem.
- AVD snapshot caching remains a possible future optimization, not an implemented change.


## Milestone 022 - Account-Level Character Build Priority Summary

**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Objective

Turn M21's traceable character-level findings into a concise account-level overview that identifies which characters need attention first and why.

### Planned scope

1. Group existing character-build findings by character.
2. Count ERROR and WARNING findings per affected character.
3. Classify affected characters using transparent rule-based categories: `NEEDS_ATTENTION` when at least one ERROR exists, otherwise `REVIEW` when at least one WARNING exists.
4. Sort affected characters deterministically by ERROR count descending, WARNING count descending, then character ID ascending.
5. Expose account-level analyzed-character, affected-character, error, and warning counts.
6. Preserve AVAILABLE, STALE, MISSING, UNAVAILABLE, and FAILED states. Mark stale-derived summaries as stale.
7. Retain the underlying traceable findings so every summary can be explained.
8. Do not assign a numerical score, classify characters with no findings as objectively strong, infer meta value, or invent missing data.

### Implementation

- Implementation commit: `74aba79a9e41c6fb9238fba83fa08fbe21a8e967` (`feat: add account-level character build priorities`).
- Added `domain/model/AccountCharacterBuildSummary.kt` for account summary and per-character priority models.
- Added `domain/analysis/CharacterBuildAccountSummaryAnalyzer.kt` to group M21 findings, calculate severity counts, and apply stable priority ordering.
- Added `domain/analysis/CharacterBuildAccountSummaryAnalyzerTest.kt` with four tests covering grouping and ordering, empty reports, stale data, and missing/unavailable/failed states.
- [GitHub Actions Run #162](https://github.com/berao609/Irminsul-codex/actions/runs/45551342864) completed successfully for exact implementation commit `74aba79a9e41c6fb9238fba83fa08fbe21a8e967`.
- Verified: JVM unit tests, Debug APK assembly, Room schema snapshot generation and upload, persistent Debug APK signature verification, Debug APK artifact upload, and temporary Debug keystore cleanup. Room instrumentation tests were correctly skipped by the existing path filter because this milestone changes only domain models, analysis logic, and JVM tests.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.162-vc164`; artifact ID `14994715100`; size 8,468,895 bytes; SHA-256 `ec2e72d4dac6aa0124724d285dfbdf602930010833aeef51db8787f82a073e26`; expires 2026-12-09 05:08:39 UTC.
- Room schema artifact: `irminsul-room-schema-v1-run-162`; artifact ID `14993422968`; size 985 bytes; SHA-256 `1674a5fbba4ba89eac492c48bee9e6b40f47b929725ac62f748ca7cbb1ee1883`; expires 2026-12-09 05:08:37 UTC.
- Physical-device verification: not required; this is a domain-only increment with no UI or runtime workflow change.

### Acceptance criteria

- An available empty report produces a valid empty account summary, not a missing-data state.
- Missing, unavailable, and failed validation inputs remain distinct.
- Stale findings remain visibly marked stale in the summary.
- Character grouping and priority ordering are deterministic.
- Aggregate counts match the underlying findings.
- Unit tests cover grouping, priority categories, ordering, empty reports, and all input availability states.
- The existing application workflow remains unchanged; no UI is included in this milestone.

## Milestone 023 - Explainable Character Stat Reconciliation Findings

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Milestone scope:** Convert existing character stat-reconciliation mismatches into traceable, explainable account findings.  
**Status:** Implemented, unit-test verified, and Debug CI verified.

### Objective

Build on M13's existing deterministic arithmetic reconciler. Turn only confirmed `MISMATCH` reconciliation results into explanatory findings, without changing the arithmetic or treating discrepancies as proof of incorrect upstream data.

### Work implemented

1. Added stat-analysis domain models with explicit `AVAILABLE`, `STALE`, `MISSING`, `UNAVAILABLE`, and `FAILED` input availability.
2. Added a domain analyzer that consumes the existing reconciliation report and produces findings only for `MISMATCH` results. `MATCH`, `WITHIN_ROUNDING_TOLERANCE`, and `NOT_CHECKED` remain non-findings.
3. Each finding preserves character ID and optional name, field path, property type, stat name, original base/add/final strings, calculated expected final, signed difference, and rounding tolerance.
4. Each finding includes a plain-language arithmetic explanation and a practical verification action. It does not declare which source value is wrong.
5. Stale reconciliation, stale character data, and stale weapon data are marked on derived findings. Missing, unavailable, and failed reconciliation inputs remain distinct.
6. Findings are ordered deterministically by character ID, field path, and stat name.
7. No score, external meta comparison, automatic repair, UI, or change to M13's calculation/rounding policy was introduced.

### Files added

- `app/src/main/java/com/irminsul/codex/domain/model/CharacterStatAnalysis.kt`
- `app/src/main/java/com/irminsul/codex/domain/analysis/CharacterStatAnalyzer.kt`
- `app/src/test/java/com/irminsul/codex/domain/analysis/CharacterStatAnalyzerTest.kt`

### Verification

- Model commit: `1fd6831c3df7c4df181695d1ba674a30528c3d5c`
- Analyzer commit: `d83b5994bbf3338dc5fdc4911fd8105071787150`
- Unit-test commit: `a15234b846849fcd21ce9e4b094b099e2c8785bd`
- [GitHub Actions Run #165](https://github.com/berao609/Irminsul-codex/actions/runs/45552276639) succeeded.
- [GitHub Actions Run #166](https://github.com/berao609/Irminsul-codex/actions/runs/45552280557) succeeded.
- [GitHub Actions Run #167](https://github.com/berao609/Irminsul-codex/actions/runs/45552288840) succeeded.
- All three runs completed successfully. The implementation, analyzer, and unit-test commits were each CI-verified. Run #167 verified the unit-test commit and full applicable workflow steps.
- Seven unit tests cover mismatch evidence/explanation, no findings for matches/tolerance, unchecked values, stale-data markers, missing/unavailable/failed states, empty reports, and stable ordering.
- Physical-device verification: not required; this milestone adds domain analysis and tests without UI or device workflow changes.

### Qualifications

- Findings reflect M13's arithmetic and tolerance policy and are limited to the reconciliation data available in the selected account export.
- A mismatch is a detected arithmetic discrepancy, not proof that the game, upstream service, or any particular field is defective.
- This analyzer does not yet combine stat integrity with build validation or account completeness. That unified character assessment is the next planned milestone (M24).
- No user-facing analysis screen currently displays these findings.



## Milestone 024 - Unified Character Assessment

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Milestone scope:** Combine character build validation, stat integrity findings, and character data completeness into one deterministic assessment per character.  
**Status:** Implemented, unit-test verified, and Debug CI verified (Run #175).

### Objective

Provide a unified, explainable character assessment without inventing a score or treating missing information as a healthy result.

### Work implemented

1. Added assessment models covering availability, completeness level, attention status, completeness evidence, per-character assessment, and account-level report.
2. Combined M21 build-validation findings and M23 stat-mismatch findings per character, while keeping the original evidence intact.
3. Added explicit completeness evidence for character level and rarity, weapon, artifacts, and relevant stat collections. Missing or unverified components are listed rather than treated as zero or confirmed complete.
4. Classified characters as `NEEDS_ATTENTION` for build errors or stat mismatches, `REVIEW` for warnings or incomplete data, and `CLEAR` only when no identified build/stat/completeness concerns remain.
5. Kept stat mismatches separate from completeness evidence. A numerical mismatch is an integrity finding, not a missing-data indicator.
6. Preserved stale-data markers and distinct missing, unavailable, and failed input states.
7. Applied deterministic ordering so the highest-attention assessments appear first, with stable secondary ordering.
8. No score, meta ranking, combat-strength rating, automatic repair, or UI was added.

### Implementation

- Model commit: `76212e48186303364d3ba3fea5901084f8a824f3`.
- Analyzer commit: `8c5ac30d217320e5c96fe2db7f6a6466568dcf46`.
- Test commits: `2d29e1355917bfe70c8c5b0f07179ccb669764fb` and `0ae05c72b9fb80cf0b1a9a8d958cfd25fcbb5ed`.
- Corrected test report constructor commit: `5f683bfa70d68f038890072957a017b007c23942`.
- Analyzer evidence correction: `9a116d08f9a2e7aaae04fc5ab2e841837aa0c356` (stat mismatches are not completeness gaps).
- Added `UnifiedCharacterAssessment.kt`, `UnifiedCharacterAssessmentAnalyzer.kt`, and `UnifiedCharacterAssessmentAnalyzerTest.kt`.

### Verification

- [GitHub Actions Run #170](https://github.com/berao609/Irminsul-codex/actions/runs/45552796697) succeeded for the model commit.
- [GitHub Actions Run #171](https://github.com/berao609/Irminsul-codex/actions/runs/45552801836) succeeded for the analyzer commit.
- Runs #172, #173, and #174 failed during test compilation because the test helper passed its insights list positionally into the report's integer `matchCount` parameter. The failure was corrected by using the named `insights` argument.
- [GitHub Actions Run #175](https://github.com/berao609/Irminsul-codex/actions/runs/45553232661) succeeded for commit `5f683bfa70d68f038890072957a017b007c23942`, verifying the corrected test code and applicable workflow.
- Physical-device verification: not required. This milestone adds domain logic and tests without a user-facing UI or runtime workflow change.

### Qualifications

- Assessment coverage depends on the account data and source-analysis availability supplied to the analyzer. It is not a full character optimizer or combat-performance evaluation.
- `CLEAR` means no identified concerns in the checks represented by this assessment; it is not proof that the character is optimally built.
- The assessments are domain foundations and are not yet surfaced in the app UI.

### M24 safeguard follow-up

A subsequent edge-case review found that a character with complete raw data could otherwise appear `CLEAR` when one or more required analysis sources were missing, unavailable, or failed. Missing analysis is not equivalent to zero findings.

- Model safeguard commit: `e4b82d197274774357195ec141dccc86e0ea6560`. Added `unverifiedAnalyses` to completeness evidence so analysis-source gaps remain separate from raw character-data gaps.
- Analyzer safeguard commit: `5fd971dda43bb1f78d4248313e7446cab58b5a43`. Explicitly records build-validation and stat-reconciliation availability gaps and forces `REVIEW` when analysis is unverified, even if raw character data is complete.
- Test commit: `f5bdb32e080c69c827ab154e222a633f20e21ba2`. Added regression coverage for complete character data with missing build validation and unavailable stat reconciliation.
- [GitHub Actions Run #178](https://github.com/berao609/Irminsul-codex/actions/runs/45553595570), [Run #179](https://github.com/berao609/Irminsul-codex/actions/runs/45553599260), and [Run #180](https://github.com/berao609/Irminsul-codex/actions/runs/45553603979) all completed successfully for the respective commits.
- Documentation follow-up commits: README `556b92eb1c3afc6976e9c6180a13610f2567a682`; this milestone record `479f7ea567c303c6e79329bb05fe651e731a2a52`. [Run #176](https://github.com/berao609/Irminsul-codex/actions/runs/45553539533) and [Run #177](https://github.com/berao609/Irminsul-codex/actions/runs/45553544019) passed.
- M24 acceptance includes this safeguard. `CLEAR` means no identified concerns among verified checks represented by the assessment, not proof of an optimal build or complete analysis coverage.



## Milestone 025 - Evidence-Backed Account Development Recommendations

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Milestone scope:** Convert unified character assessments into practical, traceable next actions without inventing costs, farming requirements, meta rankings, or missing evidence.  
**Status:** Implemented, unit-test verified, and Debug CI verified (Runs #181-183). Domain foundation only; not yet surfaced in the app UI.

### Objective

Help a player understand what to address next, why the action is recommended, and which account evidence supports it. Recommendations are deterministic outputs from M24 assessments, not AI-generated guesses.

### Recommendation rules

1. **P1 - Resolve confirmed build errors.** Create an action only from existing M21 ERROR findings; retain issue code, field path, and source message as evidence.
2. **P2 - Investigate stat discrepancies.** Create an action only from existing M23 mismatch findings; retain field path, reported value, expected value, difference, and tolerance.
3. **P3 - Review build warnings.** Create an action only from existing M21 WARNING findings; retain their source evidence.
4. **P4 - Complete or verify character data.** Create a review action when M24 lists missing character components or unverified analysis sources. This is a data-quality action, not a claim that a build defect exists.
5. Characters with no identified issues or data gaps receive no unnecessary recommendation.
6. Recommendations are ordered deterministically by priority (P1 to P4), character ID, then recommendation type.
7. Each recommendation carries character ID/name, stable recommendation ID, action, reason, evidence, priority, and stale-data status.
8. STALE assessments remain usable but every derived recommendation is marked stale. MISSING, UNAVAILABLE, and FAILED assessment inputs remain distinct and produce no fabricated recommendations.
9. Aggregate recommendation and affected-character counts are derived from the generated list.

### Implementation

- Model commit: `d91ba89d26d55ad67e552a24b1483082856c5c4c`.
- Analyzer commit: `8a3129195ad64b9b00c78ec60f37aaa490ce92f5`.
- Test commit: `143a5e1ed275487321baec672afbb6d01666280d`.
- Added `app/src/main/java/com/irminsul/codex/domain/model/CharacterDevelopmentRecommendation.kt`.
- Added `app/src/main/java/com/irminsul/codex/domain/analysis/CharacterDevelopmentRecommendationAnalyzer.kt`.
- Added `app/src/test/java/com/irminsul/codex/domain/analysis/CharacterDevelopmentRecommendationAnalyzerTest.kt`.

### Verification

- [GitHub Actions Run #181](https://github.com/berao609/Irminsul-codex/actions/runs/45553964118) passed for the recommendation model commit.
- [GitHub Actions Run #182](https://github.com/berao609/Irminsul-codex/actions/runs/45553985382) passed for the analyzer commit.
- [GitHub Actions Run #183](https://github.com/berao609/Irminsul-codex/actions/runs/45554012123) passed for the test commit.
- Seven unit tests cover priority/evidence ordering, no unnecessary recommendations, incomplete data, unverified analysis sources, stale assessments, unavailable input states, and deterministic cross-character ordering.
- The applicable CI workflow verified JVM unit tests, Debug APK build, signature verification, artifact upload, and cleanup. Room instrumentation tests were not required because this milestone changes only domain models, analysis logic, and JVM tests.
- Physical-device verification: not required; no UI or runtime workflow was changed.

### Qualifications and non-goals

- Recommendations identify data-backed next actions; they do not prove that a character deserves further investment.
- No Mora, Resin, material, or farming cost is estimated. No farming route or resource requirement is invented.
- No numerical character score, combat-strength ranking, meta ranking, pull advice, or team recommendation is introduced.
- No automatic account modification or repair is performed.
- Recommendations are domain foundations. They are not yet connected to the Account Command Center or a user-facing analysis screen.
- Goal-aware recommendation adjustment is deferred to M26.


## Milestone 026 - Goal-Aware Character Development Prioritization

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Milestone scope:** Reorder existing evidence-backed character development recommendations according to an explicitly selected player objective, without changing their source evidence or M25 base priority.  
**Status:** Implemented, unit-test verified, and Debug CI verified (Runs #187-189). Domain foundation only; not yet surfaced in the app UI.

### Objective

Allow a player-selected development objective to influence which existing recommendation appears first. The selected goal changes ordering only. It does not change what the account data says, create new findings, or imply that every character should receive investment.

### Goal profiles

1. **Balanced development:** preserves M25's default order: build errors, stat mismatches, build warnings, then data review.
2. **Build reliability:** prioritizes build errors, then build warnings, followed by stat mismatches and data review.
3. **Stat integrity:** prioritizes stat mismatches, then build errors, build warnings, and data review.
4. **Data confidence:** prioritizes missing or unverified data, then build errors, stat mismatches, and build warnings.

A goal must be supplied explicitly to the analyzer. No user objective is inferred.

### Work implemented

1. Added explicit CharacterDevelopmentGoal values for the four supported profiles.
2. Added goal-aware result models that retain the original recommendation and expose its goal-specific priority tier and a plain-language prioritization reason.
3. Added a deterministic analyzer that reorders the existing M25 recommendation list. Within a goal tier, ordering is stable by character ID, original base priority, recommendation type, and recommendation ID.
4. Preserved each recommendation's original P1-P4 priority, evidence, action, reason, character identity, and stale-data marker. The goal-aware analyzer does not mutate or recalculate these values.
5. Preserved source availability and failure reason, including stale, missing, unavailable, and failed states. No recommendations are fabricated for an unusable source report.
6. Added tests for all four goal profiles, balanced-order compatibility, unchanged base priority/evidence/action/reason, deterministic tie ordering, stale data, and unavailable source handling.

### Implementation

- Goal/result model commit: d9f0eaef2506306944f50c038af52ab368d21b29.
- Prioritization analyzer commit: 1f8a367301292b54789b6840f8ca86250c721d2e.
- Unit-test commit: 21093dc15ad409aa45c96dfadc352af7a3f34142.
- Added app/src/main/java/com/irminsul/codex/domain/model/GoalAwareCharacterDevelopment.kt.
- Added app/src/main/java/com/irminsul/codex/domain/analysis/GoalAwareCharacterDevelopmentAnalyzer.kt.
- Added app/src/test/java/com/irminsul/codex/domain/analysis/GoalAwareCharacterDevelopmentAnalyzerTest.kt.

### Verification

- [GitHub Actions Run #187](https://github.com/berao609/Irminsul-codex/actions/runs/45554692375) passed for the goal/result model commit.
- [GitHub Actions Run #188](https://github.com/berao609/Irminsul-codex/actions/runs/45554724078) passed for the prioritization analyzer commit.
- [GitHub Actions Run #189](https://github.com/berao609/Irminsul-codex/actions/runs/45554760422) passed for the unit-test commit.
- All three runs completed successfully. The applicable CI workflow verified JVM unit tests, Debug APK build, signature verification, artifact upload, and cleanup.
- Physical-device verification: not required; this milestone adds domain logic and tests without changing UI or device runtime behavior.

### Qualifications and non-goals

- Goal profiles reorder only the recommendations already produced by M25. They do not establish that an action is mandatory or that a character deserves investment.
- Goal tier is distinct from M25's base P1-P4 priority. The base priority and supporting evidence remain unchanged and available for traceability.
- No resource costs, farming plans, meta rankings, combat-strength scores, pull advice, or AI-generated priorities are introduced.
- No goal-selection UI or user-facing recommendation screen is implemented in this milestone.

## Milestone 027 - Evidence-Based Account Roster and Equipment Gap Analysis

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Milestone scope:** Identify roster-integrity and character-equipment gaps that are directly supported by retrieved account data, while keeping confirmed integrity findings distinct from data-review conditions.  
**Status:** Implemented, unit-test verified, and Debug CI verified (Runs #192-194). Domain foundation only; not yet surfaced in the app UI.

### Objective

Create a deterministic account-level gap report from the available character roster and equipment snapshots. Report only evidence-backed observations. Missing or unavailable information must remain a data-review condition rather than being presented as a confirmed defect or an investment recommendation.

### Work implemented

1. Added account gap models for analysis availability, gap type, confidence, character identity, explanation, evidence, stale-data status, aggregate counts, and failure reason.
2. Added deterministic analysis for invalid roster entries and duplicate character IDs as confirmed roster-integrity findings.
3. Added data-review findings for missing, stale, unavailable, or failed weapon and artifact data without treating unknown information as a confirmed equipment defect.
4. Reported unoccupied artifact slots as confirmed equipment observations, explicitly not as recommendations to invest in a character.
5. Detected supported equipment-integrity issues, including duplicate or invalid artifact identifiers/slots and invalid artifact subproperty entries.
6. Preserved roster availability and failure reason. Findings derived from stale roster data remain marked stale.
7. Applied stable ordering to findings. No gameplay-strength score, team-coverage inference, farming plan, resource cost, investment priority, or AI-generated conclusion was introduced.

### Implementation

- Model commit: `748d1a29beff0e83d78828d16b3e9698d8f43309`.
- Analyzer commit: `c9f22d13b69bca9e317e756d0d7e975cf6cde5f1`.
- Unit-test commit: `cfc9735c62b1e24f1e6d3662d7261b83bc006f05`.
- Added `app/src/main/java/com/irminsul/codex/domain/model/AccountGapAnalysis.kt`.
- Added `app/src/main/java/com/irminsul/codex/domain/analysis/AccountGapAnalyzer.kt`.
- Added `app/src/test/java/com/irminsul/codex/domain/analysis/AccountGapAnalyzerTest.kt`.

### Verification

- [GitHub Actions Run #192](https://github.com/berao609/Irminsul-codex/actions/runs/45555403281) passed for the account-gap model commit.
- [GitHub Actions Run #193](https://github.com/berao609/Irminsul-codex/actions/runs/45555451798) passed for the analyzer commit.
- [GitHub Actions Run #194](https://github.com/berao609/Irminsul-codex/actions/runs/45555489070) passed for the unit-test commit.
- Eight unit tests cover clean data, invalid and duplicate roster entries, missing equipment data, unavailable and failed states, unoccupied artifact slots, malformed artifact integrity evidence, stale collections, and unusable roster availability.
- The three implementation runs were confirmed successful by the project owner. Physical-device verification is not required because this milestone adds domain models, deterministic analysis, and JVM tests without UI or runtime workflow changes.

### Qualifications and non-goals

- This milestone covers character roster and equipment gaps only. It is not a complete account-wide capability, progression, or team-coverage analysis.
- An unoccupied artifact slot is an observation, not proof that the character needs investment or that the slot should be filled.
- Missing, unavailable, failed, and stale inputs remain distinct. Unknown data is not converted into a confirmed gap.
- No artifact/weapon farming requirements, Mora or Resin costs, build-strength scores, meta rankings, pull advice, or automatic account changes are produced.
- The report is a domain foundation and is not yet connected to a user-facing account intelligence screen.

## Milestone 028 - Unified Account Intelligence Overview Composition

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Milestone scope:** Compose the existing unified character assessment, goal-aware character development recommendations, and account roster/equipment gap report into one account-level overview without recalculating or altering their source findings.  
**Status:** Implemented, unit-test verified, and Debug CI verified (Runs #195-197). Domain foundation only; not yet surfaced in the app UI.

### Objective

Provide one deterministic domain object for a future Account Command Center to consume. The overview exposes selected player goal, existing assessment and recommendation counts, account gap counts, stale-evidence presence, and whether any source remains unverified. Each source report remains available in full and retains its own availability state and evidence.

### Work implemented

1. Added `AccountIntelligenceOverview`, retaining the original `UnifiedCharacterAssessmentReport`, `GoalAwareCharacterDevelopmentReport`, and `AccountGapAnalysisReport` objects.
2. Exposed the selected development goal and existing summary values: assessed character count, needs-attention/review/clear counts, recommendation and affected-character counts, confirmed account-gap count, and data-review count.
3. Added a stale-evidence indicator that checks source report availability and stale markers on their underlying assessments, recommendations, and gap findings.
4. Added an unverified-source indicator for missing, unavailable, or failed reports. Stale remains a separate state rather than being collapsed into missing/unverified.
5. Added a composition analyzer that only assembles the supplied reports. It does not filter, recalculate, reorder, or reclassify their findings.
6. Preserved available evidence from successful sources when another source is missing, unavailable, or failed.
7. Added five unit tests for report composition and counts, stale evidence, missing/unavailable/failed sources, available empty reports, and partial source failure.

### Implementation

- Overview model commit: [3545e97](https://github.com/berao609/Irminsul-codex/commit/3545e97ed416a42e3f10925548154a8c64572bf1).
- Composition analyzer commit: [b04aefb](https://github.com/berao609/Irminsul-codex/commit/b04aefba1b29065f91fca8abadfd4f30c91416cc).
- Unit-test commit: [c82a7fb](https://github.com/berao609/Irminsul-codex/commit/c82a7fbad08d809b9419f0517708cb1a5945296b).
- Added `app/src/main/java/com/irminsul/codex/domain/model/AccountIntelligenceOverview.kt`.
- Added `app/src/main/java/com/irminsul/codex/domain/analysis/AccountIntelligenceOverviewAnalyzer.kt`.
- Added `app/src/test/java/com/irminsul/codex/domain/analysis/AccountIntelligenceOverviewAnalyzerTest.kt`.

### Verification

- GitHub Actions Runs #195, #196, and #197 passed for the overview model, composition analyzer, and unit tests respectively, as confirmed by the project owner.
- Documentation CI Runs #198 and #199 also passed, as confirmed by the project owner.
- Five unit tests cover source-report retention, summary counts, stale-data signaling, missing/unavailable/failed states, empty available reports, and preservation of valid results when another source is unusable.
- Physical-device verification: not required; this milestone adds domain composition and JVM tests without changing UI or runtime workflows.

### Qualifications and non-goals

- This is a composed domain overview, not a user-facing dashboard or Account Command Center.
- It combines the existing M24 unified character assessment, M26 goal-aware recommendations, and M27 roster/equipment gap report. It does not imply complete account-wide gameplay, team-coverage, or progression analysis.
- Source reports remain authoritative. The overview does not create an overall account score, combat-strength ranking, meta ranking, farming plan, resource cost, pull recommendation, or AI-generated conclusion.
- A stale or unverified source is surfaced as such; it is not treated as a clean result.
- No device installation or UI behavior was verified in this milestone.

## Milestone 029 - Account Data Confidence Summary

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Milestone scope:** Summarize account snapshot section coverage and freshness evidence while preserving their distinct states and avoiding unsupported confidence claims.  
**Status:** Implemented, unit-test verified, and Debug CI verified (Runs #202-204). Domain foundation only; not yet surfaced in the app UI.

### Objective

Give the future account intelligence experience a concise, explainable view of how much account-section data is present and whether its freshness has been assessed. Coverage and freshness remain separate evidence sources. Unknown freshness is not stale, and an absent freshness report is not proof that data is fresh.

### Work implemented

1. Added `AccountDataConfidenceSummary`, retaining the original `AccountSnapshotCoverageReport` and nullable `AccountSnapshotFreshnessReport`.
2. Exposed coverage counts for total, present, stale, missing, unavailable, and failed sections, plus coverage inconsistency count.
3. Exposed freshness assessment state and nullable counts for fresh, stale, unknown, and invalid-timestamp sections. Nullable counts preserve the distinction between no freshness assessment and an assessed report with zero findings.
4. Added explicit indicators for stale sections, incomplete section data, freshness uncertainty, coverage inconsistencies, and overall data-confidence concerns.
5. Added a pure summarizer that wraps the supplied source reports without merging their states or inferring new findings.
6. Preserved unknown freshness as uncertainty rather than counting it as stale. A missing freshness report remains unassessed and raises freshness uncertainty.
7. Added five unit tests covering source-report retention and counts, coverage inconsistencies, unknown freshness, missing freshness assessment, and fully present/consistent/fresh data.

### Implementation

- Summary model commit: [18b9d96](https://github.com/berao609/Irminsul-codex/commit/18b9d9657db362bfb00d3a1b299ff5518b206e90).
- Summarizer commit: [b5ee2d2](https://github.com/berao609/Irminsul-codex/commit/b5ee2d22074b33474837db4c18238850ca3670f5).
- Unit-test commit: [a17dbc4](https://github.com/berao609/Irminsul-codex/commit/a17dbc4f23d20201699cfeb1a5c15a2b2da3cf72).
- Added `app/src/main/java/com/irminsul/codex/domain/model/AccountDataConfidenceSummary.kt`.
- Added `app/src/main/java/com/irminsul/codex/domain/analysis/AccountDataConfidenceAnalyzer.kt`.
- Added `app/src/test/java/com/irminsul/codex/domain/analysis/AccountDataConfidenceAnalyzerTest.kt`.

### Verification

- [GitHub Actions Run #202](https://github.com/berao609/Irminsul-codex/actions/runs/45556348344) passed for the summary model commit.
- [GitHub Actions Run #203](https://github.com/berao609/Irminsul-codex/actions/runs/45556359607) passed for the summarizer commit.
- [GitHub Actions Run #204](https://github.com/berao609/Irminsul-codex/actions/runs/45556394451) passed for the unit-test commit.
- Five unit tests cover source-report retention and counts, coverage inconsistencies, unknown freshness, missing freshness assessment, and fully present/consistent/fresh data.
- The applicable CI workflow verified the JVM unit-test and Debug build pipeline, including its configured signature/artifact/cleanup steps.
- Physical-device verification: not required; this milestone adds domain models, deterministic summary logic, and JVM tests without changing UI or runtime workflows.

### Qualifications and non-goals

- This summary does not independently validate the correctness of the underlying HoYoLAB data or guarantee that every relevant account section is retrieved.
- Coverage and freshness counts describe their respective source reports; they are not merged into a single completeness score.
- Unknown freshness is not stale. A null freshness report means freshness has not been assessed.
- No account quality score, combat-strength score, progression ranking, farming plan, resource estimate, pull recommendation, or AI-generated conclusion is introduced.
- This is a domain foundation and is not yet connected to the user-facing account intelligence experience.
- No physical-device installation or UI behavior was verified.

## Milestone 030 - Integrate Account Data Confidence into Unified Overview

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Milestone scope:** Extend the unified account intelligence overview to retain and expose the M29 account data-confidence summary alongside the existing character assessment, goal-aware recommendations, and roster/equipment gap reports.  
**Status:** Implemented, unit-test verified, and Debug CI verified (final implementation Run #209). Intermediate commits #207 and #208 failed because they represented incomplete stages of the multi-commit change; the final commit #209 passed.

### Objective

Make account-section coverage and freshness evidence available through the same domain overview that already composes character assessments, development recommendations, and account gaps. Preserve every source report and its own availability/state semantics. Do not convert the combined overview into a score or imply complete gameplay analysis.

### Work implemented

1. Extended `AccountIntelligenceOverview` to retain the supplied `AccountDataConfidenceSummary` as a fourth source report.
2. Exposed account-section coverage counts: total, present, stale, missing, unavailable, and failed sections.
3. Exposed freshness counts as nullable values so an absent freshness assessment remains distinguishable from an assessed report with zero findings.
4. Exposed explicit indicators for incomplete account data, unassessed freshness, coverage inconsistencies, and data-confidence concerns.
5. Extended stale-evidence reporting to include stale account-section coverage/freshness evidence while keeping unknown freshness separate from stale evidence.
6. Kept missing, unavailable, and failed character-analysis sources distinct from stale sources. Existing source reports remain accessible and unchanged.
7. Extended the composition analyzer to accept and retain all four source reports without recalculating, filtering, reordering, or reclassifying findings.
8. Added eight unit tests covering four-report composition and counts, stale account data, unknown freshness, missing freshness assessment, incomplete coverage and inconsistencies, stale analysis reports, unusable source states, empty reports, and partial source failure.

### Implementation

- Overview model commit: [0166a10](https://github.com/berao609/Irminsul-codex/commit/0166a10def499b71f3d2f663a1da2e46d7ee73f1).
- Composition analyzer commit: [7e8123d](https://github.com/berao609/Irminsul-codex/commit/7e8123d1d25fa078c86f1cccdd5dbe77d3e7cf59).
- Unit-test commit: [fbf5f3a](https://github.com/berao609/Irminsul-codex/commit/fbf5f3a9b38d6f822127c727515d7c8591d3116d).
- Updated `app/src/main/java/com/irminsul/codex/domain/model/AccountIntelligenceOverview.kt`.
- Updated `app/src/main/java/com/irminsul/codex/domain/analysis/AccountIntelligenceOverviewAnalyzer.kt`.
- Updated `app/src/test/java/com/irminsul/codex/domain/analysis/AccountIntelligenceOverviewAnalyzerTest.kt`.

### Verification

- [GitHub Actions Run #207](https://github.com/berao609/Irminsul-codex/actions/runs/45556952899) failed at Kotlin compilation because the model commit introduced the required `dataConfidence` constructor parameter before the analyzer commit supplied it.
- [GitHub Actions Run #208](https://github.com/berao609/Irminsul-codex/actions/runs/45556955419) failed at test compilation because the analyzer commit preceded the test-file update; existing test constructions had not yet supplied the new required parameter.
- [GitHub Actions Run #209](https://github.com/berao609/Irminsul-codex/actions/runs/45556959401) passed on the final test commit, which contains the complete model, analyzer, and test changes. The full JVM unit-test task, Debug build, Room schema verification, signature verification, artifact uploads, and cleanup passed; emulator instrumentation tests were skipped because this domain-only change did not modify Room-sensitive paths.
- Physical-device verification: not required; no UI or runtime workflow changed.

### Qualifications and non-goals

- The two intermediate CI failures are recorded rather than hidden. They do not represent failures of the final combined tree; Run #209 verifies the final implementation commit.
- The overview remains a domain composition object and is not connected to a user-facing Account Command Center or account dashboard.
- Coverage and freshness remain separate evidence sources. Unknown freshness is not stale, and an absent freshness report is not equivalent to verified freshness.
- No unified account score, combat-strength ranking, meta ranking, farming plan, resource estimate, pull recommendation, or AI-generated conclusion is introduced.
- The combined reports do not establish complete account-wide gameplay, team-coverage, or progression analysis.
- No physical-device installation or UI behavior was verified.


## Milestone 031 - Deterministic Account Intelligence Pipeline

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Milestone scope:** Provide one domain entry point that runs the existing deterministic account-analysis stages from an `AccountSnapshot` and an explicitly selected character-development goal, then composes their reports into the unified account intelligence overview.  
**Status:** Implemented, unit-test verified, and Debug CI verified (final implementation/test Run #216). Domain orchestration foundation only; not yet connected to the ViewModel or user interface.

### Objective

Make the existing analysis capabilities consumable through one deterministic domain entry point. The caller supplies the account snapshot and selected development goal. The engine orchestrates the existing analyzers and returns the unified overview without introducing new scoring, reinterpretation, or inferred findings.

### Work implemented

1. Added `AccountIntelligenceEngine` as the orchestration entry point.
2. Runs the existing character build and stat analysis stages using the supplied snapshot.
3. Builds the unified character assessment, then evidence-backed recommendations, then goal-aware recommendation ordering using the explicitly supplied goal.
4. Runs account roster/equipment gap analysis using the snapshot's character data and collection-integrity evidence.
5. Summarizes account section coverage and freshness using the existing account data-confidence analyzer.
6. Composes the four source report groups through `AccountIntelligenceOverviewAnalyzer`, preserving the source reports and their established availability/evidence semantics.
7. Added pipeline tests for successful composition, preservation of missing-source states, and failed analysis evidence remaining visible as unverified character evidence.

### Implementation

- Engine commit: [80fe3a5](https://github.com/berao609/Irminsul-codex/commit/80fe3a50c64a959ce1e9cc22c5c7e2929abe6266).
- Initial pipeline test commit: [4de6e8e](https://github.com/berao609/Irminsul-codex/commit/4de6e8eade4ef4844a494f669df2de4d19835e23).
- Test-fixture and failed-source assertion refinements: [82971ff](https://github.com/berao609/Irminsul-codex/commit/82971ffba4855757bf80f1a544d92d4969095d85) and [495ddb8](https://github.com/berao609/Irminsul-codex/commit/495ddb8d21020e7af96e6e7f15ec54387f08bbf6).
- Final test expectation correction: [885481a](https://github.com/berao609/Irminsul-codex/commit/885481a1d5a0938cfe7778fa02f5973b9cfd310e).
- Added `app/src/main/java/com/irminsul/codex/domain/analysis/AccountIntelligenceEngine.kt`.
- Added `app/src/test/java/com/irminsul/codex/domain/analysis/AccountIntelligenceEngineTest.kt`.

### Verification

- [GitHub Actions Run #216](https://github.com/berao609/Irminsul-codex/actions/runs/45558137695) passed for final commit `885481a1d5a0938cfe7778fa02f5973b9cfd310e`.
- Verified workflow steps: JVM unit tests, Debug APK build, Room schema snapshot verification/upload, APK signature verification, APK artifact upload, and temporary signing-key cleanup all passed.
- Room emulator instrumentation was skipped by the workflow because this domain-only change did not modify Room-sensitive paths.
- Physical-device verification: not required; no UI, persistence, or runtime workflow was changed.

### Qualifications and non-goals

- This milestone orchestrates existing deterministic analyzers. It does not add new account facts, alter source findings, or replace the underlying analyzers' authority.
- The development goal is supplied explicitly by the caller; the engine does not infer player intent.
- Missing, unavailable, failed, and stale source states remain governed by the existing analysis components. They are not converted into clean results or zero findings.
- The engine is not yet called by `AccountViewModel` or surfaced in the application UI. The account intelligence experience remains a domain foundation.
- No account score, combat-strength ranking, meta ranking, resource-cost estimate, farming plan, pull recommendation, or AI-generated conclusion is introduced.
- No device installation or user-facing behavior was verified.


## Milestone 032 - Integrate Account Intelligence into the Application

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Milestone scope:** Connect the M31 deterministic account-intelligence pipeline to the account retrieval workflow, expose its result through ViewModel state, and surface an initial evidence-backed account summary in the existing Android screen.  
**Status:** Implemented, unit-test verified, Debug CI verified (final Run #221). Physical-device verification pending.

### Objective

Make the existing deterministic intelligence engine useful inside the application without introducing a backend, AI dependency, or premature full dashboard. A successful account retrieval should continue to be persisted locally and should also produce an account-intelligence overview that the UI can explain. The player must be able to explicitly select the objective used to order existing recommendations.

### Work implemented

1. Extended `AccountExportUiState` with the selected development goal, analysis progress/error state, and nullable `AccountIntelligenceOverview`.
2. Connected `AccountViewModel` to `AccountIntelligenceEngine`. After a successful retrieval and the existing Room-save attempt, the ViewModel analyzes the normalized snapshot and publishes the unified overview.
3. Preserved the existing export behavior: raw JSON remains available for the Save JSON document flow, persistence failures remain visible without discarding the retrieved export, and authentication credentials are not added to the snapshot or intelligence model.
4. Added explicit goal selection for Balanced Development, Build Reliability, Stat Integrity, and Data Confidence. Balanced Development is the initial default; changing the goal re-runs the deterministic pipeline with that explicit goal. The goal changes recommendation ordering only, not the source findings or base priorities.
5. Added an initial Account Intelligence card to the existing Compose screen. It displays character assessment counts, up to five evidence-backed recommendations, account gap counts, account-section coverage counts, and warnings for stale or unverified sources.
6. Extended the ViewModel test to verify successful pipeline integration, preservation of unverified-source state when character data is missing, and re-analysis after changing the development goal.

### Implementation

- ViewModel integration commit: [43f205d](https://github.com/berao609/Irminsul-codex/commit/43f205d72af0a1c7c08be9659381f74bbc84de36).
- ViewModel integration test commit: [1e0a2d8](https://github.com/berao609/Irminsul-codex/commit/1e0a2d8db2763b2262ccb7ae3ba9c92e9b5fb848).
- Initial Compose overview commit: [44a43cf](https://github.com/berao609/Irminsul-codex/commit/44a43cf54064ffccc84a1b96a881635d52514307).
- Updated `app/src/main/java/com/irminsul/codex/presentation/viewmodel/AccountViewModel.kt`.
- Updated `app/src/test/java/com/irminsul/codex/presentation/viewmodel/AccountViewModelTest.kt`.
- Updated `app/src/main/java/com/irminsul/codex/MainActivity.kt`.

### Verification

- [GitHub Actions Run #219](https://github.com/berao609/Irminsul-codex/actions/runs/45559291966) passed for the ViewModel integration commit.
- [GitHub Actions Run #220](https://github.com/berao609/Irminsul-codex/actions/runs/45559315653) passed for the ViewModel test commit.
- [GitHub Actions Run #221](https://github.com/berao609/Irminsul-codex/actions/runs/45559376464) passed for the final combined implementation commit `44a43cf54064ffccc84a1b96a881635d52514307`.
- Run #221 passed JVM unit tests, Debug APK build, Room schema snapshot verification and upload, APK signature verification, APK artifact upload, and temporary signing-key cleanup. Room emulator instrumentation was skipped because this milestone did not modify Room-sensitive paths.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.221-vc223`, artifact ID `14997636089`, size 8,524,460 bytes. Room schema artifact: `irminsul-room-schema-v1-run-221`, artifact ID `14996454020`.
- Physical-device verification: pending. The Compose screen and goal-selection behavior still need confirmation on the project owner's Android device.

### Qualifications and non-goals

- Balanced Development is an application default, not an inferred statement about the player's personal objective. The user can change it in the Account Intelligence card.
- The initial screen shows at most five recommendations. It is not a full recommendation detail/history experience or the complete Account Command Center.
- The selected goal is currently in ViewModel state and is not persisted as a user preference across process recreation.
- This milestone does not add AI, resource-cost estimates, farming plans, pull advice, combat-strength scores, meta rankings, or a complete account-wide gameplay analysis.
- JSON export remains an optional utility. The account intelligence pipeline runs from the normalized snapshot inside the app.


### M32 Follow-up - Make Account Analysis a First-Class Action

**Date recorded:** 2026-11-25  
**Reason:** Physical-device feedback after installing the initial M32 build showed no visible change from the previous app version. Investigation found that the Account Intelligence card was rendered only after `AccountViewModel.snapshot` became non-null. The existing Daily Note Save & Sync path did not populate that snapshot; the card was effectively gated behind the optional full-account JSON export flow. This contradicted the product decision that JSON export is secondary.

**Correction implemented:**

1. Added `AccountViewModel.analyzeAccount(uid, cookie)`, which uses the existing account repository, saves the retrieved snapshot to Room, and runs the deterministic intelligence pipeline without setting `pendingExport` or opening the JSON-save flow.
2. Kept `exportAccount` behavior for the optional Save JSON action. Both entry points share the same retrieval, persistence, and analysis implementation.
3. Made the Account Intelligence card visible when account credentials are available, before a snapshot has been retrieved.
4. Added a dedicated **Analyze Account** button. After analysis, it changes to **Refresh Analysis**. The UI also explains that this action is separate from optional JSON export.
5. Added a ViewModel regression test proving that account analysis publishes an overview without preparing a JSON export.

**Correction commits:**

- ViewModel retrieval/action separation: [5dba2f1](https://github.com/berao609/Irminsul-codex/commit/5dba2f18f2e77bb4af309a4fb50edd4a95b08d96).
- Compose card visibility and Analyze Account action: [b5f3cb3](https://github.com/berao609/Irminsul-codex/commit/b5f3cb3ff929a460cad64949078ea0743b7fbefc).
- Regression test: [399753b](https://github.com/berao609/Irminsul-codex/commit/399753bc3a3328c7c921a4f5c153b371c7a7f24c).

**Verification:**

- [GitHub Actions Run #226](https://github.com/berao609/Irminsul-codex/actions/runs/45561255523) passed for the ViewModel change.
- [GitHub Actions Run #227](https://github.com/berao609/Irminsul-codex/actions/runs/45561284173) passed for the UI change.
- [GitHub Actions Run #228](https://github.com/berao609/Irminsul-codex/actions/runs/45561311725) passed for the final combined tree at `399753bc3a3328c7c921a4f5c153b371c7a7f24c`.
- Run #228 completed the build job successfully, including the workflow's unit-test, Debug build, schema verification, signature verification, artifact upload, and signing-key cleanup stages.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.228-vc230`, artifact ID `14997867111`, size 8,524,897 bytes. Room schema artifact: `irminsul-room-schema-v1-run-228`, artifact ID `14998455653`.
- Physical-device verification of the corrected entry point and visible card: pending.

**Updated M32 status:** Implementation and final combined CI verified. Physical-device verification remains pending. The initial M32 build's integration defect is documented here rather than treated as a successful user-facing delivery.

## Milestone 033 - Rounding-Aware Character Stat Reconciliation

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - First Deterministic Account Analysis  
**Milestone scope:** Account for the displayed precision of the reported base, additive, and final stat values when deciding whether a reconciliation difference is explainable by independent rounding.  
**Status:** Implemented, regression-test verified, and full Debug CI verified (Run #234). Physical-device verification is not required for this deterministic domain change.

### Objective

Investigate repeated ±1 stat mismatches observed in the Account Intelligence screen. The previous reconciler based its tolerance only on the reported final value's displayed precision. It did not account for rounding in the base and additive inputs. This could classify independently rounded displayed values as genuine mismatches.

### Work implemented

1. Changed the reconciliation tolerance to the sum of the half-unit precision allowances for the reported base, additive, and final values.
2. Preserved the original expected-value calculation and difference. Only the tolerance/status decision changed.
3. Added regression coverage proving an integer ±1 difference can fall within the combined rounding interval.
4. Added boundary coverage proving differences beyond the combined interval remain mismatches.
5. Updated analyzer tests to verify a rounding-tolerated difference does not produce a stat-mismatch insight.
6. Updated the model documentation to describe the combined precision allowance.

### Implementation

- Reconciler commit: [5185263](https://github.com/berao609/Irminsul-codex/commit/51852636edf35c4cd2245936d99e5e5380283af8).
- Reconciler regression tests: [f0ab36e](https://github.com/berao609/Irminsul-codex/commit/f0ab36ef46480d3fb730a65e1a5f55fce94f2fb4).
- Analyzer regression tests: [c262b69](https://github.com/berao609/Irminsul-codex/commit/c262b6900e948fa0981f26adb5f18570280aaaa8).
- Model documentation: [6a92bae](https://github.com/berao609/Irminsul-codex/commit/6a92bae7b67e6357b6844ad67547e5dd315fa909).

### Verification

- Runs #231 and #232 failed on intermediate commits while tests still expected the old tolerance behavior.
- [Run #233](https://github.com/berao609/Irminsul-codex/actions/runs/45563395999) passed the updated unit tests and Debug build for the combined analyzer/test state.
- [Run #234](https://github.com/berao609/Irminsul-codex/actions/runs/45563509042) passed the final combined tree, including JVM unit tests, Room emulator instrumentation, Debug APK build, Room schema verification, APK signature verification, artifact upload, and temporary signing-key cleanup.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.234-vc236`, artifact ID `14998832885`, size 8,524,955 bytes.
- Room schema artifact: `irminsul-room-schema-v1-run-234`, artifact ID `14998573459`, size 985 bytes.

### Qualifications and non-goals

- The combined tolerance is derived from the decimal precision represented by the three source strings. It does not claim to model every possible game-side calculation or data-source transformation.
- The change does not hide arbitrary differences: discrepancies beyond the combined precision interval remain mismatches.
- The observed account's actual mismatch counts have not yet been rechecked on a newly installed APK.
- No physical-device verification has been performed for the updated displayed findings.

## Milestone 034 - Multi-Page Navigation and Account Home

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Replace the single long home screen with focused bottom-navigation destinations, make Home an account and Daily Note dashboard, move JSON export into Info, and establish dedicated destinations for intelligence, visuals, and AI chat.  
**Status:** Implemented, final combined Debug CI verified (Run #237), README and milestone documentation CI verified (Runs #238-239). Physical-device verification pending.

### Objective

Give Irminsul Codex a clean, full-screen native-app structure rather than placing account retrieval, export, intelligence, and daily activity in one vertically crowded screen. Preserve the existing retrieval and analysis workflows while assigning each capability a clear destination.

### Work implemented

1. Added five bottom-navigation destinations: Home, Intel, Visuals, AI Chat, and Info.
2. Reorganized Home around account identity (UID and detected server), the latest retrieved account snapshot summary, and Daily Note activity.
3. Kept Resin, commission progress and bonus status, Realm Currency, weekly Resin discounts, and expeditions on Home.
4. Moved the detailed Account Intelligence experience and its four development-goal selectors to the dedicated Intel destination.
5. Added an initial Visuals dashboard using existing deterministic results: character assessment distribution, recommendation count, confirmed account gaps, and account-section coverage.
6. Added a dedicated AI Chat destination that clearly states BYOAI is not connected or active. It does not simulate AI responses or collect provider credentials.
7. Moved Account Data Export and the Save JSON action to Info, separate from Home and the primary analysis experience.
8. Added account-management access in Info while retaining the existing HoYoLAB login and manual credential flow.
9. Preserved existing Daily Note synchronization, account retrieval, Room snapshot persistence, deterministic analysis, and JSON document-picker behavior.

### Implementation

- Initial navigation and page composition: [bd87626](https://github.com/berao609/Irminsul-codex/commit/bd876260c2d77aec479eed28daa0c69b71f926b6).
- Home snapshot summary: [027a88b](https://github.com/berao609/Irminsul-codex/commit/027a88b53b39a0e3334234023ee6c7ce57ef6c72).
- Updated `app/src/main/java/com/irminsul/codex/MainActivity.kt`.
- Updated `README.md` to describe the current navigation and distinguish active screens from planned AI functionality.

### Verification

- [Run #236](https://github.com/berao609/Irminsul-codex/actions/runs/45565734113) completed JVM unit tests, Debug APK build, Room schema verification, APK signature verification, artifact upload, and signing-key cleanup successfully for the initial navigation commit.
- [Run #237](https://github.com/berao609/Irminsul-codex/actions/runs/45565899838) passed for commit `027a88b53b39a0e3334234023ee6c7ce57ef6c72`: JVM unit tests, Debug APK build, Room schema verification, APK signature verification, artifact upload, and temporary signing-key cleanup. Room emulator instrumentation was skipped by the existing path filter because the changes did not affect Room-sensitive code.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.237-vc239`, artifact ID `14999962669`, size 8,547,739 bytes.
- Room schema artifact: `irminsul-room-schema-v1-run-237`, artifact ID `14999867965`, size 985 bytes.
- [Run #238](https://github.com/berao609/Irminsul-codex/actions/runs/45565975108) passed for README commit `1c11e591afe80c1603206e7094aeaf1dbe5f2eb8`.
- [Run #239](https://github.com/berao609/Irminsul-codex/actions/runs/45566032822) passed for this milestone record's prior commit `e420850a6309099f963af153973da8b79d582a7b`.
- Physical-device verification remains pending.
- Physical-device verification: pending.

### Qualifications and next work

- The initial Visuals destination is a compact deterministic summary dashboard, not yet a full charting, comparison, or interactive table experience.
- AI Chat is a navigation destination and explanatory placeholder only. BYOAI provider configuration, chat messages, context controls, and model calls are not implemented.
- The five destinations currently use Compose navigation state within the existing application screen; this milestone does not add Navigation Compose or split the project into additional Gradle modules.
- Account identity currently displays the UID and detected server plus the retrieved character-roster count when available. Profile nickname, Adventure Rank, and World Level are not added because those fields are not currently exposed by the normalized snapshot model.
- Detailed gap explanations, data-confidence causes, and full recommendation inspection remain follow-up work.
- No paid runners, mandatory backend, release, or signing changes were introduced.

## Milestone 035 - Consolidate Account Analysis into Visuals

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Remove the redundant Intel bottom-navigation destination and consolidate its account-analysis controls and detailed deterministic findings into the Visuals destination.  
**Status:** Implemented, final combined Debug CI verified (Run #241), README and milestone documentation CI verified (Runs #242-243). Physical-device verification pending.

### Rationale

The Intel destination and Visuals destination exposed two parts of the same account-intelligence workflow. Intel contained analysis execution, objective selection, and evidence-backed findings; Visuals showed aggregate assessment, recommendation, gap, and account-data metrics. Keeping them as separate top-level destinations made users switch pages to understand one analysis. The product does not need a separate Intel destination.

### Work implemented

1. Removed Intel from bottom navigation. The app now has four destinations: Home, Visuals, AI Chat, and Info.
2. Moved the complete account-analysis section into Visuals, including Analyze Account / Refresh Analysis, the four development-goal selectors, assessment counts, recommendations, account gaps, account-section coverage, and stale/unverified warnings.
3. Kept the visual assessment bars and account metric summaries in the same Visuals destination.
4. Removed the redundant "Open detailed intelligence" navigation action.
5. Updated the Visuals page heading and description to communicate that analysis and visual summaries are one integrated experience.
6. Preserved Home, AI Chat, Info, account retrieval, Room persistence, Daily Note synchronization, and optional JSON export behavior.

### Implementation

- MainActivity consolidation: [a7d9b92](https://github.com/berao609/Irminsul-codex/commit/a7d9b92456c76b1f85781d60a4ba195dd1d0b1d5).
- README update: [cfccec3](https://github.com/berao609/Irminsul-codex/commit/cfccec3e2786d74e085604a6eb896fcdb17f18f4).

### Verification

- [Run #241](https://github.com/berao609/Irminsul-codex/actions/runs/45567021493) passed for the consolidated MainActivity implementation commit `a7d9b92456c76b1f85781d60a4ba195dd1d0b1d5`.
- [Run #242](https://github.com/berao609/Irminsul-codex/actions/runs/45567053962) passed for README commit `cfccec3e2786d74e085604a6eb896fcdb17f18f4`.
- [Run #243](https://github.com/berao609/Irminsul-codex/actions/runs/45567094064) passed for milestone documentation commit `9e841b5acac7635529b2eaf66f91ce04cb3703a6`.
- Physical-device verification: pending.

### Qualifications

- The existing Visuals screen still uses the current deterministic summary components. This milestone consolidates navigation and content; it does not yet add expanded interactive charts, full recommendation drill-down, or detailed gap explanations.
- AI Chat remains an inactive placeholder pending BYOAI implementation.
- No domain analysis logic, account data, persistence, credentials, or export behavior was changed.

## Milestone 036 - Edge-to-Edge Layout and Appearance Preferences

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Extend the app surface edge-to-edge and provide persistent System, Light, and Dark appearance choices.  
**Status:** Implemented and committed. Final combined implementation and documentation CI verification pending. Physical-device verification pending.

### Objective

Address the request for a full-screen presentation and user-selectable appearance without changing the four-destination navigation or account workflows.

### Work implemented

1. Enabled edge-to-edge window layout with transparent status and navigation bar backgrounds. Android system status and navigation controls remain visible and usable.
2. Updated status-bar and navigation-bar icon appearance to maintain contrast with the active theme.
3. Replaced the forced dark-only Material 3 color scheme with coordinated dark and light color schemes.
4. Added three appearance choices under Info: System, Light, and Dark.
5. System mode follows the device's current appearance setting.
6. Persisted the selected appearance in the existing app preferences under `theme_mode`; the default for existing installs is System.
7. Kept the existing four navigation destinations, account data, credentials, persistence, retrieval, and export workflows unchanged.

### Implementation

- Edge-to-edge and theme settings: [35760ed](https://github.com/berao609/Irminsul-codex/commit/35760ed1b993b45deede77d9b09a34c8fd6a26ee).
- Internal visibility correction: [f00df6d](https://github.com/berao609/Irminsul-codex/commit/f00df6d4a78197b727ea6ee72747c2b9f89d04bb).
- README update: [514b325](https://github.com/berao609/Irminsul-codex/commit/514b32576823259f2f91c292ea18230048c21615).
- Main implementation file: `app/src/main/java/com/irminsul/codex/MainActivity.kt`.

### Verification

- [Run #244](https://github.com/berao609/Irminsul-codex/actions/runs/45567570932) failed during JVM unit tests on the initial implementation commit `35760ed1b993b45deede77d9b09a34c8fd6a26ee`. This was an intermediate commit before the follow-up visibility correction; it is retained here as verification history.
- [Run #245](https://github.com/berao609/Irminsul-codex/actions/runs/45567600077) passed on the final combined implementation commit `f00df6d4a78197b727ea6ee72747c2b9f89d04bb`: JVM unit tests, Debug APK build, Room schema verification, APK signature verification, artifact uploads, and signing-key cleanup.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.245-vc247`, artifact ID `15000099805`, size 8,554,168 bytes.
- Room schema artifact: `irminsul-room-schema-v1-run-245`, artifact ID `15001250430`, size 985 bytes.
- [Run #246](https://github.com/berao609/Irminsul-codex/actions/runs/45567642492) passed for README commit `514b32576823259f2f91c292ea18230048c21615`.
- [Run #247](https://github.com/berao609/Irminsul-codex/actions/runs/45567689211) passed for this milestone record's previous commit `213b7ce61869d0021db27ef8f1c3319c08c632b3`.
- Physical-device verification: pending.

### Qualifications

- Edge-to-edge means the app's surfaces extend behind the Android status and navigation bars; it does not hide Android's system indicators or enable immersive mode.
- The three theme choices are applied to the Material 3 application color scheme and system-bar icon contrast. Further component-by-component visual refinement may be needed after device testing.
- No new dependencies, backend services, release configuration, or signing changes were introduced.

## Milestone 037 - Remove Duplicate Analyze Account Action

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Remove the duplicate Analyze Account button from the Visuals page while retaining the primary analysis action.  
**Status:** Implemented, committed, CI-verified, and physical-device verified.

### Finding

The Visuals page rendered the primary Analyze Account / Refresh Analysis button in its account-analysis section. When no overview existed, the Visual Dashboard empty-state card rendered a second Analyze Account button. Both invoked the same `accountViewModel.analyzeAccount(uid, cookie)` action.

### Change

- Removed the duplicate button from the empty-state card.
- Kept the primary Analyze Account / Refresh Analysis action.
- Updated the empty-state instruction to direct the user to the primary button above.
- No analysis logic, account data, navigation, appearance settings, or other actions changed.

### Implementation

- MainActivity fix: [3e9be98](https://github.com/berao609/Irminsul-codex/commit/3e9be98158c6150d22fb7ef76ee5725acd2434ae).

### Verification

- [Run #249](https://github.com/berao609/Irminsul-codex/actions/runs/45568622860) passed for implementation commit `3e9be98158c6150d22fb7ef76ee5725acd2434ae`.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.249-vc251`, artifact ID `15001291778`, size 8,553,043 bytes.
- Room schema artifact: `irminsul-room-schema-v1-run-249`, artifact ID `15001491320`, size 985 bytes.
- User reported Run #250, the milestone documentation workflow, succeeded.
- User installed and tested the updated app and confirmed the redundant Analyze Account button is gone. This is physical-device verification of the duplicate-button fix.

### User feedback

- User confirmed the edge-to-edge full-screen presentation and dark/system theme behavior are acceptable.


## Milestone 038 - Full Recommendation Access and Evidence Details

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Let users inspect every deterministic development recommendation, including its reason, full supporting evidence, and stale-data status, without changing recommendation generation or ordering.  
**Status:** Implemented and committed. CI verification pending. Physical-device verification pending.

### Objective

The Visuals page previously displayed only the first five recommendations even when the overview reported a larger recommendation count. This milestone makes the complete available recommendation set inspectable while retaining a compact default preview.

### Work implemented

1. Kept the initial five-recommendation preview.
2. Added a Show all recommendations / Show fewer recommendations control when more than five recommendations are available.
3. In expanded mode, displays every recommendation in the existing goal-aware order.
4. Displays each recommendation's priority, title, character, action, reason, and all evidence entries.
5. Displays an explicit stale-data indicator for recommendations based on stale account evidence.
6. Preserved the existing deterministic recommendation model, priority ordering, development-goal behavior, and analysis pipeline.

### Implementation

- Visuals recommendation expansion: [54c554c](https://github.com/berao609/Irminsul-codex/commit/54c554cf8dd851034f49520094aa41c724e3a5ca).
- README update: [89cd2e4](https://github.com/berao609/Irminsul-codex/commit/89cd2e45dc373d9984688f30d537034b4a41493b).

### Verification

- CI verification: pending.
- Physical-device verification: pending.

### Qualifications

- The expansion is a presentation change. It does not add filtering, sorting, farming calculations, material requirements, or new recommendation rules.
- The initial preview remains five items. Full details are shown only after the user expands the list.
- No account data, credentials, persistence, navigation, theme, or JSON export behavior was changed.

### Next development checkpoint

After CI and device verification, continue with a focused explanation of account-data coverage and freshness concerns so users can understand which source sections are missing, unavailable, failed, stale, or not freshness-assessed. Preserve the distinction between unknown freshness and stale data.


## Milestone 039 - Player-Friendly Character Recommendation Cards

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Present character recommendations as visual, understandable cards instead of technical diagnostic text  
**Status:** Implemented, CI verified, and physical-device tested by the user (Debug build #264).

### Objective

Make the Visuals page understandable to Genshin players by presenting character-specific findings in visual cards. Hide internal P1-P4 codes from the player-facing UI, explain the issue in plain language, and give a clear next action. Preserve deterministic findings, evidence, priorities, and recommendation ordering.

### Work implemented

1. Replaced the plain text recommendation rows with character cards showing the character name, an available portrait image, and a visual fallback using the character's element when no portrait is supplied or the image cannot load.
2. Extended the character snapshot model with an optional portrait icon and mapped it from available character icon/image fields without requiring it to exist.
3. Reworded data-review recommendations in player-friendly language. Artifact completeness warnings now explain that fewer than five equipped artifacts were found in the account data and advise refreshing/checking the character details.
4. Explicitly clarify that an incomplete-data warning is not a judgment that the character's build is weak.
5. Replaced P1-P4 codes in the UI with plain-language labels. The underlying priority enum and deterministic recommendation ordering remain unchanged.
6. Preserved the existing five-card preview and Show all / Show fewer behavior. Expanded cards retain additional explanation and stale-data notices.
7. Added a mapper test for optional character portrait mapping and the absent-icon fallback data case.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/model/AccountSnapshot.kt`
- `app/src/main/java/com/irminsul/codex/data/mapper/AccountExportMapper.kt`
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/test/java/com/irminsul/codex/data/mapper/AccountExportMapperTest.kt`
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `README.md`

### Implementation commits

- Model portrait field: `b1fc572c247751877842fb48259d777330f6fb48`
- Portrait mapping: `cea32656107dbf643e6edfa82be38c39c7222ad2`
- Mapper test: `cd8d031fdaa84c0552ea0f3b6489d2c8b9a71d82`
- Coil dependency catalog: `cdc28df5b43c3b71876afcc9c940d13c2f585490`
- Coil app dependency: `1a0bfe98bf5c705bed83c44a20602c943b4b0143`
- Recommendation cards: `76d91d41b125bc4f286271c441339cff82eb966b`
- Portrait loading fallback: `aaf1891349ae3587ceb5edff2b0eff7ba8572d78`
- Artifact-count presentation correction: `c4962d16535c0e9fe990138eb446164bd6a7713b`

### Verification evidence

- CI workflow run #264 succeeded for the implementation and milestone documentation commits. The Debug build, unit tests, Room schema check, signature verification, and artifact upload completed successfully; Room instrumentation tests were skipped by the workflow's decision logic.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.264-vc266`, artifact ID `15003606569`, size 8,916,185 bytes.
- APK SHA-256: `a714ec976587b71c854a9395d9a224b6c40f110d62ba910d340696eb9f7bc83f`.
- User confirmed that Debug build #264 was installed and tested on the physical device.

### Closure

- M39 is closed based on successful CI and the user's device-test confirmation.


## Milestone 040 - Character Showcase Foundation

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Add a Battle Chronicle-inspired character roster and a detailed selected-character build view using the account data already mapped by the app.  
**Status:** Implemented, CI verified, and physical-device tested by the user (Debug build #266).

### Objective

The Visuals page previously showed character portraits only inside recommendation cards. This milestone introduces a dedicated character showcase so the account's retrieved characters and their existing build data can be explored directly.

### Work implemented

1. Added a horizontally browsable character roster with portrait artwork, character name, and level. Selecting a character opens its detail view.
2. Added a character profile header with portrait, element, rarity, level, friendship, and constellation count.
3. Added a weapon section with weapon artwork, level, refinement, rarity, and stale-data indication.
4. Added an artifact section with available artifact artwork, slot, level, main stat, and substats. Missing, unavailable, and failed artifact data remain distinguishable from an empty equipped-artifact list.
5. Added selected character attributes, talent artwork and levels, and constellation artwork with unlocked/locked/unknown states.
6. Reused source-provided image URLs already mapped by the account parser. Missing or failed images display local visual fallbacks. No external asset catalog, new network source, backend, or new dependency was introduced.
7. Kept the showcase within the existing Visuals destination. Account analysis and recommendation behavior remain unchanged.

### Files changed

- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `README.md`

### Implementation commits

- Character showcase UI: [24b40ea](https://github.com/berao609/Irminsul-codex/commit/24b40ea1abb92d88263ac84fb6ed9658cb9f1437).
- README: [9f4ed84](https://github.com/berao609/Irminsul-codex/commit/9f4ed847d3072424fcff76ef9c8845187b8d9b87).

### Verification

- Implementation commit: `24b40ea1abb92d88263ac84fb6ed9658cb9f1437`.
- README commit: `9f4ed847d3072424fcff76ef9c8845187b8d9b87`.
- Milestone documentation commit: `3de8f87`.
- GitHub Actions #266 (implementation): success. Unit tests, Debug APK build, Room schema check, APK signature verification, and artifact upload passed. Room instrumentation tests were skipped by workflow logic.
- GitHub Actions #267 (README): success.
- GitHub Actions #268 (milestone documentation): success.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.266-vc268`, artifact ID `15005853740`, size 8,946,411 bytes.
- User confirmed the character showcase was tested on the physical device using build #266.

### Qualifications

- The showcase can display only characters and fields returned by the current account-data source. It does not infer missing equipment or fabricate artwork.
- The UI uses existing mapped URLs. This milestone does not yet add a separate maintained game-asset catalog, alternate splash/portrait artwork, artifact-set gallery, or full-screen artwork viewer.
- The roster is horizontally browsable in this first iteration. A richer multi-column roster layout can be considered after device feedback.
- The showcase requires a retrieved character snapshot. Users must run Analyze Account before character profiles can be populated.


## Milestone 041 - Analysis Objective Validation and Clarity

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Validate the four recommendation-ordering objectives, correct misleading player-facing labels and explanations, and make each recommendation's selected-goal rationale visible.  
**Status:** Implemented and committed; CI verification pending; physical-device verification pending.

### Objective

The user reported that Balanced, Builds, Stat Integrity, and Data Confidence appeared to show the same results. Repository inspection found that the objectives were designed to reorder the same evidence-backed findings, not generate different findings. The domain analyzer already assigned distinct category orders, but the UI did not explain the selected focus, and the test for Stat Integrity incorrectly expected build errors to rank first.

### Findings

1. The four objectives are ordering strategies over the same recommendation set. They do not filter, generate, or alter findings.
2. The initial five-card preview can look unchanged when the selected account has no findings in the categories that would otherwise move ahead. An objective cannot surface a category for which no finding exists.
3. The Build label represented build-validation reliability, not character build optimization or meta build recommendations.
4. Data Confidence currently reorders character-level missing/unverified information checks. It does not create account-wide freshness or coverage recommendations.
5. The Stat Integrity unit test contradicted the intended objective by expecting a build error ahead of a stat discrepancy.

### Work implemented

1. Clarified the goal-aware analyzer documentation: objectives reorder existing evidence, and cannot create missing findings.
2. Corrected the Stat Integrity test to expect stat discrepancies first.
3. Added a test asserting the complete and distinct recommendation-category order for all four objectives.
4. Renamed the player-facing objectives to Balanced, Build quality, Stat accuracy, and Data completeness. The underlying enum values remain unchanged for compatibility.
5. Added a plain-language explanation of the selected objective and clarified that switching objectives reorders the same findings without changing their evidence or severity.
6. Added each recommendation's goal-specific prioritization rationale to its character card.
7. Preserved the base recommendation priorities, evidence, actions, findings, analysis calculations, and five-item preview behavior.

### External game-knowledge direction reaffirmed

This milestone also records the user's explicit requirement to integrate external Genshin community knowledge as a planned product capability. The specification now describes patch-aware character meta and relevance, character builds and artifact recommendations, weapon rankings/comparisons, theorycrafting, observed player usage, and account-aware pull evaluation. KeqingMains and Prydwen are initial candidate sources, subject to validation of access methods, licensing, attribution, freshness, and redistribution permissions. No external data integration is implemented by M41.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/analysis/GoalAwareCharacterDevelopmentAnalyzer.kt`
- `app/src/test/java/com/irminsul/codex/domain/analysis/GoalAwareCharacterDevelopmentAnalyzerTest.kt`
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `README.md`
- `docs/SPECS.md`
- `docs/PROGRESS_MILESTONES.md`

### Implementation commits

- Analyzer clarification: `59eab1785313e9692d2231ea1ae6c88f2a2c1031`.
- Distinct objective-ordering tests: `a073f7bc45be95561479721fb39e179420cfea4b`.
- Objective labels, explanations, and per-card rationale: `dfe2f4c191ca625dd338f4d9b5d313c63376154f`.
- README roadmap update: `6bfaf5c17b3be3f9ab2725c8e991afe7c410e15d`.
- Product specification update: `230eabcf68abc77c71a143e3a53d790ef8e99dba`.

### Verification

- [Run #270](https://github.com/berao609/Irminsul-codex/actions/runs/45584655813) succeeded for the analyzer clarification commit.
- [Run #271](https://github.com/berao609/Irminsul-codex/actions/runs/45584683242) succeeded for the objective-ordering tests. The full unit-test suite, Debug APK build, Room schema check, APK signature verification, and artifact upload passed; Room instrumentation tests were skipped by workflow logic.
- [Run #272](https://github.com/berao609/Irminsul-codex/actions/runs/45584722292) succeeded for the UI implementation. The full unit-test suite, Debug APK build, Room schema check, APK signature verification, and artifact upload passed; Room instrumentation tests were skipped by workflow logic.
- [Run #273](https://github.com/berao609/Irminsul-codex/actions/runs/45584794802) succeeded for the README update.
- [Run #274](https://github.com/berao609/Irminsul-codex/actions/runs/45584837090) succeeded for the external game-knowledge product specification update.
- [Run #275](https://github.com/berao609/Irminsul-codex/actions/runs/45584906490), for this milestone documentation commit, was still running when this verification record was updated.
- Physical-device verification: pending.

### Qualifications

- The objectives intentionally do not produce four different analysis reports. They change recommendation order only. If the account has no findings in a goal's focus category, that objective cannot make such findings appear.
- Build quality refers to deterministic validation of currently available build data. It is not yet an optimizer that recommends best-in-slot weapons, artifact sets, or rotations.
- Data completeness refers to the existing character-level data review recommendation category. Account-wide section coverage and freshness remain displayed separately.
- External meta/build/weapon/pull knowledge is planned, not implemented. External sources must not be scraped or republished without checking their permitted access and terms.
- No account data, credentials, persistence schema, network behavior, or recommendation-generation rules were changed.
