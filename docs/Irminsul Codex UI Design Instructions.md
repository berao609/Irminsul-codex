# Irminsul Codex — Unified UI Design Instructions

> Target: AI coding agents and contributors implementing the Irminsul Codex native Android UI.
> Platform: Kotlin, Jetpack Compose, Material 3. This is not a Flutter project.
> Design Objective: High-fidelity Battle Chronicles presentation, disciplined anti-slop design, and modern Android touch usability.
>
> Irminsul Codex must feel like a faithful, premium Genshin account companion designed specifically for Android. Do not flatten distinctive reference layouts into generic dashboards. Do not transplant a web page into Android. Reconstruct the reference's visual and interaction intent, then adapt it into a native, maintainable, touch-first experience.

---

## 1. Binding Design Priorities

All UI work must follow these priorities. Do not reinterpret or reorder them to make implementation easier.

1. Functional correctness and data integrity. Preserve working features, accurate account data, correct states, and meaningful behavior.
2. Battle Chronicles fidelity. Treat the reference as the primary visual and interaction benchmark for applicable account, character, exploration, and game-mode screens.
3. Android usability. Adapt the design for touch, accessibility, navigation, system insets, responsive layouts, and platform behavior.
4. Anti-slop quality. Remove needless decoration, generic component stacking, redundant content, visual noise, and inconsistent styling. Do not remove meaningful reference design.
5. Maintainability. Use the simplest maintainable implementation that satisfies the above. Do not simplify the design itself merely to reduce implementation effort.

When a distinctive reference composition improves hierarchy or information clarity, it takes priority over generic component uniformity. Android adaptation and anti-slop refinement must not unnecessarily erase that composition.

## 2. Three Design Authorities

### 2.1 Battle Chronicles: Visual and Interaction Benchmark

Battle Chronicles is the primary reference for WHAT applicable screens should present and HOW they organize it.

Preserve and reconstruct its relevant:
- Layout and composition.
- Character artwork scale, placement, and presentation.
- Profile and identity hierarchy.
- Backgrounds, contextual imagery, and meaningful layering.
- Information grouping, ordering, density, and emphasis.
- Exploration and game-mode identity.
- Equipment, progression, talent, constellation, and statistics presentation.
- Selection, navigation, expansion, tabs, panels, sheets, and other meaningful interactions.
- Responsive organization, where observed and relevant.

Use the existing docs/BATTLE_CHRONICLE_REFERENCE_ANALYSIS.md and its captured reference materials. Do not invent unobserved behavior and label assumptions as observations. The reference is a development-time blueprint, not a runtime dependency.

### 2.2 Irminsul Anti-Slop Discipline: Quality Filter

The anti-slop principles remain mandatory:
- Every visible element must have a purpose.
- No generic AI-generated dashboard composition.
- No unnecessary cards, nested cards, badges, pills, borders, gradients, shadows, icons, or ornamental dividers.
- No redundant data or filler copy.
- Avoid stacked containers where layout, spacing, or grouping is clearer.
- Maintain consistent typography, spacing, radii, and theme tokens.
- Preserve useful information density without sacrificing readability.
- Use abstractions only when they solve a recurring real problem.

These are quality filters, not rigid visual prohibitions. A reference-backed background, gradient, border, layered surface, badge, or ornament is permitted when it contributes to identity, hierarchy, state, or composition. Do not remove meaningful visual treatments merely to make a screen more neutral.

### 2.3 Android: Touch and Platform Adaptation

The Android experience governs HOW the reconstructed design works on-device:
- Comfortable and accessible touch targets.
- Discoverable tap interactions and clear selection states.
- Natural scrolling and gesture compatibility.
- Appropriate navigation, dialogs, sheets, selection controls, and feedback.
- Edge-to-edge rendering with correct system-bar and gesture insets.
- Readable text, accessibility semantics, and content descriptions.
- Responsive layouts for supported screen sizes and orientations.
- Appropriate, restrained motion and action feedback.
- State preservation when it is expected.

Use Jetpack Compose and Material 3 as the implementation foundation, not as a mandatory visual template. Prefer current Android/Material interaction patterns when they improve usability. Android 16/17-era expressive components or APIs may be used only when available through the project's resolved dependencies/minimum-supported platform, provide a concrete benefit, and do not compromise compatibility. Never raise the minimum Android version or add dependencies solely to use a new visual component without explicit approval.

## 3. Battle Chronicles Fidelity Is High

High fidelity is the chosen direction. Do not reduce it to "inspired by" or a loose style reference.

### Preserve faithfully when applicable
- The reference screen's composition and recognizable layout.
- Character-first presentation and artwork prominence.
- Visual hierarchy, proportion, placement, and grouping.
- Section structure and meaningful background treatments.
- Contextual element, rarity, region, and mode identity.
- The relationship between summary information and detailed information.
- Meaningful interaction patterns and state transitions.

### Adapt only where needed
- Touch-target dimensions and interaction affordances.
- Android navigation and system insets.
- Accessibility, text scaling, and readability.
- Scrolling, sheet behavior, responsive layout, and state management.
- Web-specific mechanics that have no useful native equivalent.

Do not replace a distinctive reference layout with generic Material cards, a uniform vertical list, or a dashboard of metric tiles. Do not reproduce web awkwardness when a native interaction can preserve the intent more effectively.

Fidelity means preserving the design intent and composition, not copying pixels, web code, implementation internals, or every decoration.

## 4. Screen-Specific Visual Direction

Screens must share the Irminsul identity without being forced into one generic template.

| Screen / Area | Fidelity | Required direction |
|---|---|---|
| Character Showcase and Character Details | Highest | Character-first composition, prominent artwork, background identity, selector, character identity/progression, stats, weapons, artifacts, talents, constellations. |
| World Exploration | High | Reference-backed region identity, contextual backgrounds, progress presentation, overview/detail hierarchy, and region grouping. |
| Abyss, Theater, Stygian Onslaught, and other supported modes | High | Distinct mode identity, meaningful grouping, team/character presentation, records, results, and period navigation where supported by data. |
| Account Overview | High | Reconstruct reference hierarchy, account summary, collectibles/progression, and My Characters presentation. Keep dense data organized and scannable. |
| Home | Balanced | Clean and useful account overview; preserve established Real-Time Notes placement and avoid redundant account information. |
| Tools and Calculators | Utility-first | Fast task completion, correct calculations, clear inputs/outputs, compact material presentation, and comfortable touch controls. |
| Settings and secondary utility surfaces | Utility-first | Straightforward, accessible, restrained, and consistent with Android conventions. |

Real-Time Notes belong on Home only. Do not duplicate them in Visuals or other destinations.

Do not invent missing account values, exploration counts, records, or progress. Preserve distinctions between actual zero, missing, unavailable, stale, and failed retrieval states.

## 5. Irminsul Visual Identity and Theme

Retain the existing identity and tokens, but apply them contextually rather than restricting every screen to neutral presentation.

### 5.1 Archival Gold

| Role | Dark | Light |
|---|---|---|
| Primary accent | #D3BC8E | #8E6E35 |
| On-primary | #1A1814 | #FFFFFF |

Archival Gold represents Irminsul Codex identity and should be used for important app actions, selected states, active navigation, focus, and intentional accents. Do not use it indiscriminately on every element.

### 5.2 Surface and Background Strategy

- Default utility surfaces: dark charcoal/slate in Dark theme and warm alabaster/off-white in Light theme.
- Reference-driven screen regions may use distinctive backgrounds, contextual imagery, tonal layering, gradients, or surface treatments when needed for fidelity.
- Surfaces are screen- and component-specific. Do not force one neutral background onto every section.
- Use Material 3 tonal surface roles where suitable and supported by the project's resolved version.
- Prefer visual grouping and spacing. Add containers when they improve hierarchy, containment, or interaction clarity.
- Avoid pure-black canvases and heavy borders/shadows as defaults, but do not ban them when a justified reference treatment calls for them.

### 5.3 Element, Rarity, Region, and Mode Identity

- Keep all seven element colors and rarity tones in shared theme tokens with Light/Dark variants.
- Preserve meaningful Genshin semantic colors, including rarity, element, region, character, and mode identity, where they communicate information or reproduce the reference hierarchy.
- Ensure contrast in both themes. Bright tones such as Cryo, Geo, and Anemo may need adjusted Light-theme values.
- Elemental color is not restricted to a fixed opacity budget or only character-detail headers. Apply it wherever it is meaningful and reference-backed. Avoid large saturated color floods that obscure content or create noise.
- Keep selected, disabled, warning, error, and status colors semantically distinct from game identity colors.

### 5.4 Game Assets and Contrast

- Continue using the existing approved dynamic Battle Chronicle JSON artwork URL flow and image-loading/cache path.
- Do not create a new asset pipeline or add a runtime Battle Chronicles dependency as part of UI reconstruction.
- White-on-transparent talent or constellation icons must have an appropriate high-contrast backing in both themes.
- Use permitted, curated assets when available. Do not approximate official Genshin marks with custom drawing if an appropriate permitted asset exists.
- Review provenance, license, and permission before introducing third-party assets. A visible URL or captured screenshot is not redistribution permission.
- Static assets should use Android-supported resource formats. Do not add an SVG renderer or dependency without checking existing support and getting approval when needed.

## 6. Typography and Data Presentation

- Use the existing shared theme and typography roles. If available and licensing permits, Plus Jakarta Sans or Inter may be used as the primary sans-serif; inspect the actual assets and setup before assuming availability.
- JetBrains Mono is reserved for UIDs, copyable codes, and raw technical data such as JSON. Do not use it for general UI labels or prose.
- Use tabular numerals for changing or comparable values such as Resin, timers, character stats, artifact values, damage, levels, and record counts when supported by the chosen typeface.
- Maintain readable hierarchy among display titles, section headings, labels, supporting text, and values.
- Do not apply arbitrary font sizes repeatedly. Use shared styles for recurring roles; one-off sizes may be used when required by a reference composition.
- Preserve high information density when the reference calls for it, but do not allow text, labels, or stats to become cramped or unreadable.
- Shorten UI copy. Helper text should be brief. Put detailed technical explanations behind a deliberate information affordance when they are not essential to the task.

## 7. Iconography

Maintain a clear distinction between application icons and Genshin domain artwork.

### App UI icons
- Use the existing shared icon registry/wrapper (AppIcons) when present.
- Centralize icon choices, content descriptions, and size conventions.
- Do not scatter direct icon-library calls through feature composables.
- Use accurate, consistent icon metaphors and optical sizing.
- Do not assume Flutter packages or APIs exist in this Kotlin project.

### Genshin assets
- Use approved game assets through existing asset paths and Android-supported resource handling.
- Keep app-shell icons and solid game crests visually balanced when they share a row.
- Do not custom-draw approximations of official emblems when curated permitted assets are available.

Do not add icon dependencies without compatibility, maintenance, licensing, and size review.

## 8. Layout, Geometry, and Components

### 8.1 Spacing

Use the established 4dp/8dp spacing family (4, 8, 12, 16, 20, 24, 32dp) as the default. Reference composition may justify deliberate deviations. Do not distort proportions to force every gap into the scale.

### 8.2 Radii and Shape

- Keep related control shapes consistent.
- Apply the concentric-radius principle to genuinely nested surfaces: innerRadius = outerRadius - padding when it produces a coherent result.
- This is not a requirement to nest surfaces. Avoid unnecessary container nesting.
- Controls grouped within one form should use compatible shapes and sizes. Do not mix shapes arbitrarily.

### 8.3 Surface and Card Usage

- No card-itis. Do not place bordered cards inside bordered cards by default.
- Use surface, spacing, layout, imagery, and hierarchy instead of wrapping every content block.
- Metric tiles, cards, and containers are allowed when they reproduce the reference or improve comprehension, grouping, interaction, or accessibility.
- Do not force every screen into the same card-based structure.
- A component's visual treatment must serve its context; reuse behavior and tokens without flattening screen-specific design.

### 8.4 Shared Compose Components

Inspect the actual package tree and existing components before creating or extending primitives. Recommended patterns include:
- SurfaceGroup
- StatRow / MetricTile
- SectionHeader
- AssetGlyphBadge
- ElementAccentChip
- CodexSheet
- SkeletonBox

These are candidates, not a checklist. Do not build all of them preemptively. Introduce or reuse components when they solve an active, repeated need. Keep abstractions small and testable.

## 9. Navigation Shell and Android Interaction

Preserve existing functional destinations and AI behavior unless an approved implementation batch changes them.

### Main navigation
The intended five logical slots are:
[Home, Visuals, Center AI, Reserved Future Slot, Account]

- Keep the center AI action visually prominent and centered, with adequate spacing and non-overlapping hit targets.
- The reserved slot is layout space only until a real destination is approved and implemented. No fake route, dead click, or placeholder screen.
- Account must not be duplicated in a top-right header if it already has a bottom-navigation destination.
- Keep useful top-bar actions only when implemented and relevant.
- Inspect the current navigation implementation before changing its component. Do not assume a particular Compose API is available; verify project dependencies.

### Touch and behavior guardrails

- Interactive hit targets must be comfortably touchable and meet applicable Android accessibility guidance. Small visual icons may have larger invisible/semantic hit regions.
- Every control must perform the action it advertises. No fake buttons, decorative controls that look interactive, dead click handlers, or controls that only change color without a meaningful result.
- Selection, expanded/collapsed, loading, disabled, success, and error states must be clear.
- Prefer a simple tap when it is more discoverable than a gesture. Do not add hidden gestures without clear affordance.
- Horizontal scrolling must not conflict with vertical scrolling or back gestures.
- Sheets and dialogs must be dismissible, respect system insets, and keep essential controls reachable.
- Preserve state appropriately across navigation and configuration changes according to existing architecture.
- Use haptics only for meaningful events, respect system settings, and avoid repetitive feedback.
- Use Compose semantics and accessibility labels for actions and game artwork where needed.
- Do not implement interaction for appearance alone. Static presentation is acceptable when content is already visible and no interaction adds value.

## 10. Edge-to-Edge, Insets, and Adaptive Layout

Irminsul supports Light, Dark, and System themes and uses edge-to-edge rendering. Preserve theme parity and edge-to-edge behavior in all new and refactored screens.

- Use current Scaffold padding and Compose window-inset APIs appropriate to the project's dependency version.
- Do not hardcode device-specific status-bar, navigation-bar, or gesture offsets.
- Avoid double-applying bottom navigation, system-bar, or safe-drawing padding.
- Draw immersive backgrounds and scrolling content edge-to-edge where appropriate; keep interactive and readable content within safe areas.
- Maintain system-bar icon contrast for the active theme.
- Adapt to supported screen sizes and orientations without sacrificing the primary mobile hierarchy.
- Check text scaling, long names, and localized or variable-length content where relevant.
- Do not assume newer Android/Material APIs are available; verify the resolved version before implementation.

## 11. Motion, Feedback, and UI States

- Prefer content-matched loading placeholders (e.g., SkeletonBox) over blank screens with a lone spinner.
- Use compact progress indicators when they clearly communicate an active operation.
- Empty, expired, unavailable, and error states should explain the state concisely and provide a relevant action where possible.
- Do not conflate missing data, retrieval failure, stale data, or actual zero values.
- Use restrained transitions and animation. Typical short transitions around 150–250ms may be suitable, but duration and easing must match context. Do not apply motion everywhere.
- Avoid bouncy, floaty, or attention-seeking animation without a specific purpose.
- Feedback should be immediate and meaningful. Do not claim success before the action actually completes.
- Respect reduced-motion or platform accessibility behavior where applicable.

## 12. Copy, Density, and Action Hierarchy

- Keep labels and helper copy short, specific, and scannable.
- Avoid multi-paragraph disclaimers inside normal workflows.
- Put nonessential caveats behind a concise info affordance or expandable explanation.
- Do not duplicate values already visible in the screen header or another nearby region.
- Preserve the reference's useful density and visual grouping.
- Use one clear primary action when a screen has a dominant task. Secondary actions must remain visually subordinate but discoverable.
- Do not make every action gold or visually prominent.
- Controls, chips, labels, and badges must communicate a real state, identity, filter, or action. Remove filler.

## 13. Reference Research, Reconstruction, and Comparison

Battle Chronicles research is a one-time, offline development process. Repeat targeted research only when a concrete unresolved design question or new scope requires it.

### Research method
1. Read docs/BATTLE_CHRONICLE_REFERENCE_ANALYSIS.md and the relevant captured materials.
2. Inspect the relevant screenshots and states. Do not assume a screenshot proves interaction behavior.
3. Explore controls only when needed to resolve a real implementation question. Record observed behavior separately from assumptions.
4. Use screenshots and layout inspection together. Do not infer exact Android dp measurements from CSS pixels or screenshot pixels without accounting for scale and density.
5. Reconstruct the design locally with Compose, shared theme tokens, and app data.
6. Compare local screenshots against the reference and refine composition, scale, spacing, hierarchy, density, and interaction.
7. Keep the implementation offline and independent of the reference site.

### Evidence guardrails
- Do not invent reference behavior, measurements, colors, or structure. Distinguish confirmed observations from assumptions.
- Do not exhaustively inspect every control unless implementation needs it.
- Use docs/BATTLE_CHRONICLE_REFERENCE_ANALYSIS.md as the research record. Do not create duplicate reference documentation without a concrete need.
- Reference screenshots, DOM captures, and CSS are research artifacts, not production dependencies.
- Do not copy HoYoLAB source code, private implementation, or protected assets.
- Do not embed Battle Chronicles in WebView or load remote CSS, HTML, or components to recreate the design.
- Do not scrape Battle Chronicles at runtime, add a runtime reference inspector, or make UI rendering depend on Battle Chronicles availability.
- The app must render from its own Kotlin/Compose UI, design tokens, and validated local account data.
- Continue using the established approved artwork loading/cache path. Do not create an alternate asset pipeline as a UI shortcut.

## 14. Screen Review and Quality Gates

Before marking a UI task or batch complete, review the changed screen against these requirements:

### Design fidelity
- Does the screen preserve the intended Battle Chronicles composition, hierarchy, artwork prominence, grouping, and identity?
- Has a distinctive layout been flattened into generic cards or lists without a clear reason?
- Are backgrounds, colors, gradients, and decorations meaningful and reference-backed rather than arbitrary?
- Is the screen-specific visual personality preserved while remaining part of Irminsul?

### Anti-slop
- Is every component purposeful?
- Are there redundant cards, duplicated information, unnecessary borders, icons, badges, pills, or decoration?
- Is the information density appropriate to the reference and task?
- Are shared tokens and components used without forcing generic uniformity?

### Android usability
- Are touch targets comfortable, controls discoverable, and states clear?
- Are scrolling and gestures compatible?
- Are system insets, edge-to-edge, navigation, text scaling, and accessibility handled?
- Do sheets/dialogs work naturally on Android and keep content reachable?
- Are animations restrained and feedback meaningful?

### Technical integrity
- Does the UI preserve existing features and data correctness?
- Are missing, stale, unavailable, and failed states represented accurately?
- Are existing architecture and dependency constraints respected?
- Are tests/build checks run as appropriate?
- Is physical-device verification still required and clearly distinguished from automated verification?

Do not declare a batch complete without identifying unresolved regressions and the verification actually performed.

## 15. Final Design Standard

Irminsul Codex must be:
- Faithful: unmistakably based on Battle Chronicles presentation for applicable screens.
- Purposeful: anti-slop principles remove noise, not identity.
- Native: designed for Android touch, accessibility, system behavior, and adaptive layouts.
- Distinctive: Archival Gold and meaningful Genshin semantics coexist with contextual reference visuals.
- Consistent: shared tokens and component behavior, without forcing every screen into one template.
- Maintainable: native Compose, existing project architecture, deliberate reuse, and no unnecessary dependencies.

The target is a high-fidelity Battle Chronicles experience, reconstructed as a premium native Android companion. It must not look like a generic dashboard, a web page transplanted into an app, or a decorative game-menu clone.
