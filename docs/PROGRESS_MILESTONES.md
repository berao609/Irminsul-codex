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
- `app/src/main/java/com/irminsul/codex/api/HoYoLabClient.kt`
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
**Status:** Closed. Corrective revision implemented, CI verified in run #287, and user-confirmed physical-device test passed.

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
- [Run #275](https://github.com/berao609/Irminsul-codex/actions/runs/45584906490) succeeded for the milestone documentation update.
- Physical-device verification: pending.

### Qualifications

- The objectives intentionally do not produce four different analysis reports. They change recommendation order only. If the account has no findings in a goal's focus category, that objective cannot make such findings appear.
- Build quality refers to deterministic validation of currently available build data. It is not yet an optimizer that recommends best-in-slot weapons, artifact sets, or rotations.
- Data completeness refers to the existing character-level data review recommendation category. Account-wide section coverage and freshness remain displayed separately.
- External meta/build/weapon/pull knowledge is planned, not implemented. External sources must not be scraped or republished without checking their permitted access and terms.
- No account data, credentials, persistence schema, network behavior, or recommendation-generation rules were changed.


### Post-device-test correction: remove ineffective analysis modes

The user tested the M41 build and confirmed that switching from Balanced to Build quality left the same Jean artifact-data finding, evidence, and next action on screen. Only the explanatory sentence changed. This exposed that the four controls did not provide four useful analyses.

**Product decision:** The current app does not need selectable analysis objectives. Its analyzers generate one shared set of findings, and the four objectives only reorder categories. That distinction is too small and data-dependent to justify four prominent analysis controls.

**Corrective work implemented:**

1. Remove the four objective chips and their changing explanatory text from the Account Analysis screen.
2. Present one consistent balanced recommendation order.
3. Remove the goal-specific rationale sentence from each recommendation card. Keep the actual finding, explanation, action, priority, evidence, and stale-data warning.
4. Remove objective-selection state and its ViewModel action. The application always requests the existing balanced order.
5. Retain the domain analyzer's objective-ordering tests as internal coverage for possible future use, but do not expose these modes in the current product.
6. Revisit user-selectable priorities only when they lead to genuinely different useful recommendations or views. Filters for concrete account areas may be considered separately.

The user tested the corrected single-analysis workflow on the physical device and confirmed it worked. Corrective implementation commit: aef0949021cb4d1b1e968e93ed77aa44f2240b4f. GitHub Actions run #287 succeeded. M41 is closed.


## Milestone 042 - Treat Unequipped Artifacts as a Valid State

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Stop generating data-completeness findings solely because a character has fewer than five equipped artifacts. Preserve actual artifact retrieval failures and existing build/stat findings.  
**Status:** Closed. Implementation and regression tests committed; CI run #289 succeeded; user-confirmed physical-device test passed.

### Product correction

Empty artifact slots are a valid player choice. Having zero, one, four, or five artifacts equipped is not inherently an account defect and should not trigger a recommendation to refresh account data.

### Work implemented

1. Removed the completeness rule that marked an available artifact list with fewer than five entries as incomplete.
2. Kept distinct artifact states: missing, unavailable, failed, stale, and available. Genuine unavailable or failed retrieval states remain reviewable.
3. Added regression coverage for available artifact lists with 0, 1, 4, and 5 entries. These states no longer produce an artifact-only completeness finding.
4. Added coverage confirming unavailable artifact data is still reported distinctly.
5. Updated README feature documentation.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/analysis/UnifiedCharacterAssessmentAnalyzer.kt`
- `app/src/test/java/com/irminsul/codex/domain/analysis/UnifiedCharacterAssessmentAnalyzerTest.kt`
- `README.md`
- `docs/PROGRESS_MILESTONES.md`

### Implementation commits

- Analyzer correction: `e7f99be220b690986ec88db70b066faf765de477`
- Regression tests: `01be522a5bf9447d1d59ce1b6ec85b060fd551b6`
- README: `25806e052d5eb5e687ecc936b6931a3e5f10391f`

### Verification

- CI and physical-device verification pending.
- Regression tests cover available artifact counts of 0, 1, 4, and 5, plus unavailable artifact data.
- Existing build-error, stat-mismatch, and warning recommendations were not modified.

### Closure

- CI run #289 succeeded for regression-test commit 01be522a5bf9447d1d59ce1b6ec85b060fd551b6.
- User-confirmed physical-device verification passed on build #289.
- M42 is closed.

## Milestone 043 - Account-First Investment Intelligence Requirements

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Establish the product rules and architecture boundary for selective, account-relative investment recommendations and the hybrid external game-knowledge lifecycle.  
**Status:** Requirements and architecture decision documented; implementation not started.

### Objective

The user clarified that the app should not flag every unbuilt or underinvested character. It should focus attention on characters who genuinely deserve resources for the current account. Community knowledge is needed to evaluate character roles, strengths, team synergies, build requirements, and changing game relevance. The user selected a hybrid knowledge-update approach: automatically discover and collect updates, then verify and curate them before they influence recommendations.

### Agreed product rules

1. Keep account diagnostics separate from investment intelligence. A missing-data or validation finding is not automatically a reason to invest in that character.
2. Evaluate characters relative to the actual account, including owned roster, weapons, development, team options, role coverage, and known resource constraints.
3. Rank candidates using explainable factors such as account value, team synergy, role redundancy, current game-mode relevance, investment requirements/cost, and evidence quality.
4. Show a selective shortlist rather than treating every underbuilt character as a deficiency. Underbuilt does not mean high priority.
5. Explain why each candidate matters to this account, which teams it improves or enables, what is required, and why it ranks above alternatives.
6. Recalculate when relevant account data or trusted game knowledge changes, and explain material priority changes.
7. Preserve deterministic authority for account facts, numerical calculations, and explicit ranking rules. AI assists with evidence synthesis and explanations; it does not invent account state or silently override deterministic results.

### Agreed hybrid knowledge lifecycle

1. Automatically detect and collect updates from supported sources using permitted access methods.
2. Keep newly collected information in a candidate/staging state, not trusted recommendation input.
3. Verify source permissions, attribution, freshness, patch context, evidence quality, assumptions, and conflicts.
4. Publish verified knowledge as versioned records with source provenance and evidence type.
5. Recalculate affected account priorities when trusted knowledge or account state changes, preserving enough history to explain what changed.

Automatic collection is not automatic trust and does not authorize unrestricted scraping. Candidate sources such as KeqingMains and Prydwen require access, terms, licensing, attribution, and technical validation. No mandatory backend or paid AI service is approved by this decision. Core account viewing and deterministic analysis must remain useful without cloud AI.

### Documentation updated

- docs/SPECS.md: added account-first investment intelligence, hybrid external game-knowledge lifecycle, and AI responsibility sections.
- docs/PROGRESS_MILESTONES.md: closed M41 and M42 using user-reported device verification and observed CI evidence; recorded this requirements milestone.

### Implementation boundary

This milestone defines requirements only. It does not add external-source collection, a backend, an AI service, investment scoring code, or new UI. Those require subsequent focused milestones and source-access validation.

### Next development checkpoint

M44 should define and validate the initial external knowledge source set and its permitted update/access mechanisms before implementing a collector or importing community data. In parallel, the deterministic investment-ranking criteria should be specified against the current account model before implementation.


## Milestone 044 - External Knowledge Source Validation and Investment Input Boundary

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Validate the initial community and structured-data source candidates, document permitted access boundaries, and define the evidence dimensions required for deterministic account-relative investment prioritization.  
**Status:** Closed. Research and requirements documented; no external source collector, dataset import, AI service, investment scoring implementation, or new UI added.

### Objective

Establish which external sources can safely support future investment intelligence and identify what the current account model can and cannot support. Avoid building an automated collector or ranking engine on unverified source rights or missing account evidence.

### Source review and decisions

1. **KeqingMains (KQM):** The TCL repository describes community-submitted knowledge vetted by KQM Theorycrafting Editors. The public repository's root does not expose a LICENSE file. KQM is retained as a linked research reference, but guide/TCL content is not approved for automated ingestion, copying, or redistribution without an applicable content license or explicit permission. Metadata-only revision monitoring may be evaluated separately.
2. **Prydwen:** Its Terms of Service, last updated March 2026, prohibit scraping, crawling, and bulk extraction. Prydwen remains link-only unless a supported API, license, or written permission is obtained.
3. **genshin-db:** The repository declares MIT and supplies structured static game records, but its README identifies Fandom Wiki and GenshinData as upstream sources. The package license does not by itself establish rights to every upstream record, text, or asset. It remains a technical/schema evaluation candidate only until those rights and attribution requirements are checked.
4. **genshin.dev:** The API repository declares OSL-3.0. Its redistribution and external-deployment conditions require compatibility review, and the software license does not independently clear third-party game data or assets. It is not selected for the initial integration.
5. **Official HoYoverse content/assets:** No general redistribution permission was established. M44 does not authorize scraping or bundling official site/game assets.

No third-party community guide, ranking, or game-asset dataset is approved for automated content ingestion or redistribution by this milestone. Public accessibility is not permission to republish. The source register and reference links are recorded in `docs/SPECS.md` section 3.10.

### Current account-model assessment

The existing normalized model already exposes owned character identity, level, rarity, element, constellation count, weapons, artifacts, stats, talents, equipment integrity, build validation, stat reconciliation, section coverage, and freshness where returned by the account source.

The current normalized model does not yet provide explicit, validated records for character roles, team/rotation synergy, patch- and mode-specific relevance, user resource inventory and investment costs, or observed-usage methodology. Those dimensions cannot be inferred reliably from character level, rarity, or tier labels.

### Deterministic ranking criteria established

Future account-relative investment ranking must consider:

1. Incremental account value and role/capability coverage.
2. Synergy with owned characters and validated team/rotation evidence.
3. Current relevance for identified game modes and patch context.
4. Redundancy against owned alternatives and existing investment.
5. Investment cost and efficiency relative to actual resource constraints.
6. Evidence confidence, provenance, freshness, and source disagreement.

Theorycrafting, editorial evaluations, calculated comparisons, and observed usage must remain distinct evidence types. Missing inputs remain unknown and reduce confidence; they must not become negative character ratings. Do not introduce arbitrary numerical weights or a universal tier score before normalized evidence supports them.

### Files changed

- `docs/SPECS.md`: added the initial external source validation register and the deterministic investment-ranking input boundary.
- `docs/PROGRESS_MILESTONES.md`: recorded M44 research findings and the next checkpoint.

### Verification

- M43 SPECS CI #292: success.
- M43 milestone documentation CI #293: success.
- SPECS commit `6a5c42294317641e5c558b66606f321e06b8a3b6`; CI #294 success: https://github.com/berao609/Irminsul-codex/actions/runs/45602354271.
- Milestone documentation commit `d1d383d3096b790a2382ff849bf4c700315998fc`; CI #295 success: https://github.com/berao609/Irminsul-codex/actions/runs/45602387221.
- Both documentation files were refetched and the M44 sections confirmed present.
- No runtime behavior changed. No APK or device test is applicable to this research/documentation-only milestone.

### Outcome

The project now has a conservative source-access boundary and an evidence-based definition of what the current account model can support. KQM and Prydwen remain useful linked references, but neither is approved for automated content ingestion. Structured game-data candidates require upstream rights review. Investment prioritization has defined factors, but implementation must wait until missing knowledge and account inputs can be represented explicitly.

### Next development checkpoint

M45 defined the versioned game-knowledge record, social-source access boundaries, usage-observation context, and deterministic investment-shortlist contract. M46 should implement and test the smallest domain-level knowledge record and shortlist contracts without adding collectors or platform SDKs.


## Milestone 045 - Versioned Game Knowledge and Investment Shortlist Contract

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Define the smallest versioned, source-attributed knowledge record; classify source channels including HoYoLAB and social platforms; preserve usage-stat methodology; and specify the deterministic, account-relative investment-shortlist output.  
**Status:** Requirements documented; no runtime implementation, source collector, platform SDK, AI service, scoring engine, or new UI added. CI verification pending.

### Objective

Prepare a trustworthy knowledge and recommendation contract that can support selective account-first investment decisions. Include community usage statistics and public creator content as possible evidence without treating public visibility as permission to scrape or republish.

### Repository retrieval

Before updating the specification, retrieved current `AGENTS.md`, `README.md`, `docs/SPECS.md`, `docs/PROGRESS_MILESTONES.md`, and the current `AccountSnapshot` model from `main`. Confirmed that the normalized account model includes character, weapon, artifact, talent, constellation, validation, reconciliation, coverage, and freshness information, but lacks explicit normalized role, synergy, patch relevance, and resource-cost records.

### Source-channel decisions

1. **HoYoLAB:** Public community posts demonstrate that Abyss and Stygian usage reports are discoverable, including reports attributed to YSHelper/HxG. HoYoLAB is a publication/discovery channel, not an assumed official usage-data API. Its reviewed terms restrict unauthorized scraping and reproduction. Use canonical links and manual curation only; seek the original data publisher for an authorized dataset/API.
2. **YSHelper / HxG and other original data publishers:** Preferred candidates for usage metrics if a supported API, license, or explicit permission is available. Preserve the exact sample population, denominator, game mode/cycle, difficulty, encounter, collection period, ownership filters, and methodology.
3. **YouTube:** Official Data API can support public video metadata discovery, subject to developer policies and quotas. Official playback links/embeds remain the path to video content. Do not assume transcript/caption harvesting or republication rights.
4. **X:** Public-post API access exists through a registered developer application, but current access tier, cost, rate limits, display, retention, and redistribution rules must be checked before any adapter is approved.
5. **TikTok:** Research API access is limited to eligible approved research applicants and is not a general-purpose commercial content-search API. No automated collector is approved.
6. **Facebook:** Public Page access may require Graph API permissions and review. Meta Content Library/API access is restricted to qualified research organizations and approved programs. No broad Facebook scraper is approved.
7. **KQM, Prydwen, and structured game-data candidates:** M44's source decisions remain in force.

No platform adapter is approved for implementation by M45. Public posts, videos, and creator demonstrations are candidate discovery evidence. Full posts, media, comments, transcripts, and personal profile histories must not be mirrored by default. Store only permitted source metadata, canonical links, and minimally necessary reviewed claims. Respect platform terms, creator rights, access revocation, deletion, rate limits, and official embed requirements.

### Versioned knowledge record

The M45 specification defines a small typed record containing schema version, stable record ID, subject, claim type, minimal structured claim, evidence class, source references, game/patch/mode context, publication/observation/retrieval timestamps, validity/expiry, review state, reviewer notes, and a qualitative confidence state.

Source registry metadata is separate from claim evidence and records access method, terms/license/permission state, attribution, allowed actions/fields, retention/deletion requirements, quotas, and last review date. A changed claim or context creates a new version or superseding record. AI extraction remains candidate-only until verified.

### Usage-statistics handling

Abyss and Stygian usage observations must not be conflated. Each observation preserves source methodology and the exact denominator/population when available. Clear-only samples, ownership filters, difficulty, encounter buffs, and collection windows affect interpretation. Usage is contextual evidence and cannot independently establish character strength or investment priority. Unknown methodology lowers confidence and prevents unsupported cross-cycle comparisons.

### Deterministic shortlist contract

The first recommendation output is a small selective account-relative shortlist, not a universal tier list or an alert for every unbuilt character. The contract includes a versioned ruleset, evaluation time, account snapshot reference, priority band, stable reason codes, account benefit, owned-team synergy, patch/mode context, redundancy comparison, supported investment requirements, trusted evidence IDs/links, unknown/conflicting factors, and an explanation of why the candidate ranks ahead of alternatives.

The initial ruleset is deterministic and explainable. It must not use an LLM-generated score, arbitrary numeric weights, or usage rate as a standalone ranking signal. Unknown evidence stays unknown. Account diagnostics remain separate from investment recommendations. If evidence cannot justify a recommendation, the shortlist may be empty.

### Files changed

- `docs/SPECS.md`: added sections 3.12-3.14 covering the versioned knowledge record, social-source policy, usage observations, and deterministic investment-shortlist contract.
- `docs/PROGRESS_MILESTONES.md`: closed M44 with CI evidence and recorded M45 scope, decisions, and verification state.

### Verification

- M44 SPECS CI #294: success, https://github.com/berao609/Irminsul-codex/actions/runs/45602354271.
- M44 milestone documentation CI #295: success, https://github.com/berao609/Irminsul-codex/actions/runs/45602387221.
- M45 SPECS CI #296 and milestone documentation CI #297: success; both runs completed successfully on 2026-11-25.
- No runtime behavior changed. No APK or physical-device test is applicable to this documentation-only milestone.

### Outcome

M45 defines how the app can represent source-attributed, patch-aware, versioned knowledge and use it to produce selective account-relative investment recommendations. HoYoLAB usage reports and social content can contribute evidence, but only through permitted access and with methodology preserved. The source platform, original data publisher, and evidence class remain distinct.

### Next development checkpoint

M46 should implement and unit-test the smallest domain-level knowledge-record and investment-shortlist contracts. Keep source registry metadata and evidence provenance explicit. Do not add network collectors, platform SDKs, AI services, a numeric score, or UI until the domain contract is validated.


## Milestone 046 - Domain Contracts for Versioned Knowledge and Investment Shortlists

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Add typed domain contracts and deterministic unit tests for source-attributed game knowledge and a selective account-relative investment shortlist.  
**Status:** Closed. Implementation and unit tests passed CI #310. No UI, persistence, network collector, platform SDK, AI service, or APK-facing behavior was added.

### Objective

Turn the M45 requirements into a small testable domain boundary. Preserve claim provenance and review state, keep usage observations methodologically explicit, and prevent unsupported or unpermitted knowledge from driving investment priority.

### Repository retrieval

Retrieved the current root `AGENTS.md`, `README.md`, `docs/SPECS.md`, `docs/PROGRESS_MILESTONES.md`, relevant domain models and tests, `app/build.gradle.kts`, and `.github/workflows/build.yml` from `main`. Confirmed M45 CI #296 and #297 both succeeded and the starting main head was `834ad631033084da1e62f3d91397c2882eb5d4c8`.

### Implementation

1. **Versioned knowledge domain model** in `GameKnowledge.kt`:
   - Typed subject, claim, evidence, review, confidence, source-platform, access, and permission states.
   - Separate source-registry entries and per-claim canonical source references.
   - Explicit game/patch/mode context and distinct publication, observation, retrieval, validity, expiry, and review timestamps.
   - A structured usage-observation context retaining mode/cycle, difficulty, encounter, sample size, numerator, denominator and population, ownership context, collection period, methodology, and preliminary/updating/finalized/qualified status.
   - Structural validation for schema version, identifiers, source references, HTTPS URLs, publisher-host consistency, approved source permission, verified-review metadata, timestamp consistency, and usage count/date consistency.
   - Candidate records remain representable without being treated as verified.

2. **Deterministic investment shortlist** in `InvestmentShortlist.kt`:
   - Versioned ruleset and evaluation timestamp tied to an explicit account snapshot reference.
   - Priority bands: High priority, Worth investing, Situational, Low priority for now, and Already sufficient.
   - Stable reason codes, owned-team references, investment requirements, supporting record IDs, unknown/conflicting factors, and plain-language explanations.
   - Only verified, supported, structurally valid records from approved source-registry entries can independently support a priority.
   - Usage-observation records may accompany a recommendation but cannot independently promote a character.
   - No account benefit or no eligible independent evidence produces no investment recommendation.
   - No aggregate numeric score or arbitrary factor weights. Deterministic explicit rules only.
   - This is a domain contract, not yet wired to the account snapshot, current diagnostics, persistence, or UI.

3. **Unit tests**:
   - Knowledge validation: provenance, source host/permission, review requirements, usage metadata, impossible counts/dates, missing usage context, and candidate state.
   - Shortlist evaluation: role-gap and team-improvement bands, no-benefit/unknown omission, unreviewed/rejected/limited evidence rejection, usage-only rejection, usage as contextual supporting evidence, uncertainty, redundancy, already-sufficient state, stable ordering, and source-permission gate.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/model/GameKnowledge.kt`
- `app/src/main/java/com/irminsul/codex/domain/model/InvestmentShortlist.kt`
- `app/src/test/java/com/irminsul/codex/domain/model/GameKnowledgeRecordValidatorTest.kt`
- `app/src/test/java/com/irminsul/codex/domain/model/InvestmentShortlistEvaluatorTest.kt`
- `docs/PROGRESS_MILESTONES.md`

### Implementation commits

- `34826c0b26e805b01e5c6ff1873ce4de5a3e36b6` - Add versioned game knowledge domain contract.
- `80770e693bfce7fa1177fd05123d5708f2e2505a` - Add deterministic investment shortlist contract.
- `0e824a79313168dbe1123fc414232f5fc5aa642a` - Test versioned game knowledge validation.
- `c0cc57276295df52fe6659b01163d04ecac4073d` - Test account-relative investment shortlist rules.
- `212645a7d01f1d3c85c0367b0fc28a797cf696fb` - Enforce source registry permission and provenance checks.
- `319849ec897db6054340393d95ed80bec650ef47` - Require validated permitted knowledge for investment priorities.
- `754e12e129db248d74f377bec8fd20e2c7573c85` - Test source permission gate for investment evidence.
- `45ba92238f7f7afe0d910724252e3b2262ce715a` - Cover source host and permission validation cases.
- `b14d1840d8ed9905fa8125d1d1cb6d1461a261be` - Prevent usage-only evidence from qualifying investment recommendations.
- `68eaadd11cda4acbda10b2438e1797272c4fd69f` - Exclude knowledge outside its validity window from investment decisions.
- `65d55c13e6a0a6a0fc382b86b2155bf8beb67861` - Test knowledge validity window before investment use.

### Verification

- M45 CI #296: success, https://github.com/berao609/Irminsul-codex/actions/runs/45603259730.
- M45 CI #297: success, https://github.com/berao609/Irminsul-codex/actions/runs/45603302548.
- M46 implementation CI #310: success, https://github.com/berao609/Irminsul-codex/actions/runs/45604739493. Unit tests, Debug APK build, Room schema verification, APK signature verification, and artifact uploads passed. Room instrumentation tests were skipped because no Room/persistence code changed.
- M46 closure-documentation CI #311: success, https://github.com/berao609/Irminsul-codex/actions/runs/45605101295.
- The final M46 Debug APK artifact was irminsul-codex-debug-v1.0.0-dev.310-vc312, artifact ID 15015884126, size 8,969,856 bytes. The Room schema artifact was irminsul-room-schema-v1-run-310, artifact ID 15017312621, size 985 bytes.
- Intermediate runs #301-#306 exposed test-contract issues during incremental commits. These were corrected in subsequent commits; final CI #310 passed.
- No APK-facing behavior changed. Physical-device verification is not applicable to these domain-only changes.

### Boundaries and limitations

- Source-registry entries and knowledge records are in-memory domain models only; no database schema or persistence was introduced.
- No automatic discovery or collection from HoYoLAB, KQM, Prydwen, YouTube, X, TikTok, Facebook, or other sources.
- No source is granted permission by the model itself; a registry entry must explicitly represent approved permission and allowed actions.
- No automatic knowledge verification, AI extraction, or AI ranking.
- Candidate assessment inputs are supplied to the evaluator; this milestone does not yet derive role gaps, team synergy, patch relevance, redundancy, or affordability from the live account model.
- The shortlist evaluator is not yet integrated with the existing account diagnostics. Existing diagnostic behavior remains unchanged.

### Next development checkpoint

M47 should validate how these contracts map to real account and game-domain inputs. Identify which candidate facts can be derived reliably from the current account snapshot and which require curated knowledge or explicit unknown states. Do not add source collectors, platform SDKs, AI services, arbitrary scoring weights, or UI until the input boundary is validated.

## Milestone 047 - Account and Game-Knowledge Input Mapping

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Validate the current normalized account model against the M46 investment-shortlist inputs. Define which facts can be extracted directly, which require reviewed game knowledge, and which require new account data.  
**Status:** Closed. Mapping documentation and milestone record passed CI #312 and #313. Documentation-only milestone. No runtime code, data schema, source collector, scoring logic, AI service, or UI changed.

### Objective

Prevent the investment engine from treating raw account attributes as combat conclusions. Establish a defensible boundary between observed account facts, curated game knowledge, and currently unavailable information before implementing a snapshot-to-investment input mapper.

### Repository retrieval

Retrieved current AGENTS.md, README.md, docs/SPECS.md, docs/PROGRESS_MILESTONES.md, AccountSnapshot.kt, account export mapping, account-gap/build/stat analyzers, AccountIntelligenceEngine.kt, M46 GameKnowledge.kt and InvestmentShortlist.kt, and their relevant tests.

Confirmed M46 closure CI #310 and documentation CI #311 both succeeded before starting M47.

### Mapping results

**Directly derivable from the current account snapshot:**

- Retrieved owned-character IDs and known names, elements, rarities, and levels.
- Observed character constellation count and talent levels when populated.
- Equipped weapon identity, type, rarity, level, ascension, refinement, and available stats.
- Equipped artifact entries, slots, levels, rarity, main/sub-stats, and source-retained set payload.
- Explicit missing, unavailable, failed, stale, and available equipment states.
- Roster/equipment integrity, build validation, stat classification/reconciliation, section coverage, and freshness.
- Current/previous Abyss and Theater section presence and retained structured payload, without assuming its internal schema is normalized for team analysis.

**Requires reviewed game knowledge:**

- Character roles and capabilities.
- Team synergy, rotations, and team enablement.
- Role/capability coverage and account gaps.
- Patch- and mode-specific relevance.
- Build suitability and target investment criteria.
- Functional redundancy against owned alternatives.

**Requires additional account or game-reference data:**

- Material-cost tables for character, weapon, talent, and artifact investment.
- Actual player material inventory and resource balances for affordability.
- Validated normalized team/performance observations from mode payloads.
- Banner, pity, guarantee, and pull-preference data for future pull advice.

### Decisions and safeguards

1. The first mapper may emit only observed facts: owned character identity, known element/rarity, current character and equipment investment, equipment state, and source provenance/freshness.
2. Derived facts retain the relevant DataValue availability and retrieval timestamp. Stale inputs remain stale.
3. Missing, unavailable, failed, malformed, or unknown values remain explicit. They are not converted into zero, false, empty-success, or negative character assessments.
4. Existing build/stat/equipment findings remain account diagnostics. They can be attached as context but cannot alone generate an investment-priority recommendation.
5. Role, capability, team synergy, patch/mode relevance, and build suitability require verified, supported, permitted, in-date knowledge records.
6. Element combinations, weapon types, and co-occurrence in opaque Abyss/Theater payloads do not prove team synergy.
7. Usage statistics remain contextual evidence and cannot independently qualify an investment recommendation.
8. Redundancy requires comparable role/capability evidence for the candidate and owned alternatives. Same element or weapon type is not sufficient.
9. Investment cost and affordability remain unknown until upgrade-cost reference data and actual player material inventory are available. Resin alone is not a material inventory.
10. The first shortlist input mapper covers owned characters only. Pull evaluation and unowned-character opportunities require a separate later contract.

### Files changed

- docs/SPECS.md: added section 3.15, recording the M47 account-input mapping matrix and safeguards.
- docs/PROGRESS_MILESTONES.md: corrected stale M45/M46 verification status and recorded the M47 mapping outcome.

### Verification

- M46 implementation CI #310: success, https://github.com/berao609/Irminsul-codex/actions/runs/45604739493.
- M46 closure documentation CI #311: success, https://github.com/berao609/Irminsul-codex/actions/runs/45605101295.
- M47 SPECS commit CI #312: success, https://github.com/berao609/Irminsul-codex/actions/runs/45605691281.
- M47 milestone documentation CI #313: success, https://github.com/berao609/Irminsul-codex/actions/runs/45605805301.
- CI #313 artifact: irminsul-codex-debug-v1.0.0-dev.313-vc315, artifact ID 15016769899, size 8,969,858 bytes. Room schema artifact ID 15016104549, size 985 bytes.
- Both workflows passed unit tests, Debug APK build, Room schema verification, APK signature verification, and artifact upload. Room instrumentation tests were skipped because the changes were documentation-only.
- No runtime behavior changed. No physical-device test is applicable to this documentation-only milestone.

### Outcome

The current account snapshot can reliably describe what the player owns and how those characters are currently invested. It cannot independently determine role coverage, team synergy, meta relevance, redundancy, target investment sufficiency, or resource affordability. These conclusions must come from separately reviewed game knowledge and, where necessary, additional account data.

### Next development checkpoint

M48 should implement and unit-test a small snapshot-to-observed-facts mapper for owned characters. It should preserve source availability/freshness, map only fields already present in the current domain model, and explicitly represent unknown facts. It must not infer roles, team synergy, meta priority, material cost, affordability, or pull value. Keep this mapper separate from account diagnostics and from the M46 investment evaluator until the mapping tests establish a safe join boundary.
## Milestone 048 - Owned Character Investment Fact Mapper

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Extract directly observed owned-character investment facts from the normalized account snapshot, preserving source availability, freshness, section provenance, and collection integrity.  
**Status:** Closed. Implementation CI #330 and milestone documentation CI #331 succeeded. Domain-only change; no UI, persistence, knowledge collector, recommendation scoring, or shortlist integration.

### Objective

Implement the first safe bridge between retrieved account data and future account-relative investment intelligence. The mapper must describe what is observed without turning raw attributes into combat or investment conclusions.

### Repository retrieval

Rechecked AGENTS.md, README.md, SPECS.md, PROGRESS_MILESTONES.md, AccountSnapshot.kt, AccountExportMapper.kt, M46 GameKnowledge.kt and InvestmentShortlist.kt, and existing mapper/model test conventions. Confirmed M47 closure CI #314 succeeded before M48 implementation.

### Implementation

1. Added `OwnedCharacterInvestmentFacts.kt` with:
   - Explicit fact states: AVAILABLE, STALE, NOT_REPORTED, MISSING, UNAVAILABLE, and FAILED.
   - Generic observed facts with optional value, source timestamp, and failure reason.
   - Character, weapon, artifact, talent, constellation, and roster fact models.
   - Roster source-section status, source label, retrieval timestamps, and collection integrity.
   - An `isPartial` indicator when section status is PARTIAL, malformed source entries were omitted, duplicate character IDs exist, or source and mapped entry counts differ.
2. Added `OwnedCharacterInvestmentFactMapper.kt`, a pure mapper over `AccountSnapshot.characters`.
3. Mapped observed character identity/level/rarity/element/constellation count; weapon ID/type/rarity/level/ascension/refinement and available stat properties; artifact ID/slot/rarity/level/set payload/main/sub-stats; and reported talent/constellation levels and states.
4. Preserved roster and nested weapon/artifact missing, unavailable, failed, and stale states. A stale roster propagates stale status to known nested values. Nested stale equipment retains its own last-retrieved timestamp.
5. Preserved raw artifact set payload as `StructuredData`; no set-bonus or build interpretation was introduced.
6. Kept empty equipped-artifact lists as AVAILABLE. Empty talent, constellation, and artifact-substat lists become NOT_REPORTED because the existing normalized model uses `orEmpty()` and cannot distinguish an explicitly empty source array from an omitted source field.
7. Preserved collection integrity without dropping mapped entries from the fact output. Invalid source indexes and duplicate IDs remain visible through the roster integrity object and `isPartial`.

### Tests

`OwnedCharacterInvestmentFactMapperTest.kt` covers:
- Direct mapping of owned-character, weapon, artifact, talent, constellation, and stat facts.
- Null/unreported fields remaining NOT_REPORTED rather than becoming zero or false.
- Empty equipped artifacts remaining AVAILABLE and retaining all five unoccupied slots.
- Stale roster propagation and nested stale timestamps.
- Nested weapon/artifact MISSING, UNAVAILABLE, and FAILED states with failure reasons.
- Roster MISSING, UNAVAILABLE, and FAILED states.
- Partial source-section metadata and retrieval timestamps.
- Partial/malformed/duplicate roster integrity while preserving mapped entries.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/model/OwnedCharacterInvestmentFacts.kt`
- `app/src/main/java/com/irminsul/codex/domain/analysis/OwnedCharacterInvestmentFactMapper.kt`
- `app/src/test/java/com/irminsul/codex/domain/analysis/OwnedCharacterInvestmentFactMapperTest.kt`
- `docs/PROGRESS_MILESTONES.md`

### Implementation commits

- `e029cf3f2868e3648eac8275390471aa0cca3bdd` - Add observed owned-character investment fact models.
- `a4440b35cb0b8cc62570d3032e80766d106499a7` - Add snapshot-to-facts mapper.
- `cd28c8d1dace6ba0d518f851b04bf098066995e4` and `caa5aaa33b8efd36d401e3ffd072d2e346197847` - Add mapper tests and correct the character fixture.
- `a423d2b7bf75ea06fabc2ec7cb175eb372f8cb47` - Include observed weapon and artifact stat facts.
- `1091909880601b9b681db6de02a07d8b961e26be` - Map weapon and artifact stat observations.
- `79d8ebf957606dad222b5ecd66ff41a6f5ba5068` - Test mapped weapon and artifact stat observations.
- `8bc31f5fd5742b7f8ef9a0f392ed16bec0e2d079` - Fix the character test fixture.
- `acab7ef33890fa295f8f19e57cd290372cf83896` - Test unavailable nested equipment states.
- `296aabc7d1c005c613d5ed63a39dd74730ce3a46` and `66f6946cf101aaee7c4d3f39080523f291e38b69` - Expose and test partial roster integrity.
- `53d02d3452bb1e67e7bcfeef24df91ebc4650dbd`, `57adf69a724348358bd659ef278a06e8c3acf56c`, `38b0d2d4d07fa154d69b684479b88233f3f9129d`, `168b3d9bf7381e42183da009f4f06da9a41e3f6f`, and `5d253d54dbe605b566b4badda7a707e7d8a8391c` - Preserve and test source-section metadata across roster availability states.

### Verification

- Final combined implementation CI #330: success, https://github.com/berao609/Irminsul-codex/actions/runs/45607745288.
- M48 milestone documentation CI #331: success, https://github.com/berao609/Irminsul-codex/actions/runs/45608163826.
- CI #330 passed unit tests, Debug APK build, Room schema verification, APK signature verification, artifact uploads, and temporary signing-key cleanup.
- Room instrumentation tests were skipped by the workflow because M48 did not change Room or persistence code.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.330-vc332`, artifact ID `15018157177`, size 8,984,220 bytes.
- Room schema artifact: `irminsul-room-schema-v1-run-330`, artifact ID `15019620069`, size 985 bytes.
- Intermediate runs #317-#321 failed while the implementation and test fixture were being updated in separate commits. The failures were missing fixture data and temporarily unmatched model/mapper constructor fields. These were corrected; the final combined CI #330 passed.
- No UI or persistence behavior changed. Physical-device testing is not applicable to this domain-only milestone.

### Boundaries and limitations

- The fact mapper is not connected to `InvestmentShortlistEvaluator` or the existing account diagnostics.
- It does not infer roles, capabilities, team synergy, role gaps, patch/meta relevance, build quality, redundancy, target sufficiency, investment cost, affordability, or pull value.
- Empty talent/constellation/substat lists remain NOT_REPORTED because the current `CharacterSnapshot` normalization does not preserve whether those source arrays were absent or explicitly empty.
- The raw Abyss and Theater payloads remain unparsed for team/performance inference.

### Outcome

M48 establishes a tested, provenance-aware account-fact boundary. The app can now expose observed owned-character investment state without conflating missing information with a valid zero/false value or treating incomplete retrieval as a complete roster. The result is intentionally not yet a recommendation.

### Next development checkpoint

M49 should define a typed, versioned character role/capability knowledge contract and a safe join against these owned-character facts. It should cover only evidence-backed capabilities needed to reason about account role coverage, preserve game-version/mode applicability and provenance, and reject unknown or unsupported claims. Do not add platform collectors, arbitrary weights, AI ranking, or UI at this stage.

## Milestone 049 - Typed Character Capability Knowledge and Owned-Roster Join

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Define a typed, versioned character role/capability assertion contract and safely join trusted assertions to characters observed in the M48 account roster.  
**Status:** Closed. Final implementation CI #339 and milestone documentation CI #340 succeeded. Domain-only change; no UI, persistence, source collector, recommendation scoring, or AI integration.

### Objective

Create the first typed bridge between reviewed character knowledge and a player's actual owned roster. The bridge must preserve evidence and conflicts, reject unsupported or context-mismatched knowledge, and never treat absence from an incomplete roster as proof of non-ownership.

### Repository retrieval

Rechecked AGENTS.md, README.md, SPECS.md, PROGRESS_MILESTONES.md, M46 GameKnowledge.kt and InvestmentShortlist.kt, M48 OwnedCharacterInvestmentFacts.kt and OwnedCharacterInvestmentFactMapper.kt, AccountSnapshot.kt, and existing GameKnowledgeRecordValidator tests. M48 implementation CI #330, documentation CI #331, and M48 closure CI #332 were confirmed successful before M49 implementation.

### Implementation

1. Added `CharacterCapabilityKnowledge.kt`:
   - Versioned `CharacterCapabilityKnowledgeClaim` linked to one canonical `GameKnowledgeRecord`.
   - Typed combat-role vocabulary: on-field damage dealer, off-field damage dealer, support, enabler, driver, healer, shielder, buffer, debuffer, battery, and crowd control.
   - Typed capability vocabulary: on-/off-field damage, elemental application, reaction triggering/enabling, healing, shielding, damage/defense buffs, resistance reduction, energy generation, crowd control/grouping, interruption resistance, damage mitigation, and elemental infusion.
   - Explicit AFFIRMS/REFUTES polarity and optional qualifying conditions.
   - Typed query context for game version, patch phase, mode, difficulty, and encounter.
2. Added structural and trust validation:
   - Requires supported schema, unique claim IDs, positive character IDs, and at least one explicitly typed role/capability.
   - Requires an exact source record, CHARACTER subject, matching character ID, and ROLE_CAPABILITY claim type.
   - Requires typed role/capability/polarity/condition values to exactly match the canonical source record attributes. The typed interpretation cannot silently diverge from its provenance record.
   - Requires VERIFIED + SUPPORTED knowledge, approved source permission, structural validity, and an active validity window.
   - Rejects observed-usage evidence as a basis for role/capability assertions.
   - Requested game-version/patch/mode/difficulty/encounter dimensions must exactly match populated source-record context; missing context is not a wildcard.
3. Added `OwnedCharacterCapabilityJoiner`:
   - Emits profiles only for character IDs present in an AVAILABLE or STALE M48 roster.
   - Preserves roster state and partial-coverage status.
   - Does not infer that absent characters are unowned.
   - Suppresses joins for duplicate character IDs and marks those profiles ownership-ambiguous.
   - Preserves supporting and contradicting source record IDs. Affirming and refuting claims for the same role/capability become CONFLICTED; refutation-only evidence is CONTRADICTED.
   - No accepted assertion means no role/capability evidence, not a negative role assessment.
4. Added no automatic source adapters, scraped content, persistence, UI, numerical weighting, team-synergy inference, or investment recommendations.

### Tests

`CharacterCapabilityKnowledgeTest.kt` covers:
- Joining only verified typed claims to characters actually present in the roster.
- Affirming/refuting evidence conflict preservation.
- Candidate/untrusted knowledge rejection.
- Usage-only evidence rejection.
- Expired and game-version-mismatched knowledge rejection.
- Unapproved source permission and mismatched subject identity rejection.
- Empty/invalid typed assertions.
- Exact binding between the typed claim and canonical source-record attributes.
- Stale/partial roster preservation.
- Duplicate owned-character IDs blocking ambiguous joins.
- Unavailable roster producing no ownership profiles.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/model/CharacterCapabilityKnowledge.kt`
- `app/src/test/java/com/irminsul/codex/domain/model/CharacterCapabilityKnowledgeTest.kt`
- `docs/SPECS.md`: added section 3.16.
- `docs/PROGRESS_MILESTONES.md`: this M49 record.

### Implementation and specification commits

- `9e64cc63d34976eaa1c36c0037ca1a3b9baa6a24` - Add typed character capability knowledge contract and safe join.
- `e32d891c2e671b53c72befd1dffd38b1a5828000` - Add capability validation and roster-join tests.
- `fd2c0cc07c2aa9861618cd8f3b2acf68469dd835` - Remove unused join-helper inputs.
- `60f703b27a9f905c09682660ed0e6f8bd3613649` - Specify M49 contract and trust boundaries in SPECS.
- `e250cdfc5b5a07bc6f0efde64e9a2c33ac310e52` - Bind typed assertions to canonical source-record attributes.
- `958977fd63658a335dbafa3c681abf9c50ac45de` - Test canonical payload binding.
- `3aa5785933d420ce290d9d56a05b43850fd56c4c` - Use a stable delimiter for condition attributes.

### Verification

- CI #333: initial source model, validator, and join compiled; unit tests, Debug APK build, Room schema verification, APK signature verification, and artifact uploads passed. https://github.com/berao609/Irminsul-codex/actions/runs/45610405724
- CI #334: capability-join unit tests passed; Debug APK build, Room schema verification, APK signature verification, and artifact uploads passed. https://github.com/berao609/Irminsul-codex/actions/runs/45610532811
- CI #335: join-helper cleanup passed the unit tests and Debug APK build. https://github.com/berao609/Irminsul-codex/actions/runs/45610778949
- CI #336: SPECS update passed unit tests and Debug APK build. https://github.com/berao609/Irminsul-codex/actions/runs/45610833596
- CI #337 initially failed four new tests because the fixture's generic source records did not yet mirror the typed assertion attributes. The fixture was corrected in #338; #338 passed the complete unit-test suite, Debug APK build, Room schema verification, APK signature verification, and artifact uploads. https://github.com/berao609/Irminsul-codex/actions/runs/45611040236
- Final implementation CI #339 passed the complete unit-test suite, Debug APK build, Room schema verification, APK signature verification, artifact uploads, and signing-key cleanup. https://github.com/berao609/Irminsul-codex/actions/runs/45611057722
- M49 milestone documentation CI #340 passed the complete unit-test suite, Debug APK build, Room schema verification, APK signature verification, artifact uploads, and signing-key cleanup. https://github.com/berao609/Irminsul-codex/actions/runs/45611213103
- Final implementation Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.339-vc341`, artifact ID `15018264965`, size 9,002,170 bytes. Room schema artifact `irminsul-room-schema-v1-run-339`, ID `15018469722`, size 985 bytes.
- Documentation CI #340 Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.340-vc342`, artifact ID `15019444867`, size 9,003,155 bytes. Room schema artifact `irminsul-room-schema-v1-run-340`, ID `15019838687`, size 985 bytes.
- Room instrumentation tests were skipped because M49 changes no Room or persistence code.
- No user-facing runtime behavior changed. Physical-device testing is not applicable.

### Boundaries and limitations

- Role/capability claims are curated typed assertions. This milestone does not populate them with live KQM, Prydwen, HoYoLAB, social, or creator data.
- Source record review notes and canonical source references remain necessary; a typed payload is not independently verified merely because it is well-formed.
- Context matching is exact for requested dimensions. Broader compatibility/patch-range semantics are not inferred.
- A joined profile is not a team, role-gap assessment, character ranking, build recommendation, or investment priority.
- The joiner does not connect to the M46 InvestmentShortlistEvaluator.

### Outcome

M49 establishes a provenance-bound, patch/mode-aware typed capability contract and a conservative join to observed owned characters. It preserves contradictory claims and incomplete roster states rather than manufacturing certainty. The output is evidence for later account coverage analysis, not a recommendation.

### Next development checkpoint

M50 defines and implements the deterministic role/capability coverage assessment using M49 accepted claims and M48 observed owned-character facts. Its coverage results remain tied to the requested game context and preserve unknown, stale, partial, and conflicting evidence. It does not infer team synergy, assign numerical weights, rank investments, or add UI.

### M49 follow-up - Enforce source action permissions

**Date recorded:** 2026-11-25  
**Reason:** A post-implementation trust-boundary review found that the M49 validator checked `permissionState == APPROVED` through `GameKnowledgeRecordValidator`, but did not enforce the source registry's `allowedActions`. An otherwise approved source restricted to link-only use could therefore pass the typed capability-claim gate.

**Correction:** `CharacterCapabilityKnowledgeValidator` now requires every referenced source registry entry to explicitly include `manual_claim_curation` in `allowedActions`. Otherwise the claim is rejected with `SOURCE_ACTION_NOT_APPROVED`. The M49 specification now records this additional gate.

**Regression test:** An APPROVED source whose only allowed action is `canonical_link` must not contribute a role/capability assertion.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/domain/model/CharacterCapabilityKnowledge.kt`
- `app/src/test/java/com/irminsul/codex/domain/model/CharacterCapabilityKnowledgeTest.kt`
- `docs/SPECS.md`
- `docs/PROGRESS_MILESTONES.md`

**Commits:**
- `0bf13d8e131695923cf771e1c63a0acf25da70c2` - Enforce source action permission for capability curation.
- `7da3902ed18340ebf4c69485b5d16e37203616d5` - Add source action permission regression test.
- `3c7b542fc4767c3f4f3324ae4c822ba497ec27d7` - Specify source action permission in M49 trust gates.

**Verification:**
- CI #342 (implementation) passed unit tests, Debug APK build, Room schema verification, APK signature verification, artifact upload, and temporary signing-key cleanup: https://github.com/berao609/Irminsul-codex/actions/runs/45616846889.
- CI #343 (regression test) passed the same checks: https://github.com/berao609/Irminsul-codex/actions/runs/45616867195.
- CI #344 (specification update) passed the same checks: https://github.com/berao609/Irminsul-codex/actions/runs/45616895927.
- CI #342 Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.342-vc344`, artifact ID `15023390365`, size 9,004,198 bytes. Room schema artifact ID `15023510804`, size 985 bytes.
- CI #344 Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.344-vc346`, artifact ID `15021364697`, size 9,004,213 bytes. Room schema artifact ID `15023695332`, size 985 bytes.
- Room instrumentation tests were skipped because this correction changes no persistence code.

**Boundary:** This is a trust-gate correction to M49. It adds no source adapters, scraping, content ingestion, AI, ranking, persistence, or UI.

## Milestone 050 - Deterministic Owned-Roster Role and Capability Coverage

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Aggregate M49 validated character role/capability evidence across M48 observed owned-character facts.  
**Status:** Closed; implementation CI #355 passed.

### Objective

Establish which combat roles and capabilities have affirmative evidence among the characters observed in the account roster, while retaining contradictions and refusing to turn missing knowledge or incomplete account data into negative findings. This is a reusable evidence layer for the planned lineup simulator, not a team-performance simulator or investment recommendation engine.

### Active Memory Recall and repository verification

Rechecked the current AGENTS.md, README.md, docs/SPECS.md, docs/PROGRESS_MILESTONES.md, M48 owned-character fact model/mapper, M49 capability knowledge validator/joiner, M46 knowledge and investment contracts, and the latest M49 CI run before implementation.

M49 latest progress-document commit 925143203cd382efd9f4b3e3608726717be56f69 was confirmed by CI #346: success. Run: https://github.com/berao609/Irminsul-codex/actions/runs/45617229133.

### Work implemented

1. Added RoleCapabilityCoverage.kt:
   - Typed result models for role and capability coverage.
   - States: COVERED, COVERED_WITH_CONFLICTS, CONFLICTED, NOT_ESTABLISHED, INCOMPLETE, and UNKNOWN.
   - Supporting, conflicted, and contradicted character IDs.
   - Supporting and contradicting source-record IDs.
   - Preserved evaluation time, requested game context, roster state, partiality, and rejected M49 claims.
2. Added RoleCapabilityCoverageAssessor.kt:
   - Consumes only M49 OwnedCharacterCapabilityKnowledge; it cannot bypass M49 source, review, validity, or context validation.
   - Aggregates each role and capability across unambiguous owned-character profiles.
   - Any affirmative evidence from an unambiguous owned character establishes coverage.
   - Conflicts remain visible. A refutation from one character does not cancel another character's affirmative evidence.
   - Excludes ownership-ambiguous duplicate profiles and marks the result partial.
   - Missing/unavailable/failed rosters produce UNKNOWN.
   - Stale or partial rosters without affirmative evidence produce INCOMPLETE.
   - No affirmative evidence in a complete roster produces NOT_ESTABLISHED, explicitly not a proven account gap.
3. Added tests for multi-character aggregation, positive coverage plus conflicts, conflict-only evidence, refutation-only evidence, no-claim neutrality, partial and stale rosters, unavailable rosters, ambiguous duplicate profiles, context preservation, and rejected-claim preservation.
4. Added SPECS section 3.17 defining the M50 contract and its limits.

### Files changed

- app/src/main/java/com/irminsul/codex/domain/model/RoleCapabilityCoverage.kt (new)
- app/src/main/java/com/irminsul/codex/domain/analysis/RoleCapabilityCoverageAssessor.kt (new)
- app/src/test/java/com/irminsul/codex/domain/analysis/RoleCapabilityCoverageAssessorTest.kt (new)
- docs/SPECS.md

### Implementation and specification commits

- 4ca4bbc0b2a7665f30b988f0a0bbdcb2042ed398 - Add role/capability coverage result models.
- 95cd022b455d19644e95f521c0de4eb1dd1865c6 - Add deterministic owned-roster coverage assessor.
- 9e62f0e4cfeec177463c2aa102cba101dcfb84a8 - Add coverage aggregation tests.
- 39a74dc39361b7cf6981db07dcba2f7954f93bd8 - Treat stale roster coverage as incomplete when no affirmative evidence exists.
- 2bfd113c6cfe4013f60dd77631e8bc4fb952537d - Test stale-roster uncertainty.
- a611856133f6f0c0f57c6658cbdbb3f18c358d45 - Specify M50 coverage contract and evidence boundaries.

### Verification

- Final combined implementation CI #355 passed for head `9eaf816b66e73e3c7897848e6f77ba5ea52d3c63`: https://github.com/berao609/Irminsul-codex/actions/runs/45620405448.
- CI #355 passed unit tests, Debug APK build, Room schema verification, APK signature verification, artifact uploads, and temporary signing-key cleanup. KVM enablement and Room instrumentation tests were skipped by the workflow; no Room/persistence code changed.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.355-vc357`, artifact ID `15023653977`, size 9,013,872 bytes. Room schema artifact: `irminsul-room-schema-v1-run-355`, artifact ID `15023174643`, size 985 bytes.
- No UI, persistence, source collector, external SDK, AI service, numerical score, or investment evaluator integration changed.
- Physical-device testing is not applicable to this domain-only milestone.
- M50 closure documentation commit `487a453ce8f67b526712a1f504e7b72d74553c30` passed CI #356: https://github.com/berao609/Irminsul-codex/actions/runs/45620921620.

### Boundaries and limitations

- M50 does not establish that the external character-knowledge catalogue is complete. NOT_ESTABLISHED is neutral and cannot be presented as a proven roster deficiency.
- Coverage does not imply team synergy, a valid four-character lineup, rotation quality, reaction performance, current meta strength, or investment value.
- No team-synergy claims, material-cost tables, affordability calculations, lineup UI, or materials calculator are implemented here.

### Next development checkpoint

Proceed to M51 only after M50's final CI evidence is recorded. M51 should validate material-cost sources and define versioned character, talent, and weapon upgrade-cost records before implementing calculator arithmetic or farming estimates.


## Milestone 051 - Materials Data Source Validation and Upgrade-Cost Contract

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Validate potential material-cost sources and define a provenance-bound, versioned domain contract for future character/talent/weapon calculations.  
**Status:** Closed; implementation and documentation CI #362-#366 passed.

### Objective

Establish a safe and testable source and data boundary for the planned Materials Calculator. No numerical cost dataset is bundled or treated as trusted by this milestone.

### Active Memory Recall and repository verification

Rechecked current `AGENTS.md`, `README.md`, `docs/SPECS.md`, `docs/PROGRESS_MILESTONES.md`, M46 `GameKnowledge.kt` source registry/permission contract, M46 `InvestmentShortlist.kt`, and M50 coverage model/assessor. Confirmed M50 documentation CI #356 passed for commit `487a453ce8f67b526712a1f504e7b72d74553c30`: https://github.com/berao609/Irminsul-codex/actions/runs/45620921620.

### Source validation findings and decisions

1. **HoYoverse Help Center / HoYoWiki**
   - References: https://support.hoyoverse.com/hc/en-us/articles/50333964005913-How-can-I-farm-materials-to-upgrade-my-weapons and https://www.hoyolab.com/article/1680746.
   - Official help guidance identifies weapon ascension materials, enemy drops, enhancement ores, Mora, and domain rotation.
   - HoYoLAB's official tools overview documents the Enhancement Progression Calculator and HoYoWiki material calendar.
   - No public structured material-cost API or third-party structured-data redistribution permission was established in this review.
   - Decision: canonical manual reference only, pending API/permission verification. No automated collection or republishing.

2. **genshin-db**
   - Reference: https://github.com/theBowja/genshin-db. Its README identifies Fandom Wiki and GenshinData as upstream data sources; GenshinData reference: https://github.com/potsh/GenshinData; Fandom licensing: https://www.fandom.com/licensing.
   - Technically useful structured catalog for characters, weapons, materials, talents, and other game data.
   - Repository advertises MIT licensing for the package, but README identifies upstream content from both Genshin Impact Fandom Wiki and GenshinData.
   - The package's software license does not independently establish redistribution rights for all underlying game-derived/upstream data. Fandom text is generally CC BY-SA unless otherwise specified; media assets have separate licenses. GenshinData README requests credit but provides no clear dataset license.
   - Decision: technical candidate for schema/reference evaluation only. Not approved for automated bulk ingestion or bundled cost-data redistribution.

3. **HoyoWhatpeon planner**
   - Reference: https://hoyowhatpeon.com/en/genshin/planner.
   - Public planner documents using a pinned genshin-db snapshot and explicitly describes excluded EXP categories and unsupported cases.
   - Decision: reference for calculator coverage and transparent limitation handling only, not an authorized data source.

4. **Community calculators and other databases**
   - Decision: reference-only until each publisher's provenance, terms/license, attribution, permitted access, retention/deletion, update cadence, and redistribution rights are independently reviewed.

### Implemented domain contract

1. Added `UpgradeCostKnowledge.kt`:
   - Explicit character/weapon subject types.
   - Separate character level, character ascension, talent level, weapon level, and weapon ascension tracks.
   - Stable subject/catalog ID, exact game version, from/to level range, optional explicit talent slot, item IDs and positive integer quantities.
   - Source references, retrieval time, optional validity window, review state and notes, positive revision, and supersession reference.
   - Structural/trust validator requiring approved source permission and explicit `material_cost_curation` in `allowedActions`.
   - Exact game-version and validity-window applicability check.
   - Candidate/unverified, missing, malformed, expired, mismatched, or unapproved data cannot enter trusted cost calculations.

2. Added `UpgradeCostRecordValidatorTest.kt` covering:
   - Valid verified records.
   - Link-only action rejection and unapproved-source rejection.
   - Candidate/unverified record rejection and missing review metadata.
   - Subject/track mismatch and talent-slot rules.
   - Empty, zero, negative, and duplicate cost entries.
   - Host mismatch and unknown source references.
   - Exact game-version matching and validity boundaries.
   - Revision and supersession validation.
   - Character, talent, and weapon record shapes.

3. Added SPECS section 3.18 documenting source decisions, trust gates, calculator boundaries, and explicit exclusions.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/model/UpgradeCostKnowledge.kt` (new)
- `app/src/test/java/com/irminsul/codex/domain/model/UpgradeCostRecordValidatorTest.kt` (new)
- `docs/SPECS.md`
- `docs/PROGRESS_MILESTONES.md`

### Implementation and specification commits

- `1e1ac4309d2dfe4fdde25daf458bb3efde98cfd0` - Add versioned upgrade-cost knowledge contract and validator.
- `aa352dab23e2c9534bb14afc59800257bc8d1d63` - Add upgrade-cost provenance and applicability tests.
- `d2bb28e472f4669345a802520e68c4a7e3bdb58f` - Specify M51 source decisions and upgrade-cost contract.

### Verification

- Implementation CI #357 and validator-test CI #358 passed; specification CI #359 passed; milestone-documentation CI #360 passed; canonical-source-reference CI #361 passed.
- Workflow runs: https://github.com/berao609/Irminsul-codex/actions/runs/45621399922, https://github.com/berao609/Irminsul-codex/actions/runs/45621448168, https://github.com/berao609/Irminsul-codex/actions/runs/45621508638, https://github.com/berao609/Irminsul-codex/actions/runs/45621544513, https://github.com/berao609/Irminsul-codex/actions/runs/45621647880.
- Unit tests, Debug APK build, Room schema verification, APK signature verification, artifact upload, and signing-key cleanup passed in the final implementation workflow; Room instrumentation tests were skipped because no persistence code changed.
- No physical-device test was applicable to this domain-only contract milestone.

### Boundaries and limitations

- No external source is approved yet for automated material-cost ingestion or bundled redistribution.
- The M48 account snapshot does not expose actual player material inventory. M52 resolves this by accepting explicit manual inventory input; it is not account-synced or independently verified.
- Artifact enhancement/feeding costs are excluded from this contract.
- Domain availability calendars, drop yields, resin estimates, and farming-time forecasts are not implemented.
- M51 does not calculate costs. It defines the trusted data required before a calculator can safely do so.

### Next development checkpoint

M51 is closed. M52 implements deterministic aggregation and manual inventory subtraction; see the M52 entry below.


## Milestone 052 - Deterministic Upgrade-Cost Aggregation and Manual Inventory

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Calculate complete upgrade requirements from M51 trusted cost segments and subtract manually entered material inventory.  
**Status:** Implemented; final CI verification pending.

### Objective and user decision

The user confirmed that in-game inventory access is not required. The Materials Calculator must let the player enter owned material quantities manually. The app must distinguish an explicit zero from a material whose inventory has not been entered.

### Active Memory Recall and repository verification

Rechecked current `AGENTS.md`, `README.md`, `docs/SPECS.md`, `docs/PROGRESS_MILESTONES.md`, M51 `UpgradeCostKnowledge.kt` and validator tests, M48 `OwnedCharacterInvestmentFacts.kt`, and M46 `GameKnowledge.kt` source registry. Confirmed M51 final CI #357-#361 all passed:
- #357 implementation: https://github.com/berao609/Irminsul-codex/actions/runs/45621399922
- #358 validator tests: https://github.com/berao609/Irminsul-codex/actions/runs/45621448168
- #359 specification: https://github.com/berao609/Irminsul-codex/actions/runs/45621508638
- #360 milestone documentation: https://github.com/berao609/Irminsul-codex/actions/runs/45621544513
- #361 canonical source references: https://github.com/berao609/Irminsul-codex/actions/runs/45621647880

### Implemented domain calculation

1. Added `UpgradeCostCalculator.kt`:
   - Explicit request identity: character/weapon subject, subject ID, upgrade track, level range, exact game version, and talent slot where applicable.
   - Selects only matching cost records and revalidates M51 source permission, review, and validity gates at calculation time.
   - Requires exact contiguous segment coverage from requested start to requested target.
   - Aggregates by stable material item ID with checked integer addition.
   - Preserves the record IDs used by the result.
   - Returns explicit statuses for invalid request, incomplete cost data, invalid/untrusted data, overlapping segments, and integer overflow.
   - Never fills missing segments with zero or falls back to another game version.

2. Added manual inventory input:
   - `ManualMaterialInventory` stores user-entered item quantities and an update timestamp.
   - Explicit zero is valid and distinct from not entered.
   - Blank IDs, negative quantities, and duplicate IDs are invalid.
   - Valid entered inventory produces `max(required - owned, 0)` remaining quantities.
   - Items not entered retain unknown owned and remaining quantities (null), rather than being treated as zero.
   - Invalid inventory does not corrupt verified required totals, but owned/remaining values are withheld until corrected.
   - Inventory is explicitly manual, not account-retrieved or verified.

3. Added `UpgradeCostCalculatorTest.kt` covering:
   - Multi-segment aggregation and per-item totals.
   - Missing segments and overlap/duplicate range rejection.
   - Unverified and expired records.
   - Exact version, subject, and track matching.
   - Manual inventory not-entered versus explicit zero.
   - Remaining quantities and owned quantities exceeding requirements.
   - Invalid manual inventory.
   - Integer overflow.
   - Invalid requests and explicit talent-slot requirements.

### Documentation

- SPECS section 3.19 defines the calculation and manual inventory contract.
- README roadmap now identifies a Materials Calculator with manual inventory input as planned.
- M51 verification record corrected to reflect successful CI #357-#361.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/analysis/UpgradeCostCalculator.kt` (new)
- `app/src/test/java/com/irminsul/codex/domain/analysis/UpgradeCostCalculatorTest.kt` (new)
- `docs/SPECS.md`
- `README.md`
- `docs/PROGRESS_MILESTONES.md`

### Implementation and documentation commits

- `79bbb5edb1eab5484494db8302b9f0645961bddd` - Add deterministic upgrade-cost calculator.
- `1d9dc00e854e9bdbc29376a92f21e1e38c7651dc` - Test aggregation and manual inventory.
- `5fc524fda77db6e04ce8b88fc9486e1d5c16c72c` - Define M52 aggregation and manual inventory in SPECS.
- `86ffd6f7a278390cd12b37ad48e4619bc476f11a` - Update README roadmap.

### Verification

- Implementation CI #362 and calculator-test CI #363 passed; SPECS CI #364, README CI #365, and milestone documentation CI #366 passed.
- Workflow runs: https://github.com/berao609/Irminsul-codex/actions/runs/45622796686, https://github.com/berao609/Irminsul-codex/actions/runs/45622898477, https://github.com/berao609/Irminsul-codex/actions/runs/45622990001, https://github.com/berao609/Irminsul-codex/actions/runs/45623005045, https://github.com/berao609/Irminsul-codex/actions/runs/45623159208.
- CI #362 Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.362-vc364`, artifact ID `15025597510`, size 9,033,548 bytes. CI #363 Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.363-vc365`, artifact ID `15026221996`, size 9,033,558 bytes. Room schema artifacts were also uploaded.
- Unit tests, Debug APK build, Room schema verification, APK signature verification, artifact upload, and temporary signing-key cleanup passed. Room instrumentation tests were skipped because no persistence code changed.
- No physical-device test was applicable to this domain-only calculation milestone.

### Boundaries and next checkpoint

- The calculator currently has no approved production cost dataset. It is ready to calculate only when verified, permitted M51 records are supplied.
- Manual inventory in the verified calculation engine is a domain input. M53 adds a separate manual planning UI and local preference storage, but does not claim user-entered requirements are verified.
- Artifact enhancement/feeding costs, farming calendars, drop yields, resin, and farming-time estimates remain excluded.
- M53 adds the user-facing manual Materials Planner; automatic verified cost population remains pending source approval.


## Milestone 053 - Manual Materials Planner UI

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Add a usable local Materials Planner while no redistribution-approved production cost catalogue is available.  
**Status:** Implemented and CI-verified; user reported testing the final #376 APK, but did not explicitly state pass/fail.

### Objective and product boundary

M52 delivered the deterministic engine for verified, versioned cost records. M51 source review did not approve any source for automated ingestion or bundled cost-data redistribution. Rather than inventing costs or blocking the feature entirely, M53 adds a clearly labeled manual planning mode.

The user can select an observed owned character or an equipped weapon, set a level range, enter material requirements manually, enter owned quantities, and see remaining quantities. Manual requirements are not represented as trusted game data.

### Active Memory Recall and repository verification

Rechecked current `AGENTS.md`, `README.md`, `docs/SPECS.md`, `docs/PROGRESS_MILESTONES.md`, current `MainActivity.kt`, M52 `UpgradeCostCalculator.kt`, M51 `UpgradeCostKnowledge.kt`, and account snapshot character/weapon models. Confirmed M52 CI #362-#366 all passed.

### Implemented

1. Added a dedicated **Materials** bottom-navigation destination.
2. Added target selection:
   - Characters from the retrieved roster.
   - Weapons observed as equipped on retrieved characters.
   - Custom target for an item not present in the retrieved roster.
   - Current level prefilled when observed; current and target levels remain editable.
3. Added manual material rows:
   - Material name.
   - Required quantity.
   - Owned quantity.
   - Remaining quantity calculated from the two entered quantities.
   - Add and remove row controls.
4. Added explicit states:
   - Blank owned quantity means inventory not entered and remaining stays unknown.
   - Entered zero means the player owns none.
   - Invalid quantities do not produce a result.
   - Owned quantities above requirements yield zero remaining, never a negative number.
5. Persisted material rows locally under target-specific preference keys, so switching characters/weapons does not mix their plans. The selected target and level range are also stored locally. Custom-target editing keeps its active plan stable.
6. Added `ManualMaterialPlanCalculator.kt` and tests for valid subtraction, explicit zero versus not-entered, inventory above requirement, missing required amount, negative/malformed values, and numeric overflow.
7. The screen clearly states that required quantities are manually entered and not game-verified because no redistribution-approved production cost catalogue is connected.

### Files changed

- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/domain/analysis/ManualMaterialPlanCalculator.kt` (new)
- `app/src/test/java/com/irminsul/codex/domain/analysis/ManualMaterialPlanCalculatorTest.kt` (new)
- `README.md`
- `docs/SPECS.md`
- `docs/PROGRESS_MILESTONES.md`

### Implementation and documentation commits

- `030fc947d8dc73ba41ef2a68ccc43fc553bb026f` - Add manual material plan calculation.
- `f78c1ff3d1f412e9f5d46f0012c20a3b3eaaa345` - Test manual material plan quantities.
- `a99689bfe0845379393f6226dec82f15aadb5f7f` - Add manual Materials Planner screen (initial compile attempt).
- `4e0930342f075408219c04e34eb4e42f81413b00` - Correct Compose annotation placement.
- `47af2f99d28a17e2a0bfefe3b63dbe8799d8fbde` - Isolate saved material rows by selected target.
- `cad00cd42d5e2dea2b881b2c5390e16834304181` - Keep custom-target plan storage stable while editing its name.
- `0f18d25299bb7295b04d12f57911e79e4ee65639` - Describe manual Materials Planner in README.
- `78a8c3d62f52ae8af234c0d02e999d62491805e7` - Specify M53 UI behavior and trust boundary.
- `b8f9ee111817418861e2749df3e80c2ce9eda629` - Specify target-isolated local material plans.

### Verification

- Domain calculator CI #367 passed: https://github.com/berao609/Irminsul-codex/actions/runs/45624659985.
- Domain calculator test CI #368 passed: https://github.com/berao609/Irminsul-codex/actions/runs/45624666219.
- Initial UI CI #369 failed because the `@Composable` annotation was accidentally attached to a data class and the existing CharacterShowcaseSection annotation was displaced. README/SPECS/progress CI #370-#372 also failed because they built that same intermediate source state.
- Corrected annotation placement in commit `4e0930342f075408219c04e34eb4e42f81413b00`.
- Combined CI #373 passed after the annotation correction: https://github.com/berao609/Irminsul-codex/actions/runs/45625322427.
- CI #374 passed the milestone-documentation update: https://github.com/berao609/Irminsul-codex/actions/runs/45625719165.
- CI #375 passed the target-specific plan persistence refinement: https://github.com/berao609/Irminsul-codex/actions/runs/45625825955.
- CI #376 passed the final custom-target storage correction: https://github.com/berao609/Irminsul-codex/actions/runs/45625965817.
- CI #376 passed unit tests, Debug APK build, Room schema verification, APK signature verification, artifact upload, and temporary signing-key cleanup. KVM and Room instrumentation tests were skipped because no Room/persistence schema code changed.
- Final Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.376-vc378`, artifact ID `15026489813`, size 9,064,766 bytes. Room schema artifact ID `15028935149`, size 985 bytes.
- Final SPECS target-isolation update CI #377 passed: https://github.com/berao609/Irminsul-codex/actions/runs/45626388663.
- The final progress-document update CI #378 passed: https://github.com/berao609/Irminsul-codex/actions/runs/45626563675.
- Physical-device UI and persistence testing: user reported that the final #376 APK was tested on 2026-11-25. The test outcome was not explicitly stated, so device verification is recorded as performed but not yet confirmed successful.

### Boundaries and next checkpoint

- This is a manual planning interface, not yet a fully automatic game-cost calculator.
- M52's verified-cost engine is not wired into this UI until an approved, in-date, version-matched cost catalogue is available.
- The user can manually enter required quantities from a source they trust and enter owned quantities without any in-game inventory integration.
- Next planned feature remains the four-character Lineup Simulator foundation. Do not add combat simulation or unverified team synergy claims.

## Milestone 054 - Streamlined Navigation and Integrated AI Entry Point

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Simplify the primary navigation, relocate Materials Planner into Visuals, and introduce a centered AI action with a chat-style bottom sheet.  
**Status:** Closed. Implementation, documentation CI, and physical-device verification passed.

### Active Memory Recall and repository verification

Rechecked `AGENTS.md`, `README.md`, `docs/SPECS.md`, `docs/PROGRESS_MILESTONES.md`, `MainActivity.kt`, `app/build.gradle.kts`, and `gradle/libs.versions.toml`. Confirmed M53 build CI #376 passed. The user reported testing #376; the result was not explicitly stated.

### Approved product decisions

1. Keep only Home, Visuals, and Account as bottom-navigation destinations.
2. Keep Account accessible from the top app-bar shortcut and the rightmost bottom-navigation item. Account contains appearance settings, HoYoLAB account management, and optional JSON export.
3. Put a raised AI sparkle action at the center of the bottom bar. Tapping it opens a modal bottom-sheet chat interface.
4. Keep the AI surface explicitly inactive until a provider is connected. Do not imply that message sending or AI responses work yet.
5. Move Materials Planner into Visuals as a collapsible section. Preserve its existing target-specific local storage and manual/unverified quantity semantics.
6. Replace text-glyph navigation with consistent vector icons: door/entrance for Home, analytics chart for Visuals, sparkle for AI, and profile for Account.

### Implemented

- Added the Compose Material extended vector-icon dependency.
- Replaced the five-item text-glyph navigation with Home, Visuals, and Account plus a raised center AI action.
- Added a modal bottom-sheet AI interface with a larger conversation area, a clear provider-not-connected state, and the existing cookie privacy statement.
- Removed the dedicated Materials and AI Chat navigation destinations.
- Integrated Materials Planner into Visuals behind a collapsible section; planner calculation and persistence logic are unchanged.
- Kept Account and settings accessible from the top app bar and the Account navigation item.
- Renamed the Account page heading and updated README and product specification to match the new navigation model.

### Files changed

- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `README.md`
- `docs/SPECS.md`
- `docs/PROGRESS_MILESTONES.md`

### Implementation commits

- `6a2b1d5cb47f3b5558475e97333595ab2a4753da` - Add Compose vector icon catalog alias.
- `512fdd61486279b1ebfa800a80b6d14f51c9c74a` - Add Material vector icon dependency.
- `271a2f96a7b3fa1b435814bf04c6fc299bf744d9` - Implement navigation redesign, AI sheet, and integrated Materials Planner.
- `66422a272fc26b17b1d68cc79021bc1e14067e5f` - Add required Material 3 experimental API opt-in.
- `a73dca4fd0ec41004d6f53a162d896b4b8085100` - Update README.
- `275ee7525260db41910301130f108d7315d913f4` - Update product specification.
- `0cac21d05a6e1bf28dbdd89ac4c0fa16f0622703` - Refine Account page heading and AI chat conversation area.
- `72434f5b8745905d36362748d2e6d149aeef5eef` - Switch navigation symbols to outlined vector icons (initial chart icon selection did not compile).
- `505b9724814429ca22188bd2a31e6bab6d99ae00` - Use the supported outlined InsertChart icon.

### Verification

- CI #381 and #382 passed the icon catalog and dependency changes.
- Initial navigation CI #383 failed at Kotlin compilation because the new Material 3 bottom-sheet API required an experimental API opt-in. This was corrected in commit `66422a272fc26b17b1d68cc79021bc1e14067e5f`.
- Final implementation CI #384 passed: https://github.com/berao609/Irminsul-codex/actions/runs/45630588460.
- CI #385 README, #386 SPECS, #387 progress documentation, #388 follow-up progress documentation, and #390 final M54 documentation all passed.
- CI #389 passed the initial UI refinement: https://github.com/berao609/Irminsul-codex/actions/runs/45631311189.
- CI #391 failed because `Icons.Outlined.BarChart` is not present in the installed Material icon set. The chart was changed to the supported `Icons.Outlined.InsertChart` symbol in commit `505b9724814429ca22188bd2a31e6bab6d99ae00`.
- Final outlined-icon implementation CI #392 passed: https://github.com/berao609/Irminsul-codex/actions/runs/45632185584.
- CI #392 passed unit tests, Debug APK build, Room schema verification, APK signature verification, artifact upload, and temporary signing-key cleanup. Room instrumentation tests were skipped because no Room/persistence schema changed.
- Final Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.392-vc394`, artifact ID `15030547596`, size 15,866,909 bytes, SHA-256 `77afd91035f15442a4dd9ab3f9d957baf2d0f41486cfa9d908989d2f9698d28d`. Room schema artifact ID `15031916116`, size 985 bytes.
- User installed and tested the final CI #392 APK and confirmed success on 2026-11-25. The user accepted the current navigation and AI-sheet design, with further UI polish deferred until their separate UI Design Logic document is ready.

### Boundaries and next checkpoint

- The AI bottom sheet is only a UI foundation. It does not send messages or call an AI provider.
- The Materials Planner remains manual and does not gain a trusted material-cost dataset.
- The next planned product feature after this navigation milestone is the four-character Lineup Simulator foundation.

## Milestone 055 - Four-Character Lineup Simulator Foundation

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Add a collapsible four-character lineup sandbox inside Visuals, using the observed owned roster and preserving uncertainty.  
**Status:** Closed. Implementation CI #402 and documentation CI #403 passed; user confirmed physical-device testing.

### Active Memory Recall and repository verification

Rechecked current `AGENTS.md`, `README.md`, `docs/SPECS.md`, `docs/PROGRESS_MILESTONES.md`, `MainActivity.kt`, the account snapshot character model, M48 owned-character fact mapper, M49 capability knowledge join, and M50 role/capability coverage contract. Confirmed M54 documentation CI #393 passed and the user confirmed successful physical-device testing of APK #392.

### Product decisions

1. Add the Lineup Simulator as a collapsible section inside Visuals, not a new navigation destination.
2. Allow the user to select up to four distinct characters from the observed owned roster.
3. Preserve selection order as lineup slot order. Tapping a selected character removes it; a fifth character cannot be added while all four slots are occupied.
4. Display the selected character portraits, names, and observed element labels/counts.
5. Explicitly disclose stale and partial roster data.
6. Do not claim role coverage, reaction quality, team synergy, rotation quality, encounter suitability, or combat performance without verified, permitted, current evidence.
7. Keep selection temporary in this first foundation. No saved team profiles, combat simulation, AI, backend, or source collector.

### Implemented

- Added pure `LineupSelectionRules` with a four-character maximum, unique-ID normalization, stable selection order, add/remove toggle behavior, and no-op behavior when full.
- Added unit tests for add/order, duplicate selection, removal, maximum team size, and malformed pre-existing selections.
- Added a collapsible Lineup Simulator section in Visuals.
- Added four lineup slots and an owned-roster picker with portrait fallbacks and selected-state feedback.
- Added descriptive element counts without inferring reaction quality or synergy.
- Added stale-roster and partial-roster warnings.
- Existing account analysis, character showcase, and Materials Planner behavior remain unchanged.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/analysis/LineupSelectionRules.kt` (new)
- `app/src/test/java/com/irminsul/codex/domain/analysis/LineupSelectionRulesTest.kt` (new)
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `README.md`
- `docs/SPECS.md`
- `docs/PROGRESS_MILESTONES.md`

### Implementation commits

- `97710b3ec81be6455d644aad988ff1a7f37062bd` - Add deterministic lineup selection rules.
- `bd0ec28217d6ad03b7550231d784bcae7e25f44d` - Test four-character lineup selection.
- `a48d3c859ae01c5aab57bb8fca29e4c0cd83a5ad` - Add four-character lineup simulator sandbox.
- `5adc7f9f69ea89c7601cb600eb84ff5a6b10f65e` - Preserve lineup roster freshness context.
- `359ff572b38277b2850814f7cd53e707a108413c` - Correct the lineup toggle test to match the intended remove-on-toggle behavior.
- `f7b9c8e2d6ad0241e6c72e61a9629411279300f3` - Document the Lineup Simulator in README.
- `5b2a6a86256d42e3e66a44be19d79018d41a154d` - Specify M55 lineup selection and evidence boundaries.

### Verification

- M54 closure documentation CI #394 passed: https://github.com/berao609/Irminsul-codex/actions/runs/45634311908.
- M55 selection-rules model CI #395 passed: https://github.com/berao609/Irminsul-codex/actions/runs/45634496973.
- Initial test CI #396 failed because the test expected toggling an already-selected character to leave it selected. The intended toggle behavior is to remove it; the test was corrected in commit `359ff572b38277b2850814f7cd53e707a108413c`.
- CI #397 and #398 (initial UI/freshness changes), #399 (README), #400 (SPECS), and #401 (milestone documentation) also failed while building the same intermediate test state. These are not final source failures.
- Final combined implementation CI #402 passed: https://github.com/berao609/Irminsul-codex/actions/runs/45634948651.
- CI #402 passed unit tests, Debug APK build, Room schema verification, APK signature verification, artifact upload, and temporary signing-key cleanup. Room instrumentation tests were skipped because no Room/persistence schema changed.
- Final Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.402-vc404`, artifact ID `15032876933`, size 15,880,772 bytes, SHA-256 `cc58b4ad3d83955b421e0ba612181923bf3776027b75bbda82489e5507e0bf72`. Room schema artifact ID `15032652360`, size 985 bytes.
- Final M55 progress-document CI #403 passed: https://github.com/berao609/Irminsul-codex/actions/runs/45635363391.
- User confirmed the #403 milestone build was device-tested. M55 is closed.

### Boundaries and next checkpoint

- This milestone is a team selection sandbox, not a team recommendation engine or combat simulator.
- Element counts are descriptive only. Do not infer reactions or team quality from element co-occurrence.
- M50 role/capability coverage remains roster-level evidence, not a team synergy score.
- Next milestone should add evidence-backed team synergy and improvement suggestions only when verified team interaction claims and required source permissions exist.


## Milestone 056 - Restore Saved Account Analysis on Startup

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Restore the latest locally saved account snapshot and rebuild its deterministic analysis when the app opens, without repeating account JSON retrieval.  
**Status:** Implementation committed; CI verification passed; physical-device verification passed, with active-save cancellation retained as a follow-up.

### Active Memory Recall and repository verification

Rechecked `AGENTS.md`, `README.md`, `docs/SPECS.md`, `docs/PROGRESS_MILESTONES.md`, `MainActivity.kt`, `AccountViewModel.kt`, the Room snapshot DAO/store, and ViewModel tests. Confirmed M55 documentation CI #403 succeeded and the user confirmed device testing.

### Product decisions

1. Restore the latest successful Room snapshot for the signed-in UID during app startup.
2. Rebuild the deterministic account-intelligence overview locally from that snapshot. Do not call the account-export endpoint during restoration.
3. Keep Daily Note synchronization independent from account snapshot restoration.
4. Keep account refresh explicit. A successful refresh stores a new snapshot; a failed refresh must not erase the cached snapshot or existing overview.
5. If no snapshot exists for the current UID, retain the existing Analyze Account empty state.
6. Do not persist credentials in Room or add credentials to the snapshot-store contract.

### Implemented

- Added `getLatest(uid)` to the snapshot-store boundary.
- Added a Room query that selects the newest snapshot for the UID.
- Added `RoomAccountSnapshotStore.getLatest` to map the persisted raw JSON back into the normalized snapshot.
- Added `AccountViewModel.restoreLatestSnapshot`, which restores the cached snapshot and rebuilds deterministic analysis without calling the network repository.
- Added regression tests that fail if cache restoration attempts a network retrieval and verify that a failed explicit refresh preserves the previously restored snapshot and overview.
- Triggered cache restoration when the signed-in UID becomes available.
- Added a restoring state to the Analyze/Refresh control, disabled the control while restoration is in progress, and hid the empty-analysis placeholder during restoration.
- Guarded against overlapping restoration/refresh operations and rejected a cached record whose UID does not match the signed-in account.
- Preserved existing snapshot and overview while an explicit refresh is running or fails.
- Closed M55 after CI #403 and user device verification.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/repository/AccountSnapshotStore.kt`
- `app/src/main/java/com/irminsul/codex/data/local/dao/AccountSnapshotDao.kt`
- `app/src/main/java/com/irminsul/codex/data/local/repository/RoomAccountSnapshotStore.kt`
- `app/src/main/java/com/irminsul/codex/presentation/viewmodel/AccountViewModel.kt`
- `app/src/test/java/com/irminsul/codex/presentation/viewmodel/AccountViewModelTest.kt`
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `README.md`
- `docs/SPECS.md`
- `docs/PROGRESS_MILESTONES.md`

### Implementation commits

- `91081197d2810e4ee2ae3143682944730d3487d5` - Extend the snapshot-store boundary with latest-snapshot lookup.
- `918589beebb1273ae50442e5716c4a69e79a7a43` - Query the newest saved snapshot by UID.
- `8954274fc525b3eb743fcaeaff56331638aae5c1` - Restore normalized exports from Room.
- `9bd7ff835cc5885a6862be02052e706477769e84` - Restore cached account analysis in the ViewModel.
- `e490ee9c58b11d43f84147f44c97cbd5b87134c5` - Test restoration without a network call.
- `6235fd3e53e45fbfdfd05b90d815466ed221e540` - Restore the cached snapshot when the signed-in UID is available.
- `347255df4712b0e23fdb8eef4f2cfb07c8e84807` - Show the saved-analysis restoration state in the UI.
- `cf16c0eb874acbf20d025b5d031eccbb9268ddc5` - Hide the empty analysis state while restoring cache.
- `eae75a3668b38e3863a68375d50f0c0e89a28364` - Guard restoration against account and refresh races.
- `0cb6bfcd25a46e18fdc647f360cac2a6ac17d588` - Test cached analysis survives a failed refresh.

### Verification

- M55 final documentation CI #403 passed: https://github.com/berao609/Irminsul-codex/actions/runs/45635363391. User confirmed device testing of the #403 milestone build.
- Intermediate CI #404-#414 exposed incomplete states between sequential interface, Room, ViewModel, test, UI, and documentation commits. #404-#405 failed while the interface and Room implementation were not yet both present; #406-#410 failed because the test double had not yet implemented the new store method; #411-#414 were also built from those intermediate states.
- CI #415 passed unit tests on the then-current implementation; APK build was still running when recorded.
- Final combined code CI #418 passed: https://github.com/berao609/Irminsul-codex/actions/runs/45643514735. Physical-device verification of cached restoration remains pending.
- Physical-device verification of cached restoration is pending.

### Boundaries and next checkpoint

- Restoration reads the newest snapshot for the UID; it does not fetch or refresh the full account JSON.
- Daily Note remains a separate network request and is not part of the cached character/account analysis.
- Material Calculator redesign remains a separate active task. The current manual-row planner has no approved production material catalogue, so it must not invent costs or material identities.


## Milestone 057 - CursorWindow-Safe Account Snapshot Persistence

**Date recorded:** 2026-11-25  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Correct M56 startup restoration failure caused by retrieving multi-megabyte raw JSON from one SQLite row.  
**Status:** Implementation committed; CI verification pending; physical-device verification pending.

### Active Memory Recall and root-cause verification

Rechecked repository instructions, README, SPECS, progress history, the Room database, snapshot entity, DAO, snapshot store, and ViewModel. The reported Android error was:

`Could not restore the saved account analysis: Row too big to fit into CursorWindow`.

Confirmed that M56 stored the complete raw export in `AccountSnapshotEntity.rawJson` and its latest-snapshot query selected the full entity. The approximately 4 MB JSON value exceeded the available CursorWindow capacity.

### Implemented

- Added a dedicated `account_snapshot_json_chunks` table keyed by snapshot ID and ordered chunk index.
- Limited each stored JSON text value to 32 KiB.
- Updated new snapshot writes to store JSON chunks inside the same Room transaction as snapshot metadata.
- Updated latest-snapshot and history restoration to reassemble chunks in their original order.
- Added Room database version 1-to-2 migration.
- The migration reads only snapshot IDs and JSON lengths through CursorWindow, then uses SQLite `substr()` to copy bounded slices into chunk rows. It clears the legacy large `rawJson` value only after its chunks have been inserted.
- Kept a compatibility fallback for a legacy raw JSON value when no chunks are present.
- Added unit coverage for large-content splitting, exact reassembly, and empty content.
- Updated README and SPECS with the bounded-storage contract.

### Files changed

- `app/src/main/java/com/irminsul/codex/data/local/entity/AccountSnapshotJsonChunkEntity.kt` (new)
- `app/src/main/java/com/irminsul/codex/data/local/SnapshotJsonChunks.kt` (new)
- `app/src/test/java/com/irminsul/codex/data/local/SnapshotJsonChunksTest.kt` (new)
- `app/src/main/java/com/irminsul/codex/data/local/dao/AccountSnapshotDao.kt`
- `app/src/main/java/com/irminsul/codex/data/local/IrminsulDatabase.kt`
- `app/src/main/java/com/irminsul/codex/data/local/repository/RoomAccountSnapshotStore.kt`
- `README.md`
- `docs/SPECS.md`
- `docs/PROGRESS_MILESTONES.md`

### Implementation commits

- `29ca3e91c6eb652295502705041d115494ba8484` - Add chunked account snapshot JSON entity.
- `8b8fe9d21dea4820aa029705504dc17862b6ec35` - Add bounded JSON chunking helper.
- `e0f894caf16fea293b8bd2de8f95197f675e1541` - Test bounded JSON chunking and reassembly.
- `341f5845242c60e5b147f32c51cc19f1f921b0f0` - Add chunk access to snapshot DAO.
- `a5df0f2e3279ae0af213ecb5a27ff340d5fe62ef` - Add version 1-to-2 chunk-storage migration.
- `fca2de71b6be0bbadaead992968a6b2203abdcc7` - Persist and restore account JSON through bounded chunks.
- `7820bd26d2e801b1e110af869a83a0f3b5555588` - Preserve Unicode surrogate pairs across chunk boundaries.
- `4628dc755f33662c9d9a33b4b2664309c82fbd95` - Test Unicode-safe chunk boundaries.

### Verification

- The source changes above are committed to `main`.
- CI verification for the combined chunk-storage implementation is pending.
- The existing M56 CI #418 passed, but it predates this CursorWindow correction.
- Existing saved data migration and startup restoration have not yet been verified on the user's physical device.

### Boundaries and next actions

1. Verify Room schema generation, migration behavior, unit tests, and Debug APK build in CI.
2. Install the resulting APK over the existing installation without clearing app data.
3. Confirm the app restores the existing account analysis after a full close and reopen, without downloading the account JSON again.
4. Keep M56/M57 open until migration and physical-device restoration are confirmed.

### CI workflow correction (2026-11-25)

- CI run #432 failed at the Room schema snapshot check because the workflow still required database schema version 1 after the database had been upgraded to version 2.
- Confirmed `IrminsulDatabase` declares `version = 2` and KSP exports Room schemas to `app/schemas`.
- Updated `.github/workflows/build.yml` to verify and upload `com.irminsul.codex.data.local.IrminsulDatabase/2.json`, with the artifact named for schema version 2.
- Fix commit: `50c5f981b14e9940599a80f5f981c7aee188762a`.
- The corrected workflow has not yet been verified by a completed CI run. The next CI result must confirm schema generation, instrumentation tests, APK build, and signature verification.


## Milestone 058 - Deterministic Materials Planner Foundation

**Date recorded:** 2026-11-26  
**Development phase:** Phase 3 - Account Intelligence Experience  
**Milestone scope:** Replace manually authored material requirements with a verified, versioned game-data catalogue and deterministic progression calculations.  
**Status:** Initial calculation engine and unit tests committed; catalogue ingestion, UI integration, CI verification, and device verification pending.

### Active Memory Recall and source investigation

Rechecked `AGENTS.md`, `README.md`, `docs/SPECS.md`, the current progress history, the existing manual planner calculator and tests, and the planner section in `MainActivity.kt`.

Confirmed the current planner still stores manually entered material rows in SharedPreferences. Its existing calculator only subtracts manually entered owned quantities from manually entered required quantities. It does not derive requirements from character, talent, or weapon progression.

Inspected the upstream `theBowja/genshin-db` repository documentation and representative English JSON records. The inspected data schema includes:

- Character ascension cost records keyed by ascension stage, with stable item IDs, names, and quantities.
- Talent cost records keyed by talent level.
- Weapon ascension cost records keyed by ascension stage.
- Material metadata including names, categories, source descriptions, domain names, and weekday availability.
- The upstream project declares an MIT license and explicitly warns that data formats can change between versions.

The upstream data is a promising catalogue source, not yet an approved imported production catalogue. Coverage, version pinning, attribution, image handling, crafting recipes, character/weapon EXP curves, Traveler exceptions, and source-data integrity still require validation.

### Implemented

- Added a normalized domain model for one material cost per upgrade step.
- Added a deterministic progression material calculator that aggregates costs across every level step in a requested current-to-target range by stable material ID.
- Requires an explicit cost record for every intermediate target level. Missing records return `MISSING_COST_DATA`; they are never interpreted as free upgrades.
- Distinguishes unknown inventory from explicitly entered zero inventory.
- Aggregates repeated material IDs, rejects conflicting names and malformed quantities, validates level ranges and inventory quantities, and detects Long overflow.
- Added unit tests for aggregation, missing cost steps, inventory semantics, invalid ranges, malformed data, duplicate steps, conflicting material names, and overflow.
- The engine is catalogue-agnostic. It does not embed unverified game costs or make network requests.

### Files changed

- `app/src/main/java/com/irminsul/codex/domain/analysis/ProgressionMaterialCalculator.kt` (new)
- `app/src/test/java/com/irminsul/codex/domain/analysis/ProgressionMaterialCalculatorTest.kt` (new)

### Implementation commits

- `d29a3d956ede8d838d932f9d83c85de7a3ee49f6` - Add deterministic progression material aggregation.
- `028381f236ec338dc360afbdcd522098c486aa28` - Test progression material aggregation and validation.

### Verification

- Both files were created on the repository default branch through the GitHub contents API.
- CI status for these commits has not yet been observed.
- Unit tests have not been executed locally in this environment.
- No APK has been built or device-tested for M58.
- Existing M53 manual planner data has not been migrated or modified.

### Next actions

1. Validate and pin an upstream catalogue revision. Preserve source IDs and raw source records during normalization.
2. Audit character ascension, all three talent tracks, weapon ascension, character EXP, weapon EXP, crafting recipes, and Traveler-specific data.
3. Add import/parser validation and fixture tests before bundling any catalogue data.
4. Adapt the planner UI to separate character levels, three talent tracks, and customizable weapon targets.
5. Replace manual material-name/required-quantity entry with catalogue-derived rows while retaining owned-quantity entry.
6. Preserve and migrate existing manual planner rows without silently discarding user data.
7. Verify combined plans, farming-day grouping, local persistence, CI, APK installation, and physical-device behavior before closing M58.


### M58 implementation continuation - catalogue cost parsing

After implementing the aggregation engine, the model was refined to use explicit upgrade milestone thresholds rather than assuming every integer character level has a cost row. This is necessary because character and weapon ascension costs are sparse milestones (20, 40, 50, 60, 70, 80), while talent costs use levels 2 through 10.

Additional implementation:

- Added `GenshinDbCostParser` to normalize the upstream `costs` JSON object for character ascension, weapon ascension, and talent progression.
- Missing stage keys return `MISSING_STAGE`; they are not interpreted as free upgrades.
- Explicit empty arrays remain valid empty cost stages.
- Malformed arrays or material records return `INVALID_DATA`.
- Added parser tests for character/weapon ascension thresholds, talent levels, missing stages, explicit empty stages, and malformed records.

Additional files:

- `app/src/main/java/com/irminsul/codex/data/catalogue/GenshinDbCostParser.kt` (new)
- `app/src/test/java/com/irminsul/codex/data/catalogue/GenshinDbCostParserTest.kt` (new)

Additional commits:

- `48bfa3b9a68277317a4c30c0d126358f11b61def` - Model sparse progression cost milestones.
- `004eb14c2711709d5296d743c002b87c78088380` - Test sparse progression milestones.
- `3f56580f2656eb70057ad3f53d40daac6dd54b27` - Parse Genshin DB upgrade cost records.
- `6820f32ae81e6be2bef916e70969608282c0358a` - Test Genshin DB cost parsing.
- The calculator contract comment was clarified in a follow-up commit.

Verification remains pending: no local Gradle execution is available in this session, and CI status checks have not yet reported a result for the M58 commits.


### M58 architecture correction - reuse the existing verified cost registry

The first M58 code attempt introduced a second progression calculator and duplicate domain result types. CI #435 and #436 exposed the conflict: `ProgressionMaterialCalculator.kt` redeclared `CalculatedMaterialRequirement`, which is already owned by the existing `UpgradeCostCalculator.kt` architecture. The same build also reported constructor-property mismatches caused by that duplicate type resolution.

Re-audited the full existing cost model before proceeding. The repository already contains:

- `UpgradeCostRecord`, `UpgradeCostItem`, `UpgradeCostTrack`, and `UpgradeCostSubjectType`.
- `KnowledgeSourceRegistryEntry` and `KnowledgeSourceReference`.
- `UpgradeCostRecordValidator`, which requires explicit source registry permission, allowed curation action, source-host consistency, verification state, review timestamp, and review notes.
- `UpgradeCostCalculator`, which only aggregates exact subject/track/game-version records, checks validity and source permission, requires contiguous segments, rejects missing or overlapping cost data, and detects overflow.
- `ManualMaterialInventory`, which already distinguishes not-entered inventory from explicit zero and rejects invalid/duplicate inventory entries.

The duplicate `ProgressionMaterialCalculator.kt` and its test were deleted. No duplicate calculator or duplicate domain result model should be added. Future catalogue ingestion must feed the existing cost-record governance path rather than bypass it.

The Genshin DB parser was revised to return `GenshinDbParsedCostSegment` candidate data using the existing `UpgradeCostItem` model. It maps ascension stages to contiguous ranges (1-20, 20-40, 40-50, 50-60, 60-70, 70-80) and talent levels to (1-2) through (9-10). Parsed candidates are not trusted `UpgradeCostRecord` objects and cannot enter the calculator until the source registry and human verification requirements are satisfied. Explicit empty source arrays are preserved in the parsed candidate for review; they are not silently treated as verified zero-cost records.

### Corrected M58 files

- `app/src/main/java/com/irminsul/codex/domain/analysis/ProgressionMaterialCalculator.kt` - added and then deleted because it duplicated the existing governed calculator.
- `app/src/test/java/com/irminsul/codex/domain/analysis/ProgressionMaterialCalculatorTest.kt` - added and then deleted with the duplicate calculator.
- `app/src/main/java/com/irminsul/codex/data/catalogue/GenshinDbCostParser.kt` - now emits untrusted parsed cost segments compatible with the existing model.
- `app/src/test/java/com/irminsul/codex/data/catalogue/GenshinDbCostParserTest.kt` - tests source-stage mapping, missing stages, explicit empty arrays, malformed records, fractional values, and overflow.

### CI evidence and correction status

- CI #435 and #436 failed at Kotlin compilation because of the duplicate `CalculatedMaterialRequirement` declaration and resulting type mismatch errors.
- The duplicate calculator and test were removed.
- The parser and tests were rewritten to reuse the existing domain model.
- New CI runs triggered by the corrective commits are pending. Do not treat the correction as verified until a later run completes successfully.


### M51 source-permission boundary reaffirmed during M58

A focused reread of SPECS §3.18 confirmed the existing decision: no external source is currently approved for automated bulk ingestion or bundled upgrade-cost redistribution. The genshin-db package's MIT license does not grant rights to its upstream game-derived datasets or game assets. Its parser therefore remains an isolated, untrusted candidate-format adapter only. It must not fetch, bundle, publish, or promote genshin-db data into trusted calculations unless the specific source rights, attribution, access method, retention obligations, and `material_cost_curation` permission are reviewed and approved in the source registry.

The existing `UpgradeCostCalculator` and `UpgradeCostRecordValidator` remain the sole trusted calculation path. Candidate parsed segments are not usable by account analysis.


### M58 implementation update - material catalogue bridge and planner redesign

**Status:** Source implementation committed; CI and device verification pending.

#### Material metadata and calculation connection

- Added `MaterialCatalogue.kt` with a typed material category, stable item-ID lookup, display metadata (name, rarity, HTTPS icon URL), duplicate-ID rejection, and explicit unknown-material fallback.
- Catalogue entries require matching source-registry provenance, an approved `material_cost_curation` permission, HTTPS registry/reference URLs, and explicit human verification notes/timestamps. Unapproved or incomplete metadata is rejected.
- Added `CatalogueUpgradeCostCalculator` as a presentation adapter. It delegates all arithmetic, source validation, game-version matching, contiguous-segment checks, inventory handling, and overflow handling to the existing `UpgradeCostCalculator`. It only enriches the resulting requirements with catalogue metadata.
- Added tests for metadata resolution, unknown IDs, duplicate IDs, unapproved sources, missing-cost status, and a complete verified-record path through the existing calculator into named/quantified material output.
- The catalogue currently has no bundled game-data entries. This is intentional: M51/SPECS §3.18 still approves no external source for automated bulk ingestion or cost-data redistribution. Parsed genshin-db records remain candidates only. No automatic costs are claimed or displayed as verified until reviewed records and metadata are supplied by an approved source.

#### Planner UI and saved-plan compatibility

- Reworked the planner target form so each character and weapon target has its own persisted current/target level pair instead of sharing one global pair.
- Added separate current and target levels for Normal Attack, Elemental Skill, and Elemental Burst, persisted independently per character.
- Added a planned weapon selector per character using the currently observed weapon options plus a custom-weapon entry, with its own independent current/target levels.
- Kept the existing material-row SharedPreferences key format `manual_material_plan_rows_<targetId>` and the existing row JSON fields (`id`, `materialName`, `requiredQuantity`, `ownedQuantity`). Existing rows are loaded through the original parser and are not rewritten into a new schema.
- Kept the legacy shared level preference keys readable and updated them alongside the new per-target keys for compatibility with older builds.
- The UI explicitly states that verified automatic material costs are unavailable while the source-permission boundary remains unresolved. Manual material rows remain available.

#### Files changed

- `app/src/main/java/com/irminsul/codex/data/catalogue/MaterialCatalogue.kt` (new)
- `app/src/test/java/com/irminsul/codex/data/catalogue/MaterialCatalogueTest.kt` (new)
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`

#### Commits

- `288024dbda84318b465646e00d34541d912ce28d` - add material catalogue metadata and calculator adapter.
- `8e6e9e294f79fdbb2e2489fbf296312a1897df91` - test material catalogue resolution and calculator delegation.
- `b1afae3a4112aacb9f9281a004e2954ee45f6316` - redesign planner target and material planning sections.
- `1c1ae8a16afa42b9f6e1869d08dd1376aea0cc44` - persist talent targets independently per character.
- `a96533651cfeb748c0e0ce96bdc72e6fdb735f7a` - persist current/target talent levels and weapon plans per character.
- `d445602da8ff6ab5ff05c3604058333466f8427f` and `31bba2f7cedb88811e5378593ece449a07a6fefb` - enforce material catalogue provenance and source permission.
- `027172563b48a1ea838c9916d7286af3066bf2df` - test approved-source enforcement for material metadata.
- `7f5487eefee81c9ac90234749c547d66c4fd138a` - test a reviewed cost record flowing through the existing calculator and catalogue metadata.
- `04060f4d92cc7a7d3ffcc4d392dbc586800c97bd` - require HTTPS source-registry URLs for catalogue metadata.

#### CI failure #468 and correction

- Run #468 failed in `:app:testDebugUnitTest`, not during Kotlin compilation or APK packaging.
- The only reported failing test was `GenshinDbCostParserTest.parsesCharacterAscensionSegmentsIntoVerifiedCalculatorRanges` at line 18.
- Root cause: the synthetic fixture generates the first `ascend1` count as `(index + 1) * 10_000`, so its expected quantity is 10,000. The assertion incorrectly expected 20,000.
- Corrected the assertion to 10,000 and renamed the test to avoid implying that parser output is already trusted calculator data.
- Fix commit: `f404f723772f42081ec8c947d146e3dfb7ce2aa4`.
- A new CI run must confirm that the corrected test and the full unit-test suite pass. No passing result is claimed yet.

#### Verification and remaining work

- No successful CI run has been observed for these commits yet. The GitHub commit status endpoint returned no status entries; that is not evidence of a passing build.
- No APK has been built or installed for this update, and no physical-device planner test has been reported.
- Existing saved-plan preservation is supported by retaining the original target-specific row keys and row JSON fields, but still requires an on-device regression test using a pre-existing saved plan.
- The new planner UI provides independent character/weapon levels, current/target levels for all three talent tracks, and per-character planned-weapon choices and levels. It does not yet automatically populate material requirements, Mora, EXP books, weapon ore, crafting conversions, domain schedules, boss farming, or Resin estimates because no redistribution-approved trusted cost dataset is connected.
- The weapon selector is limited to weapons currently represented in the retrieved roster plus a custom text entry. It is not yet a complete inventory of every owned weapon.
- The catalogue bridge has a positive integration test using a synthetic, explicitly approved source registry entry. This proves the governed adapter path, not that a real upstream game-data source has been approved.
- Review Kotlin compilation and unit tests in CI. Fix any failures before asking for device verification.



### M58 CI verification update - Builds #469 and #470

The parser test correction is now CI-verified.

**Build #469**
- Run: https://github.com/berao609/Irminsul-codex/actions/runs/45688783781
- Run ID: `45688783781`
- Tested commit: `f404f723772f42081ec8c947d146e3dfb7ce2aa4`
- Result: **Success**
- Unit tests: **Success**
- Debug APK build: **Success**
- Room schema snapshot verification and upload: **Success**
- Debug APK signature verification and artifact upload: **Success**
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.469-vc471` (artifact ID `15057986214`)
- Room schema artifact: `irminsul-room-schema-v2-run-469` (artifact ID `15057093573`)

**Build #470**
- Run: https://github.com/berao609/Irminsul-codex/actions/runs/45688801385
- Run ID: `45688801385`
- Tested commit: `35c611a65e871a631c35f03d15b8a02dcfee4257`
- Result: **Success**
- Unit tests: **Success**
- Debug APK build: **Success**
- Room schema snapshot verification and upload: **Success**
- Debug APK signature verification and artifact upload: **Success**
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.470-vc472` (artifact ID `15056449871`)
- Room schema artifact: `irminsul-room-schema-v2-run-470` (artifact ID `15056734729`)

Room instrumentation tests were skipped in both runs because the workflow determined they were not required for these changes. Do not describe them as having passed.

**Verification boundary**
- The corrected parser tests and full configured JVM unit-test task passed in CI.
- Debug APKs were built, signature-verified, and uploaded successfully.
- No physical-device installation or Materials Planner runtime test has yet been reported.
- Existing M53 saved-plan preservation still requires an on-device regression test.
- M58 remains open pending device verification and the remaining automatic-cost/catalogue work described above.

**Next action**
Install the latest Debug artifact from Build #470 (`v1.0.0-dev.470-vc472`) and test independent character levels, all three talent tracks, each character's planned weapon and weapon levels, and an existing saved manual material plan. Confirm that saved values remain after leaving and reopening the planner and after restarting the app.

### UI guideline platform-alignment correction

**Status:** Documentation update committed; Android implementation unchanged.

- Updated `docs/Irminsul Codex UI Design Instructions.md` to explicitly target native Kotlin, Jetpack Compose, and Material 3.
- Preserved the visual direction: Archival Gold, neutral surfaces, restrained elemental accents, data-first typography, compact artifact inspection, dark backing for white game glyphs, and the symmetric five-slot navigation concept.
- Replaced Flutter-specific theme, widget, asset, WebView, animation, haptics, and verification guidance with Android/Compose equivalents.
- Clarified that the reserved fifth-shell position is layout space only until a real destination exists; no fake route or dead navigation item should be added.
- Corrected the icon dependency boundary: the current app has Compose Material Icons Extended; a Lucide Compose dependency is not assumed or added.
- Clarified that Compose UI/instrumentation testing must be based on the repository's actual test setup. CI JVM unit tests do not prove layout correctness, and device verification remains a separate evidence category.
- No application source, Gradle dependencies, runtime behavior, or saved data were changed.

**Commit:** `2372621ea4cbdbe611953e998ec6504cc00573c1`

**Next action:** Resume M58 implementation. Continue the Materials Planner work using the corrected native Compose design guidance, preserving manual material rows and existing saved-plan compatibility. Keep source-permission restrictions from SPECS §3.18 in force; do not invent or bundle unapproved upgrade-cost data.

### M58 planner presentation pass - native Compose

**Status:** UI source committed; CI and device verification pending.

- Reorganized the Materials Planner into a numbered flow: upgrade target, talent investment, planned weapon, and material requirements.
- Increased section hierarchy and spacing, standardized form-control corner radii, and reduced repeated explanatory copy.
- Reworked manual material rows into compact cards with a clear remaining-quantity result when both required and owned values are valid.
- Kept blank owned quantities distinct from an explicit zero.
- Preserved the existing target-specific SharedPreferences keys, talent/weapon persistence keys, manual row JSON fields, and save/load effects. The UI-only change does not introduce a new saved-plan schema.
- Kept the automatic-cost status explicit. No unapproved source data or automatic cost claims were introduced.
- No new dependencies were added.

**Commit:** `a30e20ae6a46dbc17e07eba8a069cf4fb3fcc0a0`

**Verification boundary:** The code change is committed. CI compilation/unit tests and a physical-device check are still required. The existing manual material rows and previously saved plan values must be checked on device after installing the new build.

### Character Showcase progressive inspection pass

**Status:** Compose implementation committed; CI and physical-device verification pending.

- Replaced the always-expanded weapon, five-artifact stat dump, and full attribute list with tap-to-inspect interactions.
- Weapon summary is compact by default. Tapping it reveals the weapon description, main/secondary stat, level, ascension, and refinement details.
- Artifacts remain a compact horizontal five-slot row. Tapping one artifact reveals only that artifact's set item identity, slot, rarity, level, main stat, and substats. Other artifacts stay collapsed.
- Attributes are collapsed behind a summary row and expand only when selected.
- Talent and constellation icons remain compact and reveal their descriptions/effects when selected.
- One shared selection state is used per character. Opening a different item switches the detail panel; tapping the active item again collapses it. Changing characters resets the open detail.
- Stale equipment indicators and unavailable/missing data states remain visible.
- No account model, source data, persistence schema, network behavior, or dependencies were changed.

**Commit:** `d697496c312b6157d8b876463fe0147cea5896af`

**Verification boundary:** Source change is committed. CI compilation and tests are pending. Physical-device verification must confirm tap targets, horizontal artifact scrolling, expanded stat readability, and Light/Dark appearance. The CI workflow does not by itself prove visual correctness on device.

### Expandable character roster and collection controls

**Status:** Compose implementation committed; CI and physical-device verification pending.

- Replaced the always-visible horizontal character strip with a compact, expandable Character Roster header. The selected character's detailed showcase remains visible when the roster list is collapsed.
- Expanded roster uses a compact three-column portrait grid and highlights the currently selected character.
- Added name search and filters for element, weapon type, rarity, and minimum/maximum character level.
- Added sorting by name, level, rarity, and friendship level, with ascending/descending order.
- Filter choices are derived from the retrieved roster and weapon metadata. Missing weapon metadata is grouped as Unknown; numeric weapon type identifiers are displayed as source type codes rather than guessed weapon names.
- Added a clear-filters action and an explicit no-results state.
- Kept selection and character showcase details connected to the original character IDs. No account payload, persistence schema, networking, or dependencies were changed.

**Commit:** `83ee34e5a997d7e036c58edc7178987ac1b1661a`

**Verification boundary:** Source is committed. CI compilation/unit tests and physical-device layout/interaction checks are pending. Device checks should cover roster collapse/expand, portrait selection, all filter combinations, level boundaries, sort direction, empty results, and Light/Dark themes.

### Showcase compact progression and description cleanup

**Status:** Source committed; CI build in progress; weapon passive source still requires verification.

- Moved talent and constellation icon groups into the compact character profile area directly following portrait/core identity, ahead of build inspection.
- Added shared display cleanup for talent, constellation, enhanced descriptions, and weapon description text: strips markup, removes internal link syntax, converts escaped newline sequences, and reduces excessive blank lines. Original mapped data remains untouched.
- Weapon passive is not added yet: current account mapper only maps weapon `desc`, which is not a verified combat-passive source. Do not display lore as a passive or fabricate refinement text.
- No account schema or persisted data changes.

**Commit:** `ff72b6414ad0e5f14b2d5feb0c507e1e5e022cd2`
**CI:** Run #483 in progress.


### Home Real-Time Notes visual reconstruction pass

**Status:** Compose source committed; CI and physical-device verification pending.

- Reworked the Home Daily Note presentation into a compact Chronicle-inspired panel.
- Resin is presented as the primary left-side metric with current/max values, progress, and recovery status.
- Expeditions, Realm Currency, Daily Commission progress/reward status, and weekly Resin discounts are grouped in a compact right-side stack.
- Kept the section on Home. It is not duplicated in Visuals.
- Reused only fields already present in the existing Daily Note response model. Individual expedition names/progress and other unavailable sub-records are not fabricated.
- Preserved the existing Daily Note request, parser, nullable-value behavior, refresh action, and authentication flow.
- No persistence schema, API endpoint, dependency, or account-export behavior changed.

**Commits:**
- `56da00ab4cf8daa0fb1b242d1aeb4390e84d9b55` - Home Daily Note panel layout
- `df54b792c4f10107e844ae93fb5c2d92ee5edd95` - shared Daily Note metric row

**Verification boundary:** Source is committed. CI compilation/unit tests and physical-device layout checks are pending. Confirm the panel at narrow mobile width and in both Light and Dark themes. The current API model does not provide per-expedition detail rows, so only the aggregate expedition count is displayed.


### Battle Chronicle Visuals data-section pass

**Status:** Compose source committed; CI #492 running; device verification pending.

- Added expandable Account Chronicle sections near the top of Visuals: Account Overview, World Exploration & Collectibles, current/previous Spiral Abyss, and Imaginarium Theater.
- Sections render existing structured account-export payloads without changing or discarding source fields.
- Nested objects and arrays expand inline. Labels are humanized from source keys; primitive values retain their source values.
- Missing, unavailable, failed, and stale section states are distinguished. Stale payloads remain visible with a stale-data label.
- No synthetic statistics, chest counts, exploration percentages, or game-mode records are generated.
- Stygian Onslaught is not displayed yet because the current AccountSnapshot model does not expose a dedicated Stygian section. Add it only after tracing and mapping an actual supported source payload.
- Real-Time Notes remain exclusively on Home.

**Commit:** `1f31fd63de3826d86240bcf40f23b33a79340e24`

**CI:** Run #492 started for this commit: https://github.com/berao609/Irminsul-codex/actions/runs/45802370862

**Verification boundary:** Compilation, tests, APK packaging, and device rendering are not yet confirmed. This is a data-faithful expandable first pass, not the final polished Battle Chronicle visual treatment. Follow-up work should replace generic nested-data presentation with purpose-built overview, exploration, chest, and game-mode layouts after confirming the exact export keys and payload shapes.


## Milestone 059 - HoYoLAB Credential Protection and Room Migration Integrity

**Status:** Implementation committed; CI verification passed; physical-device verification passed.

### Implemented

- Added `HoYoLabCredentialStore`, which encrypts the HoYoLAB session cookie with AES-GCM using an Android Keystore-held key.
- Persisted only encrypted cookie ciphertext in app-private SharedPreferences. The UID and theme preference remain non-sensitive preferences.
- Added automatic migration of the legacy plaintext cookie on first read. The legacy value is removed only after encryption succeeds. Decryption/corruption errors are surfaced so the user can explicitly clear the unusable saved credential.
- Added an Account-screen sign-out action that clears the saved UID, encrypted/legacy cookie values, and in-memory credentials while retaining local account snapshots.
- Added Android instrumentation coverage for encryption and restoration through a new store instance, legacy-cookie migration, replacement, clearing, and corrupted ciphertext behavior.
- Added a version-1 Room schema snapshot and a migration test that creates a version-1 database, migrates small and multi-chunk JSON snapshots, and checks raw JSON reconstruction and metadata/account preservation.
- Configured the Room schema directory as Android instrumentation-test assets so `MigrationTestHelper` can load historical schema files.
- Added the generated version-2 Room schema snapshot to version control.
- Updated README architecture/security descriptions to identify the credential-store boundary and Keystore-backed encryption.

### Files changed

- `app/src/main/java/com/irminsul/codex/integration/security/HoYoLabCredentialStore.kt`
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/androidTest/java/com/irminsul/codex/integration/security/HoYoLabCredentialStoreTest.kt`
- `app/src/androidTest/java/com/irminsul/codex/data/local/IrminsulDatabaseMigrationTest.kt`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `app/schemas/com.irminsul.codex.data.local.IrminsulDatabase/1.json`
- `app/schemas/com.irminsul.codex.data.local.IrminsulDatabase/2.json`
- `README.md`

### Implementation commits

- `a060320a9dca3e9a3536fd63d67142063b3a1771` - credential protection and initial migration test.
- `dcaa806645354e97776816faebfc8d3169bfdfab` - package Room schema history as instrumentation-test assets.
- `2dc605dc467d719a9f184dd1c2cf5fc98577a6ec` - preserve the generated Room version-2 schema.
- `e0d57b1a8e0b321a654fc3cc2e4bc872799f40b5` - align README with secure credential storage.

### CI verification

- **Run #520, attempt 1:** Failed because Maven Central returned HTTP 429 while resolving `kotlin-stdlib:1.9.20` for the instrumentation test host.
- **Run #520, attempt 2:** Exposed a test configuration defect: `MigrationTestHelper` could not find the version-1 schema because the schema directory was not packaged as androidTest assets.
- **Run #521:** Passed on commit `dcaa806645354e97776816faebfc8d3169bfdfab`. Unit tests passed; all 8 Android instrumentation tests passed; Debug APK build, Room schema generation check, APK signature verification, and artifact upload passed.
  - Run: https://github.com/berao609/Irminsul-codex/actions/runs/45847411030
  - Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.521-vc523` (ID `15131419035`)
  - Room schema artifact: `irminsul-room-schema-v2-run-521` (ID `15131059231`)
- **Run #522:** Passed on commit `2dc605dc467d719a9f184dd1c2cf5fc98577a6ec`. Unit tests, Debug APK build, generated schema check, signature verification, and artifact upload passed. Instrumentation tests were skipped because this commit only added the committed schema snapshot.
  - Run: https://github.com/berao609/Irminsul-codex/actions/runs/45847457876
  - Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.522-vc524` (ID `15131764251`)
  - Room schema artifact: `irminsul-room-schema-v2-run-522` (ID `15132685195`)

### Verification boundary and next actions

- CI verified the credential-store instrumentation tests and version-1-to-2 Room migration test on an Android API 34 emulator.
- Physical-device checks passed: the saved session survived installation and app restarts; sign-out returned to login while the local character roster remained available; fresh login succeeded and remained connected after another restart.
- The encrypted bytes stored on the physical device were not directly inspected. Encryption-at-rest behavior is covered by automated tests.
## Milestone 060 - JSON Export Save-Result Integrity

**Status:** Implementation committed; CI verification pending; physical-device verification pending.

### Implemented

- Fixed the Android document-save callback so a null output stream is treated as a failed save instead of silently reporting success.
- Extracted the UTF-8 document write into a small testable JsonDocumentWriter.
- The writer returns the byte count only after the stream has been written and closed successfully. Open, write, and close failures propagate to the caller.
- The UI displays the saved-file success message only after the writer completes. Failed writes clear the success status and show an error. A cancelled document picker is reported as cancelled.
- Added unit tests for exact UTF-8 output, null output stream, stream-open failure, write failure, and close failure.
- No export payload shape, source-data preservation, account retrieval, Room persistence, credential handling, or export-format version was changed.

### Files changed

- app/src/main/java/com/irminsul/codex/MainActivity.kt
- app/src/main/java/com/irminsul/codex/data/export/JsonDocumentWriter.kt
- app/src/test/java/com/irminsul/codex/data/export/JsonDocumentWriterTest.kt

### Implementation commits

- a509acede2a9540a593f29cac141d66207f1458b - add testable JSON document writer.
- c62e9f73b1a73e686f54d1516bf6e134ca14716d - add JSON document writer failure tests.
- d4c0369295d788bf224d4a23a35fa78f0683fd14 - prevent false success on JSON export failure.
- f2f83954342b8ffccbfc455774bf25eb209df676 - import the JSON document writer in MainActivity.

### CI verification

- CI runs #526, #527, #529, and final integrated run #530 passed. Run #528 failed before the final import/integration commits; the final run #530 passed on commit `ec38bc7895aa399b11e6c4da47e681cd6562860c`.
- Final run: https://github.com/berao609/Irminsul-codex/actions/runs/45850549052

### Physical-device verification

- User confirmed the export flow is clear and the save result is understandable.
- Follow-up observation: after saving begins, there is no cancel control. Add a safe cancellation action in a future relevant batch. This is a follow-up UX requirement, not a failure of the false-success fix.

### Verification boundary

- The automated writer tests cover successful UTF-8 output, null stream, open failure, write failure, and close failure.
- The final integrated CI run passed. Do not claim that an in-progress write can currently be cancelled.

## Milestone 061 - CPU-Bound Account Processing Dispatch

**Status:** Implementation committed; CI verification passed; physical-device verification pending.

### Implemented

- Moved HoYoLAB export JSON-to-domain mapping to an injected CPU dispatcher, defaulting to `Dispatchers.Default`.
- Moved deterministic account analysis in AccountViewModel to an injected CPU dispatcher.
- Moved Room snapshot JSON chunk splitting, chunk joining, and restored snapshot mapping to an injected CPU dispatcher.
- Kept Room DAO queries and transactions as suspend Room operations on Room-managed executors.
- Preserved coroutine cancellation propagation.
- Added tests confirming repository mapping, ViewModel analysis, and Room snapshot restoration mapping execute away from the caller thread.
- Added cancellation coverage for export mapping.
- No new dependencies, Room schema changes, export-format changes, or deterministic analysis rule changes.

### Files changed

- `app/src/main/java/com/irminsul/codex/data/repository/HoYoLabAccountRepository.kt`
- `app/src/main/java/com/irminsul/codex/data/local/repository/RoomAccountSnapshotStore.kt`
- `app/src/main/java/com/irminsul/codex/presentation/viewmodel/AccountViewModel.kt`
- `app/src/test/java/com/irminsul/codex/data/repository/HoYoLabAccountRepositoryTest.kt`
- `app/src/test/java/com/irminsul/codex/presentation/viewmodel/AccountViewModelTest.kt`
- `app/src/androidTest/java/com/irminsul/codex/data/local/RoomAccountSnapshotStoreTest.kt`

### Implementation commits

- `6f752366f7f16388bbc9425fb2c9e4053615514a` - dispatch export mapping.
- `9f7ae9a737a151ebe5ff83dcee9f176efe0ef087` - test export mapping dispatcher and cancellation.
- `c861864694c8cfa9662e4dd00d9b61b2b189c371` - dispatch snapshot chunk preparation and restoration mapping.
- `d061d1a3777b068a85ef70ec3ebfba2135b2869c` - dispatch deterministic analysis.
- `27be3508de96b886b8047d4dabe8639de6910020` - make analysis dispatcher injectable for tests.
- `a04882154cf51234d364a74c0ef9fb748e72f374` - test analysis dispatcher.
- `b1760fba239f83108c8cbc987493320d093ae5a4` - test Room restoration dispatcher.
- `448d2823745bd169dcfb72f152f8d45d0ae8240f`, `3a88ba07452fac7c3236f687aa11487b5fcc509f`, `393e117c8112c87a604441e4f80cd0184dbf38e0` - correct off-caller thread assertions.
- `d87193175dbd415328ef9d844aaa45b2267145ce` - keep ViewModel tests deterministic with an injected test dispatcher.
- `9bc551fe1b59ff08c244fbb398d684a7973c01c8` - add missing instrumentation assertion import.

### CI verification

- **Run #541:** Passed unit tests, Debug APK build, Room schema verification, and APK signature verification. Room instrumentation tests were skipped because the run did not include an instrumentation-test source change.
- **Run #542:** Passed all workflow steps on commit `9bc551fe1b59ff08c244fbb398d684a7973c01c8`, including unit tests, Room instrumentation tests, Debug APK build, Room schema verification, APK signature verification, and artifact upload.
  - Run: https://github.com/berao609/Irminsul-codex/actions/runs/45853412200
  - Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.542-vc544` (ID `15134681823`, SHA-256 `b0cf7d0f602c3f179f1f9580ecf8c18185be434a9c830f660a99e3b45a00086b`)
  - Room schema artifact: `irminsul-room-schema-v2-run-542` (ID `15135495906`)

### Verification boundary and next actions

- CI confirms unit and Android instrumentation tests, Debug APK packaging, Room schema verification, and APK signature verification.
- No comparable baseline System Trace was captured before the dispatcher changes. No quantified speedup or frame-time improvement is claimed.
- Physical-device checks remain: fresh account retrieval, app restart and cached snapshot restoration, full-roster deterministic analysis, and confirmation that the UI remains responsive during these operations.
- Clear this milestone only after the physical-device checks are completed. The next optimization batch is AUD-007, Release test gate.


## Milestone 062 - Bounded Account Chronicle Rendering

**Status:** Implementation committed; CI verification pending; physical-device verification pending.

### Trigger

During physical-device testing of Debug build #542, account retrieval and account information were correct, but expanding Account Chronicles caused visible FPS drops. World Exploration & Collectibles was the most affected section. The expanded UI also displayed raw URLs, image/icon paths, and internal identifiers.

Static inspection found that the Chronicle renderer recursively composed every nested object and array immediately. This was consistent with the reported behavior, but no runtime trace or quantified frame-time measurement was available.

### Implemented

- Changed Chronicle rendering to show immediate object fields while keeping nested objects and arrays collapsed by default.
- Expanded arrays now use a bounded-height lazy list with stable item keys, avoiding eager composition of every entry.
- Added presentation rules to hide technical field names (IDs, URL/image/icon/path/source metadata) and remote URLs/artwork filenames.
- Preserved meaningful profile, region, level, exploration, and achievement fields when supplied by the source.
- Kept source StructuredData unchanged. No export, mapper, Room schema, persistence, or account-analysis behavior was changed.
- Preserved section status handling for available, stale, missing, unavailable, and failed data.
- Kept the dedicated Character Showcase as the primary detailed character presentation.

### Files changed

- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/ChroniclePresentationRules.kt` (new)
- `app/src/test/java/com/irminsul/codex/presentation/ChroniclePresentationRulesTest.kt` (new)
- `docs/CODEBASE_OPTIMIZATION_AUDIT.md`
- `docs/PROGRESS_MILESTONES.md`

### Verification

- Unit tests added for technical-key filtering, URL/artwork-filename suppression, and retention of meaningful account values.
- CI result: pending.
- Debug APK artifact: pending.
- Physical-device checks: pending. The user must retest World Exploration & Collectibles, the remaining Chronicle sections, and data/export integrity after installing the new build.

### Verification boundary

This change defers nested traversal and lazy-composes expanded array rows. A measurable FPS improvement is not claimed until device retesting or comparable profiling confirms it. The original #542 build remains the comparison point; Batch 3 is not cleared yet.


## Milestone 063 - Replace Generic Chronicle Payload Rendering

**Date recorded:** 2026-11-27  
**Scope:** Remove the failed generic raw-payload Chronicle renderer and replace it with allowlisted section-specific summaries.

**Implementation status:** Prepared in this commit. Account overview and mode summaries use explicit field allowlists. World exploration uses named region cards in a horizontal strip with normalized progress values. Raw IDs, URLs, image paths, nested source metadata, and unknown fields are not rendered. Original StructuredData and JSON export remain unchanged.

**Reason for replacement:** The user tested the previous bounded recursive renderer and reported worse lag plus continued exposure of reference IDs and URLs. The prior implementation is considered failed.

**Verification status:** CI pending for this commit. Physical-device verification pending. No performance improvement is claimed before the replacement is installed and retested.

**Known limitation:** The export's exact exploration and mode payload variants are not fully represented by repository fixtures. The new presentation intentionally shows only supported allowlisted fields; unsupported variants remain hidden rather than falling back to raw rendering.


## Milestone 064 - Correct Chronicle Renderer Compilation Error

**Date recorded:** 2026-11-27  
**Scope:** Correct the duplicate Compose annotation identified by Debug CI #550.

**Trigger:** CI #550 failed during `app:compileDebugKotlin` with `MainActivity.kt:2837:1: This annotation is not repeatable.` Inspection confirmed two consecutive `@Composable` annotations immediately before `ChronicleOverviewSection`.

**Correction:** Removed the duplicate annotation. The function retains one `@Composable` annotation. No presentation behavior, source data, or export logic was changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `docs/CODEBASE_OPTIMIZATION_AUDIT.md`
- `docs/PROGRESS_MILESTONES.md`

**Verification status:** Correction committed; fresh CI pending. CI #550 did not reach unit tests, APK build, signature verification, or artifact upload. Physical-device verification of the allowlisted Chronicle renderer remains pending. No performance improvement is claimed.


## Milestone 065 - Release Unit-Test Gate

**Date recorded:** 2026-11-27  
**Scope:** Prevent official Release APK signing and publication when tagged-source JVM unit tests fail.

**Implemented:**
- Added `gradle testDebugUnitTest --no-daemon` to the Release workflow after tag/version validation and Gradle setup.
- The test gate runs before Release keystore decoding, APK build/signing, signature verification, and GitHub Release publication.
- A failing test terminates the job before any signing or publication step.
- Kept Room instrumentation tests under the existing Debug CI policy. Relevant persistence/schema changes and manual full-test runs continue to execute the emulator suite.
- Updated `RELEASE_PROCESS.md` with the Release test-gate behavior and coverage boundary.
- Closed Batch 3 in the optimization audit after CI #542/#555 and successful physical-device verification.

**Files changed:**
- `.github/workflows/release.yml`
- `RELEASE_PROCESS.md`
- `docs/CODEBASE_OPTIMIZATION_AUDIT.md`
- `docs/PROGRESS_MILESTONES.md`

**Verification status:** Implementation committed; main-branch CI pending. The successful tagged-release publication path has not been run. No test tag was created and no official release was published.

**Coverage boundary:** Official releases run JVM unit tests against the tagged source. Room instrumentation tests are not repeated per release; they remain gated by relevant Debug workflow changes or manual full-test runs.


## Milestone 066 - Lazy Character Roster Grid

**Date recorded:** 2026-11-27  
**Scope:** Reduce eager composition of the expanded character roster while preserving its existing behavior.

**Implemented:**
- Replaced the eager three-column `Column`/row/card tree with a three-column `LazyVerticalGrid`.
- Added stable character-ID item keys.
- Bounded the nested grid viewport to a maximum of four estimated card rows, allowing the lazy grid to operate safely inside the existing vertically scrollable Visuals page.
- Memoized roster filter choices and filtered/sorted results against their input state.
- Preserved search, element/weapon/rarity/level filters, sort direction, selected-character highlighting, and the detail panel during this performance-only change.
- Added no dependencies and changed no account data, persistence, or deterministic analysis behavior.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `docs/CODEBASE_OPTIMIZATION_AUDIT.md`
- `docs/PROGRESS_MILESTONES.md`

**Verification status:** CI #557 passed; physical-device verification passed.

**Device test checklist:**
1. Passed: the final character is reachable by scrolling inside the grid.
2. Passed: the surrounding Visuals page scrolls when swiping outside the grid.
3. Passed: search, element, weapon, rarity, minimum/maximum level filters, and Clear filters.
4. Passed: Name, Level, Rarity, and Friendship sorting in both directions.
5. Passed: selecting characters updates highlighting and the detail panel.
6. Passed: collapsing/reopening and scrolling remain responsive.

**Verification boundary:** No runtime trace, composition count, frame-time comparison, or memory benchmark has been captured. The user confirmed normal, responsive device behavior; no quantified performance improvement is claimed.

**Follow-up UI requirements recorded for a separate focused change:**
- Remove the Rarity filter entirely.
- Remove all sorting controls and sorting options.
- Display characters in chronological release order, oldest first, by default.
- Give 5-star character cards a permanent gold background and 4-star cards a permanent purple background.
- Add a subtle selection highlight without replacing the permanent rarity background.


## Milestone 067 - Extract Account Chronicle Presentation

**Date recorded:** 2026-11-27  
**Scope:** Begin AUD-002 by separating cohesive Account Chronicle UI from the oversized MainActivity source file.

**Implemented:**
- Moved Account Overview, World Exploration & Collectibles, and Chronicle record summary composables into `app/src/main/java/com/irminsul/codex/presentation/ChronicleSections.kt`.
- Kept the composables' existing parameters, mapper calls, expansion behavior, and visual content unchanged.
- Updated MainActivity imports and retained its existing Chronicle call sites.
- Left Daily Note metrics, materials planning, lineup simulation, and character showcase untouched.

**Verification status:** CI pending; physical-device verification pending.

**Verification checklist:**
1. Confirm the app builds and unit tests pass.
2. Open Visuals and verify Account Overview.
3. Verify World Exploration & Collectibles and its region cards.
4. Verify current/previous Spiral Abyss and Imaginarium Theater sections.
5. Expand and collapse each Chronicle section and confirm the page remains responsive.

**Boundary:** This is a structural extraction, not a UI redesign or performance optimization claim.


## Milestone 068 - Extract Lineup Simulator Presentation

**Date recorded:** 2026-11-27  
**Scope:** Continue AUD-002 by separating the Lineup Simulator UI from the oversized MainActivity source file.

**Implemented:**
- Moved the Lineup Simulator composable into `presentation/LineupSimulatorSection.kt`.
- Kept selection state, active-roster filtering, stale/partial status derivation, and toggle callbacks in `CodexScreen`.
- Kept `LineupSelectionRules` unchanged in the domain layer.
- Moved lineup-specific artwork and element fallback helpers with the extracted presentation.
- Preserved the four-slot layout, selection order, full-team behavior, roster warnings, and descriptive-only element summary.
- No account data, persistence, navigation, or analysis behavior was intentionally changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/LineupSimulatorSection.kt`

**Commits:**
- `e2e21c3703f733448e7918ea7be7ad2de3b52636`
- `21b68f750a54fce3f5115146225cfca1638bda7b`

**Verification status:** Physical-device verification passed (user confirmed checklist clear on 2026-11-27). CI remains pending and is not claimed as passed.

**Device verification:** User confirmed the device checklist is clear on 2026-11-27. The following behaviors were included in the checklist: expand the section, add/remove characters, preserve slot order and the four-character limit, confirm roster warnings/empty state, and collapse/reopen without losing selection.

**Remaining verification:** Debug CI compilation, unit tests, APK build, schema verification, and signature verification have not been confirmed.

**Result:** Extraction verified and cleared. Structural extraction only; no performance improvement is claimed.


## Milestone 069 - Extract Character Showcase Presentation

**Date recorded:** 2026-11-27  
**Scope:** Continue AUD-002 by separating the Character Showcase UI from the oversized MainActivity source file.

**Implemented:**
- Moved the Character Showcase roster, filters, selected-character detail profile, talent and constellation details, weapon and artifact inspection, and supporting presentation helpers into `presentation/CharacterShowcaseSection.kt`.
- Kept selected-character ID and selection callback ownership in `CodexScreen`.
- Kept roster search, filters, and expansion state local to the Character Showcase presentation.
- Preserved release ordering, rarity card backgrounds, selection highlight, filters, lazy grid, character details, weapon passive lookup, and artifact/talent/constellation interactions.
- No account model, persistence, navigation, analysis, or product behavior was intentionally changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CharacterShowcaseSection.kt`

**Commits:**
- `96628ec862051308703c0a90e783bf77bbcac8c6`
- `f3ca764cfb0c22291cd92baeceb753679369e5cc`
- `177c74737e4097d1187499784ff5fd253f15d551`
- `6aa462c0743264f2a48311d20ed6b17c181ea59c`
- `6ab25a876e7aed9e7db4f92e1cad7743ab5a0fc9`

**Static source verification:** MainActivity imports and calls the extracted composable; its previous private implementation is absent; selected-character ownership remains in CodexScreen; roster and detail components are present in the new presentation file.

**Verification status:** CI #581 passed; physical-device verification passed (user confirmed all checklist items).

**CI correction record:** Initial Debug CI attempts found missing coroutine imports in the extracted file and a shared avatar fallback still referenced by the recommendation card in MainActivity. Both issues were corrected in commits `6aa462c` and `6ab25a8`. Subsequent CI reruns are pending; do not treat the earlier failed runs as passing.

**Device checklist:**
1. Open Visuals and expand Character Roster.
2. Verify newest-first release order, portrait cards, permanent rarity backgrounds, and selected-card highlight.
3. Test search, element and weapon filters, level range, and Clear filters.
4. Select multiple characters and confirm the detail panel follows the selected character.
5. Inspect talents, constellations, weapon passive, weapon stats, artifacts, and artifact substats.
6. Collapse/reopen the roster and confirm the selected character remains selected.

**Verification completed:** CI #581 passed compilation, unit tests, APK build, Room schema verification, APK signature verification, and artifact upload. User installed build #581 and confirmed the device checklist passed.

**Boundary:** Structural extraction only. No performance improvement is claimed.


## Milestone 070 - Character Showcase Extraction Verification

**Date recorded:** 2026-11-27  
**Scope:** Close AUD-002 Character Showcase presentation extraction after automated and physical-device verification.

**Verification:**
- Debug CI runs #579, #580, and #581 passed after the initial compile issues were corrected.
- Run #581 verified compilation, unit tests, Debug APK build, Room schema verification, signature verification, and artifact upload.
- User installed build #581 and confirmed all Character Showcase device checklist items passed.

**Result:** Character Showcase extraction is cleared. Continue AUD-002 with one focused presentation extraction at a time.

**Boundary:** No measured performance improvement is claimed.


## Milestone 071 - Extract Materials Planner Presentation

**Date recorded:** 2026-11-27  
**Scope:** Continue AUD-002 by separating the Materials Planner UI and its presentation-local persistence helpers from MainActivity.

**Implemented:**
- Moved Materials Planner composable, target option/row models, preference-backed state, and manual material JSON serialization helpers into `presentation/MaterialsPlannerSection.kt`.
- Kept MainActivity's existing call site and supplied the extracted composable through an import.
- Preserved existing SharedPreferences keys, saved-row serialization, and domain calculation logic.
- No intentional behavior or persistence format change.

**Commits:**
- `1336b1de15c3920fcd3fd34409743efec8135cd0`
- `9f19650b11a6180696b988c5baaa01c9d7cccf26`

**Verification status:** CI runs #584 and #585 are pending. Physical-device verification is pending.

**Boundary:** Structural extraction only; no measured performance improvement is claimed.


## Milestone 072 - Genshin DB Progression Candidate Import Foundation

**Date recorded:** 2026-11-27  
**Scope:** Connect the existing Genshin-DB cost parser to stable character/weapon progression identities while preserving the candidate-versus-verified trust boundary.

**Implemented:**
- Corrected Genshin-DB `ascend1` through `ascend6` level ranges to 20→40, 40→50, 50→60, 60→70, 70→80, and 80→90.
- Added optional character `ascensionLevel` mapping from source `promote_level`; missing values remain null and zero is preserved.
- Added stable, source-attributed candidate mapping for parsed upgrade-cost segments.
- Added character import for ascension and separate Normal Attack, Elemental Skill, and Elemental Burst talent tracks.
- Added weapon ascension import.
- Preserved incomplete stages and invalid data as explicit import issues.
- Character level EXP and weapon level EXP are explicitly reported as missing. The importer therefore returns partial coverage rather than pretending the progression dataset is complete.
- Candidate data is not promoted to verified calculation records. No source permission is approved and no automatic cost dataset is connected to the Materials Planner UI.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/data/catalogue/GenshinDbCostParser.kt`
- `app/src/test/java/com/irminsul/codex/data/catalogue/GenshinDbCostParserTest.kt`
- `app/src/main/java/com/irminsul/codex/domain/model/AccountSnapshot.kt`
- `app/src/main/java/com/irminsul/codex/data/mapper/AccountExportMapper.kt`
- `app/src/test/java/com/irminsul/codex/data/mapper/AccountExportMapperTest.kt`
- `app/src/main/java/com/irminsul/codex/data/catalogue/GenshinDbUpgradeCostCandidateMapper.kt`
- `app/src/test/java/com/irminsul/codex/data/catalogue/GenshinDbUpgradeCostCandidateMapperTest.kt`
- `app/src/main/java/com/irminsul/codex/data/catalogue/GenshinDbProgressionCandidateImporter.kt`
- `app/src/test/java/com/irminsul/codex/data/catalogue/GenshinDbProgressionCandidateImporterTest.kt`

**Commits:**
- `f5a26e3576cc03bff1b2568138f46154180e65a9`
- `2383c78db6ae379e2e67d428565f3f98b3cc8749`
- `b82e69955b601cc0f6037c82c02ccbbaafdb9a90`
- `e753dde949bdeecafd99ac82285c8165bc9eda1f`
- `3be0b925f71e6afe86b896d6844fc7762d9ae9e8`
- `ab986aceea117a4f8a38a9e1e341410699daa9b8`
- `012b76623d8fc9fa16099d134bb72b03b2c04a9f`

**Verification:**
- CI #594 passed parser regression tests, Debug APK build, schema verification, signature verification, and artifact upload.
- CI #595 passed after character ascension rank was added to the snapshot model.
- CI #596 passed candidate-mapper tests.
- CI #599 passed unit tests, Debug APK build, Room schema verification, signature verification, and artifact upload.
- Intermediate failures were not hidden: #593 exposed the old ascension-range test expectation; #597 and #598 exposed incorrect importer test assertions. The assertions were corrected and the final #599 run passed.
- CI #599: https://github.com/berao609/Irminsul-codex/actions/runs/45922119144
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.599-vc601` (artifact ID `15165061135`).
- APK artifact SHA-256: `947ea64e01adcb586ee5eb21d4f0d6c90f52641c4537634e542462e1f06052f4`.

**Physical-device verification:** Not performed. This milestone changes domain/data parsing only and introduces no visible UI or persistence behavior.

**Remaining work:**
- Add verified character EXP and weapon EXP progression curves.
- Complete material metadata and source attribution review.
- Verify source reuse/redistribution terms before bundling data.
- Promote only reviewed, complete records into the calculation catalogue.
- Connect the verified records to automatic character/weapon target inputs and the Materials Planner UI.

**Result:** Progression data can now be imported as stable, provenance-carrying candidates. This is not yet a complete or user-facing automatic materials calculator.


## Milestone 073 - Character EXP Progression Candidate Calculator

**Date recorded:** 2026-11-27  
**Scope:** Add the shared character level EXP curve and a deterministic candidate calculator without bypassing the verified-cost trust boundary.

**Implemented:**
- Added the complete cumulative Character EXP curve for levels 1-90, indexed once for all characters rather than duplicated per character.
- Added candidate provenance and a separate CC BY-SA 3.0 attribution/license notice for the adapted Genshin Impact Wiki table. The dataset remains marked `CANDIDATE` and source permission remains `PENDING_REVIEW`; it is not promoted into the trusted material catalogue.
- Added a pure character EXP calculation path accepting current level, current EXP within that level, target level, and the maximum level unlocked by ascension.
- Enforced valid ascension caps (20, 40, 50, 60, 70, 80, 90), level bounds, within-level EXP bounds, and monotonic progression data.
- Calculated Hero's Wit (Genshin-DB ID 104003), Adventurer's Experience (104002), Wanderer's Advice (104001), and Mora (202).
- Rounded book requirements independently at each ascension phase, matching the published 1→90 material totals: 415 Hero's Wit, 11 Adventurer's Experience, 12 Wanderer's Advice, and 1,673,400 Mora.
- Exposed required EXP, granted EXP, wasted EXP, and Mora totals. Candidate results are explicitly labelled `CANDIDATE_DATA`, not verified production costs.
- Added regression coverage for all level checkpoints, full 1→20 and 1→90 totals, partial current-level EXP, ascension-cap restrictions, maximum level, malformed curves, and source-review state.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/data/catalogue/GenshinCharacterExperienceCurveCandidate.kt`
- `app/src/main/java/com/irminsul/codex/domain/analysis/CharacterExperienceCalculator.kt`
- `app/src/test/java/com/irminsul/codex/domain/analysis/CharacterExperienceCalculatorTest.kt`

**Commits:**
- `ffcaf73edaecba26a5c77a39e8a4cfeec11efcf9`
- `9187f4fdd33c910af2cd4115624e5d1e532eed14`
- `1bb0f273ca470527c1a6a50899366f7efda0860b`

**Verification:**
- CI #601 and #602 failed because the initial implementation rounded the entire requested level range as one batch, which did not match the published per-ascension-phase book counts. This was corrected; failures were not hidden.
- CI #603 passed all 310 unit tests, Debug APK build, Room schema snapshot verification, Debug APK signature verification, and artifact upload. Room instrumentation tests were skipped because this change does not modify Room schema or persistence.
- CI #603: https://github.com/berao609/Irminsul-codex/actions/runs/45922802196
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.603-vc605` (artifact ID `15165486192`).
- APK artifact SHA-256: `f1d5a22eec0f65d7922f5043b841f21d3ff4df839bbae9512caeb49d0cc6f6ce`.

**Physical-device verification:** Not required for this data/domain-only step; the new calculator is not yet connected to the Materials Planner UI.

**Remaining work:**
- Add rarity-specific weapon EXP curves and the corresponding weapon EXP/Mora/Enhancement Ore calculator.
- Complete source/license review before promotion into trusted calculation data.
- Connect reviewed character and weapon progression data to the automatic Materials Planner.

**Result:** Character EXP requirements can now be calculated deterministically for a target level range, including ascension-phase rounding and material totals. This is a tested candidate calculation foundation, not yet a user-facing or verified production calculator.


## Milestone 074 - 4-Star and 5-Star Weapon EXP Candidate Calculator

**Date recorded:** 2026-11-27  
**Scope:** Add ore-only weapon enhancement calculations for 4-star and 5-star weapons. Lower-rarity weapons and weapon fodder are explicitly outside this calculator's scope.

**Implemented:**
- Added separate 90-level cumulative EXP curves for 4-star and 5-star weapons.
- Adapted the 4-star and 5-star weapon EXP tables from the Genshin Impact Wiki, with CC BY-SA 3.0 attribution and share-alike notice. Both datasets remain `CANDIDATE` with `PENDING_REVIEW` permission; neither is promoted into trusted production costs.
- Added a deterministic calculator accepting weapon rarity, current level, current within-level EXP, target level, and current ascension-unlocked level cap.
- Enforced ascension caps at levels 20, 40, 50, 60, 70, 80, and 90.
- Calculated Mystic Enhancement Ore (104013), Fine Enhancement Ore (104012), Enhancement Ore (104011), and Mora (202).
- Limited enhancement input to ore materials. Weapon fodder EXP is intentionally excluded.
- Rounded ore EXP independently at each ascension phase because excess EXP cannot carry through a weapon level cap.
- Added tests for both complete curves, full level 1-to-90 totals, phase rounding, current partial EXP, ascension cap restrictions, maximum-level no-op behavior, malformed curves, and candidate provenance.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/data/catalogue/GenshinWeaponExperienceCurveCandidate.kt`
- `app/src/main/java/com/irminsul/codex/domain/analysis/WeaponExperienceCalculator.kt`
- `app/src/test/java/com/irminsul/codex/domain/analysis/WeaponExperienceCalculatorTest.kt`

**Implementation commit:**
- `4bdbd4db75fb42442a86677afe11d2d758caac0d`

**Source references:**
- 4-star weapon EXP: https://genshin-impact.fandom.com/wiki/Weapon_EXP/4-Star_Weapons
- 5-star weapon EXP: https://genshin-impact.fandom.com/wiki/Weapon_EXP/5-Star_Weapons
- Weapon Enhancement Material values: https://genshin-impact.fandom.com/wiki/Weapon_Enhancement_Material
- Item IDs cross-check: Enhancement Ore 104011, Fine Enhancement Ore 104012, Mystic Enhancement Ore 104013.

**Verification status at recording:**
- CI #605 is running: https://github.com/berao609/Irminsul-codex/actions/runs/45923403862
- Unit tests, Debug APK build, signature verification, and artifact upload are pending CI completion.
- No physical-device verification has been performed. This is a domain/data foundation and is not connected to the Materials Planner UI.

**Reference full-level ore-only totals (per-phase rounding):**
- 4-star: 6,042,650 required EXP; 600 Mystic Ore, 19 Fine Ore, 15 Enhancement Ore; 604,400 Mora; 1,350 excess EXP.
- 5-star: 9,064,450 required EXP; 903 Mystic Ore, 15 Fine Ore, 13 Enhancement Ore; 906,520 Mora; 750 excess EXP.

**Remaining work:**
- Complete CI verification and update this milestone with the result and artifact evidence.
- Complete source permission/review before any promotion into trusted calculation data.
- Integrate weapon EXP results with weapon ascension costs and the shared Materials Planner flow.
- Connect 4-star/5-star weapon selection and target-level controls to the user-facing calculator.

**Result:** A tested-in-progress candidate foundation exists for ore-only enhancement of 4-star and 5-star weapons. It does not yet constitute a complete user-facing automatic weapon calculator.

## Milestone 075 - Weapon Ascension Candidate Cost Calculator

**Date recorded:** 2026-11-27  
**Scope:** Add deterministic calculation for weapon ascension materials using the existing Genshin-DB candidate importer output. This increment does not approve candidate data or connect a production dataset to the Materials Planner.

**Implemented:**
- Added `WeaponAscensionCandidateCalculator` in the data/catalogue layer, reusing the existing `GenshinDbUpgradeCostCandidate` model and importer output.
- Restricted calculations to 4-star and 5-star weapons.
- Derives the ascension gates required by the selected target level and starts at the weapon's current ascension rank, so already-paid stages are not charged again.
- Aggregates Mora, weapon ascension materials, and enemy drops by item ID across the required stages.
- Preserves source material names for candidate display and rejects conflicting names for the same item ID.
- Returns explicit incomplete, invalid, overlapping, and quantity-overflow states. Partial material totals are withheld when required candidate stages are missing or invalid.
- Does not convert candidates into trusted `UpgradeCostRecord` values or bypass source permission and human review.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/data/catalogue/WeaponAscensionCandidateCalculator.kt` (new)
- `app/src/test/java/com/irminsul/codex/data/catalogue/WeaponAscensionCandidateCalculatorTest.kt` (new)

**Commits:**
- `043b84004db12d5941bd0d3997f7d3ada917bc45`
- `2c8a75a31bc76a6da850909605fbdd21e41c3348`

**Verification status:**
- CI #612: running at time of recording.
- CI #613: running at time of recording.
- Physical-device verification: not applicable to this data/domain-only increment; no visible UI behavior changed.

**Remaining work:**
- Provide a versioned offline candidate dataset for weapon ascension costs for 4-star and 5-star weapons.
- Resolve material display metadata and source attribution/reuse review without promoting unapproved data.
- Connect candidate weapon ascension costs with the ore-only EXP calculation in the Materials Planner, showing an explicit candidate/incomplete state.
- Allow target levels beyond the currently unlocked ascension cap only when the plan includes the required ascension stages, while retaining correct per-phase EXP rounding.
- Add an end-to-end regression test proving EXP ores, Mora, ascension materials, and enemy drops aggregate without double-counting Mora.

**Result:** A deterministic stage-aware weapon ascension candidate calculator now exists and is covered by regression tests. It is not yet wired to a bundled weapon-cost dataset or the user-facing calculator.

## Milestone 076 - Weapon EXP Plans Across Ascension Gates

**Date recorded:** 2026-11-27  
**Scope:** Allow the weapon EXP calculator to calculate a desired target level beyond the weapon's current ascension cap, while preserving the current level/EXP validation and per-phase ore rounding.

**Implemented:**
- Kept the weapon's current unlocked level cap separate from the planned target cap.
- Added `targetMaximumUnlockedLevel` to the weapon EXP request. It defaults to the current cap for compatibility with existing callers.
- Validates that both caps are supported ascension boundaries and that the planned cap does not move backward.
- Preserves current EXP validation against the actual current cap; a weapon already at its current level cap cannot claim additional within-level EXP.
- Updated the Materials Planner preview to derive the planned target cap from the requested target level, allowing plans such as level 20/20 to level 90.
- Preserves independent ore rounding at each ascension boundary.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/domain/analysis/WeaponExperienceCalculator.kt`
- `app/src/test/java/com/irminsul/codex/domain/analysis/WeaponExperienceCalculatorTest.kt`
- `app/src/main/java/com/irminsul/codex/presentation/WeaponEnhancementCalculatorSection.kt`

**Commits:**
- `8ffdcdb26ec73bedc5ed575d37a0f0ee8d26f62e`
- `009a61e385d6ce9f9360c83251e6cec82f486ba2`
- `621a56d11cfe2238b608a2545b594b87c3f37bfa`

**Verification status:**
- CI #615, #616, and #617 are running at time of recording.
- Physical-device verification is pending. The updated target-level behavior is visible in the Materials Planner.

**Remaining work:**
- Connect the candidate ascension calculator to a versioned offline weapon-cost dataset.
- Combine weapon EXP ores/Mora with weapon ascension materials and enemy drops in one result, avoiding duplicate Mora.
- Keep candidate source status visible until data rights and human review are resolved.
- Verify future-level planning on-device after a successful APK build.

**Result:** The weapon EXP calculator can now calculate ore requirements across planned ascension phases instead of incorrectly rejecting every target above the current cap. Ascension materials are not yet included in the displayed totals.


## Milestone 077 - Combined Weapon Upgrade Plan Regression Matrix

**Date recorded:** 2026-11-27  
**Scope:** Strengthen end-to-end regression coverage for the integrated 4-star/5-star weapon EXP and ascension candidate planner.

**Implemented:**
- Added target-level coverage for 20, 40, 50, 60, 70, 80, and 90 for both supported weapon rarities.
- Added combined-plan checks with current partial EXP for both rarities.
- Added repeated ascension material-ID aggregation coverage across multiple stages.
- Retained existing checks for Mora aggregation, completed ascension exclusion, missing-stage total withholding, invalid current EXP, conflicting material names, and quantity overflow.

**Files changed:**
- `app/src/test/java/com/irminsul/codex/data/catalogue/WeaponUpgradePlanCandidateCalculatorTest.kt`

**Verification:** CI and physical-device verification are pending. Record actual workflow run, artifact, and user device confirmation before marking either complete.

**Current boundary:**
- The combined weapon calculator is integrated into the Materials Planner.
- The current display provides material names, required quantities, optional owned quantities, and remaining quantities. Material icon/metadata presentation remains incomplete.
- Weapon cost records remain candidate data pending source permission and human review.
- Only 4-star and 5-star weapons are supported. Weapon fodder remains excluded.

**Next work:**
- Verify the regression suite and Debug APK workflow.
- Complete material icon/metadata presentation without fabricating missing metadata.
- Perform physical-device checks for cross-ascension plans and owned/remaining quantities.


## Milestone 078 - Offline Character Progression Candidate Dataset

**Date recorded:** 2026-11-27  
**Scope:** Bundle version-pinned character ascension and talent-cost source records for offline use. This milestone establishes the character progression dataset and loader; it does not yet connect the character calculator to the Materials Planner UI.

**Implemented:**
- Bundled all 126 English character records from Genshin-DB game-data snapshot 7.2, split across 15 numbered JSON assets for predictable Android asset loading.
- Included character ID, name, rarity, six ascension-stage cost arrays, and the matching talent-cost payload when the upstream record exists.
- Bundled talent costs for 122 character records. Four Traveler variants (Aether, Lumine, Manekin, and Manekina) have no matching per-character talent JSON in this source snapshot; their talent data remains explicitly missing rather than fabricated.
- Pinned every asset chunk to upstream commit `8e5badd60b5d894284ccc927a56c9683529d2592`; the repository rejects chunks with a different game version, source commit, or schema version.
- Added `BundledCharacterProgressionCandidateRepository` to load candidate records from Android assets and pass them through the existing `GenshinDbProgressionCandidateImporter`.
- Preserved separate Normal Attack, Elemental Skill, and Elemental Burst candidate tracks using the existing talent-slot mapping.
- Added repository tests for all three talent slots, immutable source provenance, missing Traveler talent data, unknown characters, duplicate IDs, malformed records, and version/source mismatch.
- Added explicit Genshin-DB MIT attribution and redistribution notice.

**Data inventory:**
- Character records: 126
- Records with matching talent-cost payloads: 122
- Records without matching talent-cost payloads: 4
- Asset chunks: 15
- Combined serialized asset size: approximately 405 KB

**Source and permission:**
- Source: https://github.com/theBowja/genshin-db
- Immutable source commit: `8e5badd60b5d894284ccc927a56c9683529d2592`
- Source license: MIT; copyright and permission notice retained in `app/src/main/assets/genshin_db/GENSHIN_DB_ATTRIBUTION.md`.
- These are still candidate game facts. The MIT license permits redistribution of the source dataset but does not independently verify game values. No automatic promotion into trusted production costs occurs.

**Files changed:**
- `app/src/main/assets/genshin_db/characters_7_2_01.json` through `characters_7_2_15.json`
- `app/src/main/assets/genshin_db/GENSHIN_DB_ATTRIBUTION.md`
- `app/src/main/java/com/irminsul/codex/data/catalogue/BundledCharacterProgressionCandidateRepository.kt`
- `app/src/test/java/com/irminsul/codex/data/catalogue/BundledCharacterProgressionCandidateRepositoryTest.kt`

**Commits:**
- Dataset chunks: `c5c6065`, `b62f11b`, `54ec93c`, `01d2804`, `a991682`, `8d6fa00`, `679c85b`, `7b9e8b6`, `7f3ebbd`, `230f8c2`, `0313925`, `c0e9d84`, `bd35106`, `f1a2e03`, `43549d7`
- Repository: `64ca4c8`
- Tests: `71f8b2c`, `3cfcdb9`
- Attribution: `8a1a28c`

**Verification status:**
- Source inventory verified against the pinned upstream Git tree: 126 character records, 122 matching talent-cost payloads, no duplicate character IDs, and all 15 chunks share the same version and source commit.
- CI #691 failed because a weapon combined-plan regression test expected 100 ascension Mora while its fixture supplied 10. The fixture-derived expectation was corrected in commit `18b293ad4de5ab26dadae8121bf5c45d61abe65d`; the failure was not hidden.
- CI #692 passed: 375 unit tests, Debug APK assembly, Room schema snapshot verification, Debug APK signature verification, and artifact uploads. Room instrumentation tests were skipped because no Room schema or persistence code changed.
- CI #692: https://github.com/berao609/Irminsul-codex/actions/runs/45934729313
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.692-vc694` (artifact ID `15170345820`, archive SHA-256 `12fface025e0316ba16559cb3965dea2c07f1e8e62c4297913946212df91077a`).
- Room schema artifact: `irminsul-room-schema-v2-run-692` (artifact ID `15170480105`).
- Physical-device verification: not required for this data/loader-only milestone; no Materials Planner UI behavior changed.

**Remaining work:**
- Verify repository tests and the Debug APK workflow.
- Integrate character level/ascension and independent talent calculations into the shared Materials Planner plan.
- Keep missing Traveler talent records incomplete until a separately sourced and reviewed dataset is available.
- Preserve the separate character EXP curve and its existing candidate provenance.

**Result:** The app now has a version-pinned offline candidate source and repository for character ascension and talent costs. This is a data foundation, not yet a completed user-facing automatic character calculator.



## Milestone 079 - Character Level and Ascension Planner Integration

**Date recorded:** 2026-11-27  
**Scope:** Connect the version-pinned offline character progression candidates and existing character EXP/ascension calculations to the Materials Planner. This increment covers character level progression only; talent costs remain a separate milestone.

**Implemented:**
- Added `CharacterEnhancementCalculatorSection` using the existing `CharacterUpgradePlanCandidateCalculator`.
- Loads the selected character's version-pinned progression candidates from Android assets.
- Uses the account snapshot's current ascension rank and the planner's current/target level fields.
- Adds a persisted current EXP-within-level input.
- Combines character EXP, ascension materials, and Mora using the existing candidate plan calculator.
- Displays material requirements with optional owned quantities and remaining quantities.
- Withholds combined totals when the source dataset cannot load, the ascension rank is missing, or required ascension data is incomplete/invalid.
- Keeps all costs explicitly labelled as candidate data; no promotion to trusted production costs occurs.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/presentation/CharacterEnhancementCalculatorSection.kt` (new)
- `app/src/main/java/com/irminsul/codex/presentation/MaterialsPlannerSection.kt`

**Commits:**
- `7bee675c36650b2b48fc137ef79a43aa727e8b4b`
- `7d2891eaadaa1d762ffd0976652fc581772a0e73`
- `5d5d87b8df859db5778adfe32d3676b996f09414`

**Verification status:**
- Source integration is committed.
- GitHub Actions verification is pending; do not treat this milestone as CI-verified until the new Debug workflow completes successfully.
- Physical-device verification is pending. Test current EXP, cross-ascension targets, missing ascension rank, offline loading, owned/remaining quantities, and preservation of existing weapon/manual plans.

**Remaining work:**
- Add and verify automated coverage for the character UI integration where the existing test architecture supports it.
- Complete independent Normal Attack, Elemental Skill, and Elemental Burst cost calculations in the next milestone.
- Combine all character level and talent requirements into one material list without double-counting Mora.
- Keep Traveler talent requirements incomplete until separately sourced and reviewed data is available.

**Result:** Character level/ascension candidate calculations are now connected to the Materials Planner UI. CI and physical-device verification remain pending.


## Milestone 080 - Automatic Character Materials Preview and Planner Separation

**Date recorded:** 2026-11-27  
**Scope:** Deliver the requested character-first enhancement workflow using the bundled version-pinned candidate dataset. Keep candidate costs explicitly distinct from approved production cost records.

**Implemented:**
- Added `CharacterFullUpgradePlanCandidateCalculator` to combine the existing character EXP/ascension plan with independent Normal Attack, Skill, and Burst talent tracks.
- Talent costs are selected by character ID, game version, talent slot, and exact level segment. Unchanged tracks incur no cost.
- Missing talent stages withhold the entire combined total. Invalid/overlapping records and quantity overflow also withhold totals.
- Mora and duplicate material IDs are aggregated across character levels, ascension, and all three talent tracks.
- Character ascension rank mapping now falls back from the character detail payload to the outer character payload when the nested base object omits `promote_level`.
- Current talent levels are prefilled from account skill records when their types are available; target levels default to the current levels.
- Character selection is now separate from weapon selection. The character calculator no longer includes a planned-weapon editor.
- Weapon selection and weapon level controls now have their own independent planner section.
- Manual material tracking is collapsed and labelled optional. Existing stored rows are retained.
- Character material requirements display source-derived material names, required quantities, optional owned quantities, and remaining quantities.
- Current character EXP adjustment is optional and defaults to zero.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/data/catalogue/CharacterFullUpgradePlanCandidateCalculator.kt` (new)
- `app/src/main/java/com/irminsul/codex/presentation/CharacterEnhancementCalculatorSection.kt`
- `app/src/main/java/com/irminsul/codex/presentation/MaterialsPlannerSection.kt`
- `app/src/main/java/com/irminsul/codex/data/mapper/AccountExportMapper.kt`
- `app/src/test/java/com/irminsul/codex/data/catalogue/CharacterFullUpgradePlanCandidateCalculatorTest.kt` (new)
- `app/src/test/java/com/irminsul/codex/data/mapper/AccountExportMapperTest.kt`
- `README.md`

**Important data status:**
- The calculator currently previews costs from bundled Genshin-DB 7.2 candidate records and the character EXP curve.
- This implementation does not promote candidate data into approved production `UpgradeCostRecord` records.
- Traveler talent data and any missing candidate stages remain incomplete; combined totals are withheld rather than shown as complete.

**Verification status:**
- CI runs #709, #710, #711, and #712 passed.
- CI run #713 passed for the milestone documentation commit.
- CI run #714 passed unit tests, built the Debug APK, verified its signature, and uploaded the artifact. Room instrumentation tests were skipped by the workflow condition.
- Latest calculator code commit: `d8ae884367b063492575872786e448404fe2c61c`.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.714-vc716` (artifact ID `15172589288`).
- Earlier failures #703, #705, #706, and #707 were tied to an initial test assertion, an outdated composable call, and intermediate UI brace/structure edits. Later runs #704 and #708 passed after corrections.
- Physical-device testing is pending. CI #714 produced the Debug APK; no device installation or functional test has been confirmed.

**Remaining work:**
- Verify current talent-level mapping against real exported account snapshots.
- Review Genshin-DB source permissions, cost correctness, and material metadata before any production approval.
- Test level gates, all three talent tracks, Mora aggregation, owned/remaining values, Traveler missing data, offline use, and independent weapon planning on device.

**Result:** The Materials Planner now has a character-first automatic cost preview and a separate weapon workflow. Production approval and physical-device verification are not complete.


## Milestone 081 - Tools Navigation and Materials Calculator Relocation

**Date recorded:** 2026-11-27

**Implemented:**
- Added a dedicated Tools destination to bottom navigation: Home, Visuals, central AI action, Tools, Account.
- Relocated Materials Calculator from Visuals to Tools. The existing planner component, calculations, preference-backed storage, and collapse state are retained.
- Removed the duplicate Account shortcut from the top app header. Account remains available from the bottom navigation, which opens the credential-management context as before.
- Updated README navigation description.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `README.md`

**Verification status:** User confirmed physical-device testing passed. CI verification remains unconfirmed in the currently available evidence.

**Device checklist:**
1. Confirm navigation order is Home, Visuals, AI, Tools, Account.
2. Open Tools and confirm Materials Calculator is available.
3. Confirm calculator character and weapon flows still work, including saved manual tracking rows.
4. Confirm Visuals no longer contains Materials Calculator.
5. Confirm only one Account entry exists in navigation/header and it still opens account settings and credentials.
6. Confirm switching tabs preserves expected state and scrolling.

**Result:** The calculator now lives in Tools, while Visuals remains focused on account analysis and visual information. No calculation logic or saved preference keys were intentionally changed.


## Milestone 082 - Dashboard Metric Presentation Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue Batch 6 / AUD-002 by moving shared dashboard metric presentation helpers out of the oversized activity source.

**Implemented:**
- Extracted AssessmentBar, MetricTile, and DailyNoteMetricRow composables, plus formatDuration, into presentation/DashboardMetrics.kt.
- Updated MainActivity imports while preserving existing call sites and helper behavior.
- No data, state ownership, calculations, persistence, navigation, or calculator behavior intentionally changed.

**Files changed:**
- app/src/main/java/com/irminsul/codex/MainActivity.kt
- app/src/main/java/com/irminsul/codex/presentation/DashboardMetrics.kt
- docs/CODEBASE_OPTIMIZATION_AUDIT.md
- docs/PROGRESS_MILESTONES.md

**Verification status:** User confirmed Debug CI run #727 succeeded and the app was tested on a physical device. The user-confirmed device checklist passed. No workflow run ID, artifact ID, or artifact checksum was independently retrieved in this session. No performance improvement is claimed.

**Device verification:** User confirmed the app was tested and passed after CI #727. Exact device checklist details were not separately itemized in the confirmation.


## Milestone 083 - Bottom Navigation Presentation Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue Batch 6 / AUD-002 by extracting the bottom navigation and central AI action from MainActivity.

**Implemented:**
- Moved the bottom navigation bar and raised AI action into CodexBottomNavigationBar.kt.
- Moved CodexTab into the same presentation file.
- Kept selected-tab state and Account credential-panel behavior in CodexScreen through callbacks.
- Preserved the existing Home, Visuals, AI, Tools, Account arrangement.

**Files changed:**
- app/src/main/java/com/irminsul/codex/MainActivity.kt
- app/src/main/java/com/irminsul/codex/presentation/CodexBottomNavigationBar.kt
- docs/CODEBASE_OPTIMIZATION_AUDIT.md
- docs/PROGRESS_MILESTONES.md

**Verification status:** CI #738 succeeded. User confirmed physical-device navigation testing passed, including navigation order, tab selection, central AI action, and Account access. No performance improvement is claimed.


## Milestone 084 - Visual Dashboard Presentation Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue Batch 6 / AUD-002 by extracting the Visual Dashboard composable from MainActivity without changing analysis behavior.

**Implemented:**
- Extracted Visual Dashboard rendering into `presentation/VisualDashboardSection.kt`.
- MainActivity delegates to `VisualDashboardSection(accountExportState.intelligenceOverview)`.
- Preserved no-analysis state, assessment bars, recommendation/gap metrics, account-data coverage, and warning text.
- Reused the shared `AssessmentBar` and `MetricTile` presentation helpers.
- Kept state ownership, analysis logic, account mapping, navigation, and calculator behavior unchanged.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/VisualDashboardSection.kt`

**Commits:**
- Extraction/integration and source corrections: `9f730e5c510b73bfe5600107c91b9a1d7724268b`, `9840297f1afab04fb8962da2055ba7639f4fdf78`, `82c764635e4166005e4f42754964c30bc835acb9`, `43f0b5de84d7c52b126439cf8b8cf164650aca48`, `2e2d505956a1c9e21ea4e4ecf3508b785ec891cc`, `6bdceb471321b727bee9f22b0aeb97167e2c6273`, `756f2fa6d7a23ae8e8c26a23b541bff8d7dbe937`.

**Verification:**
- CI #744 failed on a missing Compose `dp` import; corrected in the final commit.
- CI #745 passed unit tests, Debug APK build, Room schema verification, APK signature verification, and artifact upload. Room instrumentation tests were skipped by workflow conditions.
- Workflow: https://github.com/berao609/Irminsul-codex/actions/runs/45948947383
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.745-vc747` (ID `15175399449`; SHA-256 `442e74e737e89fcdce84800ae0b349e6aa77897ccb712f9f6fc272276295b266`).
- Room schema artifact: `irminsul-room-schema-v2-run-745` (ID `15175592695`).
- Physical-device verification: pending user installation and Visuals dashboard checklist.

**Result:** Visual Dashboard presentation is extracted and CI-verified. Device verification remains outstanding; no performance improvement is claimed.


## Milestone 085 - Home-Only Visual Dashboard and Coverage Panel Removal

**Date recorded:** 2026-11-27

**Scope:** Correct dashboard placement after device observation and simplify the dashboard presentation.

**Implemented:**
- Restricted VisualDashboardSection to the Home destination. It no longer appears on Visuals, Tools, or Account.
- Removed the Account data coverage card from the Visual Dashboard.
- Preserved the Home dashboard's no-analysis state, character assessment, recommendation count, and confirmed-gap metrics.
- Kept Visuals account analysis and its existing recommendation/character content, Tools calculator, Account functions, and underlying analysis data unchanged.
- Updated README navigation and feature descriptions.

**Files changed:**
- app/src/main/java/com/irminsul/codex/MainActivity.kt
- app/src/main/java/com/irminsul/codex/presentation/VisualDashboardSection.kt
- README.md

**Commits:**
- 7e0f86b36126654f5bd2b35a1b8122928a8424e0 - restrict dashboard to Home.
- a9fc65af97ea30c15a8a4b80f5a89c633a11bbf0 - remove dashboard coverage card.
- 901100de487339238d8ec8957919ea9cfecc7e13 - update README.

**Verification status:** CI #752 passed for the corrected source. User confirmed successful physical-device testing.

**Device checklist:**
1. Home shows the Visual Dashboard exactly once.
2. Dashboard has no Account data coverage card.
3. Visuals has no Visual Dashboard; account analysis, recommendations, character showcase, and other Visuals content remain.
4. Tools has no Visual Dashboard; Materials Calculator remains available and usable.
5. Account has no Visual Dashboard; account settings and export remain available.
6. Switch between all destinations and confirm content and scrolling remain correct.

**Result:** Dashboard presentation is limited to Home and no longer includes the account data coverage panel. Automated and physical-device verification are pending.


## Milestone 086 - Home Daily Note Presentation Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue Batch 6 / AUD-002 by extracting the Home Daily Note presentation from MainActivity.

**Implemented:**
- Added `presentation/HomeDailyNoteSection.kt` for the Daily Note empty state and populated Resin, expedition, Realm Currency, commission, weekly discount, and refresh UI.
- MainActivity delegates the Daily Note content to the extracted component.
- Kept retrieval, loading, credentials, export state, and refresh action ownership in CodexScreen.
- Preserved nullable-value display, Resin progress/recovery rules, button enablement, and the existing loading-indicator position.
- No API, parsing, persistence, navigation, or calculator behavior was intentionally changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/HomeDailyNoteSection.kt`

**Commits:**
- `816288d4847ace9ec2c74eeba1b46ba1c5492370`
- `f12e48f2eb10738d47397d3c7f396fd1f611a81a`
- `33c360f31862361630c9b0fb306df85f8598b89a`
- `5a8884338f7f8cd86763fa41552352833c3288c4`

**Verification status:** CI #756 passed unit tests, Debug APK assembly, Room schema verification, APK signature verification, and artifact upload. Room instrumentation tests were skipped by workflow conditions. Physical-device verification pending.

**CI evidence:** [Workflow #756](https://github.com/berao609/Irminsul-codex/actions/runs/45951308270). Debug APK artifact `irminsul-codex-debug-v1.0.0-dev.756-vc758` (ID `15176821455`, SHA-256 `b3db8ba816b58d7b4cc540c29f2f4e120b83a5b678d02a40c2be062ac2ccd12e`).

**Device checklist:**
1. Confirm loading indicator, empty state, and Sync Daily Note action.
2. Confirm Resin value, maximum, progress, recovery, and full-resin states.
3. Confirm expedition, Realm Currency, commission, and weekly discount values/statuses, including unavailable values.
4. Confirm Refresh Notes and loading/disabled behavior.
5. Confirm other tabs and their existing features remain unaffected.

**Result:** Daily Note presentation is separated from MainActivity while state and data retrieval remain screen-owned. CI #756 passed, and the user confirmed successful physical-device testing.


## Milestone 087 - Account Data Export Presentation Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue Batch 6 / AUD-002 by extracting the Account Data Export card presentation from MainActivity.

**Implemented:**
- Added `presentation/AccountDataExportSection.kt`.
- MainActivity delegates the Account Data Export card while passing existing progress, export status, exporting/restoring flags, and the export callback.
- Kept UID/cookie checks, export orchestration, ViewModel interaction, document-picker handling, JSON writing, and state ownership in CodexScreen.
- Preserved Account-tab visibility conditions, progress/status messaging, Save JSON label, and disabled behavior.
- No export format, account retrieval, persistence, credentials, navigation, or calculator logic was intentionally changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/AccountDataExportSection.kt`

**Verification status:** CI #767 passed unit tests, Debug APK assembly, Room schema verification, APK signature verification, and artifact upload. Room instrumentation tests were skipped by workflow conditions. User confirmed successful physical-device testing.

**Device checklist:**
1. Confirm the Account Data Export card appears only on Account when both UID and cookie are present.
2. Confirm it is hidden when credentials are missing.
3. Confirm progress and export status messages remain visible at the correct times.
4. Confirm Save JSON is disabled during export and snapshot restoration, and shows Extracting... while exporting.
5. Confirm successful save and document-picker cancellation still report the correct result.
6. Confirm Home, Visuals, Tools, navigation, and Materials Calculator remain unaffected.

**Result:** Account Data Export presentation is separated from MainActivity. Export behavior remains screen-owned and unchanged. CI #767 passed, and the user confirmed successful physical-device testing.


## Milestone 088 - Centered First-Run Sign-In

**Date recorded:** 2026-11-27

**Scope:** Simplify the first-launch sign-in experience.

**Implemented:**
- Added `presentation/InitialSignInSection.kt` with a centered, width-constrained sign-in card.
- The initial Home screen now shows only the Irminsul Codex heading, short connection guidance, and the Sign in with HoYoLAB action, plus relevant loading or authentication error feedback.
- Removed the Genshin UID field, HoYoLAB cookie field, divider, and Save & Sync button from the first-run card.
- Kept manual UID/cookie entry and Save & Sync available from the Account destination as a recovery/fallback path.
- Preserved embedded HoYoLAB WebView authentication, secure cookie storage, automatic UID detection, and Daily Note synchronization.
- Updated the UID-detection failure message to direct the user to Account for manual entry.
- No credential format, storage, API, account retrieval, navigation order, or calculator behavior was intentionally changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/InitialSignInSection.kt`
- `README.md`

**Verification status:** CI #774 passed unit tests, Debug APK assembly, Room schema verification, APK signature verification, and artifact upload. Room instrumentation tests were skipped by workflow conditions. User confirmed successful physical-device testing.

**Device checklist:**
1. On a fresh install, confirm the sign-in card is centered in the available screen area.
2. Confirm the initial card has no UID field, cookie field, divider, or Save & Sync button.
3. Confirm Sign in with HoYoLAB opens the existing WebView login and automatic UID detection/sync still works.
4. Confirm loading and authentication error feedback remains readable.
5. Confirm Account still exposes manual UID/cookie entry and Save & Sync as fallback.
6. Confirm existing signed-in startup, navigation, account export, and Materials Calculator remain unaffected.

**Result:** First-run sign-in presentation is simplified; manual credential recovery remains available under Account. CI #774 passed and the user confirmed successful physical-device testing.


## Milestone 089 - Lineup Simulator Relocation to Tools

**Date recorded:** 2026-11-27

**Scope:** Move the existing Lineup Simulator from Visuals to Tools without changing its behavior.

**Implemented:**
- Removed the collapsible Lineup Simulator card and content from Visuals.
- Added the same collapsible simulator to Tools, alongside Materials Calculator.
- Preserved the four-character selection limit, selection order, removal/toggle behavior, roster-derived character data, element summary, and stale/partial roster warnings.
- Kept lineup selection and expansion state screen-owned. No persistence or team-analysis capability was added.
- Updated README navigation and feature descriptions.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `README.md`
- `docs/PROGRESS_MILESTONES.md`

**Verification status:** CI #778 passed. The user confirmed successful physical-device testing.

**Device checklist:**
1. Confirm Lineup Simulator is absent from Visuals.
2. Confirm Lineup Simulator appears in Tools alongside Materials Calculator.
3. Confirm expanding/collapsing the simulator works.
4. Confirm owned character portraits, four slots, add/remove behavior, and element summary work.
5. Confirm stale/partial roster warnings remain visible when applicable.
6. Confirm switching tabs preserves the expected selection state.
7. Confirm Materials Calculator and the remaining tabs still work.

**Result:** Lineup Simulator is now grouped with the app's calculators and utilities in Tools. CI #778 passed and the user confirmed successful physical-device testing. Its existing selection behavior and trust limitations are unchanged.


## Milestone 090 - Account Settings and Credentials Presentation Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue AUD-002 by separating Account credential and settings presentation from MainActivity.

**Implemented:**
- Added `presentation/AccountSettingsSection.kt` with `AccountCredentialsSection` for manual UID/cookie entry, HoYoLAB sign-in, credential-unlock error, clear action, and Save & Sync.
- Added `AccountSettingsSection` for appearance selection and account/session management.
- Moved `ThemeMode` into the presentation package while preserving preference keys and System/Light/Dark behavior.
- Kept credential values, visibility, loading/export state, secure storage, and all orchestration in CodexScreen.
- Preserved existing visibility conditions, labels, callbacks, and disabled states.
- No credential storage, account retrieval, export, navigation, or calculator behavior was intentionally changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/AccountSettingsSection.kt`

**Verification status:** Source extraction committed; Debug CI and physical-device verification pending.

**Device checklist:**
1. Verify System, Light, and Dark selection and persistence.
2. Verify HoYoLAB sign-in, manual UID/cookie entry, credential error/clear, and Save & Sync.
3. Verify sign-out clears credentials while retaining saved account snapshots.
4. Verify Manage/Hide HoYoLAB account controls and Save JSON.
5. Confirm Home, Visuals, Tools, Materials Calculator, and Lineup Simulator remain unchanged.

**Boundary:** Structural extraction only; no performance improvement is claimed.


## Milestone 091 - AI Assistant Sheet Presentation Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue AUD-002 by extracting the existing AI placeholder bottom sheet from MainActivity.

**Implemented:**
- Added `presentation/AiAssistantSheet.kt`.
- MainActivity delegates the sheet UI and passes its existing remembered SheetState and dismissal callback.
- Kept sheet visibility, state ownership, and bottom-navigation launch handling in CodexScreen.
- Preserved all existing placeholder copy, disabled input, cookie privacy statement, styling, insets, and dismissal behavior.
- Did not add AI provider integration or active chat behavior.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/AiAssistantSheet.kt`

**Verification status:** Source extraction committed; Debug CI and physical-device verification pending.

**Device checklist:**
1. Open the centered AI action and verify the sheet content and layout.
2. Verify Close and swipe dismissal.
3. Verify keyboard/navigation-bar insets.
4. Confirm Home, Visuals, Tools, Account, Materials Calculator, and Lineup Simulator remain functional.

**Boundary:** Structural extraction only; AI remains inactive.


## Milestone 092 - Home Account Overview Presentation Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue Batch 6 / AUD-002 by extracting the Home account identity and saved-snapshot summary cards from MainActivity.

**Implemented:**
- Added `presentation/HomeAccountOverviewSection.kt` for the Adventurer account and Account snapshot cards.
- MainActivity derives roster count and passes UID, detected server, snapshot timestamp, and the Analysis callback.
- Kept snapshot state, roster interpretation, server detection, and navigation ownership in CodexScreen.
- Preserved existing card visibility, text, unavailable-roster handling, timestamp, styling, and Analysis navigation.
- No retrieval, persistence, account analysis, navigation structure, or calculator behavior was intentionally changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/HomeAccountOverviewSection.kt`
- `docs/CODEBASE_OPTIMIZATION_AUDIT.md`
- `docs/PROGRESS_MILESTONES.md`

**Verification status:** CI #799 initially failed during Kotlin compilation because the snapshot timestamp argument was `Instant?` while the presentation component expects `String?`. Corrected the call site in commit `9d4c66ca656d8d0b764597e4be73f90b557f59db`; CI #800 passed. Physical-device verification remains pending.

**Device checklist:**
1. Verify the Adventurer account card with and without configured credentials.
2. Verify the Account snapshot card appears only when a snapshot exists.
3. Verify roster count, unavailable-roster fallback, and last-retrieved timestamp.
4. Tap Analysis and confirm it opens Visuals.
5. Confirm Daily Note, Visual Dashboard, Tools, Account, Materials Calculator, and Lineup Simulator remain functional.

**Boundary:** Structural presentation extraction only. No performance improvement is claimed.


## Milestone 093 - Tools Destination Presentation Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue AUD-002 by extracting the Tools destination container from MainActivity.

**Implemented:**
- Added `presentation/ToolsSection.kt` for the Tools heading, Materials Calculator card, and Lineup Simulator card/container.
- MainActivity supplies the existing character roster, SharedPreferences, expansion state, active lineup selection, roster freshness/completeness flags, and callbacks.
- Kept state ownership, roster interpretation, and lineup selection-rule orchestration in CodexScreen.
- Reused the existing MaterialsPlannerSection and LineupSimulatorSection without changing their internal behavior.
- Preserved labels, descriptions, expand/collapse affordances, selected-character count, and existing selection behavior.
- No calculator logic, account retrieval, persistence, navigation, or lineup-analysis capability was changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/ToolsSection.kt`
- `docs/CODEBASE_OPTIMIZATION_AUDIT.md`
- `docs/PROGRESS_MILESTONES.md`

**Verification status:** CI #801 exposed a missing `RoundedCornerShape` import; fixed in `081faa98399ae2d0f3918bf4f236bacd08e16f34`. CI #805 passed unit tests, Debug APK assembly, Room schema verification, APK signature verification, and artifact upload. Room instrumentation tests were skipped by workflow conditions. Debug APK: `irminsul-codex-debug-v1.0.0-dev.805-vc807` (artifact ID `15181947395`). Physical-device verification pending.

**Device checklist:**
1. Open Tools and expand/collapse Materials Calculator.
2. Verify character and weapon selectors, level/talent inputs, and generated material preview remain unchanged.
3. Expand/collapse Lineup Simulator.
4. Verify selected count, four-character limit, add/remove behavior, and element summary.
5. Verify stale/partial roster warnings remain visible when applicable.
6. Confirm Home, Visuals, Account, navigation, and AI placeholder sheet remain functional.

**Boundary:** Structural presentation extraction only. No performance improvement or new calculator/lineup capability is claimed.


## Milestone 094 - Visuals Destination Presentation Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue AUD-002 Batch 6 by extracting the Visuals destination composition from MainActivity.

**Implemented:**
- Added `presentation/VisualsSection.kt` for Account Chronicle, Character Showcase (including its empty state), and Account Analysis composition.
- MainActivity passes the existing account snapshot, intelligence overview, loading/error flags, credential availability, selected character, recommendation expansion state, and callbacks.
- Kept screen state, snapshot ownership, credential checks, analysis invocation, and navigation in CodexScreen.
- Preserved section order, stale-roster display, empty-state copy, Account Analysis credential gating, and its existing character input behavior.
- No retrieval, mapping, persistence, analysis rules, navigation, or calculator behavior was intentionally changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/VisualsSection.kt`
- `docs/CODEBASE_OPTIMIZATION_AUDIT.md`
- `docs/PROGRESS_MILESTONES.md`

**Commits:**
- `c573d32169aa2d117c25d04faf8e1345989efcfd` - add Visuals destination presentation.
- `92319ddc68a925f0d63c71fe9062c94853215ed9` - delegate Visuals composition from MainActivity.

**Verification status:** Source changes committed. Debug CI and physical-device verification pending.

**Device checklist:**
1. Verify Account Chronicle overview and World Exploration & Collectibles.
2. Verify current and previous Spiral Abyss and Imaginarium Theater sections.
3. Verify Character Showcase roster, selection, and no-roster empty state.
4. Verify Account Analysis refresh, loading/error feedback, recommendations, and credential visibility conditions.
5. Confirm Home, Tools, Account, navigation, and AI placeholder remain functional.

**Boundary:** Structural presentation extraction only. No performance improvement is claimed.


## Milestone 095 - Shared Screen Feedback Presentation Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue AUD-002 Batch 6 by extracting shared screen feedback presentation from MainActivity.

**Implemented:**
- Added `presentation/ScreenFeedbackSections.kt` with `ScreenErrorBanner` and `HomeInitialLoadingIndicator`.
- MainActivity delegates the existing shared error card and Home initial loading indicator.
- Preserved error precedence, banner colors/shape/text/padding, and original placement.
- Preserved the exact Home loading condition and indicator alignment/spacing.
- Kept error/loading state ownership and all retrieval/navigation behavior in CodexScreen.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/ScreenFeedbackSections.kt`
- `docs/CODEBASE_OPTIMIZATION_AUDIT.md`
- `docs/PROGRESS_MILESTONES.md`

**Commits:**
- `45123d072e4676c3c6fb32e2d0307d3acb06cb53` - add shared feedback presentation.
- `39e0901c3ddda2cf69c1aefd68899e2fd5e53943` - delegate feedback rendering from MainActivity.

**Verification status:** CI #811 (run `45966052224`) and CI #812 (run `45966055782`) passed. Unit tests, Debug APK assembly, Room schema verification, APK signature verification, and artifact upload passed; Room instrumentation tests were skipped by workflow conditions. Integration Debug APK: `irminsul-codex-debug-v1.0.0-dev.812-vc814` (artifact ID `15182921675`). The user confirmed all physical-device checks clear, including loading feedback, error display/precedence, banner styling/placement, and all destinations.

**Boundary:** Structural presentation extraction only. No performance improvement is claimed.


## Milestone 096 - Home Destination Composition Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue AUD-002 Batch 6 by extracting the Home destination composition from MainActivity.

**Implemented:**
- Added `presentation/HomeSection.kt` to compose the existing Home account overview, Daily Note, and Visual Dashboard sections in their original order.
- MainActivity continues to derive and own roster count, server name, snapshot timestamp, credential availability, and all screen state.
- Passed existing data and callbacks to HomeSection; UID/cookie, snapshot, Daily Note, loading/error, and intelligence state plus refresh/navigation orchestration remain in CodexScreen.
- Reused existing presentation components without changing their internal behavior.
- Preserved the Home destination condition, loading indicator placement, snapshot visibility, Daily Note sync/refresh behavior, and Visual Dashboard display.
- No retrieval, mapping, persistence, analysis, navigation structure, or calculator behavior was changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/HomeSection.kt`

**Commits:**
- `760a08d6e08ef05fbbc4f1ff9e2e041245be272d` - extract Home destination composition.
- `cf308f82ec5b571e8154e2f7a17baefce1e66eac` - delegate Home composition from MainActivity.

**Verification status:** CI #815 (run `45966978956`) and CI #816 (run `45966984614`) passed. Unit tests, Debug APK assembly, Room schema verification, APK signature verification, and artifact upload passed; Room instrumentation tests were skipped by workflow conditions. Integration Debug APK: `irminsul-codex-debug-v1.0.0-dev.816-vc818` (artifact ID `15182808332`). User confirmed all physical-device checks clear, including account overview, Daily Note states/refresh, Visual Dashboard visibility, and other destinations/tools.

**Boundary:** Structural presentation extraction only. No performance improvement is claimed.


## Milestone 097 - Account Destination Composition Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue AUD-002 Batch 6 by extracting Account destination composition from MainActivity.

**Implemented:**
- Added `presentation/AccountSection.kt` to compose the existing credentials panel, JSON export panel, and Account & settings section.
- MainActivity continues to own UID/cookie values, credential visibility, theme state, export progress/status, and all callbacks.
- Passed existing visibility conditions explicitly, preserving credential and export gating.
- Reused existing account presentation components without changing their internal UI or behavior.
- Preserved the original content order and kept sign-in, credential storage, export, theme persistence, and navigation orchestration in CodexScreen.
- No credential storage, account retrieval, export mapping, persistence, or calculator behavior was changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/AccountSection.kt`

**Commits:**
- `711efc64ced18833a16a079a66e89fbe28274a23` - extract Account destination composition.
- `9d140637c24244cf17aee59b0a07fb23842264ba` - delegate Account composition from MainActivity.

**Verification status:** Cleared. Component CI #819 passed. Initial integration CI #820 and documentation CI #821/#822 failed on a stale duplicate `AccountSettingsSection` reference in MainActivity. Removed the duplicate block in fix commit `13950fa0f122d233e6f43b238de5a3e0ecd530aa`. Corrected integration CI #823 (run `45968107535`) passed unit tests, Debug APK assembly, Room schema verification, APK signature verification, and artifact upload. Room instrumentation tests were skipped by workflow conditions. Debug APK: `irminsul-codex-debug-v1.0.0-dev.823-vc825` (artifact ID `15184221465`). User confirmed successful physical-device testing and all clear.

**Boundary:** Structural presentation extraction only. No performance improvement is claimed.



## Milestone 098 - Screen Header Presentation Extraction

**Date recorded:** 2026-11-27

**Scope:** Continue AUD-002 Batch 6 by extracting the existing app title and UID/server identity header.

**Implemented:**
- Added `presentation/CodexScreenHeader.kt` for the title and optional UID/server line.
- MainActivity retains UID ownership and server-label derivation.
- Preserved existing text, typography, colors, alignment, spacing, and conditional identity display.
- No navigation, account state, retrieval, or persistence behavior changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CodexScreenHeader.kt`

**Commits:**
- `752f6be52ea92866384937a90ee8cdf9bd34510d` - add header component.
- `dde97640026e75ed78f3bedf566a85882660bc4f` - integrate header component.

**Verification status:** User confirmed physical-device testing passed. CI verification remains unconfirmed in the currently available evidence.

**Device checklist:**
1. Confirm the title appears correctly.
2. Confirm UID/server line appears only when UID is available and displays the correct server.
3. Confirm all destinations, bottom navigation, and AI entry remain functional.

**Boundary:** Structural presentation extraction only. No performance improvement is claimed.


## Milestone 099 - Theme Color-Scheme Presentation Extraction

**Date recorded:** 2026-11-27  
**Development phase:** Batch 6 - AUD-002 incremental UI responsibility extraction  
**Status:** User confirmed successful CI/build and physical-device testing. Exact workflow run and artifact identifiers were not supplied in the confirmation.

### Objective
Move the existing Material 3 light and dark color-scheme definitions out of MainActivity without changing theme behavior.

### Implementation
- Added `presentation/CodexThemeColors.kt` with the existing `CodexDarkColors` and `CodexLightColors` definitions.
- MainActivity imports and applies those definitions.
- All color values remain unchanged.
- Theme selection, preference persistence, system-bar icon handling, and MaterialTheme ownership remain in MainActivity.

### Files changed
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CodexThemeColors.kt`

### Verification
- Source changes are committed on `main`.
- CI verification: pending.
- Debug APK artifact: pending.
- Physical-device verification: pending.

### Remaining work
Run Debug CI, inspect the resulting artifact, and verify System/Light/Dark appearance plus all existing destinations and navigation on the physical device.


## Milestone 100 - System-Bar Appearance Presentation Extraction

**Date recorded:** 2026-11-27  
**Development phase:** Batch 6 - AUD-002 incremental UI responsibility extraction  
**Status:** Implemented and committed; CI and physical-device verification pending.

### Objective
Move the existing system-bar icon appearance side effect out of MainActivity without changing behavior.

### Implementation
- Added `presentation/CodexSystemBars.kt` to apply the existing status-bar and navigation-bar icon appearance using `WindowInsetsControllerCompat`.
- MainActivity supplies the existing `useDarkTheme`, current Compose view, and Activity window.
- Preserved the original SideEffect and both appearance assignments.
- Theme selection, preference persistence, edge-to-edge setup, and MaterialTheme ownership remain in MainActivity.

### Files changed
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CodexSystemBars.kt`

### Verification
- Source changes committed on `main`.
- CI verification: pending.
- Debug APK artifact: pending.
- Physical-device verification: pending.

### Remaining work
Run Debug CI, inspect the resulting artifact, and verify status/navigation bar icon contrast in System, Light, and Dark modes, plus existing navigation and account functionality.


## Milestone 101 - App Theme Composition Extraction

**Date recorded:** 2026-11-27  
**Development phase:** Batch 6 - AUD-002 incremental UI responsibility extraction  
**Status:** Implemented, committed, CI #845 passed, and physical-device verified by user.

### Objective
Extract the existing app-level theme composition from MainActivity without changing theme behavior.

### Implementation
- Added `presentation/CodexAppTheme.kt` to compose the existing system-bar appearance effect, Material 3 color scheme, and root Surface.
- MainActivity retains theme preference state, System/Light/Dark resolution, preference persistence, Activity window/view acquisition, and CodexScreen state.
- Preserved the existing color-scheme selection, Surface background, fill-size modifier, and system-bar effect.
- Step 20 system-bar appearance extraction was confirmed by the user as successfully tested. Exact CI run and artifact identifiers were not supplied.

### Files changed
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CodexAppTheme.kt`

### Verification
- Source changes committed on `main`.
- CI verification: pending.
- Debug APK artifact: pending.
- Physical-device verification: pending.

### Device checklist
1. Verify System, Light, and Dark modes, including status/navigation bar icon contrast.
2. Verify theme preference persistence after restart.
3. Confirm sign-in, Home, Visuals, Tools, Account, and AI entry remain functional.

### Boundary
Composition extraction only. Theme selection, preference persistence, and screen state remain caller-owned. No visual or performance improvement is claimed.


## Milestone 102 - Root Screen Composable Extraction

**Date recorded:** 2026-11-27  
**Development phase:** Batch 6 - AUD-002 incremental UI responsibility extraction  
**Status:** Implemented and committed; CI #852 passed; physical-device verification pending.

### Objective
Move the existing root screen composable out of MainActivity while preserving its state ownership and behavior.

### Implementation
- Moved the existing `CodexScreen` composable into `presentation/CodexScreen.kt`.
- MainActivity now owns Activity setup, ViewModel construction, edge-to-edge window setup, theme preference state/resolution/persistence, and invokes the extracted root screen.
- The extracted composable retains its existing screen-local Compose state, callbacks, refresh/export/login orchestration, tab selection, and destination composition.
- No state was moved into a new ViewModel or service; the composable body and parameter contract remain unchanged apart from visibility becoming internal.

### Files changed
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CodexScreen.kt`

### Verification
- Source changes committed on `main`.
- CI verification: pending.
- Debug APK artifact: pending.
- Physical-device verification: pending.

### Device checklist
1. Verify first-run sign-in and existing credential restoration.
2. Verify sync, Daily Note refresh, JSON export, credential clearing, and theme persistence.
3. Verify Home, Visuals, Tools, Account, and AI entry remain functional.

### Boundary
File/responsibility relocation only. Screen state ownership and behavior are unchanged. No performance improvement is claimed.


## Milestone 103 - Agent Context Retrieval Efficiency

**Date recorded:** 2026-11-27  
**Development phase:** Batch 8 - AUD-010 agent workflow efficiency  
**Status:** Implemented and committed; CI verification pending.

### Objective
Reduce unnecessary repository context loading for narrow tasks without weakening project, privacy, testing, cancellation, or Git safety requirements.

### Implementation
- Replaced the blanket minimum-reading list in root `AGENTS.md` with task-specific context retrieval rules.
- Kept relevant source/test inspection and current branch/commit checks mandatory.
- Made README, technical specifications, milestone history, release instructions, Gradle configuration, and workflows conditional on the task they govern.
- Explicitly requires agents to retrieve relevant context without loading unrelated documents.

### Files changed
- `AGENTS.md`
- `docs/CODEBASE_OPTIMIZATION_AUDIT.md`
- `docs/PROGRESS_MILESTONES.md`

### Verification
- Static instruction review completed.
- CI verification: pending.
- Physical-device verification: not applicable; no app behavior changed.

### Boundary
This is an agent workflow instruction change. It does not alter application source, runtime behavior, build configuration, dependencies, or product architecture.


## Milestone 104 - UI Revamp Batch 1: Design-System Foundation

**Date recorded:** 2026-11-27  
**Development phase:** UI Revamp - Batch 1  
**Status:** Implemented on `ui-revamp/batch-1-design-foundation`; CI and physical-device verification pending.

### Objective
Establish the shared theme foundation for the agreed high-fidelity Battle Chronicles, anti-slop, Android-native UI direction without redesigning individual screens.

### Implementation
- Replaced the former teal Material 3 primary palette with Archival Gold for Dark (`#D3BC8E`) and Light (`#8E6E35`) themes.
- Added coordinated Light/Dark surface, text, outline, and status color roles.
- Added shared Light/Dark Genshin element and character-rarity semantic palettes.
- Added `CodexDesignTokens.kt` with the shared 4/8dp spacing family, Material typography roles, standard shapes, and a tabular-numeric text style.
- Integrated shared colors, typography, and shapes through `CodexAppTheme`.
- Preserved existing System/Light/Dark selection, preference persistence, edge-to-edge setup, and system-bar icon handling.
- No dependencies, SDK levels, screen layouts, navigation, account behavior, or calculation logic were changed.

### Files changed
- `app/src/main/java/com/irminsul/codex/presentation/CodexThemeColors.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CodexDesignTokens.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CodexAppTheme.kt`
- `docs/PROGRESS_MILESTONES.md`

### Verification
- Source implementation: committed on the Batch 1 branch.
- Debug build and automated tests: pending GitHub Actions.
- APK artifact: pending.
- Physical-device verification: pending user testing.

### Device checklist
1. Verify System, Light, and Dark appearance modes.
2. Check primary actions and text contrast in both themes.
3. Check system-bar icon contrast and edge-to-edge content.
4. Confirm Home, Visuals, Tools, Account, and AI sheet still render and function.
5. Confirm theme preference remains saved after restarting the app.

### Boundary
Theme and shared-token foundation only. No individual screen redesign is included in Batch 1.


## Milestone 105 - UI Revamp Batch 2: Home Destination

**Date recorded:** 2026-11-27  
**Development phase:** UI Revamp - Batch 2  
**Status:** Implemented on `ui-revamp/batch-2-home`; CI and physical-device verification pending.

### Objective
Reconstruct Home as a coherent, touch-first account landing page using the shared design foundation, while preserving existing account, Daily Note, and intelligence data behavior.

### Implementation
- Rebuilt Home composition with deliberate section spacing based on shared design tokens.
- Reworked the account identity region into a prominent Archival Gold profile surface, avoiding a duplicate UID already shown in the app header.
- Reorganized snapshot information into a compact hierarchy with retrieved character count, retrieval timestamp, and the existing Visuals analysis destination.
- Rebuilt Real-Time Notes for narrow Android screens: prominent Resin and recovery state, a readable progress bar when source values are available, and a two-column layout for expeditions, Realm Currency, commissions, and weekly discounts.
- Kept missing values visibly unavailable; no null values are converted to zero or false.
- Consolidated the Home intelligence summary into one surface with assessment bars and compact recommendation/gap totals.
- Preserved the existing refresh callback, loading/export enablement, analysis navigation, and account-intelligence source model.
- Kept Real-Time Notes on Home. No exploration or game-mode data was invented or duplicated from Visuals; Home currently receives no source-backed exploration/game-mode preview models.

### Files changed
- `app/src/main/java/com/irminsul/codex/presentation/HomeSection.kt`
- `app/src/main/java/com/irminsul/codex/presentation/HomeAccountOverviewSection.kt`
- `app/src/main/java/com/irminsul/codex/presentation/HomeDailyNoteSection.kt`
- `app/src/main/java/com/irminsul/codex/presentation/VisualDashboardSection.kt`
- `docs/PROGRESS_MILESTONES.md`

### Verification
- Implementation source committed on the Batch 2 branch.
- Automated tests and Debug APK build: pending merge-triggered GitHub Actions.
- APK artifact: pending.
- Physical-device verification: pending user testing.

### Device checklist
1. Check Home in Light, Dark, and System modes.
2. Verify profile and snapshot layout with and without an account snapshot.
3. Verify Daily Note with valid, zero, and unavailable values.
4. Check narrow-screen wrapping, Resin progress, recovery text, and all four metric values.
5. Verify Refresh remains disabled during loading/export and triggers the existing refresh flow.
6. Verify View analysis opens Visuals and the Home intelligence summary remains consistent.
7. Confirm other destinations, bottom navigation, and AI sheet remain functional.

### Boundary
Home presentation only. No account retrieval, persistence, data model, analysis, navigation architecture, dependency, or SDK changes.


## Milestone 106 - Home Profile Identity and Parametric Transformer

**Date recorded:** 2026-11-27  
**Development phase:** Home follow-up  
**Status:** Implemented and automated verification successful; physical-device verification pending.

### Requested changes
- Replace the Home Weekly Discounts metric with the Parametric Transformer cooldown.
- Show the signed-in Genshin IGN, UID, HoYoLAB profile avatar, and game server in the Adventurer Profile surface.

### Implementation
- Daily Note parsing now preserves transformer ownership, reached state, and validated `Day/Hour/Minute/Second` recovery time. Missing or malformed values remain unavailable.
- The Home transformer metric displays Not obtained, cooldown remaining, Ready, or an explicit unavailable state. A local Compose countdown ticks once per second from the last server-reported cooldown; refresh remains the source of truth.
- Added best-effort HoYoLAB profile retrieval using the game record card for IGN/server/UID and the community user profile for `avatar_url`. Avatar failure does not hide valid game identity.
- The Home profile card now shows the profile image with an initials fallback, IGN, UID, and readable server name.
- Profile retrieval runs alongside the existing Daily Note refresh workflow. Clearing credentials clears the in-memory profile.
- Added parser tests for transformer ready/cooldown/malformed states and account profile mapping.
- Updated README Home feature description.

### Files changed
- `app/src/main/java/com/irminsul/codex/api/HoYoLabClient.kt`
- `app/src/main/java/com/irminsul/codex/api/DailyNoteParser.kt`
- `app/src/main/java/com/irminsul/codex/api/AccountProfileParser.kt` (new)
- `app/src/main/java/com/irminsul/codex/presentation/CodexScreen.kt`
- `app/src/main/java/com/irminsul/codex/presentation/HomeSection.kt`
- `app/src/main/java/com/irminsul/codex/presentation/HomeAccountOverviewSection.kt`
- `app/src/main/java/com/irminsul/codex/presentation/HomeDailyNoteSection.kt`
- `app/src/test/java/com/irminsul/codex/api/DailyNoteParserTest.kt`
- `app/src/test/java/com/irminsul/codex/api/AccountProfileParserTest.kt` (new)
- `README.md`

### Verification
- Main implementation commit: `c062890bbc857ae789a29871fa96a4eba8d243b7`.
- GitHub Actions run #875: successful. [Build and test results](https://github.com/berao609/Irminsul-codex/actions/runs/45999159450).
- Unit tests, Debug APK build, Room schema check, APK signature verification, and artifact upload: successful.
- Debug APK artifact ID: `15197008745`; name: `irminsul-codex-debug-v1.0.0-dev.875-vc877`.
- Room schema artifact ID: `15197213103`; name: `irminsul-room-schema-v2-run-875`.
- Physical-device verification: pending user testing.

### Device checklist
1. Confirm IGN, UID, profile avatar, and server appear after login and remain after app restart.
2. Confirm avatar fallback works if the profile image cannot load.
3. Confirm account switching and sign-out do not show the previous account's profile.
4. Confirm Parametric Transformer states: not obtained, cooldown, ready, and unavailable.
5. Confirm cooldown ticks down and refresh reconciles it with the server response.
6. Confirm Weekly Discounts is no longer displayed and the rest of Daily Note remains intact.

### Boundary
Home/profile and Daily Note presentation/data mapping only. No account snapshot schema, analysis engine, navigation, dependencies, or SDK changes.



## Milestone 107 - UI Revamp Batch 3: Visuals Destination

**Date recorded:** 2026-11-27  
**Development phase:** UI Revamp - Batch 3  
**Status:** Implemented and CI-verified; physical-device verification pending.

### Objective
Reconstruct the Visuals destination around the agreed high-fidelity Battle Chronicles hierarchy while preserving existing account data, analysis, and interactions.

### Implementation
- Reorganized Visuals into a clear account journey: Account Overview, selected Character Showcase, World Exploration, game-mode records, and Account Analysis.
- Moved the selected character detail presentation ahead of the expandable roster so character artwork, identity, equipment, stats, talents, and constellations lead the showcase; the roster remains available for selection.
- Rebuilt Account Overview with a distinct player/adventure-rank identity region and a compact three-column progress/collectibles layout.
- Reworked World Exploration into horizontally browsable regional progress panels with clearer region names, percentages, and progress bars.
- Reworked Spiral Abyss and Imaginarium Theater record presentation to emphasize available star results and retain supporting record metrics.
- Removed generic enclosing cards from Chronicle and Account Analysis section wrappers. Kept containers only where they communicate identity or group actual metric data.
- Preserved the existing presentation mapper, source data, stale/missing/unavailable/failed distinctions, analysis engine, recommendation ordering, refresh action, and character selection behavior.
- Kept Real-Time Notes exclusively on Home. No new dependencies, account endpoints, data fields, or calculation behavior were introduced.

### Files changed
- `app/src/main/java/com/irminsul/codex/presentation/ChronicleSections.kt`
- `app/src/main/java/com/irminsul/codex/presentation/VisualsSection.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CharacterShowcaseSection.kt`
- `app/src/main/java/com/irminsul/codex/presentation/AccountAnalysisSection.kt`

### Verification
- Implementation committed on `ui-revamp/batch-3-visuals`.
- GitHub Actions: pending.
- Debug APK artifact: pending.
- Physical-device verification: pending user testing.

### Device checklist
1. Check the Visuals destination in Light, Dark, and System modes.
2. Confirm Account Overview shows only source-backed player, rank, progression, and collectible values.
3. Confirm selected character detail is visible before the expandable roster; selecting a roster portrait updates the detail view.
4. Verify character artwork, equipment expansion, stats, talents, and constellations still render.
5. Verify World Exploration horizontal scrolling, region names, percentages, and progress bars.
6. Verify current/previous Spiral Abyss and Imaginarium Theater values, including missing, unavailable, stale, and failed states.
7. Verify Account Analysis action, loading/error states, recommendation expansion, and data-coverage warnings.
8. Confirm no Real-Time Notes appear in Visuals and Home remains unchanged.
9. Confirm bottom navigation, AI sheet, Tools, and Account remain functional.

### Boundary
Visual presentation and composition only. No account retrieval, persistence, analysis calculations, mapper rules, navigation architecture, dependencies, or SDK changes.


### Reference-alignment correction (2026-11-27)
- Re-reviewed the actual HTML exports after the first Batch 3 CI attempt. The first implementation did not fully reflect the reference: the account summary lacked its primary/secondary expandable metric hierarchy, and the character showcase lacked the compact horizontal portrait selector.
- Added the horizontal, release-ordered character portrait selector before the selected character detail, matching the reference's selector/detail relationship. The existing expanded roster remains available as a supplementary selection surface.
- Corrected game-mode primary metrics: Best Floor for Spiral Abyss and Difficulty for Imaginarium Theater, with Stars as fallback.
- The account summary hierarchy and exact `Main Areas Only` label were not actually completed in this correction; they were completed in Milestone 108.
- The current `AccountSnapshot` has no Stygian Onslaught section. The reference includes it, but this batch does not fabricate or add a data source for it.
- Added the missing `dp` import to `VisualsSection.kt`, which caused CI run #877 to fail during Kotlin compilation.
- GitHub Actions run #878 succeeded for merge commit `e74f2c1ff7360444415ef097dd4b3f50264e6888`; Debug APK artifact ID `15198589056` (`irminsul-codex-debug-v1.0.0-dev.878-vc880`). Physical-device verification remains pending.

## Milestone 108 - UI Revamp Batch 3.1: Reference Fidelity Completion

**Date recorded:** 2026-11-27  
**Development phase:** UI Revamp - Batch 3.1  
**Status:** Implemented, merged, and CI-verified; physical-device verification pending.

### Objective
Close the verified Batch 3 presentation gaps using the consolidated Battle Chronicle references without changing account retrieval, persistence, analysis, or navigation architecture.

### Implementation
- Rebuilt Account Overview's statistics hierarchy: Active Days, Achievements, and Characters are the primary row; remaining supported progression/collectible statistics are behind an explicit expand/collapse control.
- Reworked World Exploration into compact horizontal region cards with the exact `Main Areas Only` label, source-backed region emblem URLs when present, and a `View more regions` action that switches to a vertically stacked detail presentation. Missing emblems remain omitted rather than replaced with invented assets.
- Increased the selected-character portrait prominence and placed identity/progression details in a distinct contextual hero surface. The current account model provides a portrait URL, not verified full-body splash artwork; no URL is synthesized and no new asset source is introduced.
- Preserved the existing character selection, equipment, attributes, talents, constellations, game-mode records, and Account Analysis behavior.
- Stygian Onslaught remains excluded because `AccountSnapshot` has no corresponding source-backed section or validated payload mapping.
- No new dependencies, endpoints, persisted fields, calculation behavior, or navigation destinations were introduced.

### Files changed
- `app/src/main/java/com/irminsul/codex/presentation/ChronicleSections.kt`
- `app/src/main/java/com/irminsul/codex/presentation/ChroniclePresentationMapper.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CharacterShowcaseSection.kt`
- `app/src/test/java/com/irminsul/codex/presentation/ChroniclePresentationMapperTest.kt`
- `docs/PROGRESS_MILESTONES.md`

### Verification
- Implementation merged in PR #5, main commit `786bea41a058754f3a42b6cf17b0077edd8180f4`.
- Follow-up test correction and milestone record merged in PR #6, commit `e1110d9174e2b6a5257a50c0430c1399c3dff232`.
- GitHub Actions run #880 initially failed one new grouping assertion; the mapper was corrected in PR #6.
- GitHub Actions run [#881](https://github.com/berao609/Irminsul-codex/actions/runs/46010779366) succeeded. Unit tests, Debug APK build, Room schema snapshot verification/upload, APK signature verification, and both artifact uploads succeeded. Room instrumentation tests were correctly skipped because no persistence or database code changed.
- Debug APK: `irminsul-codex-debug-v1.0.0-dev.881-vc883`, artifact ID `15203742837`, archive SHA-256 `1cfa60f13a89b453e75cc39948d7e4a620f5c22662ca707d5cf7dd43cc0ed793`.
- Room schema artifact ID: `15203702406`.
- Physical-device verification: partially tested by user; tested portions clear; remaining Batch 3.1 checklist items remain open and unverified.

### Device checklist
1. Check Visuals in Light, Dark, and System modes.
2. Confirm Account Overview shows identity, the three primary statistics, and collapsed additional statistics; expand/collapse the secondary group.
3. Confirm missing primary metrics do not become zero or get replaced with unrelated metrics.
4. Confirm World Exploration horizontal cards show `Main Areas Only`, source-backed emblem where present, percentage, and progress bar.
5. Tap `View more regions`; confirm all available regions appear as a vertical list; collapse back to the horizontal carousel.
6. Confirm selected-character portrait/identity hero is larger and readable on narrow screens.
7. Confirm character selection, weapon/artifact expansion, attributes, talents, and constellations remain functional.
8. Confirm Spiral Abyss and Imaginarium Theater current/previous records and unavailable states remain intact.
9. Confirm Account Analysis, bottom navigation, AI sheet, Tools, and Account remain functional.

### Boundary
Presentation-only reference fidelity work. No account retrieval, raw source schema, persistence, analysis calculations, navigation architecture, dependencies, or SDK changes.


## Milestone 109 - UI Revamp Batch 4: Expanded Battle Chronicle Coverage

**Date recorded:** 2026-11-27  
**Development phase:** UI Revamp - Batch 4  
**Status:** Implemented, merged, and CI-verified in PRs #7 and #8; partially device-verified on 2026-11-28 (mode visibility and initial expanded state confirmed; detailed behavior remains unverified).

### Objective
Extend the existing Visuals Battle Chronicle experience with source-backed Stygian Onslaught data while establishing a typed domain boundary for this additional Chronicle section. This is part of the ongoing UI revamp, not a separate task.

### Retrieval finding
- The existing HoYoLAB `/index` request already returns the Battle Chronicle `stats` object.
- Independent read-only endpoint research identifies `stats.hard_challenge.name` as the Stygian Onslaught best-record field and `stats.difficulty` as its difficulty field. See [HoYoLAB Genshin Battle Chronicle MCP field notes](https://glama.ai/mcp/servers/danisec/hoyolab-mcp), which document these fields and warn that the upstream endpoints are unofficial and undocumented.
- No additional network request is added for this summary. The app reuses the retained `profile_and_exploration` payload and its existing section provenance/timestamp, avoiding redundant retrieval.
- This does not establish that every detailed Stygian cycle/team field is available through the current account index. Detailed record retrieval remains subject to separate endpoint and response validation.

### Implementation
- Added `StygianOnslaughtSnapshot` as a typed domain model with optional best-record name and difficulty.
- Derived the model from `profile_and_exploration.stats.hard_challenge.name` and `profile_and_exploration.stats.difficulty`.
- Preserved missing, unavailable, failed, and stale source states. Missing record fields are not converted into fabricated values.
- Added a dedicated Stygian Onslaught section to Visuals.
- Kept the original raw export payload unchanged; no Room schema migration or new dependency was required.
- Added mapper tests for valid values, missing fields, and failed index retrieval.
- Extracted Battle Chronicle retrieval into `BattleChronicleCollector`, which independently coordinates the index, current/previous Spiral Abyss, and Imaginarium Theater requests while the account retrieval workflow continues to coordinate identity, Daily Note, and character requests.
- Kept the four Chronicle requests concurrent and mapped each response into its own export section with independent source/timestamp/status.
- Added collector tests for endpoint routing, independent section mapping, sibling success when one endpoint fails, and cancellation propagation.

### Files changed
- `app/src/main/java/com/irminsul/codex/domain/model/AccountSnapshot.kt`
- `app/src/main/java/com/irminsul/codex/data/mapper/AccountExportMapper.kt`
- `app/src/main/java/com/irminsul/codex/api/HoYoLabClient.kt`
- `app/src/main/java/com/irminsul/codex/api/BattleChronicleCollector.kt`
- `app/src/test/java/com/irminsul/codex/api/BattleChronicleCollectorTest.kt`
- `app/src/main/java/com/irminsul/codex/presentation/ChronicleSections.kt`
- `app/src/main/java/com/irminsul/codex/presentation/VisualsSection.kt`
- `app/src/test/java/com/irminsul/codex/data/mapper/AccountExportMapperTest.kt`
- `README.md`
- `docs/PROGRESS_MILESTONES.md`

### Verification
- PR #7 merged as `d34dede92c5fc557ae1b50df4b585cdb31ad67c9`; PR #8 merged as `2fd8d3d7ff14418a211e5976f8a598783b9bc63e`.
- GitHub Actions [run #883](https://github.com/berao609/Irminsul-codex/actions/runs/46017911203) succeeded for PR #7's merge commit. Unit tests, Room instrumentation tests, Debug APK build, Room schema verification/upload, APK signature verification, and artifact uploads succeeded.
- GitHub Actions [run #884](https://github.com/berao609/Irminsul-codex/actions/runs/46018327952) succeeded for PR #8's merge commit. Unit tests, Debug APK build, Room schema verification/upload, APK signature verification, and artifact uploads succeeded. Room instrumentation tests were correctly skipped because PR #8 changed no persistence or snapshot-model code.
- Latest Debug APK: `irminsul-codex-debug-v1.0.0-dev.884-vc886`, artifact ID `15207231460`, SHA-256 `38f044b90e71d48367f372afc25c39e45e5e727996c0a5f4817f95dfa9be838c`.
- Room schema artifact ID: `15207725741`; SHA-256 `bc3887f3199ac6c94af8895d458a449df68ebccf414b7778a58d8ddb547c126f`.
- Physical-device verification (2026-11-28): user confirmed Spiral Abyss, Imaginarium Theater, and Stygian Onslaught all appear in Visuals and are expanded on entry. This verifies section visibility and the current default-expanded behavior only; detail completeness, navigation, and all data fields remain unverified.
- No Room schema migration was required; the typed Stygian summary is derived from the existing persisted raw index payload.

### Device checklist
1. Verify Stygian Onslaught appears in Visuals when the index response includes the supported fields.
2. Verify the best-record name and difficulty match the source data.
3. Verify missing fields do not display invented values or misleading zeros.
4. Verify failed/unavailable index data is communicated without crashing or hiding unrelated Chronicle sections.
5. Confirm existing Account Overview, Exploration, Character Showcase, Abyss, Theater, Account Analysis, navigation, Home, and Tools remain functional.

### Boundary
This milestone adds typed Stygian summary mapping, Visuals presentation, and a dedicated local Battle Chronicle collector. It does not add an unverified Stygian detail endpoint, historical cycle/team records, a separate cloud service, or a Room migration. Further Chronicle endpoint expansion remains part of the same ongoing UI revamp batch plan and must be evidence-led.


---

# Active UI Revamp Continuation Plan
**Updated:** 2026-11-28  
**Plan status:** Active; Batch 5 implementation in progress  
**Reference authority:** `docs/BATTLE_CHRONICLE_REFERENCE_ANALYSIS.md`, especially its updated Section 10, plus `docs/Irminsul Codex UI Design Instructions.md`.

This is the continuation of the existing UI revamp. It does not create a separate product task or supersede completed milestones 107-109.

## Current verified position

- Milestones 107-109 established the Visuals overview, reference-aligned account/exploration/character presentation, and typed Chronicle coverage.
- PRs #7 and #8 delivered the initial Stygian summary and Theater mapping; the latest relevant implementation merge is `2272910489dbf672466f165e19d4a4ec6e88a621`. The latest reference-document commit on `main` is `3dc6ad9610843b1e566ef8851b61365d4e801121`.
- User device check (2026-11-28): Spiral Abyss, Imaginarium Theater, and Stygian Onslaught all appear in Visuals and are expanded on entry.
- Current source still renders Visuals as one long vertical composition (`VisualsSection.kt`). Shared `ChronicleSection()` starts expanded (`remember(title) { mutableStateOf(true) }`). This matches the observed device behavior but is not the intended final navigation model.
- No standalone UI revamp implementation-plan file was found in the repository. This active continuation plan is recorded here beside the milestone history to avoid creating a duplicate plan document.

## Batch 5 prerequisite - Android platform modernization

**Status:** Integrated prerequisite within the existing UI Revamp. This is not a separate product task or a new UI batch.

### Why this is required
- Batch 5 is merged on `main` at `237eb2393230c694c0e6eecc4611f005d9c58f15`.
- GitHub Actions [Build Debug APK run #895](https://github.com/berao609/Irminsul-codex/actions/runs/46080591345) failed at `:app:checkDebugAarMetadata` before tests or APK assembly.
- Lucide Android 2.2.1 resolves Compose 1.9.3, which requires a newer compile SDK and AGP than the existing AGP 8.5.0 / compileSdk 34 baseline.
- This exposed a broader toolchain gap. Resolve it before Batch 5 device verification and before continuing the next UI implementation batches.

### Verified current baseline
- AGP 8.5.0; CI Gradle 8.14.8; JDK 17.
- Kotlin 2.0.0; Compose BOM 2024.06.00.
- compileSdk 34; targetSdk 34; minSdk 26.
- KSP 2.0.0-1.0.21; Room 2.6.1; Lifecycle 2.8.2; Activity Compose 1.9.0.
- Lucide Android 2.2.1.

### Implemented stable upgrade baseline
This matrix is implemented on PR #13. CI verification and physical-device testing remain pending.

| Component | Implemented version | Compatibility basis |
|---|---:|---|
| Android Gradle Plugin | 9.3.1 | Documented minimum AGP for API 37.1; within Kotlin 2.4.21's fully supported range |
| Gradle | 9.7.0 | Latest Gradle within Kotlin 2.4.21's documented fully supported range |
| Kotlin / Compose compiler plugin | 2.4.21 | Stable release; supports AGP through 9.3.1 and Gradle through 9.7.0 |
| KSP | 2.3.12 | Supports KGP 2.3.20-2.4.x, AGP 8.13+, Gradle 8.13+, and JDK 17+ |
| Compose BOM | 2026.11.01 | Stable Compose 1.13 line; requires compileSdk 37.1 and Kotlin 2.2+ |
| compileSdk | API 37.1 | Stable minor API platform; use `release(37) { minorApiLevel = 1 }` |
| targetSdk | API 36 | Retained until Android 17 target-behavior changes are audited |
| minSdk | API 26 | Preserved |
| Room | 2.8.5 | Stable Room release |
| Lifecycle | 2.11.1 | Stable Lifecycle release |
| Activity Compose | 1.13.0 | Stable Activity release |
| Core KTX | 1.19.1 | Stable Core release |

CI package IDs:
- Compile platform: `platforms;android-37.1`
- Build tools: `build-tools;37.0.0`
- Emulator image: `system-images;android-37.2;google_apis_ps16k;x86_64` (stable Android 17 QPR2, 16 KB page size)

Official compatibility references reviewed:
- [AGP API-level support and Gradle requirements](https://developer.android.com/build/releases/about-agp)
- [Kotlin Gradle plugin compatibility table](https://kotlinlang.org/docs/gradle-configure-project.html)
- [AGP built-in Kotlin migration](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [KSP requirements](https://kotlinlang.org/docs/ksp-overview.html)
- [Compose November 2026 stable release](https://developer.android.com/blog/posts/jetpack-compose-november-2026-release)
- [Android Emulator release notes](https://developer.android.com/studio/releases/emulator)
- [Google Play target API requirement](https://support.google.com/googleplay/android-developer/answer/11926878)

### Migration requirements

**Stable Android 17 SDK package correction (2026-11-28):** The failed CI attempts used the major-only package IDs `platforms;android-37` and `system-images;android-37;google_apis;x86_64`. The stable packages carry minor API identifiers: compile against `platforms;android-37.1`, and test on `system-images;android-37.2;google_apis_ps16k;x86_64`. This makes the stable Compose 1.13 BOM and API 37.1 compile platform viable without using a preview SDK. CI still needs to verify both package installations and emulator boot.

1. Recheck the compatibility matrix against official release notes immediately before implementation. Prefer the latest stable versions that are mutually supported; do not use alpha, beta, RC, or dynamic dependency versions. Keep compileSdk at stable API 37.1 and use the stable API 37.2 emulator image for runtime testing.
2. Migrate AGP 9 built-in Kotlin correctly. Remove the redundant `org.jetbrains.kotlin.android` plugin where required; retain and align the Kotlin Compose compiler plugin. Inspect AGP 9 DSL and build behavior changes rather than suppressing migration warnings blindly.
3. Upgrade KSP independently to a version supported by the selected KGP/AGP/Gradle combination. Keep Room's compiler and runtime versions aligned.
4. Review every existing library individually. Do not force unrelated major migrations (for example Coil 2 to Coil 3) merely to claim that every dependency is newest. Preserve behavior and update only where compatibility, security, maintenance, or platform support justifies it.
5. Compile against API 37.1 and keep targetSdk at 36 until Android 17 target-behavior changes are audited. Review edge-to-edge/system bars, predictive back, orientation/resizability behavior on large screens, permissions, background execution, notifications, and storage behavior. Keep minSdk 26 unless a demonstrated dependency requirement forces a separately reviewed decision.
6. Update CI Gradle setup and Android emulator API coherently. Preserve persistent Debug signing, version-code calculation, Room schema verification, signature verification, artifact naming, and temporary-keystore cleanup.
7. Add/adjust dependency-resolution and build checks so future incompatible AAR metadata, plugin, Kotlin, or SDK combinations fail before a UI batch is treated as ready.
8. Preserve the existing single-module native Kotlin/Compose architecture, local-first design, account data integrity, export compatibility, and all Batch 5 UI behavior.

### Acceptance criteria
- AGP, Gradle, Kotlin, Compose, KSP, SDK, and AndroidX versions are mutually supported by official compatibility documentation.
- No alpha/beta/RC or dynamic versions are used in the production build.
- Gradle configuration and AGP migration complete without unresolved deprecations or compatibility workarounds that hide future breakage.
- Unit tests, Room instrumentation/migration tests, lint/build checks, Debug APK assembly, Room schema verification, signing verification, and artifact upload succeed.
- Batch 5's Lucide resources resolve and render correctly in Light, Dark, and System themes.
- Physical-device checks are recorded separately from CI. Do not mark Batch 5 complete until the user verifies the APK on-device.
- The next UI Revamp batch proceeds on the verified modernized foundation without creating a separate modernization task.

## Locked direction and constraints

1. Reconstruct the top-level Battle Chronicles overview as a compact landing page, not a long page of fully expanded feature content.
2. Use concise, source-backed previews with clear entry actions. Detailed content belongs on dedicated destinations.
3. Preserve the four existing bottom destinations (Home, Visuals, Tools, Account) and the centered AI action. Add nested destinations under Visuals; do not add or fake a new bottom-navigation destination.
4. Detail destinations need their own title/back action, independent scrolling, and appropriate state preservation. Returning from a detail page should restore the Visuals overview position and relevant selections.
5. Keep the reference's distinct composition and mode identity. Do not replace it with generic cards or a uniform metrics dashboard.
6. Preserve missing, null, malformed, unavailable, stale, failed, and actual-zero distinctions. Never invent record fields, region progress, or account values.
7. Stygian's currently validated source supports the best-record name and difficulty only. Do not imply that historical cycles, teams, or detailed challenge statistics are available until their source is validated.
8. Do not add a runtime Battle Chronicles dependency, WebView, scraping, remote CSS, or a mandatory backend. Keep implementation native Compose and local-first.
9. **System icon preference:** use Lucide icons for application/system UI icons. Centralize them through the shared app icon wrapper/registry (create or extend one only if the code audit shows it is needed). Preserve Genshin character, weapon, element, rarity, region, and mode artwork as game assets; do not replace those with Lucide.
10. Lucide Android 2.2.1 and its centralized registry are already implemented in Batch 5. Build #895 proves the old build foundation is incompatible with its Compose dependency. Keep the Lucide implementation; complete the integrated platform-modernization prerequisite and verify the generated resource names, APK build, and rendered appearance. Audit Material icon usage before removing any remaining dependency.

## Batch 5 - Visuals Navigation and Overview Hierarchy

**Status:** Implemented and merged on `main` in `237eb2393230c694c0e6eecc4611f005d9c58f15`. Build #895 failed before tests/APK assembly due to the Lucide/Compose/AGP/compileSdk incompatibility. Complete the integrated platform-modernization prerequisite above, then perform CI and device verification.

### 5.1 Rebuild the Visuals landing page
- Reproduce the top-most Battle Chronicles overview hierarchy from the updated reference.
- Keep the account identity/overview compact, retaining the existing primary/secondary statistics behavior.
- Keep My Characters as a horizontal portrait selector with a compact selected-character preview and a clear route to the full roster/details.
- Keep World Exploration as horizontal region previews using the reference's `Main Areas Only` treatment and a dedicated View More destination.
- Present Spiral Abyss, Imaginarium Theater, and Stygian Onslaught as compact mode-specific summaries with clear detail entry points.
- Keep Account Analysis as a concise preview/entry point; preserve its current calculation, recommendation ordering, and source-state behavior.
- Remove the current requirement that all Chronicle content be expanded inline. Do not merely change every section's default to collapsed; replace the long-form inline presentation with purposeful summaries and navigation.

### 5.2 Establish nested Visuals navigation
- Add an internal destination/state model for Visuals pages without changing the four primary bottom-navigation destinations.
- Support back navigation and direct transitions among the Visuals landing page and its child pages.
- Give each destination its own scroll container and preserve the overview scroll/selection state when returning.
- Ensure system back and visible back controls behave consistently.
- Keep the implementation incremental and compatible with the existing app architecture; do not introduce a navigation dependency or broad navigation rewrite without demonstrated need.

### 5.3 Add the Lucide compatibility pilot
- Inspect all current system icon usages and any existing shared icon wrapper before changing dependencies.
- Add the Android vector-resource artifact as a pilot only; verify the exact generated resource names from the resolved artifact rather than assuming them.
- Replace a small representative set of app-shell/navigation/action icons with Lucide through the shared wrapper.
- Build and inspect the APK in Light and Dark themes. Check optical sizing, tinting, accessibility descriptions, and APK/resource impact.
- If compatible, migrate remaining system icons in a controlled follow-up and remove Material Icons Extended only after a full usage audit. If incompatible, stop and document the blocker; do not silently switch to a newer incompatible stack.

### Batch 5 acceptance checks
- Visuals opens to a compact Battle Chronicles overview, not the current long expanded page.
- All three Chronicle modes remain visible as summaries when their data is available; tapping each opens its own detail destination.
- Character, exploration, and analysis entry points remain usable.
- Back navigation restores the overview position and selected character/region where applicable.
- Home, Tools, Account, AI sheet, refresh/analyze actions, and existing account data behavior remain intact.
- Light, Dark, and System themes; edge-to-edge insets; narrow-screen layout; touch targets; and accessibility semantics are checked.
- Lucide pilot resolves and builds before wider icon migration is accepted.
- Automated tests/build and physical-device verification are recorded separately.

## Batch 6 - Battle Chronicle Detail Experiences

- Build dedicated Spiral Abyss, Imaginarium Theater, and Stygian Onslaught pages using validated source fields and reference-specific layouts.
- Spiral Abyss: current/previous cycle context, floor/chamber grouping, stars, teams, character presentation, and available record details.
- Theater: nested cycle structure, act/round details, available character levels/lineups, medals/blessings, and period navigation only where the payload supports them.
- Stygian: begin with the validated best-record name/difficulty summary. Investigate historical phases, teams, and challenge details through captured exports/validated endpoint responses before modeling or displaying them.
- Keep per-section retrieval status and provenance. Add parser/mapper tests for valid, missing, malformed, and failed data. No fabricated placeholders that look like real records.

## Batch 7 - World Exploration Detail and Data Hierarchy

- Build the dedicated vertically stacked exploration page.
- Extend the model/mapper to represent parent regions and nested subareas (for example, subareas belonging to Liyue) only where the source export provides reliable grouping.
- Add region progression systems (Statues, offerings, and similar levels) only when source-backed.
- Preserve overview carousel values and detail-page totals without double-counting or conflating parent and child percentages.
- Add mapper tests for grouping, missing values, and incomplete source sections.

## Batch 8 - Character and Weapon Reference Fidelity

- Refine the dedicated Character Roster and Character Details destinations against the reference, preserving the horizontal selector/detail relationship and source-backed character artwork.
- Implement the Weapon Roster as a dedicated destination with the reference-matched rarity and weapon-type filter layout and roster grid.
- Filters must operate on the actual roster, support clear/reset behavior, and expose selected state accessibly. Do not add unnecessary sorting controls or unrelated rarity categories.
- Preserve the existing character/weapon detail data and approved artwork-loading path. Do not fabricate passive descriptions or other missing fields.
- Validate the roster and details on-device in both themes and narrow layouts.

## Cross-batch data and verification gates

- Before adding or expanding a data field, inspect the raw account export and current mapper/model. Use Lemur DevTools or another read-only capture only when a specific missing source field needs validation.
- Keep retrieval, domain mapping, presentation state, and Compose rendering responsibilities separated.
- Add deterministic tests for new mapping/navigation state behavior. Do not let UI code parse raw JSON or orchestrate network requests.
- Record exact source files, commits, CI run/artifacts, and user device checks in the relevant milestone. A successful build is not physical-device verification.
- Do not mark a batch complete while any known regression or unverified acceptance item is hidden.

## Immediate next action

Continue Batch 5 by compiling and validating the new Visuals navigation and Lucide resource pilot. Fix any dependency/resource or Compose issues before visual refinement. Then inspect the overview and each destination on-device in Light and Dark themes. Do not begin detailed Chronicle endpoint expansion until the new navigation structure is verified.


---

## Milestone 110 - UI Revamp Batch 5: Visuals Navigation Foundation

**Date recorded:** 2026-11-28  
**Development phase:** UI Revamp - Batch 5  
**Status:** Implementation in progress on `ui-revamp/batch-5-navigation-overview`; CI and physical-device verification pending.

### Objective

Replace the long, fully expanded Visuals composition with a compact overview and dedicated nested destinations, while introducing the Lucide system-icon library.

### Implemented on the feature branch

- Added `VisualsDestination` and a small tested navigation-stack rule set.
- Added nested Visuals routes for Character Roster, Character Details, World Exploration, current/previous Spiral Abyss, Imaginarium Theater, Stygian Onslaught, and Account Analysis.
- Added Android system Back handling and visible detail-page back navigation without changing the four primary bottom-navigation destinations or centered AI action.
- Added saveable per-destination scroll-state hosting so returning to a page can restore its scroll position.
- Replaced the long inline Visuals composition with a compact overview: account statistics, horizontal character preview and selected-character summary, exploration region previews, four Chronicle mode summaries, and an Account Analysis preview.
- Reused existing source-backed detail components on the new destinations. No new Chronicle fields or endpoint assumptions were added.
- Added a character portrait selector on the dedicated Character Details destination and a View Selected Character action on the roster destination.
- Audited system-icon usage across the presentation sources and app shell. Added a centralized `AppIcons` registry using `com.composables:icons-lucide-android:2.2.1`, migrated active app/system icon usages, and removed the unused Material Icons Extended dependency/catalog entry.
- Kept Genshin character, weapon, element, rarity, region and mode artwork on existing game-asset paths.

### Known boundaries

- The account hero's latest-patch splash artwork is not added. The reference does not establish a permitted asset source/update process; do not bundle or scrape artwork until provenance and permission are verified.
- Character roster filter layout and weapon roster reconstruction remain later Batch 8 work.
- Exploration parent/subarea modeling remains Batch 7 work.
- Dedicated Chronicle pages currently reuse the existing validated summaries. Full floor/chamber, Theater act, and Stygian challenge histories remain Batch 6 and require source validation.
- Lucide resource resolution and compatibility with Kotlin 2.0.0, AGP 8.5.0 and Compose BOM 2024.06.00 are not yet build-verified.

### Files changed

- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/AppIcons.kt`
- `app/src/main/java/com/irminsul/codex/presentation/VisualsDestination.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CodexScreen.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CodexBottomNavigationBar.kt`
- `app/src/main/java/com/irminsul/codex/presentation/VisualsSection.kt`
- `app/src/main/java/com/irminsul/codex/presentation/ChronicleSections.kt`
- `app/src/main/java/com/irminsul/codex/presentation/CharacterShowcaseSection.kt`
- `app/src/main/java/com/irminsul/codex/presentation/ToolsSection.kt`
- `app/src/main/java/com/irminsul/codex/presentation/AiAssistantSheet.kt`
- `app/src/test/java/com/irminsul/codex/presentation/VisualsNavigationStackTest.kt`

### Verification

- Source files and branch diff were retrieved from GitHub after editing.
- Navigation stack unit tests were added for push, duplicate destination, back, and root behavior; test execution is pending.
- No successful Gradle build, GitHub Actions run, APK artifact, or physical-device verification is claimed.
- Latest implementation commit: `bec6f3b95326251d04bc90b7e093f0c3439f729e` (includes the generated app-resource import correction for Lucide).

### Next action

Run the Debug build and unit tests for this branch. Resolve any Lucide dependency/resource or Compose compilation issues, then inspect the overview and nested destinations on-device before treating Batch 5 as complete.


---

## Milestone 111 - UI Revamp Batch 5: Android 17 API 37.1 Modernization

**Date recorded:** 2026-11-28  
**Development phase:** UI Revamp - Batch 5 integrated platform modernization  
**Status:** JVM target correction committed; main branch integration commit prepared; CI rerun pending; APK and physical-device verification pending.

### Objective

Move the existing Batch 5 modernization from the API 36.1 dependency baseline to the current stable Android 17 API 37.1 / Compose baseline without changing targetSdk behavior or creating a separate task.

### Implemented on `ui-revamp/batch-5-platform-modernization`

- Kept the mutually supported AGP 9.3.1, Gradle 9.7.0, Kotlin 2.4.21, and KSP 2.3.12 toolchain.
- Raised compileSdk to API 37.1 using `release(37) { minorApiLevel = 1 }`.
- Kept targetSdk 36 and minSdk 26. Android 17 target-behavior opt-in is deferred until its behavior changes are audited.
- Upgraded Compose BOM to stable 2026.11.01 and AndroidX Core KTX to 1.19.1 / Lifecycle to 2.11.1. Activity Compose 1.13.0 and Room 2.8.5 remain aligned.
- Updated CI to install stable platform API 37.1 / Build Tools 37.0.0 and use the stable Android 17 API 37.2 QPR2 16 KB page-size emulator image.
- Replaced `MigrationTestHelper` with a manually created version-1 SQLite fixture opened by the production Room database. The test still executes `MIGRATION_1_2`, exercises Room's schema validation, and verifies that migrated JSON chunks and metadata are preserved.
- Removed the unused `room-testing` dependency to avoid the observed `FieldBundle$$serializer` / kotlinx-serialization runtime linkage failure.

### Files changed

- `.github/workflows/build.yml`
- `app/build.gradle.kts`
- `app/src/androidTest/java/com/irminsul/codex/data/local/IrminsulDatabaseMigrationTest.kt`
- `gradle/libs.versions.toml`
- `docs/PROGRESS_MILESTONES.md`

### Implementation commits

- `060f21d0880403746e47b41e7c464b08be766799` - replace the Room migration helper test path.
- `e545200becb6cf539a25ee264e3b809fc2ecf995` - remove the unused Room testing dependency.
- `a8e93f859e48d9b17032615e5766966c1b2ab226` - update CI SDK and emulator configuration to API 37.
- `917161ebbb3418bc5d2b769b5314f79f9e943e24` - compile against API 37.1.
- `2fcf368dad76c2c880cd7963395ae32a4b1036d4` - upgrade stable Compose and AndroidX dependencies.
- `785474a5dd13f3b865c5697b39d5efd7c01d7173` - correct stable API 37.1 platform and API 37.2 emulator image package IDs.
- `7ef063d33dd48b398bcfba169c239a79089e8a3d` - trigger CI on UI Revamp branch pushes as well as PR updates.
- `5759e15f9f9f4d306c95f1cefa030e181110d0cf` - label the Android 17 API 37.2 runtime test.
- `dade60c8ecf96c1803dd58b50996606d03e7ae55` - align Java and Kotlin bytecode targets to JVM 11.
- `20697379d817c63007c1b27596a3c361c46be869` - reconcile the milestone plan with the latest main documentation.

### Verification

- Run #915 failed at the Room migration instrumentation test with `AbstractMethodError` from `androidx.room.migration.bundle.FieldBundle$$serializer`. Unit tests passed; APK assembly and upload were skipped.
- Run #918 failed because the emulator action requested the obsolete package ID `system-images;android-37;google_apis;x86_64`; the current image package uses `android-37.2` and `google_apis_ps16k`. Run #920 failed because CI requested `platforms;android-37`; the stable platform package uses `platforms;android-37.1`. Both failures occurred before instrumentation tests. The corrected package IDs were verified to install successfully in run #926.
- Run #926: stable API 37.1 SDK installation succeeded. Unit-test compilation then failed because Java source/target remained 1.8 while Kotlin inline dependencies were JVM 11. The Android emulator and APK steps were skipped.
- The branch now explicitly aligns Java compatibility and Kotlin `jvmTarget` to JVM 11. This correction is not yet CI-verified.
- No successful API 37.1 build, instrumentation test run, APK artifact, or physical-device verification is claimed.

### Next action

Check the next CI run after integrating the latest `main` head and the JVM 11 correction. Confirm stable API 37.1 installation, API 37.2 emulator boot, Room migration tests, APK assembly, schema verification, signing, and artifact upload. Correct any remaining failure within PR #13. Do not merge without the user's explicit approval.

---

## Milestone 112 - UI Revamp Batch 6: Chronicle Detail Data and Presentation

**Date recorded:** 2026-11-28  
**Development phase:** UI Revamp - Batch 6  
**Status:** Complete; CI-verified, merged to `main`, and physical-device tested.

### Objective

Replace summary-only Spiral Abyss and Imaginarium Theater detail pages with source-backed floor, chamber, lineup, round, and performance details. Keep Stygian Onslaught limited to its currently validated summary.

### Source evidence

- Account export: docs/genshin_865157206.json
- Exported: 2026-11-27T20:42:56Z
- Export blob SHA: 0c9dc9967c37cd58744495eaeddb4ead780e93bb
- Spiral Abyss current and previous sections contain floor/chamber stars, battle halves, character IDs/portraits/levels, completion date parts, and Ley Line Disorder text.
- Imaginarium Theater contains cycle statistics, round lineups with names/elements/levels/portraits, round medal state, finish dates, round blessing names, and fight_statisic performance fields.
- This export has no dedicated Stygian Onslaught record section. No detailed Stygian history or team data is inferred.

### Implemented on ui-revamp/batch-6-chronicle-details

- Added typed presentation mapping for Spiral Abyss cycles, floors, chambers, battle halves, character lineups, stars, completion timestamps, and Ley Line Disorder text.
- Resolves Spiral Abyss character names and fallback portraits/levels from the already retrieved account roster; does not display raw character IDs.
- Expanded Theater round mapping with character portraits, names, elements, levels, rarity, medal state, finish date, and both ordinary and splendour_buff.buffs blessing names.
- Maps source-provided Theater performance values for highest damage, most defeats, most damage taken, and total battle time. Character names are resolved against the account roster when possible.
- Passes roster context into the current/previous Abyss and Theater destinations.
- Keeps missing, unavailable, malformed, and stale section handling distinct. Stale records receive a visible freshness note.
- Added mapper tests for Abyss floors/chambers/teams and partial/malformed data, plus Theater character, blessing, date, and performance mapping.

### Files changed

- app/src/main/java/com/irminsul/codex/presentation/ChroniclePresentationMapper.kt
- app/src/main/java/com/irminsul/codex/presentation/ChronicleSections.kt
- app/src/main/java/com/irminsul/codex/presentation/VisualsSection.kt
- app/src/test/java/com/irminsul/codex/presentation/ChroniclePresentationMapperTest.kt
- docs/PROGRESS_MILESTONES.md

### Verification

- Repository source and export schema were retrieved from GitHub before implementation.
- GitHub Actions run #943: https://github.com/berao609/Irminsul-codex/actions/runs/46089004111 - Success.
- Unit tests: passed.
- Debug APK assembly: passed.
- Room schema snapshot verification and upload: passed.
- Debug APK signature verification: passed.
- Debug APK artifact: irminsul-codex-debug-v1.0.0-dev.943-vc945 (artifact ID 15239923231; 15,879,814 bytes).
- Room instrumentation tests were skipped by the workflow because the changed files do not affect Room persistence or schema.
- PR #14 was marked ready for review and merged to `main`.
- Latest integrated branch CI: run #968 succeeded, including unit tests, Debug APK build, Room schema verification/upload, APK signature verification, and artifact upload.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.968-vc970` (artifact ID 15244045907; SHA-256 `34a839872cdf2fbcd7dbfa1f6776512ef926692da8d65b92fabe249f923794df`).
- Physical-device verification: user confirmed Batch 6 was successful and tested.

### Remaining

- Detailed Stygian Onslaught history and team data remain out of scope until a real source record payload is captured and validated.

### Next action

Batch 6 is complete. Continue with the next planned UI Revamp task.



---

## Milestone 113 - UI Revamp Batch 6: Consistent Expanded Endgame Cards

**Date recorded:** 2026-11-28  
**Development phase:** UI Revamp - Batch 6  
**Status:** Complete; CI-verified, merged to `main`, and physical-device tested.

### Change

- Applied the Theater card's non-collapsible presentation to Spiral Abyss and Stygian Onslaught.
- Both cards now render their content continuously; their section headers no longer show a collapse icon or respond to collapse clicks.
- Preserved the Spiral Abyss current/previous cycle selector and all existing record content.
- Preserved the existing Stygian Onslaught best-record summary and source-data limitations.
- No data mapping, retrieval, or other Chronicle section behavior was changed.

### Implementation

- Commit: `a03425bdc888d2c35aba9f586edddd5173e0bc7e`
- File: `app/src/main/java/com/irminsul/codex/presentation/ChronicleSections.kt`

### Verification

- Implementation commit: `a03425bdc888d2c35aba9f586edddd5173e0bc7e`.
- Latest branch-head CI run #968: success; unit tests, Debug APK build, Room schema verification/upload, APK signature verification, and artifact upload passed.
- Debug APK artifact: `irminsul-codex-debug-v1.0.0-dev.968-vc970` (artifact ID 15244045907; SHA-256 `34a839872cdf2fbcd7dbfa1f6776512ef926692da8d65b92fabe249f923794df`).
- Physical-device verification: user confirmed the three endgame cards remain expanded and the Abyss cycle selector works.
- PR #14 merged to `main` in commit `2e5e2a016135f52a9487d41e48d7aee645ee44fc`. No successful build or device result is claimed.

### Next action

No further work is pending for Milestone 113.
