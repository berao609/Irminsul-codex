# AGENTS.md

## Purpose

Irminsul Codex is a native Android application for Genshin Impact account data retrieval, analysis, and progression intelligence. The long-term product direction is documented in `docs/SPECS.md`.

This file provides repository-wide instructions for AI coding agents and automated contributors. Follow it before making changes.

## Repository identity and context isolation (highest priority)

This repository is **Irminsul Codex**. Its only canonical repository is `berao609/Irminsul-codex`.

These rules take priority over conversation carryover, summaries, remembered workflows, examples, and instructions belonging to any other project:

- **Repository lock:** For every GitHub, file, branch, commit, issue, pull request, workflow, and CI operation in this project, verify and use the exact repository identity `berao609/Irminsul-codex`. Never infer a repository from a previous conversation or a similarly named project.
- **Instruction isolation:** Apply only instructions stored in this repository, especially this `AGENTS.md`, its `README.md`, and task-relevant files in this repository. Never import another repository's governance rules, file-edit restrictions, handoff formats, architecture, paths, milestones, or workflow.
- **Context quarantine:** Treat details from other projects as unrelated and unusable here, even if they appear in conversation history, memory, generated summaries, or tool output. Do not mention or consult another project unless the user explicitly asks to switch projects.
- **No cross-project writes:** Never create, edit, commit, or otherwise mutate a different repository as part of Irminsul Codex work. Do not copy another project's files or procedures into this repository unless the user explicitly requests that specific integration.
- **Path and evidence discipline:** Resolve every file path relative to this repository. Before editing, verify the target file, current branch, current file blob SHA, and intended change against this repository. Before reporting status, verify the relevant commit, CI run, and artifact in this repository.
- **Ambiguity stop:** If a request, instruction, path, branch, or tool result appears to point outside `berao609/Irminsul-codex`, do not guess or silently switch. Pause and ask one concise clarification.
- **Conversation reset:** At the start of each new task, re-anchor to this repository and this file. Do not carry over the previous task's branch, PR, implementation plan, or unfinished work unless it is verified in this repository and relevant to the new request.

The required preflight identity is:

```text
Project: Irminsul Codex
Repository: berao609/Irminsul-codex
Default branch: main
```

## Task-specific context retrieval

Always read this file first. Then retrieve only the additional repository context that is relevant to the current task.

- **Always:** Inspect the exact source files, tests, and configuration that the change can affect. Check the current branch and relevant recent commits before editing.
- **Product behavior or user-facing UI:** Read the relevant README sections for implemented behavior, limitations, and navigation. Read `docs/SPECS.md` when the task changes agreed product behavior, architecture, data ownership, privacy, or technical boundaries.
- **Architecture or data-model changes:** Read the relevant sections of `docs/SPECS.md`, plus the affected domain/data/presentation source and tests.
- **Continuing existing work or recording a milestone:** Read the relevant entries in `docs/PROGRESS_MILESTONES.md` and the active audit/plan. Retrieve broader history only when it affects the change.
- **Build, dependency, or CI changes:** Inspect the relevant Gradle files, version catalog, and affected GitHub Actions workflow. Check dependency use before adding, removing, or updating dependencies.
- **Versioning, signing, or release changes:** Read `version.properties` and `RELEASE_PROCESS.md` before making changes.
- **Narrow bug fixes or isolated refactors:** Do not load unrelated product specifications, release instructions, or the full milestone history when the affected source, tests, and directly relevant documentation provide sufficient context.

Do not skip a source or instruction that is relevant merely to reduce context. Do not read unrelated material merely because it exists. Reassess the required context when the task changes.

## Architecture and implementation principles

- Platform: native Android.
- Language: Kotlin.
- UI: Jetpack Compose and Material 3.
- Architecture direction: layered, with presentation, domain, data, and external integration responsibilities separated as the codebase evolves.
- Data direction: local-first. Do not introduce a mandatory application backend without an explicit architecture decision.
- AI direction: Bring Your Own AI (BYOAI), behind provider-independent interfaces when implemented.
- Keep deterministic calculations, validation, and account rules in application logic. Do not make an AI model the authority for numerical results.
- Keep HoYoLAB networking and unofficial endpoint/signature behavior isolated from domain logic. Treat external API responses as untrusted and potentially incomplete or changing.
- Prefer incremental refactoring over broad rewrites or premature multi-module decomposition.
- Preserve existing behavior unless the task explicitly changes it.

## Data integrity and privacy

- Preserve source account data unless a documented schema or deliberate transformation requires otherwise.
- Distinguish missing, null, malformed, unavailable, and actual zero/false values. Never silently turn unknown data into a valid-looking default.
- Partial retrieval must remain distinguishable from complete retrieval. Surface section-level failures where appropriate.
- Do not claim that selected account exports are complete raw API archives or complete account inventories.
- Never log, commit, print, or expose HoYoLAB cookies, passwords, API keys, signing passwords, keystore contents, or other credentials.
- Never send HoYoLAB authentication cookies to AI providers.
- Do not add secrets or keystore files to the repository. Use the configured GitHub Actions secrets for CI signing.
- Treat current credential storage limitations documented in the README as real; do not describe ordinary SharedPreferences as encrypted storage.

## Code and testing expectations

- Follow the conventions already used by the surrounding Kotlin and Gradle files.
- Keep changes focused and avoid unrelated formatting or refactoring.
- Add or update unit tests for parsing, data transformation, status handling, calculations, and other deterministic behavior.
- Include tests for missing, null, malformed, boundary, and failure cases when relevant.
- Preserve coroutine cancellation semantics. Do not swallow `CancellationException` as an ordinary API failure.
- Run the most relevant unit tests and build tasks available in CI. Do not claim tests or builds passed unless their results were observed.
- If a task cannot be tested, state exactly what remains unverified.
- Do not add dependencies without a clear need and compatibility check.

## Versioning, signing, and CI

- `version.properties` is the source of truth for official Release `VERSION_NAME` and `VERSION_CODE`.
- Official Release version codes must increase for each published release and must never be reused or decreased.
- The Debug GitHub Actions workflow assigns a development version using the workflow run number. It calculates Debug `versionCode = VERSION_CODE + GITHUB_RUN_NUMBER` and Debug `versionName = VERSION_NAME-dev.GITHUB_RUN_NUMBER`.
- A new workflow run is required for a higher Debug version code. Re-running the same workflow run retains its run number and version.
- CI Debug APKs must use the persistent Debug signing certificate. Do not replace or regenerate that key casually; doing so breaks in-place updates.
- Release APKs use a separate persistent Release signing certificate. Switching between Debug and Release requires a one-time uninstall/reinstall because the certificates differ.
- Do not create release tags or publish official releases unless explicitly requested and the release prerequisites have been verified.
- Preserve the signature verification and temporary-keystore cleanup steps in CI.
- Do not change signing secrets or key material as part of unrelated tasks.

## Milestone and documentation discipline

- Update `docs/PROGRESS_MILESTONES.md` for each meaningful development milestone.
- Add a new numbered milestone rather than rewriting historical milestone records.
- Keep updating the active milestone as evidence arrives. Separate implementation status, CI verification, artifact evidence, and physical-device verification.
- Record exact commit SHAs, workflow run URLs and numbers, artifact names/IDs, and checksums when available.
- Distinguish completed, pending, failed, and unverified work. Never mark a milestone verified based only on source changes.
- Keep README descriptions aligned with what is actually implemented. Label planned capabilities as planned.
- Update `docs/SPECS.md` only when an explicit architecture or product decision changes; do not silently reinterpret agreed decisions.

## Git and change safety

- Work on the intended repository and branch. The default development branch is `main`.
- Make only changes needed for the authorized task.
- Do not delete, move, rename, or overwrite user data or unrelated files.
- Do not force-push, rewrite history, or change repository settings unless explicitly authorized.
- Report the exact files changed and commit SHA(s). Distinguish a committed change from a tested, released, or installed change.
- Never claim a GitHub Actions run, APK artifact, device installation, or release succeeded without checking the corresponding evidence.

## User workflow

The project owner primarily works from an Android phone using Termux, GitHub, and Obsidian. Give concise, sequential instructions when a manual device action is needed. Avoid assuming desktop-only tools are available. Do not ask the user to paste credentials or secrets into chat.

## Runtime architecture guardrails

Apply these execution patterns when they fit the affected workflow. They strengthen the layered architecture above; they do not authorize a broad rewrite or require a prescribed package tree.

- **MVVM with unidirectional state flow:** ViewModels own screen-level state and expose observable immutable UI state. UI emits user actions; ViewModels coordinate use cases and publish state changes. Composables render state and forward actions. Do not put account calculations, raw JSON parsing, credential handling, or network orchestration in Composables.
- **Lifecycle-aware observation:** Collect observable ViewModel state with lifecycle-aware Compose APIs. Keep transient UI events distinct from durable screen state. Preserve state across navigation or configuration changes only where the workflow requires it.
- **Cache-first, stale-while-revalidate:** When valid cached account data exists, render it promptly while refresh proceeds independently. Show its freshness and refresh status accurately. A refresh must not replace useful content with a blocking spinner or erase the last successful snapshot on failure. Replace cached sections only with validated results for those sections.
- **Section-aware synchronization:** Keep retrieval, freshness, and failure state independent for each account section. Partial success must remain usable and visible. Do not mark a whole snapshot complete when required sections failed or were not retrieved.
- **Explicit asynchronous ownership:** Run network and disk work away from the main thread using appropriate coroutine dispatchers. Keep dispatcher choice testable where practical. Propagate cancellation; never convert CancellationException into an ordinary failure. Cancel obsolete work when its owning screen, request, or conversation no longer needs it. Do not launch unmanaged work from Composables.
- **Repository and use-case boundaries:** ViewModels coordinate application workflows through domain-facing contracts. Repositories coordinate local and remote data sources; mappers validate and translate external representations. Keep provider-specific request details and persistence implementation types out of domain models and UI state.
- **Lightweight composition:** Keep dependency construction explicit through a small application composition root or an equally lightweight established mechanism. Do not introduce a DI framework, annotation processor, or new Gradle module without a demonstrated benefit. Reuse existing networking and persistence dependencies when suitable.
- **Cache integrity:** Treat cache entries as data with provenance, schema/version information, and timestamps. Validate data before publishing it as current. Preserve last-known-good data on transient failures, and make cache invalidation, migration, and pruning deliberate. Do not let a cache hide authentication expiry, malformed responses, or stale data.
- **Incremental serialization:** Prefer typed models at stable application boundaries. Migrate legacy JSON handling gradually, with fixtures and compatibility tests. Preserve required raw/source information and the existing export contract unless a separately approved change intentionally versions it. Never perform a lossy conversion merely to simplify UI models.
- **AI transport and orchestration:** Keep provider adapters behind provider-independent contracts. Streaming is a transport capability, not a domain dependency; support it where the selected provider offers it and provide clear completion, failure, and cancellation behavior. Build minimal, task-relevant context from normalized account data and deterministic results. Never send credentials, unrelated raw payloads, or unreviewed external knowledge as trusted facts.
- **Kotlin safety:** Avoid destructuring a List into more than five values; Kotlin's standard list component operators cover only the first five positions. Prefer named result models or indexed access for larger result sets.
- **Architecture is evidence-led:** These patterns describe desired responsibilities, not mandatory class names, file names, or a fixed directory layout. Inspect the current code and reuse sound existing structures. Refactor only when it improves correctness, testability, reliability, or maintainability for the active task.
