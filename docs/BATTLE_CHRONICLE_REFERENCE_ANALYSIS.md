# Battle Chronicle Reference Analysis

**Project:** Irminsul Codex  
**Purpose:** Preserve development-time observations from the HoYoLAB Genshin Impact Battle Chronicle reference for later native Jetpack Compose implementation.  
**Status:** Initial HTML analysis complete; stylesheet and interaction-state capture remain pending.  
**Reference capture:** `docs/share-page.html`  
**Captured HTML blob SHA:** `40c5fee0766a722eb22efc6ce7213310797ee29e`

> This is a design-research document, not an implementation specification. Separate observed facts from assumptions. Recheck the reference if the site changes before implementation.

## 1. Scope and product decisions

The reference is used only as a one-time development-time design and interaction study. Irminsul Codex must reproduce approved patterns through its own local Kotlin and Jetpack Compose implementation.

The relevant Visuals scope is:

- Account overview and progression summary.
- Character roster and character showcase.
- World exploration and regional progress.
- Collectibles: waypoints, domains, oculi, and chest categories.
- Spiral Abyss current and previous records.
- Imaginarium Theater records.
- Stygian Onslaught records.

Real-Time Notes belong on **Home only**, not Visuals. Event Calendar, Common Tools, Genius Invokation TCG, and Serenitea Pot are outside the agreed Visuals reconstruction scope. Do not add modules merely because they appear in the captured page.

The reference process does not authorize copying HoYoverse source code, private implementation details, or protected assets. Do not introduce a runtime Battle Chronicle dependency, remote stylesheet, reference WebView, DOM inspector, or page scraping into the production app.

## 2. Evidence inspected

### 2.1 Captured HTML

- File: `docs/share-page.html`
- Captured outerHTML length: approximately 116 KB.
- SHA: `40c5fee0766a722eb22efc6ce7213310797ee29e`
- Contains the rendered `.share-page` subtree and its nested page content.
- Contains 8 page anchor IDs: character, exploration, calendar, tools, challenge, Spiral Abyss, TCG, and Serenitea Pot.
- Contains approximately 482 distinct CSS class names, 117 image elements, and 35 SVG elements.
- The capture includes populated account-specific content. Do not copy its UID, nickname, character data, or other account-specific values into this analysis.

### 2.2 Browser DevTools observations previously reported

The page shell includes:

- `.m-root-layout` as the main UI mount.
- A Vue 2.7.14 script.
- Swiper 5.4.5.
- Main page bundles named `vendors_7a198bac.js` and `m_7532c8ec.js`.
- A stylesheet filename observed in DevTools: `m_55ae43ba.css`.

The inspected root style showed `.m-root-layout` with a minimum height of 100vh and a dark background around `#2a2a2a`. This is one observed root value, not a complete color palette.

The captured outerHTML does **not** include the document head or stylesheet link. It does not contain the CSS file itself. No stylesheet URL or complete CSS rules have been verified yet.

## 3. Page structure observed in the HTML

The captured page hierarchy includes:

- `.share-page`: page content container.
- `.m-index-layout`: main page layout.
- `.game-kv`: large top/key visual region.
- `.game-header`: header/navigation region.
- `.m-index-page-info`: primary information content.
- `.account-block`: account identity area.
- `.game-summary`, `.summary-top`, and `.summary-btm`: compact account summary and additional statistics.
- `.world-exploration-block`: exploration module.
- `.exploration-card`, `.area-item`, and `.progress-section`: regional exploration cards and progress elements.
- `.abyss-block`: Spiral Abyss section.
- `.util-block`: common tools section.
- `.tcg-block`: TCG section.
- `.serenitea-block`: Serenitea Pot section.

These are observed rendered DOM class names. They are not evidence of the original Vue component architecture, source-code structure, or backend model.

## 4. Visual and interaction patterns identified

### 4.1 Account summary

The top account region groups a profile identity/header with a compact metric summary. The summary has a primary row and a secondary group of additional statistics. A visible expand control and an associated expanded-state marker indicate that the secondary metrics can be collapsed or expanded.

**Irminsul direction:** Use a compact overview with a deliberate primary/secondary hierarchy. Preserve the ability to expand secondary metrics rather than displaying every statistic at full prominence at once.

### 4.2 Character entry points

The captured page includes a horizontally scrollable strip of compact character portraits and a separate character information presentation. The visible character content uses compact identity, element, level, and equipment-related information.

**Irminsul direction:** Keep the roster navigable and compact. The selected character should reveal details progressively. Preserve the existing Irminsul requirement that weapon and artifact details are inspectable on demand rather than dumping every detail into one long page.

### 4.3 World exploration

The HTML contains a dedicated exploration section with multiple regional cards. Class groups indicate area names, progress sections, progress values, and card imagery/backgrounds. Inline styles show region-specific colors and progress widths in the captured state.

**Irminsul direction:** Use region cards with clear region identity and a readable progress indicator. Do not hardcode the captured account's progress values or treat one screenshot's colors as universal theme tokens.

### 4.4 Game-mode sections

The page includes distinct section structures for Spiral Abyss, Imaginarium Theater, and Stygian Onslaught. The captured text indicates that these sections present period or cycle context, best-record summaries, and mode-specific statistics.

**Irminsul direction:** Keep each mode's summary tailored to its own data model. Reuse shared section framing where appropriate, but do not force all modes into one generic table or invent fields absent from the account export.

### 4.5 Section headers and navigation

Repeated `.block-title` structures include a left title/icon group and, in some sections, a right-side action such as a view-more affordance.

**Irminsul direction:** Establish a consistent section-header pattern with optional trailing action. Use native Compose click targets and accessibility semantics.

## 5. What the HTML cannot tell us

The captured HTML is a snapshot of rendered output. It does not, by itself, establish:

- The original Vue component tree or source code.
- The complete CSS cascade, inherited styles, breakpoints, or responsive rules.
- The behavior of controls that were not activated before capture.
- The complete set of loading, empty, error, selected, or expanded states.
- Server-side logic or data not present in this particular response.
- Permission to reuse any image, font, icon, or other protected asset.

The class names and utility classes are useful clues, not a substitute for measured styles or interaction testing.

## 6. Pending reference capture

### 6.1 Stylesheet

The stylesheet filename `m_55ae43ba.css` was seen in DevTools, but its full URL and file contents have not yet been obtained. The next research step is to retrieve the stylesheet from the exact URL shown by the browser, then record:

- Typography families, weights, sizes, and line heights.
- Layout widths, heights, padding, gaps, and alignment.
- Surface, text, border, and accent color roles.
- Card geometry, radii, shadows, and overlays.
- Image crop/fit behavior.
- Responsive rules that apply to the mobile layout.

Do not guess the stylesheet URL from its hashed filename.

### 6.2 Interaction states

Capture and document representative states for the agreed scope:

- Expanded and collapsed account summary.
- Character selection and character detail navigation.
- Expandable character equipment details.
- Exploration card or region expansion, if supported.
- Current/previous cycle switching for the relevant game modes.
- Any detail panel, tab, carousel, or bottom-sheet behavior that materially changes the layout.

Record only behavior actually observed. If a control is decorative or navigation-only, distinguish it from an expandable control.

### 6.3 Screenshot measurements

Capture screenshots at the reference mobile viewport and note viewport dimensions where available. Use screenshots and DevTools computed/layout values together. Do not infer exact dp values from CSS pixels without accounting for viewport scaling and device density.

## 7. Translation into Irminsul Compose

The eventual implementation should translate the observed patterns into Irminsul's existing design system:

- Use existing theme tokens; do not scatter copied raw colors through feature composables.
- Follow the established 4dp/8dp spacing scale and concentric-radius rule in the UI design instructions.
- Use shared section headers and metric primitives only where they improve consistency.
- Use native Compose expansion, selection, scrolling, and sheets.
- Preserve local-first rendering and the current account data model.
- Keep missing, unavailable, stale, and actual zero values distinct.
- Use only account fields that are present and validated. Do not synthesize chest counts, exploration values, or game-mode records.
- Keep Real-Time Notes on Home only.
- Continue using the app's existing approved dynamic game-art URL and image-cache flow. This design reference does not create a new asset pipeline.
- Adapt the reference to Irminsul's clean Android utility direction with subtle Genshin archival flavor; do not copy every web decoration or gradient.

## 8. Privacy and handling of the raw capture

The current `docs/share-page.html` contains account-specific profile information, including a UID. The file should not remain publicly accessible in that form.

Before treating the capture as a durable repository artifact, sanitize account-specific values and review embedded image URLs and other identifiers. Keep the unsanitized capture private and local if it is still needed for research. This analysis intentionally omits the actual UID, nickname, and account values.

## 9. Reference sources

- Captured DOM: [`docs/share-page.html`](share-page.html)
- UI design and reconstruction rules: [`Irminsul Codex UI Design Instructions.md`](Irminsul%20Codex%20UI%20Design%20Instructions.md), especially Section 10.
- Official HoYoLAB description of Battle Chronicle modules: [HoYoLAB: Exclusive Game Tools Introduction](https://www.hoyolab.com/article/1680746). This is general product context, not evidence for the current page's exact layout.
- Official Battle Chronicle update example: [HoYoLAB: Battle Chronicle Update](https://www.hoyolab.com/article/2002956). This documents historical feature behavior and should not be treated as a current UI specification.

## 10. Research completion checklist

- [x] Retrieve and inspect captured `.share-page` outerHTML.
- [x] Identify major page sections and reusable DOM class groups.
- [x] Record initial account-summary, character, exploration, and game-mode presentation patterns.
- [x] Separate the agreed Irminsul scope from unrelated modules visible in the source.
- [x] Record limitations of a rendered DOM snapshot.
- [ ] Obtain the exact stylesheet URL and inspect the CSS.
- [ ] Capture representative interaction states.
- [ ] Capture reference screenshots and reliable viewport/layout measurements.
- [ ] Map observed source data fields to the Irminsul account model before implementation.
- [ ] Prepare a section-by-section native Compose implementation plan.
- [ ] Sanitize or otherwise restrict access to the account-specific raw HTML capture.
