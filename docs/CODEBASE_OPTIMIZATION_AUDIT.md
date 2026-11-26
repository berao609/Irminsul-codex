# Codebase Optimization Audit - Progress Queue

## Project identity

- **Repository:** `berao609/Irminsul-codex`
- **Working branch:** `main` (repository default branch, per `AGENTS.md`)
- **Record purpose:** Track the phased codebase optimization audit independently of the development milestone history.
- **Audit mode:** Investigation completed. Remediation is proceeding in separately authorized, focused implementation batches.
- **Overall status:** In progress
- **Current phase:** Remediation Batch 3 - AUD-006 (CPU-bound work off the main thread)
- **Last updated:** 2026-11-26

## 1. Objective

Conduct a comprehensive, evidence-based review of the current Irminsul Codex codebase before further feature implementation. Identify actual defects, avoidable complexity, performance risks, architectural weaknesses, build and testing inefficiencies, and worthwhile simplifications.

The audit must help answer:

1. What does the repository currently contain and how is it organized?
2. Which parts are already sound and should be preserved?
3. Where is complexity, duplication, coupling, or unnecessary work present?
4. Are there measurable or credible performance, reliability, privacy, or maintainability risks?
5. Which improvements are necessary, which are optional, and which are not justified?
6. What is the safest and most efficient order for any later remediation?

The goal is not to rewrite the application or minimize code at the expense of correctness. Retain necessary validation, security, accessibility, error handling, tests, and clear architecture.

## 2. Governing constraints

- Follow the repository's current `AGENTS.md`, `README.md`, `docs/SPECS.md`, and `docs/PROGRESS_MILESTONES.md`.
- Re-read relevant repository sources when the audit moves into a new technical area. Do not rely on chat summaries when current repository evidence is available.
- Keep this audit record separate from `docs/PROGRESS_MILESTONES.md`. The milestone file remains the historical record of development milestones; this file tracks audit phases, findings, decisions, and the next audit action.
- Do not modify application source code, tests, build configuration, dependencies, workflows, or product specifications during the audit.
- Do not implement recommendations during the audit.
- Do not perform unrelated cleanup, formatting, restructuring, or refactoring.
- Do not commit, push, pull, create branches, or change repository settings as part of the investigation. A repository documentation update specifically requested by the user is separate from those audit restrictions.
- Protect account data, credentials, signing material, and private information. Do not reproduce secrets in this record.
- Do not add further audit documents unless the user explicitly requests them. Record findings and progress in this file.
- Do not assume a tool, coding skill, external repository, or optimization philosophy is suitable without checking its fit against the actual project.
- Distinguish repository inspection from builds, tests, CI verification, and physical-device verification. Do not claim a check passed unless its result was observed.

## 3. Audit queue

Statuses used:

- **Not started** - investigation has not begun.
- **In progress** - evidence gathering and assessment are underway.
- **Blocked** - progress requires unavailable access, evidence, or a user decision.
- **Complete** - the defined scope has been investigated, findings recorded, and limitations disclosed.
- **Deferred** - intentionally postponed with a reason.
- **Not applicable** - checked and shown not to apply to this repository or audit scope.

### Part 1 - Repository inventory

**Status:** Complete

**Purpose:** Establish an accurate map of the repository before drawing architectural or performance conclusions.

**Initial inventory evidence (2026-11-26):**
- Recursive Git tree on `main`: 175 entries, not truncated; 122 tracked file blobs, approximately 1.72 MB total.
- Root structure: single Android app module (`app`), Gradle wrapper/configuration, version properties, two GitHub Actions workflows, and documentation/reference material under `docs/`.
- App source: one Android manifest, one very large `MainActivity.kt` (146,608 bytes), plus separate API, data/catalogue, local Room storage, mapper, repository, domain analysis/model/validation, ViewModel, and login UI packages.
- App tests: JVM unit tests across API, catalogue, mapper, repository, domain analysis/model/validation, and ViewModel; one Room instrumented test file under `androidTest`.
- Root/app build configuration and version catalog are present. Gradle wrapper files were not present in the recursive tree inventory.
- CI workflows: `.github/workflows/build.yml` and `.github/workflows/release.yml`.
- Largest tracked files by size include `docs/PROGRESS_MILESTONES.md` (289,767 bytes), `MainActivity.kt` (146,608 bytes), five captured Hoyolab HTML/CSS reference files (78-116 KB each), `docs/SPECS.md` (116,288 bytes), and the audit/reference documentation.
- These are inventory observations only. Large file size is not by itself proof of a defect; the UI source and reference captures require context and further inspection.


**Initial structural observations (not yet classified as findings):**
- `MainActivity.kt` is unusually large relative to the other source files and is a clear candidate for focused responsibility/structure review in Part 2 and Compose review in Part 4.
- Documentation/reference capture files are among the largest tracked assets. Their purpose, duplication, and whether they are needed at runtime or solely for offline development must be checked before any recommendation to remove or relocate them.
- The repository has a single app module. This is not inherently a problem; modularization should be considered only if concrete coupling or build/ownership benefits are demonstrated.

**Additional inventory checks:**
- README explicitly states that the Gradle Wrapper is not included and uses an installed Gradle distribution for builds. This confirms the tree observation; it is not automatically a defect.
- Root `.gitignore` covers common Gradle/build outputs, local configuration, APK/AAB files, and keystore files.
- `AndroidManifest.xml` declares INTERNET permission, disables backup, and defines the launcher activity. Manifest permissions and security configuration will be reviewed more deeply in Part 7.
- README and the Battle Chronicle reference analysis identify the captured web materials as development references, not runtime dependencies. However, `docs/share-page.html` is publicly tracked and the capture contains a UID-like account identifier. A privacy exposure concern is recorded below. Do not repeat the identifier in audit notes.
- Build configuration uses a single app module, Android SDK 34, min SDK 26, target SDK 34, Kotlin 2.0.0 / AGP 8.5.0, Compose BOM 2024.06.00, Room 2.6.1, and Gradle 8.14.8 in CI. This is inventory context; compatibility or upgrade recommendations belong in Part 6.

**Finding AUD-001 - Public account-specific reference capture**
- **Part:** 1 (inventory discovery; detailed privacy review in Part 7)
- **Classification:** High-priority privacy risk (not a credential/security compromise claim)
- **Evidence:** `docs/share-page.html` is publicly tracked; its content scan confirmed a UID-like identifier. `docs/BATTLE_CHRONICLE_REFERENCE_ANALYSIS.md` also notes the capture contains account-specific profile information and explicitly recommends sanitization.
- **Impact:** Publishing account identifiers in a development reference capture exposes personal game-account metadata. A UID is not an authentication secret, but it can enable account lookup or unwanted correlation.
- **Confidence:** Verified presence; impact depends on user privacy preference and identifier sensitivity.
- **Recommendation:** Remove or sanitize account-specific identifiers and review embedded URLs/content before retaining a public reference capture. Preserve a sanitized reference if it remains useful for implementation.
- **Verification for later remediation:** Scan the sanitized tracked file and related captures for UID/account-specific values; inspect the diff to ensure layout/reference usefulness remains and no account-specific identifiers remain.
- **Implementation status:** No remediation performed during this read-only audit.

**Inventory limitations:** This first pass used GitHub's recursive tree, file metadata, and targeted configuration/document inspection. It did not inspect every source file, calculate test coverage, build the app, run tests, profile runtime performance, or validate on a physical device.

**Investigation scope:**
- Repository root and module structure.
- Kotlin source packages and major classes.
- Resources, assets, raw data, and documentation.
- Tests and test organization.
- Gradle files, version catalogs, wrapper availability, and build scripts.
- GitHub Actions workflows and other automation.
- Generated, duplicated, obsolete, or unusually large tracked files.
- Existing project instructions and architectural documentation.

**Expected output:** A concise repository map, major component inventory, evidence-backed structural observations, and a list of areas requiring deeper inspection.

### Part 2 - Architecture and code complexity

**Status:** Complete


**Purpose:** Assess whether the current architecture is understandable, cohesive, appropriately layered, and proportionate to the application's actual needs.

**Investigation scope:**
- Presentation, domain, data, and integration boundaries.
- Dependency direction and coupling.
- Responsibilities of major classes and services.
- Duplicate logic and repeated transformations.
- Abstractions, wrappers, managers, and interfaces that may or may not provide value.
- Oversized classes and files with unrelated responsibilities.
- State ownership and data-flow clarity.
- Unnecessary indirection, premature generalization, and overengineering.
- Existing architecture that is working and should remain unchanged.

**Expected output:** Architecture map, concrete complexity findings, justified simplification opportunities, and risks of proposed structural changes.

#

## Part 2 - Architecture and code complexity (in progress)

### Scope correction
- This audit is limited to application code and build-relevant configuration. Documentation, milestone history, HTML/CSS captures, and other non-app assets are excluded from code optimization assessment unless they directly affect build/runtime behavior or security/privacy.

### Evidence inspected
- Kotlin production source inventory and byte sizes from the recursive Git tree.
- Full declaration/range inventory of `MainActivity.kt` (2,988 lines) and its key composable/function boundaries.
- `AccountViewModel.kt`, `AccountIntelligenceEngine.kt`, `HoYoLabAccountRepository.kt`, and `AccountRepository.kt`.
- Project README app architecture/build context was used only where it described the application/build setup.

### Finding AUD-002 - UI responsibilities concentrated in MainActivity.kt
- **Classification:** Maintainability / cohesion concern; confirmed structural complexity, not a runtime-performance defect.
- **Evidence:** `MainActivity.kt` is 2,988 lines / 146,423 bytes. `CodexScreen` spans roughly 994 lines (163-1156) and contains application screen state and orchestration including refresh/export/login handling. The same file also contains a 536-line MaterialsPlannerSection (1172-1707), 155-line LineupSimulatorSection, 224-line CharacterShowcaseSection, 223-line CharacterShowcaseDetail, recommendation cards, chronicle rendering, metric components, and supporting formatting/rendering functions.
- **Impact:** Makes navigation, screen-level state, unrelated UI sections, and presentation helpers harder to locate and review together. Increases risk that changes to one screen feature are entangled with other UI behavior. File size alone does not prove Compose recomposition or frame performance problems.
- **Confidence:** High for concentration and file boundaries; performance impact unmeasured.
- **Recommendation:** Candidate for later extraction into feature/section files (Home, account/JSON export, materials planner, lineup simulator, character showcase/detail, reusable chronicle/metric UI) while preserving existing behavior and state ownership. Do not split solely by line count; Part 4 will examine Compose/state concerns and propose a safe order.
- **Implementation status:** None. Audit remains read-only.


### Architecture observations (not yet defects)
- The repository has recognizable API, data, domain, repository, and presentation/ViewModel boundaries.
- `AccountViewModel` receives repository and snapshot-store abstractions and manages a StateFlow UI state; it delegates deterministic analysis to `AccountIntelligenceEngine` and uses coroutine scope.
- `HoYoLabAccountRepository` adapts network export/mapping behind the domain repository contract. It injects retrieval and mapping functions, supporting tests without real network/credentials.
- `AccountIntelligenceEngine` orchestrates multiple dedicated deterministic analyzers rather than owning their individual rules. This is a reasonable separation in principle; Part 3 will inspect logic overlap and Part 5 data flow before judging whether the analyzer/model count is excessive.
- The UI is not fully separated into source files, despite domain/data boundaries. The material planner, lineup simulator, character showcase, and other large UI sections are colocated with the main screen.


### Part 2 activity log
- 2026-11-26: Reviewed source-file inventory, MainActivity declaration boundaries, screen state/wiring, ViewModel state/workflows, repository abstraction, deterministic analysis engine, overview composer, and representative build/stat analyzers. Confirmed AUD-002 UI concentration in MainActivity. Architecture boundaries appear intentional in sampled core flow; no duplication or layering defect confirmed yet. Completed Part 2. No app code or build files modified.



## Part 3 - Kotlin and Android code quality

**Status:** Complete

**Purpose:** Review correctness, idiomatic Kotlin, lifecycle safety, and maintainability in the Android implementation.

**Investigation scope:**
- Nullability and type modeling.
- Coroutine scope, cancellation, dispatchers, and exception handling.
- Lifecycle-aware work and resource cleanup.
- State and data ownership.
- Parsing and validation boundaries.
- Error representation and propagation.
- Threading and blocking operations.
- Collection and memory behavior.
- Naming, readability, and consistency with existing conventions.
- Relevant unit-test coverage for deterministic logic.

**Expected output:** Specific code-quality findings with file and symbol references, severity, practical impact, and focused recommendations.

### Part 4 - Jetpack Compose performance and UI architecture

**Status:** Complete

**Purpose:** Identify avoidable UI work and state-management patterns that could affect responsiveness, rendering, or maintainability.

**Investigation scope:**
- Recomposition triggers and state reads.
- State hoisting and ownership.
- Stability and unnecessary allocations.
- Lazy list and grid keys, item construction, and rendering.
- Navigation and screen lifecycle behavior.
- Derived state and expensive calculations in composition.
- Image loading and resource usage.
- Animation and layout complexity.
- Large composables and reusable UI boundaries.
- Accessibility and adaptive layout concerns where relevant.

**Expected output:** Evidence-backed Compose findings. Static inspection must be distinguished from measured runtime performance; do not label a suspected recomposition issue as measured without profiling evidence.

### Part 5 - Data processing and local-first design

**Status:** Not started

**Purpose:** Evaluate the reliability and efficiency of account retrieval, parsing, persistence, and offline analysis.

**Investigation scope:**
- HoYoLAB integration boundaries and response handling.
- Account retrieval and section-level failure isolation.
- Raw-source field preservation and normalization.
- JSON parsing and transformation cost.
- Unified account data model and data provenance.
- Room schema, snapshot storage, migrations, and queries.
- Local caching and restoration.
- Daily Note refresh boundaries.
- Material planner data and calculation boundaries.
- Offline behavior and avoidable network dependencies.
- Credential handling, privacy, and sensitive-data exposure risks.

**Expected output:** Data-flow map, data-integrity and privacy findings, local-first assessment, and clearly identified unverified assumptions.

### Part 6 - Gradle, dependencies, build, and CI optimization

**Status:** Not started

**Purpose:** Find unnecessary build complexity, unused dependencies, avoidable build work, and fragile automation.

**Investigation scope:**
- Root and app Gradle configuration.
- Plugin and dependency declarations.
- Actual dependency usage and duplication.
- Build variants and configuration.
- Gradle task and configuration overhead where evidence is available.
- Resource and APK-size contributors.
- GitHub Actions job structure, caching, concurrency, and repeated work.
- Test/build task selection.
- Signing and artifact handling.
- Compatibility and maintenance risks of proposed build changes.

**Expected output:** Dependency inventory, build/CI findings, and a prioritized list of safe optimizations. Do not remove or update dependencies during the audit.

### Part 7 - Testing, reliability, and security

**Status:** Not started

**Purpose:** Assess whether important behavior is adequately tested and whether the application handles failures and sensitive data safely.

**Investigation scope:**
- Existing unit and instrumentation tests.
- Parsing, calculation, transformation, and repository tests.
- Missing, null, malformed, stale, partial, and failure cases.
- Coroutine cancellation and concurrent request behavior.
- Persistence and migration coverage.
- UI and navigation reliability.
- Network failure and authentication handling.
- Logging, credentials, permissions, storage, and exported data.
- CI verification coverage and gaps.
- Claims in documentation compared with available test evidence.

**Expected output:** Coverage map, concrete reliability/security findings, risk classification, and verification gaps. Static review alone must not be described as a penetration test or complete security assessment.

### Part 8 - Coding-agent and development workflow optimization

**Status:** Not started

**Purpose:** Determine how coding agents and development instructions can reduce unnecessary work without introducing more process or documentation overhead.

**Investigation scope:**
- Current `AGENTS.md` effectiveness and clarity.
- Existing development and testing instructions.
- Repeated agent mistakes or avoidable repository exploration, if supported by evidence.
- Applicability of Ponytail-style simplification principles.
- Official Android and Jetpack Compose guidance relevant to identified issues.
- Whether existing tools, skills, or workflows already solve the observed problem.
- Cost and maintenance burden of adding agent skills or automation.
- Android-phone and Termux workflow constraints.

**Expected output:** A fit assessment and only those workflow changes that address demonstrated friction. Do not install tools, add skills, or rewrite instructions during this audit.

### Part 9 - Consolidated optimization plan

**Status:** Not started

**Purpose:** Reconcile all audit findings into a safe, ordered, evidence-based improvement plan.

**Investigation scope:**
- Remove duplicate findings and reconcile cross-phase dependencies.
- Separate defects and risks from optional improvements and cosmetic refinements.
- Rank by impact, likelihood, urgency, implementation cost, and regression risk.
- Identify quick wins versus changes requiring design or architecture decisions.
- Preserve working behavior and existing product decisions.
- Define verification and regression checks for each proposed change.
- Identify items that should not be changed.
- Identify deferred items and the reason for deferral.

**Expected output:** A prioritized remediation backlog, recommended implementation sequence, dependencies, verification criteria, and explicit items requiring user approval.

## 4. Finding classification

Every finding must use one of these categories:

- **Critical defect:** Demonstrably breaks an essential workflow, produces materially incorrect behavior, compromises security or authorization, or creates serious data-integrity risk.
- **High-priority defect or risk:** A significant, evidence-backed reliability, performance, security, or maintainability issue.
- **Medium-priority issue:** A real problem with limited scope or a manageable workaround.
- **Low-priority issue:** A minor but demonstrated inefficiency or maintainability concern.
- **Efficiency opportunity:** A justified way to reduce work, complexity, resource use, or maintenance burden that is not itself a defect.
- **Optional improvement:** A potentially useful enhancement that is not required to correct an established problem.
- **Cosmetic refinement:** A presentation or style preference without material operational impact.
- **Inconclusive:** A suspected issue that cannot be established with available evidence.
- **No issue found in inspected scope:** The relevant area was checked and no actionable issue was identified within the stated inspection limits.

Do not promote optional improvements, cosmetic preferences, or inconclusive suspicions into defects.

## 5. Evidence requirements

For each material finding, record:

1. Finding ID, such as `AUD-001`.
2. Audit part.
3. Category and severity.
4. Exact file path and relevant class, function, configuration key, or workflow step.
5. What was observed.
6. Why it matters to the actual Irminsul Codex requirements.
7. Whether the conclusion is verified, inferred, suspected, or unverified.
8. Recommended action, if any.
9. Risks and regression concerns.
10. How a later change should be verified.

Use direct repository evidence. Cite exact paths and symbols; include line numbers only when they have actually been checked and are stable enough to be useful.

Do not infer runtime performance from source appearance alone. Use profiling or measured build/test evidence when a claim requires it. If the required measurement cannot be obtained, record the limitation.

## 6. Progress update protocol

Update this file after each meaningful audit work session or completed investigation step.

For every update:
- Preserve the nine-part queue and all earlier valid findings.
- Update only statuses supported by actual work.
- Add newly inspected files and evidence to the relevant part.
- Record findings with stable IDs.
- Record decisions and reasons, including rejected approaches where they affect future work.
- Record blockers and unresolved questions.
- State the exact current stopping point.
- State one clear next action.
- Do not mark a part complete while material in-scope questions remain unassessed.
- Do not rewrite historical findings merely to make the report shorter.
- Do not create a new document for each part; this file is the audit's single progress record.

When resuming after a pause:
1. Read this file.
2. Re-read current `AGENTS.md` and any governing source needed for the next task.
3. Verify relevant repository state and previously recorded evidence.
4. Reconcile changes since the last recorded update.
5. Continue from the last verified stopping point.

## 7. Audit activity log

### 2026-11-26 - Part 1 initial inventory completed


- Retrieved the recursive `main` Git tree (175 entries, non-truncated) and reviewed root/app/source/test/docs/workflow structure plus key project instructions and build configuration.
- Inventory: 122 tracked files, ~1.72 MB total. `MainActivity.kt` is 146,608 bytes; the milestone record is 289,767 bytes; the five captured HTML/CSS reference files are 78-116 KB each.
- Identified follow-up inspection candidates without prematurely classifying them as defects: MainActivity responsibilities, purpose/runtime relevance of web reference captures, and whether project setup actually relies on a missing Gradle wrapper.
- **No application code, build configuration, dependencies, tests, or workflows were modified.**

### 2026-11-26 - Part 1 repository inventory completed

- Inspected the recursive repository tree, root/app/source/test/docs structure, project instructions, README structure, specifications headings, build configuration, version catalog, Gradle settings, Android manifest, CI/release workflows, and reference-analysis document.
- Verified the Gradle Wrapper is intentionally absent and README requires an installed Gradle distribution.
- Confirmed `MainActivity.kt` is 146,608 bytes and tracked reference captures are substantial. Size is a review candidate, not automatically a defect.
- Verified the public `docs/share-page.html` contains a UID-like account identifier. Recorded AUD-001; did not copy the identifier into this audit file.
- Part 1 is complete at inventory scope. Deeper code responsibilities, performance, dependencies, tests, and security remain for later parts.
- No app code/config/tests/workflows were changed.


### 2026-11-26 - Audit plan initialized

- **Status:** Preparation only; technical audit not started.
- **Repository:** `berao609/Irminsul-codex`.
- **Branch:** `main`, identified as the repository default branch by `AGENTS.md`.
- **Sources retrieved for preparation:** `AGENTS.md`, `README.md`, `docs/PROGRESS_MILESTONES.md) (latest content was retrieved but the large response was truncated in the preparation view), and the `docs/` directory listing.
- **Confirmed repository guidance:** Read required project instructions before code changes; preserve local-first architecture; prefer incremental refactoring; update the development milestone file for meaningful development milestones; do not claim unobserved verification.
- **Scope decision:** Keep this audit queue in this separate file. Do not turn it into a development milestone or mix audit progress into historical milestone entries.
- **Changes to application code:** None as part of the audit. This entry records only audit preparation.
- **Unresolved before technical audit:** Confirm with the user that `main` is the intended audit target before beginning Part 1.
- **Next action:** Obtain branch confirmation, then start Part 1 - Repository inventory.

## 8. Current state and next action


- **Overall audit status:** Complete - awaiting user review.
- **Current phase:** Audit complete - awaiting user review.
- **Completed audit parts:** Parts 1, 2, 3, 4, 5, 6, 7, 8, and 9.
- **In-progress audit parts:** None.
- **Blocked items:** None.
- **Application source changes made by this audit:** None.
- **Current stopping point:** All nine audit parts completed. The consolidated plan is recorded below. No implementation has been authorized or performed.
- **Next action:** User reviews the consolidated plan and decides whether to authorize a specific implementation batch.

## Part 9 - Consolidated optimization plan (completed)

### Scope and reconciliation
- Consolidated app-code and build-relevant findings AUD-002 through AUD-010.
- AUD-001 concerns a UID-like value in a publicly tracked HTML reference capture. It is outside the narrowed app-code/build optimization scope and is excluded from the remediation backlog. It must not be used to expand this audit into general documentation cleanup.
- No duplicate findings were found among AUD-002 through AUD-010. AUD-003 is a specific export correctness issue; AUD-006 is a broader execution-context risk. AUD-007 is a release test-gate gap; it is distinct from the Debug workflow's existing selective test gate.
- The priority labels below are remediation order, not claims of measured runtime impact. Static risks remain unmeasured where explicitly stated.
- All recommendations remain unimplemented. The user must approve the chosen implementation scope before any app files are changed.

### Prioritized remediation backlog

| Order | Finding | Priority | Recommended action | Verification required |
|---|---|---|---|---|
| 1 | AUD-008 | High - credential protection | Replace ordinary SharedPreferences storage for the HoYoLAB session cookie with a Keystore-backed encrypted design compatible with minSdk 26. Keep non-sensitive preferences separate. Ensure explicit clear/sign-out removes persisted and in-memory credentials. | Verify raw token is not stored in plaintext; test save, restart, replacement, clear/sign-out, key/decryption failure, and confirm cookie destination remains restricted to HoYoLAB HTTPS endpoints. |
| 2 | AUD-009 | High - data integrity | Add a real version-1-to-2 Room migration test using the exact legacy schema and representative records. Preserve Room schema exports in version control. | Test on-disk migration, byte-for-byte raw JSON reconstruction, metadata, ordering, multiple chunks, Unicode boundaries, and row integrity. Run in CI. |
| 3 | AUD-003 | Medium - correctness; quick fix | Correct the document-save callback so a null output stream or any failed write cannot display a success message. Prefer a small testable helper around the write operation. | Test successful write, null stream, open failure, write failure, and success notification only after a completed write. |
| 4 | AUD-006 | High - responsiveness risk; measure then fix | Move CPU-heavy export mapping, deterministic analysis, chunk preparation, and snapshot restoration mapping off the main thread using explicit injectable dispatchers. Keep Room operations on Room-managed dispatchers and preserve cancellation. | Capture baseline and post-change traces for cold import, cached restore, and full-roster analysis. Confirm main-thread responsiveness, cancellation, UI progress/error states, and deterministic output. |
| 5 | AUD-007 | Medium - release reliability | Add a test gate to the Release workflow before publication. At minimum run unit tests against the tagged source. Decide whether Room instrumentation tests must also run for every release or whether the main-branch gate plus migration coverage is sufficient. | Demonstrate that a failing test blocks release publication; verify successful tagged release path in a controlled test workflow. |
| 6 | AUD-005 | Medium - efficiency opportunity | Consider replacing eager character-card rendering with a lazy grid, but only after confirming parent/nested scroll constraints and preserving existing filter, sort, and selection behavior. | Compare composition count, scrolling smoothness, memory/frame timing with a large roster; regression-test filters, sorting, selection, and detail opening. |
| 7 | AUD-002 | Medium - maintainability | Incrementally extract cohesive UI sections from MainActivity into feature files. Preserve state ownership and public behavior. Start with a self-contained section rather than a broad rewrite. | Build and run relevant UI flows after each extraction; compare behavior and keep diffs narrowly scoped. Avoid splitting solely by line count. |
| 8 | AUD-004 | Low - lifecycle improvement | Use lifecycle-aware collection for the ViewModel StateFlow where appropriate, such as collectAsStateWithLifecycle. | Compile and verify state collection across foreground/background and activity recreation. |
| 9 | AUD-010 | Low - workflow efficiency | Make AGENTS.md reading requirements conditional by task while keeping privacy, credential, test, cancellation, and Git safety rules unconditional. | Review revised instructions against representative UI, data, dependency, and release tasks; confirm no mandatory safety checks were weakened. |

### Optional improvements, not required fixes
- Add a Debug workflow concurrency group with cancellation of superseded runs only if older in-progress Debug artifacts do not need to be retained. Do not add paid runners.
- Consider removing the direct lifecycle-runtime-ktx dependency only after confirming it is unused across all source sets and not needed by another declared dependency. Expected benefit is small.
- Consider a Gradle Wrapper only if reproducible local setup across contributors becomes a requirement. Current CI pins Gradle explicitly and the user works primarily from Android/Termux; adding wrapper files is not necessary to fix a demonstrated defect.
- Consider R8/minification only as a separate measured APK-size effort with keep-rule, signing, and release smoke-test coverage.
- Collect Gradle task timing and APK-size baselines before making build-speed or size claims.

### Recommended implementation batches and dependencies
1. **Security and data integrity:** AUD-008 and AUD-009. Treat as separate focused changes, with credential lifecycle tests and migration fixtures/schema history.
2. **Correctness and execution responsiveness:** AUD-003 and AUD-006. AUD-003 is a small isolated fix. For AUD-006, record baseline traces first, then change dispatching with injectable test dispatchers and cancellation coverage.
3. **Release assurance:** AUD-007 after the relevant test tasks are reliable and the migration test is included in CI. Keep the Debug workflow's current selective instrumentation policy unless evidence supports changing it.
4. **UI efficiency and maintainability:** AUD-005 and AUD-002. Assess nested scrolling and capture a baseline before replacing the grid; perform UI extraction incrementally, not as a single rewrite.
5. **Low-risk workflow cleanup:** AUD-004 and AUD-010. These can be scheduled independently after the higher-risk changes.

### Preserve; do not optimize without new evidence
- Keep the single app module and current recognizable presentation/domain/data boundaries. No evidence justifies multi-module decomposition or a broad architecture rewrite.
- Keep raw source JSON preservation, unknown-field retention, and the normalized account model. Their processing cost is an intentional trade-off for fidelity and offline analysis.
- Keep the existing local-first snapshot restore and deterministic analysis path. Do not make cloud AI or a remote backend mandatory.
- Keep the current initial API concurrency and sequential character-detail batching unless rate-limit and latency measurements justify a change.
- Keep Room's chunked transactional storage and legacy fallback while strengthening migration verification.
- Keep Debug persistent signing, separate Release signing, signature verification, temporary keystore cleanup, KVM detection, and emulator TCG fallback.
- Do not remove Coil or extended Material icons based on name alone; the inspected UI uses them.
- Do not upgrade AGP/Kotlin/Compose/Room as a blind version sweep. No specific compatibility failure was established.
- Do not enable R8, add dependencies, add custom build logic, or introduce paid CI runners as part of routine cleanup.

### Audit limitations and confidence
- This was a static repository review. No app build, unit/instrumentation test suite, runtime profiler, large-account benchmark, real-device credential extraction, dynamic security scan, penetration test, production API test, or real version-1 database migration was executed as part of this audit.
- AUD-006's main-thread execution paths are established by static inspection, but the degree of user-visible jank is unmeasured.
- AUD-008 confirms plaintext-capable SharedPreferences usage, but no physical-device extraction test was performed.
- AUD-009 confirms the absence of a visible migration test and committed schema history in the inspected repository tree; actual migration failure or data loss has not been demonstrated.
- Optional performance/build improvements require before-and-after measurements. No speedup, memory reduction, or APK-size reduction is claimed.
- The audit did not modify application source, tests, Gradle configuration, dependencies, workflows, AGENTS.md, or product specifications.

### Part 9 activity log
- 2026-11-26: Reconciled AUD-002 through AUD-010, excluded the out-of-scope documentation capture concern AUD-001, prioritized remediation, identified dependencies and verification criteria, and documented preservation/deferment decisions. Part 9 completed. Awaiting user review and explicit implementation authorization.


## 9. Audit completion criteria

The audit may be closed when:

- All nine parts have been completed, marked not applicable with evidence, or explicitly deferred with user-visible reasons.
- Material findings have evidence, classification, impact, and recommended verification.
- Contradictions and dependencies between findings have been reconciled.
- Confirmed defects are separated from optional improvements and inconclusive items.
- A prioritized optimization plan and safe implementation order are recorded.
- Remaining uncertainties and limitations are explicit.
- The user has reviewed the consolidated plan and decided what, if anything, should be implemented.

Closing this audit does not mean the application is perfect or that every optional improvement must be implemented. It means the defined review has been completed to the documented scope.


## Part 3 - Kotlin and Android code quality (completed)

### Evidence inspected
- `AccountViewModel.kt`: snapshot restoration, retrieval/export workflow, deterministic analysis, StateFlow updates, cancellation and exception handling.
- `HoYoLabClient.kt`: OkHttp setup, synchronous request helpers wrapped in IO context, response envelope/error handling, UID lookup, daily-note retrieval, export concurrency.
- `HoYoLabResponseParser.kt` and `HoYoLabExportResponseValidator.kt`: HTTP/JSON envelope and required structure validation.
- `HoYoLabAccountRepository.kt`: result handling and mapping boundary.
- `RoomAccountSnapshotStore.kt`: transactional persistence, JSON chunking, legacy fallback.
- `UpgradeCostCalculator.kt`: request validation, segment matching, cost accumulation with overflow checking, inventory validation.
- `MainActivity.kt`: Compose-level refresh, login, export-file saving, preference state, and UI progress/error handling.


### Finding AUD-003 - Export file save can report success when output stream is unavailable
- **Classification:** Medium-priority reliability issue.
- **Evidence:** In `MainActivity.kt`, the CreateDocument result handler checks URI and JSON, then uses `context.contentResolver.openOutputStream(uri)?.use { ... }`. The stream is nullable, but the following code reports `Saved JSON file` regardless of whether `openOutputStream` returned null.
- **Impact:** A file-provider/storage failure can produce a false success message, leaving the user believing their JSON export was saved.
- **Confidence:** Verified by static code inspection; actual provider behavior not reproduced.
- **Recommendation:** Treat a null output stream as a save failure and only show success after a non-null stream has been opened and written without exception.
- **Verification:** Test successful write, null stream, and thrown write/open errors through a small testable save helper or equivalent targeted verification.
- **Implementation status:** No change made.


### Kotlin/Android code-quality observations (no confirmed issue in sampled areas)
- `AccountViewModel` consistently rethrows `CancellationException` rather than swallowing coroutine cancellation.
- Network calls in `HoYoLabClient` use `withContext(Dispatchers.IO)` and close responses via `use`; parsing is validated before data mapping.
- Snapshot persistence is transaction-wrapped and uses chunk rows with a compatibility fallback for legacy raw JSON storage.
- `UpgradeCostCalculator` validates requests and cost segments and uses `Math.addExact` to detect overflow rather than silently wrapping.
- These are positive patterns to preserve during optimization.
- A potential credential-storage concern was observed: `MainActivity.kt` persists the HoYoLAB cookie in app preferences. Its security implications and storage alternatives are reserved for Part 7; no conclusion is made here.


### Part 3 activity log
- 2026-11-26: Reviewed ViewModel, network client/parser/validator, repository, local snapshot store, cost calculator, and relevant MainActivity account flows. Confirmed AUD-003. Part 3 complete. No app source/config/tests/workflows modified.


## Part 4 - Jetpack Compose performance and UI architecture (completed)

### Evidence inspected
- `MainActivity.kt`: root screen state, tab rendering, UI-state collection, vertical scrolling, conditional tab sections, roster filtering/sorting, character grid rendering, character detail sections, and artifact/constellation sorting.
- Focused review of `CodexScreen` (lines 163-1156), `CharacterShowcaseSection` (lines 1902-2125), and `CharacterShowcaseDetail` (lines 2164-2386).
- Android official architecture guidance was checked for lifecycle-aware UI state collection and Compose/ViewModel patterns.

### Finding AUD-004 - UI state is collected without lifecycle awareness
- **Classification:** Low-priority efficiency opportunity / lifecycle concern.
- **Evidence:** `MainActivity.kt` collects `accountViewModel.uiState` using `collectAsState()` at line 171. The root screen is activity-composed UI, but the collector is not lifecycle-aware.
- **Impact:** Collection can continue while the UI is not in an active lifecycle state, doing avoidable UI observation work.
- **Confidence:** Verified implementation pattern; actual measurable resource impact not established.
- **Recommendation:** Consider `collectAsStateWithLifecycle()` from Lifecycle Compose. This is a targeted modernization, not evidence of a current user-visible defect.
- **Verification:** Ensure state updates are still reflected on resume and account workflows remain correct. No runtime measurement was performed.
- **Implementation status:** None.

### Finding AUD-005 - Expanded character roster is eagerly rendered
- **Classification:** Medium efficiency opportunity; performance impact unmeasured.
- **Evidence:** `CharacterShowcaseSection` filters and sorts the roster, then groups it with `chunked(3)` and renders each row and card inside regular `Column` composables. It does not use a lazy grid/list. Roster expansion is gated, but once expanded all filtered character cards are composed together; the current roster is around 67 characters.
- **Impact:** Creates and lays out all visible roster cards and artwork slots at once rather than composing only viewport items. This may increase initial work and memory when users expand a large roster.
- **Confidence:** Verified rendering pattern; runtime jank/frame or memory impact is not measured.
- **Recommendation:** Consider replacing the expanded roster with `LazyVerticalGrid` or another lazy container, but only after checking nested scrolling constraints and keeping the current grid selection/filter/sort behavior. Avoid an unnecessary change if profiling shows no meaningful benefit at current roster size.
- **Verification:** Compare composition/layout and scroll performance on-device with roster expanded, confirm selection, filters, sort, and detail panel behave identically.
- **Implementation status:** None.

### Compose observations (not confirmed defects)
- The screen uses a root vertical scroll and conditional content for Home, Visuals, and Account. Tab changes recompose the screen, but conditional content prevents every tab's main section from being rendered simultaneously.
- The roster's filtering and sorting are simple operations over a modest list. They are recomputed during composition; no finding is raised for this alone because the list is small and there is no performance measurement.
- Character detail collections are small (talents, constellations, artifacts); eager rendering and sorting in these areas are not prioritized without evidence.
- No measured recomposition, frame-time, or memory issue was confirmed. Static source review cannot establish runtime performance.
- Official Android guidance recommends lifecycle-aware UI state collection such as `collectAsStateWithLifecycle()`. This supports AUD-004 as an opportunity, not a defect claim.

### Part 4 activity log
- 2026-11-26: Reviewed Compose state collection, tab content conditions, root scrolling, roster filtering/sorting and eager grid construction, character detail rendering, and collection sorting. Recorded AUD-004 and AUD-005. Part 4 completed as a static review with runtime profiling limitations stated. No app source/config/tests/workflows modified.


## Part 5 - Data processing and local-first design (completed)

### Evidence inspected
- `HoYoLabClient.kt`: seven initial account endpoints launched concurrently; character detail retrieved in sequential batches of eight with a 200 ms inter-batch delay; raw response preservation; JSON export assembly.
- `AccountExportDataPreserver.kt`: recursive deep-copy of JSONObject/JSONArray values, preserving source fields and nulls while adding property names.
- `AccountExportMapper.kt`: conversion from raw JSON into normalized domain models, structured data, coverage/freshness, and character integrity.
- `RoomAccountSnapshotStore.kt` and `SnapshotJsonChunks.kt`: chunked raw JSON persistence, transaction boundary, retrieval and legacy fallback.
- `AccountViewModel.kt`: local snapshot restore, persistence, analysis scheduling, and UI state flow.
- `AccountIntelligenceEngine.kt`: deterministic analysis stages over normalized snapshots.

### Finding AUD-006 - CPU-heavy mapping and analysis are not explicitly dispatched off the main thread
- **Classification:** High-priority responsiveness risk; requires measurement.
- **Evidence:** `HoYoLabAccountRepository.fetchAccountExport` calls `exportJson` then immediately calls `mapExport(json)` in the caller context. The default network exporter uses `withContext(Dispatchers.IO)`, but that context is restored when it returns. The repository mapper therefore runs in the original caller context. The ViewModel is normally invoked from Compose/main and calls `analyzeSnapshot` synchronously; `AccountIntelligenceEngine.analyze` runs multiple analyzers synchronously. `RoomAccountSnapshotStore.save` also performs raw JSON chunk splitting and entity-list construction before its first Room suspend operation; `getLatest` maps the restored JSON after the DAO suspend call returns.
- **Impact:** Mapping a full export, running all deterministic analyzers, splitting a large raw JSON export into chunks, and mapping restored snapshots may occupy the main thread and cause UI stalls/ANRs for large account data.
- **Confidence:** High for the execution-context path by static inspection; actual duration and user-visible jank not measured.
- **Recommendation:** Move CPU-bound mapping, chunk preparation, and deterministic analysis to an injected/default `Dispatchers.Default` (or an appropriate IO dispatcher for serialization/storage preparation). Keep database operations on Room's managed dispatchers. Make dispatcher ownership explicit and injectable for tests; avoid indiscriminately wrapping all suspend operations.
- **Verification:** Profile cold import, cached restore, and analysis with a full roster; inspect main-thread traces and frame timing. Confirm cancellation and UI progress/error behavior remain correct.
- **Implementation status:** None.

### Data-flow observations (no confirmed defect)
- The network exporter performs seven independent initial requests concurrently, then serially batches character details in groups of eight with a short delay. This is a reasonable balance between parallelism and endpoint/request limits; do not increase concurrency without rate-limit evidence.
- Raw source data is deliberately preserved and stored alongside normalized domain models. This supports the local-first archive and future analysis, but creates expected memory/CPU overhead from JSON deep-copying, serialization, parsing, and normalized mapping. Preserve raw export fidelity unless product requirements explicitly change.
- Room persistence stores large raw JSON in 32 KiB chunks, avoids oversized SQLite text values, writes snapshot metadata/chunks transactionally, and supports legacy unchunked rows.
- Snapshot restoration reads local Room data and runs deterministic analysis without contacting HoYoLAB. This is a key local-first behavior to preserve.
- `getHistory` currently loads and maps every history record into full `AccountExport` objects. No call-site evidence or large history size was established in this review; investigate usage before changing the API or loading strategy.
- The JSON data preserver recursively copies source objects. This is intentional lossless preservation, not automatically redundant copying. Any optimization must retain unknown fields, nulls, and nested values.
- No remote AI dependency is required for the deterministic analysis path inspected.

### Part 5 activity log
- 2026-11-26: Reviewed network fan-out/detail batching, raw JSON preservation, mapper transformations, chunked Room persistence, snapshot restoration, and deterministic analysis execution context. Recorded AUD-006. Part 5 completed as static review; no app source/config/tests/workflows modified.


## Part 6 - Gradle, dependencies, build, and CI optimization (completed)

### Evidence inspected
- Root `build.gradle.kts`, `settings.gradle.kts`, and `gradle.properties`.
- `gradle/libs.versions.toml`, `app/build.gradle.kts`, and `version.properties`.
- `.github/workflows/build.yml` and `.github/workflows/release.yml`.
- Android manifest and ProGuard rules.
- Main source imports/usages for Coil and Material icons to check whether the corresponding visible dependencies are actually used.
- Repository tree for Gradle wrapper files and Room schema snapshots.

### Finding AUD-007 - Release workflow builds and publishes without running tests
- **Classification:** Medium-priority release reliability gap.
- **Evidence:** `.github/workflows/release.yml` validates the tag/version, configures signing, runs `gradle assembleRelease --no-daemon`, verifies the APK signature, calculates a checksum, and publishes the APK. It does not run unit tests or Room instrumentation tests. The Debug build workflow runs unit tests and conditionally runs Room instrumentation tests.
- **Impact:** A release tag can publish an APK even if the tagged source has failing tests. A successful Release compilation and signature check do not establish behavioral correctness.
- **Confidence:** Verified directly from the complete release workflow.
- **Recommendation:** Add a test gate to the release workflow before publishing. At minimum run `testDebugUnitTest` (or an equivalent unit-test task against the tagged source). Decide whether Room instrumentation tests should also run on every release or whether the existing main-branch CI gate is sufficient; the release workflow should not silently imply test coverage it does not perform.
- **Verification:** Create a test tag on a controlled branch or workflow test revision and confirm test failure prevents release publication.
- **Implementation status:** None.

### Build and CI observations (no confirmed defect)
- The project has one app module, a centralized version catalog, Kotlin DSL, and KSP for Room. This is already a relatively simple build structure; no multi-module or build-logic restructuring is justified by this audit.
- CI uses JDK 17 and Gradle 8.14.8. The Android Gradle Plugin is 8.5.0, Kotlin is 2.0.0, Compose compiler plugin is 2.0.0, and Room is 2.6.1. These are pinned versions, but this static review did not establish a specific compatibility failure. Upgrade as a coordinated, tested compatibility change rather than a blind version sweep.
- The build workflow uses `gradle/actions/setup-gradle@v6`, which provides Gradle setup/caching support. It runs unit tests on every push and runs Room instrumentation tests when relevant persistence/schema/build files change or on manual dispatch. The path filter includes Room storage, mapper, snapshot model/contracts, Gradle configuration, and the workflow itself.
- The workflow deliberately checks for KVM and retains an emulator TCG fallback. This is robust for GitHub-hosted runner variability. Do not remove this fallback or introduce paid runners as an assumed optimization.
- Debug signing uses a dedicated persistent certificate supplied through Actions secrets; Release signing requires a separately configured keystore. Temporary keystore files are removed with an `always()` cleanup step. Release checkout disables persisted Git credentials.
- Build workflow checkout uses `fetch-depth: 0`, supporting reliable comparison against the push event's `before` SHA for selective instrumentation testing. Reducing history depth is not recommended without proving that the diff base is always available.
- The repository does not contain Gradle wrapper scripts/properties. CI installs the pinned Gradle distribution explicitly, while local development relies on an installed Gradle distribution. This is a reproducibility/onboarding trade-off, not a build defect in the current setup. Consider a wrapper only if consistent local setup across contributors becomes a requirement.
- Coil and extended Material icons are used by the UI; they are not candidates for removal based on dependency name alone.
- `lifecycle-runtime-ktx` appears in the direct dependency catalog but its APIs were not identified in the inspected main-source imports. Confirm its use across all source sets and transitive requirements before considering removal; its potential impact is likely small.
- Release minification is disabled. Enabling R8 could reduce APK size, but it may introduce reflection/serialization/Room/Compose issues and should be a separate measured release-size project with mapping and smoke-test verification.
- No build scan, Gradle task timing, dependency insight report, APK size baseline, or clean-room build benchmark was available. No claim of actual build slowness or dependency bloat is made.

### Part 6 activity log
- 2026-11-26: Reviewed Gradle scripts, version catalog, app dependencies, build/release workflows, signing configuration, manifest, ProGuard rules, wrapper presence, and visible dependency usages. Recorded AUD-007. Part 6 completed as static review; no app source/config/tests/workflows modified.


## Part 7 - Testing, reliability, and security (completed)

### Evidence inspected
- Unit-test inventory and focused tests for API response parsing/validation, export assembly/preservation, mapping, repository behavior, chunk splitting, analysis, ViewModel workflows, and domain validation.
- `RoomAccountSnapshotStoreTest.kt`, `SnapshotJsonChunksTest.kt`, and `IrminsulDatabase.kt`, including the version 1-to-2 migration.
- `MainActivity.kt`: credential persistence, refresh/export flows, document-provider save callback, and user-facing cookie handling.
- `HoYoLabLoginDialog.kt`: WebView configuration, cookie extraction, cookie cleanup, and navigation behavior.
- `HoYoLabClient.kt`: HTTPS endpoints, Cookie header use, transport error handling, cancellation, and timeouts.
- `AndroidManifest.xml`: permissions, exported activity, and backup policy.
- Build workflow test gates and Room schema snapshot handling.

### Finding AUD-008 - HoYoLAB session cookie is stored in ordinary SharedPreferences
- **Classification:** High-priority credential-protection concern.
- **Evidence:** `MainActivity.kt` reads and writes the full `ltoken_v2`/`ltuid_v2` cookie under the `cookie` key in `getSharedPreferences("irminsul_prefs", Context.MODE_PRIVATE)`. The cookie is persisted after manual Save & Sync and after WebView login. The WebView extraction intentionally reduces the captured cookie to these two values, and the manifest disables Android application backup.
- **Impact:** `MODE_PRIVATE` provides app-sandbox access control, but it does not encrypt the preference value at rest. A valid HoYoLAB session cookie is a bearer credential; extraction from a compromised, rooted, or otherwise exposed app-private data store could expose the account session.
- **Confidence:** Verified storage path by static inspection. No device compromise or filesystem extraction test was performed.
- **Recommendation:** Store session credentials using Android Keystore-backed encryption, or a currently supported encrypted-storage design appropriate to the app's minSdk. Keep the UID and non-sensitive preferences in ordinary preferences. Add an explicit credential-clear/sign-out path and ensure clearing removes the persisted cookie and in-memory value. Avoid logging the cookie or including it in crash/diagnostic reports.
- **Verification:** Inspect persisted storage to confirm the raw token is absent; test save, app restart, credential replacement, sign-out/clear, and failed key/decryption behavior. Confirm no cookie is sent to any destination other than the intended HoYoLAB HTTPS endpoints.
- **Implementation status:** None.

### Finding AUD-009 - Room version 1-to-2 data migration has no visible migration test or committed schema history
- **Classification:** High-priority data-integrity verification gap.
- **Evidence:** `IrminsulDatabase.MIGRATION_1_2` creates the JSON chunk table, reads existing snapshot IDs and raw JSON lengths, copies each legacy JSON value into ordered chunks using SQL `substr()`, and clears the old rawJson field. The only instrumented Room test file covers in-memory snapshot save/history and account separation; it does not build a version-1 database and execute the migration. The repository tree contains only `app/schemas/.gitkeep`, not a committed Room schema JSON. CI checks that KSP generates schema version 2 and uploads that generated file as an artifact, but this does not preserve a versioned schema history in the repository.
- **Impact:** A defect in the SQL migration or schema evolution could corrupt or lose existing account snapshots during an upgrade without being caught by the current visible tests. Missing committed schema history also weakens Room's migration review and reproducibility.
- **Confidence:** Verified migration implementation, test inventory, repository tree, and CI workflow. Migration execution against a real version-1 database was not performed.
- **Recommendation:** Add a migration test that creates/populates the exact version-1 schema with representative small and multi-chunk raw JSON, opens it through Room with `MIGRATION_1_2`, and verifies exact reconstructed JSON, metadata, ordering, and row integrity. Preserve exported Room schema snapshots in version control so future migrations can be reviewed and tested against known schemas. Keep the existing CI generation check, but make schema history a maintained source artifact rather than relying only on an ephemeral build output.
- **Verification:** Run the migration test on CI and assert byte-for-byte raw JSON equality before and after migration, including a payload larger than multiple chunk boundaries and Unicode boundary cases.
- **Implementation status:** None.

### Test and reliability observations (no additional confirmed defect)
- The unit suite is broad across pure parsing, data preservation, mapping, account analysis, recommendation rules, validation, calculations, repository behavior, and ViewModel state transitions. This is a strong foundation; retain deterministic unit tests as the default for domain behavior.
- API response parsing has explicit tests for success, API errors, HTTP errors, empty/malformed bodies, and bounded server messages. Repository tests cover success, mapping failure paths, malformed JSON, and coroutine cancellation.
- Snapshot chunk splitting has tests for multi-chunk round trips, UTF-16 surrogate-pair boundaries, and empty input. The Room instrumented tests cover immutable history, raw JSON preservation, and separation of same-UID records by server.
- The Room migration is more consequential than ordinary CRUD and needs its own on-disk migration test. Current in-memory tests do not exercise the 1-to-2 migration.
- The document save callback still contains the nullable-output-stream success-reporting issue already recorded as AUD-003. No duplicate finding was created.
- The login WebView clears cookies/cache/history/form data when captured or dismissed, extracts only `ltoken_v2` and `ltuid_v2`, and does not expose a JavaScript bridge in the inspected code. JavaScript and third-party cookies are enabled for the HoYoLAB login flow. Consider restricting navigation to the expected HoYoLAB domains and handling unexpected external navigation explicitly; no exploit was demonstrated.
- The app requests only the INTERNET permission in the manifest, declares the launcher activity exported as required, and sets `allowBackup="false"`. The inspected API endpoints use HTTPS and the client has finite connect/read timeouts. No cleartext-traffic opt-in or broad storage permission was found in the manifest.
- The cookie is passed to the HoYoLAB client as an HTTP Cookie header. The inspected UI states it will not be sent to an AI provider, and no AI provider integration or cookie forwarding path was present in the reviewed code. This is a code-review observation, not a runtime network-capture result.
- The test suite does not visibly cover the Android document-provider save callback, WebView login/cookie lifecycle, or actual OkHttp transport against a controlled server. Add focused tests when these platform boundaries change; do not force brittle UI tests for every pure domain behavior.
- No dynamic security scan, penetration test, real-device storage inspection, or production API test was performed.

### Part 7 activity log
- 2026-11-26: Reviewed test inventory and representative parser/repository/ViewModel/Room tests, Room migration SQL and schema history, credential persistence, WebView cookie handling, network transport, manifest permissions, and CI test/schema gates. Recorded AUD-008 and AUD-009. Part 7 completed as static review; no app source/config/tests/workflows modified.


## Part 8 - Coding-agent and development workflow optimization (completed)

### Evidence inspected
- Root `AGENTS.md` instructions for repository reading, architecture, privacy, testing, release, milestone, and Git safety.
- Debug and Release GitHub Actions workflows.
- `.gitignore`, Gradle configuration, and repository tree for automation/support scripts.
- Existing audit findings from Parts 1-7 for cross-part workflow implications.

### Finding AUD-010 - Agent instructions can cause unnecessary broad context loading and documentation churn
- **Classification:** Low-priority workflow efficiency opportunity.
- **Evidence:** Root `AGENTS.md` lists README, `docs/SPECS.md`, and `docs/PROGRESS_MILESTONES.md` as minimum reading before changing code, and separately requires milestone updates for each meaningful development milestone. It also requires `RELEASE_PROCESS.md` and `version.properties` for version/signing/release changes. The user has explicitly narrowed this optimization effort to app implementation/build behavior and asked that unrelated repository documentation not become audit targets.
- **Impact:** If agents interpret every listed document as mandatory for every narrow app change, they may load unrelated context and produce unnecessary documentation edits. This increases task time and makes focused app work harder to review.
- **Confidence:** Instruction text verified. Actual agent token/time overhead was not benchmarked.
- **Recommendation:** In a later authorized AGENTS.md revision, make reading conditional by task: always inspect the directly affected source/tests/build files; read product specs for behavior/architecture changes; read version/release instructions only for version/signing/release changes; consult milestone history only when continuing or recording a development milestone. Keep the privacy, credential, test, cancellation, and Git safety rules unconditional. Do not modify AGENTS.md during this audit.
- **Implementation status:** None.

### Workflow observations (no confirmed defect)
- Debug CI runs unit tests on every push and conditionally runs Room instrumentation tests for persistence, mapper, schema, and build-related changes, plus manual runs. This is a reasonable balance for free GitHub-hosted runners; retain the selective test gate.
- The workflow uses `fetch-depth: 0` because it compares the push's before-SHA with the current SHA to determine whether Room tests are required. Do not reduce checkout depth unless the change-detection mechanism is replaced and tested.
- Persistent Debug signing, unique run-number-based Debug versioning, signature verification, and artifact upload are automated. Release signing is separate, checkout credentials are not persisted, and temporary signing material is removed.
- The Release workflow's missing test gate is already recorded as AUD-007 in Part 6; no duplicate finding was created.
- The Debug workflow has no concurrency group. Rapid pushes can queue redundant builds. An optional branch-scoped concurrency group with `cancel-in-progress: true` could reduce wasted runner minutes, but only if cancelling older in-progress Debug builds is acceptable. Preserve the latest successful artifact requirement and do not add paid runners.
- No repository-specific developer scripts or custom build-logic modules were identified in the inspected workflow/configuration file set. The one-module Gradle setup and version catalog are already straightforward.
- `.gitignore` excludes generated Gradle/build outputs, local SDK configuration, APKs, keystores, and profiling dumps. No obvious generated-artifact tracking issue was found in the inspected rules.
- No workflow execution-time baseline, runner queue analysis, or agent-effort benchmark was available. The above workflow opportunities are static assessments, not measured speedups.

### Part 8 activity log
- 2026-11-26: Reviewed agent instructions, Debug/Release workflows, build support files, ignore rules, and existing audit findings. Recorded AUD-010 and optional CI concurrency improvement. Part 8 completed as static review; no app source/config/tests/workflows or AGENTS.md modified.


---

## 10. Remediation implementation tracker

**Purpose:** Continue this audit record through implementation and verification of every approved batch. The findings and investigation history above remain intact. This tracker records the later implementation phase and supersedes the earlier audit-complete / awaiting-review status.

**Tracking rules:**
- A batch is not cleared until its relevant CI checks and required physical-device checks pass.
- Record implementation, commits, CI, APK evidence, and device verification separately.
- Do not claim measured performance improvement without comparable profiling evidence.
- Keep follow-up enhancements visible without reopening an otherwise cleared batch.

### Batch overview

| Batch | Audit finding(s) | Status | Notes |
|---|---|---|---|
| 1 | AUD-008 credential protection; AUD-009 Room migration integrity | Cleared | CI #521 and #522 passed. User completed credential/session and sign-out device checks. |
| 2 | AUD-003 JSON export false-success fix | Cleared | Final CI #530 passed. User confirmed export flow is clear. Cancellation during an active save remains a future UX enhancement. |
| 3 | AUD-006 CPU-heavy processing off main thread; AUD-011 Chronicle rendering follow-up | CI/device verification pending | CI #542 passed for AUD-006. The #542 device test exposed eager Chronicle rendering; AUD-011 follow-up is recorded below. |
| 4 | AUD-007 Release test gate | Not started | Next approved batch after Batch 3 is cleared. |
| 5 | AUD-005 lazy roster grid | Not started | Preserve existing filtering, sorting, and selection behavior. |
| 6 | AUD-002 incremental UI extraction | Not started | Extract only cohesive sections, incrementally. |
| 7 | AUD-004 lifecycle-aware StateFlow collection | Not started | Low-risk lifecycle improvement. |
| 8 | AUD-010 agent workflow efficiency | Not started | Keep safety and Git rules unconditional. |

### Batch 1 - Credential protection and Room migration integrity

**Findings:** AUD-008 and AUD-009  
**Status:** Cleared

**Implementation:**
- Added Android Keystore-backed AES-GCM encryption for the HoYoLAB cookie.
- Kept non-sensitive preferences separate from the encrypted credential ciphertext.
- Added legacy plaintext migration and explicit credential clearing behavior.
- Added Room version-1-to-version-2 migration instrumentation coverage and committed schema snapshots.

**Principal files:**
- `app/src/main/java/com/irminsul/codex/integration/security/HoYoLabCredentialStore.kt`
- `app/src/androidTest/java/com/irminsul/codex/data/local/IrminsulDatabaseMigrationTest.kt`
- `app/schemas/com.irminsul.codex.data.local.IrminsulDatabase/1.json`
- `app/schemas/com.irminsul.codex.data.local.IrminsulDatabase/2.json`
- `app/build.gradle.kts`

**Commits:** `a060320a`, `dcaa8066`, `2dc605dc`, `e0d57b1a`, `f1b204b3`, `f44f5ef8`  
**CI:** #521 passed, including all eight Android instrumentation tests. #522 passed, including build and schema verification.  
**Physical-device verification:** User confirmed the session survived installation and app restarts; sign-out returned to login while the saved character roster remained available; fresh login succeeded and remained connected after another restart.  
**Verification boundary:** The UI checks did not directly inspect encrypted bytes on-device. Encryption and migration behavior are covered by automated tests.

### Batch 2 - JSON export save-result integrity

**Finding:** AUD-003  
**Status:** Cleared

**Implementation:**
- Added a testable JSON document writer.
- Treats a null output stream, open failure, write failure, or close failure as a failed save.
- Reports success only after the output stream has been written and closed successfully.
- Handles document-picker cancellation without displaying a false success message.

**Principal files:**
- `app/src/main/java/com/irminsul/codex/data/export/JsonDocumentWriter.kt`
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/test/java/com/irminsul/codex/data/export/JsonDocumentWriterTest.kt`

**Commits:** `a509acede`, `c62e9f73`, `d4c03692`, `f2f83954`, `ec38bc78`  
**CI:** #526 passed, #527 passed, #528 failed, #529 passed, and final integrated run #530 passed.  
**Physical-device verification:** User confirmed the export flow is clear.  
**Follow-up requirement:** Once an export/save operation begins, the UI currently has no cancel control. Add a safe cancellation action in a future relevant batch. A cancelled or partially written file must never be reported as successfully saved. This follow-up does not reopen the completed false-success fix.

### Batch 3 - CPU-heavy processing off the main thread

**Finding:** AUD-006  
**Status:** Implementation committed; CI verification passed; physical-device verification pending.

**Implementation:**
- `HoYoLabAccountRepository` now performs export JSON-to-domain mapping on an injected CPU dispatcher, defaulting to `Dispatchers.Default`.
- `AccountViewModel` now runs deterministic account analysis on an injected CPU dispatcher. Its factory forwards the dispatcher and analyzer dependencies.
- `RoomAccountSnapshotStore` now performs JSON chunk splitting, chunk joining, and restored snapshot mapping on an injected CPU dispatcher.
- Room DAO queries and transactions remain suspend Room operations on Room-managed executors.
- Coroutine cancellation is rethrown rather than converted into ordinary mapping or analysis failures.
- No dependency, Room schema, JSON format, account model, or analysis rule was changed.

**Files changed:**
- `app/src/main/java/com/irminsul/codex/data/repository/HoYoLabAccountRepository.kt`
- `app/src/main/java/com/irminsul/codex/data/local/repository/RoomAccountSnapshotStore.kt`
- `app/src/main/java/com/irminsul/codex/presentation/viewmodel/AccountViewModel.kt`
- `app/src/test/java/com/irminsul/codex/data/repository/HoYoLabAccountRepositoryTest.kt`
- `app/src/test/java/com/irminsul/codex/presentation/viewmodel/AccountViewModelTest.kt`
- `app/src/androidTest/java/com/irminsul/codex/data/local/RoomAccountSnapshotStoreTest.kt`

**Commits:** `6f752366`, `9f7ae9a7`, `c8618646`, `d061d1a3`, `27be3508`, `a0488215`, `b1760fba`, `448d2823`, `3a88ba07`, `393e117c`, `d8719317`, `9bc551fe`.

**Automated verification:**
- CI #541 passed unit tests, Debug APK build, Room schema verification, and APK signature verification. Room instrumentation tests were skipped on that run because its changed-file set did not include the instrumentation test.
- CI #542 passed on commit `9bc551fe1b59ff08c244fbb398d684a7973c01c8`. Unit tests passed (267 tests); Room instrumentation tests, Debug APK build, Room schema verification, APK signature verification, and artifact upload all passed.
- Earlier intermediate runs #532 through #540 failed while tests were being adjusted for asynchronous dispatch and thread verification. Those runs are superseded by the later corrected run #541 and final run #542. Do not treat an intermediate failure as a final batch result.

**Performance evidence limitation:** The original audit established the main-thread execution paths by static inspection, but no baseline System Trace or comparable before-change timing was captured before implementation. No quantified speedup or frame-time improvement is claimed. Physical-device responsiveness checks and any available post-change profiling remain pending.

**Next actions:**
1. Install the CI #542 Debug APK on the physical device.
2. Test a fresh account retrieval and confirm the UI remains responsive while the account export is mapped and saved.
3. Restart the app and confirm cached snapshot restoration and deterministic analysis complete without a new account download.
4. Confirm full-roster analysis remains responsive and produces the same account findings and recommendations.
5. Clear Batch 3 only after these device checks pass. Then begin Batch 4, AUD-007.

### Continuing update log

- 2026-11-26: Recorded Batch 1 and Batch 2 implementation and verification history; started the Batch 3 remediation record. Batch 3 source changes are committed. CI #542 passed all unit, Room instrumentation, build, schema, signature, and artifact checks. Physical-device verification remains pending.


### Batch 3 follow-up - AUD-011 bounded Chronicle rendering

**Finding:** AUD-011 - Unbounded recursive Chronicle rendering  
**Status:** Implementation prepared; CI and physical-device verification pending.

**Observed during physical-device testing of Debug build #542:**
- Account retrieval completed and the account information was reported correct.
- Expanding Account Chronicles caused visible frame-rate drops, most noticeably in World Exploration & Collectibles.
- The expanded section exposed source implementation details such as image URLs, icon paths, and internal entry identifiers.
- Static source inspection confirmed that `ChronicleStructuredNode` recursively traversed every nested object and array and composed all entries immediately. This is a code-level explanation consistent with the report; no runtime trace or measured frame-time data is available.

**Focused remediation:**
- Keep the original structured payload intact. No mapper, export, snapshot, or persistence transformation is required.
- Render immediate object fields only. Nested objects and arrays remain collapsed until the user opens them.
- Render expanded arrays through a bounded-height `LazyColumn` with stable item keys rather than eagerly composing every entry.
- Hide technical field names and URL/artwork-filename values from the presentation layer.
- Keep the existing Character Showcase as the primary detailed character presentation. Chronicle remains a compact account-data overview.
- Preserve missing, unavailable, failed, and stale section handling.

**Files added/changed:**
- `app/src/main/java/com/irminsul/codex/MainActivity.kt`
- `app/src/main/java/com/irminsul/codex/presentation/ChroniclePresentationRules.kt`
- `app/src/test/java/com/irminsul/codex/presentation/ChroniclePresentationRulesTest.kt`

**Verification plan:**
1. Run the repository Debug CI workflow, including JVM tests and APK build/signature/artifact checks.
2. Install the new APK and compare Chronicle expand/collapse responsiveness, especially World Exploration & Collectibles.
3. Confirm URLs, artwork paths, and internal IDs are no longer presented as account details.
4. Confirm profile values, exploration values, and independently expanded groups remain readable.
5. Confirm current/previous Spiral Abyss and Imaginarium Theater sections still expand and preserve their status messaging.
6. Confirm JSON export and restored account data are unchanged.

**Performance evidence boundary:** Lazy composition and deferred nested traversal are structural optimizations. Do not claim a measured FPS improvement until the user confirms the device retest or comparable profiling evidence is available.



## AUD-011 Follow-up - Failed generic Chronicle renderer replacement

**Device result:** The user tested the build containing the bounded recursive renderer and reported that scrolling/expansion lag became worse and technical reference IDs and URLs remained visible. The attempted mitigation is therefore **failed**, not accepted.

**Cause confirmed by source inspection:**
- A recursive generic renderer still exposed raw payload values through incomplete denylist filtering.
- It created nested Compose content for large object/array trees.
- A nested `LazyColumn` was placed inside the screen's vertically scrolling content, increasing layout/scroll complexity.

**Corrective implementation direction:** Remove the generic raw-payload renderer from Visuals. Replace it with section-specific presentation mapping that uses explicit allowlists of supported user-facing fields. Render exploration regions as a bounded horizontal card strip. Keep unsupported and unknown source fields out of the UI. Preserve raw structured data unchanged for local analysis and JSON export.

**Verification:** The previous renderer is rejected based on physical-device feedback. The replacement requires CI and a fresh device test. No FPS improvement is claimed until the user retests the replacement build.


## AUD-011 Follow-up - CI #550 compilation failure and correction

**CI evidence:** Workflow run [#550](https://github.com/berao609/Irminsul-codex/actions/runs/45860847660) failed at `app:compileDebugKotlin` before unit tests, APK packaging, signature verification, or artifact upload. The compiler reported `MainActivity.kt:2837:1: This annotation is not repeatable.`

**Root cause:** `ChronicleOverviewSection` had two consecutive `@Composable` annotations. This was introduced in the section-specific renderer change. It is a direct source compilation defect, not a Gradle, dependency, signing, or test failure.

**Correction:** Remove the duplicate annotation and retain the single required `@Composable` annotation. No runtime behavior or data mapping is changed by this correction.

**Verification boundary:** The correction must pass a fresh CI run before an APK can be offered. The allowlisted renderer remains unverified on the physical device; the earlier bounded recursive renderer remains rejected based on the user's device feedback.
