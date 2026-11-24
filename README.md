Irminsul Codex

An AI-powered Genshin Impact Account Intelligence System

Irminsul Codex is an Android application designed to help Genshin Impact players understand their accounts, make better investment decisions, and plan their progression more efficiently.

The project aims to go beyond displaying account statistics. Its long-term goal is to transform account data into actionable intelligence: identifying development priorities, analyzing account weaknesses, optimizing farming, evaluating potential pulls, and recommending what players should focus on next.

Project status: Early development. The current application provides HoYoLAB connectivity, Daily Note synchronization, selected account data retrieval, and JSON export. Advanced analytics, AI integration, and the interactive dashboard are planned features.

---

Table of Contents

- "Features" (#features)
- "Screenshots" (#screenshots)
- "Technology Stack" (#technology-stack)
- "Getting Started" (#getting-started)
- "How It Works" (#how-it-works)
- "Project Structure" (#project-structure)
- "Account Data Export" (#account-data-export)
- "Planned Features" (#planned-features)
- "Development Roadmap" (#development-roadmap)
- "Security and Privacy" (#security-and-privacy)
- "Current Limitations" (#current-limitations)
- "Contributing" (#contributing)
- "Disclaimer" (#disclaimer)

---

Features

Currently Implemented

Feature| Description
HoYoLAB Login| Embedded WebView login and authentication cookie capture.
Manual Credentials| Option to enter a Genshin UID and HoYoLAB cookie manually.
Account Detection| Detects the Genshin Impact server from the account UID.
Daily Note| Retrieves daily account activity information.
Resin Tracking| Displays current Resin, maximum capacity, and estimated recovery time.
Daily Commissions| Displays commission progress and extra-task reward status.
Realm Currency| Displays current and maximum Realm Currency.
Weekly Resin Discounts| Displays remaining discounted Resin usage.
Expeditions| Displays current and maximum expedition counts.
Account Data Export| Retrieves selected account information and exports it as JSON.
Clipboard Export| Copies exported JSON to the clipboard.

Planned

- Account analytics and progression tracking.
- Character investment recommendations.
- Account gap analysis.
- Farming and Resin optimization.
- Character and weapon pull evaluation.
- AI-powered account assistant.
- Interactive Account Command Center.
- Account relationship explorer.
- Historical account snapshots.

See "Planned Features" (#planned-features) and "Development Roadmap" (#development-roadmap) for details.

---

Screenshots

Screenshots will be added as the application interface develops.

---

Technology Stack

Component| Technology
Platform| Android
Language| Kotlin
UI Framework| Jetpack Compose
Design System| Material 3
Networking| OkHttp
Asynchronous Processing| Kotlin Coroutines
Authentication| Android WebView
Data Format| JSON
Local Storage| SharedPreferences
Build System| Gradle
Continuous Integration| GitHub Actions

Development Configuration

Component| Version
Android Gradle Plugin| 8.5.0
Kotlin| 2.0.0
Compile SDK| 34
Target SDK| 34
Minimum SDK| 26
OkHttp| 4.12.0
Kotlin Coroutines| 1.8.1
Java for CI| 17
Gradle in CI| 8.14.8

---

Getting Started

Requirements

To build the project locally, you need:

- Android Studio.
- Android SDK 34.
- JDK 17.
- Gradle 8.14.8.
- An Android device or emulator running Android 8.0 (API 26) or later.

Note: The repository currently does not include a Gradle Wrapper. The build instructions below use an installed Gradle distribution.

Clone the Repository

git clone https://github.com/berao609/Irminsul-codex.git
cd Irminsul-codex

Open the Project

1. Open Android Studio.
2. Select Open.
3. Choose the cloned "Irminsul-codex" directory.
4. Allow Android Studio to synchronize the Gradle project.
5. Ensure that the required Android SDK and JDK are installed.

Build the Debug APK

From the repository root, run:

gradle assembleDebug

The generated APK should be located at:

app/build/outputs/apk/debug/app-debug.apk

Run the Application

Connect an Android device with USB debugging enabled or start an emulator, then run the "app" configuration through Android Studio.

Build verification: These are the project's intended build instructions. A successful local build has not been verified as part of this README preparation.

---

How It Works

The current application follows a relatively simple data retrieval workflow:

1. The user authenticates through HoYoLAB or supplies their credentials manually.
2. The application obtains the required authentication information.
3. The user provides or confirms their Genshin Impact UID.
4. The application communicates with the relevant HoYoLAB services.
5. Retrieved account information is processed by the application.
6. Daily Note information is displayed, and selected account data can be exported as JSON.

The current implementation is focused on retrieving and presenting data. It does not yet include the planned account intelligence or AI recommendation systems.

---

Project Structure

The main application code is organized into the following components:

Irminsul-codex/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/irminsul/codex/
│   │       │       ├── MainActivity.kt
│   │       │       │
│   │       │       ├── api/
│   │       │       │   └── HoYoLabClient.kt
│   │       │       │
│   │       │       └── ui/
│   │       │           └── HoYoLabLoginDialog.kt
│   │       │
│   │       └── AndroidManifest.xml
│   │
│   └── build.gradle.kts
│
├── gradle/
│   └── libs.versions.toml
│
├── .github/
│   └── workflows/
│       └── build.yml
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md

Main Components

"MainActivity.kt"

Handles the main application interface, application state, credential handling, synchronization actions, and JSON export interactions.

"api/HoYoLabClient.kt"

Handles HoYoLAB communication, HTTP requests, request signatures, UID and server handling, Daily Note retrieval, account data retrieval, character detail requests, and JSON processing.

"ui/HoYoLabLoginDialog.kt"

Handles embedded HoYoLAB authentication through WebView, cookie capture, and WebView browsing-data cleanup.

".github/workflows/build.yml"

Defines the GitHub Actions workflow for building and uploading the debug APK artifact.

---

Account Data Export

The current JSON exporter retrieves selected account information and organizes the available results into a structured JSON document.

The current export structure includes:

JSON Field| Description
"exported_at"| Export timestamp.
"uid"| Genshin Impact account UID.
"server"| Detected account server.
"account_card"| Available account card information.
"profile_and_exploration"| Profile and exploration information.
"daily_note"| Daily Note information.
"characters"| Retrieved character information.
"spiral_abyss_current"| Current Spiral Abyss information.
"spiral_abyss_previous"| Previous Spiral Abyss information.
"imaginarium_theater"| Imaginarium Theater information.

Character detail requests are processed in batches of up to eight character IDs.

Important Data Limitations

The current exporter processes and filters certain response fields. It is not a complete raw API archive.

In particular:

- Not every account resource is currently retrieved.
- Some API sections may be unavailable or incomplete.
- A successful export does not guarantee that every section contains usable information.
- The current export should not be treated as a complete inventory of Primogems, Mora, materials, artifacts, or every other account asset.

Improving data coverage and establishing a reliable unified account model are important future development priorities.

---

Planned Features

The long-term vision is to develop Irminsul Codex into an account intelligence platform rather than simply another account statistics viewer.

1. Account Analytics

Analyze account progression, roster composition, character development, exploration, and available resources.

2. Investment Intelligence

Evaluate character development opportunities using:

- Current character progression.
- Upgrade requirements.
- Material availability.
- Mora and Resin costs.
- Expected improvements.
- Investment priorities.

The goal is to answer:

"What should I invest in next?"

3. Account Gap Intelligence

Identify account weaknesses, missing capabilities, incomplete builds, and opportunities for improvement.

4. Farming Intelligence

Determine material requirements, farming dependencies, Resin costs, overlapping objectives, and efficient farming priorities.

The goal is to answer:

"What should I farm first?"

5. Pull Intelligence

Evaluate potential character and weapon pulls based on:

- Existing roster compatibility.
- Missing team capabilities.
- Character and weapon redundancy.
- Potential new team compositions.
- Primogem opportunity costs.
- Player preferences.

The goal is to answer:

"Is this character or weapon worth pulling for my account?"

6. Resource Optimization

Help players allocate available resources efficiently, including Resin, Mora, Primogems, and character-development materials where reliable data is available.

7. Next-Step Intelligence

Combine account analytics and investment priorities to recommend practical next actions.

8. AI-Powered Account Assistant

Introduce a conversational assistant that can understand account information, explain recommendations, compare options, and answer account-specific questions.

9. Interactive Account Command Center

Build an interactive dashboard that connects account information, analytics, investments, farming objectives, and AI assistance.

Rather than presenting disconnected statistics, the dashboard should allow users to explore their accounts and take action.

Account Relationship Explorer

An interactive graph-style exploration system inspired by the discoverability of Obsidian's Graph View.

Example relationship:

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

Selecting an element should reveal its related information, dependencies, and possible actions.

Other planned dashboard workspaces include:

- Account Overview.
- Interactive Analytics.
- Investment Command Center.
- Farming Command Center.
- Pull Evaluation Workspace.
- Context-Aware AI Assistant.

10. Account History

Store historical account snapshots to track progression, compare changes, and analyze development over time.

---

AI Integration Strategy

Irminsul Codex does not need to train or maintain its own foundation model.

The proposed approach is Bring Your Own AI (BYOAI).

Users will eventually be able to configure their preferred supported AI provider and supply their own API credentials.

Potential providers include:

- OpenAI.
- Google Gemini.
- Anthropic.

Provider support will depend on implementation and API compatibility.

Intended AI Responsibilities

The AI assistant should:

- Interpret available account data.
- Answer account-specific questions.
- Explain investment recommendations.
- Identify missing information.
- Compare development options.
- Explain farming priorities.
- Evaluate potential pulls.
- Provide context-aware guidance.

Intelligence Architecture

The application should handle numerical calculations and resource requirements through deterministic code wherever possible.

AI should operate as the reasoning and interpretation layer.

For example:

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

Recommendations must be grounded in available data. Missing information must be identified instead of invented.

HoYoLAB authentication cookies must never be transmitted to AI providers.

---

Development Roadmap

Development will proceed incrementally. Reliable account data and deterministic calculations should be established before advanced AI recommendations.

Phase| Focus| Intended Outcome
0| Data Feasibility| Identify available data, missing endpoints, and limitations.
1| Account Data Model| Establish a structured and unified account representation.
2| Calculation Engine| Implement deterministic calculations for costs, requirements, and resource planning.
3| Interactive Analytics| Build account statistics, visualizations, filtering, and drill-down interactions.
4| Decision Systems| Implement investment, gap, farming, and pull evaluation logic.
5| AI Integration| Introduce BYOAI, provider configuration, and account-aware conversations.
6| Account Command Center| Integrate analytics, decision systems, relationship exploration, and AI.
7| Proactive Intelligence| Introduce prioritized suggestions and upcoming account needs.
8| Account History| Add historical snapshots, progression tracking, and comparisons.
9| Refinement| Improve reliability, security, usability, performance, and testing.

This roadmap describes the intended development direction. It does not mean that these phases have already been implemented.

---

Security and Privacy

Account authentication and API credentials require careful handling.

Current Implementation

- HoYoLAB authentication cookies are stored using ordinary Android SharedPreferences.
- Masking credentials in the interface does not encrypt their stored values.
- The login WebView clears its cookies, cache, history, and form data after authentication capture or dismissal.
- The Android manifest sets "allowBackup" to "false".

Planned Improvements

- Android Keystore-backed credential protection.
- Secure AI API key storage.
- Explicit AI data-sharing controls.
- Minimized transmission of personal account information.
- Clear handling of expired authentication credentials.
- Separation of HoYoLAB credentials and AI provider credentials.

Important: The current credential storage should not be considered encrypted or production-grade secure storage.

Users should never share their HoYoLAB authentication cookies or AI API keys.

---

Current Limitations

Irminsul Codex is an early-stage application.

The following capabilities are not currently implemented or verified:

- AI provider integration.
- BYOAI configuration.
- AI account conversations.
- Investment scoring and recommendations.
- Automated account gap analysis.
- Farming optimization.
- Pull evaluation.
- Complete account inventory extraction.
- Interactive Account Command Center.
- Account relationship graph.
- Historical account snapshots.
- Automated test coverage.

The application currently provides a foundation for future development, not the complete intelligence system described in the project vision.

---

Contributing

Irminsul Codex is an evolving project.

Contributions, suggestions, bug reports, and feature ideas are welcome.

Before proposing major architectural changes, consider the project's long-term direction:

- Reliable account data should come before advanced recommendations.
- Numerical calculations should be deterministic and verifiable.
- AI should interpret account information rather than invent missing data.
- Features should integrate with a unified account model.
- Security and privacy should remain core design considerations.

For significant changes, opening an issue to discuss the proposed approach before implementation is recommended.

---

Disclaimer

Irminsul Codex is an independent, community-oriented project.

It is not affiliated with, endorsed by, or officially connected to HoYoverse or HoYoLAB.

Genshin Impact and related assets are properties of their respective owners.

Use the application responsibly and comply with the applicable service terms and policies. Authentication and data retrieval functionality may change if the underlying services or endpoints change.

---

Repository

GitHub: "berao609/Irminsul-codex" (https://github.com/berao609/Irminsul-codex)

Project Goal: Turn Genshin Impact account data into useful, interactive, and AI-powered account intelligence.
