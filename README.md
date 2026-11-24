Irminsul Codex

An AI-powered Genshin Impact Account Intelligence System

Irminsul Codex is an Android application designed to help Genshin Impact players understand, analyze, and improve their accounts.

Rather than simply displaying account statistics, the long-term vision is to turn account data into actionable intelligence: identifying investment opportunities, finding account weaknesses, prioritizing farming objectives, evaluating potential pulls, and helping players make better resource-allocation decisions.

The project is currently in its early development stage. Its existing foundation focuses on HoYoLAB connectivity, account data retrieval, and JSON export. Advanced analytics, AI integration, and the interactive dashboard are planned development goals, not currently implemented features.

---

1. Project Vision

Irminsul Codex aims to answer practical account-development questions:

- What should I invest in next?
- Which characters should I prioritize building?
- What important resources or materials am I missing?
- What should I farm first?
- Which upgrades provide the greatest value for my account?
- Is a character or weapon worth pulling for my current roster?
- How should I allocate my Resin and other resources efficiently?

The goal is to provide account-specific recommendations based on actual account data, game mechanics, resource requirements, and player priorities.

2. Current Features

The application currently provides the following functionality:

HoYoLAB Integration

- Embedded HoYoLAB login using an Android WebView.
- Manual UID and cookie input.
- Capture of required HoYoLAB authentication cookies.
- Genshin Impact UID and server detection.

Daily Account Information

- Current Original Resin and maximum capacity.
- Estimated Resin recovery time.
- Daily commission progress.
- Extra daily-task reward status.
- Realm Currency.
- Weekly discounted Resin usage.
- Expedition counters.

Account Data Retrieval

The current data retrieval and export implementation includes support for:

- Account card information.
- Profile and exploration information.
- Daily Note information.
- Character information.
- Current Spiral Abyss information.
- Previous Spiral Abyss information.
- Imaginarium Theater information.

Character detail requests are processed in batches.

JSON Export

- Export account data as a JSON file.
- Copy exported JSON to the clipboard.
- Preserve the available information returned by the current data retrieval implementation.

Data limitation: The current exporter filters certain response fields. It is not a complete raw API archive or a verified inventory of every account resource. Successful export does not guarantee that every section contains complete data.

---

3. Technology Stack

Component| Technology
Platform| Android
Programming Language| Kotlin
UI Framework| Jetpack Compose
Design System| Material 3
Networking| OkHttp
Asynchronous Operations| Kotlin Coroutines
Account Authentication| Android WebView
Data Format| JSON
Local Credential Storage| SharedPreferences
Build System| Gradle
CI| GitHub Actions

Project Configuration

- Kotlin: 2.0.0
- Android Gradle Plugin: 8.5.0
- Compile SDK: 34
- Target SDK: 34
- Minimum SDK: 26
- OkHttp: 4.12.0
- Kotlin Coroutines: 1.8.1
- CI Java version: 17

---

4. Project Architecture

The current application is organized around the following components:

"MainActivity.kt"

Responsible for:

- Main application interface.
- Application state.
- Credential handling.
- Synchronization actions.
- JSON export actions.
- Displaying retrieved account information.

"api/HoYoLabClient.kt"

Responsible for:

- HoYoLAB API communication.
- HTTP requests and headers.
- DS signature generation.
- UID and server handling.
- Daily Note retrieval.
- Account information retrieval.
- Character data retrieval.
- JSON response processing and export.

"ui/HoYoLabLoginDialog.kt"

Responsible for:

- Embedded HoYoLAB login.
- WebView authentication.
- Authentication cookie capture.
- WebView browsing-data cleanup.

The architecture is expected to evolve as the application develops more advanced data management, analytics, and AI capabilities.

---

5. Long-Term Intelligence Systems

The following systems define the intended direction of the project.

5.1 Account Analytics

Analyze account progression, roster composition, character development, exploration, and available resources.

5.2 Investment Intelligence

Evaluate character development opportunities, upgrade costs, expected benefits, and investment priorities.

5.3 Account Gap Intelligence

Identify weaknesses, missing capabilities, incomplete character builds, and development opportunities.

5.4 Farming Intelligence

Determine material requirements, farming dependencies, Resin costs, overlapping farming objectives, and efficient farming priorities.

5.5 Pull Intelligence

Evaluate potential character and weapon pulls using roster compatibility, account needs, redundancy, resource opportunity costs, and player preferences.

5.6 Resource Optimization

Help players allocate available resources efficiently, including Resin, Mora, Primogems, and character-development materials where reliable data is available.

5.7 Next-Step Intelligence

Combine account analytics and investment priorities to recommend practical next actions.

5.8 Proactive Intelligence

Eventually identify upcoming account needs and notify players about useful development opportunities.

These systems are planned capabilities. Their recommendations will depend on the completeness and reliability of available account data.

---

6. AI Integration Strategy

Irminsul Codex is intended to use existing AI models rather than train or maintain its own foundation model.

The proposed approach is Bring Your Own AI (BYOAI).

Users will be able to configure their own supported AI provider and API credentials.

Potential providers include:

- OpenAI
- Google Gemini
- Anthropic

Provider availability and supported models will depend on implementation and API compatibility.

Intended AI Capabilities

The AI assistant should be able to:

- Understand the user's account data.
- Answer account-specific questions.
- Explain investment recommendations.
- Identify missing or incomplete information.
- Compare development options.
- Explain farming priorities.
- Evaluate potential pulls.
- Interpret account analytics.
- Provide context-aware guidance.

AI Reliability Principles

The intelligence engine should follow several important rules:

1. Recommendations must be grounded in available account data.
2. Missing information must be identified rather than invented.
3. Numerical calculations and resource costs should be handled by deterministic application logic whenever possible.
4. AI should interpret, explain, compare, and reason using calculated results.
5. HoYoLAB authentication cookies must never be sent to AI providers.
6. API credentials must not be hardcoded, committed, logged, or exposed.
7. AI provider credentials should be stored securely using Android Keystore-backed protection.

The application should also provide clear privacy controls and make external AI data transmission understandable to users.

---

7. Interactive Account Command Center

A major planned feature is an interactive dashboard that serves as the central workspace for exploring and improving a Genshin Impact account.

The goal is not to create a static statistics page. It should be an interactive environment where account information connects to useful actions and recommendations.

Account Overview

A unified view of:

- Account progression.
- Character roster.
- Character development.
- Weapons and equipment.
- Exploration.
- Available resources.
- Daily activities.
- Important account insights.

Interactive Analytics

Charts, statistics, and categories should be selectable.

Users should be able to explore specific data, filter results, and open detailed information instead of simply viewing static graphs.

Account Relationship Explorer

An interactive graph-style exploration system inspired by the discoverability of Obsidian's Graph View.

Potential relationships include:

- Characters and weapons.
- Characters and artifacts.
- Characters and teams.
- Character upgrades and required materials.
- Farming materials and their sources.
- Account weaknesses and possible solutions.
- Investments and their expected benefits.

For example:

Character → Talent Upgrade → Required Materials → Domain → Farming Priority

Selecting an element should reveal its relationships, details, and relevant actions.

Investment Command Center

A workspace for examining:

- Character development status.
- Upgrade requirements.
- Material shortages.
- Mora requirements.
- Resin costs.
- Investment benefits.
- Upgrade priorities.
- Dependencies between upgrades.

Farming Command Center

A workspace for:

- Selecting farming objectives.
- Identifying required materials.
- Comparing overlapping farming needs.
- Estimating Resin requirements.
- Identifying farming locations and schedules.
- Prioritizing objectives.

Pull Evaluation Workspace

An account-specific environment for evaluating:

- Roster compatibility.
- New team possibilities.
- Missing capabilities.
- Character and weapon redundancy.
- Resource opportunity costs.
- Long-term account value.

Context-Aware AI

The AI assistant should work alongside the dashboard.

Users should be able to select a character, resource, chart, or investment and ask questions about that specific context.

For example:

"What should I improve here first?"

The dashboard and AI should operate on a unified account model rather than function as disconnected features.

---

8. Development Roadmap

Development should proceed incrementally, with reliable account data and deterministic calculations established before advanced AI recommendations.

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

The roadmap is a development direction, not a claim that these phases have already been implemented.

---

9. Security and Privacy

Account authentication and AI provider credentials require careful handling.

Current implementation considerations:

- HoYoLAB authentication cookies are stored using ordinary SharedPreferences.
- Masking credentials in the interface does not encrypt their stored values.
- The login WebView clears its browsing data after authentication capture or dismissal.
- The application manifest currently sets "allowBackup=false".

Planned improvements:

- Android Keystore-backed credential protection.
- Secure AI API key storage.
- Explicit AI data-sharing controls.
- Minimized transmission of personal account information.
- No transmission of HoYoLAB authentication cookies to AI providers.
- Clear handling of authentication failures and expired credentials.

Security improvements must be verified through implementation and testing before being considered complete.

---

10. Current Limitations

Irminsul Codex is an early-stage project.

The following capabilities are not yet implemented or verified:

- AI provider integration.
- BYOAI configuration.
- AI account conversations.
- Account investment scoring.
- Automated account gap analysis.
- Farming optimization.
- Pull evaluation.
- Complete account inventory extraction.
- Interactive Account Command Center.
- Account relationship graph.
- Historical account snapshots.
- Automated test coverage.

The application's current data retrieval capabilities should not be confused with a complete representation of every Genshin Impact account asset.

---

11. Development Principles

The project should follow these principles as it evolves:

Account-specific intelligence

Recommendations should reflect the actual account rather than generic character-building advice.

Data before intelligence

The quality of recommendations depends on reliable, structured, and sufficiently complete data.

Deterministic calculations

Resource costs, upgrade requirements, and numerical comparisons should use application logic rather than relying on AI-generated arithmetic.

AI as an intelligence layer

AI should interpret and explain account information, not replace the underlying data and calculation systems.

Interactive by design

The interface should encourage exploration, filtering, comparison, and action.

Modular architecture

Data retrieval, account modeling, calculations, AI providers, and presentation should remain separable and maintainable.

Privacy and security

Credentials and personal account information must be handled carefully.

Incremental development

New capabilities should be built on verified foundations rather than implemented as disconnected features.

---

12. Project Status

Irminsul Codex currently has an Android application foundation with HoYoLAB authentication, Daily Note synchronization, selected account data retrieval, and JSON export.

The next major development priority is to establish a reliable unified account data model and determine which additional account information can be retrieved accurately.

This foundation will support the eventual intelligence engine, interactive Account Command Center, and AI-powered account assistant.

---

13. Repository

GitHub: https://github.com/berao609/Irminsul-codex

Irminsul Codex is an evolving project. Features described under the long-term vision and roadmap are planned capabilities unless explicitly identified as current functionality.
