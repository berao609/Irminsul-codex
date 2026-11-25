# Irminsul Codex
## Technical Architecture & Implementation Specification

**Document status:** Architecture baseline  
**Project:** Irminsul Codex  
**Repository:** `berao609/Irminsul-codex`  
**Target platform:** Android  
**Primary language:** Kotlin  
**UI framework:** Jetpack Compose  
**Architecture direction:** Layered, local-first, BYOAI  
**Last updated:** 2026-11-24

---

## 1. Purpose and Scope

This document defines the technical architecture and implementation direction for Irminsul Codex.

Irminsul Codex is intended to become an Android-based Genshin Impact Account Intelligence System. It should grow beyond displaying account statistics and provide useful, evidence-based guidance about account development, investment priorities, farming plans, resource allocation, and future actions.

This specification serves as the engineering reference for:

- Architectural decisions and boundaries.
- Application layers and package responsibilities.
- Account data representation and persistence.
- HoYoLAB integration.
- Deterministic account analysis and recommendation engines.
- Bring Your Own AI (BYOAI) integration.
- Security and privacy requirements.
- Dashboard and graph-based account exploration.
- Testing, build infrastructure, and implementation sequencing.

It is not a requirement to implement every described component immediately. The implementation should proceed incrementally, with each stage producing a working and testable application.

### 1.1 Document authority

This document distinguishes between:

- **Agreed direction:** A decision explicitly selected or accepted for the project.
- **Proposed design:** A recommended technical approach that still requires validation during implementation.
- **Open decision:** A question intentionally left unresolved until sufficient implementation experience or requirements exist.

Do not treat every proposed class, package, database table, or library as permanently finalized. Preserve the architectural intent while allowing evidence-based adjustments.

### 1.2 Out of scope

Unless separately approved, this specification does not authorize:

- Building or operating a mandatory cloud backend.
- Replacing the native Android application with a web app, Flutter, or React Native.
- Automatically uploading account data to a third-party service.
- Treating AI-generated recommendations as authoritative game calculations.
- Implementing every planned intelligence feature before stabilizing the existing application.
- Making repository changes merely because a design is documented here.

---

## 2. Product Vision

Irminsul Codex is an **AI-integrated Genshin Impact account intelligence and progression analyzer**.

Its primary purpose is to turn retrieved account data into reliable analysis, understandable insights, and practical progression decisions. Account retrieval and synchronization are supporting capabilities that supply the intelligence system; they are not the product's main user-facing purpose. JSON export remains available as an optional interoperability utility for third-party readers, backups, and users who want their data outside the application.

The application should help answer practical questions such as:

- What should I invest in next?
- Which characters are underdeveloped relative to my goals?
- What important account gaps should I address?
- What should I farm first?
- Which resources are limiting my planned upgrades?
- How should I allocate my available Resin?
- Which characters, weapons, or teams would provide the greatest account value?
- What should my next account-development step be?
- How has my account changed over time?
- Why is a particular recommendation being made?

The goal is to transform available account data into useful decisions, not merely present more statistics.

### 2.1 Core capabilities

The long-term product vision includes:

1. **Account Analytics** — Understand account progression, development, and overall state.
2. **Investment Intelligence** — Evaluate character, weapon, talent, and artifact development.
3. **Account Gap Intelligence** — Identify missing capabilities, weak coverage, and development gaps.
4. **Farming Intelligence** — Identify required materials and useful farming priorities.
5. **Pull Intelligence** — Evaluate potential character or weapon acquisitions against account needs.
6. **Resource Optimization** — Help allocate Resin, Mora, materials, and other resources.
7. **Next-Step Intelligence** — Recommend practical next actions based on goals and available resources.
8. **Proactive Intelligence** — Surface useful observations without requiring the user to ask every question.
9. **Historical Analytics** — Compare snapshots and show account development over time.
10. **AI Assistant** — Let users ask natural-language questions about their account through their chosen AI provider.
11. **Account Command Center** — Present account information through an interactive dashboard and relationship explorer.
12. **External Game Knowledge** — Combine the user's account data with current, source-attributed Genshin character meta, build guidance, weapon rankings, team information, and observed player usage so recommendations can explain both general game context and account-specific value.

These are product goals, not claims that all features are already implemented.

### 2.3 External game-knowledge sources

External game knowledge is an agreed product direction. It complements, but does not replace, deterministic analysis of the user's own account.

The intended information includes:

- Character rankings and meta relevance, separated by game mode and role where the source supports that distinction.
- Character build recommendations: weapons, artifact sets, main stats, substat priorities, talent priorities, teams, and rotations.
- Character-specific weapon comparisons, including refinement and team assumptions where available.
- Community theorycrafting and observed player usage, presented as different kinds of evidence rather than blended into one unexplained score.
- Patch-aware pull context and account-specific evaluations of whether a character or weapon fills a meaningful roster need.
- Guidance for prioritizing characters the user already owns but has not developed.

Initial candidate community sources include KeqingMains (KQM) and Prydwen. They are candidates, not approved or guaranteed integrations. Before importing or redistributing data, validate each source's available API or permitted access method, terms, licensing, attribution requirements, update cadence, and technical stability. Do not assume unrestricted scraping or republication is permitted.

Every external recommendation or ranking should retain, where available, its source, original link, retrieval/update date, game version or patch, role or game mode, and relevant assumptions such as team composition, weapon refinement, or constellation. Preserve whether a claim is theorycrafting, editorial ranking, calculated comparison, or observed usage. Do not present popularity as proof of optimal performance, or a tier placement as a universal measure of account value.

Personalized recommendations should compare external guidance with the user's actual roster, owned weapons, character development, and team needs. A high general ranking alone is not sufficient reason to recommend a pull. Missing, stale, conflicting, or unavailable source data must remain visible rather than being silently treated as current fact.

The initial implementation should remain local-first and avoid a mandatory backend or paid service. External source integration can begin with source-linked information and permitted structured data. Any future caching, synchronization, or provider service requires a focused design decision; no source credentials or HoYoLAB authentication cookies may be exposed to third parties.

These capabilities are planned product scope, not claims that external meta data is currently integrated.

### 2.2 Product positioning and feature priority

The primary product experience is account analysis and progression intelligence, not data extraction. The user should connect or synchronize an account and then use the in-app account overview, deterministic analysis, and AI assistance without needing to export a file or use a separate data reader.

The intended priority is:

1. **Account intelligence experience:** account overview, data quality and freshness, character/build assessment, account gaps, and actionable progression priorities.
2. **Deterministic intelligence engine:** authoritative calculations, validation, resource requirements, and explainable rules that ground analysis.
3. **Integrated AI assistant:** natural-language questions, explanations, comparisons, and context-aware guidance grounded in the locally available account model and deterministic results.
4. **Account Command Center:** dashboard and relationship exploration that make account condition and next actions discoverable.
5. **Optional data interchange:** versioned JSON export and, when appropriate, import/backup workflows. These are supporting utilities, not the primary onboarding or daily-use workflow.

Account synchronization remains essential as a data-input mechanism. It should feed the unified account model and local storage. The application should not require users to manually export and re-import their own account data to access core in-app analysis.

AI is an integrated interface to account intelligence, not a replacement for deterministic calculations. If AI is unavailable or not configured, core account viewing and deterministic analysis should remain useful. AI features require explicit user configuration and consent before account context is sent to an external provider.

This decision changes product priority and user experience. It does not authorize discarding the existing data-preservation, export-format, repository, or domain-model work, nor does it imply that AI, analytics, local persistence, or the redesigned interface are already implemented.

---

## 3. Agreed Architectural Direction

The following choices establish the current architecture baseline.

| Area | Agreed direction |
|---|---|
| Platform | Native Android |
| Primary language | Kotlin |
| UI | Jetpack Compose and Material 3 |
| Application data | Local-first |
| AI integration | BYOAI |
| Architecture | Layered |
| Calculations | Deterministic application logic |
| AI responsibility | Interpretation, explanation, and conversational assistance |
| Initial Gradle structure | One application module |
| Backend | No mandatory backend |
| Account data model | One unified model shared by application features |

### 3.1 Native Android

Continue using Kotlin and Jetpack Compose.

Do not migrate the application to Flutter, React Native, or a web-based architecture without a separate, evidence-based decision.

### 3.2 Local-first data

The device is the primary location for account data, user plans, and historical records.

The application should remain useful when no new account synchronization is available. Previously retrieved data should remain accessible, subject to the application's storage and retention policies.

Local-first does not mean entirely offline:

- HoYoLAB synchronization requires network access.
- Requests to external AI providers require network access.
- Other optional external services may require network access.

### 3.3 Bring Your Own AI

Users provide their own supported AI provider credentials.

The application should not require a proprietary Irminsul Codex AI backend to provide its core AI functionality.

The architecture must isolate provider-specific details behind adapters so additional providers can be supported without rewriting the intelligence domain.

### 3.4 Layered architecture

Separate presentation, domain logic, data access, and external integrations.

The UI must not own account calculations or directly coordinate low-level API requests. External provider details must not leak into the domain layer.

### 3.5 Deterministic intelligence first

Numerical calculations and authoritative game-rule logic belong in deterministic application code.

AI may explain results, compare trade-offs, help interpret goals, and answer questions using supplied context. It must not silently replace deterministic calculations with invented numbers.

### 3.6 Avoid premature complexity

Start with one Gradle application module.

Use package boundaries to establish separation. Introduce additional Gradle modules only when actual project size, build performance, dependency boundaries, or team needs justify them.

Do not create every package or abstraction in advance without a real implementation need.

---

## 4. Existing Application Baseline

The following describes the existing implementation as previously inspected. It is a working baseline to preserve and improve, not a complete code-quality or runtime audit.

### 4.1 Existing capabilities

The application currently includes:

- HoYoLAB login through an embedded WebView.
- Cookie capture from the login flow.
- Manual UID and cookie input.
- Genshin UID/server detection.
- Daily Note synchronization.
- Current and maximum Original Resin.
- Resin recovery estimates.
- Daily commission and extra-task reward status.
- Realm Currency.
- Weekly discounted Resin usage.
- Expedition counters.
- Selected account data retrieval.
- JSON export to a file or clipboard.

### 4.2 Previously inspected source areas

The existing implementation is concentrated in:

- `MainActivity.kt` — UI, application state, SharedPreferences credential handling, sync actions, and export actions.
- `api/HoYoLabClient.kt` — OkHttp requests, request headers, DS signature generation, UID/server handling, Daily Note retrieval, account retrieval, filtering, and JSON export.
- `ui/HoYoLabLoginDialog.kt` — WebView login, cookie capture, and WebView browsing-data cleanup.
- `.github/workflows/build.yml` — CI debug APK build and artifact upload.
- `AndroidManifest.xml` — Application and Android configuration.
- `app/build.gradle.kts` — App module build configuration.

Previously observed configuration included Kotlin 2.0.0, Android Gradle Plugin 8.5.0, compile SDK 34, target SDK 34, minimum SDK 26, OkHttp 4.12.0, Coroutines 1.8.1, JDK 17, and Gradle 8.14.8 in CI. These values must be rechecked against the current repository before implementation decisions depend on them.

### 4.3 Existing export structure

The existing JSON export has included sections such as:

- `exported_at`
- `uid`
- `server`
- `account_card`
- `profile_and_exploration`
- `daily_note`
- `characters`
- `spiral_abyss_current`
- `spiral_abyss_previous`
- `imaginarium_theater`

Character details have been retrieved in batches of up to eight IDs.

The current export is filtered. It should not be described as a complete raw API archive or a complete account inventory.

### 4.4 Known technical risks to address

Previously observed risks include:

1. **Concentrated responsibilities:** `MainActivity` and `HoYoLabClient` perform too many responsibilities.
2. **Credential storage:** HoYoLAB cookies have been stored in ordinary SharedPreferences. This is not encrypted credential storage.
3. **WebView cleanup:** Clearing WebView browsing data does not secure a cookie after it has been stored separately by the application.
4. **Missing versus zero:** Some parsing paths default missing numeric values to zero. For example, a missing `current_resin` can become `0`, making unavailable data look like a real zero.
5. **Error handling:** API failures and exceptions are not handled consistently across all export sections.
6. **Export filtering:** A `stripBloat()` routine has removed selected fields and strings containing HTTP URLs. Some removed fields may become useful to future features.
7. **Build reproducibility:** A Gradle Wrapper was not present in the previously inspected repository.
8. **Testing:** A complete automated test suite and runtime audit have not been established.
9. **Unofficial API dependency:** HoYoLAB endpoints and request-signature behavior may change.
10. **Data completeness:** Current account retrieval does not establish that every potentially useful account field is available.

These are investigation and stabilization targets. Recheck the current source before changing anything.

---

## 5. High-Level Architecture

The application should use four primary architectural areas:

1. Presentation
2. Domain
3. Data
4. External Integrations

A simplified dependency direction is:

```text
Presentation
     |
     v
   Domain
     ^
     |
Data and External Integrations
```

The domain defines the application-facing contracts. Data and integration implementations satisfy those contracts. Presentation invokes domain use cases and observes their results.

The exact dependency-injection mechanism may be selected during implementation. The important requirement is that dependencies remain explicit and testable.

### 5.1 Presentation layer

Responsibilities:

- Compose screens and reusable UI components.
- Navigation and screen state.
- ViewModels.
- User input and interaction.
- Loading, empty, partial, success, and error states.
- Rendering domain results and recommendations.

Presentation must not:

- Construct low-level HoYoLAB requests.
- Parse raw API JSON.
- Store credentials directly.
- Implement authoritative game calculations.
- Contain provider-specific AI request logic.
- Become the source of truth for account data.

### 5.2 Domain layer

Responsibilities:

- Core account and game-related models.
- Repository interfaces.
- Use cases.
- Deterministic calculations.
- Account analytics.
- Investment analysis.
- Account gap analysis.
- Farming and resource planning.
- Recommendation prioritization.
- Data-quality and freshness rules.

The domain layer should remain independent of Android UI and external SDKs.

It must not depend directly on:

- Jetpack Compose.
- Android UI classes.
- Room implementation classes.
- OkHttp.
- WebView.
- AI provider SDKs.

Prefer Kotlin types and interfaces that can be unit-tested without an Android runtime.

### 5.3 Data layer

Responsibilities:

- Repository implementations.
- Local database access.
- Remote data sources.
- API response parsing and mapping.
- Data normalization.
- Snapshot persistence.
- Import and export.
- Data migration.
- Synchronization coordination.

The data layer converts external and persisted representations into domain models.

### 5.4 External integration layer

Responsibilities:

- HoYoLAB connectivity.
- AI provider adapters.
- Credential protection services.
- External service-specific request and response handling.

Provider-specific SDKs and protocols must remain behind interfaces. Their types should not leak into domain models or presentation state.

### 5.5 Dependency rules

- Presentation depends on domain abstractions.
- Domain does not depend on presentation, data implementations, Android UI, or provider SDKs.
- Data implements domain repository interfaces.
- External integrations implement application-facing contracts.
- The application composition root wires implementations together.
- Cross-layer communication should use explicit models and contracts.

---

## 6. Proposed Package Structure

The following is the target package organization within the existing app module. It is a guide, not an instruction to create every directory immediately.

```text
app/src/main/java/com/irminsul/codex/
  app/
    CodexApplication.kt
    AppContainer.kt

  presentation/
    navigation/
    dashboard/
    characters/
    farming/
    investment/
    assistant/
    explorer/
    settings/
    components/

  domain/
    model/
    repository/
    account/
    analytics/
    investment/
    farming/
    recommendations/
    calculation/

  data/
    local/
      database/
      dao/
      entity/
    remote/
      hoyolab/
    repository/
    mapper/
    export/

  integration/
    ai/
      AiProvider.kt
      AiOrchestrator.kt
      context/
      providers/
    security/

  core/
    common/
    result/
    time/
```

### 6.1 Package guidance

| Package | Responsibility |
|---|---|
| `app` | Application initialization and dependency composition |
| `presentation` | Compose UI, ViewModels, navigation, and screen state |
| `domain/model` | Core application models |
| `domain/repository` | Repository contracts |
| `domain/account` | Account use cases and account-level rules |
| `domain/analytics` | Account analytics |
| `domain/investment` | Character and equipment investment logic |
| `domain/farming` | Material requirements and farming plans |
| `domain/recommendations` | Prioritization and recommendation models |
| `domain/calculation` | Reusable deterministic calculations |
| `data/local` | Room database, entities, and DAOs |
| `data/remote/hoyolab` | HoYoLAB requests and response handling |
| `data/repository` | Repository implementations |
| `data/mapper` | Mapping between API, database, and domain models |
| `data/export` | Versioned import/export |
| `integration/ai` | AI provider contracts, orchestration, and context construction |
| `integration/security` | Credential protection and cryptographic services |
| `core` | Small shared primitives that do not belong to a feature |

Avoid creating empty abstractions or generic utility packages without a concrete need.

---

## 7. Unified Account Data Model

All major features must use one coherent account representation.

The dashboard, analytics, farming engine, investment engine, graph explorer, and AI context builder must not independently parse HoYoLAB responses or maintain disconnected copies of account state.

### 7.1 Design goals

The unified model should:

- Represent the account consistently.
- Preserve the source and retrieval time of imported information.
- Distinguish missing data from actual zero values.
- Represent partial and failed retrievals.
- Support historical snapshots.
- Support multiple account sections with different freshness.
- Be independent of the raw HoYoLAB response format.
- Be suitable for local persistence and JSON export.
- Expand as new features require additional data.

### 7.2 Conceptual account model

A conceptual model may contain:

```text
Account
  accountId
  uid
  server
  displayName
  createdAt
  lastUpdatedAt

  latestSnapshot
  snapshotHistory

  profile
  exploration
  characters
  weapons
  artifacts
  resources
  dailyNote
  spiralAbyss
  imaginariumTheater

  sectionStatuses
```

This is illustrative. Exact Kotlin data classes and field types should be designed against actual source data and feature requirements.

### 7.3 Data quality and availability

A missing value must never silently become a valid zero.

The application should distinguish at least:

- **Available:** A value was successfully retrieved.
- **Missing:** The source did not provide the field.
- **Unavailable:** The source does not support or expose the information.
- **Failed:** Retrieval or parsing failed.
- **Stale:** A previously retrieved value exists but is older than the accepted freshness threshold.
- **Partial:** Some fields in a section were retrieved while others were not.

Where useful, model values with an explicit availability wrapper rather than using arbitrary sentinel values.

For example:

```kotlin
sealed interface DataValue<out T> {
    data class Available<T>(
        val value: T,
        val retrievedAt: Instant
    ) : DataValue<T>

    data object Missing : DataValue<Nothing>
    data object Unavailable : DataValue<Nothing>

    data class Failed(
        val reason: String
    ) : DataValue<Nothing>
}
```

This is a proposed pattern. Adapt it to the actual model and avoid excessive wrappers where a simpler type is sufficient.

### 7.4 Section-level status

Each independently retrieved account section should be able to report:

- Retrieval status.
- Retrieval timestamp.
- Data source.
- Error or warning information.
- Last successful retrieval timestamp, where applicable.

A failure to retrieve one section should not automatically erase valid data from unrelated sections.

### 7.5 Source metadata

Where practical, preserve:

- Source identifier.
- Retrieval timestamp.
- Source schema or parser version.
- Snapshot identifier.
- Relevant warnings.
- Whether the data was imported, synchronized, or manually entered.

This makes it possible to explain where a displayed value came from.

---

## 8. Local Data Storage

### 8.1 Storage direction

Use local persistence as the primary data store.

Proposed responsibilities:

| Technology | Intended use |
|---|---|
| Room | Structured account data, snapshots, plans, and sync history |
| DataStore | Ordinary preferences and UI settings |
| Android Keystore | Protection of cryptographic key material |
| Encrypted credential storage | HoYoLAB cookies and AI API keys |
| JSON | Portable import and export format |

The exact encryption implementation and database schema must be validated during implementation.

### 8.2 Room database

Room is the proposed primary structured database.

Potential initial entities:

- `AccountEntity`
- `AccountSnapshotEntity`
- `DataSectionStatusEntity`
- `CharacterEntity`
- `WeaponEntity`
- `ArtifactEntity`
- `ResourceSnapshotEntity`
- `UserPlanEntity`
- `SyncRecordEntity`

These are candidate entities, not a requirement to create all tables in the first database migration.

Start with the smallest schema that supports the first persistent account workflow. Add entities when real use cases require them.

### 8.3 Historical snapshots

Historical data is necessary for meaningful progress analysis.

The system should be able to:

- Save a snapshot after a successful synchronization.
- Preserve the timestamp and source of each snapshot.
- Compare the current account state with earlier snapshots.
- Identify changes in character development and resources when the underlying data supports it.
- Avoid treating failed or partial retrievals as complete account snapshots.

Snapshot creation should be deliberate. Avoid creating redundant history records for every UI refresh.

### 8.4 Raw payload preservation

Preserving original source payloads can help with:

- Parser improvements.
- Debugging.
- New feature development.
- Recovering fields not yet represented in normalized models.
- Auditing data transformations.

However, raw payload preservation must use a deliberate retention policy.

Requirements:

- Store only data that is useful and permitted to retain.
- Associate payloads with source, retrieval time, and schema/version metadata.
- Avoid unlimited storage growth.
- Provide a way to prune old raw payloads.
- Never include authentication cookies or API keys in exported account data.
- Do not assume every raw response is safe to expose to AI providers.

Raw data storage and normalized account records serve different purposes. The normalized model remains the application-facing source of truth.

### 8.5 Preferences

Use DataStore for ordinary preferences such as:

- Selected account.
- Theme preference.
- Dashboard layout preference.
- Selected AI provider identifier.
- Non-sensitive feature settings.

Do not store API keys or authentication cookies as ordinary preferences.

### 8.6 Import and export

JSON remains a portable data interchange format, but export is a secondary utility rather than the primary product workflow. The normal user journey should lead from account synchronization to local account analysis and intelligence inside Irminsul Codex. Users must not need to export JSON to a third-party reader to access the application's core value.


Export requirements:

- Include an export format version.
- Include export timestamp.
- Include account identity fields needed to interpret the export.
- Preserve section-level availability and retrieval metadata where useful.
- Avoid silently omitting useful fields through broad filtering.
- Exclude credentials, cookies, API keys, and authentication tokens.
- Make clear whether the export contains normalized data, raw payloads, or both.

Import requirements:

- Validate format version.
- Validate required fields.
- Handle unknown optional fields safely.
- Avoid overwriting existing account data without an explicit user action.
- Report partial import failures.
- Preserve source timestamps when appropriate.
- Do not trust imported values as verified live account data.

Encrypted backup/export may be considered later. It is an open decision, not a prerequisite for the initial storage implementation.

---

## 9. HoYoLAB Integration

### 9.1 Integration boundary

HoYoLAB access must be isolated behind a dedicated adapter.

The rest of the application should request account information through repository interfaces or use cases. It should not know how requests are signed, which headers are required, or how raw response JSON is structured.

### 9.2 Responsibilities

The HoYoLAB integration should handle:

- Authentication/session data supplied by the user.
- UID and server handling.
- Request construction.
- Required headers and signatures.
- Network execution.
- Response validation.
- API error interpretation.
- Raw response capture where permitted.
- Mapping into application data models.

### 9.3 API instability

HoYoLAB endpoints and request-signature behavior are unofficial and may change.

Therefore:

- Keep endpoint definitions centralized.
- Keep signing logic isolated.
- Avoid spreading endpoint-specific assumptions across the app.
- Validate responses before mapping them.
- Handle missing and changed fields safely.
- Preserve useful error context without exposing credentials.
- Make it possible to update the adapter without rewriting domain logic.

### 9.4 Synchronization behavior

Synchronization should be section-aware.

A failure in Daily Note retrieval should not erase valid character data. A successful character retrieval should not cause a failed Spiral Abyss section to appear current.

Each section should retain its own status and retrieval timestamp.

### 9.5 Request and response handling

The integration should distinguish:

- Network failure.
- Authentication failure.
- Rate limiting.
- Server-side error.
- Unexpected response format.
- Missing data.
- Unsupported endpoint.
- Successful response with partial content.

Do not convert all failures into empty objects or default numeric values.

### 9.6 Credential lifecycle

Credential handling should include:

- Secure storage.
- Explicit replacement and removal.
- Avoidance of logs and crash-report leakage.
- No inclusion in exports.
- No inclusion in AI context.
- Clear behavior when credentials expire or become invalid.

WebView cleanup may reduce retained browser data, but it does not replace secure storage of credentials captured by the application.

---

## 10. Deterministic Intelligence Engine

The intelligence engine is the core application logic that turns account data into actionable findings.

It should operate on the unified account model and should not depend on AI to calculate authoritative values.

### 10.1 Core engines

The planned intelligence capabilities should be implemented as separate, testable domain services.

| Engine | Responsibility |
|---|---|
| Account Analytics | Summarize account progression and development |
| Investment Engine | Evaluate character, weapon, talent, and artifact development |
| Account Gap Engine | Identify missing capabilities and development gaps |
| Farming Engine | Calculate material requirements and farming priorities |
| Pull Analysis Engine | Evaluate potential acquisitions against account needs |
| Resource Optimization Engine | Allocate Resin and other resources against goals |
| Recommendation Engine | Rank practical next actions |
| Data Quality Rules | Identify missing, stale, inconsistent, or incomplete data |

These engines should share models and calculations rather than independently reimplementing the same logic.

### 10.2 Deterministic calculations

Examples include:

- Required Mora.
- Required character experience materials.
- Required weapon enhancement materials.
- Required talent materials.
- Material shortfalls.
- Resin estimates when the relevant game rules and source data are known.
- Remaining upgrade requirements.
- Resource availability.
- Goal completion estimates.

Calculations should be traceable and unit-tested.

When a calculation depends on assumptions, expose those assumptions.

### 10.3 Recommendation model

A recommendation should be a structured result, not just a sentence generated by AI.

A conceptual recommendation may contain:

```text
Recommendation
  id
  title
  category
  priority
  reasons
  estimatedRequirements
  missingInformation
  supportingCalculations
  assumptions
  confidence
  relatedEntities
  generatedAt
```

Exact fields should be refined when the first recommendation workflow is implemented.

### 10.4 Explainability

The application should be able to answer:

- What is being recommended?
- Why is it recommended?
- Which account data supports it?
- What resources are required?
- What assumptions were used?
- What information is missing?
- What could change the recommendation?

Avoid presenting a priority score without explaining what it means.

### 10.5 User goals and constraints

Recommendations should eventually account for user-defined goals and constraints, such as:

- Building a particular character.
- Preparing a team.
- Improving Spiral Abyss performance.
- Saving resources for a future character.
- Avoiding certain farming activities.
- Prioritizing a short-term target over general account efficiency.

The recommendation engine should not assume that maximum theoretical account efficiency is always the user's goal.

### 10.6 Data limitations

An engine must not manufacture certainty when required account data is unavailable.

If a recommendation depends on missing or stale data, it should:

- Identify the missing information.
- Reduce confidence where appropriate.
- Avoid presenting an unsupported numerical result.
- Explain what additional data would improve the result.

---

## 11. BYOAI Architecture

### 11.1 Objective

Allow users to connect their own supported AI provider and ask questions about their Genshin account.

The application should support a provider-independent architecture while acknowledging that providers have different capabilities, APIs, limits, and behavior.

### 11.2 Main components

Proposed components:

- `AiProvider` — Common provider-facing contract.
- Provider adapters — Implementations for individual AI services.
- `AiOrchestrator` — Coordinates requests, context, and provider selection.
- `AiContextBuilder` — Builds the minimum relevant account context.
- Conversation repository — Stores conversation history locally if enabled.
- Credential service — Securely stores and retrieves provider keys.

### 11.3 Provider interface

A conceptual contract:

```kotlin
interface AiProvider {
    val providerId: String
    val capabilities: AiProviderCapabilities

    suspend fun generate(
        request: AiRequest
    ): AiResult
}
```

The exact request and response types must be designed around the supported common baseline.

Do not force all providers into capabilities they do not support.

### 11.4 Provider capabilities

Provider capabilities should be explicit and may include:

- Text generation.
- Streaming responses.
- Structured output.
- Tool or function calling.
- Image input, if supported and deliberately implemented.
- Context-window information, where available.

The application should detect or configure supported capabilities rather than assuming every provider behaves identically.

### 11.5 Initial provider candidates

Potential providers include:

- OpenAI.
- Google Gemini.
- Anthropic.

The final initial provider list, supported models, and compatibility baseline remain implementation decisions.

Provider APIs and model availability change. Verify current documentation before implementing each adapter.

### 11.6 AI responsibility boundaries

AI may:

- Explain deterministic analysis.
- Summarize account state.
- Answer natural-language questions.
- Compare options and trade-offs.
- Help interpret user goals.
- Suggest follow-up questions.
- Explain why a recommendation may or may not fit the user's priorities.

AI must not:

- Invent account values.
- Claim missing data is available.
- Replace deterministic calculations with unsupported arithmetic.
- Treat its own generated assumptions as verified account facts.
- Receive HoYoLAB cookies or provider API keys in its prompt context.
- Silently change account data or user plans.

### 11.7 Context construction

The AI Context Builder should provide only the data relevant to the user's question.

For example:

- A farming question needs target upgrades, material requirements, current materials, and relevant Resin information.
- An investment question needs character development, equipment, user goals, and deterministic evaluation results.
- A pull question needs account gaps, current roster, user priorities, and any explicitly supplied banner information.

Do not send the entire local database or all raw API payloads for every request.

Context should include provenance and freshness information where it affects the answer.

### 11.8 Grounded AI responses

The assistant should:

- Identify which account data it used.
- Distinguish retrieved facts from estimates and suggestions.
- Disclose missing or stale data.
- Refer to deterministic calculations where available.
- Explain uncertainty and assumptions.
- Avoid claiming that a recommendation is guaranteed to be optimal.

### 11.9 API key handling

API keys must be treated as secrets.

Requirements:

- Encrypt keys using cryptographic key material protected by Android Keystore.
- Do not store keys in ordinary SharedPreferences or DataStore as plaintext.
- Do not log keys.
- Do not include keys in account exports or backups unless a separate secure backup design is explicitly approved.
- Do not include keys in prompts.
- Provide a way to replace or remove a key.
- Avoid sending keys to any service other than the provider the user selected.

No client-side storage design can guarantee that a key is impossible to extract from a compromised or instrumented device. The application should reduce exposure and communicate this limitation honestly.

### 11.10 No mandatory backend

The default design sends AI requests from the device to the selected provider.

A backend is not required for the initial BYOAI architecture.

Any future proxy, relay, or cloud service would require a separate architecture and privacy decision.

---

## 12. Account Command Center

The Account Command Center is the main interactive environment for understanding and exploring the account.

It should unify the dashboard, relationship explorer, and AI assistant rather than treating them as disconnected features.

### 12.1 Dashboard

The dashboard should provide a useful overview of:

- Account status.
- Important resources.
- Character development.
- Current priorities.
- Farming requirements.
- Recent changes.
- Data freshness and synchronization status.
- Recommended next actions.

The dashboard should prioritize actionable information over decorative statistics.

### 12.2 Interactive relationship explorer

The graph explorer is inspired by the usefulness of Obsidian's Graph View, but it should represent meaningful Genshin account relationships.

Potential nodes:

- Characters.
- Weapons.
- Artifacts.
- Teams.
- Materials.
- Upgrade goals.
- Farming domains.
- Recommendations.

Potential relationships:

- Character uses weapon.
- Character equips artifact.
- Character belongs to team.
- Character requires material.
- Goal requires resources.
- Recommendation relates to character.
- Farming activity supplies required material.

The graph must be based on actual account and game data. It should not be a decorative visualization with arbitrary connections.

### 12.3 Context-aware AI

The user should be able to select a dashboard item or graph node and ask the AI assistant about that context.

Examples:

- Select a character and ask what to improve next.
- Select a material and ask which planned upgrades require it.
- Select a recommendation and ask why it has high priority.
- Select a team and ask which account gaps limit its development.

The selected item should become part of the AI context through a controlled context builder. Do not send unrelated account data by default.

### 12.4 Shared source of truth

The dashboard, graph explorer, and AI assistant must use the same unified account model and domain results.

Do not build separate data stores for each visualization or AI feature.

### 12.5 Graph implementation choice

A custom Jetpack Compose graph or a dedicated graph library may be evaluated later.

The choice remains open until requirements for graph size, interaction, layout, performance, accessibility, and maintenance are clearer.

---

## 13. Security and Privacy

Security is a foundational requirement because the application handles account credentials and user-provided AI keys.

### 13.1 Sensitive data

Treat the following as sensitive:

- HoYoLAB cookies and session credentials.
- AI provider API keys.
- Any authentication tokens.
- Private user notes or plans, where applicable.

### 13.2 Minimum requirements

- Use Android Keystore-protected cryptographic key material for credential encryption.
- Do not store credentials as plaintext in ordinary preferences.
- Do not log secrets or full authenticated request headers.
- Exclude credentials from JSON exports.
- Do not include credentials in AI context.
- Provide credential removal and replacement.
- Handle invalid or expired credentials safely.
- Review backup behavior and ensure secrets are not unintentionally included.
- Avoid collecting or transmitting account data that is not needed for a feature.

### 13.3 Local data protection

Local-first storage reduces dependence on a project-operated backend but does not automatically make local data secure.

The implementation should assess:

- Whether database encryption is necessary for the threat model.
- Whether raw payloads contain sensitive information.
- Whether device backups include application data.
- Whether exported files are protected by the user.
- Whether logs or crash reports can expose account information.

Database encryption and encrypted backup are separate design decisions from encrypting API credentials.

### 13.4 AI privacy

Before sending account context to an AI provider:

- Use the provider selected by the user.
- Send only the minimum relevant context.
- Never send HoYoLAB cookies or AI API keys as context.
- Avoid sending unrelated raw payloads.
- Make the external data transfer understandable to the user.
- Do not imply that local-first means AI requests remain on-device.

---

## 14. Error Handling and Application State

The application must represent loading, success, partial success, empty, stale, and failure states clearly.

### 14.1 Result handling

Use consistent result types or an equivalent pattern for operations that can fail.

A result should distinguish:

- Successful data.
- Successful but partial data.
- Recoverable failure.
- Authentication failure.
- Network failure.
- Invalid or unexpected source data.
- Unsupported operation.

Do not use empty collections or zero values as universal error signals.

### 14.2 Partial success

Account synchronization is composed of multiple independent sections.

The application should preserve successful section results when another section fails.

A summary should show which sections updated and which did not.

### 14.3 User-facing errors

Messages should be actionable and understandable.

Where appropriate, distinguish:

- Check your connection.
- Re-authenticate.
- Try again later.
- The source returned an unexpected response.
- This information is not available from the current source.
- Some account sections could not be updated.

Avoid exposing raw secrets, signed URLs, or sensitive request headers in error messages.

---

## 15. Testing Strategy

Testing should grow alongside implementation. A successful APK build is not proof that account synchronization, calculations, storage, or AI behavior is correct.

### 15.1 Domain unit tests

Test deterministic logic independently of Android and network access.

Examples:

- Material requirement calculations.
- Mora calculations.
- Resin estimates.
- Investment scoring rules.
- Account gap rules.
- Recommendation prioritization.
- Missing-data behavior.
- Stale-data behavior.
- Confidence and assumption handling.

### 15.2 Data parsing tests

Use sanitized fixtures for API responses.

Test:

- Expected response structures.
- Missing fields.
- Null fields.
- Unexpected types.
- Empty responses.
- Partial responses.
- Changed or unknown fields.
- Error responses.

A missing numeric field must not silently become a real zero.

### 15.3 Repository tests

Test:

- Local and remote coordination.
- Snapshot persistence.
- Partial synchronization.
- Failure preservation.
- Data freshness.
- Import/export behavior.
- Multiple account handling if implemented.

### 15.4 Database tests

When Room is introduced, test:

- DAO behavior.
- Inserts and updates.
- Snapshot queries.
- Relationships and foreign keys.
- Migrations.
- Data retention and pruning.

### 15.5 AI adapter tests

Test:

- Request mapping.
- Response mapping.
- Provider errors.
- Unsupported capabilities.
- Streaming behavior if supported.
- Structured output validation.
- Context minimization.
- Secret exclusion.

Use mock responses. Do not require real API keys in ordinary CI tests.

### 15.6 UI tests

Test important user journeys:

- Navigation.
- Loading states.
- Empty states.
- Error states.
- Account selection.
- Synchronization feedback.
- Dashboard rendering.
- AI configuration and conversation flows as implemented.

### 15.7 Integration tests

Test complete workflows where practical:

- Account synchronization.
- Persistence after synchronization.
- Reopening the app and loading saved data.
- Snapshot comparison.
- Import and export.
- Credential replacement and removal.
- AI request context construction.

### 15.8 Network testing

MockWebServer may be useful for testing HTTP request and response behavior without relying on live HoYoLAB services.

Never make automated tests depend on a user's real account credentials.

### 15.9 CI

The CI workflow should eventually:

1. Set up the required JDK.
2. Use the repository's Gradle Wrapper.
3. Run unit tests.
4. Run applicable static checks.
5. Build the debug APK.
6. Upload build artifacts when appropriate.

A CI build passing does not establish runtime correctness on a physical Android device.

---

## 16. Build and Development Infrastructure

### 16.1 Gradle Wrapper

Add and maintain the Gradle Wrapper as part of build reproducibility work.

CI and documented local build instructions should use the wrapper rather than assuming a globally installed Gradle version.

### 16.2 Dependency management

- Keep dependencies explicit.
- Avoid adding large libraries without a clear need.
- Review compatibility with the current Kotlin and Android Gradle Plugin versions.
- Keep provider SDKs isolated from domain logic.
- Prefer stable, maintained dependencies.
- Document important version constraints when they affect builds.

### 16.3 Static quality

Adopt appropriate linting and formatting checks as the codebase grows.

Do not let formatting or tooling setup delay foundational application work unnecessarily.

### 16.4 Physical-device verification

At meaningful milestones, verify the application on a real Android device.

At minimum, test:

- Startup and navigation.
- Login and credential lifecycle.
- Account synchronization.
- Local persistence.
- Export.
- Error and offline behavior.
- Performance of major dashboard and graph interactions when implemented.

---

## 17. Implementation Roadmap

Implementation should proceed incrementally. Establish reliable account data and deterministic calculations before depending on advanced AI recommendations.

The phases describe architectural dependencies. They do not prohibit small parallel experiments when those experiments do not destabilize the main application.

### Phase 0 — Stabilize the Existing Application

**Focus:** Reliability, security, and reproducible builds.

Tasks:

- Inspect the current source and configuration.
- Add the Gradle Wrapper.
- Establish a basic test framework.
- Fix missing-versus-zero parsing behavior.
- Standardize API error handling.
- Review and secure credential storage.
- Ensure secrets are excluded from logs and exports.
- Review existing export behavior.
- Preserve current working functionality.

**Exit criteria:**

- The existing synchronization workflow still works.
- Export still works and does not expose credentials.
- Missing data is not misrepresented as zero.
- Important failures are represented clearly.
- The project can be built reproducibly.
- Foundational tests run successfully.

### Phase 1 — Establish Layered Architecture

**Focus:** Separate UI, domain logic, and data access.

Tasks:

- Introduce ViewModels where appropriate.
- Move API operations out of UI code.
- Define initial domain models.
- Define repository interfaces.
- Implement repository adapters around existing behavior.
- Separate parsing and mapping from request execution.
- Move responsibilities incrementally rather than rewriting the app all at once.

**Exit criteria:**

- UI does not need to know HoYoLAB request details.
- Domain calculations can be tested independently.
- Data retrieval and parsing have clear boundaries.
- Existing functionality remains usable.

### Phase 2 — Introduce Local Account Storage

**Focus:** Persistent account state and history.

Tasks:

- Introduce Room.
- Define the initial unified account model.
- Add only the database entities required by the first workflows.
- Normalize retrieved account data.
- Persist successful snapshots.
- Track section-level status and freshness.
- Load saved account data without requiring a new synchronization.
- Improve versioned JSON import/export.

**Exit criteria:**

- Account data persists locally.
- Previously saved data remains accessible after restarting the app.
- Section-level freshness and failure states are represented.
- Snapshot history can be queried.
- Import/export behavior is validated.

### Phase 3 — Implement Deterministic Intelligence

**Focus:** Useful account guidance without AI dependency.

Tasks:

- Implement account analytics.
- Implement initial investment analysis.
- Implement account gap analysis.
- Implement material requirement calculations.
- Implement farming priorities.
- Implement resource optimization.
- Implement explainable recommendation records.
- Add tests for calculations and prioritization.
- Introduce user goals where required by the first workflows.

**Exit criteria:**

- The application can produce useful account analysis without AI.
- Numerical results are deterministic and tested.
- Recommendations explain their supporting data and assumptions.
- Missing data is handled honestly.

### Phase 4 — Add BYOAI

**Focus:** Provider-independent conversational intelligence.

Tasks:

- Define the common AI provider contract.
- Define provider capability reporting.
- Implement credential protection.
- Implement the AI Context Builder.
- Implement the orchestration layer.
- Add the first provider adapter.
- Add additional provider adapters based on validated requirements.
- Add conversation handling and local persistence if enabled.
- Test context minimization and secret exclusion.
- Ensure deterministic calculations remain authoritative.

**Exit criteria:**

- A user can configure a supported provider.
- The app can send a relevant, minimal account context.
- The AI can answer account questions with appropriate grounding.
- Provider errors and unsupported capabilities are handled clearly.
- Credentials remain outside prompts, logs, and account exports.

### Phase 5 — Build the Account Command Center

**Focus:** Integrated dashboard, graph exploration, and context-aware AI.

Tasks:

- Develop the account dashboard.
- Add historical account views.
- Build the relationship explorer.
- Connect graph nodes to actual account entities and domain relationships.
- Allow users to select an item and ask the AI about it.
- Surface proactive recommendations.
- Improve navigation and interaction between account data and intelligence.

**Exit criteria:**

- Dashboard and graph use the same account model.
- Graph relationships are grounded in actual data.
- Selected elements can provide relevant AI context.
- Historical views use persisted snapshots.
- The Command Center provides actionable account guidance.

---

## 18. Acceptance Criteria by Capability

These criteria describe expected behavior when the corresponding capability is implemented.

| Capability | Acceptance criteria |
|---|---|
| Account synchronization | Retrieves supported data, reports section status, and preserves prior valid data on partial failure |
| Local account storage | Persists normalized account data and reloads it after app restart |
| Data quality | Distinguishes zero, missing, unavailable, failed, and stale values |
| Historical analytics | Compares valid snapshots and identifies the relevant time periods |
| Investment intelligence | Produces explainable evaluations from available account data |
| Farming intelligence | Calculates requirements and identifies shortages using tested rules |
| Resource optimization | Shows the constraints and assumptions behind resource plans |
| Recommendations | Provides reasons, requirements, supporting data, and uncertainty |
| BYOAI | Uses the selected provider and sends only relevant context |
| Credential security | Protects credentials and excludes them from logs, prompts, and exports |
| Dashboard | Presents useful account state and actionable priorities |
| Graph explorer | Shows meaningful, data-backed relationships |
| Context-aware assistant | Uses the selected account element and relevant supporting data |
| Import/export | Validates versioned data and excludes secrets |
| Build quality | Uses reproducible builds and automated tests appropriate to implemented features |

---

## 19. Open Architectural Decisions

These decisions are intentionally deferred. They do not all block the initial stabilization phase.

### 19.1 Multiple accounts

Should one installation support multiple Genshin accounts?

Consider:

- Account switching.
- Separate credentials.
- Separate snapshots.
- Separate user goals.
- Data isolation.
- Export/import behavior.

Do not build complex multi-account support before confirming the expected user workflow. However, avoid a data model that makes future account separation unnecessarily difficult.

### 19.2 Historical retention

What should the default snapshot and raw-payload retention periods be?

Consider:

- Database size.
- Value of long-term trends.
- Frequency of synchronization.
- User control.
- Whether raw payloads need shorter retention than normalized snapshots.

### 19.3 Encrypted account backup

Should the application support encrypted backup/export?

This is separate from ordinary JSON export and credential encryption. Define the threat model and recovery behavior before implementation.

### 19.4 Initial AI provider baseline

Which providers and capabilities should be supported first?

Choose based on:

- API stability.
- Documentation.
- Structured output support.
- Tool calling support.
- Streaming support.
- Model availability.
- Cost and user accessibility.
- Implementation and maintenance effort.

Do not assume provider feature parity.

### 19.5 Graph implementation

Should the graph explorer use a custom Compose implementation or a dedicated graph library?

Evaluate:

- Interaction quality.
- Layout algorithms.
- Performance.
- Accessibility.
- Maintenance.
- Licensing.
- Integration with Compose.

### 19.6 Optional cloud synchronization

Could optional cloud synchronization become useful later?

It is not part of the current baseline. Any future cloud sync requires a separate decision covering authentication, conflict resolution, encryption, privacy, hosting, and operational cost.

### 19.7 Database encryption

Determine whether encrypting the full local database is necessary based on the actual threat model and implementation cost. Credential encryption is required independently.

---

## 20. Engineering Rules and Non-Negotiable Boundaries

1. **One unified account model:** Major features must share the same normalized account representation.
2. **Deterministic calculations:** Authoritative numerical calculations belong in tested application logic.
3. **AI is not the source of truth:** AI explains and interprets application data; it does not invent account facts.
4. **Missing is not zero:** Never silently convert unavailable or missing data into a valid zero.
5. **Preserve partial success:** A failed section must not erase unrelated successful data.
6. **Isolate external services:** HoYoLAB and AI provider details must remain behind adapters.
7. **Protect secrets:** Credentials must not appear in logs, prompts, or ordinary exports.
8. **Minimize AI context:** Send only the account information relevant to the user's request.
9. **Local-first by default:** Core account data and intelligence should not require a project-operated backend.
10. **Test calculations:** Important game and resource calculations must have automated tests.
11. **Build reproducibly:** Use the Gradle Wrapper and CI checks.
12. **Implement incrementally:** Preserve working functionality and avoid unnecessary rewrites.
13. **Avoid premature modularization:** Keep one Gradle app module until additional modules have a demonstrated benefit.
14. **Be transparent about uncertainty:** Recommendations must disclose assumptions and missing data.
15. **Do not confuse plans with implementation:** A roadmap item is not a completed feature until it exists and has been verified.

---

## 21. Current Limitations and Verification Notes

This specification is an architecture and implementation plan, not a claim that the planned architecture has already been implemented.

The existing application baseline was previously inspected, but the following still require fresh verification before implementation:

- Current repository source and branch state.
- Current Gradle and dependency configuration.
- Current HoYoLAB endpoint behavior.
- Current Android security configuration.
- Actual runtime behavior on the target device.
- Existing automated test coverage.
- Current export schema and data completeness.
- Current provider API documentation and model capabilities.

The previously inspected app configuration and source may have changed since that inspection. Always verify the current source before applying code changes.

No repository changes are implied by this document.

---

## 22. Implementation Workflow

For each implementation task:

1. Identify the relevant phase and acceptance criteria.
2. Inspect the current source files and build configuration.
3. Confirm the existing behavior and dependencies.
4. Define the smallest useful implementation scope.
5. Identify affected layers and interfaces.
6. Implement without breaking existing working behavior.
7. Add or update tests.
8. Run the relevant build and test commands.
9. Verify behavior on a device when the change affects runtime functionality.
10. Report exactly what changed, what was tested, and what remains unverified.

### 22.1 Change reporting

Implementation reports should distinguish:

- **Designed:** Architecture or code approach proposed.
- **Edited:** Source files changed locally.
- **Built:** Build command completed successfully.
- **Tested:** Named automated tests passed.
- **Device-verified:** Behavior was observed on a physical or emulated device.
- **Committed:** A Git commit was created.
- **Pushed:** The commit was uploaded to the remote repository.

Never claim one status based only on another. For example, a successful build does not prove device behavior, and a local edit does not mean a change was committed or pushed.

---

## 23. Reference Summary

The current architectural baseline can be summarized as follows:

- Native Kotlin Android application using Jetpack Compose.
- Layered architecture with clear presentation, domain, data, and integration boundaries.
- Local-first account data and historical storage.
- Room as the proposed structured database.
- DataStore for ordinary preferences.
- Android Keystore-protected encryption for credentials.
- HoYoLAB isolated behind an adapter.
- One unified account model shared by all features.
- Deterministic intelligence engines for calculations and recommendations.
- BYOAI through provider adapters.
- AI context minimized and grounded in account data.
- Dashboard and graph explorer powered by the same account model.
- One Gradle application module initially.
- Incremental implementation, automated tests, and reproducible builds.

The guiding principle is:

> Build a reliable account-data foundation first, make deterministic intelligence useful second, and integrate AI as an explainable assistant on top of that foundation.

---

**End of specification.**
