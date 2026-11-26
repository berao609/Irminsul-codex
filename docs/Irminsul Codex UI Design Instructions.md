# Irminsul Codex — UI Design & Anti-Slop Guidelines

> **Target Audience:** AI coding agents (Codex) and contributors building the native Android UI for **Irminsul Codex**.
> **Platform:** Kotlin, Jetpack Compose, and Material 3. This is not a Flutter project.
> **Design Philosophy:** **90% clean, modern Android utility / 10% subtle Genshin archival flavor.**
> Prioritize scannable data, native Android ergonomics, and strict token consistency over decorative game-menu chrome.

---

## 1. Foundations: Theme Parity & Edge-to-Edge

Irminsul Codex supports **Light, Dark, and System** appearances and runs in full **edge-to-edge** mode. Every new screen and refactor must preserve both.

* **Theme-owned colors and typography:** Feature composables must read colors and text styles from `MaterialTheme.colorScheme`, `MaterialTheme.typography`, and shared app theme tokens. Do not place raw hex colors in feature composables. Keep light/dark variants together in the app theme.
* **Material 3 surface roles:** Prefer the tonal surface ladder (`surface`, `surfaceContainerLow`, `surfaceContainer`, `surfaceContainerHigh`) when those roles are available in the project's resolved Material 3 version. Check the actual dependency/API before using a newer role. Avoid introducing deprecated aliases; migrate existing uses only when the resolved API supports a safe replacement.
* **Edge-to-edge scroll clearance:** Use the insets already provided by `Scaffold` and Compose window-inset APIs. Scrollable content must account for navigation-bar/gesture insets and the app's bottom navigation area without double-applying padding. Prefer `WindowInsets.navigationBars` / `WindowInsets.safeDrawing` and the relevant `Scaffold` content padding over hardcoded device-specific offsets.
* **WebView & modal chrome:** Keep status/navigation bars transparent where the existing edge-to-edge setup requires it, and update system-bar icon contrast when the selected Light/Dark theme changes. Use native Compose `TopAppBar`, `ModalBottomSheet`, and Material 3 progress components for app UI. For dark third-party pages (e.g., HoYoLAB login), use a dark tonal container and a subtle 2dp Archival Gold loading indicator where appropriate.

---

## 2. Color System: Archival Gold & The 10% Elemental Budget

Eliminate mixed default accents (no Material lavender/purple pills clashing with the selected primary accent).

### Primary Signature Accent — Archival Gold
| Token Role | Dark Mode | Light Mode | Usage |
| :--- | :--- | :--- | :--- |
| **Primary Accent** | `#D3BC8E` (Soft Parchment Gold) | `#8E6E35` (Burnished Bronze) | Active nav indicator, primary CTA (`Sync`, `Done`), selected chips, active focus rings, `✦` motifs. |
| **Primary Container** | `#D3BC8E` at `16%` opacity | `#8E6E35` at `14%` opacity | Active navigation pill background, selected character card tint. |
| **On Primary** | `#1A1814` | `#FFFFFF` | Text/icons placed directly on solid primary buttons (e.g., center AI action). |

### Neutral Surface Ladder
* **Dark Mode:** Deep, quiet charcoal/slate tones (avoid pure `#000000` canvas for cards; rely on tonal steps rather than drop shadows or heavy borders).
* **Light Mode:** Warm alabaster/off-white canvas (`#F7F6F2`–`#FAF9F6`) with crisp white (`#FFFFFF`) or subtle warm-grey containers so Burnished Bronze reads naturally.

### Elemental & Rarity Colors (App Theme Tokens)
* Store all 7 Genshin elements (Pyro, Hydro, Anemo, Electro, Dendro, Cryo, Geo) and 4★/5★ rarity tones in shared app theme tokens, with **separate Light and Dark palettes** (bright Cryo, Geo, and Anemo tones must be darkened/saturated in Light Mode to maintain contrast).
* **The 10% Elemental Budget:** Keep Home, Rosters, Planners, and Settings **100% neutral**. Restrict elemental tinting strictly to character detail views—specifically a faint `6–10%` opacity ambient wash behind the character header, the element badge, and active detail tabs.

### White-on-Transparent Game Assets (Mandatory Backing)
* HoYoLAB Talent and Constellation icons may be white-on-transparent PNGs that disappear in Light Mode.
* Always render white-glyph game assets inside a dedicated **high-contrast dark tonal backing** (e.g., `#26282E` squircle or circle) regardless of Light or Dark mode.

---

## 3. Typography & Data-First Hierarchy

* **Primary UI Font:** All-sans-serif using **Plus Jakarta Sans** (or **Inter**) across the entire interface, if the font assets and licensing are available. Do not introduce decorative fantasy serifs.
* **Monospace Font:** Reserve **JetBrains Mono** strictly for **UIDs, copyable share codes, and raw HoYoLAB JSON dumps**. Never use monospace for general labels, descriptions, or buttons.
* **Mandatory Tabular Numerals:** Apply Compose `FontFeature.tabularFigures()` through `TextStyle` / `SpanStyle` font features to numeric displays—Resin counts, timers, CRIT Rate/DMG, ATK/HP/DEF, artifact substats, Abyss clears, and level badges—so columns align vertically and do not jitter on updates.
* **No ad-hoc typography:** Prefer `MaterialTheme.typography` and shared text styles over arbitrary `fontSize` values. A deliberate exception is acceptable for a one-off layout only when the design system has no suitable role; promote repeated exceptions into shared styles.

---

## 4. Iconography System

Maintain a strict boundary between **App Shell UI Icons** and **Genshin Domain Assets**.

### App Chrome & Actions
* Standardize UI icons (navigation, search, filter, sync, copy, add, remove, settings) through one shared `AppIcons` registry/wrapper.
* **Do not scatter direct icon-library calls** through feature composables. Centralize icon choice, content descriptions, and size tiers (`sm = 16dp`, `md = 20dp`, `lg = 24dp`).
* Use accurate metaphors (e.g., a house icon for Home, not a door icon).
* **Dependency boundary:** The current project includes `androidx.compose.material:material-icons-extended`. Do not assume `lucide_icons_flutter` or any Flutter package exists in this Android project. A Lucide Android/Compose library may be evaluated separately, but do not add a dependency without checking compatibility, maintenance, licensing, and bundle impact first.
* **Active Tab States:** Indicate selection through the Archival Gold selected indicator/container and icon tint/weight. Keep the icon family consistent across the navigation bar.

### Genshin Domain Assets
* Dynamic game art (character portraits, weapons, artifacts) comes from Battle Chronicle JSON URLs and the existing image-loading/cache path.
* Static domain icons (elemental crests, weapon silhouettes, artifact slot icons, subtle `✦` 4-point star motif) should be clean, monochrome assets stored in Android-supported resource locations. Prefer Android Vector Drawable XML for scalable static UI artwork. Use SVG only if a compatible renderer is already present or a dependency is separately reviewed and approved. Do not assume Flutter's `flutter_svg` package exists.
* **No custom-drawn substitutes for game marks:** Do not approximate official Genshin emblems with Compose Canvas or Android custom drawing when a curated, permitted asset is available.
* **Optical Weight Balance:** Keep outlined app icons and solid Genshin crests in separate visual lanes. When a solid Genshin icon shares a row with an outlined app icon, reduce the solid icon by about `2dp` so both carry equal visual weight.

---

## 5. Layout, Geometry & Navigation Shell

### Spacing & Surface Hierarchy
* Follow a strict **4dp / 8dp spacing scale** (`4, 8, 12, 16, 20, 24, 32`).
* **Group with spacing, not nested boxes:** Avoid wrapping bordered Material 3 `Card` composables inside other bordered cards. Use `12–16dp` spacing inside a shared surface group and `20–24dp` between distinct sections.

### Concentric Radii (Mandatory Math)
* Enforce **`innerRadius = outerRadius - padding`** on all nested containers.
  * Example: A surface group with a 20dp outer radius and 12dp padding gives its inner character portrait or weapon box an 8dp radius.
* **Consistent Form Controls:** Dropdowns and text inputs inside the same card must share the same corner radius (e.g., `12dp`) and subtle tonal fill/outline—never mix pill-shaped dropdowns with sharp rectangular outlined text fields.

### Bottom Navigation Bar (5-Slot Symmetric Shell)
* Do not place a floating center AI button on top of a standard 3-item `NavigationBar` if it causes overlap or hit-target collisions.
* Build the main shell with a Material 3 `BottomAppBar` or a custom Compose `Row` with five equal logical slots:
  `[Home, Visuals, Center AI, Reserved Future Slot, Account]`.
* Keep the prominent circular **Irminsul AI** action centered in the middle slot with generous horizontal clearance.
* The reserved future slot is layout space only until a real destination is designed and implemented. Do not add a fake route, dead clickable item, or placeholder screen.
* **No duplicate header navigation:** When `Account` lives in the bottom navigation bar, remove the duplicate `Account` button from the top-right app bar; reserve that slot for a quiet utility action (like `Sync` or `Settings`) only if that action is implemented and useful.
* Preserve the existing real destinations (Home, Visuals, Account) and AI bottom-sheet behavior unless a specific approved milestone changes them.

---

## 6. Core Primitive Composables (Compose, Don't Reinvent)

Feature screens should reuse shared composables rather than independently styling repeated containers. The current project is still evolving and does not yet have a confirmed shared-widget package layout. Before creating a new component, inspect the existing source tree and reuse or extend the actual established package structure; do not assume Flutter paths such as `lib/core/widgets/`.

Recommended reusable primitives:

1. **`SurfaceGroup`**: Material 3 surface wrapper with standardized padding, `16dp`/`20dp` outer radius, and a documented inner-radius calculation for concentric child widgets.
2. **`StatRow` & `MetricTile`**: Standardized key-value displays with muted `onSurfaceVariant`-equivalent labels, high-contrast tabular-number values, and zero decorative pill tags.
3. **`SectionHeader`**: Single-line title with an optional muted `✦` 4-point star prefix and an optional quiet trailing action (`View all`, `+ Add`). Never duplicate a collapsible header's title inside its expanded body.
4. **`AssetGlyphBadge`**: Standardized dark-backed squircle/circle container for white-on-transparent HoYoLAB PNGs (Talents, Constellations).
5. **`ElementAccentChip`**: Low-chroma, compact indicator (monochrome element asset + label) used only for filtering or element identity.
6. **`CodexSheet`**: Shared Compose `ModalBottomSheet` wrapper with a centered drag handle, tonal surface, `24dp` top corner radius, and correct navigation-bar inset handling.
7. **`SkeletonBox`**: Layout-matched tonal shimmer/pulse placeholder for HoYoLAB Chronicle syncs and loading states.

These are design-system recommendations, not permission to create all components in one pass. Introduce only the primitives needed by the active milestone and keep each change independently testable.

---

## 7. Copywriting, Density & Screen-Specific Rules

* **Eliminate robotic disclaimer slop:** Keep in-screen helper text to **one short, scannable sentence**. Move technical caveats (e.g., manual planning disclaimers, unverified synergy notes, or AI provider status explanations) behind a compact info affordance or collapsible note.
* **No redundant data cards:** Do not repeat UID and server in a top Home card when they already appear in the Home header.
* **Compact Artifact Inspection:** Never dump all 5 artifacts and all 20+ substat lines as one vertical wall of text. Make the 5-icon artifact row interactive—tapping an artifact updates a compact inline stat block or opens a `CodexSheet`.
* **Action Hierarchy:** One clear primary action per screen/sheet in Archival Gold (`Sync`, `Done`); secondary actions (`Cancel`, `Remove`, `Close`) remain quiet (`onSurfaceVariant`-equivalent).

---

## 8. States, Motion & Feedback

* **Loading States:** Use `SkeletonBox` layouts matching the target content. Avoid bare centered `CircularProgressIndicator` spinners on otherwise blank screens; use a compact progress indicator only when it communicates an active operation.
* **Empty & Expired Session States:** Provide a concise one-line explanation plus a single Archival Gold primary action (e.g., `Reconnect HoYoLAB`).
* **Motion:** Use native Compose animation APIs with restrained `150–250ms` transitions and an ease-out curve such as `FastOutSlowInEasing` where appropriate. Avoid bouncy or floaty animations.
* **Haptics:** Use Android/Compose haptic feedback only for meaningful selection or completed actions, respect system settings, and avoid repetitive feedback. Use supported Android APIs rather than Flutter `HapticFeedback` examples.

---

## 9. Explicit Banned Patterns ("Do Not Do")

- [ ] **No raw hex colors** inside feature composables.
- [ ] **No scattered icon-library calls** inside feature composables; route app chrome icons through the shared `AppIcons` registry.
- [ ] **No custom-drawn approximations** of Genshin elements or official UI marks when curated permitted assets are available.
- [ ] **No filler badges or non-interactive pills** (e.g., `"SYNCED"`, `"INFO"`, `"STATS"`).
- [ ] **No nested bordered cards** inside cards ("Card-itis").
- [ ] **No mismatched inner/outer border radii** (always subtract padding).
- [ ] **No white Talent/Constellation PNGs** placed directly on light surfaces.
- [ ] **No full-saturation elemental gradients** on list items or dashboard cards.
- [ ] **No multi-paragraph AI disclaimers** cluttering forms or empty states.
- [ ] **No production runtime DOM styling or scraping** from HoYoLAB WebViews. The narrowly scoped Debug-only Battle Chronicles Reference Mode in Section 10 is the sole exception for development-time visual inspection; its findings must be reviewed and translated into native Compose UI.

---

## 10. Battle Chronicles Visual Reference Mode (Debug-Only)

### Purpose

Use HoYoLAB Battle Chronicles as a **visual and interaction reference** when designing Irminsul Codex. Do not invent every layout, information hierarchy, spacing rule, or interaction from scratch when the existing Battle Chronicles experience can be studied directly.

Think of this workflow as **tracing paper**: inspect the reference, understand how its visual system works, and then recreate the useful design ideas as clean, native Android Compose components that fit Irminsul Codex. The goal is informed reconstruction, not a pixel-for-pixel web clone.

This is a development tool for improving the production interface. It is not an end-user feature and is not required for normal app functionality.

### What We Should Study

Use relevant Battle Chronicles pages and states as references for:

* **Character presentation:** portrait placement, profile header composition, character identity, level and progression information.
* **Weapons:** image scale, placement, refinement and level hierarchy, and how weapon details are revealed.
* **Artifacts:** the five-slot arrangement, artifact icon treatment, set information, main stats, substats, and detail-inspection behavior.
* **Character progression:** talent and constellation presentation, icon sizing, grouping, and selected states.
* **Account statistics:** how attributes, exploration, Spiral Abyss, Imaginarium Theater, Stygian Onslaught, and other account information are grouped and prioritized.
* **Visual language:** typography, color relationships, surface treatments, borders, spacing, alignment, iconography, image crops, and information density.
* **Interaction patterns:** expandable sections, selectable icons, tabs, detail panels, sheets, scrolling behavior, and transitions between summary and detailed information.
* **Responsive behavior:** how the reference reorganizes or prioritizes information across available screen sizes, where observable.

Do not limit inspection to static screenshots. Study how important controls behave and what information appears after each interaction.

### Reference Inspection Workflow

1. **Open the reference.** Use a dedicated Debug-only reference-inspection session to access the relevant HoYoLAB Battle Chronicles page or state.
2. **Explore the experience.** Navigate the relevant sections and interact with controls to reveal collapsed, selected, and detailed states.
3. **Inspect visual structure.** Where useful, inspect visible DOM structure, element dimensions, computed styles, font stacks, colors, spacing, image sizing, and asset references through narrowly scoped WebView developer tooling.
4. **Record the design pattern.** Describe the hierarchy, layout, component behavior, and the reason the pattern works. Keep useful measurements and observations as development references.
5. **Translate into Compose.** Recreate the relevant pattern with native Android and Jetpack Compose components, using Irminsul Codex theme tokens and architecture.
6. **Compare and refine.** Compare the Irminsul implementation against the reference using screenshots or equivalent visual comparisons. Adjust hierarchy, proportions, spacing, density, and interaction behavior where appropriate.
7. **Keep the result native.** Production screens must render from Irminsul Codex Compose components and app data, not from embedded HoYoLAB pages or copied web stylesheets.

### Debug-Only Inspector Requirements

* **Debug builds only:** Any inspector entry point, WebView debugging support, JavaScript evaluation, or reference-capture controls must be excluded from Release builds. Gate the feature with the Android build configuration for the Debug variant. Do not use Flutter `kDebugMode` or Dart WebView APIs.
* **Keep it separate from production navigation:** Do not add a user-facing destination, permanent app setting, or production UI entry for this developer tool. Use an isolated developer-only screen or entry point.
* **Dedicated inspection session:** Do not silently reuse or extend the normal HoYoLAB login WebView lifecycle. The current login flow captures the required authentication cookies and then clears the WebView's cookies, cache, history, and form data. Preserve that security behavior. If a reference requires authentication, use a deliberately separate, user-initiated inspection session with its own lifecycle and clear its session data when inspection ends. Never retain or expose credentials for convenience.
* **Narrow destination scope:** Restrict navigation and inspection to the intended HoYoLAB domains and the specific reference pages needed for the task. Do not turn the inspector into a general-purpose browser or crawler.
* **Inspection data only:** The inspector may examine visible page structure and presentation metadata, such as element hierarchy, dimensions, computed visual properties, font information, and references to visible assets. It must not collect authentication cookies, tokens, account identifiers, personal account data, or unrelated session state.
* **No access-control bypass:** Do not bypass authentication, anti-bot protections, service restrictions, or other access controls. Only inspect pages the user can legitimately access through the normal service experience.
* **No bulk harvesting:** Do not build automatic crawling, mass asset downloading, unrestricted DOM export, or background collection. Inspection should be deliberate and limited to the selected reference screen or interaction.
* **No automatic asset reuse:** Discovering an image, icon, SVG, font, or URL does not grant permission to bundle or redistribute it. Any proposed asset must undergo a separate provenance, licensing, permission, attribution, and suitability review before production use. Prefer original native Compose treatments or approved assets when reuse is not permitted.

### From Reference to Irminsul Design System

* Treat Battle Chronicles as a **design reference**, not as the source code or runtime UI framework for Irminsul Codex.
* Extract reusable design principles: hierarchy, proportions, spacing, grouping, visual emphasis, information density, and interaction behavior.
* Reinterpret those principles for a native Android experience. Use Material 3, the app's theme tokens, established Compose components, and Android interaction conventions.
* Preserve Irminsul Codex's design direction: **90% clean, modern Android utility / 10% subtle Genshin archival flavor**. Do not blindly reproduce every Battle Chronicles decoration, web-specific layout, gradient, animation, or visual inconsistency.
* Keep the reference and the implementation distinct. Do not inject inspected HTML, CSS, JavaScript, or remote web components into production screens.
* Dynamic character, weapon, and artifact artwork must continue to use the existing approved Battle Chronicle JSON URL flow and image-loading/cache path. The inspector is not a replacement data or asset pipeline.
* When a reference pattern is useful, turn it into a reusable native Compose component or documented theme rule only when it supports more than a one-off imitation. Avoid creating abstractions for every inspected element.
* The inspector is an aid, not the deliverable. The valuable outcome is a better, coherent, maintainable Irminsul Codex interface.

### Scope and Priority

Battle Chronicles Reference Mode should be developed as a focused visual-development aid when meaningful UI work requires it. Start with the screens and interactions most relevant to the current task, rather than building a large inspection platform up front.

The normal app must remain fully functional when the inspector is absent, disabled, or unable to load a reference page. No production feature may depend on it.

---

## 11. Verification, Android Build & Blind-Coding Workflow

Because AI coding agents cannot visually inspect a live device screen, every UI change must pass the repository's actual Android verification workflow before being considered complete.

### Mandatory Verification After Every UI Edit

1. **Kotlin/Android static checks and build:**
   * Use the repository's configured Gradle tasks and GitHub Actions workflow. At minimum, run the relevant unit tests and the Debug APK build through the established CI workflow.
   * Do not prescribe `flutter analyze`, `flutter test`, Dart tooling, or Flutter widget-test commands for this repository.
   * Fix Kotlin compilation errors and new relevant warnings. Do not suppress diagnostics without a documented reason.
2. **Compose/UI tests:**
   * Add or update Compose UI/instrumentation tests when the project has the required test dependencies and test setup. Do not assume Flutter widget tests or an existing Compose UI test harness.
   * For a new or refactored screen, cover Light and Dark themes and a narrow mobile layout (around `360dp` width) where the available test harness supports it. Assert key content/actions and absence of layout exceptions.
   * If UI instrumentation tests are not configured or CI skips them, state that limitation explicitly. A JVM unit-test pass does not prove Compose layout correctness.
3. **Physical-device verification:**
   * Provide a concise test checklist for the user when layout, navigation, touch targets, insets, animation, or image rendering need real-device confirmation.
   * Record user-reported device results separately from CI results. Never claim device verification without the user's report or direct test evidence.

### Self-Audit Checklist Before Finishing a Diff

Before returning a completed UI change, inspect the modified Kotlin files and verify:
* Feature composables do not contain raw color literals; colors come from app theme tokens.
* App chrome icons use the shared icon registry where one has been established.
* Numeric metrics use tabular figures where alignment matters.
* Scrollable content accounts for Scaffold and system-bar insets without duplicate padding.
* Light and Dark appearance remain legible.
* Existing navigation destinations, saved data, and account behavior remain intact unless the milestone explicitly changes them.

### Scoped Execution Rule

* Refactor **one concern per pass** (e.g., Pass 1: theme tokens and shared icon registry; Pass 2: symmetric five-slot bottom shell; Pass 3: Home screen; Pass 4: Character Showcase). Avoid sweeping multi-screen rewrites in a single diff so CI and real-device visual verification stay fast and easy to debug.
