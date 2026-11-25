# Irminsul Codex

**An AI-Integrated Genshin Impact Account Intelligence and Progression Analyzer**

Irminsul Codex is an Android application designed to help Genshin Impact players understand their accounts, make better investment decisions, and plan their progression efficiently.

The product's primary purpose is to turn account data into actionable in-app intelligence. Irminsul Codex aims to identify development priorities, analyze account weaknesses, optimize farming, evaluate potential pulls, and help players decide what to focus on next. Account retrieval supports this analysis; JSON export is an optional utility for third-party readers and backups.

> **Project Status:** Early development. The current application provides HoYoLAB connectivity, Daily Note synchronization, selected account data retrieval, optional JSON file export, and Room-backed persistence of retrieved account snapshots. The deterministic account-intelligence pipeline is now connected to the retrieval workflow and an initial Account Intelligence card. The screen shows character assessment counts, up to five evidence-backed recommendations, account-gap counts, account-section coverage, and stale/unverified-source warnings. Players can choose Balanced Development, Build Reliability, Stat Integrity, or Data Confidence to change recommendation ordering. The full Account Command Center, user-facing account history, integrated BYOAI assistant, and broader progression features remain planned.

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
| HoYoLAB Login | Embedded WebView login and authentication cookie capture. |
| Manual Credentials | Manual entry of Genshin UID and HoYoLAB cookie. |
| Account Detection | Detects the Genshin Impact server from the account UID. |
| Daily Note | Retrieves daily account activity information. |
| Resin Tracking | Displays current Resin, maximum capacity, and estimated recovery time. |
| Daily Commissions | Displays commission progress and extra-task reward status. |
| Realm Currency | Displays current and maximum Realm Currency. |
| Weekly Resin Discounts | Displays remaining discounted Resin usage. |
| Expeditions | Displays current and maximum expedition counts. |
| Account Data Retrieval | Retrieves selected account information for the existing app workflow. |
| Optional JSON Export | Exports selected account data for third-party readers and backups. |
| Local Snapshot Persistence | Saves each successful account export in Room for future in-app history and analysis. |\n| Initial Account Intelligence | Runs deterministic analysis after retrieval and displays character assessment, recommendations, account gaps, and data coverage. |\n| Development Goal Selection | Lets the user choose one of four explicit objectives for recommendation ordering. |

### Planned Core Experience

- A complete Account Command Center and broader account overview as the primary user workflow.
- Expanded account analysis beyond the currently implemented character-assessment, recommendation, roster/equipment-gap, and data-confidence foundations.
- Full recommendation detail, filtering, and progression-history experiences beyond the initial summary card.
- Integrated BYOAI assistant grounded in the unified account model.
- Account analytics and progression tracking.
- Broader account gap analysis beyond the implemented roster and character-equipment gap foundation.
- Farming and Resin optimization.
- Character and weapon pull evaluation.
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
| Preferences | SharedPreferences (including the current credential flow; not encrypted) |
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

The application retrieves selected account data, saves each successful raw export in Room, and runs the deterministic account-intelligence pipeline from the normalized snapshot. The ViewModel exposes the unified overview and the existing screen displays an initial summary with up to five recommendations. The user can select one of four explicit development goals; Balanced Development is the initial default. The goal changes recommendation ordering only. User-facing account history, integrated BYOAI assistance, expanded analysis, and the complete Account Command Center remain planned.

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

* Main application interface and existing Daily Note presentation.
* Credential entry and the current SharedPreferences handling.
* Daily Note synchronization actions.
* Saving account JSON exports through Android's document picker.

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
* Running the account-intelligence pipeline after retrieval and re-running it when the selected development goal changes.
* Retaining the original JSON export for the Save JSON file action.

### `data/local/`

Contains the Room database, account and snapshot entities, DAO, and local snapshot-store implementation. Each successful account export is saved as an immutable raw JSON snapshot associated with its UID and server. HoYoLAB cookies and AI credentials are not stored in Room.

### `data/repository/HoYoLabAccountRepository.kt`

Adapts the existing HoYoLAB JSON export operation and AccountExportMapper to the domain repository contract.

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

* HoYoLAB authentication cookies are stored using ordinary Android SharedPreferences.

* Masking credentials in the interface does not encrypt their stored values.

* The login WebView clears its cookies, cache, history, and form data after authentication capture or dismissal.

* The Android manifest sets `allowBackup` to `false`.

### Planned Improvements

* Android Keystore-backed credential protection.

* Secure AI API key storage.

* Explicit AI data-sharing controls.

* Minimized transmission of personal account information.

* Clear handling of expired authentication credentials.

* Separation of HoYoLAB credentials and AI provider credentials.

> Important: Current credential storage should not be considered encrypted or production-grade secure storage.

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
