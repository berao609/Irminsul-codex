# Battle Chronicle Reference Analysis

**Project:** Irminsul Codex  
**Purpose:** Preserve development-time observations from the HoYoLAB Genshin Impact Battle Chronicle reference for later native Jetpack Compose implementation.  
**Status:** HTML and stylesheet captures reviewed; representative interaction behavior and seven-page screenshot reference reviewed. Detailed interaction coverage and exact layout measurements remain partial.  
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

**Observed interaction:** Selecting a different portrait switches the displayed character while remaining on the Character Details page. The selector does not require a full-page navigation.

**Irminsul direction:** Keep the roster navigable and compact. The selected character should reveal details progressively. Preserve the existing Irminsul requirement that weapon and artifact details are inspectable on demand rather than dumping every detail into one long page.

### 4.3 World exploration

The HTML contains a dedicated exploration section with multiple regional cards. Class groups indicate area names, progress sections, progress values, and card imagery/backgrounds. Inline styles show region-specific colors and progress widths in the captured state.

**Screenshot-confirmed overview (reference PDF, page 3):** The Home/Battle Chronicle overview presents World Exploration as a horizontal carousel of region cards. Each visible card contains a prominent region emblem, region name, the label `Main Areas Only`, a percentage, and a horizontal progress bar. A `View More` action opens the dedicated World Exploration page.

**Screenshot-confirmed detail page (reference PDF, page 4):** The dedicated page presents larger vertically stacked region cards. Each card includes the region emblem and name, a relevant progression descriptor and level where available (for example, Statue of The Seven level or an offering-system level), plus an exploration percentage and progress bar. The screenshot shows different visual backgrounds per region/card. A small downward indicator is visible at the bottom of cards; its exact interaction was not tested and must not be assumed to expand anything.

**Observed interaction coverage:** No individual exploration card control was exhaustively tested. The user described the overview as simple region-name slides with a percentage bar, consistent with the screenshot. Do not spend further research time testing every small control unless a specific implementation question requires it.

**Irminsul direction:** Use a compact horizontal region-card carousel in the overview and a vertically stacked card list on the dedicated exploration page. Show only source-backed region progression fields. Do not hardcode the captured account's percentages or progression levels, and do not treat screenshot-specific colors as universal theme tokens.

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
- [x] Obtain the exact stylesheet URL and inspect the CSS.
- [x] Capture representative interaction states: character selection switches in place; tested equipment/talent/constellation links navigate to HoYoLAB Wiki. Detailed control coverage intentionally limited.
- [x] Review the supplied seven-page screenshot reference for major layouts and section hierarchy. Exact viewport measurements and dp conversion remain pending.
- [ ] Map observed source data fields to the Irminsul account model before implementation.
- [ ] Prepare a section-by-section native Compose implementation plan.
- [ ] Sanitize or otherwise restrict access to the account-specific raw HTML captures (`share-page.html` and `body-detail.html`) before public repository use.


## 6.4 Retrieved stylesheet analysis

The stylesheet was obtained from the exact browser Network request URL:

`https://act.hoyolab.com/app/community-game-records-sea/m_55ae43ba.css`

Repository copy: [`docs/m_55ae43ba.css`](m_55ae43ba.css)

- Git blob SHA: `c7cf50e27b1007d9e78697731155ed67659535f9`
- File size: 113,102 bytes in the repository listing.
- Retrieved text length: 113,028 characters.
- Parsed rule count: 1,145 CSS rules.
- The file is minified into a small number of long lines.
- The stylesheet begins with a miHoYo copyright notice. It is retained as a research reference only; do not copy its source code into Irminsul.

### Confirmed styling patterns

1. **Viewport-scaled measurements:** Many dimensions use `calc(N * var(--gpx))`. The page's global `--gpx` scale variable is therefore central to the original mobile layout. Treat these as reference CSS units, not Android dp values. Measure the rendered viewport and convert deliberately.
2. **Navigation geometry:** The navigation container uses flex alignment, space-between distribution, and approximately 10 gpx vertical / 16 gpx horizontal padding. Navigation icons are 24 gpx square. The centered title is 16 gpx, weight 600, with a 24 gpx line height.
3. **Gold identity accent:** For game ID 2, the navigation title uses `#d3bc8e`, with subtle letter spacing. This supports the warm archival-gold visual identity already selected for Irminsul, but should be mapped to a theme token rather than copied as a raw value.
4. **Dark base surfaces:** The root layout and some dialog surfaces use dark charcoal values such as `#2a2a2a`, `#333`, and `#0d0e12`. Other dialogs use a pale parchment-like surface `#f6f3ed`, with warm borders and muted brown text. The source uses both dark and light dialog treatments, so surface color is component/state-specific rather than one universal card color.
5. **Typography:** The stylesheet repeatedly uses Microsoft YaHei / sans-serif for dialog content, with explicit font sizes and line heights. Examples include 16/24 gpx titles and 12/16 gpx supporting text. Do not assume these are the exact fonts used by every page section.
6. **Character avatar overlays:** `.avatar-container` is positioned relatively. Constellation badges are placed at the top-right, use white semibold 12 gpx text, an 18 gpx line height, and a translucent black background. Avatar labels sit below with an 8 gpx top margin. This is a useful observed pattern for compact roster tiles.
7. **Detail dialogs:** Skill, rune, buff, and generic dialog selectors define compact fixed-width panels, rounded corners, inset borders, close controls, scrollable descriptions, and decorative corner/background images. These are candidate references for detail inspection, but their actual trigger and relevance to the captured Battle Chronicle modules must be verified through interaction testing.
8. **Utility classes:** The file includes many generated utility selectors (including `tw-` prefixed width, height, spacing, text, background, border, and positioning classes). Their presence indicates utility-class styling is mixed with component-scoped CSS.

### Interpretation limits

- The stylesheet is a shared page bundle, not a clean stylesheet dedicated only to the captured `.share-page` node. It contains styles for navigation, dialogs, popovers, verification/error states, and other components. A selector appearing in the file does not prove it is visible in the captured page state.
- Vue scoped-style attributes such as `data-v-...` identify component-scoped rules. They are useful for grouping related rules, but they do not expose the original Vue component source.
- The captured DOM and stylesheet together provide stronger evidence than either alone. Computed styles and interaction states are still needed before recording exact section/card geometry.
- Colors and dimensions above are examples explicitly present in CSS. They are not a complete design-token inventory and should not be applied indiscriminately.

### Next stylesheet-related work

- Cross-reference additional selectors against the captured DOM and the visible sections in the approved scope.
- Inspect computed styles on representative account summary, exploration, character, Abyss, Theater, and Stygian elements.
- Capture interaction states before assigning dialog styles to a particular user action.
- Measure the relevant layout in the actual mobile viewport. Do not infer Android dp values directly from `gpx`.



## 6.5 Individual character detail capture

A complete rendered `<body>` capture from the Character Details page was uploaded to the repository:

- File: `docs/body-detail.html`
- SHA: `18ba0399a4ef55a1b6e38b627c0e600ff4672a1c`
- Captured source begins with the page's `<body data-game-id="2"...>` and contains the `.share-container.role-detail-layout` subtree.
- The capture includes the character selector, character presentation, base information, constellation icons, attribute statistics, weapon section, artifact/relic section, talent icons, and other page markup.
- The captured page is a rendered DOM snapshot. It does not prove the original component architecture or all runtime behavior.

### Character detail structure observed

- `.header-navbar`: page header titled Character Details.
- `.bgs`: character-specific background region, including gradient/video-related markup.
- `.list-swiper`: horizontal character portrait selector.
- `.role-detail-container` and `.base-info-section`: main character presentation and identity.
- `.constellations` and `.constellation-icon`: six constellation icon entries.
- `.info-panel`, `.top-info`, and `.base-info`: character identity and base information.
- `.prop-info-section`, `.prop-stats`, `.base-stat`, `.add-stat`, and `.final-stat`: character attribute presentation.
- `.weapon-info-section`: weapon information.
- `.relic-info-section`, `.relic-list`, `.relic-item`, `.relic-top`, and `.relic-icon`: five-slot artifact presentation and related stats.
- `.skill-item` and `.skill-icon`: talent presentation.
- `.gt-modal` / `.gt-popup-root`: modal-related markup exists, but its relationship to the tested icon actions is not established.

### Character detail interactions actually tested

| Element | Observed behavior |
|---|---|
| Character portrait selector | Selecting another portrait changes the displayed character in place on the same Character Details page. |
| Weapon entry | Redirects to the HoYoLAB Wiki. |
| Artifact entry | Redirects to the HoYoLAB Wiki. |
| Constellation icons | Redirect to the HoYoLAB Wiki. |
| Talent icons | Redirect to the HoYoLAB Wiki. |

The tested weapon, artifact, constellation, and talent controls are external Wiki navigation, not in-page expandable inspectors. Do not copy that navigation behavior into Irminsul's character showcase: Irminsul's separately approved interaction is local progressive disclosure, where tapping a weapon or artifact reveals its information in the app. This is an intentional product difference, not an attempt to reproduce the reference's external links.

The user explicitly stopped exhaustive one-by-one control inspection. Treat the table above as the representative interaction sample. Do not continue testing every icon or control without a concrete unresolved implementation question.

## 6.6 Screenshot reference review

A seven-page screenshot reference was supplied in the conversation on 2026-11-26. It is a visual source for this analysis; it is not currently a repository file. Page descriptions below refer to the uploaded PDF's page order.

- **Page 1 - All Characters:** Four-column portrait grid; character cards show portraits, element indicators, level labels, and character names. A sort control (shown as High → Low Level) and Filter action appear above the grid. A reference-only data disclaimer is visible.
- **Page 2 - Common Tools and Stygian Onslaught:** Common Tools appears above a Stygian Onslaught Battle Records Review card. The record card has Current Lunar Phase / Previous Lunar Phase tabs, period context, best-record summary, per-challenge time elapsed, strongest single strike, highest total damage, and a View Challenge Details action. Theater Lobby begins below. Common Tools is outside the agreed Irminsul Visuals scope; the Stygian record presentation is relevant.
- **Page 3 - World Exploration overview and Event Calendar:** World Exploration is a compact horizontal region-card carousel with a View More action. Each region card shows an emblem, region name, Main Areas Only label, percentage, and progress bar. Event Calendar and Event Wishes appear below but are excluded from the agreed reconstruction scope.
- **Page 4 - World Exploration detail page:** Large vertical region cards show region identity, progression descriptor/level where available, exploration percentage, and a progress bar. Examples of progression descriptors visible include Statue of The Seven, Temple of Space/Memory Core, and offering-system level. The screenshot shows region-specific background artwork/color treatments.
- **Page 5 - Character Details:** A large character artwork/background presentation, compact horizontal character selector, character identity/level/friendship, constellation and talent icon rows, Character Stats panel, and the start of the Weapons section.
- **Page 6 - Account overview and My Characters:** Profile identity and account summary metrics appear first. Visible metrics include Days Active, Achievements, Characters, Spiral Abyss, Max Friendship Characters, Imaginarium Theater, and Stygian Onslaught. A grid of collectibles/progression counts follows, including waypoints, domains, oculi categories, and chest categories. My Characters appears as a horizontal portrait selector with a compact selected-character equipment card.
- **Page 7 - Account overview and Real-Time Notes:** The account summary is shown in a more compact/collapsed state above My Characters. Real-Time Notes appears below with Resin as the primary metric and a supporting list for expeditions, Realm Currency, Daily Commission rewards/progress, and weekly progress. This confirms the reference placement of Real-Time Notes on its own account/Home surface; Irminsul's agreed rule remains that Real-Time Notes are on Home only and must not be duplicated in Visuals.

### Additional screenshot-derived layout observations

- The page uses a dark charcoal overall background with warm gold section headings and restrained decorative star/flower markers.
- Account summary metrics are arranged in a compact multi-column grid of small cards, with a visible expand/collapse affordance between the summary and lower content.
- My Characters uses a compact portrait strip and a pale selected-character equipment card. The selected card summarizes character level, weapon, and five artifact icons/levels rather than showing the full character detail page.
- The All Characters page uses a denser multi-column grid than the compact My Characters strip.
- The screenshots are evidence of one captured account and one UI state. Do not copy account-specific values, names, UID, or progression figures into product defaults or analysis examples. Do not infer exact Android dp dimensions from screenshot pixels.

## 6.7 Research decisions and remaining coverage

- The CSS file and representative rendered HTML captures are available in the repository; a representative Character Details body capture is also present.
- The seven-page screenshot reference has been reviewed for major page structure and visual hierarchy.
- Character selector behavior and representative outbound Wiki-link behavior have been tested.
- World Exploration overview and detail-page layouts are screenshot-confirmed.
- Detailed interaction coverage is intentionally limited. Do not turn this research into an exhaustive audit of every icon, link, or decorative control.
- Still useful if implementation requires it: inspect one representative game-mode detail destination (Abyss, Theater, or Stygian) and capture computed dimensions for a small number of high-value components. These are optional targeted follow-ups, not prerequisites to keep exploring the current character page.
- Before any public repository release, review and sanitize account-specific information in `docs/share-page.html` and `docs/body-detail.html`. The screenshot reference also contains account-specific information and should not be committed publicly in raw form.


## 8. Related HoYoLAB tool: Enhancement Progression Calculator

This section records a separate HoYoLAB calculator page explored after the Battle Chronicle reference. It is related reference research, not part of the Battle Chronicle page itself.

### 8.1 Page capture

- File: `docs/enhancement-calculator-body.html`
- SHA: `30e2221e2d5e94ee7c5a2f31578ce465ce006a6c`
- Captured page title: **Genshin Impact Enhancement Progression Calculator**
- The captured DOM contains the calculator landing page, profile header, quick-leveling module, gameplay-based leveling entry, custom calculation entry, and modal/loading markup.
- The page capture includes account-specific profile information. Do not copy that information into implementation examples or expose it unnecessarily; sanitize before public distribution.

### 8.2 Landing-page structure observed in the capture

1. **Quick Character Leveling**
   - Subtitle: Check characters that can be leveled up based on materials in Inventory.
   - Entry options: `Level up based on characters` and `Recommended Inventory Materials`.
2. **Level up based on the gameplay**
   - Entry: `Spiral Abyss/Imaginarium Theater`.
3. **Custom Calculation**
   - Character: select 1–8 characters; weapons are optional.
   - Weapon: select 1–8 weapons.
   - Serenitea Pot: select furnishings; replica importing is supported.
   - Artifact: select 1–5 artifacts.

**Irminsul scope decision:** Exclude Artifact and Serenitea Pot custom calculations. The user explicitly does not want these modules included. The observed HoYoLAB options are reference-page features only, not requirements for Irminsul.

These are labels and options visible in the captured HTML. Their calculation logic and full subsequent screens have not been verified.

### 8.3 Interaction observations

- User opened `Level up based on characters`.
- **Observed result:** the calculator displays a list of the user's owned characters for selection.
- Selection behavior after choosing a character, material calculation details, and subsequent screens remain unverified.

### 8.4 Research boundary

Continue by selecting one owned character and documenting the next screen. Focus on the workflow, selected inputs, required materials, totals, and useful controls. Avoid exhaustively inspecting every decorative element.


### 8.4 Custom character calculation and material balance

**User-observed workflow:** In the Custom Calculation page, the user can select a character's target character level and target levels for the character's individual talents. After proceeding to the result, the calculator displays the total materials required and material-balance information, including how much is still needed and how much is in excess.

This establishes the following useful output concepts:

- Total required quantity per material for the selected target levels.
- Remaining quantity still needed after accounting for available inventory.
- Excess quantity when available inventory exceeds the requirement.

The exact labels, grouping, sorting, and numerical formulas have not been independently inspected. Treat the above as user-reported behavior, not a verified DOM-level specification.

### 8.5 Inventory synchronization and Irminsul scope decision

HoYoLAB automatically synchronizes the user's in-game inventory with its calculator. The user explicitly decided **not** to reproduce this automatic synchronization in Irminsul Codex.

**Irminsul implementation direction:**

- Do not introduce real-time or automatic in-game inventory synchronization for this calculator.
- Keep material calculations local and deterministic.
- Use inventory quantities already available in the app's local account data, where present, and/or allow users to enter or adjust quantities manually.
- Clearly distinguish required, owned/available, remaining, and excess quantities.
- Do not make the calculator depend on a live HoYoLAB connection or a recurring remote sync.

This is an explicit product-scope decision, not a claim that HoYoLAB's synchronization mechanism has been technically reverse-engineered.


### 8.6 Custom weapon calculation

**User-observed workflow:** The user selects a weapon, specifies its target level, and the calculator displays the total count of materials required for that target.

The following details are confirmed at the workflow level:

- Weapon selection precedes target-level selection.
- The result presents the total required material quantities.

The exact material categories, per-material inventory balance, and whether the weapon result screen also displays remaining and excess quantities have not been separately confirmed. Do not assume the character result's full balance presentation is identical for weapons without further observation.

**Irminsul direction:** Reuse the local deterministic material-requirement calculation and result presentation patterns where appropriate, while keeping weapon requirements distinct from character and talent requirements. No automatic inventory synchronization.


### 8.7 External item and material reference

**Product decision:** Use **HoYoWiki as the primary external reference** for explaining characters, weapons, materials, and their uses. Offer the community-maintained Genshin Impact Wiki as a fallback when a HoYoWiki entry is missing or insufficient. The app does not need to reproduce either Wiki's encyclopedia content locally.

**Suggested interaction:** Make a material/resource name or its detail affordance open the corresponding HoYoWiki entry. Prefer a direct item-specific page when the mapping is known; use the community Wiki as a fallback. Keep the calculator's required quantities and inventory arithmetic inside Irminsul.

**Boundary:** Wikis are explanatory reference content, not a calculation engine or runtime data dependency. Do not require live Wiki access for character/weapon material calculations. If a page cannot be opened, the calculator must still work.

**Reference checked:** The community Genshin Impact Wiki provides item/material classification and dedicated material pages, including character talent materials and character/weapon enhancement materials:
- [Genshin Impact Wiki](https://genshin-impact.fandom.com/wiki/Genshin_Impact_Wiki)
- [Material](https://genshin-impact.fandom.com/wiki/Material)
- [Character Talent Material](https://genshin-impact.fandom.com/wiki/Character_Talent_Material)
- [Character and Weapon Enhancement Materials](https://genshin-impact.fandom.com/wiki/Category:Character_and_Weapon_Enhancement_Materials)

These pages confirm the Wiki is suitable as an explanatory reference. Exact URL resolution for each in-app resource should be handled deliberately and tested; do not construct arbitrary URLs from untrusted display strings.


### 8.8 Explicitly excluded custom calculation modules

The user explicitly decided to exclude the following HoYoLAB calculator modules from Irminsul Codex:

- **Artifact custom calculation** (the option to select 1–5 artifacts).
- **Serenitea Pot furnishing calculation** (including replica importing).

Do not implement or plan these as part of the Irminsul enhancement calculator unless the user later changes this scope decision. Character and weapon enhancement calculations remain in scope.

## 9. Consolidated analysis of the five repository reference captures

**Analysis date:** 2026-11-27  
**Purpose:** Consolidate the CSS, account overview captures, character detail capture, and enhancement-calculator landing capture into one retrieval point for the native UI revamp. This section supplements Sections 2–8; it does not replace their screenshot observations, interaction records, or product decisions.

### 9.1 Source inventory and evidence boundaries

| File | Retrieved blob SHA | Size | What it represents |
|---|---|---:|---|
| `docs/m_55ae43ba.css` | `c7cf50e27b1007d9e78697731155ed67659535f9` | 113,028 characters | Minified shared HoYoLAB mobile stylesheet; 1,145 parsed CSS rules |
| `docs/share-page.html` | `40c5fee0766a722eb22efc6ce7213310797ee29e` | 116,408 characters | Full captured Battle Chronicle share-page subtree and page shell |
| `docs/index-page-info.html` | `c4ed98716c085081e22b842eeaee9a1048ea610d` | 106,467 characters | The main account-information content subtree without the complete outer share-page wrapper |
| `docs/enhancement-calculator-body.html` | `30e2221e2d5e94ee7c5a2f31578ce465ce006a6c` | 83,422 characters | Enhancement Progression Calculator landing page, profile header, entry modules, and modal/loading shells |
| `docs/body-detail.html` | `18ba0399a4ef55a1b6e38b627c0e600ff4672a1c` | 78,824 characters | Rendered Character Details page body, including selector, stats, weapon, artifacts, talents, and constellations |

All four HTML files are rendered DOM snapshots, not original application source code. The CSS is a shared bundle, not a stylesheet dedicated only to these captured elements. Class names, markup, and CSS rules are evidence of rendered structure and styling; they do not independently prove original component architecture, data contracts, or unobserved interaction behavior.

The HTML captures contain account-specific profile and game data, including identifying account fields. Do not reproduce those values in UI examples, fixtures, documentation, screenshots, or logs. Review and sanitize the raw captures before any public distribution; the existing public-repository exposure should be treated as a data-hygiene issue.

### 9.2 Stylesheet: visual language and reusable patterns

The stylesheet contains 1,145 parsed rules and is heavily minified. It combines component-scoped rules, generated utility classes, shared controls, dialogs, popovers, navigation, and error/loading components. A selector in this shared file is not proof that the corresponding component appears in a particular captured screen.

**Confirmed visual characteristics**

- **Base canvas:** `.m-root-layout` uses a full-viewport minimum height and `#2a2a2a` background.
- **Primary identity accent:** `#d3bc8e` is a recurring warm gold. Related lighter gold values include `#ddca9f`. This aligns with Irminsul's approved Archival Gold direction; map to shared theme tokens rather than scattering raw values.
- **Dark surfaces:** Examples include `#2a2a2a`, `#333`, and `#0d0e12`. Some surfaces use translucent white borders or overlays rather than heavy opaque outlines.
- **Warm light surfaces:** Dialog variants use parchment-like `#f6f3ed`, warm border tones such as `#dac9ad` / `#e2d4b7`, and muted brown text such as `#675856` / `#9c7f69`.
- **Cool secondary surface:** A tooltip/popover surface uses `rgba(44, 50, 61, 1)`, 16 gpx corner radius, 12 gpx padding, and a 5 gpx backdrop blur. Its arrow is hidden.
- **Typography:** Language-specific font stacks are defined. English uses Tahoma/Helvetica/Arial/sans-serif; other locales use different stacks. Dialog content includes Microsoft YaHei rules, while selected page text uses HYWenHei. Do not assume one font family applies to the whole application.
- **Scale system:** Many dimensions are expressed as `calc(N * var(--gpx))`; some font sizes use viewport-width units. These are reference-specific responsive units, not Android dp or sp.
- **Common geometry:** Repeated values include 24 gpx icons, 4/8/10/12/16/20 gpx spacing increments, 7 gpx dialog radii, 16 gpx popover radii, and 285 gpx dialog widths. These describe individual web rules, not universal design tokens.
- **Avatar overlays:** The avatar container clips its image; a constellation badge sits at the top-right with a translucent black background, white semibold 12 gpx text, and compact padding. This is a strong pattern for dense roster/avatar presentation.
- **Dialog construction:** Some dialogs use inset borders, decorative corner/background images, a close affordance positioned outside the panel, and a scrollable description region. Skill dialogs use a dark surface; rune/buff/announcement variants use warm light surfaces. This confirms contextual dialog styling, not a single universal dialog theme.
- **Empty/error states:** Shared CSS includes centered empty-state and error layouts, muted text, and bounded content widths. These are useful state-design references but their exact visual states are not all present in the HTML captures.

**Native translation:** Preserve contextual contrast, gold hierarchy, compact game-data typography, avatar overlays, and purposeful decorative framing where it serves the screen. Rebuild using Compose theme roles, native surfaces, image clipping, badges, and sheets/dialogs. Do not port CSS selectors, gpx calculations, web fonts, CSS blur, or decorative image-border mechanics as literal implementation requirements. Verify actual Compose dp/sp geometry on-device.

### 9.3 Battle Chronicle overview: share-page and index-page-info

The two captures represent overlapping content at different DOM boundaries:

- `share-page.html` includes the outer `.share-page` / `.m-index-layout` composition and top-level visual/page-shell elements, including the large `.game-kv` hero region.
- `index-page-info.html` begins at `.m-index-page-info` and isolates the account-information body. It is useful for examining the information hierarchy without the full page wrapper.
- Both contain substantially the same populated account-information sections and repeated component patterns. Treat them as complementary captures, not two separate product designs.
- The main account content includes Character, World Exploration, Event Calendar, Tools, Challenge, Spiral Abyss, TCG, and Serenitea-related anchors. Existing product-scope exclusions in Section 1 remain binding; the presence of a section in the source does not authorize its implementation.

**Observed content hierarchy and presentation**

1. **Account identity and compact summary:** A profile/identity region precedes grouped account metrics. The visible summary distinguishes headline account facts from secondary metrics. Existing screenshot research confirms the summary can be presented in compact and expanded forms.
2. **My Characters:** A compact horizontal character selector is distinct from the denser All Characters destination. The overview presents a selected character with condensed equipment context rather than the complete detail page.
3. **World Exploration:** Region cards use region-specific artwork/emblems, region names, a progression qualifier such as Main Areas Only, percentage values, and progress bars. The overview is a compact horizontal carousel; the dedicated detail page is a larger vertically stacked presentation. The HTML contains region-specific content and styling; do not convert captured percentages into defaults.
4. **Challenge modes:** Spiral Abyss, Imaginarium Theater, and Stygian Onslaught each have their own period/record context and mode-specific summary data. Shared section framing is reasonable; forcing identical metric layouts across different data models is not.
5. **Section headers:** Repeated `.block-title` patterns pair a small icon and title, sometimes with a trailing View More action. This supports one reusable native section-header behavior with an optional trailing action.
6. **Collectibles/progression:** Exploration and collectible counts are grouped by meaningful categories. Preserve the distinction between counts, percentages, mode records, and character progression instead of turning all values into interchangeable metric tiles.
7. **Excluded reference content:** Event Calendar, Common Tools, TCG, and Serenitea Pot remain outside the agreed Visuals reconstruction scope. Real-Time Notes remain Home-only under the Irminsul product decision.

**Native translation:** Keep the compact account summary expandable; use a horizontally browsable character strip and region overview; retain distinct region identity; use a dedicated region-detail composition; group collectibles semantically; and use consistent section headers. Avoid a generic wall of cards, duplicating Home-only Real-Time Notes, or adding reference-only modules.

### 9.4 Character Details page: composition and data hierarchy

The rendered body uses `.role-detail-layout` and contains these major regions:

- Header/title region for Character Details.
- Character-specific background/presentation region (`.bgs`, `.role-detail-container`, `.base-info-section`).
- Horizontal character selector (`.list-swiper`).
- Character identity and base information (`.info-panel`, `.top-info`, `.base-info`).
- Constellation presentation (`.constellations`, `.constellation-icon`).
- Character attributes (`.prop-info-section`, `.prop-stats`, `.base-stat`, `.add-stat`, `.final-stat`).
- Weapon information (`.weapon-info-section`).
- Five artifact/relic entries (`.relic-info-section`, `.relic-list`, `.relic-item`, `.relic-top`, `.relic-icon`).
- Talent presentation (`.skill-item`, `.skill-icon`).

**Data hierarchy visible in the capture**

- Character identity is established before the dense statistics.
- Character stats distinguish base contribution, additional contribution, and final total. This is a useful semantic distinction: do not flatten base and additive values into one unexplained number.
- The sample includes both percentage and flat stats. Labels and values must remain paired and units must remain visible.
- Weapon information separates weapon identity/level, Base ATK, and its secondary stat.
- Each artifact is represented as an individual slot/item with its own main affix and minor/substat values. Artifact slot identity and item identity must not be lost when rendering a compact list.
- Talent and constellation icons are separate groups; they should not be mistaken for ordinary navigation tabs.

**Interaction evidence and product difference**

The previously tested behavior is recorded in Section 6.5: selecting another character portrait updates the displayed character in place. Weapon, artifact, constellation, and talent entries in the reference navigate externally to HoYoLAB Wiki. Irminsul intentionally differs: its approved character showcase uses local progressive disclosure for weapon/artifact inspection. Do not copy the external-link behavior as the primary detail interaction.

**Native translation:** Keep character artwork and identity prominent; retain the compact horizontal selector; present stats in a structured panel with base/additive/final semantics; group weapon and five artifact slots distinctly; and reveal secondary equipment information locally when requested. Preserve existing Irminsul behavior and data availability semantics. Do not fabricate missing stats or equipment.

### 9.5 Enhancement Progression Calculator: what this capture proves

The captured file is the calculator's **landing page**, not a full capture of the character/weapon custom-calculation form or result page. Its visible modules are:

- **Quick Character Leveling:** A helper description says it identifies characters that can be leveled based on inventory materials. It offers a character-based route and a recommended-inventory-material route.
- **Level up based on the gameplay:** An entry for Spiral Abyss / Imaginarium Theater.
- **Custom Calculation:** Separate entry points for Character, Weapon, Serenitea Pot, and Artifact. The page labels the selection capacity: 1–8 characters, optional weapons alongside the character route, 1–8 weapons, 1–5 artifacts, and furnishing/replica selection for Serenitea Pot.
- **Profile header:** The landing screen includes a signed-in profile identity region. Do not reuse captured account values.
- **Modal/loading infrastructure:** The snapshot includes popup/modal roots and loading markup, but not the actual contents or transitions of all those dialogs.

**Evidence limits:** No standard form controls were present in the captured DOM snapshot. The landing capture does not establish the detailed character selector UI, target-level controls, material-row design, calculation formula, result grouping, or exact button sequence. The workflow-level observations and decisions already documented in Sections 8.3–8.8 remain the evidence for later stages. Do not infer the unobserved screens from this landing capture.

**Irminsul translation and locked scope**

- Keep character and weapon enhancement calculations as the supported calculator domains.
- Exclude Artifact and Serenitea Pot custom calculations unless the user explicitly changes scope.
- Use the landing page's task separation as a workflow reference, not a requirement to clone all its modules.
- Preserve deterministic local calculations and the existing decision against automatic in-game inventory synchronization.
- Keep target level selection and character talent targets explicit; aggregate all selected requirements into a comprehensible material result.
- Distinguish total required, owned/available, remaining, and excess quantities where the applicable workflow supports those values.
- Keep character ascension, character EXP, talent, and weapon enhancement requirements logically distinct even when the result combines them into one material summary.
- Item-specific Wiki links are explanatory only; the calculator must continue working without network access to a Wiki.

### 9.6 Cross-reference synthesis for the native UI revamp

| Native destination/component | Strongest reference | Design intent to preserve | Important boundary |
|---|---|---|---|
| Home/account identity and summary | Battle Chronicle overview captures | Clear identity, compact headline metrics, secondary expandable facts | Home owns Real-Time Notes; never duplicate them in Visuals |
| Visuals account overview | `share-page.html` + `index-page-info.html` | Dense but grouped progression summary and clear section hierarchy | Only approved Visuals modules |
| Character roster/showcase | Overview captures + `body-detail.html` | Character-first artwork, horizontal selector, meaningful selected state | Keep current Irminsul roster decisions and local detail interactions |
| Character stats | `body-detail.html` | Base/additional/final value hierarchy and readable stat groups | Do not fabricate unavailable values |
| Weapon/artifact details | `body-detail.html` | Separate weapon identity and five artifact slots, item-specific stats | Irminsul local progressive disclosure intentionally differs from Wiki links |
| World Exploration | Overview captures + CSS | Region identity, contextual card treatment, progress percentage/bar, overview/detail scale change | Use current account data; no captured-account defaults |
| Game-mode records | Overview captures and existing screenshot notes | Mode-specific records, periods, best result, and meaningful metrics | Do not normalize distinct modes into one false schema |
| Materials Calculator | `enhancement-calculator-body.html` + Section 8 workflow observations | Separate task entry points and clear character/weapon calculation intent | Landing capture is not evidence of detailed form/result layout; exclude Artifact/Pot |
| Theme and shared components | `m_55ae43ba.css` | Archival gold, charcoal and warm parchment contrast, compact geometry, overlays | CSS/gpx values are not Compose tokens or dp measurements |

### 9.7 Retrieval guidance for future implementation batches

Use this analysis as the first reference lookup. Retrieve the raw captures again only when a concrete unresolved question requires source-level inspection.

- Account summary, overview, and section hierarchy: Sections 3, 4.1, 4.3–4.5, 6.6, and 9.3.
- Character selector, stats, weapon, artifacts, talents, and constellations: Sections 6.5, 9.4, and the raw `docs/body-detail.html` only for unresolved markup detail.
- CSS colors, typography, dimensions, avatar overlays, and dialog variants: Sections 6.4 and 9.2; inspect the raw CSS only for a specific selector/rule.
- Calculator scope and landing-page modules: Sections 8.1–8.8 and 9.5.
- Product boundaries and exclusions: Sections 1, 8.5, 8.7, 8.8, and the current UI Design Instructions.

**Implementation rule:** This document is design research, not permission to change product scope. The current `docs/Irminsul Codex UI Design Instructions.md`, approved UI revamp decisions, native architecture, and validated account data remain authoritative. Reconcile conflicts in favor of explicit Irminsul decisions, not the reference website.

**Remaining evidence gaps:** Exact computed dimensions for the account overview and individual cards; full responsive breakpoint behavior; and detailed calculator form/result DOM captures. These are not prerequisites for continuing the current revamp unless a specific implementation decision depends on them. Do not conduct exhaustive control testing or invent missing measurements.
