# Irminsul Codex — UI Design & Anti-Slop Guidelines

> **Target Audience:** AI coding agents (Codex) and contributors building Flutter UI for **Irminsul Codex**.  
> **Design Philosophy:** **90% clean, modern Android utility / 10% subtle Genshin archival flavor.**  
> Prioritize scannable data, native Android ergonomics, and strict token consistency over decorative game-menu chrome.

---

## 1. Foundations: Theme Parity & Edge-to-Edge

Irminsul Codex supports **Light, Dark, and System** appearances and runs in full **edge-to-edge** mode. Every new screen and refactor must preserve both.

* **Zero hardcoded hex colors in feature widgets.** Always read from `Theme.of(context).colorScheme`, `Theme.of(context).textTheme`, or the app's custom `CodexThemeExtension`.
* **No deprecated Material 3 surface roles.** Use the tonal surface ladder (`surface` → `surfaceContainerLow` → `surfaceContainer` → `surfaceContainerHigh`). Do not use `background` or `surfaceVariant`.
* **Edge-to-edge scroll clearance:** Every scrollable view (`ListView`, `CustomScrollView`, `SingleChildScrollView`) must include bottom padding equal to `MediaQuery.paddingOf(context).bottom + 16` (plus bottom navigation bar height when inside the main shell) so content never gets trapped behind the Android gesture pill.
* **WebView & Modal Chrome:** Set `statusBarColor: Colors.transparent` and match `AppBar.systemOverlayStyle` to the bar's brightness to prevent grey status-bar scrim sandwiches. For dark third-party pages (e.g., HoYoLAB login), use a dark tonal `AppBar` with a `2dp` Archival Gold `LinearProgressIndicator` along its bottom edge while loading.

---

## 2. Color System: Archival Gold & The 10% Elemental Budget

Eliminate mixed default accents (no Material lavender/purple pills clashing with teal buttons).

### Primary Signature Accent — Archival Gold
| Token Role | Dark Mode | Light Mode | Usage |
| :--- | :--- | :--- | :--- |
| **Primary Accent** | `#D3BC8E` (Soft Parchment Gold) | `#8E6E35` (Burnished Bronze) | Active nav indicator, primary CTA (`Sync`, `Done`), selected chips, active focus rings, `✦` motifs. |
| **Primary Container** | `#D3BC8E` at `16%` opacity | `#8E6E35` at `14%` opacity | Active navigation pill background, selected character card tint. |
| **On Primary** | `#1A1814` | `#FFFFFF` | Text/icons placed directly on solid primary buttons (e.g., center AI FAB). |

### Neutral Surface Ladder
* **Dark Mode:** Deep, quiet charcoal/slate tones (avoid pure `#000000` canvas for cards; rely on tonal steps rather than drop shadows or heavy borders).
* **Light Mode:** Warm alabaster/off-white canvas (`#F7F6F2`–`#FAF9F6`) with crisp white (`#FFFFFF`) or subtle warm-grey containers so Burnished Bronze reads naturally.

### Elemental & Rarity Colors (`CodexThemeExtension`)
* Store all 7 Genshin elements (Pyro, Hydro, Anemo, Electro, Dendro, Cryo, Geo) and 4★/5★ rarity tones in a custom `ThemeExtension` with **separate Light and Dark palettes** (bright Cryo, Geo, and Anemo tones must be darkened/saturated in Light Mode to maintain contrast).
* **The 10% Elemental Budget:** Keep Home, Rosters, Planners, and Settings **100% neutral**. Restrict elemental tinting strictly to character detail views—specifically a faint `6–10%` opacity ambient wash behind the character header, the element badge, and active detail tabs.

### White-on-Transparent Game Assets (Mandatory Backing)
* HoYoLAB Talent and Constellation icons are white-on-transparent PNGs that disappear in Light Mode.
* Always render white-glyph game assets inside a dedicated **high-contrast dark tonal backing** (e.g., `#26282E` squircle or circle) regardless of Light or Dark mode.

---

## 3. Typography & Data-First Hierarchy

* **Primary UI Font:** All-sans-serif using **Plus Jakarta Sans** (or **Inter**) across the entire interface. Do not introduce decorative fantasy serifs.
* **Monospace Font:** Reserve **JetBrains Mono** strictly for **UIDs, copyable share codes, and raw HoYoLAB JSON dumps**. Never use monospace for general labels, descriptions, or buttons.
* **Mandatory Tabular Numerals:** Apply `fontFeatures: const [FontFeature.tabularFigures()]` to **all numeric displays**—Resin counts, timers, CRIT Rate/DMG, ATK/HP/DEF, artifact substats, Abyss clears, and level badges—so columns align vertically and do not jitter on updates.
* **No ad-hoc `fontSize`:** Read from `Theme.of(context).textTheme` (`titleLarge`, `titleMedium`, `bodyMedium`, `labelMedium`, etc.) to maintain hierarchy.

---

## 4. Iconography System

Maintain a strict boundary between **App Shell UI Icons** and **Genshin Domain Assets**.

### App Chrome & Actions (`lucide_icons_flutter`)
* Standardize all UI icons (navigation, search, filter, sync, copy, add, remove, settings) on **`lucide_icons_flutter`**.
* **Ban direct `Icons.*` calls** inside feature widgets. Centralize all UI icons in a single `AppIcons` registry (`lib/core/theme/app_icons.dart`) with locked size tiers: `sm = 16`, `md = 20`, `lg = 24`.
* Use accurate metaphors (e.g., `LucideIcons.house` for Home, not a door icon).
* **Active Tab States:** Never mix filled Material icons into a Lucide bar. Indicate selection via the Archival Gold `primaryContainer` pill, Archival Gold icon tint, or a heavier Lucide stroke weight.

### Genshin Domain Assets (`flutter_svg` + Cached JSON Images)
* Dynamic game art (character portraits, weapons, artifacts) comes from Battle Chronicle JSON URLs (cached locally).
* Static domain icons (elemental crests, weapon silhouettes, artifact slot icons, subtle `✦` 4-point star motif) must be flat, **monochrome SVGs** bundled in `assets/icons/` and tinted via `ColorFilter.mode`. **Never draw Genshin emblems with `CustomPainter`.**
* **Optical Weight Balance:** Keep outlined Lucide icons and solid Genshin crests in separate visual lanes. When a solid Genshin icon shares a row with a Lucide icon, reduce the solid icon by `2dp` so both carry equal visual weight.

---

## 5. Layout, Geometry & Navigation Shell

### Spacing & Surface Hierarchy
* Follow a strict **4dp / 8dp spacing scale** (`4, 8, 12, 16, 20, 24, 32`).
* **Group with spacing, not nested boxes:** Avoid wrapping bordered `Card` widgets inside other `Card` widgets. Use `12–16dp` spacing inside a single `SurfaceGroup` and `20–24dp` between distinct sections.

### Concentric Radii (Mandatory Math)
* Enforce **`innerRadius = outerRadius - padding`** on all nested containers.
  * Example: A `SurfaceGroup` with `BorderRadius.circular(20)` and `EdgeInsets.all(12)` padding must give its inner character portrait or weapon box `BorderRadius.circular(8)`.
* **Consistent Form Controls:** Dropdowns and text inputs inside the same card must share the same corner radius (e.g., `12dp`) and subtle tonal fill/outline—never mix pill-shaped dropdowns with sharp rectangular outlined text fields.

### Bottom Navigation Bar (5-Slot Symmetric Shell)
* Do not place a floating center AI button on top of a standard 3-item `NavigationBar` (causes overlap collisions).
* Build the main shell with a `BottomAppBar` or custom **5-slot symmetric `Row`**:  
  `[Tab 1 (Home), Tab 2 (Visuals), Center AI Spacer, Tab 3 (Future Slot), Tab 4 (Account)]`
* Keep the prominent circular **Irminsul AI** action cleanly centered in the middle slot with generous horizontal clearance.
* **No duplicate header navigation:** When `Account` lives in the bottom navigation bar, remove the duplicate `Account` button from the top-right app bar; reserve the top-right slot for a quiet utility action (like `Sync` or `Settings`).

---

## 6. Core Primitive Widgets (Compose, Don't Reinvent)

Feature screens must compose from these shared primitives in `lib/core/widgets/` instead of styling containers from scratch:

1. **`SurfaceGroup`**: Standard M3 `surfaceContainer` wrapper with standardized padding, `16dp`/`20dp` outer radius, and an `innerRadius` getter for concentric child widgets.
2. **`StatRow` & `MetricTile`**: Standardized key-value displays with muted `onSurfaceVariant` labels, high-contrast `tabularFigures` values, and zero decorative pill tags.
3. **`SectionHeader`**: Single-line title with an optional muted `✦` 4-point star prefix and an optional quiet trailing action (`View all`, `+ Add`). Never duplicate a collapsible header's title inside its expanded body.
4. **`AssetGlyphBadge`**: Standardized dark-backed squircle/circle container for white-on-transparent HoYoLAB PNGs (Talents, Constellations).
5. **`ElementAccentChip`**: Low-chroma, compact indicator (monochrome element SVG + label) used only for filtering or element identity.
6. **`CodexSheet`**: Shared edge-to-edge modal bottom sheet wrapper with a centered drag handle, tonal surface, `24dp` top corner radius, and automatic bottom gesture-bar inset padding.
7. **`SkeletonBox`**: Layout-matched tonal shimmer/pulse placeholder for HoYoLAB Chronicle syncs and loading states.

---

## 7. Copywriting, Density & Screen-Specific Rules

* **Eliminate robotic "disclaimer slop":** Keep in-screen helper text to **one short, scannable sentence**. Move technical caveats (e.g., manual planning disclaimers, unverified synergy notes, or AI provider status explanations) behind a compact `LucideIcons.info` tooltip or collapsible note.
* **No redundant data cards:** Do not repeat UID and server in a top Home card when they already appear in the Home header.
* **Compact Artifact Inspection:** Never dump all 5 artifacts and all 20+ substat lines as one vertical wall of text. Make the 5-icon artifact row interactive—tapping an artifact updates a compact inline stat block or opens a `CodexSheet`.
* **Action Hierarchy:** One clear primary action per screen/sheet in Archival Gold (`Sync`, `Done`); secondary actions (`Cancel`, `Remove`, `Close`) remain quiet (`onSurfaceVariant`).

---

## 8. States, Motion & Feedback

* **Loading States:** Use `SkeletonBox` layouts matching the target content. Ban bare centered `CircularProgressIndicator` spinners on blank screens.
* **Empty & Expired Session States:** Provide a concise 1-line explanation + a single Archival Gold primary action (e.g., `Reconnect HoYoLAB`).
* **Motion & Haptics:** Use snappy, Android-native curves (`Curves.easeOutCubic`, `150–250ms` durations) with subtle haptics (`HapticFeedback.selectionClick` on tab/artifact selection, `HapticFeedback.lightImpact` on sync/UID copy). Avoid bouncy or floaty animations.

---

## 9. Explicit Banned Patterns ("Do Not Do")

- [ ] **No raw hex colors** inside feature widgets.
- [ ] **No `Icons.*` calls** in feature widgets (use `AppIcons` backed by `lucide_icons_flutter`).
- [ ] **No `CustomPainter` icons** for Genshin elements or UI chrome.
- [ ] **No filler badges or non-interactive pills** (e.g., `"SYNCED"`, `"INFO"`, `"STATS"`).
- [ ] **No nested bordered cards** inside cards ("Card-itis").
- [ ] **No mismatched inner/outer border radii** (always subtract padding).
- [ ] **No white Talent/Constellation PNGs** placed directly on light surfaces.
- [ ] **No full-saturation elemental gradients** on list items or dashboard cards.
- [ ] **No multi-paragraph AI disclaimers** cluttering forms or empty states.
- [ ] **No runtime DOM style scraping** from HoYoLAB WebViews (static UI tokens/SVGs are curated and bundled once in `assets/`).

---

## 10. Asset & Token Sourcing (Debug-Only WebView Extractor)

Mirroring the app's existing Battle Chronicle JSON extraction, Irminsul Codex may include an in-app **developer utility** inside the authenticated HoYoLAB WebView to dump static UI assets and reference tokens.

* **Strictly `kDebugMode` Only:** Any UI/DOM extraction trigger (button, menu action, or JS runner via `runJavaScriptReturningResult` / `evaluateJavascript`) **must** be guarded behind `if (kDebugMode)` so it never appears or executes in profile/release builds.
* **What the Extractor Captures:**
  * **Inline SVGs:** `<svg>` markup for subtle archival motifs (e.g., the `✦` 4-point star) and static UI glyphs.
  * **Static Asset URLs:** `<img>` sources and CSS `background-image` URLs for static icons not returned in Battle Chronicle JSON (e.g., Original Resin icon, Spiral Abyss / Imaginarium Theater / Stygian Onslaught emblems, elemental crests).
  * **Reference Tokens:** Computed colors and font stacks exported as a single copyable/shareable JSON dump for offline Light/Dark token curation.
* **Curate & Bundle Once:** Extracted SVGs and static icons are reviewed manually, cleaned up into monochrome assets where appropriate, and committed under `assets/icons/`.
* **Zero Runtime DOM Styling:** Never parse or apply scraped HoYoLAB HTML/CSS at runtime to style Flutter screens, and never import HoYoLAB's mismatched web gradients or parchment blocks wholesale. Dynamic game art (characters, weapons, artifacts) must always come from the Battle Chronicle JSON URLs.

---

## 11. Verification, Flutter Analyzer & Blind-Coding Workflow

Because AI coding agents cannot visually inspect a live device screen, every UI change must pass automated static analysis and headless layout checks before being considered complete.

### Mandatory Commands After Every UI Edit
1. **Static Analysis (`flutter analyze`):**
   * Run `flutter analyze` on the project (or modified paths) after every refactor.
   * **Zero errors and zero new warnings/infos** are allowed. Fix all deprecated Material 3 API warnings (such as `background`, `surfaceVariant`, or deprecated `withOpacity` calls—prefer `.withValues(alpha: ...)` on newer Flutter SDKs).
   * Do not silence warnings with `// ignore:` comments unless strictly justified.
2. **Headless Widget Tests (`flutter test`):**
   * Run `flutter test` after every screen or primitive widget edit to catch `RenderFlex overflowed`, unbounded height/width constraints, and missing `Theme` or `CodexThemeExtension` lookups.
   * For every new or refactored screen, include or update a smoke widget test that pumps the widget in **both Light Mode and Dark Mode** at a narrow mobile viewport (e.g., `360x800`) and asserts zero layout exceptions.

### Self-Audit Checklist Before Finishing a Diff
Before returning a completed task, run a quick text/grep check over modified feature files to verify:
* No `Icons.` references inside `lib/features/` (must route through `AppIcons`).
* No `Color(0x` literals inside `lib/features/` (must live only in theme token files).
* Every numeric `StatRow` / `MetricTile` / counter applies `FontFeature.tabularFigures()`.
* Every scrollable screen accounts for `MediaQuery.paddingOf(context).bottom`.

### Scoped Execution Rule
* Refactor **one concern per pass** (e.g., Pass 1: Theme tokens & `AppIcons`; Pass 2: 5-slot `BottomAppBar`; Pass 3: Home screen; Pass 4: Character Showcase). Avoid sweeping multi-screen rewrites in a single diff so visual verification on a real device stays fast and easy to debug.