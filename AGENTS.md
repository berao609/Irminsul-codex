# AGENTS.md

## Purpose

Irminsul Codex is a native Android application for Genshin Impact account data retrieval, analysis, and progression intelligence. The long-term product direction is documented in `docs/SPECS.md`.

This file provides repository-wide instructions for AI coding agents and automated contributors. Follow it before making changes.

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
