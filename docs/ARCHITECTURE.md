# Irminsul Codex — Architecture & UI Blueprint

## 1. Project Overview & Non-Negotiable Rules
- **App Name:** Irminsul Codex
- **Package Name:** `com.irminsul.codex`
- **Repository:** `https://github.com/berao609/Irminsul-codex`
- **Build Environment:** Android (Kotlin + Jetpack Compose), JDK 17, Gradle 8.14.8, built via GitHub Actions (`gradle assembleDebug`).
- **Privacy & Shareability:**
  - `android:allowBackup="false"` must always remain set in `AndroidManifest.xml`.
  - All HoYoLAB cookies, UIDs, cached snapshots, and LLM API keys stay strictly in local app storage.
  - Never hardcode any personal UID, server, or API key in source code or build files. The Info screen must dynamically display whoever is currently logged in (or `"Not signed in"`).
- **Design Philosophy:** Fast, offline-tolerant, zero-bloat personal utility that combines **Ascent's** full-screen simplicity with **HoYoLAB Battle Chronicles'** visual craft.

---

## 2. Tech Stack
- **Language & UI:** Kotlin, Jetpack Compose, Material 3 (`enableEdgeToEdge()`).
- **Architecture:** Single-module **MVVM + Unidirectional Data Flow (UDF)**.
  - *Avoid multi-module Gradle setups*—they add CI build overhead without benefit for a solo project.
- **Navigation:** Single-Activity with Compose Navigation (`NavHost` + bottom navigation bar).
- **Concurrency & State:** Kotlin Coroutines (`Dispatchers.IO` for network/disk) + `StateFlow` / `SharedFlow`.
- **Networking:** `OkHttp` (reused for both HoYoLAB API calls and Cloud LLM Server-Sent Events streaming).
- **Local Storage:**
  - **Jetpack DataStore (Preferences)** for UID, HoYoLAB cookie, theme preference, and BYOK LLM settings (replacing `SharedPreferences`).
  - **Local JSON File Cache** (`filesDir/cache/`) for instant startup ("stale-while-revalidate") of Daily Notes and the latest full account export.
- **Serialization:** Migrate gradually from `org.json.JSONObject` to `kotlinx.serialization` (`@Serializable` models) for UI data, while keeping raw JSON export intact.
- **Dependency Injection:** Lightweight manual `AppContainer` (or Koin). *Avoid Hilt/KSP* to keep GitHub Actions compilation fast and lightweight.

---

## 3. Clean Package Structure
```text
app/src/main/java/com/irminsul/codex/
├── AppContainer.kt                 # Lightweight dependency wiring (no KSP/Hilt)
├── MainActivity.kt                 # Thin entry point (~25 lines: edge-to-edge + Theme + AppNav)
├── data/
│   ├── local/
│   │   ├── PreferencesStore.kt     # DataStore: UID, cookie, AI baseUrl/apiKey/model
│   │   └── SnapshotCache.kt        # Reads/writes cached DailyNote + last account JSON
│   ├── model/
│   │   ├── DailyNote.kt            # Typed models for real-time notes
│   │   ├── AccountSummary.kt       # Lightweight parsed overview (AR, days, achievements, exploration)
│   │   └── ChatMessage.kt          # Advisor conversation models
│   ├── remote/
│   │   ├── HoYoLabClient.kt        # Existing optimized HoYoLAB client (concurrent + bloat-stripped)
│   │   └── LlmClient.kt            # OpenAI-compatible SSE streaming client using OkHttp
│   └── repository/
│       ├── CodexRepository.kt      # Single source of truth (instant cache + HoYoLAB refresh)
│       └── AiRepository.kt         # ContextBuilder (JSON slicing) + streaming chat orchestration
└── ui/
    ├── theme/
    │   ├── Color.kt                # Battle Chronicles parchment/slate/gold tokens
    │   ├── Type.kt                 # Typography scale + tabular numeral helpers
    │   └── Theme.kt                # MaterialTheme wrapper (Light & Dark support)
    ├── components/
    │   ├── ChronicleCard.kt        # HoYoLAB-inspired parchment/elevated card container
    │   ├── SectionHeader.kt        # Four-point star (✦) title + optional "View More ▸"
    │   ├── ProgressBar.kt          # Clean rounded gold/teal progress tracks
    │   └── StatusPill.kt           # Compact rounded badges ("Lv. 60", "All Claimed", "No Backup")
    ├── navigation/
    │   └── AppNav.kt               # Bottom navigation bar + tab routing
    ├── screens/
    │   ├── HomeScreen.kt           # Ascent-style full-screen actions + compact account strip
    │   ├── NotesScreen.kt          # Battle Chronicles-inspired Daily Notes & Exploration preview
    │   ├── AdvisorScreen.kt        # Cloud LLM streaming chat + character/account context selector
    │   ├── InfoScreen.kt           # Ascent-style dev profile (berao609), repo link, version, BYOK AI config
    │   └── HoYoLabLoginDialog.kt   # Existing in-app WebView login
    └── viewmodel/
        ├── CodexViewModel.kt       # Exposes StateFlow<CodexUiState>
        └── AdvisorViewModel.kt     # Manages chat history, context slices, and SSE streaming
```

---

## 4. State Management & Smooth UX Rules
1. **Zero Business Logic in Composables:** Screens only collect `StateFlow` (`collectAsStateWithLifecycle()`) and forward user actions to `ViewModel` methods.
2. **Instant Startup (Stale-While-Revalidate):**
   - On launch, immediately emit the last cached `DailyNote` and `AccountSummary` from `SnapshotCache`.
   - Trigger a silent background refresh from HoYoLAB without replacing existing screen content with a full-screen spinner.
3. **Edge-to-Edge Discipline:** Apply `enableEdgeToEdge()` in `MainActivity` and respect `Scaffold` inner padding / `systemBarsPadding()` so status bars and gesture bars never clip content.
4. **No Layout Jitter:** Use tabular numerals (`TextStyle(fontFeatureSettings = "tnum")`) on all counters, UIDs, percentages, and resin recovery timers. Animate progress bar changes with `animateFloatAsState`.
5. **Kotlin Safety Guardrail:** Never destructure more than 5 items from a Kotlin `List` (Kotlin only generates `component1()`–`component5()`). Always index `results[0]`, `results[1]`, etc.

---

## 5. UI / UX Design System (Ascent × HoYoLAB Battle Chronicles)

### Layout Structure (Inherited from Ascent)
- **Full-screen, uncluttered screens** with generous vertical breathing room and a crisp **bottom navigation bar** (`Home`, `Notes`, `Advisor`, `Info`).
- **Home Tab:** Centered utility focus—clean connection status (`Connected` / `Not Signed In`), compact dynamic UID + server strip, primary stacked full-width action buttons (`Save Account JSON`, `Copy JSON`, `Sync Now`, `Sign in with HoYoLAB`), and inline progress feedback during extraction.
- **Notes Tab:** Battle Chronicles-style dashboard showing Real-Time Notes (Original Resin split layout, Commissions, Realm Currency / Jar of Riches, Weekly Discounts, Expeditions) and an optional compact World Exploration summary parsed from the cached snapshot.
- **Advisor Tab:** Clean chat screen with quick-prompt chips (e.g., *"Review my Abyss team"*, *"Weakest artifacts"*) and a context filter chip bar so users choose which characters/sections to attach.
- **Info Tab:** Vertically balanced Ascent-style info layout:
  - Minimalist avatar + **`berao609`**
  - Clickable source link: `https://github.com/berao609/Irminsul-codex`
  - App version (read dynamically from `PackageInfo` / `BuildConfig`)
  - Dynamic **Current Account** block (logged-in UID, server, Sign Out button)
  - **Local-only storage** indicator (`No Backup` pill)
  - Collapsible **AI Provider (BYOK)** settings (Base URL, API Key, Model ID)

### Visual Identity (Inherited from HoYoLAB Battle Chronicles)
- **Color Palette (Dual-Tone Contrast):**
  - *Dark Battle Chronicles Mode (Optional/Default Dark):*
    - Outer Canvas: `#1C202B` (Deep Slate)
    - Inner Parchment Card (signature HoYoLAB contrast): `#F5F0E6` with `#3B3F4C` ink, OR Dark Elevated Card `#252A38` with `#ECEEF2` ink and `#343B4D` `1.dp` hairline borders
    - Gold Accent / Progress Fill: `#D3BC8E` / `#C8A25A`
    - Secondary Muted Gold Surface: `#E9DFCD`
  - *Light Parchment Mode (Ascent-style Clean Light):*
    - Outer Canvas: `#F8F5F0` (Warm Ivory)
    - Card Surface: `#FFFFFF` or `#F2ECE1` with `#E4DCCD` `1.dp` borders
    - Primary Ink: `#2F3542`, Secondary Ink: `#6F7685`
    - Primary Action: `#3B4354` (Slate Navy), Secondary Action: `#E6D5B8` (Warm Sand), Accent Gold: `#C59A4E`, Connected Teal: `#3E9B8C`
- **Micro UI Signatures:**
  - **Section Headers:** Prefix major headers with a crisp four-point star (`✦ Real-Time Notes`, `✦ World Exploration`, `✦ Chronicle Export`) and subtle right-aligned action cues (`Refresh ▸`, `View More ▸`).
  - **Real-Time Notes Card Pattern:** Split card with a prominent Resin panel on the left (`80 / 200` + recovery countdown + sleek horizontal gold/teal progress bar) and neatly stacked right-aligned metric rows on the right (Expeditions, Realm Currency, Commissions + `"All Claimed"` pill).
  - **Exploration Bars:** Region name + Statue/Offering level + clean horizontal gold progress track (`#D3BC8E` fill over translucent dark/sand track).
  - **Typography & Weights:** Clean geometric sans-serif (`FontFamily.SansSerif`), `FontWeight.SemiBold` for headers and key numbers, `FontWeight.Medium` for labels and buttons, `FontWeight.Normal` for descriptions. No overly heavy black weights or cluttered text shadows.
  - **Symbols & Icons:** Minimalist, uniform-stroke vector glyphs (Lucide-inspired or custom Compose `Canvas` paths) matching HoYoLAB's restrained symbol language.

### Launcher Icon Direction (TBD)
- Pure XML **Android Adaptive Icon** (`mipmap-anydpi-v26` + vector foreground/background drawables).
- Smooth, rounded, timeless minimalism using 60-30-10 color theory.
- Motif must fit **"Irminsul Codex"** (e.g., Ley Line leaf, branching data nodes, open folio/bookmark seal, or "IC" monogram)—*do not use pyramid motifs*.

---

## 6. HoYoLAB Backend Contract (`HoYoLabClient.kt`)
Keep the existing battle-tested extraction logic intact when moving it to `data/remote/HoYoLabClient.kt`:
- **Concurrent Fetching:** Fetches Record Card, Profile/Exploration (`index`), Daily Note, Character List, Spiral Abyss (current & previous), and Imaginarium Theater in parallel via `coroutineScope` + `async` + indexed list access (`results[0..6]`).
- **Chunked Character Details:** Fetches full builds in batches of 8 (`chunked(8)`) with a `200ms` delay to respect rate limits.
- **Stat Resolution & Bloat Stripping:** Extracts `property_map` to inject human-readable `stat_name` next to raw `property_type` IDs, and strips icon/image URLs and wiki bloat.
- **Optional Future Bloat Trimming:** Adding `"_desc"`, `"_effect"` to `BLOAT_SUFFIXES` and `"field_ext_map"` to `BLOAT_KEYS` reduces exported JSON size further if needed for LLM token savings.

---

## 7. Cloud LLM Integration Architecture (`Advisor`)
1. **BYOK Configuration (OpenAI-Compatible Standard):**
   - Works out of the box with OpenRouter, OmniRoute, Gemini OpenAI-compatible endpoint, or any custom proxy.
   - Stored in `PreferencesStore`: `apiBaseUrl`, `apiKey`, `modelId`.
2. **Token-Efficient `ContextBuilder`:**
   - Full raw JSON exports are ~1.1 MB (~250k+ tokens) and must **never** be sent raw on every prompt.
   - Build a compact, LLM-optimized context string from `SnapshotCache`:
     - **Always include (~200 tokens):** AR, active days, achievements, Spiral Abyss floor, Imaginarium Theater act, and a 1-line list of owned characters with level/constellation.
     - **Conditionally include (user-selected or keyword-matched):** Detailed builds (weapon, refinement, artifact set bonuses, main/substats, talent levels, final stats) only for the 1–8 relevant characters, plus Abyss/Theater floor details or Exploration percentages when asked.
3. **Real-Time SSE Streaming (`LlmClient.kt`):**
   - POST to `${baseUrl}/chat/completions` with `"stream": true`.
   - Read `text/event-stream` lines incrementally via OkHttp `BufferedSource`, parse `choices[0].delta.content`, and emit chunks as a Kotlin `Flow<String>`.
   - Support immediate stream cancellation when the user taps Stop or leaves/resets chat.

---

## 8. Recommended Build Roadmap
- **Phase 1 — Clean Foundation:** Refactor current single-file `MainActivity.kt` into `AppContainer` + `PreferencesStore` (DataStore) + `SnapshotCache` + `CodexRepository` + `CodexViewModel`. Verify GitHub Actions build passes cleanly.
- **Phase 2 — Ascent × Battle Chronicles UI:** Build `Theme`, reusable Battle Chronicles components (`ChronicleCard`, `SectionHeader`, `ProgressBar`, `StatusPill`), and the `Home`, `Notes`, and `Info` screens with bottom navigation.
- **Phase 3 — Cloud LLM Advisor:** Add `LlmClient` (SSE streaming), `ContextBuilder` (smart JSON slicing), `AiRepository`, `AdvisorViewModel`, `AdvisorScreen`, and BYOK settings in `InfoScreen`.
- **Phase 4 — Brand & Release Polish:** Finalize the Irminsul Codex adaptive vector icon, add a signed release workflow, and optionally add a Glance home-screen Resin widget.
