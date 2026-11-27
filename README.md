# Irminsul Codex

**An AI-Integrated Genshin Impact Account Intelligence and Progression Analyzer**

Irminsul Codex is an Android application designed to help Genshin Impact players understand their accounts, make better investment decisions, and plan their progression efficiently.

The product's primary purpose is to turn account data into actionable in-app intelligence. Irminsul Codex aims to identify development priorities, analyze account weaknesses, optimize farming, evaluate potential pulls, and help players decide what to focus on next. Account retrieval supports this analysis; JSON export is an optional utility for third-party readers and backups.

> **Project Status:** Early development. The app has four bottom-navigation destinations: Home, Visuals, Tools, and Account. A raised AI action sits at the center of the bottom bar and opens a chat-style bottom sheet. Tools hosts calculators and utilities, including the Materials Calculator. Account access is consolidated in the bottom navigation. Visuals contains the character showcase and account analysis. Tools contains the Materials Calculator and Lineup Simulator. Account contains appearance settings, HoYoLAB account management, and optional JSON export. The interface uses an edge-to-edge layout and supports System, Light, and Dark appearance modes, with the selected preference saved locally. Home presents the signed-in Genshin IGN, UID, HoYoLAB profile avatar, server, latest retrieved snapshot summary, Real-Time Notes including Resin and Parametric Transformer cooldown, and the Visual Dashboard. Visuals combines the deterministic account-analysis workflow, one consistent balanced recommendation order, evidence-backed recommendations, and visual summaries in one destination. The AI bottom sheet establishes the chat interface, but BYOAI is not connected or active yet. Account contains HoYoLAB account management and the optional JSON export action. Account retrieval, Daily Note synchronization, Room-backed snapshot persistence, and deterministic account analysis remain available. The broader Account Command Center, detailed finding/recommendation drill-downs, account history, integrated BYOAI assistant, and wider progression features remain planned.

---

## Table of Contents

- [Features](#features)
- [Screenshots](#screenshots)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Application Versioning and Releases](#application-versioning-and-releases)
- [How It Works](#how-it-works)
- [Project Structure](#project-structure)
- [Account Data Export](#account-data-export)
- [AI Integration](#ai-integration)
- [Planned Features](#planned-features)
- [Development Roadmap](#development-roadmap)
- [Security and Privacy](#security-and-privacy)
- [Current Limitations](#current-limitations)
- [Contributing](#contributing)
- [Disclaimer](#disclaimer)

---

## Features

### Currently Implemented

| Feature | Description |
|---|---|
| HoYoLAB Login | Centered first-run sign-in card with embedded WebView login and authentication cookie capture. |
| Manual Credentials | Manual UID and HoYoLAB cookie entry remains available under Account as a fallback. |
| Account Detection | Detects the Genshin Impact server from the account UID. |
| Daily Note | Retrieves daily account activity information. |
| Resin Tracking | Displays current Resin, maximum capacity, and estimated recovery time. |
| Daily Commissions | Displays commission progress and extra-task reward status. |
| Realm Currency | Displays current and maximum Realm Currency. |
| Weekly Resin Discounts | Displays remaining discounted Resin usage. |
| Expeditions | Displays current and maximum expedition counts. |
| Account Data Retrieval | Retrieves selected account information for the existing app workflow. |
| Optional JSON Export | Exports selected account data for third-party readers and backups. |
| Local Snapshot Persistence | Saves account exports in bounded JSON chunks in Room, migrates existing snapshots, and restores the latest saved snapshot and deterministic analysis on app startup without downloading the account JSON again. |
| Account Intelligence | Provides a dedicated Analyze Account action in Visuals and displays character assessment, recommendations, and account-gap analysis independently of JSON export. |
| Artifact Equipment State | Unequipped or partially equipped artifact slots are valid player choices and do not generate completeness recommendations by themselves; unavailable artifact data remains distinguishable. |
| Recommendation Prioritization | Presents evidence-backed findings in one consistent balanced order; the current analysis does not offer selectable analysis modes. |
| Navigation and AI Entry Point | Provides Home, Visuals, and Account destinations, with a prominent centered AI action that opens a bottom-sheet chat interface. |
| Edge-to-Edge Layout | Extends app surfaces behind the Android status and navigation bars while preserving system-bar controls. |
| Appearance Settings | Supports System, Light, and Dark themes with a saved user preference. |
| Home Dashboard | Shows the signed-in Genshin IGN, UID, HoYoLAB profile avatar, server, retrieved snapshot summary, and Real-Time Notes including the Parametric Transformer cooldown. |
| Account Analysis and Visual Dashboard | Visuals contains analysis controls, recommendations, and account details. Home displays the Visual Dashboard summary. |
| Stygian Onslaught | Visuals displays the Battle Chronicle best-record name and difficulty when those fields are returned by the account index. |
| Character Showcase | Displays the selected character's detailed profile while keeping the full roster behind an expandable portrait grid. The roster supports name search, element/weapon-type/rarity/level filters, and sorting by name, level, rarity, or friendship. Character profiles include level, element, weapon, artifacts and stats, talents, and constellations. Uses source-provided artwork with fallbacks and distinguishes missing or stale equipment data. |
| Materials Planner | Available inside Tools. The character calculator has its own character selector, current/target character levels, and independent Normal Attack, Skill, and Burst level ranges. It generates one combined preview material list from the bundled, version-pinned Genshin-DB 7.2 character ascension and talent data plus the character EXP curve, with optional owned and remaining quantities. The weapon calculator has a separate weapon selector and level range. Manual custom material rows remain saved and available as an optional secondary workflow. Automatic character costs are currently candidate previews, not approved production cost records. |
| Lineup Simulator | Provides a collapsible four-character team sandbox inside Tools. Players can select up to four owned characters, remove or reorder selections by toggling them, and inspect the elements represented. It does not yet claim team synergy, role coverage, reaction quality, or combat performance. Stale and partial roster states are disclosed. |
| AI Chat Surface | Opens from the centered AI action as a chat-style bottom sheet. Provider connection and response generation are not active yet. |
| Account and Export | Provides appearance settings, HoYoLAB account management, and optional JSON export through the Account destination and top-bar Account shortcut. |

### Planned Core Experience

- A complete Account Command Center and broader account overview as the primary user workflow.
- Expanded account analysis beyond the currently implemented character-assessment, recommendation, roster/equipment-gap, and data-confidence foundations.
- Full recommendation detail, filtering, and progression-history experiences beyond the initial summary card.
- Active BYOAI chat grounded in the unified account model.
- Expanded account analytics, interactive charts, detailed tables, and progression tracking.
- Broader account gap analysis beyond the implemented roster and character-equipment gap foundation.
- Farming and Resin optimization.
- Review and approve the version-pinned character cost data before promoting its current automatic preview calculations to production-trusted costs. The current character calculator uses candidate data only.
- Character and weapon pull evaluation.
- External Genshin game-knowledge integration for patch-aware character meta, build and weapon recommendations, observed usage, and account-aware pull evaluation. Candidate community sources include KeqingMains and Prydwen; access, attribution, freshness, and redistribution terms must be validated before integration.
- AI-powered account assistant.
- Interactive Account Command Center.
- Account relationship explorer.
- Historical account snapshots.

---

## Screenshots

Screenshots will be added as the application interface develops.

---

## Technology Stack

| Component | Technology |
|---|---|
| Platform | Android |
| Programming Language | Kotlin |
| UI Framework | Jetpack Compose |
| Design System | Material 3 |
| Networking | OkHttp |
| Asynchronous Processing | Kotlin Coroutines |
| Authentication | Android WebView |
| Data Format | JSON |
| Structured Local Storage | Room (account records and raw historical snapshots) |
| Preferences | SharedPreferences (non-sensitive settings and encrypted credential ciphertext) |
| Credential Encryption | AES-GCM with Android Keystore-backed key material |
| Build System | Gradle |
| Continuous Integration | GitHub Actions |

### Development Configuration

| Component | Version |
|---|---|
| Android Gradle Plugin | 8.5.0 |
| Kotlin | 2.0.0 |
| Compile SDK | 34 |
| Target SDK | 34 |
| Minimum SDK | 26 |
| OkHttp | 4.12.0 |
| Kotlin Coroutines | 1.8.1 |
| Java for CI | 17 |
| Gradle in CI | 8.14.8 |

---

## Getting Started

### Requirements

To build the project locally, you need:

- Android Studio.
- Android SDK 34.
- JDK 17.
- Gradle 8.14.8.
- An Android device or emulator running Android 8.0 (API 26) or later.

**Note:** The repository currently does not include a Gradle Wrapper. The build instructions below use an installed Gradle distribution.

### Clone the Repository

```bash
git clone https://github.com/berao609/Irminsul-codex.git
cd Irminsul-codex
````

### Open the Project

* Open Android Studio.

* Select Open.

* Choose the cloned `Irminsul-codex` directory.

* Allow Android Studio to synchronize the Gradle project.

* Ensure that the required Android SDK and JDK are installed.

### Build the Debug APK

From the repository root, run:

```bash
gradle assembleDebug
```

The generated APK should be located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Run the Application

Connect an Android device with USB debugging enabled or start an emulator, then run the `app` configuration through Android Studio.

Build verification: These are the intended build instructions. A successful local build has not been verified as part of this README preparation.

## Application Versioning and Releases

The official application version is maintained in the repository-root `version.properties` file:

- `VERSION_NAME` is the user-facing semantic version (for example, `1.0.0`).
- `VERSION_CODE` is Android's increasing integer version for official releases. Increase it for every official release.

The Debug CI workflow automatically assigns each new workflow run a unique development version name and a higher Android version code. Consecutive Debug APKs use the persistent Debug signing key and can update the installed Debug app in place. Re-running the same workflow run produces the same version; start a new run for a newer installable build.

Official signed APK releases are triggered by pushing a matching version tag, such as `v1.0.0`. Release builds use the exact version values in `version.properties` and a separate persistent Release signing key. Switching between Debug and Release requires a one-time uninstall/reinstall because their signing certificates differ.

See [RELEASE_PROCESS.md](RELEASE_PROCESS.md) for signing-key setup, required secrets, automatic Debug numbering, version updates, and the release procedure. Do not create a release tag until signing credentials are configured and backed up.

## How It Works

The current application follows a data retrieval workflow:

* The user authenticates through HoYoLAB or supplies credentials manually.

* The application obtains the required authentication information.

* The user provides or confirms their Genshin Impact UID.

* The application communicates with the relevant HoYoLAB services.

* Retrieved account information is processed by the application.

* Daily Note information is displayed, and selected account data can be exported as JSON.

The application retrieves selected account data through the dedicated Analyze Account action, saves the resulting snapshot in Room, and runs the deterministic account-intelligence pipeline from the normalized snapshot. On later launches, it restores the latest saved snapshot for the signed-in UID and rebuilds the overview locally without fetching the account JSON again. Daily Note synchronization remains a separate lightweight request, and account retrieval remains an explicit refresh action. This flow does not require or launch JSON export. The ViewModel exposes the unified overview and the Visuals page displays an analysis summary with a five-item recommendation preview alongside visual account metrics. Users can expand the list to inspect every available recommendation, including its reason, supporting evidence, and stale-data indicator. Recommendations use one consistent balanced order; there are no selectable analysis objectives. User-facing account history, integrated BYOAI assistance, expanded analysis, and the complete Account Command Center remain planned.

## Project Structure

The main application code is organized into the following components:

```text
Irminsul-codex/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/irminsul/codex/
│   │       │       ├── MainActivity.kt
│   │       │       ├── api/
│   │       │       │   └── HoYoLabClient.kt
│   │       │       ├── data/
│   │       │       │   ├── local/
│   │       │       │   │   ├── dao/
│   │       │       │   │   ├── entity/
│   │       │       │   │   └── repository/
│   │       │       │   ├── mapper/
│   │       │       │   │   └── AccountExportMapper.kt
│   │       │       │   └── repository/
│   │       │       │       └── HoYoLabAccountRepository.kt
│   │       │       ├── domain/
│   │       │       │   ├── model/
│   │       │       │   │   └── AccountSnapshot.kt
│   │       │       │   └── repository/
│   │       │       │       ├── AccountRepository.kt
│   │       │       │       └── AccountSnapshotStore.kt
│   │       │       ├── presentation/
│   │       │       │   └── viewmodel/
│   │       │       │       └── AccountViewModel.kt
│   │       │       ├── integration/
│   │       │       │   └── security/
│   │       │       │       └── HoYoLabCredentialStore.kt
│   │       │       └── ui/
│   │       │           └── HoYoLabLoginDialog.kt
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
│
├── gradle/
│   └── libs.versions.toml
│
├── .github/
│   └── workflows/
│       ├── build.yml
│       └── release.yml
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── version.properties
├── RELEASE_PROCESS.md
└── README.md
```

### Main Components

### `MainActivity.kt`

Responsible for:

* Screen-level composition, navigation state, and coordination of extracted presentation sections, including the first-run sign-in experience.
* Credential entry and orchestration through `HoYoLabCredentialStore` for Keystore-backed cookie encryption; non-sensitive UID and theme preferences remain in SharedPreferences.
* Daily Note synchronization and account-export actions, including document-picker coordination.
* Retaining screen-owned state and callbacks passed into presentation components.

### `api/HoYoLabClient.kt`

Responsible for:

* HoYoLAB API communication, HTTP requests, headers, and DS signatures.
* UID and server handling.
* Daily Note retrieval and selected account-data retrieval.
* Character detail requests and JSON export generation.

### `presentation/viewmodel/AccountViewModel.kt`

Responsible for:

* Running full account export through the domain repository contract.
* Exposing export progress, errors, the normalized AccountSnapshot, and the unified deterministic AccountIntelligenceOverview as observable state.
* Running the account-intelligence pipeline after retrieval and using the default balanced recommendation order.
* Retaining the original JSON export for the Save JSON file action.

### `data/local/`

Contains the Room database, account and snapshot entities, DAO, and local snapshot-store implementation. Each successful account export is saved as an immutable raw JSON snapshot associated with its UID and server. HoYoLAB cookies and AI credentials are not stored in Room.

### `data/repository/HoYoLabAccountRepository.kt`

Adapts the existing HoYoLAB JSON export operation and AccountExportMapper to the domain repository contract.

### `integration/security/HoYoLabCredentialStore.kt`

Encrypts the HoYoLAB session cookie with AES-GCM using a key held by Android Keystore. It migrates the legacy plaintext preference after successful encryption and provides credential clearing. Only ciphertext is persisted in SharedPreferences.

### `ui/HoYoLabLoginDialog.kt`

Responsible for:

* Embedded HoYoLAB authentication.
* WebView login and authentication cookie capture.
* WebView browsing-data cleanup.

### `.github/workflows/build.yml`

Defines the GitHub Actions workflow for building and uploading version-labeled Debug APK artifacts.

### `.github/workflows/release.yml`

Builds, verifies, and publishes a signed APK when a matching version tag is pushed.

## Account Data Export

The current JSON exporter retrieves selected account information and organizes the available results into a structured JSON document.

The current export structure includes:

| JSON Field              | Description                          |
| ----------------------- | ------------------------------------ |
| export_format_version   | Integer identifying the export schema version. |
| exported_at             | Export timestamp.                    |
| uid                     | Genshin Impact account UID.          |
| server                  | Detected account server.             |
| account_card            | Available account card information.  |
| profile_and_exploration | Profile and exploration information. |
| daily_note              | Daily Note information.              |
| characters              | Retrieved character information.     |
| character_roster_source | Preserved source character roster data. |
| character_detail_source | Preserved raw character-detail response batches. |
| spiral_abyss_current    | Current Spiral Abyss information.    |
| spiral_abyss_previous   | Previous Spiral Abyss information.   |
| imaginarium_theater     | Imaginarium Theater information.     |

Character detail requests are processed in batches of up to eight character IDs.

### Data Preservation and Limitations

The exporter preserves the fields returned in the selected account-data sections, including nested values, descriptions, effects, media URLs, raw character roster data, raw character-detail batches (including property maps), and explicit nulls. The `characters` section also adds a readable `stat_name` when a source `property_type` can be mapped, while retaining the original property type. The raw source sections are retained separately so the enriched character data does not replace the original response fields.

The export includes `export_format_version` so future schema changes can be identified. Each section status records its source and retrieval timestamp; successful sections also record the last successful retrieval timestamp available in this one-time export. Historical last-success timestamps are not persisted between exports. Authentication cookies and credentials are not part of the exported account sections.

The overall status is `complete` only when all required sections succeed. A skipped optional section, such as the account card when the HoYoLAB account-ID cookie is unavailable, does not by itself make the export partial. Failed or partial sections, or missing required sections, make the export `partial` when some usable data remains. The status is `failed` when no section contains usable retrieved data.

This is still a selected-section account export, not a complete raw API archive or a complete account inventory.

In particular:

* Not every account resource is currently retrieved.

* Some API sections may be unavailable or incomplete.

* A successful export does not guarantee that every section contains usable information.

* The current export should not be treated as a complete inventory of Primogems, Mora, materials, artifacts, or every other account asset.

Improving data coverage and establishing a reliable unified account model are important development priorities.

## AI Integration

Irminsul Codex is intended to use existing AI models rather than train or maintain its own foundation model.

The proposed approach is Bring Your Own AI (BYOAI).

Users will eventually be able to configure a supported AI provider and supply their own API credentials.

Potential providers include:

* OpenAI.

* Google Gemini.

* Anthropic.

Provider support will depend on implementation and API compatibility.

### Intended AI Responsibilities

The AI assistant should be able to:

* Understand available account data.

* Answer account-specific questions.

* Explain investment recommendations.

* Identify missing information.

* Compare development options.

* Explain farming priorities.

* Evaluate potential pulls.

* Provide context-aware guidance.

### Intelligence Architecture

Numerical calculations and resource requirements should be handled by deterministic application logic wherever possible.

AI should serve as the reasoning and interpretation layer.

```text
Account Data
     │
     ▼
Unified Account Model
     │
     ▼
Calculation and Analysis Engine
     │
     ▼
AI Intelligence Layer
     │
     ▼
Recommendations and Explanations
```

Recommendations must be grounded in available account data. Missing information must be identified rather than invented.

HoYoLAB authentication cookies must never be transmitted to AI providers.

## Planned Features

The long-term goal is to develop Irminsul Codex into an account intelligence platform rather than simply another account statistics viewer.

### 1\. Account Analytics

Analyze account progression, roster composition, character development, exploration, and available resources.

### 2\. Investment Intelligence

Evaluate character development opportunities using:

* Current character progression.

* Upgrade requirements.

* Material availability.

* Mora and Resin costs.

* Expected improvements.

* Investment priorities.

Goal: Answer "What should I invest in next?"

### 3\. Account Gap Intelligence

Identify account weaknesses, missing capabilities, incomplete builds, and opportunities for improvement.

### 4\. Farming Intelligence

Determine material requirements, farming dependencies, Resin costs, overlapping objectives, and efficient farming priorities.

Goal: Answer "What should I farm first?"

### 5\. Pull Intelligence

Evaluate potential character and weapon pulls based on:

* Existing roster compatibility.

* Missing team capabilities.

* Character and weapon redundancy.

* Potential new team compositions.

* Primogem opportunity costs.

* Player preferences.

Goal: Answer "Is this character or weapon worth pulling for my account?"

### 6\. Resource Optimization

Help players allocate available resources efficiently, including Resin, Mora, Primogems, and character-development materials where reliable data is available.

### 7\. Next-Step Intelligence

Combine account analytics and investment priorities to recommend practical next actions.

### 8\. AI-Powered Account Assistant

Introduce a conversational assistant that can understand account information, explain recommendations, compare options, and answer account-specific questions.

### 9\. Interactive Account Command Center

Build an interactive dashboard that connects account information, analytics, investments, farming objectives, and AI assistance.

Rather than presenting disconnected statistics, the dashboard should allow users to explore their accounts and take action.

### Account Relationship Explorer

An interactive graph-style exploration system inspired by the discoverability of Obsidian's Graph View.

Example relationship:

```text
Character
    │
    ▼
Talent Upgrade
    │
    ▼
Required Materials
    │
    ▼
Farming Domain
    │
    ▼
Farming Priority
```

Selecting an element should reveal its related information, dependencies, and possible actions.

Other planned dashboard workspaces include:

* Account Overview.

* Interactive Analytics.

* Investment Command Center.

* Farming Command Center.

* Pull Evaluation Workspace.

* Context-Aware AI Assistant.

### Context-Aware Dashboard Intelligence

Users should be able to select a character, resource, chart, or investment and ask questions about that specific context.

For example:

> What should I improve here first?

The dashboard and AI assistant should operate on a unified account model rather than function as disconnected features.

### 10\. Account History

Store historical account snapshots to track progression, compare changes, and analyze development over time.

## Development Roadmap

Development should proceed incrementally, with reliable account data and deterministic calculations established before advanced AI recommendations.

| Phase | Focus                  | Intended Outcome                                                                     |
| ----- | ---------------------- | ------------------------------------------------------------------------------------ |
| 0     | Data Feasibility       | Identify available data, missing endpoints, and limitations.                         |
| 1     | Account Data Model     | Establish a structured and unified account representation.                           |
| 2     | Calculation Engine     | Implement deterministic calculations for costs, requirements, and resource planning. |
| 3     | Interactive Analytics  | Build account statistics, visualizations, filtering, and drill-down interactions.    |
| 4     | Decision Systems       | Implement investment, gap, farming, and pull evaluation logic.                       |
| 5     | AI Integration         | Introduce BYOAI, provider configuration, and account-aware conversations.            |
| 6     | Account Command Center | Integrate analytics, decision systems, relationship exploration, and AI.             |
| 7     | Proactive Intelligence | Introduce prioritized suggestions and upcoming account needs.                        |
| 8     | Account History        | Add historical snapshots, progression tracking, and comparisons.                     |
| 9     | Refinement             | Improve reliability, security, usability, performance, and testing.                  |

This roadmap describes the intended development direction. It does not mean that these phases have already been implemented.

## Security and Privacy

Account authentication and API credentials require careful handling.

### Current Implementation

* The HoYoLAB session cookie is encrypted with AES-GCM using a key generated and held by Android Keystore. Only the encrypted ciphertext is persisted in app-private SharedPreferences.

* Existing installations migrate the legacy plaintext cookie to encrypted storage on first read. The legacy preference is removed after successful encryption.

* The UID remains in ordinary app-private preferences because it is not an authentication secret.

* The Account screen provides an explicit sign-out action that clears the saved UID, encrypted cookie, and in-memory credentials. Locally saved account snapshots are retained.

* The login WebView clears its cookies, cache, history, and form data after authentication capture or dismissal.

* The Android manifest sets `allowBackup` to `false`.

### Planned Improvements

* Secure AI API key storage.

* Explicit AI data-sharing controls.

* Minimized transmission of personal account information.

* Clear handling of expired authentication credentials.

* Separation of HoYoLAB credentials and AI provider credentials.

> Credential encryption protects the saved session cookie at rest within the app's private storage. It does not protect against a fully compromised device or a malicious process running with the app's privileges.

Users should never share their HoYoLAB authentication cookies or AI API keys.

## Current Limitations

Irminsul Codex is an early-stage application.

The following capabilities are not currently implemented or verified:

* AI provider integration.

* BYOAI configuration.

* AI account conversations.

* Investment scoring and recommendations.

* Automated account gap analysis.

* Farming optimization.

* Pull evaluation.

* Complete account inventory extraction.

* Interactive Account Command Center.

* Account relationship graph.

* Historical account snapshots.

* Automated test coverage.

The application currently provides a foundation for future development, not the complete intelligence system described in the project vision.

## Contributing

Irminsul Codex is an evolving project.

Contributions, suggestions, bug reports, and feature ideas are welcome.

Before proposing major architectural changes, consider the project's long-term direction:

* Reliable account data should come before advanced recommendations.

* Numerical calculations should be deterministic and verifiable.

* AI should interpret account information rather than invent missing data.

* Features should integrate with a unified account model.

* Security and privacy should remain core design considerations.

For significant changes, opening an issue to discuss the proposed approach before implementation is recommended.

## Disclaimer

Irminsul Codex is an independent, community-oriented project.

It is not affiliated with, endorsed by, or officially connected to HoYoverse or HoYoLAB.

Genshin Impact and related assets are properties of their respective owners.

Use the application responsibly and comply with applicable service terms and policies. Authentication and data retrieval functionality may change if the underlying services or endpoints change.

## Repository

GitHub: [berao609/Irminsul-codex](https://github.com/berao609/Irminsul-codex?utm_source=chatgpt.com)

Project Goal: Turn Genshin Impact account data into useful, interactive, and AI-powered account intelligence.
