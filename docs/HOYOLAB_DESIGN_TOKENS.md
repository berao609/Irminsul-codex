# HoYoLAB Design Tokens (WebView Extract Reference)

> **Approved scale policy:** Treat **1 `--gpx` = 1.dp** for layout dimensions, spacing, padding, radii, borders, and icon sizes; treat **1 `--gpx` = 1.sp** for typography (`font-size`, `line-height`, and `letter-spacing`). This is an explicit Android baseline policy, not a claim that the original runtime's `--gpx` variable was recovered. Every converted value below preserves its raw `--gpx` coefficient so the layer can be rescaled centrally later.
>
> **Conversion:** for a CSS declaration `calc(N * var(--gpx))`, Android layout token = `N.dp`; typography token = `N.sp`. For fixed CSS pixel declarations in these extracts that do not use `--gpx`, this reference records the raw value separately and does not silently reinterpret it as a `--gpx` coefficient. Percentages, viewport units, `rem` values, and data-driven inline colors remain contextual rather than global tokens.

## Scope and evidence

Inspected these five files on PR #17 branch `ui-revamp/batch-9-character-weapon-fidelity`:

- `docs/m_55ae43ba.css`
- `docs/index-page-info.html`
- `docs/body-detail.html`
- `docs/enhancement-calculator-body.html`
- `docs/share-page.html`

The HTML files are extracted page fragments. The CSS does not define `--gpx`; the approved scale policy above supplies the Android mapping. The calculator extract is its landing/body fragment, not the calculator's character-selection, weapon-selection, or material-detail sub-pages. Those sub-page entries below are explicitly marked **shared/inferred**, not directly observed.

## Typography

All values in the Android column follow the approved 1:1 `--gpx` → `sp` policy.

| Role / observed use | CSS class or selector in source | Raw CSS value | Android token |
|---|---|---:|---:|
| Navigation/page title | `.title[data-v-4b6b351e]` | `font-size: 16*gpx; line-height: 24*gpx; weight 600` | 16.sp / 24.sp / SemiBold |
| Game title tracking | `body[data-game-id="2"] .title[data-v-4b6b351e]` | `letter-spacing: .1*gpx`; color `#d3bc8e` | 0.1.sp; `#D3BC8E` |
| Base body / common text | `.tw-text-base-m` | 16*gpx / 20*gpx | 16.sp / 20.sp |
| Small body | `.tw-text-sm-m` | 14*gpx / 18*gpx | 14.sp / 18.sp |
| Compact body | `.tw-text-xs-m`, `.tw-text-xs` | 12*gpx / 16*gpx | 12.sp / 16.sp |
| Caption / micro label | `.tw-text-xxs-m` | 10*gpx / 12*gpx | 10.sp / 12.sp |
| Explicit compact label | `.tw-text-[9px]` | 9*gpx; paired in markup with `.tw-leading-[10px]` | 9.sp / 10.sp |
| Explicit micro label | `.tw-text-[8px]` | 8*gpx | 8.sp; line height not specified by this class |
| Section/number emphasis | `.tw-text-[18px]` | 18*gpx | 18.sp; line height supplied by surrounding style if any |
| Explicit title-size utility | `.tw-text-[16px]` | 16*gpx | 16.sp |
| Font weight regular | `.tw-font-normal` | 400 | FontWeight.Normal |
| Font weight medium | `.tw-font-medium` | 500 | FontWeight.Medium |
| Font weight semibold | `.tw-font-semibold` | 600 | FontWeight.SemiBold |
| Skill dialog title | `.mhy-skill-dialog .container .info .name` | `4.26667vw`, weight 500, line-height 1.5 | Viewport-relative source; not a fixed gpx token |
| Skill dialog description | `.mhy-skill-dialog .container .desc` | `3.2vw`, line-height 16*gpx | Viewport-relative font; 16.sp line-height |
| Rune dialog title | `.mhy-rune-dialog .container .info .inner .name` | `4.26667vw`, weight 500, line-height 1.5 | Viewport-relative source; not a fixed gpx token |
| Rune dialog description | `.mhy-rune-dialog .container .info .desc` | `3.2vw`, line-height 16*gpx | Viewport-relative font; 16.sp line-height |
| Buff title | `.buff-dialog .container .info .name` | 16*gpx / 24*gpx / weight 500 | 16.sp / 24.sp / Medium |
| Buff energy / caption | `.buff-dialog .container .energy` | 12*gpx / 12*gpx / weight 400 | 12.sp / 12.sp / Normal |
| Buff description | `.buff-dialog .container .desc` | 12*gpx / 16*gpx | 12.sp / 16.sp |
| Empty state | `.empty-block .text` | 16*gpx / 20*gpx, weight 400; later override 12*gpx | 16.sp / 20.sp base; 12.sp override |

**Letter spacing:** only the game-title rule above supplies an explicit `--gpx` letter-spacing value in the matched component rules. Do not infer tracking for other text roles.

## Spacing and sizing

Every value below is a raw coefficient from the named CSS utility/selector, converted using the approved 1:1 policy.

| Component / role | CSS class or selector | Raw coefficient | Android token |
|---|---|---:|---:|
| Page horizontal inset | `.tw-px-4` | 16*gpx each side | 16.dp |
| Compact horizontal inset | `.tw-px-3` | 12*gpx each side | 12.dp |
| Compact inner inset | `.tw-px-2` | 8*gpx each side | 8.dp |
| Navigation container | `.container[data-v-4b6b351e]` | 10*gpx vertical; 16*gpx horizontal | 10.dp vertical; 16.dp horizontal |
| Standard small gap | `.tw-gap-2` | 8*gpx | 8.dp |
| Horizontal grid gap | `.tw-gap-x-3` | 12*gpx | 12.dp |
| Repeated row spacing | `.tw-space-y-2` | 8*gpx | 8.dp |
| Tight row spacing | `.tw-space-y-1` | 4*gpx | 4.dp |
| Compact margin | `.tw-mb-2`, `.tw-mt-2` | 8*gpx | 8.dp |
| Section margin | `.tw-mb-4`, `.tw-mt-4` | 16*gpx | 16.dp |
| Large section separation | `.tw-mb-6`, `.tw-mt-6` | 24*gpx | 24.dp |
| Skill dialog info spacing | `.mhy-skill-dialog .container .info` | 10*gpx bottom | 10.dp |
| Skill icon backing | `.mhy-skill-dialog .container .info .skill-bg` | 48*gpx square | 48.dp |
| Skill glyph | `.mhy-skill-dialog .container .info .skill-bg .icon` | 34*gpx square | 34.dp |
| Rune dialog container | `.mhy-rune-dialog .container[data-v-043f08a2]` | 15*gpx padding; 152*gpx min-height | 15.dp; 152.dp |
| Rune logo | `.mhy-rune-dialog .container .logo` | 48*gpx × 92*gpx | 48.dp × 92.dp |
| Buff title row | `.buff-dialog .container .info .name` | 24*gpx height | 24.dp |
| Buff description block | `.buff-dialog .container .desc-container` | 9*gpx top margin; 120*gpx min/max height | 9.dp; 120.dp |
| Navigation icon | `.ic[data-v-4b6b351e]` | 24*gpx square | 24.dp |
| Avatar image frame (observed Chronicle markup) | `.tw-w-7.tw-h-7` | 28*gpx square | 28.dp |
| Small utility icon | `.tw-w-4.tw-h-4` | 16*gpx square | 16.dp |
| Compact badge/icon | `.tw-w-5.tw-h-5` | 20*gpx square | 20.dp |
| Tiny divider | `.tw-h-[0.5px]` | 0.5*gpx | 0.5.dp |
| One-pixel divider | `.tw-h-[1px]`, `.tw-w-px` | 1*gpx | 1.dp |

## Radii

| Shape role | CSS class | Raw coefficient | Android token |
|---|---|---:|---:|
| Small | `.tw-rounded` | 4*gpx | 4.dp |
| Medium | `.tw-rounded-md` | 6*gpx | 6.dp |
| Large | `.tw-rounded-lg` | 8*gpx | 8.dp |
| Extra large | `.tw-rounded-[10px]` | 10*gpx | 10.dp |
| Card / panel | `.tw-rounded-2xl` | 16*gpx | 16.dp |
| Pill | `.tw-rounded-full` | 9999*gpx | 9999.dp (pill sentinel) |
| Skill scrollbar thumb | `.mhy-rune-dialog .container .info .desc::-webkit-scrollbar-thumb` | 2*gpx | 2.dp |

## Surfaces, text colors, and borders

Colors below are CSS source values, not substitutions from the native app theme.

| Semantic role | CSS class / selector | CSS value | Android representation |
|---|---|---|---|
| HoYoLAB gold accent | `body[data-game-id="2"] .title[data-v-4b6b351e]`, `.tw-bg-ys-l-brand-yellow-3`, `.tw-text-ys-l-brand-yellow-3` | `#D3BC8E` / `rgba(211,188,142,1)` | `Color(0xFFD3BC8E)` |
| Light parchment text | `.tw-text-ys-l-grey-white-2` | `rgba(240,234,224,1)` | `Color(0xFFF0EAE0)` |
| Muted parchment | `.tw-text-ys-l-grey-white-5` | `rgba(203,189,167,1)` | `Color(0xFFCBBDA7)` (hex in Kotlin: `0xFFCBBDA7` without spaces) |
| White 90% | `.tw-text-gt-l-text-white-2` | `rgba(255,255,255,.90)` | `Color(0xE6FFFFFF)` |
| White 65% | `.tw-text-gt-l-text-white-3` | `rgba(255,255,255,.65)` | `Color(0xA6FFFFFF)` |
| White 64% surface | `.tw-bg-gt-g-grey-white-9` | `rgba(255,255,255,.64)` | `Color(0xA3FFFFFF)` |
| White 16% surface | `.tw-bg-gt-g-grey-white-4` | `rgba(255,255,255,.16)` | `Color(0x29FFFFFF)` |
| White 8% surface | `.tw-bg-gt-g-grey-white-6` | `rgba(255,255,255,.08)` | `Color(0x14FFFFFF)` |
| Black 32% overlay | `.tw-bg-gt-g-grey-black-3` | `rgba(0,0,0,.32)` | `Color(0x52000000)` |
| Border white 32% | `.tw-border-gt-g-grey-white-3` | `rgba(255,255,255,.32)` | `Color(0x52FFFFFF)` |
| Border white 16% | `.tw-border-gt-g-grey-white-4` | `rgba(255,255,255,.16)` | `Color(0x29FFFFFF)` |
| Border white 12% | `.tw-border-gt-g-grey-white-5` | `rgba(255,255,255,.12)` | `Color(0x1FFFFFFF)` |
| Border white 8% | `.tw-border-gt-g-grey-white-6` | `rgba(255,255,255,.08)` | `Color(0x14FFFFFF)` |
| Border black 8% | `.tw-border-gt-g-grey-black-6` | `rgba(0,0,0,.08)` | `Color(0x14000000)` |
| Detail dialog name | `.mhy-skill-dialog .container .info .name` | `#333` | `Color(0xFF333333)` |
| Detail dialog description | `.mhy-skill-dialog .container .desc` | `#7F858A` | `Color(0xFF7F858A)` |
| Buff title | `.buff-dialog .container .info .name` | `#1E1F20` | `Color(0xFF1E1F20)` |
| Buff description | `.buff-dialog .container .desc` | `#7F858A` | `Color(0xFF7F858A)` |

**Border observation:** the supplied CSS does not define one universal card border. Border presence is component-specific. The shared utility classes include explicit 1*gpx and 0.5*gpx border widths and separate alpha color classes; other components use background/spacing without a border utility. Do not add a border merely because a class is called a card. The detail-dialog button has an explicit 1*gpx border and an inset pseudo-element border (2*gpx inset, 1*gpx stroke); that is a button-specific treatment, not a general card rule.

## Layout patterns observed in the HTML fragments

| Fragment / component | HTML class evidence | Observed layout |
|---|---|---|
| Chronicle block heading | `block-title`, `block-title-left`, `block-title-left-ic`, `block-title-text` in `index-page-info.html` and `share-page.html` | Header row with icon and title; class-specific stylesheet rules are not present in the supplied CSS extract |
| Exploration cards | `exploration-card`, `area-item`, `content-bg`, `content-img`, `content-linear`, `content-main`, `area-name`, `progress-section` | Region/card structure; region artwork and progress fills are inline/data-driven, not a single reusable surface color |
| Exploration progress | `main-area-row`, `main-area-left`, `main-area-label`, `main-area-percent`, `progress`, `progress-val` | Horizontal label/value row and percentage-width progress fill |
| Character stats | `prop-list`, `prop-item`, `prop-icon`, `prop-name`, `prop-stats`, `base-stat`, `add-stat`, `final-stat` in `body-detail.html` | Repeated stat rows with icon/name/value roles; no class-specific rules for these selectors in the supplied CSS extract |
| Skills / equipment | `skill-item`, `skill-icon`, `constellation-icon`, `relic-item`, `relic-top`, `relic-icon`, `main-prop` | Icon-led repeated rows/tiles; exact class styling is outside the provided CSS rules |
| Calculator landing | `home-container`, `sticky-header`, `home-module`, `home-module__title`, `user-card`, `cards-container`, `quick-module` in `enhancement-calculator-body.html` | Header + user summary + module/card groups. Character/weapon selection and material-detail screens are absent from this fragment |
| Shared Chronicle data | `summary-data`, `btm-data`, `value`, `name` in `share-page.html` | Summary values and labels grouped into rows |
| Explicit two-column grid | `.tw-grid.tw-grid-cols-2` | `grid-template-columns: repeat(2, minmax(0, 1fr))`; 2 columns |
| Common flex row | `.tw-flex.tw-items-center` | `display:flex; align-items:center` |
| Centered flex content | `.tw-flex-center` | `display:flex; align-items:center; justify-content:center` |
| Split row | `.tw-justify-between` | `justify-content:space-between` |

### Calculator sub-screen tokens (shared / inferred)

The supplied calculator HTML contains the landing page and does **not** contain character/weapon catalogue grids or material-detail rows. Do not present their exact structure as source-observed.

| Intended component | Shared classes available in `m_55ae43ba.css` | Safe shared baseline | Evidence status |
|---|---|---|---|
| Character / weapon selector tiles | `.tw-flex`, `.tw-grid`, `.tw-items-center`, `.tw-gap-2`, `.tw-rounded-lg`, `.tw-w-7`, `.tw-h-7`, `.tw-px-3` | 8.dp gap, 8.dp radius, 28.dp icon frame, 12.dp horizontal inset | Shared/inferred; no exact selector-grid column count in calculator fragment |
| Filter chips | `.tw-rounded-full`, `.tw-px-2`, `.tw-py-1.5`, `.tw-text-xs-m`, `.tw-font-medium` | Pill radius, 8.dp horizontal and 6.dp vertical padding, 12.sp/16.sp, Medium | Shared/inferred |
| Material rows | `.tw-flex`, `.tw-items-center`, `.tw-gap-2`, `.tw-h-[0.5px]`, `.tw-text-xs-m` | Horizontal aligned row, 8.dp gap, optional 0.5.dp divider, 12.sp/16.sp | Shared/inferred |
| Numeric stat / quantity | `.tw-text-base-m`, `.tw-font-semibold`, `.tw-justify-between` | 16.sp/20.sp, SemiBold, split row | Shared/inferred |

**Grid caution:** `.tw-grid-cols-2` is present in the Chronicle markup/CSS and proves a two-column grid utility. It does not prove the calculator selector uses two columns. No four-column calculator selector is present in the five extracts. Any four-column Compose layout must be a product decision, not an extracted HoYoLAB fact.

## Source gaps and interpretation rules

- `--gpx` is used by the CSS but is not defined in the supplied stylesheet or HTML fragments. The 1:1 mapping is the user-approved implementation policy.
- Some HTML inline styles use `rem`, `vw`, raw `px`, percentages, gradients, and per-region colors. These remain source-specific and are not silently normalized into global tokens.
- Class names such as `exploration-card`, `area-item`, `prop-item`, `home-module`, and `user-card` occur in HTML, but their component-specific rules are not present in this CSS extract. Their structure is recorded, but their exact radius/padding/color is not guessed.
- Inline region background colors and gradients represent individual game regions. They are data/art direction, not general surface tokens.
- This is a token reference layer only. It does not prescribe or modify any Compose screen.

## Provenance

All CSS class names and raw declarations above are from `docs/m_55ae43ba.css`; component class evidence is from the named HTML fragment in the layout table. The Kotlin layer is in `app/src/main/java/com/irminsul/codex/presentation/HoYoLabTokens.kt`.
