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

### 3.7 Account-first investment intelligence

Investment recommendations must be account-relative rather than a blanket list of every unbuilt or underinvested character.

- Keep account diagnostics separate from investment recommendations. Diagnostics report genuine data-integrity, retrieval, build-validation, and stat-reconciliation issues. They must not automatically imply that a character deserves more resources.
- Evaluate investment candidates against the user's actual roster, owned weapons, current development, team options, role coverage, and known constraints.
- Consider team synergy, account value, role redundancy, current game-mode relevance, character/build requirements, and estimated investment cost when evidence is available.
- Produce a selective, explainable shortlist. A character can be underbuilt without being a current priority. "Low priority" means other account investments offer greater value now; it is not a judgement that the character is bad.
- Explain why a candidate is recommended, which owned teams it improves or enables, what is required to make it effective, and why it ranks ahead of alternatives.
- Recalculate when relevant account state or trusted game knowledge changes. Do not change priorities merely to create activity when the evidence has not changed.

The recommendation engine remains deterministic for explicit account facts, rule evaluation, and ranking inputs. AI can synthesize evidence, explain trade-offs, and assist with ambiguous comparisons, but it must not invent account state or become the authority for numerical calculations.

### 3.8 Hybrid external game-knowledge lifecycle

The selected direction is hybrid: automatically discover and collect updates from supported community/game sources, then verify and curate knowledge before it can influence trusted recommendations.

1. **Discover and collect:** Detect source revisions, new guides, relevant patch information, and other supported updates using permitted access methods.
2. **Stage:** Store candidate information separately from trusted knowledge. Preserve source identity, original URL, retrieval time, game version/patch when known, content type, and relevant assumptions.
3. **Verify and curate:** Validate source access and redistribution permissions, attribution, freshness, evidence quality, and conflicts. Unverified or conflicting material must not silently become trusted recommendation input.
4. **Publish trusted knowledge:** Promote verified records into a versioned knowledge set. Preserve provenance and distinguish theorycrafting, editorial guidance, calculated comparisons, and observed usage.
5. **Recalculate:** Re-evaluate affected account recommendations when trusted knowledge changes or new account data is received. Retain enough prior context to explain what changed and why.

Automatic collection does not mean automatic trust or unrestricted scraping. Source terms, licensing, rate limits, technical stability, and permitted redistribution must be checked before integration. Candidate sources such as KeqingMains and Prydwen are not guaranteed integrations.

The hybrid lifecycle does not by itself authorize a mandatory cloud backend. Begin with the simplest permitted local-first approach. Any hosted collector, publishing service, or backend requires a separate focused architecture and operating-cost decision. Core account viewing and deterministic analysis must remain useful without cloud AI. External AI receives account context only with explicit user configuration and consent, and must never receive HoYoLAB authentication cookies.

### 3.9 AI responsibility in the hybrid system

AI may assist with extracting candidate claims from permitted source material, identifying possible changes, comparing evidence, and generating user-facing explanations. Candidate extraction is not verification. A human-review or otherwise explicitly validated publishing gate must exist before AI-processed knowledge becomes trusted input.

The system must retain the original source and evidence for every trusted claim. If sources disagree, are stale, or do not support a conclusion, represent that uncertainty instead of forcing a confident ranking. AI output must be constrained by the trusted knowledge records and the current account model.

---


### 3.10 External source validation register

The following is the initial source-access decision from M44 research conducted on 2026-11-25. These decisions are conservative and must be revisited if a source publishes new terms, licenses, APIs, or permissions.

| Source | Intended role | Observed access/licensing position | Initial decision |
|---|---|---|---|
| KeqingMains (KQM) character guides and Theorycrafting Library (TCL) | Vetted theorycrafting, character builds, teams, rotations, and mechanics | KQM states that TCL content is reviewed by its Theorycrafting Editors. The public TCL repository's root currently exposes no LICENSE file. Public visibility and editor review do not grant redistribution rights. | Approved as an outbound research/reference link only. Do not bulk ingest, copy, mirror, or redistribute guide/TCL content unless KQM grants permission or publishes an applicable content license. Metadata-only revision monitoring may be evaluated separately and must not retrieve or republish protected content. |
| Prydwen | Editorial character evaluations, tier lists, guides, and databases | Its Terms of Service, last updated March 2026, prohibit scraping, crawling, and bulk extraction. Its rankings and guides are editorial analysis and may not always be current. | No automated collection or bulk extraction. Link to relevant pages only. Reconsider only with explicit written permission or a documented supported API/license. |
| genshin-db | Structured static game reference data such as characters, weapons, artifacts, talents, and constellations | Repository declares MIT. Its README says data is sourced from Fandom Wiki and GenshinData; the package license alone does not establish that all upstream material, text, or assets are available for redistribution under MIT. Its data format may change between versions. | Candidate for technical/schema evaluation only. Do not bundle, mirror, or redistribute its dataset until upstream data and asset rights, attribution, and update terms are separately verified. |
| genshin.dev API | Structured static game reference data | Repository declares Open Software License 3.0. That license carries source, attribution, and external-deployment obligations. The API's software license does not by itself clear HoYoverse-owned game names, artwork, or other third-party content. | Not selected for the initial integration. Reconsider only after legal/compatibility review of OSL-3.0 obligations and the underlying data/assets. |
| HoYoverse official game content and assets | Canonical game facts and visual assets | Game content and assets remain subject to HoYoverse intellectual-property and service terms. No general redistribution license was established in this review. | Do not scrape or bundle official site/game assets as part of M44. Existing account-source URLs remain governed by their current retrieval behavior and are not a new asset-ingestion permission. |

Source references reviewed:

- KQM TCL repository and README: https://github.com/KQM-git/TCL
- KQM Theorycrafting Library: https://library.keqingmains.com/
- KQM source/editor team description: https://keqingmains.com/apply/
- Prydwen Terms of Service: https://www.prydwen.gg/terms
- genshin-db repository and README: https://github.com/theBowja/genshin-db
- genshin-db MIT license: https://github.com/theBowja/genshin-db/blob/main/LICENSE
- genshin.dev API repository: https://github.com/genshindev/api
- genshin.dev OSL-3.0 license: https://github.com/genshindev/api/blob/mistress/LICENSE

This register is a technical planning record, not legal advice or a legal determination. A source is not approved for content reuse merely because it is publicly accessible or its software repository has an open-source license.

**M44 source decision:** no third-party community guide, ranking, or game-asset dataset is approved for automated content ingestion or redistribution at this time. KQM and Prydwen may be linked as research references. Any future automated source adapter must be scoped to a documented permitted API, license, written permission, or metadata-only mechanism whose terms and rate limits have been checked.

### 3.11 Deterministic investment-ranking input boundary

The current normalized `AccountSnapshot` and `CharacterSnapshot` provide useful account evidence:

- Owned character identity, level, rarity, element, friendship, and active constellation count.
- Character weapon, artifact, stat, talent, and constellation details when returned by the account source.
- Character collection integrity, build validation, stat classification/reconciliation, section coverage, and freshness.
- Explicit missing, unavailable, failed, stale, and available states for important account data.

The current normalized model does not yet provide a sufficiently explicit, validated basis for a complete account-relative investment ranking. In particular, it does not yet provide normalized records for:

- Character roles and capabilities mapped to the game's current combat/team context.
- Validated team compositions, rotations, and character-to-character synergy evidence.
- Patch- and game-mode-specific relevance records with source provenance and expiry/freshness rules.
- Character, weapon, talent, and artifact investment costs linked to the player's actual material inventory and resource constraints.
- Distinct observed-usage statistics with population, sample period, game mode, and methodology.
- An explicit user objective or account constraint model for prioritizing different forms of account value.

Do not infer these missing dimensions from character level, rarity, tier labels, or unstructured text. Do not publish a numerical investment score or fixed weights until the required evidence and normalized inputs exist.

The initial deterministic ranking design must evaluate, as evidence becomes available:

1. Incremental account value and missing role/capability coverage.
2. Compatibility with owned characters and validated team/rotation synergy.
3. Current relevance for explicitly identified game modes and patch context.
4. Redundancy against owned alternatives and the user's existing investment.
5. Expected investment cost and efficiency relative to available resources.
6. Evidence confidence, provenance, freshness, and disagreement between sources.

Keep source evidence types separate: theorycrafting, editorial evaluation, calculated comparison, and observed usage are not interchangeable. Missing evidence must reduce confidence or leave a factor unknown; it must not be silently treated as a negative character rating.

The first implementation should be a small, explainable shortlist with reason codes and evidence references, not a universal character tier list. The app must continue to distinguish account diagnostics from investment recommendations. This section defines the decision boundary; it does not claim the ranking engine is implemented.


### 3.12 Versioned game-knowledge record contract

The first game-knowledge record must be small, typed, attributable, and independently reviewable. It is not a copy of a guide, social post, video transcript, or third-party database.

A trusted knowledge record must carry the following logical fields:

| Field | Requirement |
|---|---|
| `schemaVersion` | Version of the record schema. |
| `recordId` | Stable identifier for this specific claim or observation. |
| `subjectType` / `subjectId` | Character, weapon, artifact, team, mechanic, game mode, or other explicitly supported subject. |
| `claimType` | Controlled type such as role/capability, team synergy, build guidance, weapon comparison, mechanic, mode relevance, or usage observation. |
| `claim` | Minimal structured fact or normalized claim. Do not store copied guide prose or full social content by default. |
| `evidenceClass` | Theorycrafting, editorial evaluation, calculated comparison, observed usage, game reference, or creator demonstration. |
| `sourceRefs` | One or more source-registry IDs and canonical URLs supporting the claim. |
| `gameContext` | Game version/patch, phase when known, game mode, difficulty, encounter/boss where relevant, and observation period. Unknown values remain explicitly unknown. |
| `publishedAt` / `observedAt` / `retrievedAt` | Distinct source publication, underlying observation, and app retrieval timestamps. Do not substitute one for another. |
| `validFrom` / `expiresAt` | Effective period and review/expiry boundary. Expiry means review required, not that the claim is automatically false. |
| `reviewState` | Candidate, verified, rejected, superseded, or expired-review-required. |
| `reviewedAt` / `reviewNotes` | Human curation record and the reason the claim was accepted, rejected, or superseded. |
| `confidenceState` | Supported, limited, conflicted, or unknown, with a short reason. This is not an invented probability. |

A separate source-registry entry must describe the publisher/platform, source category, canonical source location, access method, current terms/license/permission state, attribution requirements, allowed fields/actions, retention/deletion constraints, rate limits or quotas, and the date the terms were last checked. Source permission is evaluated independently from claim quality.

Records are immutable by version: a changed claim or changed evidence context creates a new record version or superseding record. Preserve the prior record and its provenance so a recommendation change can be explained. A record must never become trusted solely because an AI model extracted it.

### 3.13 Usage observations and social-source policy

Usage observations are a distinct evidence class, not a character-strength score. A usage observation must preserve, where the publisher supplies them:

- Game mode and exact cycle/version, including Abyss cycle or Stygian Onslaught cycle.
- Difficulty, encounter/boss, and eligible stage.
- Sample size and what the sample represents.
- Numerator and denominator, including the denominator's exact population.
- Ownership/holding-rate context when supplied.
- Collection period, publication date, and retrieval date.
- Region/server or other population filters when supplied.
- Source publisher and methodology notes.
- Whether the figure is preliminary, still updating, finalized, or otherwise qualified.

Never compare or combine two usage percentages unless their populations, denominators, game modes, difficulty, and observation windows are compatible. Do not interpret a high usage rate as proof of superior theoretical performance. Selection effects matter: clear-only samples, ownership filters, high-investment accounts, and encounter-specific buffs can materially change the result. Missing methodology means the observation is limited or unknown, not a precise universal ranking.

Initial channel decisions:

| Channel | Useful candidate evidence | Access and reuse boundary | M45 policy |
|---|---|---|---|
| HoYoLAB community posts | Abyss/Stygian reports, creator guides, links to underlying datasets | Public visibility is not an API or permission to scrape, download, mirror, or redistribute. HoYoLAB terms restrict unauthorized exploitation and reproduction of its services/content. | Keep canonical links and manually curated claim references. Do not build an automated HoYoLAB scraper or treat HoYoLAB as the authoritative origin when a report identifies another data publisher. |
| Original usage-data publishers (e.g. YSHelper, HxG) | Structured usage observations and methodology | Access depends on an actual supported API, published license, or explicit permission from the publisher. A repost on HoYoLAB does not grant rights to the underlying dataset. | Preferred source to evaluate for usage metrics. Seek publisher-origin data and document sample/methodology and reuse permission before automation. |
| YouTube | Public video/channel metadata, creator guides, demonstrations, and official playback links | Use the official YouTube Data API and comply with its Developer Policies, quota, metadata display, retention, and playback requirements. API search is quota-limited; default allocation currently includes 100 search calls per day. Do not assume video transcripts/captions may be harvested or republished. | Candidate discovery through permitted metadata and official video links/embeds. Claims from videos remain candidates until independently reviewed. |
| X | Public posts, creator announcements, patch discussion, guide links | Programmatic access requires an X developer application/API access and compliance with current API plan, developer terms, content display, deletion, and redistribution rules. Availability and cost can change. | Possible future discovery adapter only after current access tier, costs, and policy obligations are verified. Otherwise use links/manual curation. |
| TikTok | Public creator videos and build demonstrations | Research API access is limited to approved eligible researchers and organizations/regions; it is not a generally available commercial content-search API. Public videos do not grant unrestricted extraction or reuse rights. | No automated collection in the initial app. Use links or authorized embeds; manually curate claims with provenance. Reassess only if a supported eligible API or permission becomes available. |
| Facebook | Public Page posts, creator guides, discussion links | Graph API access to public Page content can require Page Public Content Access and app review/permissions. Meta Content Library/API is restricted to qualified research organizations and approved research programs. | No broad Facebook scraper. Use links/manual curation; evaluate official permissioned Page API access only for a narrowly scoped source. |
| KQM and Prydwen | Theorycrafting and editorial guides | Existing M44 decisions remain in force: KQM guide/TCL content is link-only absent a suitable content license or permission; Prydwen prohibits scraping/crawling/bulk extraction under its reviewed terms. | Link-only research references unless permissions change. |
| Licensed game-reference datasets | Character, weapon, artifact, talent, and mechanics metadata | Verify both package license and upstream data/asset rights. Software license alone does not clear third-party game content or artwork. | Separate technical and rights review before importing. |

For all social channels, store the minimum metadata needed to identify and revisit a source: canonical URL, platform, creator/publisher identifier when permitted, title or short neutral label, publication timestamp when available, retrieval timestamp, and the specific claim being evaluated. Do not mirror full posts, images, videos, comments, transcripts, or personal profile histories by default. Respect source deletion, access revocation, rate limits, and platform-required embeds/attribution. No private, follower-only, login-gated, or otherwise restricted content may be collected by bypassing access controls.

AI may assist with candidate discovery, classification, and claim extraction only when the source's terms and permitted access method allow that processing. AI output remains candidate evidence. A human or separately validated deterministic process must verify the source, patch context, methodology, and claim before promotion to trusted knowledge. Never send HoYoLAB cookies or platform credentials to AI.

### 3.14 Deterministic investment-shortlist contract

The first investment output is an account-relative shortlist, not a universal character tier list and not a warning list of every unbuilt character.

Each recommendation result must include:

- `ruleSetVersion` and `evaluatedAt`.
- Character identity and current account snapshot reference.
- Priority band: High priority, Worth investing, Situational, Low priority for now, or Already sufficient.
- One or more stable reason codes.
- Account-specific benefit: role/capability gap addressed or team meaningfully improved/enabled.
- Owned-character synergy and the evidence supporting the proposed team interaction.
- Current mode/patch context and whether it is supported, limited, conflicted, or unknown.
- Redundancy/alternative comparison against the user's owned and already-invested characters.
- Investment requirements and cost only when supported by game-reference data and, for account-specific affordability, actual user resource inventory.
- Supporting trusted knowledge-record IDs and canonical evidence links.
- Unknown or conflicting factors that materially limit the recommendation.
- A concise explanation of why this candidate ranks ahead of the next alternatives.

The first ruleset must be deterministic, versioned, and testable. It uses explicit reason-code and eligibility rules, not an LLM-generated score. The default ordering is balanced and account-first. Usage rate is contextual evidence only; it cannot independently promote a character to High priority. Editorial tier labels cannot independently determine account priority.

Do not calculate a numeric aggregate score or invent fixed factor weights until the required inputs and validation dataset exist. If a critical factor is unknown, the result must expose that uncertainty and may remain Situational or unranked. Do not convert missing data into a negative character assessment. Account diagnostics (missing, stale, malformed, or failed data) remain separate from investment recommendations.

The first useful shortlist should be small and selective. It should explain why a character is valuable to this account, which owned teams benefit, what is required to make the investment effective, and why that investment is preferable to competing uses of resources. If the evidence cannot justify a recommendation, return no recommendation rather than filling the list.

**M45 implementation boundary:** define and test the record/shortlist contract before adding network collectors, social-platform SDKs, AI services, or a scoring engine. Initial source references may be curated manually. External-source adapters must be independently permission-checked and optional; core account analysis remains local-first and usable without cloud AI.


### 3.15 M47 account-input mapping and evidence boundary

M47 inspected the current normalized `AccountSnapshot`, its nested character/equipment models, account-data mapper, existing account-gap/build/stat analyzers, and the M46 shortlist contract. The current account model supports a limited set of reliable account facts. It does not yet support a complete account-relative investment decision by itself.

#### Facts derivable from the current account snapshot

| Input area | Current source fields | What can be derived safely | Required caveats |
|---|---|---|---|
| Owned character inventory | `AccountSnapshot.characters`, `CharacterSnapshot.id/name/rarity/element`, `CharacterCollectionIntegrity` | Which character IDs are present in the retrieved roster; mapped roster count; duplicate/invalid source entries; known name, rarity, and element | Only the retrieved roster is known. Missing, unavailable, failed, stale, partial, or malformed roster data must remain distinct. Do not infer ownership of absent characters from an incomplete roster. |
| Character investment state | `CharacterSnapshot.level`, `activeConstellationCount`, `skills.level`, weapon level/ascension/refinement, artifact rarity/level | Observed current character, talent, weapon, constellation, and artifact investment values when populated | These are state observations, not quality scores. Missing values remain unknown. Do not infer build quality, combat role, or that a character is sufficiently invested from level/constellation alone. |
| Equipped weapon | `weapon: DataValue<WeaponSnapshot>` and weapon ID/type/rarity/level/ascension/refinement/stats | Whether the weapon payload is available, missing, unavailable, failed, or stale; known equipped weapon and its observed state | Weapon suitability, rankings, and comparative value require reviewed game-knowledge claims. |
| Equipped artifacts | `artifacts: DataValue<List<ArtifactSnapshot>>`, artifact slot/set/stat fields, and `EquipmentIntegritySnapshot` | Observed equipped pieces, empty slots, known main/sub-stats, duplicate or invalid slots/IDs, malformed entries | Empty or partial equipment is a valid player choice, not an investment deficiency by itself. Artifact set data is retained as `StructuredData`, not a normalized set-bonus model. |
| Build and stat diagnostics | `characterBuildValidation`, `characterStatReconciliation`, `characterStatClassification`, and existing analyzers | Existing deterministic validation errors/warnings, stat mismatches, classification unknowns/conflicts | These are account diagnostics. They must not automatically become investment priority or imply meta weakness. |
| Account data quality | `DataValue`, `sections`, `sectionCoverage`, `sectionFreshness`, collection integrity | Available/stale/missing/unavailable/failed state, retrieval times, freshness and coverage issues | Any derived candidate input must retain the relevant source state and timestamp. Unknown data cannot become zero, false, or a negative character rating. |
| Combat-mode source payloads | `spiralAbyssCurrent`, `spiralAbyssPrevious`, `imaginariumTheater` as `DataValue<StructuredData>` | Presence/state of the section and access to retained structured payload for future schema-specific analysis | These payloads are not normalized into validated team compositions, rotations, encounter-level character performance, or comparable usage observations. Do not infer team synergy or performance until a versioned parser and tests establish the exact fields and semantics. |

#### Inputs requiring curated game knowledge or additional account data

| Investment factor | Required source | Why the current snapshot is insufficient |
|---|---|---|
| Character roles and capabilities | Versioned, reviewed character/role/capability knowledge | Element, weapon type, rarity, and stats do not establish a complete combat role, application pattern, defensive utility, or capability. |
| Team synergy and team enablement | Reviewed character interaction, team, rotation, and encounter claims, plus a reliable owned-roster match | The current snapshot has no normalized team-composition or rotation model. Opaque Abyss/Theater payloads are not yet validated for this use. |
| Role/capability gaps | Role/capability taxonomy and supported coverage rules, applied to the owned roster | A roster list alone cannot determine whether an account lacks a role or whether a character fills that gap. |
| Current patch and mode relevance | Versioned knowledge with explicit game version, mode, encounter, validity, and source provenance | Account snapshot does not provide validated meta relevance or source-backed patch conclusions. |
| Redundancy and alternatives | Comparable role/capability claims for owned characters plus their observed investment state | Ownership and level can be read, but functional overlap and relative effectiveness require curated capability evidence. |
| Build suitability and target investment | Reviewed build guidance tied to character, mode, patch, weapon/artifact context | Current build validation checks data integrity and stat arithmetic; it is not a build guide or a damage-performance evaluator. |
| Investment cost | Versioned game-reference data for character/weapon/talent/artifact upgrade requirements | Current snapshot contains current levels and equipment but no authoritative material-cost model. |
| Account-specific affordability | Actual user inventory and resource balances, retrieved with clear freshness semantics | Current account model does not contain the full ascension/talent/artifact material inventory needed to calculate affordability. Daily Note resin is not a substitute for material inventory. |
| Already-sufficient judgment | Explicit target criteria for a stated account objective/mode, supported by knowledge, compared with observed investment | High level, high rarity, or constellation count alone cannot establish sufficiency. |
| Pull recommendations | Banner availability, unowned character knowledge, account role gaps, and pull constraints/preferences | Current roster is an owned-character snapshot, not a banner, pity, guarantee, or user pull-preference model. Pull advice is outside the first owned-character shortlist input mapper. |

#### M47 mapping rules

1. The first account-input mapper may emit only directly observed facts from the current snapshot: owned character identity, known element/rarity, observed character/weapon/talent/artifact investment, equipment state, and provenance/freshness.
2. Each fact must preserve its source state and applicable retrieval timestamp. A stale roster or stale equipment component remains stale in the derived input.
3. A missing, unavailable, failed, malformed, or unknown field remains an explicit unknown/unavailable state. It is never defaulted to zero, false, empty-success, or a negative assessment.
4. Existing build/stat/equipment findings remain account diagnostics. They may be linked as evidence context but cannot alone create an investment-priority recommendation.
5. Curated knowledge must supply role/capability, team synergy, patch/mode relevance, and build suitability. Knowledge claims must match the character subject and be verified, supported, permitted, and within their validity window before use.
6. Team synergy cannot be inferred from element combinations, weapon types, or co-occurrence in an unvalidated raw mode payload.
7. Usage observations remain contextual evidence only and must retain the M45 denominator, sample, mode, encounter, ownership, methodology, and period requirements. They cannot establish character strength or independently qualify investment priority.
8. Redundancy requires comparable role/capability evidence for the candidate and the owned alternatives. A second character of the same element or weapon type is not automatically redundant.
9. Investment cost and affordability remain unknown until game-reference costs and current player material inventory are both available. Do not estimate affordability from Resin alone.
10. The initial shortlist covers owned characters only. Pull evaluation, unowned-character opportunity analysis, banner state, pity, and pull preferences require a separate later input contract.

**M47 conclusion:** the current snapshot can describe what the player owns and how those characters are currently invested. It cannot, by itself, determine what those characters do for a team, which role the account lacks, whether a candidate is meta-relevant, or whether an upgrade is worth its resource cost. The next implementation should extract observed account facts without manufacturing those missing conclusions, then join them to separately verified game knowledge.


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

### 3.17 M50 deterministic owned-roster role and capability coverage

M50 aggregates the M49 validated per-character role/capability evidence across the M48 observed owned roster. It is a domain assessment contract only; it does not generate investment recommendations or infer team synergy.

#### Inputs and context

\`RoleCapabilityCoverageAssessor\` consumes only \`OwnedCharacterCapabilityKnowledge\`, which already contains the M49 roster join, trusted evidence, requested \`CharacterCapabilityQueryContext\`, source-record provenance, rejected claims, and roster availability/partiality. It does not accept raw claims or bypass M49 trust validation.

The assessment preserves evaluation time, requested game version/patch/mode/difficulty/encounter context, roster state, roster partiality, and all rejected-claim issues. Role and capability outputs retain supporting character IDs, conflicted character IDs, explicitly contradicted character IDs, supporting record IDs, and contradicting record IDs.

#### Aggregate states

Each role and capability receives one of these states:

- \`COVERED\`: at least one unambiguous owned character has accepted affirmative evidence.
- \`COVERED_WITH_CONFLICTS\`: at least one owned character supports the role/capability and at least one owned character has conflicting evidence for it.
- \`CONFLICTED\`: conflicting evidence exists, but no unambiguous owned character has affirmative-only evidence.
- \`NOT_ESTABLISHED\`: no affirmative evidence was found in the currently joined roster. This is not proof that the account lacks the role/capability and is not an investment gap.
- \`INCOMPLETE\`: the roster is stale or partial and no affirmative evidence establishes coverage.
- \`UNKNOWN\`: the roster is missing, unavailable, or failed, so account coverage cannot be assessed.

A positively supported role/capability remains COVERED even when the roster is stale/partial, but the assessment retains the stale/partial roster qualification. Ownership-ambiguous duplicate profiles are excluded from positive and negative aggregation and make the roster coverage partial.

#### Deterministic boundaries

1. Only M49 accepted, context-applicable, verified role/capability evidence contributes.
2. A positive assertion from any unambiguous owned character establishes coverage; one character's refutation does not cancel another character's affirmative evidence.
3. Contradictions remain visible with source record IDs; they are never silently resolved by source ordering or confidence ranking.
4. No assertion and refutation-only evidence never become a proven role/capability absence.
5. Stale, partial, duplicate, missing, unavailable, and failed roster states are not converted into complete coverage.
6. \`NOT_ESTABLISHED\` does not measure completeness of the external knowledge catalogue. The current model has no authoritative per-character knowledge-coverage manifest, so absence of claims must remain neutral.
7. The assessment does not infer team synergy, rotations, elemental reaction quality, performance, role gaps, character investment value, redundancy, material cost, affordability, or pull value.
8. No numerical score, weights, AI ranking, collectors, persistence, or UI are introduced.

M50 is intended as a reusable evidence layer for a future lineup builder. A future simulator must separately validate team-level synergy and combat interactions; roster role coverage alone is not proof that four characters form an effective team.


### 3.18 M51 versioned upgrade-cost knowledge and source permission contract

M51 establishes a provenance-bound contract for future character, talent, and weapon material calculations. It does not implement calculator arithmetic, inventory comparison, farming schedules, or a materials UI.

#### Cost record shape

`UpgradeCostRecord` is an immutable, versioned cost segment identified by record ID and positive revision. It binds:
- A subject type and stable subject/catalog ID (character or weapon).
- An explicit upgrade track: character level, character ascension, talent level, weapon level, or weapon ascension.
- A positive source level and strictly higher destination level.
- An explicit talent slot for talent-level records, and no talent slot for other tracks.
- An exact game version, a non-empty list of material item IDs and positive quantities, source references, retrieval time, optional validity window, review state, and optional superseded record ID.

One record represents one subject/track/range segment. Cumulative requirements are a later deterministic aggregation of non-overlapping, complete segments. The data contract does not assume every level range is available or that a missing range costs zero.

#### Trusted-use gates

`UpgradeCostRecordValidator` requires:
1. Supported schema version, nonblank stable IDs, positive revision, subject/track consistency, and a strictly increasing level range.
2. Explicit talent slot binding only for talent upgrades.
3. Non-empty game version and material list; each item ID is nonblank and each quantity is a positive integer. Duplicate item IDs in one record are rejected rather than silently summed.
4. At least one absolute HTTPS source reference whose host matches its source registry entry.
5. Every referenced source is `APPROVED` and explicitly permits `material_cost_curation` in `allowedActions`. Link-only or discovery permission is not permission to curate or redistribute structured cost facts.
6. The record is `VERIFIED` and has review timestamp and notes. Candidate, rejected, expired-review, or superseded records cannot be used as trusted costs.
7. Valid timestamp ordering and no self-supersession.

A record is applicable only when the requested game version exactly matches and the evaluation time falls within its optional validity window. No wildcard patch compatibility or silent latest-version fallback is inferred. Missing, malformed, stale, unapproved, or unverified costs remain unavailable and must never be converted to zero.

#### Source validation decision (M51)

- **HoYoverse Help Center / HoYoWiki:** official material-farming guidance confirms relevant categories and farming schedules, while HoYoLAB's official tools overview documents the in-app Enhancement Progression Calculator and HoYoWiki material calendar. These are useful canonical references for manual cross-checking, but no public structured cost API or third-party redistribution permission was established in this review. Reference-only pending permission/API verification.
- **genshin-db:** technically promising structured data for characters, weapons, materials, and related game data. Its repository advertises MIT licensing for the package and states that its data is sourced from both the Genshin Impact Fandom Wiki and GenshinData. The package license alone does not establish rights to redistribute all underlying game-derived or upstream data. Fandom text is generally CC BY-SA unless otherwise specified, while non-text media has separate licensing. GenshinData's README requests credit but does not provide a clear dataset license. Therefore it remains a technical candidate, not an approved bundled/imported source.
- **HoyoWhatpeon planner:** demonstrates a working planner using a pinned genshin-db snapshot and explicitly documents excluded cost categories and unsupported cases. It is a useful comparison/reference for coverage and missing-data handling, not an authorized source of reusable cost data.
- **Community calculators and other databases:** may be used for cross-checking only after their own provenance, source licenses, update cadence, and reuse permissions are reviewed. Do not scrape or copy their databases by default.

M51 approves no external source for automated bulk ingestion or bundled cost-data redistribution. A source can become trusted only after its specific data rights, attribution, permitted access method, retention/deletion obligations, technical stability, and explicit `material_cost_curation` action are documented in the source registry. A permissive license on a software wrapper does not automatically license upstream data or official game assets.

#### Calculator boundary

Future calculator work may calculate only from complete, compatible, trusted cost segments. It must:
- Keep character leveling, character ascension, each talent track, weapon leveling, and weapon ascension as distinct tracks.
- Preserve separate material item IDs and integer quantities.
- Distinguish required totals from player-owned inventory and remaining-to-farm values. M48 account character facts do not include the player's material inventory.
- Keep EXP books, enhancement ore, Mora, ascension materials, talent books, weekly boss drops, crowns, and other resource families separate.
- Treat artifact enhancement as a separately scoped track; M51 does not define artifact EXP/feeding costs.
- Avoid resin/farming-time estimates until domain schedules, drop-yield assumptions, world/domain level, discounts, and inventory are modeled with provenance.
- Never calculate across different game versions or fill missing segments with zero.

M51 adds only domain models, validation, tests, and source-policy documentation. No external collector, bundled dataset, persistence, UI, arithmetic engine, AI, or backend is introduced.


**End of specification.**


### 3.16 M49 character role/capability knowledge and owned-roster join

M49 adds a typed domain contract between reviewed character knowledge and the characters actually observed in an account roster. It does not change the M46 investment evaluator or create recommendations.

#### Typed capability assertion

`CharacterCapabilityKnowledgeClaim` is a versioned typed interpretation of one canonical `GameKnowledgeRecord`. It contains:
- A schema version, unique claim ID, positive character ID, and referenced source knowledge-record ID.
- An explicit assertion polarity: AFFIRMS or REFUTES.
- Zero or more typed combat roles and zero or more typed capabilities; at least one must be present.
- Optional human-readable conditions that qualify the assertion.

The initial role vocabulary is on-field damage dealer, off-field damage dealer, support, enabler, driver, healer, shielder, buffer, debuffer, battery, and crowd control.

The initial capability vocabulary covers on-/off-field damage, elemental application, reaction triggering/enabling, healing, shielding, damage/defense buffing, resistance reduction, energy generation, crowd control/grouping, interruption resistance, damage mitigation, and elemental infusion. These are explicit curated assertions, not deductions from element, weapon type, rarity, stats, or observed usage.

The referenced `GameKnowledgeRecord` remains authoritative for publisher/source references, permission, evidence class, review state, confidence, timestamps, validity window, game version, patch phase, mode, difficulty, and encounter. The typed payload does not duplicate or override those fields.

#### Trust and applicability gates

A typed claim is eligible for joining only when:
1. Its schema version is supported, claim ID is nonblank and unique, character ID is positive, and at least one typed role/capability is explicitly asserted.
2. Its source record exists exactly once, has subject type CHARACTER, subject ID equal to the typed character ID, and claim type ROLE_CAPABILITY.
3. The source record is VERIFIED and SUPPORTED, passes the existing source-registry and structural validator, has approved source permissions, explicitly permits the `manual_claim_curation` action for every referenced source, and is within its validity window at evaluation time.
4. Observed-usage evidence is not used to establish a role or capability.
5. Every requested context dimension (game version, patch phase, game mode, difficulty, encounter) exactly matches a populated source-record context field. A missing context field is not a wildcard when that dimension is requested.

AFFIRMS and REFUTES assertions are retained separately. If trusted assertions support and refute the same role/capability, the joined state is CONFLICTED. A refutation without an affirmative assertion is CONTRADICTED. No assertion is not proof that a role/capability is absent.

#### Owned-roster join boundary

`OwnedCharacterCapabilityJoiner`:
- Emits profiles only for character IDs present in an AVAILABLE or STALE M48 roster.
- Preserves the roster's AVAILABLE/STALE state and partial-coverage flag.
- Does not create profiles for characters absent from the retrieved roster. In particular, an incomplete roster cannot prove that a character is unowned.
- Does not join role/capability claims to duplicate character IDs; those profiles are marked ownership-ambiguous.
- Retains source record IDs for each supporting and contradicting assertion and returns rejected-claim issues for inspection.
- Does not interpret equipment investment, role coverage gaps, team synergy, patch strength, redundancy, material cost, affordability, or pull value.

A profile with no accepted role/capability assertions has no role/capability evidence. This is UNKNOWN, not a negative role assessment.

#### Explicit exclusions

M49 is a domain contract and deterministic join only. It adds no platform adapters, automatic collection, copied third-party guide content, source ingestion, arbitrary numerical weighting, recommendation scoring, AI ranking, persistence, or UI. Source permissions and manual review remain mandatory under the M44/M45 source policy.


### 3.19 M52 deterministic upgrade-cost aggregation and manual inventory

M52 consumes M51 trusted cost records and produces deterministic requirements for one explicitly requested subject, upgrade track, level range, and exact game version. It does not add a built-in external cost dataset or infer missing costs.

#### Calculation request and segment coverage

A calculation request identifies subject type and stable subject ID, track, from/to level, exact game version, and (for talent upgrades) an explicit talent slot. The calculator:
- Selects only records matching the complete request identity and exact game version.
- Revalidates source registry permission, review state, source references, and validity window at the evaluation time.
- Requires a contiguous, non-overlapping sequence of cost segments beginning exactly at the requested starting level and ending exactly at the requested target level.
- Aggregates quantities by stable material item ID using checked integer addition.
- Preserves the IDs of all cost records used in the result.

A missing segment produces an incomplete result with the first uncovered level. An overlapping or duplicate revision segment is rejected rather than arbitrarily selected. Unverified, expired, malformed, or permission-ineligible matching records block the calculation. A different game version is not a fallback. Missing cost data is never interpreted as zero. Integer overflow produces an explicit failure.

#### Manual inventory input

Actual material inventory is not available from the current account retrieval model. M52 therefore supports user-entered inventory quantities:
- Each entry binds one stable material item ID to a non-negative integer quantity.
- An explicit quantity of zero means the user entered zero.
- An absent item entry means inventory was not entered for that item; it is not zero.
- Duplicate item IDs, blank IDs, and negative quantities are invalid.
- Invalid inventory input does not corrupt or suppress the verified required-cost total, but owned and remaining quantities are withheld until the inventory input is corrected.
- For a valid manually entered quantity, remaining quantity is max(required - owned, 0).
- If inventory is absent or an individual material was not entered, remaining quantity is unknown, not the full requirement.
- Inventory is labeled as manually entered and timestamped. It is not represented as account-retrieved, synced, or independently verified.

Manual inventory entry is a calculation input only in this milestone. Persistence, inventory editing UI, and account synchronization are not included.

#### Scope and exclusions

M52 implements domain calculation and tests only. It does not implement:
- A character/weapon selection screen or Materials Calculator UI.
- Persistent storage or synchronization of manual inventory.
- A bundled material-cost dataset or automatic source collector.
- Artifact enhancement/feeding costs.
- Domain schedules, drop yields, resin requirements, farming-time estimates, or daily farming plans.
- AI-generated quantities or AI authority over numerical results.

A future UI must expose incomplete/invalid cost-data states and must never present unknown remaining quantities as zero.



### 3.20 M53 manual Materials Planner UI

M53 adds a usable manual planning workflow while no redistribution-approved production material-cost catalogue is connected.

- The player can select a character from the retrieved roster or a weapon observed as equipped on a retrieved character. A custom target is also supported.
- Current and target levels are editable. Retrieved current levels are prefilled when available; unavailable levels remain blank.
- The player manually enters material names, total required quantities, and owned quantities.
- Remaining quantity is calculated only when both required and owned quantities are valid non-negative whole numbers: max(required - owned, 0).
- An explicit owned quantity of zero is different from a blank owned quantity. Blank means inventory is not entered and remaining quantity stays unknown.
- Manual material rows are stored under target-specific local preference keys so changing the selected character or weapon does not mix separate plans. The selected target and level range are also stored locally. Plans are not synchronized with HoYoLAB or a remote service.
- The UI labels manually entered requirements as unverified. It must not imply that the quantities came from HoYoverse or a trusted game-knowledge record.
- The M52 verified-cost engine remains the future auto-population path. It is not wired to the UI until an approved, in-date, version-matched cost catalogue is available.
- This milestone does not add inventory synchronization, automatic material costs, farming schedules, domain drop yields, Resin estimates, or AI-generated quantities.
