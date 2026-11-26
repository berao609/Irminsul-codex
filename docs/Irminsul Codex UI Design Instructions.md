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
- [ ] **No runtime DOM styling, scraping, or Battle Chronicles dependency.** Use the one-time, offline design-reference workflow in Section 10 during development, then implement the resulting design system natively in Compose.

---

## 10. Battle Chronicles Design Reference & Local Reconstruction

### Purpose

Use HoYoLAB Battle Chronicles as the **one-time visual and interaction blueprint** for Irminsul Codex. We do not need to invent every layout, information hierarchy, spacing rule, or interaction from scratch. Study the existing experience, understand its design logic, and inherit the useful patterns in our own implementation.

Think of this as **tracing paper**: observe how Battle Chronicles presents information and how its interface behaves, then reconstruct those ideas as Irminsul Codex's own local design system using native Android and Jetpack Compose.

**This is a development-time reference process, not an app feature.** Do not build a runtime inspector, embedded reference browser, online design service, or permanent WebView dependency for it. Once the reference has been studied and its design rules have been documented and implemented locally, Irminsul Codex must not need to contact HoYoLAB to render its interface.

### What to Study in the Reference

Study the relevant Battle Chronicles screens, visual states, and interactions. Capture enough information to understand both their appearance and their underlying design logic.

* **Character presentation:** portrait placement and scale, profile header composition, character identity, level, progression, and information hierarchy.
* **Weapons:** image scale and placement, refinement and level presentation, and how weapon details are revealed.
* **Artifacts:** the five-slot arrangement, icon treatment, set information, main stats, substats, and detail-inspection behavior.
* **Character progression:** talent and constellation presentation, icon sizing, grouping, selected states, and detail views.
* **Account statistics:** attributes, exploration, Spiral Abyss, Imaginarium Theater, Stygian Onslaught, and how different categories of account information are grouped and prioritized.
* **Visual language:** typography, color relationships, backgrounds, surfaces, borders, spacing, alignment, iconography, image crops, and information density.
* **Interaction patterns:** expandable sections, selectable icons, tabs, detail panels, sheets, scrolling behavior, and transitions between summary and detailed information.
* **Responsive behavior:** how information is reorganized or prioritized at different screen sizes, where observable.

Do not study screenshots alone. Interact with the reference manually and record what changes when a control, icon, tab, or section is selected.

### One-Time Reference Capture Process

1. **Visit the reference manually.** Open HoYoLAB Battle Chronicles in a normal browser or the official app, using an account and access method already available to the user. No special in-app inspector is required.
2. **Capture representative screens.** Take screenshots of the relevant pages and important states. Include expanded details and selected states, not only default views.
3. **Explore interactions.** Tap relevant controls and record what expands, changes, or becomes visible. Capture additional screenshots where necessary.
4. **Study the design logic.** Identify layout structure, component hierarchy, relative sizing, spacing, alignment, typography, color roles, information density, and interaction behavior. Record useful measurements when they can be determined reliably.
5. **Create a local design reference.** Convert observations into concise design notes, annotated screenshots, layout descriptions, and reusable rules within the development workflow. These references must be available locally to the coding process; do not require a live HoYoLAB connection to consult them.
6. **Reconstruct in Compose.** Implement the relevant patterns as native Compose screens, components, and theme tokens. Adapt them to Irminsul Codex's architecture and Android conventions.
7. **Compare and refine.** Compare local screenshots of Irminsul Codex against the captured reference images. Refine proportions, hierarchy, spacing, density, and interaction behavior.
8. **Keep the implementation independent.** Once reconstructed, production screens must render from Irminsul Codex code, local design tokens, and the app's own data. They must not embed or load Battle Chronicles pages to reproduce the design.

### What We Should Inherit

The goal is to inherit **design logic**, not blindly duplicate implementation details.

* Reuse the reference's successful information hierarchy, grouping, proportions, visual emphasis, spacing relationships, and interaction concepts.
* Preserve familiar presentation patterns where they make account information easier to scan and understand.
* Adapt web-specific behavior into appropriate Android interactions. For example, use native expandable Compose sections, selectable detail panels, and bottom sheets where they provide the equivalent experience.
* Preserve Irminsul Codex's design direction: **90% clean, modern Android utility / 10% subtle Genshin archival flavor**. Do not reproduce every decoration, gradient, animation, or inconsistency just because it exists in the reference.
* Do not copy HoYoLAB source code, private implementation details, or protected assets. A screenshot or visible asset URL is not permission to redistribute that asset. Review provenance, licensing, permission, and attribution separately before considering any third-party asset for inclusion.
* Dynamic character, weapon, and artifact artwork must continue to use the existing approved Battle Chronicle JSON URL flow and image-loading/cache path. The design-reference process is not a new asset pipeline.
* Prefer native Compose drawing, Material 3 components, and locally defined theme tokens for recreated visual treatments when third-party asset reuse is not permitted.
* Turn recurring patterns into reusable Compose components only when they genuinely improve consistency and maintainability. Do not create abstractions for every individual visual detail.

### Local-First Requirement

* The captured screenshots, notes, measurements, and design decisions are development references. Keep the necessary reference material available locally to the development workflow.
* The resulting design tokens, component rules, and UI implementations must live in the Irminsul Codex project as ordinary local project resources and source code.
* Do not add runtime network requests, remote stylesheets, remote component libraries, WebView rendering, DOM inspection, or a Battle Chronicles availability check for UI presentation.
* Do not add a debug-only WebView inspector solely to perform this reference process.
* If the original reference changes or a future redesign needs new study, repeat the manual capture process deliberately. It is not a recurring background task.
* The app must remain visually complete and fully functional when offline, subject only to its separately defined account-data and optional online integration requirements.

**The deliverable is the locally implemented Irminsul Codex design system, not a tool for viewing HoYoLAB.**

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
