# HoYoLAB Source Extraction Inventory

> Generated from the five supplied source extracts on PR #17 branch `ui-revamp/batch-9-character-weapon-fidelity`. This is a forensic reference companion to `HOYOLAB_DESIGN_TOKENS.md`, not a claim that every class has been reconstructed into Compose. Preserve the original extracts until this inventory has been reviewed.

## Extraction policy

- CSS declarations are recorded as supplied. No browser computed styles are inferred.
- `--gpx` remains undefined in these extracts. The approved Android conversion remains 1 coefficient = 1.dp for layout and 1.sp for typography.
- HTML fragments are treated as fragments, not complete pages. External scripts, missing stylesheets, runtime state, and assets not included in the extracts remain unavailable.
- CSS rules are preserved in selector/declaration form below; this avoids silently dropping declarations that are not global tokens.

## Source file manifest

| File | Characters | SHA (source blob) |
|---|---:|---|
| `docs/m_55ae43ba.css` | 113,028 | `c7cf50e27b1007d9e78697731155ed67659535f9` |
| `docs/index-page-info.html` | 106,467 | `c4ed98716c085081e22b842eeaee9a1048ea610d` |
| `docs/body-detail.html` | 78,824 | `18ba0399a4ef55a1b6e38b627c0e600ff4672a1c` |
| `docs/enhancement-calculator-body.html` | 83,422 | `30e2221e2d5e94ee7c5a2f31578ce465ce006a6c` |
| `docs/share-page.html` | 116,408 | `40c5fee0766a722eb22efc6ce7213310797ee29e` |

## Extraction totals

- CSS rule blocks: **1145**
- Distinct CSS class selectors: **15**
- CSS rules containing selectors matching classes present in the HTML fragments: **227** (selector match is syntactic, not proof of runtime application)
- Utility rules with a `.tw-` selector: **916**
- CSS declarations with color/fill/stroke properties: **227**
- CSS rules with sizing/spacing/typography/radius declarations: **465**
- Distinct literal `--gpx` coefficients: **190**
- Distinct inline style attributes across HTML: **120**
- CSS `url(...)` references: **11**
- HTML image-file references found by attribute scan: **186**

## CSS custom properties defined in this stylesheet

- `--tw-backdrop-blur`
- `--tw-backdrop-brightness`
- `--tw-backdrop-contrast`
- `--tw-backdrop-grayscale`
- `--tw-backdrop-hue-rotate`
- `--tw-backdrop-invert`
- `--tw-backdrop-opacity`
- `--tw-backdrop-saturate`
- `--tw-backdrop-sepia`
- `--tw-bg-opacity`
- `--tw-blur`
- `--tw-border-opacity`
- `--tw-border-spacing-x`
- `--tw-border-spacing-y`
- `--tw-brightness`
- `--tw-contain-layout`
- `--tw-contain-paint`
- `--tw-contain-size`
- `--tw-contain-style`
- `--tw-contrast`
- `--tw-drop-shadow`
- `--tw-gradient-from-position`
- `--tw-gradient-to-position`
- `--tw-gradient-via-position`
- `--tw-grayscale`
- `--tw-hue-rotate`
- `--tw-invert`
- `--tw-numeric-figure`
- `--tw-numeric-fraction`
- `--tw-numeric-spacing`
- `--tw-ordinal`
- `--tw-pan-x`
- `--tw-pan-y`
- `--tw-pinch-zoom`
- `--tw-ring-color`
- `--tw-ring-inset`
- `--tw-ring-offset-color`
- `--tw-ring-offset-shadow`
- `--tw-ring-offset-width`
- `--tw-ring-shadow`
- `--tw-rotate`
- `--tw-saturate`
- `--tw-scale-x`
- `--tw-scale-y`
- `--tw-scroll-snap-strictness`
- `--tw-sepia`
- `--tw-shadow`
- `--tw-shadow-colored`
- `--tw-skew-x`
- `--tw-skew-y`
- `--tw-slashed-zero`
- `--tw-space-x-reverse`
- `--tw-space-y-reverse`
- `--tw-text-opacity`
- `--tw-translate-x`
- `--tw-translate-y`

**Important:** the stylesheet uses `var(--gpx)`, but `--gpx` is not defined in the supplied stylesheet. It may be defined by an omitted runtime stylesheet or inline page setup.

## HTML class inventory by fragment

### `docs/index-page-info.html`

`!tw-text-gt-g-grey-white-5`, `-tw-top-0.5`, `0.48]`, `255,`, `Q9o7KplaRwUOCJ0yKixOLA==`, `_19ySzZqeSWqgAkRyps1muQ==`, `_1ad2PQw7tqrLd9GC-9hCPA==`, `_4+INZ9extCW9GjMrdm7uEA==`, `aF3Xc3O5tj9VcHGrul9K4A==`, `abyss-block`, `action-buttons`, `active`, `activity-card-pop`, `area-item`, `area-list`, `area-name`, `arrow-icon`, `big`, `block-border`, `block-title`, `block-title-left`, `block-title-left-ic`, `block-title-left-ic-question`, `block-title-right`, `block-title-right-ic`, `block-title-text`, `block-title-text-more`, `card`, `challenge-border`, `challenge-wrap`, `child-item`, `child-list`, `child-name`, `child-percent`, `container`, `content`, `content-bg`, `content-img`, `content-linear`, `content-main`, `cover-wrapper`, `data-content`, `exploration-block-title`, `exploration-card`, `gt-avatar`, `gt-avatar--size-xs`, `gt-avatar-core`, `gt-avatar-img`, `gt-avatar-left`, `gt-avatar-left-block`, `gt-avatar-left-element`, `gt-avatar-left-element-grass`, `gt-avatar-left-element-mine`, `gt-avatar-left-element-rock`, `gt-avatar-left-element-water`, `gt-avatar-left-element-wind`, `gt-avatar-right`, `gt-avatar-star-bg`, `gt-avatar-star-bg-4`, `gt-avatar-star-bg-5`, `gt-avatar-ys`, `gt-badge-abs__rectangle`, `gt-badge-abs__rectangle-label`, `gt-badge-new`, `gt-badge-new__en-us`, `gt-badge-new__mob`, `gt-badge-wrap__rectangle`, `gt-badge-ys`, `gt-icon`, `gt-icon--colorless`, `gt-icon--size-m`, `gt-icon--size-s`, `gt-icon--size-xs`, `gt-icon--size-xxs`, `gt-icon-ys`, `gt-icon__img`, `header`, `home-land-container`, `honor-medal`, `hxyj-btn`, `icon`, `indicators`, `indicators-dot`, `indicators-dot--active`, `info`, `is-full`, `is-home`, `item`, `label`, `line`, `line-icon`, `look-back`, `look-back--border`, `look-back--content`, `look-back--content--rank`, `look-back--header`, `look-back--info`, `look-back--info_container`, `look-back--info_data`, `look-back-layout`, `m-index-page-info`, `main-area-label`, `main-area-left`, `main-area-percent`, `main-area-row`, `me`, `num`, `period`, `pop_title`, `progress`, `progress-section`, `progress-val`, `pt`, `ranks-list`, `recent-games-container`, `recent-games-swiper`, `role-combat-block-title`, `role-combat-index-block`, `role-combat-index-block-container`, `role-medium`, `schedule-box`, `schedule-range`, `schedule-switch`, `schedule-title`, `serenitea-block`, `shCnduN2T41Qt8xKmuNDuQ==`, `showGuide`, `sjwPKk9bUlbhI3TIXUJ-Yw==`, `small`, `stat-item-data`, `strategy`, `swiper-pagination`, `tcg-block`, `tcg-card`, `tcg-card-cover`, `tcg-container`, `tcg-container-top`, `tcg-match-item`, `tcg-match-item-avatar`, `tcg-match-item-avatars`, `tcg-match-item-duel`, `tcg-match-item-line`, `tcg-match-item-result`, `tcg-match-item-status`, `tcg-recent`, `tcg-recent-list`, `tcg-recent-title`, `tcg-recent-title-left`, `tcg-recent-title-right`, `tcg-recent-title-text`, `tcg-right-p`, `tcg-top-level`, `tcg-top-right`, `text`, `title`, `total-star`, `tw-absolute`, `tw-absolute-center`, `tw-backdrop-blur-[2px]`, `tw-backdrop-blur-[8px]`, `tw-backdrop-blur-sm`, `tw-bg-[#00000040]`, `tw-bg-[#161616]`, `tw-bg-[#D27979]`, `tw-bg-[rgba(111,79,116,0.72)]`, `tw-bg-gt-g-grey-black-3`, `tw-bg-gt-g-grey-white-4`, `tw-bg-gt-g-grey-white-6`, `tw-bg-gt-g-grey-white-7`, `tw-bg-gt-g-grey-white-9`, `tw-bg-ys-g-func-green-2`, `tw-bg-ys-l-brand-yellow-3`, `tw-bg-ys-l-grey-white-6`, `tw-border`, `tw-border-[#181818]`, `tw-border-[#DBD1BF]`, `tw-border-[0.5px]`, `tw-border-gt-g-grey-black-6`, `tw-border-gt-g-grey-white-10`, `tw-border-gt-g-grey-white-3`, `tw-border-gt-g-grey-white-4`, `tw-border-gt-g-grey-white-5`, `tw-border-gt-g-grey-white-6`, `tw-border-solid`, `tw-border-t-0`, `tw-border-white/10`, `tw-border-ys-l-brand-yellow-3`, `tw-bottom-0`, `tw-box-border`, `tw-cursor-pointer`, `tw-flex`, `tw-flex-1`, `tw-flex-center`, `tw-flex-items-center`, `tw-flex-nowrap`, `tw-flex-shrink-0`, `tw-font-genshinfont`, `tw-font-normal`, `tw-font-semibold`, `tw-gap-2`, `tw-gap-x-3`, `tw-grid`, `tw-grid-cols-2`, `tw-group`, `tw-grow`, `tw-h-10`, `tw-h-12`, `tw-h-3`, `tw-h-4`, `tw-h-5`, `tw-h-5.5`, `tw-h-7`, `tw-h-8`, `tw-h-[0.5px]`, `tw-h-[10px]`, `tw-h-[114px]`, `tw-h-[14px]`, `tw-h-[1px]`, `tw-h-[22px]`, `tw-h-[26px]`, `tw-h-[29px]`, `tw-h-[32px]`, `tw-h-[35px]`, `tw-h-[38px]`, `tw-h-[51px]`, `tw-h-[54px]`, `tw-h-[9px]`, `tw-h-full`, `tw-inline-flex`, `tw-items-center`, `tw-justify-between`, `tw-justify-center`, `tw-leading-3`, `tw-leading-[10px]`, `tw-leading-[12px]`, `tw-leading-[14px]`, `tw-leading-[15px]`, `tw-leading-[18px]`, `tw-leading-[7px]`, `tw-left-0`, `tw-left-[-2px]`, `tw-left-[1px]`, `tw-line-clamp-1`, `tw-line-clamp-2`, `tw-max-w-[200px]`, `tw-max-w-[94px]`, `tw-mb-1`, `tw-mb-2`, `tw-mb-2.5`, `tw-mb-3`, `tw-mb-4`, `tw-mb-[2px]`, `tw-mb-[4px]`, `tw-ml-0.5`, `tw-ml-1`, `tw-ml-2`, `tw-ml-[2px]`, `tw-ml-[5px]`, `tw-mr-0.5`, `tw-mr-1`, `tw-mr-2`, `tw-mr-3`, `tw-mr-px`, `tw-mt-2`, `tw-mt-2.5`, `tw-mt-3`, `tw-mt-5`, `tw-mt-[1px]`, `tw-mt-[5px]`, `tw-mt-[7px]`, `tw-mx-1`, `tw-mx-auto`, `tw-my-1`, `tw-my-[2px]`, `tw-opacity-50`, `tw-opacity-[0.15]`, `tw-overflow-hidden`, `tw-overflow-x-auto`, `tw-overflow-x-scroll`, `tw-pb-1`, `tw-pb-3`, `tw-pb-6`, `tw-pb-[6px]`, `tw-pl-12`, `tw-pl-2`, `tw-pl-3`, `tw-pl-5`, `tw-pl-[15px]`, `tw-pl-[1px]`, `tw-pr-2`, `tw-pr-3`, `tw-pr-[9px]`, `tw-pt-3`, `tw-pt-4`, `tw-px-1`, `tw-px-1.5`, `tw-px-2`, `tw-px-3`, `tw-px-4`, `tw-px-[10px]`, `tw-px-[15px]`, `tw-py-1.5`, `tw-py-2`, `tw-py-[1px]`, `tw-py-px`, `tw-relative`, `tw-right-0`, `tw-right-3`, `tw-rounded`, `tw-rounded-2xl`, `tw-rounded-[10px]`, `tw-rounded-[16px]`, `tw-rounded-[2px]`, `tw-rounded-[32px]`, `tw-rounded-b`, `tw-rounded-br-[4px]`, `tw-rounded-full`, `tw-rounded-lg`, `tw-rounded-tl-[4px]`, `tw-scrollbar-hide`, `tw-select-none`, `tw-shrink-0`, `tw-space-x-0.5`, `tw-space-x-1`, `tw-space-x-[2px]`, `tw-space-y-1`, `tw-space-y-2`, `tw-space-y-3`, `tw-text-[#7f858a]`, `tw-text-[#997754]`, `tw-text-[#fff]`, `tw-text-[0px]`, `tw-text-[10px]`, `tw-text-[12px]`, `tw-text-[14px]`, `tw-text-[16px]`, `tw-text-[18px]`, `tw-text-[8px]`, `tw-text-[9px]`, `tw-text-[rgba(255,`, `tw-text-ellipsis`, `tw-text-gt-g-grey-white-2`, `tw-text-gt-g-grey-white-8`, `tw-text-gt-g-grey-white-9`, `tw-text-gt-l-text-white-1`, `tw-text-gt-l-text-white-2`, `tw-text-gt-l-text-white-3`, `tw-text-sm-m`, `tw-text-xs`, `tw-text-xs-m`, `tw-text-xxs-m`, `tw-text-ys-d-grey-blue-2`, `tw-text-ys-d-text-yellow-4`, `tw-text-ys-g-func-orange`, `tw-text-ys-l-brand-yellow-3`, `tw-text-ys-l-grey-white-5`, `tw-top-0`, `tw-top-[-3px]`, `tw-top-[0]`, `tw-top-[14px]`, `tw-top-[1px]`, `tw-transform-gpu`, `tw-translate-y-[-3px]`, `tw-truncate`, `tw-w-2`, `tw-w-3`, `tw-w-4`, `tw-w-5`, `tw-w-7`, `tw-w-8`, `tw-w-[108px]`, `tw-w-[10px]`, `tw-w-[14px]`, `tw-w-[168px]`, `tw-w-[1px]`, `tw-w-[22px]`, `tw-w-[26px]`, `tw-w-[32px]`, `tw-w-[345px]`, `tw-w-[34px]`, `tw-w-[38px]`, `tw-w-[3px]`, `tw-w-[6px]`, `tw-w-full`, `tw-w-px`, `tw-whitespace-nowrap`, `tw-z-1`, `tw-z-[1]`, `tw-z-[2]`, `util-block`, `util-list`, `van-swipe`, `van-swipe-item`, `van-swipe__track`, `van-tab`, `van-tab--active`, `van-tab__pane`, `van-tab__pane-wrapper`, `van-tab__pane-wrapper--inactive`, `van-tab__text`, `van-tab__text--ellipsis`, `van-tabs`, `van-tabs--line`, `van-tabs__content`, `van-tabs__content--animated`, `van-tabs__line`, `van-tabs__nav`, `van-tabs__nav--line`, `van-tabs__track`, `van-tabs__wrap`, `view-more-btn`, `wWo-oTRNkqwGkHejy9ehKQ==`, `wenli-bg`, `world-exploration`, `world-exploration-block`, `you`, `yysp-badge`, `yysp-btn`, `z3wM-MdSkGFXqy-b+bDIyA==`

### `docs/body-detail.html`

`active`, `active-value`, `add-stat`, `all-props-btn`, `base-info`, `base-info-section`, `base-stat`, `bgs`, `center`, `constellation-icon`, `constellations`, `container`, `content`, `dress-entry-container`, `element-icon`, `enhance-tag`, `favor-icon`, `final-stat`, `first-level`, `gt-icon`, `gt-icon--colorless`, `gt-icon-ys`, `gt-icon__img`, `gt-image`, `gt-modal`, `gt-popup-root`, `header-navbar`, `hidden`, `hyv-login-platform__loading`, `hyv-login-platform__loading--rotation`, `ic`, `icon`, `icon-value`, `info-icon`, `info-panel`, `infos`, `item-1`, `item-2`, `item-3`, `item-4`, `item-5`, `item-6`, `lang`, `lang-btn`, `langpop`, `left`, `list-swiper`, `lock-icon`, `lock-icon-bg`, `m-root-layout`, `main-content`, `main-prop`, `main-props`, `mark-props`, `me-share`, `me-share__btn`, `me-share__icon`, `me-share__icon--active`, `me-share__icon--hover`, `more-skills`, `name`, `nav-bar`, `nav-bar-container`, `painting`, `prop-icon`, `prop-info-section`, `prop-item`, `prop-list`, `prop-name`, `prop-stats`, `relic-icon`, `relic-info-section`, `relic-item`, `relic-list`, `relic-top`, `right`, `role-detail-container`, `role-detail-layout`, `second-level`, `selected-icon`, `share-bgs`, `share-container`, `share-footer`, `share-footer-default`, `share-header`, `share-page`, `skill-icon`, `skill-item`, `skill-list`, `sub-prop`, `sub-props`, `third-level`, `title`, `title-box`, `top-info`, `tw-absolute-center`, `tw-bg-[#161616]`, `tw-bg-gt-g-grey-white-7`, `tw-border`, `tw-border-[#181818]`, `tw-border-gt-g-grey-white-7`, `tw-border-solid`, `tw-break-words`, `tw-flex-center`, `tw-gap-2`, `tw-grid`, `tw-grid-cols-2`, `tw-h-10`, `tw-h-5`, `tw-h-7`, `tw-h-[38px]`, `tw-line-clamp-2`, `tw-ml-2`, `tw-mr-2`, `tw-mr-3`, `tw-my-3`, `tw-overflow-hidden`, `tw-px-3`, `tw-relative`, `tw-rounded-full`, `tw-rounded-md`, `tw-select-none`, `tw-shrink-0`, `tw-text-[8px]`, `tw-text-gt-g-grey-white-2`, `tw-text-gt-l-text-white-2`, `tw-w-5`, `tw-w-7`, `tw-w-[102px]`, `tw-w-[172px]`, `tw-w-[38px]`, `tw-z-[1]`, `tw-z-[2]`, `user-model-loading`, `user-model-loading__bg`, `user-model-loading__icon`, `user-model-loading__toast`, `user-model-loading_text`, `value`, `van-popover__wrapper`, `weapon-info-section`

### `docs/enhancement-calculator-body.html`

`JA4pEMFhnkLK8prsn9em2g==`, `action`, `b6-uq87BoCbX8Bf5RS7zIw==`, `card`, `cards-container`, `gt-badge-label`, `gt-badge-label__bg`, `gt-badge-label__bg-left`, `gt-badge-label__bg-mid`, `gt-badge-label__bg-right`, `gt-badge-label__content`, `gt-badge-label__mob`, `gt-badge-ys`, `gt-icon`, `gt-icon--colorless`, `gt-icon--size-s`, `gt-icon-ys`, `gt-icon__img`, `gt-modal`, `gt-popup-layout`, `gt-popup-layout-ys`, `gt-popup-root`, `gt_mobile_tag_t1_3`, `hidden`, `home-container`, `home-module`, `home-module__subtitle`, `home-module__title`, `home-new`, `hyv-login-platform__loading`, `hyv-login-platform__loading--rotation`, `icon`, `icon-0`, `icon-1`, `icon-2`, `icon-3`, `left`, `modals-container`, `nickname`, `quick-module`, `right-section`, `scroll`, `sticky-header`, `subtitle`, `title`, `title-s`, `title-size`, `top`, `tw-bg-transparent`, `tw-box-border`, `tw-fixed`, `tw-flex-1`, `tw-flex-items-center`, `tw-font-medium`, `tw-h-11`, `tw-invisible`, `tw-leading-[0.16rem]`, `tw-left-0`, `tw-mt-3`, `tw-mt-[0.16rem]`, `tw-overflow-hidden`, `tw-px-4`, `tw-py-2.5`, `tw-space-x-4`, `tw-text-[0.12rem]`, `tw-text-base-m`, `tw-text-center`, `tw-text-gt-g-grey-white-1`, `tw-text-ys-d-brand-golden-1`, `tw-text-ys-l-grey-white-2`, `tw-top-0`, `tw-truncate`, `tw-w-full`, `tw-z-10`, `uid-info`, `user-card`, `user-model-loading`, `user-model-loading__bg`, `user-model-loading__icon`, `user-model-loading__toast`, `user-model-loading_text`

### `docs/share-page.html`

`!tw-text-gt-g-grey-white-5`, `-tw-top-0.5`, `0.48]`, `255,`, `Q9o7KplaRwUOCJ0yKixOLA==`, `_19ySzZqeSWqgAkRyps1muQ==`, `_1ad2PQw7tqrLd9GC-9hCPA==`, `_4+INZ9extCW9GjMrdm7uEA==`, `aF3Xc3O5tj9VcHGrul9K4A==`, `abyss-block`, `account-block`, `account-btm`, `account-change`, `account-right`, `account-top`, `achievement-new`, `action-buttons`, `active`, `activity-card-pop`, `area-item`, `area-list`, `area-name`, `arrow-icon`, `avatar`, `big`, `block-border`, `block-title`, `block-title-left`, `block-title-left-ic`, `block-title-left-ic-question`, `block-title-right`, `block-title-right-ic`, `block-title-text`, `block-title-text-more`, `box`, `btm-data`, `card`, `center`, `challenge-border`, `challenge-wrap`, `child-item`, `child-list`, `child-name`, `child-percent`, `container`, `content`, `content-bg`, `content-img`, `content-linear`, `content-main`, `cover-wrapper`, `data-content`, `expand`, `expand-btn`, `expand-hotzone`, `exploration-block-title`, `exploration-card`, `game-header`, `game-kv`, `game-selector`, `game-summary`, `gt-avatar`, `gt-avatar--size-xs`, `gt-avatar-core`, `gt-avatar-img`, `gt-avatar-left`, `gt-avatar-left-block`, `gt-avatar-left-element`, `gt-avatar-left-element-grass`, `gt-avatar-left-element-mine`, `gt-avatar-left-element-rock`, `gt-avatar-left-element-water`, `gt-avatar-left-element-wind`, `gt-avatar-right`, `gt-avatar-star-bg`, `gt-avatar-star-bg-4`, `gt-avatar-star-bg-5`, `gt-avatar-ys`, `gt-badge-abs__rectangle`, `gt-badge-abs__rectangle-label`, `gt-badge-label`, `gt-badge-label__bg`, `gt-badge-label__bg-left`, `gt-badge-label__bg-mid`, `gt-badge-label__bg-right`, `gt-badge-label__content`, `gt-badge-label__mob`, `gt-badge-new`, `gt-badge-new__en-us`, `gt-badge-new__mob`, `gt-badge-wrap__rectangle`, `gt-badge-ys`, `gt-icon`, `gt-icon--colorless`, `gt-icon--size-m`, `gt-icon--size-s`, `gt-icon--size-xs`, `gt-icon--size-xxs`, `gt-icon-ys`, `gt-icon__img`, `gt-image`, `gt_mobile_tag_t1_3`, `header`, `home-land-container`, `honor-medal`, `hxyj-btn`, `ic`, `ic-set`, `icon`, `indicators`, `indicators-dot`, `indicators-dot--active`, `info`, `inner`, `is-full`, `is-home`, `item`, `label`, `lang`, `lang-btn`, `langpop`, `left`, `level`, `line`, `line-icon`, `look-back`, `look-back--border`, `look-back--content`, `look-back--content--rank`, `look-back--header`, `look-back--info`, `look-back--info_container`, `look-back--info_data`, `look-back-layout`, `m-index-layout`, `m-index-page-info`, `main-area-label`, `main-area-left`, `main-area-percent`, `main-area-row`, `me`, `me-share`, `me-share__btn`, `me-share__icon`, `me-share__icon--active`, `me-share__icon--hover`, `name`, `nav-bar`, `nav-bar-container`, `nickname`, `num`, `period`, `pop_title`, `progress`, `progress-section`, `progress-val`, `pt`, `ranks-list`, `recent-games-container`, `recent-games-swiper`, `review-entry`, `right`, `role-combat-block-title`, `role-combat-index-block`, `role-combat-index-block-container`, `role-medium`, `schedule-box`, `schedule-range`, `schedule-switch`, `schedule-title`, `search-btn-icon`, `serenitea-block`, `shCnduN2T41Qt8xKmuNDuQ==`, `share-logo`, `share-page`, `showGuide`, `sjwPKk9bUlbhI3TIXUJ-Yw==`, `small`, `stat-item-data`, `strategy`, `summary`, `summary-btm`, `summary-data`, `summary-top`, `swiper-pagination`, `tcg-block`, `tcg-card`, `tcg-card-cover`, `tcg-container`, `tcg-container-top`, `tcg-match-item`, `tcg-match-item-avatar`, `tcg-match-item-avatars`, `tcg-match-item-duel`, `tcg-match-item-line`, `tcg-match-item-result`, `tcg-match-item-status`, `tcg-recent`, `tcg-recent-list`, `tcg-recent-title`, `tcg-recent-title-left`, `tcg-recent-title-right`, `tcg-recent-title-text`, `tcg-right-p`, `tcg-top-level`, `tcg-top-right`, `text`, `title`, `top-data`, `total-star`, `tw-absolute`, `tw-absolute-center`, `tw-backdrop-blur-[2px]`, `tw-backdrop-blur-[8px]`, `tw-backdrop-blur-sm`, `tw-bg-[#00000040]`, `tw-bg-[#161616]`, `tw-bg-[#D27979]`, `tw-bg-[rgba(111,79,116,0.72)]`, `tw-bg-gt-g-grey-black-3`, `tw-bg-gt-g-grey-white-4`, `tw-bg-gt-g-grey-white-6`, `tw-bg-gt-g-grey-white-7`, `tw-bg-gt-g-grey-white-9`, `tw-bg-ys-g-func-green-2`, `tw-bg-ys-l-brand-yellow-3`, `tw-bg-ys-l-grey-white-6`, `tw-border`, `tw-border-[#181818]`, `tw-border-[#DBD1BF]`, `tw-border-[0.5px]`, `tw-border-gt-g-grey-black-6`, `tw-border-gt-g-grey-white-10`, `tw-border-gt-g-grey-white-3`, `tw-border-gt-g-grey-white-4`, `tw-border-gt-g-grey-white-5`, `tw-border-gt-g-grey-white-6`, `tw-border-solid`, `tw-border-t-0`, `tw-border-white/10`, `tw-border-ys-l-brand-yellow-3`, `tw-bottom-0`, `tw-box-border`, `tw-cursor-pointer`, `tw-flex`, `tw-flex-1`, `tw-flex-center`, `tw-flex-items-center`, `tw-flex-nowrap`, `tw-flex-shrink-0`, `tw-font-genshinfont`, `tw-font-normal`, `tw-font-semibold`, `tw-gap-2`, `tw-gap-x-3`, `tw-grid`, `tw-grid-cols-2`, `tw-group`, `tw-grow`, `tw-h-10`, `tw-h-12`, `tw-h-3`, `tw-h-4`, `tw-h-5`, `tw-h-5.5`, `tw-h-7`, `tw-h-8`, `tw-h-[0.5px]`, `tw-h-[10px]`, `tw-h-[114px]`, `tw-h-[14px]`, `tw-h-[1px]`, `tw-h-[22px]`, `tw-h-[26px]`, `tw-h-[29px]`, `tw-h-[32px]`, `tw-h-[35px]`, `tw-h-[38px]`, `tw-h-[51px]`, `tw-h-[54px]`, `tw-h-[9px]`, `tw-h-full`, `tw-inline-flex`, `tw-items-center`, `tw-justify-between`, `tw-justify-center`, `tw-leading-3`, `tw-leading-[10px]`, `tw-leading-[12px]`, `tw-leading-[14px]`, `tw-leading-[15px]`, `tw-leading-[18px]`, `tw-leading-[7px]`, `tw-left-0`, `tw-left-[-2px]`, `tw-left-[1px]`, `tw-left-[70%]`, `tw-line-clamp-1`, `tw-line-clamp-2`, `tw-max-w-[200px]`, `tw-max-w-[94px]`, `tw-mb-1`, `tw-mb-2`, `tw-mb-2.5`, `tw-mb-3`, `tw-mb-4`, `tw-mb-[2px]`, `tw-mb-[4px]`, `tw-ml-0.5`, `tw-ml-1`, `tw-ml-2`, `tw-ml-[2px]`, `tw-ml-[5px]`, `tw-mr-0.5`, `tw-mr-1`, `tw-mr-2`, `tw-mr-3`, `tw-mr-px`, `tw-mt-2`, `tw-mt-2.5`, `tw-mt-3`, `tw-mt-5`, `tw-mt-[1px]`, `tw-mt-[5px]`, `tw-mt-[7px]`, `tw-mx-1`, `tw-mx-auto`, `tw-my-1`, `tw-my-[2px]`, `tw-opacity-50`, `tw-opacity-[0.15]`, `tw-overflow-hidden`, `tw-overflow-x-auto`, `tw-overflow-x-scroll`, `tw-pb-1`, `tw-pb-3`, `tw-pb-6`, `tw-pb-[6px]`, `tw-pl-12`, `tw-pl-2`, `tw-pl-3`, `tw-pl-5`, `tw-pl-[15px]`, `tw-pl-[1px]`, `tw-pr-2`, `tw-pr-3`, `tw-pr-[9px]`, `tw-pt-3`, `tw-pt-4`, `tw-px-1`, `tw-px-1.5`, `tw-px-2`, `tw-px-3`, `tw-px-4`, `tw-px-[10px]`, `tw-px-[15px]`, `tw-py-1.5`, `tw-py-2`, `tw-py-[1px]`, `tw-py-px`, `tw-relative`, `tw-right-0`, `tw-right-3`, `tw-rounded`, `tw-rounded-2xl`, `tw-rounded-[10px]`, `tw-rounded-[16px]`, `tw-rounded-[2px]`, `tw-rounded-[32px]`, `tw-rounded-b`, `tw-rounded-br-[4px]`, `tw-rounded-full`, `tw-rounded-lg`, `tw-rounded-tl-[4px]`, `tw-scale-[0.8]`, `tw-scrollbar-hide`, `tw-select-none`, `tw-shrink-0`, `tw-space-x-0.5`, `tw-space-x-1`, `tw-space-x-[2px]`, `tw-space-y-1`, `tw-space-y-2`, `tw-space-y-3`, `tw-text-[#7f858a]`, `tw-text-[#997754]`, `tw-text-[#9B9894]`, `tw-text-[#fff]`, `tw-text-[0px]`, `tw-text-[10px]`, `tw-text-[12px]`, `tw-text-[14px]`, `tw-text-[16px]`, `tw-text-[18px]`, `tw-text-[8px]`, `tw-text-[9px]`, `tw-text-[rgba(255,`, `tw-text-ellipsis`, `tw-text-gt-g-grey-white-2`, `tw-text-gt-g-grey-white-8`, `tw-text-gt-g-grey-white-9`, `tw-text-gt-l-text-white-1`, `tw-text-gt-l-text-white-2`, `tw-text-gt-l-text-white-3`, `tw-text-sm-m`, `tw-text-xs`, `tw-text-xs-m`, `tw-text-xxs-m`, `tw-text-ys-d-grey-blue-2`, `tw-text-ys-d-text-yellow-4`, `tw-text-ys-g-func-orange`, `tw-text-ys-l-brand-yellow-3`, `tw-text-ys-l-grey-white-5`, `tw-top-0`, `tw-top-[-3px]`, `tw-top-[-55%]`, `tw-top-[0]`, `tw-top-[14px]`, `tw-top-[1px]`, `tw-transform-gpu`, `tw-translate-y-[-3px]`, `tw-truncate`, `tw-w-2`, `tw-w-3`, `tw-w-4`, `tw-w-5`, `tw-w-7`, `tw-w-8`, `tw-w-[108px]`, `tw-w-[10px]`, `tw-w-[14px]`, `tw-w-[168px]`, `tw-w-[1px]`, `tw-w-[22px]`, `tw-w-[26px]`, `tw-w-[32px]`, `tw-w-[345px]`, `tw-w-[34px]`, `tw-w-[38px]`, `tw-w-[3px]`, `tw-w-[6px]`, `tw-w-full`, `tw-w-px`, `tw-whitespace-nowrap`, `tw-z-1`, `tw-z-[1]`, `tw-z-[2]`, `uid`, `util-block`, `util-list`, `value`, `van-popover__wrapper`, `van-sticky`, `van-swipe`, `van-swipe-item`, `van-swipe__track`, `van-tab`, `van-tab--active`, `van-tab__pane`, `van-tab__pane-wrapper`, `van-tab__pane-wrapper--inactive`, `van-tab__text`, `van-tab__text--ellipsis`, `van-tabs`, `van-tabs--line`, `van-tabs__content`, `van-tabs__content--animated`, `van-tabs__line`, `van-tabs__nav`, `van-tabs__nav--line`, `van-tabs__track`, `van-tabs__wrap`, `view-more-btn`, `wWo-oTRNkqwGkHejy9ehKQ==`, `wenli-bg`, `world-exploration`, `world-exploration-block`, `you`, `yysp-badge`, `yysp-btn`, `z3wM-MdSkGFXqy-b+bDIyA==`

## HTML ID inventory by fragment

### `docs/index-page-info.html`

`anchor_calendar`, `anchor_challenge`, `anchor_character`, `anchor_exploration`, `anchor_serenitea`, `anchor_spiral_abyss`, `anchor_tcg`, `anchor_tools`, `gti--ys-function-arrow-arrowright972286398fyyj79rfzu`, `gti--ys-function-arrow-right97228635f6u7m6ymssu`, `gti--ys-function-arrow-right97228636k5jx6di41dc`, `gti--ys-function-arrow-right97228636t1inw8znupd`, `gti--ys-function-control-arrowright972286480w6gauxedq4`, `gti--ys-function-control-right97228473vi0et1pbxpd`, `gti--ys-function-control-right97228474fe4jadoe7y`, `gti--ys-function-control-right97228474qq5d6dv82j`, `gti--ys-function-control-right97228474s9yezuljfuq`, `gti--ys-function-control-right97228475355xomqg14k`, `gti--ys-function-control-right972284754ymywrxmz2a`, `gti--ys-function-control-right97228475okxv7yxug5o`, `gti--ys-function-control-right97228475v8u8v86hl48`, `gti--ys-function-control-right972284765lit50wvnm4`, `gti--ys-function-control-right97228608k7xixvzoov`, `gti--ys-function-control-right97228608ql186pxxibj`, `gti--ys-function-control-right97228609xzup3lu787b`, `gti--ys-function-control-right97228610rjjbts4h8y`, `gti--ys-function-control-right97228611m1adv8aky4`, `gti--ys-function-control-rule97228611i9hf4lmh7lg`, `gti--ys-function-control-rule97228612f4h3clocnb9`, `gti--ys-function-control-rule97228613pobo5373xvd`, `gti--ys-function-tool-settings97227557ewo42liq8h`, `gti--ys-game-sign-events972286468mkwp299ckf`, `gti--ys-game-sign-setup97227556sr83rgof0p`, `gti--ys-game-sign-spiral97228637q3bku85bo3`, `gti--ys-game-sign-wish97228641jg8nlnfy0vo`

### `docs/body-detail.html`

`10000143`, `2`, `__gt-modal__`, `__gt_popup__`, `gti--ys-function-control-right98137999kxn6cr5efw`, `gti--ys-function-control-right981380014tivjm723ta`, `gti--ys-function-control-right98138001b2mt14jz4h9`, `gti--ys-function-control-right98138002n3i4z0d2xxh`, `gti--ys-function-tool-edit98137710qwhwfuxm41`, `gti--ys-game-sign-cup98137711x78kiazcpb`, `gti--ys-game-sign-hourglass98137709l83x2dxqju`, `gti--ys-game-sign-pileum98137714j0lbfshzilk`, `miHoYoUserModelLoading`, `miHoYoUserModelLoadingText`, `own:1_tee488sza8n`, `own:2_uw89wza461`, `role_detail_list_swiper_item_10000003`, `role_detail_list_swiper_item_10000006`, `role_detail_list_swiper_item_10000007`, `role_detail_list_swiper_item_10000014`, `role_detail_list_swiper_item_10000015`, `role_detail_list_swiper_item_10000016`, `role_detail_list_swiper_item_10000020`, `role_detail_list_swiper_item_10000021`, `role_detail_list_swiper_item_10000023`, `role_detail_list_swiper_item_10000024`, `role_detail_list_swiper_item_10000025`, `role_detail_list_swiper_item_10000027`, `role_detail_list_swiper_item_10000031`, `role_detail_list_swiper_item_10000032`, `role_detail_list_swiper_item_10000034`, `role_detail_list_swiper_item_10000035`, `role_detail_list_swiper_item_10000036`, `role_detail_list_swiper_item_10000039`, `role_detail_list_swiper_item_10000041`, `role_detail_list_swiper_item_10000043`, `role_detail_list_swiper_item_10000044`, `role_detail_list_swiper_item_10000045`, `role_detail_list_swiper_item_10000048`, `role_detail_list_swiper_item_10000050`, `role_detail_list_swiper_item_10000052`, `role_detail_list_swiper_item_10000053`, `role_detail_list_swiper_item_10000055`, `role_detail_list_swiper_item_10000056`, `role_detail_list_swiper_item_10000067`, `role_detail_list_swiper_item_10000068`, `role_detail_list_swiper_item_10000069`, `role_detail_list_swiper_item_10000073`, `role_detail_list_swiper_item_10000074`, `role_detail_list_swiper_item_10000076`, `role_detail_list_swiper_item_10000077`, `role_detail_list_swiper_item_10000079`, `role_detail_list_swiper_item_10000080`, `role_detail_list_swiper_item_10000083`, `role_detail_list_swiper_item_10000085`, `role_detail_list_swiper_item_10000087`, `role_detail_list_swiper_item_10000088`, `role_detail_list_swiper_item_10000089`, `role_detail_list_swiper_item_10000090`, `role_detail_list_swiper_item_10000092`, `role_detail_list_swiper_item_10000097`, `role_detail_list_swiper_item_10000100`, `role_detail_list_swiper_item_10000105`, `role_detail_list_swiper_item_10000109`, `role_detail_list_swiper_item_10000110`, `role_detail_list_swiper_item_10000111`, `role_detail_list_swiper_item_10000113`, `role_detail_list_swiper_item_10000115`, `role_detail_list_swiper_item_10000116`, `role_detail_list_swiper_item_10000117`, `role_detail_list_swiper_item_10000118`, `role_detail_list_swiper_item_10000120`, `role_detail_list_swiper_item_10000121`, `role_detail_list_swiper_item_10000124`, `role_detail_list_swiper_item_10000125`, `role_detail_list_swiper_item_10000126`, `role_detail_list_swiper_item_10000127`, `role_detail_list_swiper_item_10000130`, `role_detail_list_swiper_item_10000137`, `role_detail_list_swiper_item_10000140`, `role_detail_list_swiper_item_10000143`, `role_detail_list_swiper_item_10000148`, `role_detail_list_swiper_item_10000150`

### `docs/enhancement-calculator-body.html`

`__gt-modal__`, `__gt_popup__`, `gti--ys-function-tool-more99208218teq7xiuccar`, `miHoYoUserModelLoading`, `miHoYoUserModelLoadingText`, `own:1_ovl5oxcyv3j`, `own:2_5b0llynm634`

### `docs/share-page.html`

`anchor_calendar`, `anchor_challenge`, `anchor_character`, `anchor_exploration`, `anchor_serenitea`, `anchor_spiral_abyss`, `anchor_tcg`, `anchor_tools`, `gti--ys-function-arrow-arrowright93236856d04e0cou4e`, `gti--ys-function-arrow-right93235710u0e0k73c9xf`, `gti--ys-function-arrow-right93235711e6ffpevbelf`, `gti--ys-function-arrow-right93235711ouxiouf71rm`, `gti--ys-function-control-arrowright93236854yrizx5hmwin`, `gti--ys-function-control-right93234168z934mdgzsyn`, `gti--ys-function-control-right932352646yp4ry49b35`, `gti--ys-function-control-right93235265hxbxxyhxtah`, `gti--ys-function-control-right93235265jgchel6t5dl`, `gti--ys-function-control-right93235266iw8r7lbgoop`, `gti--ys-function-control-right93235266n1h63wkzi9`, `gti--ys-function-control-right93235266t2f6shr4gd`, `gti--ys-function-control-right93235267a159lsrxxnu`, `gti--ys-function-control-right93235267uzx5zhcg96l`, `gti--ys-function-control-right93235268d72m8txlwh`, `gti--ys-function-control-right93235815a4rp1lvj57s`, `gti--ys-function-control-right9323581677r5ng261q8`, `gti--ys-function-control-right93235816hxxc8251zg`, `gti--ys-function-control-right93235816ywz78mrjzb`, `gti--ys-function-control-right93235817dkkzs4q7bjt`, `gti--ys-function-control-rule93235551eu8ybmmtt8`, `gti--ys-function-control-rule93235551mjgg0fsc33`, `gti--ys-function-control-rule932355579fyle7rw4ka`, `gti--ys-function-tool-settings93234169bgmpo5ruqs`, `gti--ys-game-sign-events93236873zgwjf5kj0eb`, `gti--ys-game-sign-setup93234171xej1tirheyf`, `gti--ys-game-sign-spiral93236112ddwpnytjok`, `gti--ys-game-sign-wish932368660bo2l46b1mqb`

## Inline style inventory by fragment

These are distinct literal `style` attribute values. They may include data-driven values and should not automatically become global design tokens.

### `docs/index-page-info.html`

- `background-image: url("https://act-webstatic.hoyoverse.com/game_record/genshin/gcg/challenge_medal_0.png");`
- `background-image: url("images/block_more.78970179.png");`
- `background-image: url("images/block_star.1d49404a.png");`
- `background-image: url("images/center@3x.55a1e953.png");`
- `background-image: url("images/daoqi_v67.1d964bb2.png");`
- `background-image: url("images/fengdan_v67.716ff05f.png");`
- `background-image: url("images/kongzhishendian_v67.654a43a6.png");`
- `background-image: url("images/liyue_v67.ae0a1bd9.png");`
- `background-image: url("images/mengde_v67.ee29b572.png");`
- `background-image: url("images/nata_v67.111a3d52.png");`
- `background-image: url("images/nodkrai_v67.91f8bb94.png");`
- `background-image: url("images/xumi_v67.4fa40e30.png");`
- `background-image: url("images/zhidong_v70.9b6d008b.png");`
- `background: linear-gradient(103deg, rgb(0, 192, 255) 50%, rgb(0, 115, 153) 100%);`
- `background: linear-gradient(103deg, rgb(25, 86, 150) 50%, rgb(9, 121, 172) 100%);`
- `background: linear-gradient(103deg, rgb(64, 255, 232) 50%, rgb(38, 124, 153) 100%);`
- `background: linear-gradient(128deg, rgb(255, 180, 35) 50%, rgb(138, 13, 13) 100%);`
- `background: linear-gradient(139deg, rgb(204, 128, 255) 50%, rgb(122, 77, 153) 100%);`
- `background: linear-gradient(140deg, rgb(255, 80, 64) 50%, rgb(153, 38, 38) 100%);`
- `background: linear-gradient(140deg, rgb(75, 210, 255) 50%, rgb(38, 124, 153) 100%);`
- `background: linear-gradient(153deg, rgb(52, 234, 81) 50%, rgb(26, 145, 82) 100%);`
- `background: rgb(103, 80, 75);`
- `background: rgb(124, 99, 76);`
- `background: rgb(47, 120, 162);`
- `background: rgb(68, 82, 111);`
- `background: rgb(74, 100, 92);`
- `background: rgb(75, 86, 129);`
- `background: rgb(85, 79, 89);`
- `background: rgb(94, 100, 77);`
- `display: none;`
- `font-size: 12px;`
- `font-size: 12px; transform: scale(0.8);`
- `margin-bottom: 8px;`
- `margin-left: -16px;`
- `margin-left: -8px;`
- `margin-left: 0px;`
- `transform: translate3d(0%, 0px, 0px); transition-duration: 0.3s;`
- `transform: translateX(81.5px) translateX(-50%);`
- `transition-duration: 0ms; transform: translateX(0px); width: 674px;`
- `width: 100%;`
- `width: 28%;`
- `width: 337px;`
- `width: 62%;`
- `width: 74%;`
- `width: 96%;`

### `docs/body-detail.html`

- `--rvh: 6.88px;`
- `background-image: url("images/roleStarBg5.30180c91.png");`
- `background: linear-gradient(rgba(32, 47, 90, 0) 10%, rgb(20, 57, 62) 55%);`
- `display: none;`
- `font-family: HYWenHei !important;`
- `font-size: 0.12rem; line-height: 0.16rem;`
- `font-size: 0.1rem; line-height: 0.14rem;`
- `height: 0px;`
- `height: calc(var(--rvh, 1vh) * 100 - 96px);`
- `margin-top: 0px;`
- `margin-top: 46px;`
- `opacity: 1; visibility: visible;`
- `padding-top: 0px; background-color: rgba(42, 42, 42, 0); border-bottom-color: transparent;`
- `position: absolute; left: -99999px; top: -99999px;`
- `top: 0px; background: rgb(20, 57, 62);`
- `z-index: 3000;`
- `z-index: 3001;`

### `docs/enhancement-calculator-body.html`

- `font-family: HYWenHei !important;`
- `height: calc(46.112px);`
- `padding-top: 0px;`
- `position: absolute; left: -99999px; top: -99999px;`
- `z-index: 0; display: none;`
- `z-index: 3000;`
- `z-index: 3100;`

### `docs/share-page.html`

- `background-image: url("https://act-webstatic.hoyoverse.com/game_record/genshin/gcg/challenge_medal_0.png");`
- `background-image: url("https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/dc186b2a995c8eca19ee63569cdea866.png?x-oss-process=image%2Fformat%2Cwebp");`
- `background-image: url("images/block_more.78970179.png");`
- `background-image: url("images/block_star.1d49404a.png");`
- `background-image: url("images/center@3x.55a1e953.png");`
- `background-image: url("images/daoqi_v67.1d964bb2.png");`
- `background-image: url("images/fengdan_v67.716ff05f.png");`
- `background-image: url("images/kongzhishendian_v67.654a43a6.png");`
- `background-image: url("images/liyue_v67.ae0a1bd9.png");`
- `background-image: url("images/logo_default.cb2f6400.png");`
- `background-image: url("images/mengde_v67.ee29b572.png");`
- `background-image: url("images/nata_v67.111a3d52.png");`
- `background-image: url("images/nodkrai_v67.91f8bb94.png");`
- `background-image: url("images/xumi_v67.4fa40e30.png");`
- `background-image: url("images/ys@2x.b700cce2.png");`
- `background-image: url("images/zhidong_v70.9b6d008b.png");`
- `background: linear-gradient(103deg, rgb(0, 192, 255) 50%, rgb(0, 115, 153) 100%);`
- `background: linear-gradient(103deg, rgb(25, 86, 150) 50%, rgb(9, 121, 172) 100%);`
- `background: linear-gradient(103deg, rgb(64, 255, 232) 50%, rgb(38, 124, 153) 100%);`
- `background: linear-gradient(128deg, rgb(255, 180, 35) 50%, rgb(138, 13, 13) 100%);`
- `background: linear-gradient(139deg, rgb(204, 128, 255) 50%, rgb(122, 77, 153) 100%);`
- `background: linear-gradient(140deg, rgb(255, 80, 64) 50%, rgb(153, 38, 38) 100%);`
- `background: linear-gradient(140deg, rgb(75, 210, 255) 50%, rgb(38, 124, 153) 100%);`
- `background: linear-gradient(153deg, rgb(52, 234, 81) 50%, rgb(26, 145, 82) 100%);`
- `background: rgb(103, 80, 75);`
- `background: rgb(124, 99, 76);`
- `background: rgb(47, 120, 162);`
- `background: rgb(68, 82, 111);`
- `background: rgb(74, 100, 92);`
- `background: rgb(75, 86, 129);`
- `background: rgb(85, 79, 89);`
- `background: rgb(94, 100, 77);`
- `display: none;`
- `font-size: 12px;`
- `font-size: 12px; transform: scale(0.8);`
- `height: 0px;`
- `margin-bottom: 8px;`
- `margin-left: -16px;`
- `margin-left: -8px;`
- `margin-left: 0px;`
- `padding-top: 0px; background-color: rgba(42, 42, 42, 0); border-bottom-color: transparent;`
- `transform: translate3d(0%, 0px, 0px); transition-duration: 0.3s;`
- `transform: translateX(167px) translateX(-50%); transition-duration: 0.3s;`
- `transform: translateY(0px);`
- `transition-duration: 0ms; transform: translateX(0px); width: 554px;`
- `width: 100%;`
- `width: 277px;`
- `width: 28%;`
- `width: 62%;`
- `width: 74%;`
- `width: 96%;`

## CSS image and decorative asset references

- `images/risk-m-bg.1f090d1d.png`
- `images/role-medium-split-light@3x.c12c68a8.png`
- `images/role-medium-split@3x.80702c06.png`
- `images/sumo_skill_bg@2x.f5a1540c.png`
- `images/ys_dialog_border@2x.cad41d3a.png`
- `images/ys_dialog_border_top@2x.e90fde6c.png`
- `images/ys_dialog_buff_border@2x.d40d871f.png`
- `images/ys_dialog_close@2x.09d84262.png`
- `images/ys_dialog_skill_bg@2x.50a0e5a0.png`
- `images/ys_dialog_skill_border@2x.3781e94e.png`
- `images/ys_dialog_skill_right_bg@2x.89af0be2.png`

## HTML image-file references

- `https://act-webstatic.hoyoverse.com/game_record/genshin/avatar_prop/avatar_prop_20.png`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/avatar_prop/avatar_prop_2000.png`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/avatar_prop/avatar_prop_2001.png`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/avatar_prop/avatar_prop_2002.png`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/avatar_prop/avatar_prop_23.png`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/avatar_prop/avatar_prop_26.png`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/avatar_prop/avatar_prop_28.png`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/avatar_prop/avatar_prop_44.png`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/home/UI_Homeworld_Comfort_10.png`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/tool/bbs-lineup-qskp.png?x-oss-process=image%2Fformat%2Cwebp`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/tool/calculator_new.png?x-oss-process=image%2Fformat%2Cwebp`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/tool/map_new.png?x-oss-process=image%2Fformat%2Cwebp`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/tool/sign_in_new.png?x-oss-process=image%2Fformat%2Cwebp`
- `https://act-webstatic.hoyoverse.com/game_record/genshin/tool/wiki_new.png?x-oss-process=image%2Fformat%2Cwebp`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/image_7/e1817f5bbeed3c7b882da7bdf736d15b.png?x-oss-process=image%2Fquality%2CQ_75`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/0a024f0f2e06e99d67f112cd12eef4af.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/0a8a2c7373dcc8f2549b645d0c3374b7.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/1238a7679cc90e138ec793b08aa853c7.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/1791a03bc18b12a8030a4e16df2ea5f6.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/192f70211e4456786ebb3cb0c9222e43.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/1cebf43ac944ad3ff2a0581bebe309d8.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/1eb5023b8ca517a5fe27316b3771a191.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/1fa9d9e4780785e7ab6c2ac10e556527.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/207a0e35e0637a2bcb0a28fdfa61f7a4.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/23aa979c8e86b7006a62039b9dd81c5b.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/2747d92218297b5094d4b8fe6d5f6475.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/27ee1aba588f3e8e42800249fe1047e8.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/2825ff5127afc078dd1bd3e1b2e39996.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/2c4b6f3e78da44cbe10f9cbc37a50fe1.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/34ef1ba51a4893d9df4be717e56891ae.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/38b5bd856ea9fff842477a77265da49b.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/3dc3a368e5c3be42a05afb7a9fdc9c41.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/415cb4af1a402f97aebfcf8edc2ca235.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/41a97aae8bf579ce350a90bbddc59fdf.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/523506069469ad5f09b79daf6ed3a285.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/57ede6ce84e7030b58af29520198119b.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/58a03d4cded3b6e93af3cc3efb708031.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/5dad272ce992b32d3aa7a708869d1a4a.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/62ddf4187c2aea2dda21813b9d90f27f.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/64a90c55b4bb72cf06068f3d82dbc8da.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/7987cb608a5375cdfd1e34dda9455a31.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/7c047dce3a1be70baa30f28111222e38.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/817f1d6da2a3ff6e6bfedeb39104ad71.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/823e31bf43f378bd66c394985a323da8.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/929d6645727e400b3a294b559e36a03f.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/9f1d53d9f4d0fe7ea1c605cc873dd626.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/9fae2c3ff5862c261c8ad7200084f567.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/a3a8da43e8c4d76986237ab784e49070.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/a862d7fc50ffe15c045de4f6799a357b.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/a9184535c8743e8da52fa2debd54f7d8.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/ad90ce11f6af2cf1db44380af745e4ec.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/afac47d999995ce3577c7034ff277c44.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/b30987d539b299dc89004d72274f5e89.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/bc2903d1a46616c871d5108d033a463d.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/c009627a0c0289646a10d41779001b9d.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/c836c2dc7d71fe8065ae2798d360167b.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/d4892796ad70d139c13a6e42d170b877.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/d7ec9b285faccd804b54fa026dde96e8.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/e7beb9e36337d7f64139ee0c97d1a93a.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/eaf61637b76953695940fe1ceedb18a0.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/ec67809641b4f1d2d2e87bc0e1e88b93.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/f239b3032e85ebb64554e69a58eec91b.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/f4e68e94225fa95112ae03c0a6d10938.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6c8/f55430aba2d84b36d64136958c58c93a.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6fc/46bf6c2c183de638ec56574a3f961cb4.png?x-resource-process=image%2Fresize%2Cm_fixed%2Ch_275%2Cw_160&amp;x-oss-process=image%2Fresize%2Cw_200%2Fformat%2Cwebp`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6fc/5b454a539f6c902acd76d533663b7997.png?x-resource-process=image%2Fresize%2Cm_fixed%2Ch_275%2Cw_160&amp;x-oss-process=image%2Fresize%2Cw_200%2Fformat%2Cwebp`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6fc/db9ca37d274b1dcdf01dc98a0ebb72ce.png?x-resource-process=image%2Fresize%2Cm_fixed%2Ch_275%2Cw_160&amp;x-oss-process=image%2Fresize%2Cw_200%2Fformat%2Cwebp`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f6fc/dcf46920df471e6c0aee398047123baa.png?x-resource-process=image%2Fresize%2Cm_fixed%2Ch_275%2Cw_160&amp;x-oss-process=image%2Fresize%2Cw_200%2Fformat%2Cwebp`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f716/4acd74e1564d0d490bc15154595e0069.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f716/6a05b613714d7d352b45141ff375c59d.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f716/ae3db9ad714e4247e905058c516a9172.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f716/c2732a74d3c6fdceaf0129ac656203aa.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f716/edfbeee2e64b669ff695e609a0e349e7.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67c7f71a/3f016e2d606ca39ea86e24d5245dfa5b.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67e3357c/0a8076edeee37ec87809cfe9fb51aa9b.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67e3357c/34596fcb08c5c4b4e91379507e75dba7.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67e3357c/db3c23247fa556442b7d1fdd5e4a4fb6.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/67e3357c/f44e8b4c7a61a4abb2aa2ccfe9b810cd.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/681a947b/0b3127bb721214a48b20001c64ab727a.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6851f37b/9e5b1c5805948358c656e702e5eecae2.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6889608b/f1723762f28902659f73902bbbda1fd6.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/68c0b17a/6aae9f16cbe14ac60dd6eb2956e4acf2.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/68c0b17a/cfec735e9e867016be94f500e9dae122.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/68c0b1bb/71e19bba003e1ec9c7e7832a28c6a0da.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/692f7d8b/8ce70ee6c41e48a78bdb1a17be8b554e.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6966dc8c/3e648c232fb84bea351096c3f2129489.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6966dc8c/64666707baedabc3e6a7d80738d24730.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6966dc8c/b7ea76f3f77eec4997dad6576c183939.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6966dceb/3bbae77703f1013fc8a66924f94a45b0.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/699e3449/3ae1f1d40773cd13d361e329a7efd887.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/699e3449/3de7b18fbecffbb57a73fe5032d24950.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/699e3449/9d2730b17e4cfdc85b52b163948a8c12.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/699e3449/b3085c5955360785fc5a1c25ab1a15f4.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/699e3449/c1d2a8e0948af4942feb5d086500528b.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/699e3449/cf352253ba22524007154d6d6fc66b55.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/699e3449/e73c8896a2a5e52cfaa8847daaee53b8.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/699e3449/eabf3af20320ed8dc3fed73ab11148cc.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/69d58c7d/d1c44e49e1fcfdf9474ba6c2d2c9369d.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6a7ba976/0119db4ca0f7c650686e2da11b9c6a1d.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6a7ba976/09729586ce0f78f5111f9ee8d25eb431.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6a7ba976/11eeed27d2d7fbb0ab34f03c87325688.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6a7ba976/2aece34359e0f99802feced2f25ee01b.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6a7ba976/38c83cb1b5c605921b3d773d9e5dffc2.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6a7ba976/6d43279928ae731e4199ff123ecd210e.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6a7ba976/80425abeeb96bfefbfcb2b49cd5339db.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6a7ba976/d653c3aa684c882bbf09757f695ab533.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6a7ba976/f60c04735c5a36d349a2802b51e9b6fc.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6a7ba976/faa96b61235674d245e3134c4175f684.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31686/750126424226dddd1dbe8a75db5cbad1.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31686/88e4654b892a5ada8d0fd21301c07518.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31686/a6fbdeef294997e18e778f4cba34df66.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31686/ecedbeb41258bf291e127603ea519449.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31695/0be256b007885064a71e136a8b5f9b86.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31695/71711cd1598a001ffcbc3e565fccdd95.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31695/7fdd7b7f72512d8e92a8965e5e89b813.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31695/80d0d1c5eaa8d483f85e2a6ced11495e.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31695/931536cfba9ddecde0b17e8648cdfb30.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31695/9786d20583f617c1fdd9c196bb8eb6d6.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31695/9bfe7227048168a6191a341b58af7beb.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31695/a5687108b94cfaacaadb3c20eef945f2.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31695/c377cf652e41042f3260ce02942d94ff.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31695/c69f680aafe793aab73cba3a27a06371.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31695/c95abaf61e9ffd2626d9bd020c27e526.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31695/f856b92769c4b7a96332ed28d5c0fb55.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31696/29b50cdb9288163fc60662473ecf44d5.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6ab31696/af2310a209ec21f7d7ef986ac38c58d1.png`
- `https://act-webstatic.hoyoverse.com/hk4e/e20200928calculate/item_icon/6aea6776/2cd496faf46d4727430723b840e61881.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://act.hoyolab.com/gt-ui/assets/icons/247f14512efc8325.png`
- `https://act.hoyolab.com/gt-ui/assets/icons/48d1aac6ecc56b33.png`
- `https://act.hoyolab.com/gt-ui/assets/icons/829a6b86fb23d8bb.png`
- `https://act.hoyolab.com/gt-ui/assets/icons/b162f5384487d283.png`
- `https://act.hoyolab.com/gt-ui/assets/icons/e18d224ec1047cae.png`
- `https://fastcdn.hoyoverse.com/mi18n/bbs_oversea/m11241040191111/upload/fce31352f3e05f0041e70da23d46aa7c_2820709750037226161.jpg?x-oss-process=image%2Fquality%2CQ_75`
- `https://fastcdn.hoyoverse.com/static-resource-v2/2023/09/26/10dede5c05005ebb96bbe01f8408e775_7397178366194463086.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://fastcdn.hoyoverse.com/static-resource-v2/2025/10/16/7328f006a498626b32a1e187c0dba1ea_7198130205836288559.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://fastcdn.hoyoverse.com/static-resource-v2/2025/10/16/af27ded694227c5c49f0f75730eecbb9_5249597265949308504.png?x-oss-process=image/format,webp/quality,Q_90`
- `https://hyl-static-res-prod.hoyolab.com/communityWeb/upload/9fc58c36fc45bfc64211f5254378d788.png?x-oss-process=image%2Fformat%2Cwebp`
- `images/Anemo-lock-rank-bg.9e02ea61.png`
- `images/UI_Buff_Element_Frost.95c777ac.png`
- `images/UI_Buff_Element_Wind.bf653cfd.png`
- `images/account-switch.63c909c8.png`
- `images/active-medal-icon.b3adc53a.png`
- `images/arrow-right-icon.28c17929.png`
- `images/arrow-right-icon.8cd2964d.png`
- `images/back-icon.1f4a7b20.png`
- `images/bottom@3x.25b443d9.png`
- `images/calculator.be2fdafb.png`
- `images/daoqi_icon.59282e8d.png`
- `images/entry_block_icon.c92a179f.png`
- `images/favor-icon.389b76d0.png`
- `images/fengdan_icon.9bbccb77.png`
- `images/flower-icon.dd04a858.png`
- `images/heraldry-icon-4.0097b91f.png`
- `images/hot_role.964c8037.png`
- `images/hoyoguides.ba0f178d.png`
- `images/hoyolab_logo.6652500b.png`
- `images/icBack.801a4ad3.png`
- `images/icLang.5094a130.png`
- `images/icSet.3fd9e917.png`
- `images/icShare.cd690f8b.png`
- `images/icon_lang@2x.0cd4d0a9.png`
- `images/index-block-title-icon.4fe47393.png`
- `images/info-icon.e3cbea73.png`
- `images/kongzhishendian_icon.c7824333.png`
- `images/l_3.ffff4ee0.png`
- `images/lineup.45a004f0.png`
- `images/liyue_icon.15f334b5.png`
- `images/lock-icon.6f2531ab.png`
- `images/medal_iv.9e37f220.png`
- `images/medal_iv.bd56867e.png`
- `images/mengde_icon.a5854558.png`
- `images/nata_icon.4e68bdf3.png`
- `images/newframe@3x.1aa620ed.png`
- `images/nodkrai_icon.03c7e533.png`
- `images/record_qrcode.2b038427.png`
- `images/relic-5@3x.8b881069.png`
- `images/relic-selected-icon.a80efaba.png`
- `images/right-arrow-icon.eb5018f3.png`
- `images/search_entry.1dec32fb.png`
- `images/share-icon.c4ab7862.png`
- `images/split@3x.5d1b6397.png`
- `images/star-icon.345ec73f.png`
- `images/top@3x.76c10d48.png`
- `images/wiki.be2282d6.png`
- `images/xumi_icon.86a641c4.png`
- `images/zhidong_icon.1de880f9.png`

## CSS rules matching classes used in the HTML extracts

A selector is included when at least one class token in it occurs in one of the four HTML fragments. This includes descendant/pseudo/attribute selectors. It does not establish that every selector applies to the exact fragment or runtime state.

- `/*! Copyright © 2011 - 2026 miHoYo. All Rights Reserved */ .lang-btn[data-v-137fe46b]` → `display:flex;height:100%;align-items:center`
- `.lang-btn[data-v-137fe46b] .van-popover__wrapper` → `display:flex;align-items:center`
- `.lang-btn .lang[data-v-137fe46b]` → `width:calc(24 * var(--gpx));height:calc(24 * var(--gpx))`
- `body[data-sharing] .nav-bar[data-v-4b6b351e]` → `display:none`
- `.container[data-v-4b6b351e]` → `position:relative;display:flex;align-items:center;justify-content:space-between;padding:calc(10 * var(--gpx)) calc(16 * var(--gpx))`
- `.ic[data-v-4b6b351e]` → `-webkit-user-select:none;-moz-user-select:none;user-select:none;width:calc(24 * var(--gpx));height:calc(24 * var(--gpx));flex-shrink:0`
- `.left[data-v-4b6b351e],.right[data-v-4b6b351e]` → `display:flex;align-items:center`
- `.center[data-v-4b6b351e]` → `position:absolute;top:50%;left:50%;-webkit-transform:translate(-50%, -50%);transform:translate(-50%, -50%)`
- `.title[data-v-4b6b351e]` → `text-align:center;font-size:calc(16 * var(--gpx));font-weight:600;line-height:calc(24 * var(--gpx))`
- `body[data-game-id="2"] .title[data-v-4b6b351e]` → `color:#d3bc8e;letter-spacing:calc(.1 * var(--gpx))`
- `.right>*[data-v-4b6b351e]` → `margin-left:calc(12 * var(--gpx))`
- `.geetest-page[data-v-01889dd6] .nav-bar` → `border-bottom:none`
- `.geetest-page .content[data-v-01889dd6]` → `width:calc(345 * var(--gpx));height:calc(700 * var(--gpx));margin:0 auto;background:url(images/risk-m-bg.1f090d1d.png) no-repeat left top;background-size:100% 100%;padding-top:calc(140 * var(--gpx));box-sizing:border-box`
- `.geetest-page .content .icon[data-v-01889dd6]` → `display:block;width:calc(140 * var(--gpx));height:calc(140 * var(--gpx));margin:0 auto`
- `.geetest-page .content .message[data-v-01889dd6]` → `width:calc(280 * var(--gpx));margin:0 auto;color:rgba(255,255,255,.65);text-align:center;font-family:"PingFang SC";font-size:calc(14 * var(--gpx));font-style:normal;font-weight:400;line-height:calc(20 * var(--gpx))`
- `.geetest-page .content .tip[data-v-01889dd6]` → `width:calc(280 * var(--gpx));margin:calc(206 * var(--gpx)) auto 0;color:rgba(255,255,255,.45);text-align:center;font-family:"PingFang SC";font-size:calc(12 * var(--gpx));font-style:normal;font-weight:500;line-height:calc(16 * var(--gpx))`
- `.m-root-layout` → `min-height:100vh;background-color:#2a2a2a`
- `.mhy-skill-dialog .container` → `min-height:calc(152 * var(--gpx));padding:calc(15 * var(--gpx));position:relative`
- `.mhy-skill-dialog .container .info` → `margin-bottom:calc(10 * var(--gpx));display:flex;align-items:center`
- `.mhy-skill-dialog .container .info .skill-bg` → `margin-right:calc(10 * var(--gpx));background-image:url(images/sumo_skill_bg@2x.f5a1540c.png);background-size:100% auto;width:calc(48 * var(--gpx));height:calc(48 * var(--gpx));position:relative;display:inline-block`
- `.mhy-skill-dialog .container .info .skill-bg .icon` → `display:inline-block;position:absolute;left:50%;top:50%;-webkit-transform:translate(-50%, -50%);transform:translate(-50%, -50%);width:calc(34 * var(--gpx));height:calc(34 * var(--gpx))`
- `.mhy-skill-dialog .container .info .name` → `font-size:4.26667vw;font-family:Microsoft YaHei,sans-serif;font-weight:500;color:#333;line-height:1.5`
- `.mhy-skill-dialog .container .desc` → `font-size:3.2vw;font-family:Microsoft YaHei,sans-serif;color:#7f858a;line-height:calc(16 * var(--gpx))`
- `.mhy-rune-dialog .container[data-v-043f08a2]` → `min-height:calc(152 * var(--gpx));padding:calc(15 * var(--gpx));position:relative;display:flex`
- `.mhy-rune-dialog .container .logo[data-v-043f08a2]` → `display:inline-block;width:calc(48 * var(--gpx));height:calc(92 * var(--gpx));margin-right:calc(10 * var(--gpx));margin-top:calc(4 * var(--gpx))`
- `.mhy-rune-dialog .container .info .inner[data-v-043f08a2]` → `margin-bottom:calc(5 * var(--gpx))`
- `.mhy-rune-dialog .container .info .inner .name[data-v-043f08a2]` → `font-size:4.26667vw;font-family:Microsoft YaHei,sans-serif;font-weight:500;color:#333;line-height:1.5;margin-right:calc(5 * var(--gpx))`
- `.mhy-rune-dialog .container .info .inner .element[data-v-043f08a2]` → `width:calc(22 * var(--gpx));height:calc(24 * var(--gpx));vertical-align:bottom`
- `.mhy-rune-dialog .container .info .desc[data-v-043f08a2]` → `font-size:3.2vw;font-family:Microsoft YaHei,sans-serif;color:#7f858a;line-height:calc(16 * var(--gpx));max-height:calc(87 * var(--gpx));overflow-y:auto;position:relative;z-index:10;padding-right:calc(5 * var(--gpx))`
- `.mhy-rune-dialog .container .info .desc[data-v-043f08a2]::-webkit-scrollbar` → `width:calc(4 * var(--gpx));height:calc(4 * var(--gpx))`
- `.mhy-rune-dialog .container .info .desc[data-v-043f08a2]::-webkit-scrollbar-track` → `width:calc(10 * var(--gpx))`
- `.mhy-rune-dialog .container .info .desc[data-v-043f08a2]::-webkit-scrollbar-thumb` → `background:#d2c4aa;border-radius:calc(2 * var(--gpx))`
- `.buff-dialog .container[data-v-15f88bea]` → `min-height:calc(152 * var(--gpx));padding:calc(15 * var(--gpx));position:relative`
- `.buff-dialog .container .info[data-v-15f88bea]` → `display:flex;align-items:center`
- `.buff-dialog .container .info .level[data-v-15f88bea]` → `width:calc(4 * var(--gpx));height:calc(16 * var(--gpx));margin-right:calc(5 * var(--gpx))`
- `.buff-dialog .container .info .name[data-v-15f88bea]` → `height:calc(24 * var(--gpx));font-size:calc(16 * var(--gpx));font-weight:500;color:#1e1f20;line-height:calc(24 * var(--gpx));margin-right:calc(5 * var(--gpx))`
- `.buff-dialog .container .energy[data-v-15f88bea]` → `height:calc(12 * var(--gpx));font-size:calc(12 * var(--gpx));font-weight:400;color:#1e1f20;line-height:calc(12 * var(--gpx))`
- `.buff-dialog .container .desc-container[data-v-15f88bea]` → `min-height:calc(120 * var(--gpx));max-height:calc(120 * var(--gpx));margin-top:calc(9 * var(--gpx))`
- `.buff-dialog .container .desc[data-v-15f88bea]` → `font-size:calc(12 * var(--gpx));color:#7f858a;line-height:calc(16 * var(--gpx))`
- `.buff-dialog .container .os-scrollbar-vertical>.os-scrollbar-track>.os-scrollbar-handle[data-v-15f88bea]` → `background-color:#c8baad;border-radius:calc(3 * var(--gpx))`
- `.empty-block .icon[data-v-649410d6]` → `width:calc(120 * var(--gpx));height:calc(120 * var(--gpx));-o-object-fit:contain;object-fit:contain;-o-object-position:top center;object-position:top center`
- `.empty-block .text[data-v-649410d6]` → `font-size:calc(16 * var(--gpx));font-family:Microsoft YaHei,sans-serif;font-weight:400;color:#c0c5c8;line-height:calc(20 * var(--gpx));text-align:center`
- `.empty-block .text[data-v-649410d6]` → `padding:0 calc(38 * var(--gpx));font-size:calc(12 * var(--gpx))`
- `.error-block-layout .icon[data-v-68174fb9]` → `margin-top:calc(77 * var(--gpx));height:calc(115 * var(--gpx))`
- `.error-block-layout .text[data-v-68174fb9]` → `margin-top:calc(24 * var(--gpx));font-size:calc(14 * var(--gpx));font-family:Microsoft YaHei,sans-serif;font-weight:400;color:#c0c5c8;line-height:calc(20 * var(--gpx));word-break:break-word`
- `.error-block-layout .icon[data-v-68174fb9]` → `margin-top:calc(84.5 * var(--gpx));height:calc(96.5 * var(--gpx))`
- `.error-block-layout .text[data-v-68174fb9]` → `padding:0 calc(38 * var(--gpx));font-size:calc(15 * var(--gpx));word-break:break-word`
- `.mhy-announcement .van-dialog__content .border .top` → `position:absolute;width:100%;top:0;background:no-repeat;background-size:100% 100%;background-image:url(images/ys_dialog_border_top@2x.e90fde6c.png);height:calc(20 * var(--gpx))`
- `.mhy-announcement .van-dialog__content .border .left` → `width:calc(2 * var(--gpx));position:absolute;left:0;top:calc(20 * var(--gpx));bottom:calc(19 * var(--gpx));background-image:url(images/ys_dialog_border@2x.cad41d3a.png);background-size:100% 100%`
- `.mhy-announcement .van-dialog__content .border .right` → `width:calc(2 * var(--gpx));position:absolute;right:0;top:calc(20 * var(--gpx));bottom:calc(19 * var(--gpx));background-image:url(images/ys_dialog_border@2x.cad41d3a.png);background-size:100% 100%;-webkit-transform:rotate(180deg);transform:rotate(180deg)`
- `.mhy-dialog .van-dialog__content .container[data-v-c80048a2]` → `position:relative;flex:1`
- `.mhy-dialog .van-dialog__content .btns .item[data-v-c80048a2]` → `width:calc(118 * var(--gpx));height:calc(34 * var(--gpx));background-color:#ece5d8;border-radius:calc(17 * var(--gpx));text-align:center;font-size:3.2vw;font-family:Microsoft YaHei,sans-serif;font-weight:500;color:#675856;line-height:calc(34 * var(--gpx));position:relative`
- `.mhy-dialog .van-dialog__content .btns .item[data-v-c80048a2]::before` → `position:absolute;top:calc(2 * var(--gpx));left:calc(2 * var(--gpx));bottom:calc(2 * var(--gpx));right:calc(2 * var(--gpx));content:" ";box-sizing:border-box;border-radius:calc(15 * var(--gpx));border:calc(1 * var(--gpx)) solid #e2d4b7`
- `.mhy-dialog .van-dialog__content .btns .item.primary[data-v-c80048a2]` → `background-color:#d2c4aa;color:#fff;border:calc(1 * var(--gpx)) solid #e7dbc5`
- `.mhy-dialog .van-dialog__content .btns .item.disabled[data-v-c80048a2]` → `color:rgba(103,88,86,.4)`
- `.avatar-container[data-v-67354b94] .gt-avatar` → `border-radius:calc(4 * var(--gpx));overflow:hidden`
- `.pop-role-card .role-info-avatar .role-medium-avatar .gt-avatar,.pop-role-card .role-info-avatar .role-medium-avatar .gt-avatar-img` → `width:calc(44 * var(--gpx));height:calc(44 * var(--gpx))`
- `.tw-fixed` → `position:fixed`
- `.tw-absolute` → `position:absolute`
- `.tw-relative` → `position:relative`
- `.tw-bottom-0` → `bottom:calc(0 * var(--gpx))`
- `.tw-left-0` → `left:calc(0 * var(--gpx))`
- `.tw-right-0` → `right:calc(0 * var(--gpx))`
- `.tw-right-3` → `right:calc(12 * var(--gpx))`
- `.tw-top-0` → `top:calc(0 * var(--gpx))`
- `.tw-z-10` → `z-index:10`
- `.tw-mx-1` → `margin-left:calc(4 * var(--gpx));margin-right:calc(4 * var(--gpx))`
- `.tw-mx-auto` → `margin-left:auto;margin-right:auto`
- `.tw-my-1` → `margin-top:calc(4 * var(--gpx));margin-bottom:calc(4 * var(--gpx))`
- `.tw-my-3` → `margin-top:calc(12 * var(--gpx));margin-bottom:calc(12 * var(--gpx))`
- `.tw-mb-1` → `margin-bottom:calc(4 * var(--gpx))`
- `.tw-mb-1\.5` → `margin-bottom:calc(6 * var(--gpx))`
- `.tw-mb-2` → `margin-bottom:calc(8 * var(--gpx))`
- `.tw-mb-2\.5` → `margin-bottom:calc(10 * var(--gpx))`
- `.tw-mb-3` → `margin-bottom:calc(12 * var(--gpx))`
- `.tw-mb-4` → `margin-bottom:calc(16 * var(--gpx))`
- `.tw-ml-1` → `margin-left:calc(4 * var(--gpx))`
- `.tw-ml-2` → `margin-left:calc(8 * var(--gpx))`
- `.tw-ml-2\.5` → `margin-left:calc(10 * var(--gpx))`
- `.tw-mr-1` → `margin-right:calc(4 * var(--gpx))`
- `.tw-mr-1\.5` → `margin-right:calc(6 * var(--gpx))`
- `.tw-mr-2` → `margin-right:calc(8 * var(--gpx))`
- `.tw-mr-2\.5` → `margin-right:calc(10 * var(--gpx))`
- `.tw-mr-3` → `margin-right:calc(12 * var(--gpx))`
- `.tw-mr-3\.5` → `margin-right:calc(14 * var(--gpx))`
- `.tw-mr-px` → `margin-right:calc(1 * var(--gpx))`
- `.tw-mt-2` → `margin-top:calc(8 * var(--gpx))`
- `.tw-mt-2\.5` → `margin-top:calc(10 * var(--gpx))`
- `.tw-mt-3` → `margin-top:calc(12 * var(--gpx))`
- `.tw-mt-5` → `margin-top:calc(20 * var(--gpx))`
- `.tw-box-border` → `box-sizing:border-box`
- `.tw-line-clamp-1` → `overflow:hidden;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:1`
- `.tw-line-clamp-2` → `overflow:hidden;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:2`
- `.tw-flex` → `display:flex`
- `.tw-inline-flex` → `display:inline-flex`
- `.tw-grid` → `display:grid`
- `.tw-h-10` → `height:calc(40 * var(--gpx))`
- `.tw-h-11` → `height:calc(44 * var(--gpx))`
- `.tw-h-12` → `height:calc(48 * var(--gpx))`
- `.tw-h-3` → `height:calc(12 * var(--gpx))`
- `.tw-h-4` → `height:calc(16 * var(--gpx))`
- `.tw-h-5` → `height:calc(20 * var(--gpx))`
- `.tw-h-5\.5` → `height:calc(22 * var(--gpx))`
- `.tw-h-7` → `height:calc(28 * var(--gpx))`
- `.tw-h-7\.5` → `height:calc(30 * var(--gpx))`
- `.tw-h-8` → `height:calc(32 * var(--gpx))`
- `.tw-h-full` → `height:100%`
- `.tw-w-2` → `width:calc(8 * var(--gpx))`
- `.tw-w-2\.5` → `width:calc(10 * var(--gpx))`
- `.tw-w-3` → `width:calc(12 * var(--gpx))`
- `.tw-w-4` → `width:calc(16 * var(--gpx))`
- `.tw-w-5` → `width:calc(20 * var(--gpx))`
- `.tw-w-5\.5` → `width:calc(22 * var(--gpx))`
- `.tw-w-7` → `width:calc(28 * var(--gpx))`
- `.tw-w-7\.5` → `width:calc(30 * var(--gpx))`
- `.tw-w-8` → `width:calc(32 * var(--gpx))`
- `.tw-w-full` → `width:100%`
- `.tw-w-px` → `width:calc(1 * var(--gpx))`
- `.tw-flex-1` → `flex:1 1 0%`
- `.tw-flex-shrink-0` → `flex-shrink:0`
- `.tw-shrink-0` → `flex-shrink:0`
- `.tw-grow` → `flex-grow:1`
- `.tw-transform-gpu` → `-webkit-transform:translate3d(var(--tw-translate-x), var(--tw-translate-y), 0) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate3d(var(--tw-translate-x), var(--tw-translate-y), 0) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
- `.tw-cursor-pointer` → `cursor:pointer`
- `.tw-select-none` → `-webkit-user-select:none;-moz-user-select:none;user-select:none`
- `.tw-grid-cols-2` → `grid-template-columns:repeat(2, minmax(0, 1fr))`
- `.tw-flex-nowrap` → `flex-wrap:nowrap`
- `.tw-items-center` → `align-items:center`
- `.tw-justify-center` → `justify-content:center`
- `.tw-justify-between` → `justify-content:space-between`
- `.tw-gap-2` → `gap:calc(8 * var(--gpx))`
- `.tw-gap-x-3` → `-webkit-column-gap:calc(12 * var(--gpx));-moz-column-gap:calc(12 * var(--gpx));column-gap:calc(12 * var(--gpx))`
- `.tw-space-x-1 > :not([hidden]) ~ :not([hidden])` → `--tw-space-x-reverse:0;margin-right:calc((4 * var(--gpx)) * 0);margin-right:calc((4 * var(--gpx)) * var(--tw-space-x-reverse));margin-right:calc(calc(4 * var(--gpx)) * 0);margin-right:calc(calc(4 * var(--gpx)) * var(--tw-space-x-reverse));margin-left:calc((4 * var(--gpx)) * (1 - 0));margin-left:calc((4 * var(--gpx)) * (1 - var(--tw-space-x-reverse)));margin-left:calc(calc(4 * var(--gpx)) * calc(1 - 0));margin-left:calc(calc(4 * var(--gpx)) * calc(1 - var(--tw-space-x-reverse)))`
- `.tw-space-x-4 > :not([hidden]) ~ :not([hidden])` → `--tw-space-x-reverse:0;margin-right:calc((16 * var(--gpx)) * 0);margin-right:calc((16 * var(--gpx)) * var(--tw-space-x-reverse));margin-right:calc(calc(16 * var(--gpx)) * 0);margin-right:calc(calc(16 * var(--gpx)) * var(--tw-space-x-reverse));margin-left:calc((16 * var(--gpx)) * (1 - 0));margin-left:calc((16 * var(--gpx)) * (1 - var(--tw-space-x-reverse)));margin-left:calc(calc(16 * var(--gpx)) * calc(1 - 0));margin-left:calc(calc(16 * var(--gpx)) * calc(1 - var(--tw-space-x-reverse)))`
- `.tw-space-y-1 > :not([hidden]) ~ :not([hidden])` → `--tw-space-y-reverse:0;margin-top:calc((4 * var(--gpx)) * (1 - 0));margin-top:calc((4 * var(--gpx)) * (1 - var(--tw-space-y-reverse)));margin-top:calc(calc(4 * var(--gpx)) * calc(1 - 0));margin-top:calc(calc(4 * var(--gpx)) * calc(1 - var(--tw-space-y-reverse)));margin-bottom:calc((4 * var(--gpx)) * 0);margin-bottom:calc((4 * var(--gpx)) * var(--tw-space-y-reverse));margin-bottom:calc(calc(4 * var(--gpx)) * 0);margin-bottom:calc(calc(4 * var(--gpx)) * var(--tw-space-y-reverse))`
- `.tw-space-y-2 > :not([hidden]) ~ :not([hidden])` → `--tw-space-y-reverse:0;margin-top:calc((8 * var(--gpx)) * (1 - 0));margin-top:calc((8 * var(--gpx)) * (1 - var(--tw-space-y-reverse)));margin-top:calc(calc(8 * var(--gpx)) * calc(1 - 0));margin-top:calc(calc(8 * var(--gpx)) * calc(1 - var(--tw-space-y-reverse)));margin-bottom:calc((8 * var(--gpx)) * 0);margin-bottom:calc((8 * var(--gpx)) * var(--tw-space-y-reverse));margin-bottom:calc(calc(8 * var(--gpx)) * 0);margin-bottom:calc(calc(8 * var(--gpx)) * var(--tw-space-y-reverse))`
- `.tw-space-y-3 > :not([hidden]) ~ :not([hidden])` → `--tw-space-y-reverse:0;margin-top:calc((12 * var(--gpx)) * (1 - 0));margin-top:calc((12 * var(--gpx)) * (1 - var(--tw-space-y-reverse)));margin-top:calc(calc(12 * var(--gpx)) * calc(1 - 0));margin-top:calc(calc(12 * var(--gpx)) * calc(1 - var(--tw-space-y-reverse)));margin-bottom:calc((12 * var(--gpx)) * 0);margin-bottom:calc((12 * var(--gpx)) * var(--tw-space-y-reverse));margin-bottom:calc(calc(12 * var(--gpx)) * 0);margin-bottom:calc(calc(12 * var(--gpx)) * var(--tw-space-y-reverse))`
- `.tw-overflow-hidden` → `overflow:hidden`
- `.tw-overflow-x-auto` → `overflow-x:auto`
- `.tw-overflow-x-scroll` → `overflow-x:scroll`
- `.tw-truncate` → `overflow:hidden;text-overflow:ellipsis;white-space:nowrap`
- `.tw-text-ellipsis` → `text-overflow:ellipsis`
- `.tw-whitespace-nowrap` → `white-space:nowrap`
- `.tw-break-words` → `word-wrap:break-word`
- `.tw-rounded` → `border-radius:calc(4 * var(--gpx))`
- `.tw-rounded-2xl` → `border-radius:calc(16 * var(--gpx))`
- `.tw-rounded-full` → `border-radius:calc(9999 * var(--gpx))`
- `.tw-rounded-lg` → `border-radius:calc(8 * var(--gpx))`
- `.tw-rounded-md` → `border-radius:calc(6 * var(--gpx))`
- `.tw-rounded-b` → `border-bottom-right-radius:calc(4 * var(--gpx));border-bottom-left-radius:calc(4 * var(--gpx))`
- `.tw-border` → `border-width:calc(1 * var(--gpx))`
- `.tw-border-t-0` → `border-top-width:calc(0 * var(--gpx))`
- `.tw-border-solid` → `border-style:solid`
- `.tw-border-gt-g-grey-black-6` → `border-color:rgba(0, 0, 0, 0.08)`
- `.tw-border-gt-g-grey-white-10` → `border-color:rgba(255, 255, 255, 0.02)`
- `.tw-border-gt-g-grey-white-3` → `border-color:rgba(255, 255, 255, 0.32)`
- `.tw-border-gt-g-grey-white-4` → `border-color:rgba(255, 255, 255, 0.16)`
- `.tw-border-gt-g-grey-white-5` → `border-color:rgba(255, 255, 255, 0.12)`
- `.tw-border-gt-g-grey-white-6` → `border-color:rgba(255, 255, 255, 0.08)`
- `.tw-border-gt-g-grey-white-7` → `border-color:rgba(255, 255, 255, 0.04)`
- `.tw-border-ys-l-brand-yellow-3` → `--tw-border-opacity:1;border-color:rgba(211, 188, 142, 1);border-color:rgba(211, 188, 142, var(--tw-border-opacity))`
- `.tw-bg-gt-g-grey-black-3` → `background-color:rgba(0, 0, 0, 0.32)`
- `.tw-bg-gt-g-grey-white-4` → `background-color:rgba(255, 255, 255, 0.16)`
- `.tw-bg-gt-g-grey-white-6` → `background-color:rgba(255, 255, 255, 0.08)`
- `.tw-bg-gt-g-grey-white-7` → `background-color:rgba(255, 255, 255, 0.04)`
- `.tw-bg-gt-g-grey-white-9` → `background-color:rgba(255, 255, 255, 0.64)`
- `.tw-bg-transparent` → `background-color:rgba(255, 255, 255, 0)`
- `.tw-bg-ys-g-func-green-2` → `--tw-bg-opacity:1;background-color:rgba(121, 178, 48, 1);background-color:rgba(121, 178, 48, var(--tw-bg-opacity))`
- `.tw-bg-ys-l-brand-yellow-3` → `--tw-bg-opacity:1;background-color:rgba(211, 188, 142, 1);background-color:rgba(211, 188, 142, var(--tw-bg-opacity))`
- `.tw-bg-ys-l-grey-white-6` → `--tw-bg-opacity:1;background-color:rgba(188, 168, 144, 1);background-color:rgba(188, 168, 144, var(--tw-bg-opacity))`
- `.tw-px-1` → `padding-left:calc(4 * var(--gpx));padding-right:calc(4 * var(--gpx))`
- `.tw-px-1\.5` → `padding-left:calc(6 * var(--gpx));padding-right:calc(6 * var(--gpx))`
- `.tw-px-2` → `padding-left:calc(8 * var(--gpx));padding-right:calc(8 * var(--gpx))`
- `.tw-px-3` → `padding-left:calc(12 * var(--gpx));padding-right:calc(12 * var(--gpx))`
- `.tw-px-4` → `padding-left:calc(16 * var(--gpx));padding-right:calc(16 * var(--gpx))`
- `.tw-py-2` → `padding-top:calc(8 * var(--gpx));padding-bottom:calc(8 * var(--gpx))`
- `.tw-py-2\.5` → `padding-top:calc(10 * var(--gpx));padding-bottom:calc(10 * var(--gpx))`
- `.tw-py-px` → `padding-top:calc(1 * var(--gpx));padding-bottom:calc(1 * var(--gpx))`
- `.tw-pb-1` → `padding-bottom:calc(4 * var(--gpx))`
- `.tw-pb-1\.5` → `padding-bottom:calc(6 * var(--gpx))`
- `.tw-pb-3` → `padding-bottom:calc(12 * var(--gpx))`
- `.tw-pb-6` → `padding-bottom:calc(24 * var(--gpx))`
- `.tw-pl-12` → `padding-left:calc(48 * var(--gpx))`
- `.tw-pl-2` → `padding-left:calc(8 * var(--gpx))`
- `.tw-pl-2\.5` → `padding-left:calc(10 * var(--gpx))`
- `.tw-pl-3` → `padding-left:calc(12 * var(--gpx))`
- `.tw-pl-5` → `padding-left:calc(20 * var(--gpx))`
- `.tw-pr-2` → `padding-right:calc(8 * var(--gpx))`
- `.tw-pr-3` → `padding-right:calc(12 * var(--gpx))`
- `.tw-pt-3` → `padding-top:calc(12 * var(--gpx))`
- `.tw-pt-4` → `padding-top:calc(16 * var(--gpx))`
- `.tw-text-center` → `text-align:center`
- `.tw-font-genshinfont` → `font-family:HYWenHei, ysfont`
- `.tw-text-base-m` → `font-size:calc(16 * var(--gpx));line-height:calc(20 * var(--gpx))`
- `.tw-text-sm-m` → `font-size:calc(14 * var(--gpx));line-height:calc(18 * var(--gpx))`
- `.tw-text-xs` → `font-size:calc(12 * var(--gpx));line-height:calc(16 * var(--gpx))`
- `.tw-text-xs-m` → `font-size:calc(12 * var(--gpx));line-height:calc(16 * var(--gpx))`
- `.tw-text-xxs-m` → `font-size:calc(10 * var(--gpx));line-height:calc(12 * var(--gpx))`
- `.tw-font-medium` → `font-weight:500`
- `.tw-font-normal` → `font-weight:400`
- `.tw-font-semibold` → `font-weight:600`
- `.tw-leading-3` → `line-height:calc(12 * var(--gpx))`
- `.tw-text-gt-g-grey-white-1` → `color:rgba(255, 255, 255, 1)`
- `.tw-text-gt-g-grey-white-2` → `color:rgba(255, 255, 255, 0.48)`
- `.tw-text-gt-g-grey-white-8` → `color:rgba(255, 255, 255, 0.8)`
- `.tw-text-gt-g-grey-white-9` → `color:rgba(255, 255, 255, 0.64)`
- `.tw-text-gt-l-text-white-1` → `--tw-text-opacity:1;color:rgba(255, 255, 255, 1);color:rgba(255, 255, 255, var(--tw-text-opacity))`
- `.tw-text-gt-l-text-white-2` → `color:rgba(255, 255, 255, 0.90)`
- `.tw-text-gt-l-text-white-3` → `color:rgba(255, 255, 255, 0.65)`
- `.tw-text-ys-d-grey-blue-2` → `--tw-text-opacity:1;color:rgba(59, 66, 82, 1);color:rgba(59, 66, 82, var(--tw-text-opacity))`
- `.tw-text-ys-d-text-yellow-4` → `--tw-text-opacity:1;color:rgba(129, 100, 71, 1);color:rgba(129, 100, 71, var(--tw-text-opacity))`
- `.tw-text-ys-g-func-orange` → `--tw-text-opacity:1;color:rgba(244, 143, 1, 1);color:rgba(244, 143, 1, var(--tw-text-opacity))`
- `.tw-text-ys-l-brand-yellow-3` → `--tw-text-opacity:1;color:rgba(211, 188, 142, 1);color:rgba(211, 188, 142, var(--tw-text-opacity))`
- `.tw-text-ys-l-grey-white-2` → `--tw-text-opacity:1;color:rgba(240, 234, 224, 1);color:rgba(240, 234, 224, var(--tw-text-opacity))`
- `.tw-text-ys-l-grey-white-5` → `--tw-text-opacity:1;color:rgba(203, 189, 167, 1);color:rgba(203, 189, 167, var(--tw-text-opacity))`
- `.tw-opacity-50` → `opacity:0.5`
- `.tw-backdrop-blur-sm` → `--tw-backdrop-blur:blur(calc(4 * var(--gpx)));-webkit-backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia);backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia)`
- `.tw-flex-center` → `display:flex;align-items:center;justify-content:center`
- `.tw-flex-items-center` → `display:flex;align-items:center`
- `.tw-scrollbar-hide` → `scrollbar-width:none;`
- `.tw-scrollbar-hide::-webkit-scrollbar` → `display:none`
- `.tw-absolute-center` → `position:absolute;left:50%;top:50%;-webkit-transform:translate(-50%,-50%);transform:translate(-50%,-50%)`
- `.van-popup-lang.active` → `color:#d3bc8d`
- `.tw-group:hover .group-hover\:tw-text-ys-l-brand-golden-3` → `--tw-text-opacity:1;color:rgba(211, 188, 142, 1);color:rgba(211, 188, 142, var(--tw-text-opacity))`
- `.tw-group:hover .group-hover\:tw-text-ys-l-brand-yellow-2` → `--tw-text-opacity:1;color:rgba(220, 201, 165, 1);color:rgba(220, 201, 165, var(--tw-text-opacity))`
- `.tw-group:hover .group-hover\:tw-text-ys-l-text-yellow-2` → `--tw-text-opacity:1;color:rgba(221, 202, 159, 1);color:rgba(221, 202, 159, var(--tw-text-opacity))`
- `.tw-group:hover .group-hover\:tw-opacity-100` → `opacity:1`

## Complete CSS rule inventory

All 1145 parsed CSS rule blocks in source order. Declarations are preserved in compact form. At-rules without ordinary selector blocks and CSS comments are not represented by this simple rule parser; consult the original stylesheet for those constructs.

1. `/*! Copyright © 2011 - 2026 miHoYo. All Rights Reserved */ .lang-btn[data-v-137fe46b]` → `display:flex;height:100%;align-items:center`
2. `.lang-btn[data-v-137fe46b] .van-popover__wrapper` → `display:flex;align-items:center`
3. `.lang-btn .lang[data-v-137fe46b]` → `width:calc(24 * var(--gpx));height:calc(24 * var(--gpx))`
4. `.nav-bar-border[data-v-4b6b351e]` → `border-bottom:calc(1 * var(--gpx)) solid rgba(255,255,255,.08)`
5. `body[data-sharing] .nav-bar[data-v-4b6b351e]` → `display:none`
6. `.container[data-v-4b6b351e]` → `position:relative;display:flex;align-items:center;justify-content:space-between;padding:calc(10 * var(--gpx)) calc(16 * var(--gpx))`
7. `.ic[data-v-4b6b351e]` → `-webkit-user-select:none;-moz-user-select:none;user-select:none;width:calc(24 * var(--gpx));height:calc(24 * var(--gpx));flex-shrink:0`
8. `.left[data-v-4b6b351e],.right[data-v-4b6b351e]` → `display:flex;align-items:center`
9. `.center[data-v-4b6b351e]` → `position:absolute;top:50%;left:50%;-webkit-transform:translate(-50%, -50%);transform:translate(-50%, -50%)`
10. `.title[data-v-4b6b351e]` → `text-align:center;font-size:calc(16 * var(--gpx));font-weight:600;line-height:calc(24 * var(--gpx))`
11. `body[data-game-id="2"] .title[data-v-4b6b351e]` → `color:#d3bc8e;letter-spacing:calc(.1 * var(--gpx))`
12. `.right>*[data-v-4b6b351e]` → `margin-left:calc(12 * var(--gpx))`
13. `.geetest-page[data-v-01889dd6]` → `background:#2a2a2a`
14. `.geetest-page.outApp[data-v-01889dd6]` → `padding-top:calc(48 * var(--gpx))`
15. `.geetest-page[data-v-01889dd6] .nav-bar` → `border-bottom:none`
16. `.geetest-page .content[data-v-01889dd6]` → `width:calc(345 * var(--gpx));height:calc(700 * var(--gpx));margin:0 auto;background:url(images/risk-m-bg.1f090d1d.png) no-repeat left top;background-size:100% 100%;padding-top:calc(140 * var(--gpx));box-sizing:border-box`
17. `.geetest-page .content .icon[data-v-01889dd6]` → `display:block;width:calc(140 * var(--gpx));height:calc(140 * var(--gpx));margin:0 auto`
18. `.geetest-page .content .message[data-v-01889dd6]` → `width:calc(280 * var(--gpx));margin:0 auto;color:rgba(255,255,255,.65);text-align:center;font-family:"PingFang SC";font-size:calc(14 * var(--gpx));font-style:normal;font-weight:400;line-height:calc(20 * var(--gpx))`
19. `.geetest-page .content .tip[data-v-01889dd6]` → `width:calc(280 * var(--gpx));margin:calc(206 * var(--gpx)) auto 0;color:rgba(255,255,255,.45);text-align:center;font-family:"PingFang SC";font-size:calc(12 * var(--gpx));font-style:normal;font-weight:500;line-height:calc(16 * var(--gpx))`
20. `.m-root-layout` → `min-height:100vh;background-color:#2a2a2a`
21. `.mhy-skill-dialog` → `width:calc(285 * var(--gpx));border-radius:calc(7 * var(--gpx));overflow:visible;background-color:#333`
22. `.mhy-skill-dialog .van-dialog__content` → `position:relative`
23. `.mhy-skill-dialog .van-dialog__content .border` → `position:absolute;top:calc(3 * var(--gpx));bottom:calc(3 * var(--gpx));left:calc(3 * var(--gpx));right:calc(3 * var(--gpx));border:calc(1 * var(--gpx)) solid #dac9ad;border-image-source:url(images/ys_dialog_skill_bg@2x.50a0e5a0.png);border-image-slice:50;border-image-width:calc(30 * var(--gpx))`
24. `.mhy-skill-dialog .van-dialog__content .right_bg` → `position:absolute;right:0;bottom:0;background:no-repeat;background-size:100% 100%;width:calc(108 * var(--gpx));height:calc(108 * var(--gpx));background-image:url(images/ys_dialog_skill_right_bg@2x.89af0be2.png)`
25. `.mhy-skill-dialog .van-dialog__content .close` → `position:absolute;background:no-repeat;background-size:100% 100%;background-image:url(images/ys_dialog_close@2x.09d84262.png);width:calc(24 * var(--gpx));height:calc(24 * var(--gpx));right:calc(-7 * var(--gpx));top:calc(-7 * var(--gpx));z-index:23`
26. `.mhy-skill-dialog .container` → `min-height:calc(152 * var(--gpx));padding:calc(15 * var(--gpx));position:relative`
27. `.mhy-skill-dialog .container .info` → `margin-bottom:calc(10 * var(--gpx));display:flex;align-items:center`
28. `.mhy-skill-dialog .container .info .skill-bg` → `margin-right:calc(10 * var(--gpx));background-image:url(images/sumo_skill_bg@2x.f5a1540c.png);background-size:100% auto;width:calc(48 * var(--gpx));height:calc(48 * var(--gpx));position:relative;display:inline-block`
29. `.mhy-skill-dialog .container .info .skill-bg .icon` → `display:inline-block;position:absolute;left:50%;top:50%;-webkit-transform:translate(-50%, -50%);transform:translate(-50%, -50%);width:calc(34 * var(--gpx));height:calc(34 * var(--gpx))`
30. `.mhy-skill-dialog .container .info .name` → `font-size:4.26667vw;font-family:Microsoft YaHei,sans-serif;font-weight:500;color:#333;line-height:1.5`
31. `.mhy-skill-dialog .container .desc` → `font-size:3.2vw;font-family:Microsoft YaHei,sans-serif;color:#7f858a;line-height:calc(16 * var(--gpx))`
32. `.mhy-rune-dialog[data-v-043f08a2]` → `width:calc(285 * var(--gpx));border-radius:calc(7 * var(--gpx));background-color:#f6f3ed;overflow:visible;background-size:100% auto;background-image:url(images/ys_dialog_skill_bg@2x.50a0e5a0.png)`
33. `.mhy-rune-dialog .van-dialog__content[data-v-043f08a2]` → `position:relative`
34. `.mhy-rune-dialog .van-dialog__content .border[data-v-043f08a2]` → `position:absolute;top:calc(3 * var(--gpx));bottom:calc(3 * var(--gpx));left:calc(3 * var(--gpx));right:calc(3 * var(--gpx));background:no-repeat;background-size:100% 100%;background-image:url(images/ys_dialog_skill_border@2x.3781e94e.png)`
35. `.mhy-rune-dialog .van-dialog__content .right_bg[data-v-043f08a2]` → `position:absolute;right:0;bottom:0;background:no-repeat;background-size:100% 100%;width:calc(108 * var(--gpx));height:calc(108 * var(--gpx));background-image:url(images/ys_dialog_skill_right_bg@2x.89af0be2.png)`
36. `.mhy-rune-dialog .van-dialog__content .close[data-v-043f08a2]` → `position:absolute;background:no-repeat;background-size:100% 100%;background-image:url(images/ys_dialog_close@2x.09d84262.png);width:calc(24 * var(--gpx));height:calc(24 * var(--gpx));right:calc(-7 * var(--gpx));top:calc(-7 * var(--gpx));z-index:23`
37. `.mhy-rune-dialog .container[data-v-043f08a2]` → `min-height:calc(152 * var(--gpx));padding:calc(15 * var(--gpx));position:relative;display:flex`
38. `.mhy-rune-dialog .container .logo[data-v-043f08a2]` → `display:inline-block;width:calc(48 * var(--gpx));height:calc(92 * var(--gpx));margin-right:calc(10 * var(--gpx));margin-top:calc(4 * var(--gpx))`
39. `.mhy-rune-dialog .container .info .inner[data-v-043f08a2]` → `margin-bottom:calc(5 * var(--gpx))`
40. `.mhy-rune-dialog .container .info .inner .name[data-v-043f08a2]` → `font-size:4.26667vw;font-family:Microsoft YaHei,sans-serif;font-weight:500;color:#333;line-height:1.5;margin-right:calc(5 * var(--gpx))`
41. `.mhy-rune-dialog .container .info .inner .element[data-v-043f08a2]` → `width:calc(22 * var(--gpx));height:calc(24 * var(--gpx));vertical-align:bottom`
42. `.mhy-rune-dialog .container .info .desc[data-v-043f08a2]` → `font-size:3.2vw;font-family:Microsoft YaHei,sans-serif;color:#7f858a;line-height:calc(16 * var(--gpx));max-height:calc(87 * var(--gpx));overflow-y:auto;position:relative;z-index:10;padding-right:calc(5 * var(--gpx))`
43. `.mhy-rune-dialog .container .info .desc[data-v-043f08a2]::-webkit-scrollbar` → `width:calc(4 * var(--gpx));height:calc(4 * var(--gpx))`
44. `.mhy-rune-dialog .container .info .desc[data-v-043f08a2]::-webkit-scrollbar-track` → `width:calc(10 * var(--gpx))`
45. `.mhy-rune-dialog .container .info .desc[data-v-043f08a2]::-webkit-scrollbar-thumb` → `background:#d2c4aa;border-radius:calc(2 * var(--gpx))`
46. `.buff-dialog[data-v-15f88bea]` → `width:calc(285 * var(--gpx));border-radius:calc(7 * var(--gpx));background-color:#f6f3ed;overflow:visible;background-size:100% auto;background-image:url(images/ys_dialog_skill_bg@2x.50a0e5a0.png)`
47. `.buff-dialog .container[data-v-15f88bea]` → `min-height:calc(152 * var(--gpx));padding:calc(15 * var(--gpx));position:relative`
48. `.buff-dialog .container .info[data-v-15f88bea]` → `display:flex;align-items:center`
49. `.buff-dialog .container .info .level[data-v-15f88bea]` → `width:calc(4 * var(--gpx));height:calc(16 * var(--gpx));margin-right:calc(5 * var(--gpx))`
50. `.buff-dialog .container .info .name[data-v-15f88bea]` → `height:calc(24 * var(--gpx));font-size:calc(16 * var(--gpx));font-weight:500;color:#1e1f20;line-height:calc(24 * var(--gpx));margin-right:calc(5 * var(--gpx))`
51. `.buff-dialog .container .energy[data-v-15f88bea]` → `height:calc(12 * var(--gpx));font-size:calc(12 * var(--gpx));font-weight:400;color:#1e1f20;line-height:calc(12 * var(--gpx))`
52. `.buff-dialog .container .desc-container[data-v-15f88bea]` → `min-height:calc(120 * var(--gpx));max-height:calc(120 * var(--gpx));margin-top:calc(9 * var(--gpx))`
53. `.buff-dialog .container .desc[data-v-15f88bea]` → `font-size:calc(12 * var(--gpx));color:#7f858a;line-height:calc(16 * var(--gpx))`
54. `.buff-dialog .container .os-scrollbar-vertical>.os-scrollbar-track>.os-scrollbar-handle[data-v-15f88bea]` → `background-color:#c8baad;border-radius:calc(3 * var(--gpx))`
55. `.buff-dialog .van-dialog__content[data-v-15f88bea]` → `position:relative`
56. `.buff-dialog .van-dialog__content .border[data-v-15f88bea]` → `position:absolute;top:calc(3 * var(--gpx));bottom:calc(3 * var(--gpx));left:calc(3 * var(--gpx));right:calc(3 * var(--gpx));background:no-repeat;background-size:100% 100%;background-image:url(images/ys_dialog_buff_border@2x.d40d871f.png)`
57. `.buff-dialog .van-dialog__content .right_bg[data-v-15f88bea]` → `position:absolute;right:0;bottom:0;background:no-repeat;background-size:100% 100%;width:calc(108 * var(--gpx));height:calc(108 * var(--gpx));background-image:url(images/ys_dialog_skill_right_bg@2x.89af0be2.png)`
58. `.buff-dialog .van-dialog__content .close[data-v-15f88bea]` → `position:absolute;background:no-repeat;background-size:100% 100%;background-image:url(images/ys_dialog_close@2x.09d84262.png);width:calc(24 * var(--gpx));height:calc(24 * var(--gpx));right:calc(-7 * var(--gpx));top:calc(-7 * var(--gpx));z-index:23`
59. `.empty-block[data-v-649410d6]` → `width:100%;height:calc(195 * var(--gpx));border-radius:calc(7 * var(--gpx));display:flex;flex-direction:column;justify-content:center;align-items:center`
60. `.empty-block .icon[data-v-649410d6]` → `width:calc(120 * var(--gpx));height:calc(120 * var(--gpx));-o-object-fit:contain;object-fit:contain;-o-object-position:top center;object-position:top center`
61. `.empty-block .text[data-v-649410d6]` → `font-size:calc(16 * var(--gpx));font-family:Microsoft YaHei,sans-serif;font-weight:400;color:#c0c5c8;line-height:calc(20 * var(--gpx));text-align:center`
62. `.empty-block[data-v-649410d6]` → `height:auto`
63. `.empty-block .text[data-v-649410d6]` → `padding:0 calc(38 * var(--gpx));font-size:calc(12 * var(--gpx))`
64. `.error-block-layout[data-v-68174fb9]` → `width:100%;height:100%;display:flex;flex-direction:column;align-items:center;position:relative;z-index:20`
65. `.error-block-layout .icon[data-v-68174fb9]` → `margin-top:calc(77 * var(--gpx));height:calc(115 * var(--gpx))`
66. `.error-block-layout .text[data-v-68174fb9]` → `margin-top:calc(24 * var(--gpx));font-size:calc(14 * var(--gpx));font-family:Microsoft YaHei,sans-serif;font-weight:400;color:#c0c5c8;line-height:calc(20 * var(--gpx));word-break:break-word`
67. `.error-block-layout[data-v-68174fb9]` → `margin-top:calc(-9 * var(--gpx));border-radius:calc(7 * var(--gpx)) calc(7 * var(--gpx)) 0 0`
68. `.error-block-layout .icon[data-v-68174fb9]` → `margin-top:calc(84.5 * var(--gpx));height:calc(96.5 * var(--gpx))`
69. `.error-block-layout .text[data-v-68174fb9]` → `padding:0 calc(38 * var(--gpx));font-size:calc(15 * var(--gpx));word-break:break-word`
70. `.mhy-img-icon` → `height:1em;vertical-align:-0.15em;fill:currentColor;overflow:hidden`
71. `.mhy-announcement` → `width:calc(285 * var(--gpx));border-radius:calc(7 * var(--gpx));background-color:#f6f3ed;overflow:visible`
72. `.mhy-announcement .van-dialog__content` → `position:relative`
73. `.mhy-announcement .van-dialog__content .border` → `position:absolute;top:calc(3 * var(--gpx));bottom:calc(3 * var(--gpx));left:calc(3 * var(--gpx));right:calc(3 * var(--gpx));background:no-repeat;background-size:100% 100%`
74. `.mhy-announcement .van-dialog__content .border .top` → `position:absolute;width:100%;top:0;background:no-repeat;background-size:100% 100%;background-image:url(images/ys_dialog_border_top@2x.e90fde6c.png);height:calc(20 * var(--gpx))`
75. `.mhy-announcement .van-dialog__content .border .bottom` → `position:absolute;width:100%;bottom:0;background:no-repeat;background-size:100% 100%;background-image:url(images/ys_dialog_border_top@2x.e90fde6c.png);-webkit-transform:rotate(180deg);transform:rotate(180deg);height:calc(20 * var(--gpx))`
76. `.mhy-announcement .van-dialog__content .border .left` → `width:calc(2 * var(--gpx));position:absolute;left:0;top:calc(20 * var(--gpx));bottom:calc(19 * var(--gpx));background-image:url(images/ys_dialog_border@2x.cad41d3a.png);background-size:100% 100%`
77. `.mhy-announcement .van-dialog__content .border .right` → `width:calc(2 * var(--gpx));position:absolute;right:0;top:calc(20 * var(--gpx));bottom:calc(19 * var(--gpx));background-image:url(images/ys_dialog_border@2x.cad41d3a.png);background-size:100% 100%;-webkit-transform:rotate(180deg);transform:rotate(180deg)`
78. `.mhy-announcement .close` → `width:calc(30 * var(--gpx));height:calc(30 * var(--gpx));position:absolute;left:50%;-webkit-transform:translateX(-50%);transform:translateX(-50%);bottom:calc(-60 * var(--gpx));z-index:2003`
79. `.mhy-announcement .close img` → `width:100%;height:100%`
80. `.mhy-announcement-main` → `position:relative;z-index:1;padding:calc(17 * var(--gpx)) calc(8 * var(--gpx)) calc(17 * var(--gpx)) calc(17 * var(--gpx))`
81. `.mhy-announcement-main__title` → `text-align:center;font-size:calc(16 * var(--gpx));line-height:calc(22 * var(--gpx));font-weight:500;font-family:Microsoft YaHei,sans-serif;margin-bottom:calc(6 * var(--gpx));color:#9c7f69`
82. `.mhy-announcement-main__content` → `min-height:calc(140 * var(--gpx));max-height:calc(260 * var(--gpx));font-size:calc(14 * var(--gpx));color:#7f858a;line-height:calc(20 * var(--gpx));padding-right:calc(9 * var(--gpx))`
83. `.mhy-announcement-main__content .os-scrollbar.os-scrollbar-vertical .os-scrollbar-handle` → `background-color:#c8baad;border-radius:calc(3 * var(--gpx))`
84. `.mhy-dialog[data-v-c80048a2]` → `width:calc(285 * var(--gpx));border-radius:calc(7 * var(--gpx));overflow:visible;background-color:#0d0e12`
85. `.mhy-dialog .van-dialog__content[data-v-c80048a2]` → `position:relative`
86. `.mhy-dialog .van-dialog__content .main[data-v-c80048a2]` → `display:flex;flex-direction:column;min-height:calc(142 * var(--gpx))`
87. `.mhy-dialog .van-dialog__content .main .close[data-v-c80048a2]` → `position:absolute;background:no-repeat;background-size:100% 100%;background-image:url(images/ys_dialog_close@2x.09d84262.png);width:calc(24 * var(--gpx));height:calc(24 * var(--gpx));right:calc(-7 * var(--gpx));top:calc(-7 * var(--gpx));z-index:23`
88. `.mhy-dialog .van-dialog__content .container[data-v-c80048a2]` → `position:relative;flex:1`
89. `.mhy-dialog .van-dialog__content .btns[data-v-c80048a2]` → `position:relative;display:flex;justify-content:space-between`
90. `.mhy-dialog .van-dialog__content .btns .item[data-v-c80048a2]` → `width:calc(118 * var(--gpx));height:calc(34 * var(--gpx));background-color:#ece5d8;border-radius:calc(17 * var(--gpx));text-align:center;font-size:3.2vw;font-family:Microsoft YaHei,sans-serif;font-weight:500;color:#675856;line-height:calc(34 * var(--gpx));position:relative`
91. `.mhy-dialog .van-dialog__content .btns .item[data-v-c80048a2]::before` → `position:absolute;top:calc(2 * var(--gpx));left:calc(2 * var(--gpx));bottom:calc(2 * var(--gpx));right:calc(2 * var(--gpx));content:" ";box-sizing:border-box;border-radius:calc(15 * var(--gpx));border:calc(1 * var(--gpx)) solid #e2d4b7`
92. `.mhy-dialog .van-dialog__content .btns .item.primary[data-v-c80048a2]` → `background-color:#d2c4aa;color:#fff;border:calc(1 * var(--gpx)) solid #e7dbc5`
93. `.mhy-dialog .van-dialog__content .btns .item.disabled[data-v-c80048a2]` → `color:rgba(103,88,86,.4)`
94. `.avatar-container[data-v-67354b94]` → `position:relative`
95. `.avatar-container[data-v-67354b94] .gt-avatar` → `border-radius:calc(4 * var(--gpx));overflow:hidden`
96. `.avatar-container__disabled[data-v-67354b94]` → `opacity:.3`
97. `.avatar-container .sub-name[data-v-67354b94]` → `display:inline-block;width:100%;word-wrap:break-word;margin-top:calc(8 * var(--gpx))`
98. `.avatar-container .constellation[data-v-67354b94]` → `position:absolute;top:0;right:0;display:inline-block;height:calc(18 * var(--gpx));padding:0 calc(4 * var(--gpx));color:#fff;text-align:center;font-size:calc(12 * var(--gpx));font-weight:600;line-height:calc(18 * var(--gpx));border-radius:calc(0 * var(--gpx)) calc(4 * var(--gpx)) calc(0 * var(--gpx)) calc(4 * var(--gpx));background:rgba(0,0,0,.6)`
99. `.avatar-container .order[data-v-67354b94]` → `position:absolute;top:calc(3 * var(--gpx));right:calc(3 * var(--gpx));box-sizing:border-box;width:calc(18 * var(--gpx));height:calc(18 * var(--gpx));border-radius:50%;display:flex;align-items:center;justify-content:center`
100. `.avatar-container .order__selected[data-v-67354b94]` → `background-color:#3b4252;font-size:calc(12 * var(--gpx));font-weight:600;line-height:calc(16 * var(--gpx));color:#fff;text-align:center`
101. `.avatar-container .order__empty[data-v-67354b94]` → `border:calc(1 * var(--gpx)) solid rgba(255,255,255,.48);background-color:rgba(0,0,0,.48)`
102. `.pop-role-card` → `border-radius:calc(16 * var(--gpx));background:#0d0e12`
103. `html[gt-platform=mob] .pop-role-card.m-role-dialog` → `padding:0;background:rgba(0,0,0,0)`
104. `html[gt-platform=mob] .pop-role-card.m-role-dialog .role-info-name` → `color:#3b4252;color:var(--dark-grey-blue_2, #3b4252);font-size:calc(16 * var(--gpx));font-style:normal;font-weight:600;line-height:calc(20 * var(--gpx))`
105. `html[gt-platform=mob] .pop-role-card.m-role-dialog .role-info-desc` → `color:rgba(0, 0, 0, 0.45);color:var(--dark-text-black_4, rgba(0, 0, 0, 0.45))`
106. `html[gt-platform=mob] .pop-role-card.m-role-dialog .role-info-split` → `background:url(images/role-medium-split-light@3x.c12c68a8.png) no-repeat center/contain`
107. `html[gt-platform=mob] .pop-role-card.m-role-dialog .action-btn` → `padding:calc(8 * var(--gpx)) calc(10 * var(--gpx)) calc(8 * var(--gpx)) calc(12 * var(--gpx));border-radius:calc(4 * var(--gpx));border:calc(1 * var(--gpx)) solid rgba(0, 0, 0, 0.08);border:calc(1 * var(--gpx)) solid var(--general-grey-black_6, rgba(0, 0, 0, 0.08));background:rgba(0, 0, 0, 0.04);background:var(--general-grey-black_7, rgba(0, 0, 0, 0.04));color:rgba(0, 0, 0, 0.65);color:var(--dark-text-black_3, rgba(0, 0, 0, 0.65));font-size:calc(12 * var(--gpx));line-height:calc(16 * var(--gpx))`
108. `.pop-role-card.m-role-dialog svg path` → `fill:#000`
109. `.pop-role-card .role-info` → `display:flex;align-items:center`
110. `.pop-role-card .role-info-avatar` → `position:relative;margin-right:calc(12 * var(--gpx))`
111. `.pop-role-card .role-info-avatar .role-medium-avatar` → `width:calc(44 * var(--gpx));height:calc(44 * var(--gpx))`
112. `.pop-role-card .role-info-avatar .role-medium-avatar .gt-avatar,.pop-role-card .role-info-avatar .role-medium-avatar .gt-avatar-img` → `width:calc(44 * var(--gpx));height:calc(44 * var(--gpx))`
113. `.pop-role-card .role-info-name` → `margin-bottom:calc(4 * var(--gpx));color:rgba(255, 255, 255, 0.9);color:var(--light-text-white_2, rgba(255, 255, 255, 0.9));font-size:calc(16 * var(--gpx));font-weight:700;line-height:calc(24 * var(--gpx))`
114. `.pop-role-card .role-info-desc` → `color:rgba(255, 255, 255, 0.45);color:var(--light-text-white_4, rgba(255, 255, 255, 0.45));font-size:calc(12 * var(--gpx));line-height:calc(16 * var(--gpx))`
115. `.pop-role-card .role-info-split` → `height:calc(9 * var(--gpx));margin:calc(12 * var(--gpx)) 0;background:url(images/role-medium-split@3x.80702c06.png) no-repeat center/contain`
116. `.pop-role-card .role-actions button` → `display:flex;justify-content:space-between;align-items:center;padding:calc(7 * var(--gpx)) calc(10 * var(--gpx)) calc(7 * var(--gpx)) calc(12 * var(--gpx));width:100%;border:calc(1 * var(--gpx)) solid rgba(255, 255, 255, 0.16);border:calc(1 * var(--gpx)) solid var(--general-grey-white_4, rgba(255, 255, 255, 0.16));outline:none;border-radius:calc(4 * var(--gpx));cursor:pointer;color:rgba(255, 255, 255, 0.65);color:var(--light-text-white_3, rgba(255, 255, 255, 0.65));font-size:calc(12 * var(--gpx));line-height:calc(16 * var(--gpx));margin-bottom:calc(8 * var(--gpx));background:rgba(0,0,0,0)`
117. `.pop-role-card .role-actions button:last-child` → `margin-bottom:0`
118. `html[gt-platform=pc] .pop-role-card .role-actions button.action-btn__link:hover` → `border:calc(1 * var(--gpx)) solid #d3bc8e;border:calc(1 * var(--gpx)) solid var(--light-brand-golden_3, #d3bc8e);color:#d3bc8e;color:var(--light-brand-golden_3, #d3bc8e)`
119. `html[gt-platform=pc] .pop-role-card .role-actions button.action-btn__link:hover svg path` → `fill:#d3bc8e`
120. `.role-dialog-card[data-v-1c06bcfc] .van-dialog__content .main` → `padding:calc(15 * var(--gpx));min-height:0;min-height:initial`
121. `.role-dialog-card[data-v-1c06bcfc] .role-hover-card-inner,.role-dialog-card[data-v-1c06bcfc] .role-info` → `padding:0`
122. `.role-dialog-card[data-v-1c06bcfc] .role-actions` → `padding:0 calc(5 * var(--gpx))`
123. `html[mi18n-lang=zh-cn],html[mi18n-lang=zh-tw]` → `font-family:"Helvetica neue","PingFang SC","Hiragino Sans GB","Microsoft YaHei UI","Microsoft YaHei","Arial","sans-serif" !important`
124. `html[mi18n-lang=ko-kr]` → `font-family:"nanum gothic","나눔 고딕","Malgun Gothic","맑은 고딕","돋움",sans-serif !important`
125. `html[mi18n-lang=en-us],html[mi18n-lang=id-id],html[mi18n-lang=fr-fr],html[mi18n-lang=vi-vn],html[mi18n-lang=pt-pt],html[mi18n-lang=es-es],html[mi18n-lang=tr-tr],html[mi18n-lang=it-it]` → `font-family:Tahoma,Helvetica,Arial,sans-serif !important`
126. `html[mi18n-lang=th-th]` → `font-family:kanit,Tahoma,Helvetica,Arial,Geneva,sans-serif !important`
127. `html[mi18n-lang=ru-ru]` → `font-family:"Adelle Cyrillic","Dctz38",Arial,Helvetica,sans-serif !important`
128. `html[mi18n-lang=ja-jp]` → `font-family:"游ゴシック体",YuGothic,"ヒラギノ角ゴシック Pro","Hiragino Kaku Gothic Pro","メイリオ",Meiryo,Osaka,"ＭＳ Ｐゴシック","MS Pgothic",sans-serif !important`
129. `html[mi18n-lang=de-de]` → `font-family:"Calibri","Arial",sans-serif !important`
130. `*, ::before, ::after` → `--tw-border-spacing-x:0;--tw-border-spacing-y:0;--tw-translate-x:0;--tw-translate-y:0;--tw-rotate:0;--tw-skew-x:0;--tw-skew-y:0;--tw-scale-x:1;--tw-scale-y:1;--tw-pan-x: ;--tw-pan-y: ;--tw-pinch-zoom: ;--tw-scroll-snap-strictness:proximity;--tw-gradient-from-position: ;--tw-gradient-via-position: ;--tw-gradient-to-position: ;--tw-ordinal: ;--tw-slashed-zero: ;--tw-numeric-figure: ;--tw-numeric-spacing: ;--tw-numeric-fraction: ;--tw-ring-inset: ;--tw-ring-offset-width:calc(0 * var(--gpx));--tw-ring-offset-color:#fff;--tw-ring-color:rgba(59, 130, 246, 0.5);--tw-ring-offset-shadow:0 0 rgba(0,0,0,0);--tw-ring-shadow:0 0 rgba(0,0,0,0);--tw-shadow:0 0 rgba(0,0,0,0);--tw-shadow-colored:0 0 rgba(0,0,0,0);--tw-blur: ;--tw-brightness: ;--tw-contrast: ;--tw-grayscale: ;--tw-hue-rotate: ;--tw-invert: ;--tw-saturate: ;--tw-sepia: ;--tw-drop-shadow: ;--tw-backdrop-blur: ;--tw-backdrop-brightness: ;--tw-backdrop-contrast: ;--tw-backdrop-grayscale: ;--tw-backdrop-hue-rotate: ;--tw-backdrop-invert: ;--tw-backdrop-opacity: ;--tw-backdrop-saturate: ;--tw-backdrop-sepia: ;--tw-contain-size: ;--tw-contain-layout: ;--tw-contain-paint: ;--tw-contain-style:`
131. `::-webkit-backdrop` → `--tw-border-spacing-x:0;--tw-border-spacing-y:0;--tw-translate-x:0;--tw-translate-y:0;--tw-rotate:0;--tw-skew-x:0;--tw-skew-y:0;--tw-scale-x:1;--tw-scale-y:1;--tw-pan-x: ;--tw-pan-y: ;--tw-pinch-zoom: ;--tw-scroll-snap-strictness:proximity;--tw-gradient-from-position: ;--tw-gradient-via-position: ;--tw-gradient-to-position: ;--tw-ordinal: ;--tw-slashed-zero: ;--tw-numeric-figure: ;--tw-numeric-spacing: ;--tw-numeric-fraction: ;--tw-ring-inset: ;--tw-ring-offset-width:calc(0 * var(--gpx));--tw-ring-offset-color:#fff;--tw-ring-color:rgba(59, 130, 246, 0.5);--tw-ring-offset-shadow:0 0 rgba(0,0,0,0);--tw-ring-shadow:0 0 rgba(0,0,0,0);--tw-shadow:0 0 rgba(0,0,0,0);--tw-shadow-colored:0 0 rgba(0,0,0,0);--tw-blur: ;--tw-brightness: ;--tw-contrast: ;--tw-grayscale: ;--tw-hue-rotate: ;--tw-invert: ;--tw-saturate: ;--tw-sepia: ;--tw-drop-shadow: ;--tw-backdrop-blur: ;--tw-backdrop-brightness: ;--tw-backdrop-contrast: ;--tw-backdrop-grayscale: ;--tw-backdrop-hue-rotate: ;--tw-backdrop-invert: ;--tw-backdrop-opacity: ;--tw-backdrop-saturate: ;--tw-backdrop-sepia: ;--tw-contain-size: ;--tw-contain-layout: ;--tw-contain-paint: ;--tw-contain-style:`
132. `::backdrop` → `--tw-border-spacing-x:0;--tw-border-spacing-y:0;--tw-translate-x:0;--tw-translate-y:0;--tw-rotate:0;--tw-skew-x:0;--tw-skew-y:0;--tw-scale-x:1;--tw-scale-y:1;--tw-pan-x: ;--tw-pan-y: ;--tw-pinch-zoom: ;--tw-scroll-snap-strictness:proximity;--tw-gradient-from-position: ;--tw-gradient-via-position: ;--tw-gradient-to-position: ;--tw-ordinal: ;--tw-slashed-zero: ;--tw-numeric-figure: ;--tw-numeric-spacing: ;--tw-numeric-fraction: ;--tw-ring-inset: ;--tw-ring-offset-width:calc(0 * var(--gpx));--tw-ring-offset-color:#fff;--tw-ring-color:rgba(59, 130, 246, 0.5);--tw-ring-offset-shadow:0 0 rgba(0,0,0,0);--tw-ring-shadow:0 0 rgba(0,0,0,0);--tw-shadow:0 0 rgba(0,0,0,0);--tw-shadow-colored:0 0 rgba(0,0,0,0);--tw-blur: ;--tw-brightness: ;--tw-contrast: ;--tw-grayscale: ;--tw-hue-rotate: ;--tw-invert: ;--tw-saturate: ;--tw-sepia: ;--tw-drop-shadow: ;--tw-backdrop-blur: ;--tw-backdrop-brightness: ;--tw-backdrop-contrast: ;--tw-backdrop-grayscale: ;--tw-backdrop-hue-rotate: ;--tw-backdrop-invert: ;--tw-backdrop-opacity: ;--tw-backdrop-saturate: ;--tw-backdrop-sepia: ;--tw-contain-size: ;--tw-contain-layout: ;--tw-contain-paint: ;--tw-contain-style:`
133. `.tw-scrollbar-dark::-webkit-scrollbar` → `width:calc(4 * var(--gpx));height:calc(4 * var(--gpx))`
134. `.tw-scrollbar-dark::-webkit-scrollbar-thumb` → `border-radius:calc(2 * var(--gpx));background-color:rgba(255, 255, 255, 0.12)`
135. `.tw-scrollbar-dark::-webkit-scrollbar-thumb:hover` → `width:calc(6 * var(--gpx));height:calc(6 * var(--gpx));border-radius:calc(4 * var(--gpx));background-color:rgba(255, 255, 255, 0.32)`
136. `.tw-pointer-events-none` → `pointer-events:none`
137. `.tw-pointer-events-auto` → `pointer-events:auto`
138. `.\!tw-fixed` → `position:fixed !important`
139. `.tw-fixed` → `position:fixed`
140. `.\!tw-absolute` → `position:absolute !important`
141. `.tw-absolute` → `position:absolute`
142. `.tw-relative` → `position:relative`
143. `.-tw-right-0\.5` → `right:calc((2 * var(--gpx)) * -1);right:calc(calc(2 * var(--gpx)) * -1)`
144. `.-tw-right-3` → `right:calc((12 * var(--gpx)) * -1);right:calc(calc(12 * var(--gpx)) * -1)`
145. `.-tw-right-4` → `right:calc((16 * var(--gpx)) * -1);right:calc(calc(16 * var(--gpx)) * -1)`
146. `.-tw-top-0\.5` → `top:calc((2 * var(--gpx)) * -1);top:calc(calc(2 * var(--gpx)) * -1)`
147. `.-tw-top-1` → `top:calc((4 * var(--gpx)) * -1);top:calc(calc(4 * var(--gpx)) * -1)`
148. `.-tw-top-1\.5` → `top:calc((6 * var(--gpx)) * -1);top:calc(calc(6 * var(--gpx)) * -1)`
149. `.-tw-top-2` → `top:calc((8 * var(--gpx)) * -1);top:calc(calc(8 * var(--gpx)) * -1)`
150. `.tw-bottom-0` → `bottom:calc(0 * var(--gpx))`
151. `.tw-bottom-1` → `bottom:calc(4 * var(--gpx))`
152. `.tw-bottom-9` → `bottom:calc(36 * var(--gpx))`
153. `.tw-bottom-\[-2px\]` → `bottom:calc(-2 * var(--gpx))`
154. `.tw-bottom-\[-3px\]` → `bottom:calc(-3 * var(--gpx))`
155. `.tw-bottom-\[-9px\]` → `bottom:calc(-9 * var(--gpx))`
156. `.tw-bottom-\[0\]` → `bottom:0`
157. `.tw-bottom-\[28px\]` → `bottom:calc(28 * var(--gpx))`
158. `.tw-bottom-\[3px\]` → `bottom:calc(3 * var(--gpx))`
159. `.tw-bottom-\[4px\]` → `bottom:calc(4 * var(--gpx))`
160. `.tw-bottom-\[66px\]` → `bottom:calc(66 * var(--gpx))`
161. `.tw-bottom-px` → `bottom:calc(1 * var(--gpx))`
162. `.tw-left-0` → `left:calc(0 * var(--gpx))`
163. `.tw-left-1` → `left:calc(4 * var(--gpx))`
164. `.tw-left-4` → `left:calc(16 * var(--gpx))`
165. `.tw-left-6` → `left:calc(24 * var(--gpx))`
166. `.tw-left-\[-18px\]` → `left:calc(-18 * var(--gpx))`
167. `.tw-left-\[-2px\]` → `left:calc(-2 * var(--gpx))`
168. `.tw-left-\[-6px\]` → `left:calc(-6 * var(--gpx))`
169. `.tw-left-\[178px\]` → `left:calc(178 * var(--gpx))`
170. `.tw-left-\[1px\]` → `left:calc(1 * var(--gpx))`
171. `.tw-left-\[22\.5px\]` → `left:calc(22.5 * var(--gpx))`
172. `.tw-left-\[25\.5px\]` → `left:calc(25.5 * var(--gpx))`
173. `.tw-left-\[35px\]` → `left:calc(35 * var(--gpx))`
174. `.tw-left-\[378px\]` → `left:calc(378 * var(--gpx))`
175. `.tw-left-\[3px\]` → `left:calc(3 * var(--gpx))`
176. `.tw-left-\[400px\]` → `left:calc(400 * var(--gpx))`
177. `.tw-left-\[50\%\]` → `left:50%`
178. `.tw-left-\[70\%\]` → `left:70%`
179. `.tw-left-\[75\%\]` → `left:75%`
180. `.tw-left-px` → `left:calc(1 * var(--gpx))`
181. `.tw-right-0` → `right:calc(0 * var(--gpx))`
182. `.tw-right-1` → `right:calc(4 * var(--gpx))`
183. `.tw-right-3` → `right:calc(12 * var(--gpx))`
184. `.tw-right-4` → `right:calc(16 * var(--gpx))`
185. `.tw-right-5` → `right:calc(20 * var(--gpx))`
186. `.tw-right-6` → `right:calc(24 * var(--gpx))`
187. `.tw-right-\[-2px\]` → `right:calc(-2 * var(--gpx))`
188. `.tw-right-\[-3px\]` → `right:calc(-3 * var(--gpx))`
189. `.tw-right-\[0\]` → `right:0`
190. `.tw-right-\[178px\]` → `right:calc(178 * var(--gpx))`
191. `.tw-right-\[34px\]` → `right:calc(34 * var(--gpx))`
192. `.tw-right-\[3px\]` → `right:calc(3 * var(--gpx))`
193. `.tw-right-px` → `right:calc(1 * var(--gpx))`
194. `.tw-top-0` → `top:calc(0 * var(--gpx))`
195. `.tw-top-1\/2` → `top:50%`
196. `.tw-top-2` → `top:calc(8 * var(--gpx))`
197. `.tw-top-2\.5` → `top:calc(10 * var(--gpx))`
198. `.tw-top-4` → `top:calc(16 * var(--gpx))`
199. `.tw-top-6` → `top:calc(24 * var(--gpx))`
200. `.tw-top-\[-20px\]` → `top:calc(-20 * var(--gpx))`
201. `.tw-top-\[-2px\]` → `top:calc(-2 * var(--gpx))`
202. `.tw-top-\[-3px\]` → `top:calc(-3 * var(--gpx))`
203. `.tw-top-\[-45px\]` → `top:calc(-45 * var(--gpx))`
204. `.tw-top-\[-55\%\]` → `top:-55%`
205. `.tw-top-\[-60\%\]` → `top:-60%`
206. `.tw-top-\[-6px\]` → `top:calc(-6 * var(--gpx))`
207. `.tw-top-\[-7px\]` → `top:calc(-7 * var(--gpx))`
208. `.tw-top-\[-8px\]` → `top:calc(-8 * var(--gpx))`
209. `.tw-top-\[0\]` → `top:0`
210. `.tw-top-\[10px\]` → `top:calc(10 * var(--gpx))`
211. `.tw-top-\[127\.5px\]` → `top:calc(127.5 * var(--gpx))`
212. `.tw-top-\[138px\]` → `top:calc(138 * var(--gpx))`
213. `.tw-top-\[144px\]` → `top:calc(144 * var(--gpx))`
214. `.tw-top-\[14px\]` → `top:calc(14 * var(--gpx))`
215. `.tw-top-\[170px\]` → `top:calc(170 * var(--gpx))`
216. `.tw-top-\[192px\]` → `top:calc(192 * var(--gpx))`
217. `.tw-top-\[1px\]` → `top:calc(1 * var(--gpx))`
218. `.tw-top-\[247px\]` → `top:calc(247 * var(--gpx))`
219. `.tw-top-\[256px\]` → `top:calc(256 * var(--gpx))`
220. `.tw-top-\[276px\]` → `top:calc(276 * var(--gpx))`
221. `.tw-top-\[2px\]` → `top:calc(2 * var(--gpx))`
222. `.tw-top-\[30px\]` → `top:calc(30 * var(--gpx))`
223. `.tw-top-\[32px\]` → `top:calc(32 * var(--gpx))`
224. `.tw-top-\[38px\]` → `top:calc(38 * var(--gpx))`
225. `.tw-top-\[400px\]` → `top:calc(400 * var(--gpx))`
226. `.tw-top-\[40px\]` → `top:calc(40 * var(--gpx))`
227. `.tw-top-\[44px\]` → `top:calc(44 * var(--gpx))`
228. `.tw-top-\[50px\]` → `top:calc(50 * var(--gpx))`
229. `.tw-top-\[60px\]` → `top:calc(60 * var(--gpx))`
230. `.tw-top-\[82\.5px\]` → `top:calc(82.5 * var(--gpx))`
231. `.tw-top-px` → `top:calc(1 * var(--gpx))`
232. `.tw-z-0` → `z-index:0`
233. `.tw-z-10` → `z-index:10`
234. `.tw-z-40` → `z-index:40`
235. `.tw-z-\[-1\]` → `z-index:-1`
236. `.tw-z-\[0\]` → `z-index:0`
237. `.tw-z-\[10\]` → `z-index:10`
238. `.tw-z-\[1\]` → `z-index:1`
239. `.tw-z-\[2\]` → `z-index:2`
240. `.tw-z-\[3\]` → `z-index:3`
241. `.tw-m-\[6px\]` → `margin:calc(6 * var(--gpx))`
242. `.tw-mx-0\.5` → `margin-left:calc(2 * var(--gpx));margin-right:calc(2 * var(--gpx))`
243. `.tw-mx-1` → `margin-left:calc(4 * var(--gpx));margin-right:calc(4 * var(--gpx))`
244. `.tw-mx-16` → `margin-left:calc(64 * var(--gpx));margin-right:calc(64 * var(--gpx))`
245. `.tw-mx-2` → `margin-left:calc(8 * var(--gpx));margin-right:calc(8 * var(--gpx))`
246. `.tw-mx-3` → `margin-left:calc(12 * var(--gpx));margin-right:calc(12 * var(--gpx))`
247. `.tw-mx-4` → `margin-left:calc(16 * var(--gpx));margin-right:calc(16 * var(--gpx))`
248. `.tw-mx-5` → `margin-left:calc(20 * var(--gpx));margin-right:calc(20 * var(--gpx))`
249. `.tw-mx-6` → `margin-left:calc(24 * var(--gpx));margin-right:calc(24 * var(--gpx))`
250. `.tw-mx-\[15px\]` → `margin-left:calc(15 * var(--gpx));margin-right:calc(15 * var(--gpx))`
251. `.tw-mx-auto` → `margin-left:auto;margin-right:auto`
252. `.tw-my-1` → `margin-top:calc(4 * var(--gpx));margin-bottom:calc(4 * var(--gpx))`
253. `.tw-my-2\.5` → `margin-top:calc(10 * var(--gpx));margin-bottom:calc(10 * var(--gpx))`
254. `.tw-my-3` → `margin-top:calc(12 * var(--gpx));margin-bottom:calc(12 * var(--gpx))`
255. `.tw-my-4` → `margin-top:calc(16 * var(--gpx));margin-bottom:calc(16 * var(--gpx))`
256. `.tw-my-6` → `margin-top:calc(24 * var(--gpx));margin-bottom:calc(24 * var(--gpx))`
257. `.tw-my-\[15px\]` → `margin-top:calc(15 * var(--gpx));margin-bottom:calc(15 * var(--gpx))`
258. `.tw-my-\[19px\]` → `margin-top:calc(19 * var(--gpx));margin-bottom:calc(19 * var(--gpx))`
259. `.tw-my-\[2px\]` → `margin-top:calc(2 * var(--gpx));margin-bottom:calc(2 * var(--gpx))`
260. `.\!tw-ml-6` → `margin-left:calc(24 * var(--gpx)) !important`
261. `.\!tw-mr-\[10px\]` → `margin-right:calc(10 * var(--gpx)) !important`
262. `.\!tw-mt-2` → `margin-top:calc(8 * var(--gpx)) !important`
263. `.-tw-mt-1` → `margin-top:calc((4 * var(--gpx)) * -1);margin-top:calc(calc(4 * var(--gpx)) * -1)`
264. `.tw-mb-0\.5` → `margin-bottom:calc(2 * var(--gpx))`
265. `.tw-mb-1` → `margin-bottom:calc(4 * var(--gpx))`
266. `.tw-mb-1\.5` → `margin-bottom:calc(6 * var(--gpx))`
267. `.tw-mb-16` → `margin-bottom:calc(64 * var(--gpx))`
268. `.tw-mb-2` → `margin-bottom:calc(8 * var(--gpx))`
269. `.tw-mb-2\.5` → `margin-bottom:calc(10 * var(--gpx))`
270. `.tw-mb-3` → `margin-bottom:calc(12 * var(--gpx))`
271. `.tw-mb-4` → `margin-bottom:calc(16 * var(--gpx))`
272. `.tw-mb-5` → `margin-bottom:calc(20 * var(--gpx))`
273. `.tw-mb-6` → `margin-bottom:calc(24 * var(--gpx))`
274. `.tw-mb-7` → `margin-bottom:calc(28 * var(--gpx))`
275. `.tw-mb-8` → `margin-bottom:calc(32 * var(--gpx))`
276. `.tw-mb-\[10px\]` → `margin-bottom:calc(10 * var(--gpx))`
277. `.tw-mb-\[14px\]` → `margin-bottom:calc(14 * var(--gpx))`
278. `.tw-mb-\[15px\]` → `margin-bottom:calc(15 * var(--gpx))`
279. `.tw-mb-\[1px\]` → `margin-bottom:calc(1 * var(--gpx))`
280. `.tw-mb-\[2px\]` → `margin-bottom:calc(2 * var(--gpx))`
281. `.tw-mb-\[4px\]` → `margin-bottom:calc(4 * var(--gpx))`
282. `.tw-mb-\[6px\]` → `margin-bottom:calc(6 * var(--gpx))`
283. `.tw-ml-0\.5` → `margin-left:calc(2 * var(--gpx))`
284. `.tw-ml-1` → `margin-left:calc(4 * var(--gpx))`
285. `.tw-ml-2` → `margin-left:calc(8 * var(--gpx))`
286. `.tw-ml-2\.5` → `margin-left:calc(10 * var(--gpx))`
287. `.tw-ml-3` → `margin-left:calc(12 * var(--gpx))`
288. `.tw-ml-4` → `margin-left:calc(16 * var(--gpx))`
289. `.tw-ml-5` → `margin-left:calc(20 * var(--gpx))`
290. `.tw-ml-6` → `margin-left:calc(24 * var(--gpx))`
291. `.tw-ml-8` → `margin-left:calc(32 * var(--gpx))`
292. `.tw-ml-9` → `margin-left:calc(36 * var(--gpx))`
293. `.tw-ml-\[-34px\]` → `margin-left:calc(-34 * var(--gpx))`
294. `.tw-ml-\[12px\]` → `margin-left:calc(12 * var(--gpx))`
295. `.tw-ml-\[2px\]` → `margin-left:calc(2 * var(--gpx))`
296. `.tw-ml-\[36px\]` → `margin-left:calc(36 * var(--gpx))`
297. `.tw-ml-\[3px\]` → `margin-left:calc(3 * var(--gpx))`
298. `.tw-ml-\[5px\]` → `margin-left:calc(5 * var(--gpx))`
299. `.tw-ml-\[6px\]` → `margin-left:calc(6 * var(--gpx))`
300. `.tw-mr-0\.5` → `margin-right:calc(2 * var(--gpx))`
301. `.tw-mr-1` → `margin-right:calc(4 * var(--gpx))`
302. `.tw-mr-1\.5` → `margin-right:calc(6 * var(--gpx))`
303. `.tw-mr-10` → `margin-right:calc(40 * var(--gpx))`
304. `.tw-mr-2` → `margin-right:calc(8 * var(--gpx))`
305. `.tw-mr-2\.5` → `margin-right:calc(10 * var(--gpx))`
306. `.tw-mr-3` → `margin-right:calc(12 * var(--gpx))`
307. `.tw-mr-3\.5` → `margin-right:calc(14 * var(--gpx))`
308. `.tw-mr-4` → `margin-right:calc(16 * var(--gpx))`
309. `.tw-mr-4\.5` → `margin-right:calc(18 * var(--gpx))`
310. `.tw-mr-9` → `margin-right:calc(36 * var(--gpx))`
311. `.tw-mr-\[-8px\]` → `margin-right:calc(-8 * var(--gpx))`
312. `.tw-mr-\[11px\]` → `margin-right:calc(11 * var(--gpx))`
313. `.tw-mr-\[14px\]` → `margin-right:calc(14 * var(--gpx))`
314. `.tw-mr-\[19px\]` → `margin-right:calc(19 * var(--gpx))`
315. `.tw-mr-\[2px\]` → `margin-right:calc(2 * var(--gpx))`
316. `.tw-mr-\[3px\]` → `margin-right:calc(3 * var(--gpx))`
317. `.tw-mr-\[5px\]` → `margin-right:calc(5 * var(--gpx))`
318. `.tw-mr-\[6px\]` → `margin-right:calc(6 * var(--gpx))`
319. `.tw-mr-\[72px\]` → `margin-right:calc(72 * var(--gpx))`
320. `.tw-mr-px` → `margin-right:calc(1 * var(--gpx))`
321. `.tw-mt-0\.5` → `margin-top:calc(2 * var(--gpx))`
322. `.tw-mt-1` → `margin-top:calc(4 * var(--gpx))`
323. `.tw-mt-1\.5` → `margin-top:calc(6 * var(--gpx))`
324. `.tw-mt-2` → `margin-top:calc(8 * var(--gpx))`
325. `.tw-mt-2\.5` → `margin-top:calc(10 * var(--gpx))`
326. `.tw-mt-3` → `margin-top:calc(12 * var(--gpx))`
327. `.tw-mt-4` → `margin-top:calc(16 * var(--gpx))`
328. `.tw-mt-40` → `margin-top:calc(160 * var(--gpx))`
329. `.tw-mt-5` → `margin-top:calc(20 * var(--gpx))`
330. `.tw-mt-6` → `margin-top:calc(24 * var(--gpx))`
331. `.tw-mt-8` → `margin-top:calc(32 * var(--gpx))`
332. `.tw-mt-\[-2px\]` → `margin-top:calc(-2 * var(--gpx))`
333. `.tw-mt-\[10px\]` → `margin-top:calc(10 * var(--gpx))`
334. `.tw-mt-\[120px\]` → `margin-top:calc(120 * var(--gpx))`
335. `.tw-mt-\[185px\]` → `margin-top:calc(185 * var(--gpx))`
336. `.tw-mt-\[1px\]` → `margin-top:calc(1 * var(--gpx))`
337. `.tw-mt-\[295px\]` → `margin-top:calc(295 * var(--gpx))`
338. `.tw-mt-\[332px\]` → `margin-top:calc(332 * var(--gpx))`
339. `.tw-mt-\[5px\]` → `margin-top:calc(5 * var(--gpx))`
340. `.tw-mt-\[67px\]` → `margin-top:calc(67 * var(--gpx))`
341. `.tw-mt-\[7px\]` → `margin-top:calc(7 * var(--gpx))`
342. `.tw-mt-\[8px\]` → `margin-top:calc(8 * var(--gpx))`
343. `.tw-box-border` → `box-sizing:border-box`
344. `.\!tw-line-clamp-2` → `overflow:hidden !important;display:-webkit-box !important;-webkit-box-orient:vertical !important;-webkit-line-clamp:2 !important`
345. `.tw-line-clamp-1` → `overflow:hidden;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:1`
346. `.tw-line-clamp-2` → `overflow:hidden;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:2`
347. `.tw-line-clamp-3` → `overflow:hidden;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:3`
348. `.tw-line-clamp-4` → `overflow:hidden;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:4`
349. `.tw-line-clamp-\[28px\]` → `overflow:hidden;display:-webkit-box;-webkit-box-orient:vertical;-webkit-line-clamp:calc(28 * var(--gpx))`
350. `.tw-block` → `display:block`
351. `.tw-inline-block` → `display:inline-block`
352. `.tw-flex` → `display:flex`
353. `.tw-inline-flex` → `display:inline-flex`
354. `.tw-grid` → `display:grid`
355. `.tw-hidden` → `display:none`
356. `.\!tw-h-40` → `height:calc(160 * var(--gpx)) !important`
357. `.\!tw-h-9` → `height:calc(36 * var(--gpx)) !important`
358. `.\!tw-h-\[160px\]` → `height:calc(160 * var(--gpx)) !important`
359. `.\!tw-h-auto` → `height:auto !important`
360. `.tw-h-1` → `height:calc(4 * var(--gpx))`
361. `.tw-h-1\.5` → `height:calc(6 * var(--gpx))`
362. `.tw-h-10` → `height:calc(40 * var(--gpx))`
363. `.tw-h-11` → `height:calc(44 * var(--gpx))`
364. `.tw-h-12` → `height:calc(48 * var(--gpx))`
365. `.tw-h-13` → `height:calc(52 * var(--gpx))`
366. `.tw-h-15` → `height:calc(60 * var(--gpx))`
367. `.tw-h-16` → `height:calc(64 * var(--gpx))`
368. `.tw-h-18` → `height:calc(72 * var(--gpx))`
369. `.tw-h-2` → `height:calc(8 * var(--gpx))`
370. `.tw-h-2\.5` → `height:calc(10 * var(--gpx))`
371. `.tw-h-20` → `height:calc(80 * var(--gpx))`
372. `.tw-h-25` → `height:calc(100 * var(--gpx))`
373. `.tw-h-3` → `height:calc(12 * var(--gpx))`
374. `.tw-h-4` → `height:calc(16 * var(--gpx))`
375. `.tw-h-40` → `height:calc(160 * var(--gpx))`
376. `.tw-h-5` → `height:calc(20 * var(--gpx))`
377. `.tw-h-5\.5` → `height:calc(22 * var(--gpx))`
378. `.tw-h-6` → `height:calc(24 * var(--gpx))`
379. `.tw-h-7` → `height:calc(28 * var(--gpx))`
380. `.tw-h-7\.5` → `height:calc(30 * var(--gpx))`
381. `.tw-h-8` → `height:calc(32 * var(--gpx))`
382. `.tw-h-9` → `height:calc(36 * var(--gpx))`
383. `.tw-h-\[0\.5px\]` → `height:calc(0.5 * var(--gpx))`
384. `.tw-h-\[100vh\]` → `height:100vh`
385. `.tw-h-\[102px\]` → `height:calc(102 * var(--gpx))`
386. `.tw-h-\[103px\]` → `height:calc(103 * var(--gpx))`
387. `.tw-h-\[108px\]` → `height:calc(108 * var(--gpx))`
388. `.tw-h-\[10px\]` → `height:calc(10 * var(--gpx))`
389. `.tw-h-\[114px\]` → `height:calc(114 * var(--gpx))`
390. `.tw-h-\[117px\]` → `height:calc(117 * var(--gpx))`
391. `.tw-h-\[137px\]` → `height:calc(137 * var(--gpx))`
392. `.tw-h-\[13px\]` → `height:calc(13 * var(--gpx))`
393. `.tw-h-\[142px\]` → `height:calc(142 * var(--gpx))`
394. `.tw-h-\[144px\]` → `height:calc(144 * var(--gpx))`
395. `.tw-h-\[148px\]` → `height:calc(148 * var(--gpx))`
396. `.tw-h-\[14px\]` → `height:calc(14 * var(--gpx))`
397. `.tw-h-\[152px\]` → `height:calc(152 * var(--gpx))`
398. `.tw-h-\[162px\]` → `height:calc(162 * var(--gpx))`
399. `.tw-h-\[16px\]` → `height:calc(16 * var(--gpx))`
400. `.tw-h-\[18px\]` → `height:calc(18 * var(--gpx))`
401. `.tw-h-\[1px\]` → `height:calc(1 * var(--gpx))`
402. `.tw-h-\[20px\]` → `height:calc(20 * var(--gpx))`
403. `.tw-h-\[220px\]` → `height:calc(220 * var(--gpx))`
404. `.tw-h-\[228px\]` → `height:calc(228 * var(--gpx))`
405. `.tw-h-\[22px\]` → `height:calc(22 * var(--gpx))`
406. `.tw-h-\[246px\]` → `height:calc(246 * var(--gpx))`
407. `.tw-h-\[24px\]` → `height:calc(24 * var(--gpx))`
408. `.tw-h-\[250px\]` → `height:calc(250 * var(--gpx))`
409. `.tw-h-\[257px\]` → `height:calc(257 * var(--gpx))`
410. `.tw-h-\[269px\]` → `height:calc(269 * var(--gpx))`
411. `.tw-h-\[26px\]` → `height:calc(26 * var(--gpx))`
412. `.tw-h-\[270px\]` → `height:calc(270 * var(--gpx))`
413. `.tw-h-\[28px\]` → `height:calc(28 * var(--gpx))`
414. `.tw-h-\[29px\]` → `height:calc(29 * var(--gpx))`
415. `.tw-h-\[2px\]` → `height:calc(2 * var(--gpx))`
416. `.tw-h-\[31px\]` → `height:calc(31 * var(--gpx))`
417. `.tw-h-\[327px\]` → `height:calc(327 * var(--gpx))`
418. `.tw-h-\[32px\]` → `height:calc(32 * var(--gpx))`
419. `.tw-h-\[33px\]` → `height:calc(33 * var(--gpx))`
420. `.tw-h-\[34px\]` → `height:calc(34 * var(--gpx))`
421. `.tw-h-\[35px\]` → `height:calc(35 * var(--gpx))`
422. `.tw-h-\[376px\]` → `height:calc(376 * var(--gpx))`
423. `.tw-h-\[38px\]` → `height:calc(38 * var(--gpx))`
424. `.tw-h-\[416px\]` → `height:calc(416 * var(--gpx))`
425. `.tw-h-\[42px\]` → `height:calc(42 * var(--gpx))`
426. `.tw-h-\[45px\]` → `height:calc(45 * var(--gpx))`
427. `.tw-h-\[460px\]` → `height:calc(460 * var(--gpx))`
428. `.tw-h-\[47px\]` → `height:calc(47 * var(--gpx))`
429. `.tw-h-\[4px\]` → `height:calc(4 * var(--gpx))`
430. `.tw-h-\[500px\]` → `height:calc(500 * var(--gpx))`
431. `.tw-h-\[504px\]` → `height:calc(504 * var(--gpx))`
432. `.tw-h-\[50px\]` → `height:calc(50 * var(--gpx))`
433. `.tw-h-\[51px\]` → `height:calc(51 * var(--gpx))`
434. `.tw-h-\[52px\]` → `height:calc(52 * var(--gpx))`
435. `.tw-h-\[540px\]` → `height:calc(540 * var(--gpx))`
436. `.tw-h-\[54px\]` → `height:calc(54 * var(--gpx))`
437. `.tw-h-\[56px\]` → `height:calc(56 * var(--gpx))`
438. `.tw-h-\[600px\]` → `height:calc(600 * var(--gpx))`
439. `.tw-h-\[60px\]` → `height:calc(60 * var(--gpx))`
440. `.tw-h-\[612px\]` → `height:calc(612 * var(--gpx))`
441. `.tw-h-\[62px\]` → `height:calc(62 * var(--gpx))`
442. `.tw-h-\[64px\]` → `height:calc(64 * var(--gpx))`
443. `.tw-h-\[652px\]` → `height:calc(652 * var(--gpx))`
444. `.tw-h-\[657px\]` → `height:calc(657 * var(--gpx))`
445. `.tw-h-\[68px\]` → `height:calc(68 * var(--gpx))`
446. `.tw-h-\[720px\]` → `height:calc(720 * var(--gpx))`
447. `.tw-h-\[75px\]` → `height:calc(75 * var(--gpx))`
448. `.tw-h-\[80vh\]` → `height:80vh`
449. `.tw-h-\[82px\]` → `height:calc(82 * var(--gpx))`
450. `.tw-h-\[868px\]` → `height:calc(868 * var(--gpx))`
451. `.tw-h-\[86px\]` → `height:calc(86 * var(--gpx))`
452. `.tw-h-\[90px\]` → `height:calc(90 * var(--gpx))`
453. `.tw-h-\[91px\]` → `height:calc(91 * var(--gpx))`
454. `.tw-h-\[92px\]` → `height:calc(92 * var(--gpx))`
455. `.tw-h-\[94px\]` → `height:calc(94 * var(--gpx))`
456. `.tw-h-\[98px\]` → `height:calc(98 * var(--gpx))`
457. `.tw-h-\[9px\]` → `height:calc(9 * var(--gpx))`
458. `.tw-h-auto` → `height:auto`
459. `.tw-h-full` → `height:100%`
460. `.tw-h-px` → `height:calc(1 * var(--gpx))`
461. `.tw-h-screen` → `height:100vh`
462. `.tw-max-h-\[300px\]` → `max-height:calc(300 * var(--gpx))`
463. `.tw-max-h-\[30px\]` → `max-height:calc(30 * var(--gpx))`
464. `.tw-max-h-\[60vh\]` → `max-height:60vh`
465. `.tw-max-h-\[640px\]` → `max-height:calc(640 * var(--gpx))`
466. `.tw-max-h-\[90vh\]` → `max-height:90vh`
467. `.tw-min-h-16` → `min-height:calc(64 * var(--gpx))`
468. `.tw-min-h-8` → `min-height:calc(32 * var(--gpx))`
469. `.tw-min-h-\[314px\]` → `min-height:calc(314 * var(--gpx))`
470. `.tw-min-h-\[34px\]` → `min-height:calc(34 * var(--gpx))`
471. `.tw-min-h-\[50vh\]` → `min-height:50vh`
472. `.tw-min-h-\[60px\]` → `min-height:calc(60 * var(--gpx))`
473. `.tw-min-h-\[636px\]` → `min-height:calc(636 * var(--gpx))`
474. `.tw-min-h-\[68px\]` → `min-height:calc(68 * var(--gpx))`
475. `.tw-min-h-\[82px\]` → `min-height:calc(82 * var(--gpx))`
476. `.tw-min-h-full` → `min-height:100%`
477. `.\!tw-w-40` → `width:calc(160 * var(--gpx)) !important`
478. `.\!tw-w-9` → `width:calc(36 * var(--gpx)) !important`
479. `.\!tw-w-\[160px\]` → `width:calc(160 * var(--gpx)) !important`
480. `.\!tw-w-\[165px\]` → `width:calc(165 * var(--gpx)) !important`
481. `.\!tw-w-\[270px\]` → `width:calc(270 * var(--gpx)) !important`
482. `.tw-w-0` → `width:calc(0 * var(--gpx))`
483. `.tw-w-1` → `width:calc(4 * var(--gpx))`
484. `.tw-w-10` → `width:calc(40 * var(--gpx))`
485. `.tw-w-11` → `width:calc(44 * var(--gpx))`
486. `.tw-w-12` → `width:calc(48 * var(--gpx))`
487. `.tw-w-15` → `width:calc(60 * var(--gpx))`
488. `.tw-w-16` → `width:calc(64 * var(--gpx))`
489. `.tw-w-2` → `width:calc(8 * var(--gpx))`
490. `.tw-w-2\.5` → `width:calc(10 * var(--gpx))`
491. `.tw-w-20` → `width:calc(80 * var(--gpx))`
492. `.tw-w-25` → `width:calc(100 * var(--gpx))`
493. `.tw-w-3` → `width:calc(12 * var(--gpx))`
494. `.tw-w-33` → `width:calc(132 * var(--gpx))`
495. `.tw-w-4` → `width:calc(16 * var(--gpx))`
496. `.tw-w-40` → `width:calc(160 * var(--gpx))`
497. `.tw-w-5` → `width:calc(20 * var(--gpx))`
498. `.tw-w-5\.5` → `width:calc(22 * var(--gpx))`
499. `.tw-w-6` → `width:calc(24 * var(--gpx))`
500. `.tw-w-7` → `width:calc(28 * var(--gpx))`
501. `.tw-w-7\.5` → `width:calc(30 * var(--gpx))`
502. `.tw-w-8` → `width:calc(32 * var(--gpx))`
503. `.tw-w-9` → `width:calc(36 * var(--gpx))`
504. `.tw-w-\[1008px\]` → `width:calc(1008 * var(--gpx))`
505. `.tw-w-\[1024px\]` → `width:calc(1024 * var(--gpx))`
506. `.tw-w-\[1026px\]` → `width:calc(1026 * var(--gpx))`
507. `.tw-w-\[102px\]` → `width:calc(102 * var(--gpx))`
508. `.tw-w-\[108px\]` → `width:calc(108 * var(--gpx))`
509. `.tw-w-\[109px\]` → `width:calc(109 * var(--gpx))`
510. `.tw-w-\[10px\]` → `width:calc(10 * var(--gpx))`
511. `.tw-w-\[114px\]` → `width:calc(114 * var(--gpx))`
512. `.tw-w-\[115px\]` → `width:calc(115 * var(--gpx))`
513. `.tw-w-\[117px\]` → `width:calc(117 * var(--gpx))`
514. `.tw-w-\[1200px\]` → `width:calc(1200 * var(--gpx))`
515. `.tw-w-\[130px\]` → `width:calc(130 * var(--gpx))`
516. `.tw-w-\[13px\]` → `width:calc(13 * var(--gpx))`
517. `.tw-w-\[142px\]` → `width:calc(142 * var(--gpx))`
518. `.tw-w-\[143px\]` → `width:calc(143 * var(--gpx))`
519. `.tw-w-\[146px\]` → `width:calc(146 * var(--gpx))`
520. `.tw-w-\[14px\]` → `width:calc(14 * var(--gpx))`
521. `.tw-w-\[156px\]` → `width:calc(156 * var(--gpx))`
522. `.tw-w-\[15px\]` → `width:calc(15 * var(--gpx))`
523. `.tw-w-\[166px\]` → `width:calc(166 * var(--gpx))`
524. `.tw-w-\[168px\]` → `width:calc(168 * var(--gpx))`
525. `.tw-w-\[169px\]` → `width:calc(169 * var(--gpx))`
526. `.tw-w-\[172px\]` → `width:calc(172 * var(--gpx))`
527. `.tw-w-\[18px\]` → `width:calc(18 * var(--gpx))`
528. `.tw-w-\[196px\]` → `width:calc(196 * var(--gpx))`
529. `.tw-w-\[1px\]` → `width:calc(1 * var(--gpx))`
530. `.tw-w-\[200px\]` → `width:calc(200 * var(--gpx))`
531. `.tw-w-\[20px\]` → `width:calc(20 * var(--gpx))`
532. `.tw-w-\[219px\]` → `width:calc(219 * var(--gpx))`
533. `.tw-w-\[224px\]` → `width:calc(224 * var(--gpx))`
534. `.tw-w-\[22px\]` → `width:calc(22 * var(--gpx))`
535. `.tw-w-\[240px\]` → `width:calc(240 * var(--gpx))`
536. `.tw-w-\[248px\]` → `width:calc(248 * var(--gpx))`
537. `.tw-w-\[24px\]` → `width:calc(24 * var(--gpx))`
538. `.tw-w-\[263px\]` → `width:calc(263 * var(--gpx))`
539. `.tw-w-\[268px\]` → `width:calc(268 * var(--gpx))`
540. `.tw-w-\[26px\]` → `width:calc(26 * var(--gpx))`
541. `.tw-w-\[280px\]` → `width:calc(280 * var(--gpx))`
542. `.tw-w-\[283px\]` → `width:calc(283 * var(--gpx))`
543. `.tw-w-\[28px\]` → `width:calc(28 * var(--gpx))`
544. `.tw-w-\[292px\]` → `width:calc(292 * var(--gpx))`
545. `.tw-w-\[299px\]` → `width:calc(299 * var(--gpx))`
546. `.tw-w-\[300px\]` → `width:calc(300 * var(--gpx))`
547. `.tw-w-\[313px\]` → `width:calc(313 * var(--gpx))`
548. `.tw-w-\[327px\]` → `width:calc(327 * var(--gpx))`
549. `.tw-w-\[32px\]` → `width:calc(32 * var(--gpx))`
550. `.tw-w-\[343px\]` → `width:calc(343 * var(--gpx))`
551. `.tw-w-\[345px\]` → `width:calc(345 * var(--gpx))`
552. `.tw-w-\[34px\]` → `width:calc(34 * var(--gpx))`
553. `.tw-w-\[351px\]` → `width:calc(351 * var(--gpx))`
554. `.tw-w-\[360px\]` → `width:calc(360 * var(--gpx))`
555. `.tw-w-\[370px\]` → `width:calc(370 * var(--gpx))`
556. `.tw-w-\[38px\]` → `width:calc(38 * var(--gpx))`
557. `.tw-w-\[3px\]` → `width:calc(3 * var(--gpx))`
558. `.tw-w-\[408px\]` → `width:calc(408 * var(--gpx))`
559. `.tw-w-\[42px\]` → `width:calc(42 * var(--gpx))`
560. `.tw-w-\[45px\]` → `width:calc(45 * var(--gpx))`
561. `.tw-w-\[460px\]` → `width:calc(460 * var(--gpx))`
562. `.tw-w-\[464px\]` → `width:calc(464 * var(--gpx))`
563. `.tw-w-\[475px\]` → `width:calc(475 * var(--gpx))`
564. `.tw-w-\[476px\]` → `width:calc(476 * var(--gpx))`
565. `.tw-w-\[50\%\]` → `width:50%`
566. `.tw-w-\[500px\]` → `width:calc(500 * var(--gpx))`
567. `.tw-w-\[50px\]` → `width:calc(50 * var(--gpx))`
568. `.tw-w-\[540px\]` → `width:calc(540 * var(--gpx))`
569. `.tw-w-\[542px\]` → `width:calc(542 * var(--gpx))`
570. `.tw-w-\[54px\]` → `width:calc(54 * var(--gpx))`
571. `.tw-w-\[552px\]` → `width:calc(552 * var(--gpx))`
572. `.tw-w-\[560px\]` → `width:calc(560 * var(--gpx))`
573. `.tw-w-\[580px\]` → `width:calc(580 * var(--gpx))`
574. `.tw-w-\[60px\]` → `width:calc(60 * var(--gpx))`
575. `.tw-w-\[62px\]` → `width:calc(62 * var(--gpx))`
576. `.tw-w-\[64px\]` → `width:calc(64 * var(--gpx))`
577. `.tw-w-\[696px\]` → `width:calc(696 * var(--gpx))`
578. `.tw-w-\[6px\]` → `width:calc(6 * var(--gpx))`
579. `.tw-w-\[712px\]` → `width:calc(712 * var(--gpx))`
580. `.tw-w-\[72px\]` → `width:calc(72 * var(--gpx))`
581. `.tw-w-\[752px\]` → `width:calc(752 * var(--gpx))`
582. `.tw-w-\[75px\]` → `width:calc(75 * var(--gpx))`
583. `.tw-w-\[82px\]` → `width:calc(82 * var(--gpx))`
584. `.tw-w-\[840px\]` → `width:calc(840 * var(--gpx))`
585. `.tw-w-\[896px\]` → `width:calc(896 * var(--gpx))`
586. `.tw-w-\[90px\]` → `width:calc(90 * var(--gpx))`
587. `.tw-w-\[92px\]` → `width:calc(92 * var(--gpx))`
588. `.tw-w-\[932px\]` → `width:calc(932 * var(--gpx))`
589. `.tw-w-\[960px\]` → `width:calc(960 * var(--gpx))`
590. `.tw-w-\[9px\]` → `width:calc(9 * var(--gpx))`
591. `.tw-w-auto` → `width:auto`
592. `.tw-w-fit` → `width:-webkit-fit-content;width:-moz-fit-content;width:fit-content`
593. `.tw-w-full` → `width:100%`
594. `.tw-w-px` → `width:calc(1 * var(--gpx))`
595. `.tw-min-w-10` → `min-width:calc(40 * var(--gpx))`
596. `.tw-min-w-15` → `min-width:calc(60 * var(--gpx))`
597. `.tw-min-w-\[1200px\]` → `min-width:calc(1200 * var(--gpx))`
598. `.tw-max-w-\[114px\]` → `max-width:calc(114 * var(--gpx))`
599. `.tw-max-w-\[1200px\]` → `max-width:calc(1200 * var(--gpx))`
600. `.tw-max-w-\[140px\]` → `max-width:calc(140 * var(--gpx))`
601. `.tw-max-w-\[154px\]` → `max-width:calc(154 * var(--gpx))`
602. `.tw-max-w-\[186px\]` → `max-width:calc(186 * var(--gpx))`
603. `.tw-max-w-\[200px\]` → `max-width:calc(200 * var(--gpx))`
604. `.tw-max-w-\[300px\]` → `max-width:calc(300 * var(--gpx))`
605. `.tw-max-w-\[44px\]` → `max-width:calc(44 * var(--gpx))`
606. `.tw-max-w-\[60\%\]` → `max-width:60%`
607. `.tw-max-w-\[94px\]` → `max-width:calc(94 * var(--gpx))`
608. `.tw-flex-1` → `flex:1 1 0%`
609. `.tw-flex-shrink-0` → `flex-shrink:0`
610. `.tw-shrink-0` → `flex-shrink:0`
611. `.tw-flex-grow` → `flex-grow:1`
612. `.tw-grow` → `flex-grow:1`
613. `.tw-origin-center` → `-webkit-transform-origin:center;transform-origin:center`
614. `.tw-origin-top-left` → `-webkit-transform-origin:top left;transform-origin:top left`
615. `.-tw-translate-y-1\/2` → `--tw-translate-y:-50%;-webkit-transform:translate(var(--tw-translate-x), -50%) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), -50%) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
616. `.tw-translate-x-\[-3px\]` → `--tw-translate-x:calc(-3 * var(--gpx));-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
617. `.tw-translate-x-\[-4px\]` → `--tw-translate-x:calc(-4 * var(--gpx));-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
618. `.tw-translate-x-\[-50\%\]` → `--tw-translate-x:-50%;-webkit-transform:translate(-50%, var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(-50%, var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
619. `.tw-translate-y-\[-3px\]` → `--tw-translate-y:calc(-3 * var(--gpx));-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
620. `.tw-translate-y-\[-4px\]` → `--tw-translate-y:calc(-4 * var(--gpx));-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
621. `.-tw-rotate-90` → `--tw-rotate:-90deg;-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(-90deg) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(-90deg) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
622. `.tw-rotate-180` → `--tw-rotate:180deg;-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(180deg) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(180deg) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
623. `.tw-rotate-90` → `--tw-rotate:90deg;-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(90deg) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(90deg) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
624. `.tw-rotate-\[180deg\]` → `--tw-rotate:180deg;-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(180deg) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(180deg) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
625. `.tw-scale-\[0\.865\]` → `--tw-scale-x:0.865;--tw-scale-y:0.865;-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(0.865) scaleY(0.865);transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(0.865) scaleY(0.865);-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
626. `.tw-scale-\[0\.8\]` → `--tw-scale-x:0.8;--tw-scale-y:0.8;-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(0.8) scaleY(0.8);transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(0.8) scaleY(0.8);-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
627. `.tw-scale-\[1\.025\]` → `--tw-scale-x:1.025;--tw-scale-y:1.025;-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(1.025) scaleY(1.025);transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(1.025) scaleY(1.025);-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
628. `.tw-scale-\[1\.032258\]` → `--tw-scale-x:1.032258;--tw-scale-y:1.032258;-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(1.032258) scaleY(1.032258);transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(1.032258) scaleY(1.032258);-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
629. `.tw-scale-\[1\.125\]` → `--tw-scale-x:1.125;--tw-scale-y:1.125;-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(1.125) scaleY(1.125);transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(1.125) scaleY(1.125);-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
630. `.tw-scale-\[1\.333333\]` → `--tw-scale-x:1.333333;--tw-scale-y:1.333333;-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(1.333333) scaleY(1.333333);transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(1.333333) scaleY(1.333333);-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
631. `.tw-scale-\[1\.6\]` → `--tw-scale-x:1.6;--tw-scale-y:1.6;-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(1.6) scaleY(1.6);transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(1.6) scaleY(1.6);-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
632. `.tw-transform-gpu` → `-webkit-transform:translate3d(var(--tw-translate-x), var(--tw-translate-y), 0) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate3d(var(--tw-translate-x), var(--tw-translate-y), 0) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
633. `.\!tw-cursor-default` → `cursor:default !important`
634. `.tw-cursor-pointer` → `cursor:pointer`
635. `.tw-select-none` → `-webkit-user-select:none;-moz-user-select:none;user-select:none`
636. `.tw-grid-cols-2` → `grid-template-columns:repeat(2, minmax(0, 1fr))`
637. `.tw-grid-cols-3` → `grid-template-columns:repeat(3, minmax(0, 1fr))`
638. `.tw-grid-cols-5` → `grid-template-columns:repeat(5, minmax(0, 1fr))`
639. `.tw-grid-cols-6` → `grid-template-columns:repeat(6, minmax(0, 1fr))`
640. `.tw-flex-col` → `flex-direction:column`
641. `.tw-flex-wrap` → `flex-wrap:wrap`
642. `.tw-flex-nowrap` → `flex-wrap:nowrap`
643. `.tw-items-start` → `align-items:flex-start`
644. `.tw-items-end` → `align-items:flex-end`
645. `.\!tw-items-center` → `align-items:center !important`
646. `.tw-items-center` → `align-items:center`
647. `.tw-justify-start` → `justify-content:flex-start`
648. `.tw-justify-end` → `justify-content:flex-end`
649. `.tw-justify-center` → `justify-content:center`
650. `.tw-justify-between` → `justify-content:space-between`
651. `.tw-justify-around` → `justify-content:space-around`
652. `.tw-justify-evenly` → `justify-content:space-evenly`
653. `.tw-gap-2` → `gap:calc(8 * var(--gpx))`
654. `.tw-gap-3` → `gap:calc(12 * var(--gpx))`
655. `.tw-gap-x-1` → `-webkit-column-gap:calc(4 * var(--gpx));-moz-column-gap:calc(4 * var(--gpx));column-gap:calc(4 * var(--gpx))`
656. `.tw-gap-x-2` → `-webkit-column-gap:calc(8 * var(--gpx));-moz-column-gap:calc(8 * var(--gpx));column-gap:calc(8 * var(--gpx))`
657. `.tw-gap-x-2\.5` → `-webkit-column-gap:calc(10 * var(--gpx));-moz-column-gap:calc(10 * var(--gpx));column-gap:calc(10 * var(--gpx))`
658. `.tw-gap-x-3` → `-webkit-column-gap:calc(12 * var(--gpx));-moz-column-gap:calc(12 * var(--gpx));column-gap:calc(12 * var(--gpx))`
659. `.tw-gap-x-5` → `-webkit-column-gap:calc(20 * var(--gpx));-moz-column-gap:calc(20 * var(--gpx));column-gap:calc(20 * var(--gpx))`
660. `.tw-gap-x-\[12px\]` → `-webkit-column-gap:calc(12 * var(--gpx));-moz-column-gap:calc(12 * var(--gpx));column-gap:calc(12 * var(--gpx))`
661. `.tw-gap-y-1\.5` → `row-gap:calc(6 * var(--gpx))`
662. `.tw-gap-y-8` → `row-gap:calc(32 * var(--gpx))`
663. `.tw-gap-y-\[22px\]` → `row-gap:calc(22 * var(--gpx))`
664. `.tw-gap-y-\[6px\]` → `row-gap:calc(6 * var(--gpx))`
665. `.tw-space-x-0\.5 > :not([hidden]) ~ :not([hidden])` → `--tw-space-x-reverse:0;margin-right:calc((2 * var(--gpx)) * 0);margin-right:calc((2 * var(--gpx)) * var(--tw-space-x-reverse));margin-right:calc(calc(2 * var(--gpx)) * 0);margin-right:calc(calc(2 * var(--gpx)) * var(--tw-space-x-reverse));margin-left:calc((2 * var(--gpx)) * (1 - 0));margin-left:calc((2 * var(--gpx)) * (1 - var(--tw-space-x-reverse)));margin-left:calc(calc(2 * var(--gpx)) * calc(1 - 0));margin-left:calc(calc(2 * var(--gpx)) * calc(1 - var(--tw-space-x-reverse)))`
666. `.tw-space-x-1 > :not([hidden]) ~ :not([hidden])` → `--tw-space-x-reverse:0;margin-right:calc((4 * var(--gpx)) * 0);margin-right:calc((4 * var(--gpx)) * var(--tw-space-x-reverse));margin-right:calc(calc(4 * var(--gpx)) * 0);margin-right:calc(calc(4 * var(--gpx)) * var(--tw-space-x-reverse));margin-left:calc((4 * var(--gpx)) * (1 - 0));margin-left:calc((4 * var(--gpx)) * (1 - var(--tw-space-x-reverse)));margin-left:calc(calc(4 * var(--gpx)) * calc(1 - 0));margin-left:calc(calc(4 * var(--gpx)) * calc(1 - var(--tw-space-x-reverse)))`
667. `.tw-space-x-2 > :not([hidden]) ~ :not([hidden])` → `--tw-space-x-reverse:0;margin-right:calc((8 * var(--gpx)) * 0);margin-right:calc((8 * var(--gpx)) * var(--tw-space-x-reverse));margin-right:calc(calc(8 * var(--gpx)) * 0);margin-right:calc(calc(8 * var(--gpx)) * var(--tw-space-x-reverse));margin-left:calc((8 * var(--gpx)) * (1 - 0));margin-left:calc((8 * var(--gpx)) * (1 - var(--tw-space-x-reverse)));margin-left:calc(calc(8 * var(--gpx)) * calc(1 - 0));margin-left:calc(calc(8 * var(--gpx)) * calc(1 - var(--tw-space-x-reverse)))`
668. `.tw-space-x-2\.5 > :not([hidden]) ~ :not([hidden])` → `--tw-space-x-reverse:0;margin-right:calc((10 * var(--gpx)) * 0);margin-right:calc((10 * var(--gpx)) * var(--tw-space-x-reverse));margin-right:calc(calc(10 * var(--gpx)) * 0);margin-right:calc(calc(10 * var(--gpx)) * var(--tw-space-x-reverse));margin-left:calc((10 * var(--gpx)) * (1 - 0));margin-left:calc((10 * var(--gpx)) * (1 - var(--tw-space-x-reverse)));margin-left:calc(calc(10 * var(--gpx)) * calc(1 - 0));margin-left:calc(calc(10 * var(--gpx)) * calc(1 - var(--tw-space-x-reverse)))`
669. `.tw-space-x-3 > :not([hidden]) ~ :not([hidden])` → `--tw-space-x-reverse:0;margin-right:calc((12 * var(--gpx)) * 0);margin-right:calc((12 * var(--gpx)) * var(--tw-space-x-reverse));margin-right:calc(calc(12 * var(--gpx)) * 0);margin-right:calc(calc(12 * var(--gpx)) * var(--tw-space-x-reverse));margin-left:calc((12 * var(--gpx)) * (1 - 0));margin-left:calc((12 * var(--gpx)) * (1 - var(--tw-space-x-reverse)));margin-left:calc(calc(12 * var(--gpx)) * calc(1 - 0));margin-left:calc(calc(12 * var(--gpx)) * calc(1 - var(--tw-space-x-reverse)))`
670. `.tw-space-x-4 > :not([hidden]) ~ :not([hidden])` → `--tw-space-x-reverse:0;margin-right:calc((16 * var(--gpx)) * 0);margin-right:calc((16 * var(--gpx)) * var(--tw-space-x-reverse));margin-right:calc(calc(16 * var(--gpx)) * 0);margin-right:calc(calc(16 * var(--gpx)) * var(--tw-space-x-reverse));margin-left:calc((16 * var(--gpx)) * (1 - 0));margin-left:calc((16 * var(--gpx)) * (1 - var(--tw-space-x-reverse)));margin-left:calc(calc(16 * var(--gpx)) * calc(1 - 0));margin-left:calc(calc(16 * var(--gpx)) * calc(1 - var(--tw-space-x-reverse)))`
671. `.tw-space-x-6 > :not([hidden]) ~ :not([hidden])` → `--tw-space-x-reverse:0;margin-right:calc((24 * var(--gpx)) * 0);margin-right:calc((24 * var(--gpx)) * var(--tw-space-x-reverse));margin-right:calc(calc(24 * var(--gpx)) * 0);margin-right:calc(calc(24 * var(--gpx)) * var(--tw-space-x-reverse));margin-left:calc((24 * var(--gpx)) * (1 - 0));margin-left:calc((24 * var(--gpx)) * (1 - var(--tw-space-x-reverse)));margin-left:calc(calc(24 * var(--gpx)) * calc(1 - 0));margin-left:calc(calc(24 * var(--gpx)) * calc(1 - var(--tw-space-x-reverse)))`
672. `.tw-space-x-\[10px\] > :not([hidden]) ~ :not([hidden])` → `--tw-space-x-reverse:0;margin-right:calc((10 * var(--gpx)) * 0);margin-right:calc((10 * var(--gpx)) * var(--tw-space-x-reverse));margin-right:calc(calc(10 * var(--gpx)) * 0);margin-right:calc(calc(10 * var(--gpx)) * var(--tw-space-x-reverse));margin-left:calc((10 * var(--gpx)) * (1 - 0));margin-left:calc((10 * var(--gpx)) * (1 - var(--tw-space-x-reverse)));margin-left:calc(calc(10 * var(--gpx)) * calc(1 - 0));margin-left:calc(calc(10 * var(--gpx)) * calc(1 - var(--tw-space-x-reverse)))`
673. `.tw-space-x-\[2px\] > :not([hidden]) ~ :not([hidden])` → `--tw-space-x-reverse:0;margin-right:calc((2 * var(--gpx)) * 0);margin-right:calc((2 * var(--gpx)) * var(--tw-space-x-reverse));margin-right:calc(calc(2 * var(--gpx)) * 0);margin-right:calc(calc(2 * var(--gpx)) * var(--tw-space-x-reverse));margin-left:calc((2 * var(--gpx)) * (1 - 0));margin-left:calc((2 * var(--gpx)) * (1 - var(--tw-space-x-reverse)));margin-left:calc(calc(2 * var(--gpx)) * calc(1 - 0));margin-left:calc(calc(2 * var(--gpx)) * calc(1 - var(--tw-space-x-reverse)))`
674. `.tw-space-y-0\.5 > :not([hidden]) ~ :not([hidden])` → `--tw-space-y-reverse:0;margin-top:calc((2 * var(--gpx)) * (1 - 0));margin-top:calc((2 * var(--gpx)) * (1 - var(--tw-space-y-reverse)));margin-top:calc(calc(2 * var(--gpx)) * calc(1 - 0));margin-top:calc(calc(2 * var(--gpx)) * calc(1 - var(--tw-space-y-reverse)));margin-bottom:calc((2 * var(--gpx)) * 0);margin-bottom:calc((2 * var(--gpx)) * var(--tw-space-y-reverse));margin-bottom:calc(calc(2 * var(--gpx)) * 0);margin-bottom:calc(calc(2 * var(--gpx)) * var(--tw-space-y-reverse))`
675. `.tw-space-y-1 > :not([hidden]) ~ :not([hidden])` → `--tw-space-y-reverse:0;margin-top:calc((4 * var(--gpx)) * (1 - 0));margin-top:calc((4 * var(--gpx)) * (1 - var(--tw-space-y-reverse)));margin-top:calc(calc(4 * var(--gpx)) * calc(1 - 0));margin-top:calc(calc(4 * var(--gpx)) * calc(1 - var(--tw-space-y-reverse)));margin-bottom:calc((4 * var(--gpx)) * 0);margin-bottom:calc((4 * var(--gpx)) * var(--tw-space-y-reverse));margin-bottom:calc(calc(4 * var(--gpx)) * 0);margin-bottom:calc(calc(4 * var(--gpx)) * var(--tw-space-y-reverse))`
676. `.tw-space-y-2 > :not([hidden]) ~ :not([hidden])` → `--tw-space-y-reverse:0;margin-top:calc((8 * var(--gpx)) * (1 - 0));margin-top:calc((8 * var(--gpx)) * (1 - var(--tw-space-y-reverse)));margin-top:calc(calc(8 * var(--gpx)) * calc(1 - 0));margin-top:calc(calc(8 * var(--gpx)) * calc(1 - var(--tw-space-y-reverse)));margin-bottom:calc((8 * var(--gpx)) * 0);margin-bottom:calc((8 * var(--gpx)) * var(--tw-space-y-reverse));margin-bottom:calc(calc(8 * var(--gpx)) * 0);margin-bottom:calc(calc(8 * var(--gpx)) * var(--tw-space-y-reverse))`
677. `.tw-space-y-3 > :not([hidden]) ~ :not([hidden])` → `--tw-space-y-reverse:0;margin-top:calc((12 * var(--gpx)) * (1 - 0));margin-top:calc((12 * var(--gpx)) * (1 - var(--tw-space-y-reverse)));margin-top:calc(calc(12 * var(--gpx)) * calc(1 - 0));margin-top:calc(calc(12 * var(--gpx)) * calc(1 - var(--tw-space-y-reverse)));margin-bottom:calc((12 * var(--gpx)) * 0);margin-bottom:calc((12 * var(--gpx)) * var(--tw-space-y-reverse));margin-bottom:calc(calc(12 * var(--gpx)) * 0);margin-bottom:calc(calc(12 * var(--gpx)) * var(--tw-space-y-reverse))`
678. `.tw-space-y-4 > :not([hidden]) ~ :not([hidden])` → `--tw-space-y-reverse:0;margin-top:calc((16 * var(--gpx)) * (1 - 0));margin-top:calc((16 * var(--gpx)) * (1 - var(--tw-space-y-reverse)));margin-top:calc(calc(16 * var(--gpx)) * calc(1 - 0));margin-top:calc(calc(16 * var(--gpx)) * calc(1 - var(--tw-space-y-reverse)));margin-bottom:calc((16 * var(--gpx)) * 0);margin-bottom:calc((16 * var(--gpx)) * var(--tw-space-y-reverse));margin-bottom:calc(calc(16 * var(--gpx)) * 0);margin-bottom:calc(calc(16 * var(--gpx)) * var(--tw-space-y-reverse))`
679. `.tw-space-y-6 > :not([hidden]) ~ :not([hidden])` → `--tw-space-y-reverse:0;margin-top:calc((24 * var(--gpx)) * (1 - 0));margin-top:calc((24 * var(--gpx)) * (1 - var(--tw-space-y-reverse)));margin-top:calc(calc(24 * var(--gpx)) * calc(1 - 0));margin-top:calc(calc(24 * var(--gpx)) * calc(1 - var(--tw-space-y-reverse)));margin-bottom:calc((24 * var(--gpx)) * 0);margin-bottom:calc((24 * var(--gpx)) * var(--tw-space-y-reverse));margin-bottom:calc(calc(24 * var(--gpx)) * 0);margin-bottom:calc(calc(24 * var(--gpx)) * var(--tw-space-y-reverse))`
680. `.tw-space-y-8 > :not([hidden]) ~ :not([hidden])` → `--tw-space-y-reverse:0;margin-top:calc((32 * var(--gpx)) * (1 - 0));margin-top:calc((32 * var(--gpx)) * (1 - var(--tw-space-y-reverse)));margin-top:calc(calc(32 * var(--gpx)) * calc(1 - 0));margin-top:calc(calc(32 * var(--gpx)) * calc(1 - var(--tw-space-y-reverse)));margin-bottom:calc((32 * var(--gpx)) * 0);margin-bottom:calc((32 * var(--gpx)) * var(--tw-space-y-reverse));margin-bottom:calc(calc(32 * var(--gpx)) * 0);margin-bottom:calc(calc(32 * var(--gpx)) * var(--tw-space-y-reverse))`
681. `.\!tw-self-auto` → `align-self:auto !important`
682. `.tw-overflow-hidden` → `overflow:hidden`
683. `.tw-overflow-x-auto` → `overflow-x:auto`
684. `.tw-overflow-y-auto` → `overflow-y:auto`
685. `.tw-overflow-x-hidden` → `overflow-x:hidden`
686. `.tw-overflow-x-scroll` → `overflow-x:scroll`
687. `.tw-overflow-y-scroll` → `overflow-y:scroll`
688. `.tw-truncate` → `overflow:hidden;text-overflow:ellipsis;white-space:nowrap`
689. `.tw-text-ellipsis` → `text-overflow:ellipsis`
690. `.tw-whitespace-nowrap` → `white-space:nowrap`
691. `.tw-whitespace-pre-line` → `white-space:pre-line`
692. `.tw-break-words` → `word-wrap:break-word`
693. `.tw-rounded` → `border-radius:calc(4 * var(--gpx))`
694. `.tw-rounded-2xl` → `border-radius:calc(16 * var(--gpx))`
695. `.tw-rounded-\[10px\]` → `border-radius:calc(10 * var(--gpx))`
696. `.tw-rounded-\[11px\]` → `border-radius:calc(11 * var(--gpx))`
697. `.tw-rounded-\[12px\]` → `border-radius:calc(12 * var(--gpx))`
698. `.tw-rounded-\[16px\]` → `border-radius:calc(16 * var(--gpx))`
699. `.tw-rounded-\[18px\]` → `border-radius:calc(18 * var(--gpx))`
700. `.tw-rounded-\[20px\]` → `border-radius:calc(20 * var(--gpx))`
701. `.tw-rounded-\[22px\]` → `border-radius:calc(22 * var(--gpx))`
702. `.tw-rounded-\[2px\]` → `border-radius:calc(2 * var(--gpx))`
703. `.tw-rounded-\[31px\]` → `border-radius:calc(31 * var(--gpx))`
704. `.tw-rounded-\[32px\]` → `border-radius:calc(32 * var(--gpx))`
705. `.tw-rounded-\[4px\]` → `border-radius:calc(4 * var(--gpx))`
706. `.tw-rounded-\[5px\]` → `border-radius:calc(5 * var(--gpx))`
707. `.tw-rounded-\[6px\]` → `border-radius:calc(6 * var(--gpx))`
708. `.tw-rounded-full` → `border-radius:calc(9999 * var(--gpx))`
709. `.tw-rounded-lg` → `border-radius:calc(8 * var(--gpx))`
710. `.tw-rounded-md` → `border-radius:calc(6 * var(--gpx))`
711. `.tw-rounded-sm` → `border-radius:calc(2 * var(--gpx))`
712. `.tw-rounded-xl` → `border-radius:calc(12 * var(--gpx))`
713. `.tw-rounded-b` → `border-bottom-right-radius:calc(4 * var(--gpx));border-bottom-left-radius:calc(4 * var(--gpx))`
714. `.tw-rounded-b-lg` → `border-bottom-right-radius:calc(8 * var(--gpx));border-bottom-left-radius:calc(8 * var(--gpx))`
715. `.tw-rounded-bl` → `border-bottom-left-radius:calc(4 * var(--gpx))`
716. `.tw-rounded-bl-2xl` → `border-bottom-left-radius:calc(16 * var(--gpx))`
717. `.tw-rounded-bl-\[2px\]` → `border-bottom-left-radius:calc(2 * var(--gpx))`
718. `.tw-rounded-br-2xl` → `border-bottom-right-radius:calc(16 * var(--gpx))`
719. `.tw-rounded-br-\[4\.5px\]` → `border-bottom-right-radius:calc(4.5 * var(--gpx))`
720. `.tw-rounded-br-\[4px\]` → `border-bottom-right-radius:calc(4 * var(--gpx))`
721. `.tw-rounded-tl-2xl` → `border-top-left-radius:calc(16 * var(--gpx))`
722. `.tw-rounded-tl-\[4\.5px\]` → `border-top-left-radius:calc(4.5 * var(--gpx))`
723. `.tw-rounded-tl-\[4px\]` → `border-top-left-radius:calc(4 * var(--gpx))`
724. `.tw-rounded-tr-2xl` → `border-top-right-radius:calc(16 * var(--gpx))`
725. `.tw-border` → `border-width:calc(1 * var(--gpx))`
726. `.tw-border-\[0\.5px\]` → `border-width:calc(0.5 * var(--gpx))`
727. `.tw-border-\[0\]` → `border-width:0`
728. `.tw-border-\[1px\]` → `border-width:calc(1 * var(--gpx))`
729. `.tw-border-\[2px\]` → `border-width:calc(2 * var(--gpx))`
730. `.tw-border-x-0` → `border-left-width:calc(0 * var(--gpx));border-right-width:calc(0 * var(--gpx))`
731. `.tw-border-b` → `border-bottom-width:calc(1 * var(--gpx))`
732. `.tw-border-t-0` → `border-top-width:calc(0 * var(--gpx))`
733. `.tw-border-solid` → `border-style:solid`
734. `.tw-border-\[\#181818\]` → `--tw-border-opacity:1;border-color:rgba(24, 24, 24, 1);border-color:rgba(24, 24, 24, var(--tw-border-opacity))`
735. `.tw-border-\[\#5D47AB\]` → `--tw-border-opacity:1;border-color:rgba(93, 71, 171, 1);border-color:rgba(93, 71, 171, var(--tw-border-opacity))`
736. `.tw-border-\[\#5F6CD0B2\]` → `border-color:rgba(95,108,208,0.69804)`
737. `.tw-border-\[\#5f6cd04d\]` → `border-color:rgba(95,108,208,0.30196)`
738. `.tw-border-\[\#635493\]` → `--tw-border-opacity:1;border-color:rgba(99, 84, 147, 1);border-color:rgba(99, 84, 147, var(--tw-border-opacity))`
739. `.tw-border-\[\#84608B\]` → `--tw-border-opacity:1;border-color:rgba(132, 96, 139, 1);border-color:rgba(132, 96, 139, var(--tw-border-opacity))`
740. `.tw-border-\[\#B29A762B\]` → `border-color:rgba(178,154,118,0.16863)`
741. `.tw-border-\[\#DBD1BF\]` → `--tw-border-opacity:1;border-color:rgba(219, 209, 191, 1);border-color:rgba(219, 209, 191, var(--tw-border-opacity))`
742. `.tw-border-\[\#E0DCD5\]` → `--tw-border-opacity:1;border-color:rgba(224, 220, 213, 1);border-color:rgba(224, 220, 213, var(--tw-border-opacity))`
743. `.tw-border-\[\#FFFFFF\]` → `--tw-border-opacity:1;border-color:rgba(255, 255, 255, 1);border-color:rgba(255, 255, 255, var(--tw-border-opacity))`
744. `.tw-border-\[rgba\(255\,255\,255\,0\.03\)\]` → `border-color:rgba(255,255,255,0.03)`
745. `.tw-border-\[rgba\(255\,255\,255\,0\.06\)\]` → `border-color:rgba(255,255,255,0.06)`
746. `.tw-border-gt-g-grey-black-4` → `border-color:rgba(0, 0, 0, 0.16)`
747. `.tw-border-gt-g-grey-black-6` → `border-color:rgba(0, 0, 0, 0.08)`
748. `.tw-border-gt-g-grey-white-10` → `border-color:rgba(255, 255, 255, 0.02)`
749. `.tw-border-gt-g-grey-white-3` → `border-color:rgba(255, 255, 255, 0.32)`
750. `.tw-border-gt-g-grey-white-4` → `border-color:rgba(255, 255, 255, 0.16)`
751. `.tw-border-gt-g-grey-white-5` → `border-color:rgba(255, 255, 255, 0.12)`
752. `.tw-border-gt-g-grey-white-6` → `border-color:rgba(255, 255, 255, 0.08)`
753. `.tw-border-gt-g-grey-white-7` → `border-color:rgba(255, 255, 255, 0.04)`
754. `.tw-border-white\/10` → `border-color:rgba(255, 255, 255, 0.1)`
755. `.tw-border-ys-l-brand-golden-3` → `--tw-border-opacity:1;border-color:rgba(211, 188, 142, 1);border-color:rgba(211, 188, 142, var(--tw-border-opacity))`
756. `.tw-border-ys-l-brand-yellow-3` → `--tw-border-opacity:1;border-color:rgba(211, 188, 142, 1);border-color:rgba(211, 188, 142, var(--tw-border-opacity))`
757. `.tw-border-ys-l-grey-white-4` → `--tw-border-opacity:1;border-color:rgba(219, 209, 191, 1);border-color:rgba(219, 209, 191, var(--tw-border-opacity))`
758. `.\!tw-bg-\[rgba\(0\2c 0\2c 0\2c 0\.20\)\]` → `background-color:rgba(0,0,0,0.20) !important`
759. `.\!tw-bg-gt-g-func-bg` → `background-color:rgba(0, 0, 0, 0.65) !important`
760. `.\!tw-bg-ys-d-grey-blue-1` → `--tw-bg-opacity:1 !important;background-color:rgba(44, 50, 61, 1) !important;background-color:rgba(44, 50, 61, var(--tw-bg-opacity)) !important`
761. `.tw-bg-\[\#00000033\]` → `background-color:rgba(0,0,0,0.2)`
762. `.tw-bg-\[\#00000040\]` → `background-color:rgba(0,0,0,0.25098)`
763. `.tw-bg-\[\#0A0B0E\]` → `--tw-bg-opacity:1;background-color:rgba(10, 11, 14, 1);background-color:rgba(10, 11, 14, var(--tw-bg-opacity))`
764. `.tw-bg-\[\#0D0F12\]` → `--tw-bg-opacity:1;background-color:rgba(13, 15, 18, 1);background-color:rgba(13, 15, 18, var(--tw-bg-opacity))`
765. `.tw-bg-\[\#140B33\]` → `--tw-bg-opacity:1;background-color:rgba(20, 11, 51, 1);background-color:rgba(20, 11, 51, var(--tw-bg-opacity))`
766. `.tw-bg-\[\#161616\]` → `--tw-bg-opacity:1;background-color:rgba(22, 22, 22, 1);background-color:rgba(22, 22, 22, var(--tw-bg-opacity))`
767. `.tw-bg-\[\#1A1A1A\]` → `--tw-bg-opacity:1;background-color:rgba(26, 26, 26, 1);background-color:rgba(26, 26, 26, var(--tw-bg-opacity))`
768. `.tw-bg-\[\#21154D80\]` → `background-color:rgba(33,21,77,0.50196)`
769. `.tw-bg-\[\#23212d\]` → `--tw-bg-opacity:1;background-color:rgba(35, 33, 45, 1);background-color:rgba(35, 33, 45, var(--tw-bg-opacity))`
770. `.tw-bg-\[\#2A1B2E\]` → `--tw-bg-opacity:1;background-color:rgba(42, 27, 46, 1);background-color:rgba(42, 27, 46, var(--tw-bg-opacity))`
771. `.tw-bg-\[\#2C323D\]` → `--tw-bg-opacity:1;background-color:rgba(44, 50, 61, 1);background-color:rgba(44, 50, 61, var(--tw-bg-opacity))`
772. `.tw-bg-\[\#2F3542\]` → `--tw-bg-opacity:1;background-color:rgba(47, 53, 66, 1);background-color:rgba(47, 53, 66, var(--tw-bg-opacity))`
773. `.tw-bg-\[\#393A7A\]` → `--tw-bg-opacity:1;background-color:rgba(57, 58, 122, 1);background-color:rgba(57, 58, 122, var(--tw-bg-opacity))`
774. `.tw-bg-\[\#575757\]` → `--tw-bg-opacity:1;background-color:rgba(87, 87, 87, 1);background-color:rgba(87, 87, 87, var(--tw-bg-opacity))`
775. `.tw-bg-\[\#5D3A64\]` → `--tw-bg-opacity:1;background-color:rgba(93, 58, 100, 1);background-color:rgba(93, 58, 100, var(--tw-bg-opacity))`
776. `.tw-bg-\[\#65446A\]` → `--tw-bg-opacity:1;background-color:rgba(101, 68, 106, 1);background-color:rgba(101, 68, 106, var(--tw-bg-opacity))`
777. `.tw-bg-\[\#95989E\]` → `--tw-bg-opacity:1;background-color:rgba(149, 152, 158, 1);background-color:rgba(149, 152, 158, var(--tw-bg-opacity))`
778. `.tw-bg-\[\#D27979\]` → `--tw-bg-opacity:1;background-color:rgba(210, 121, 121, 1);background-color:rgba(210, 121, 121, var(--tw-bg-opacity))`
779. `.tw-bg-\[\#EBE7DF\]` → `--tw-bg-opacity:1;background-color:rgba(235, 231, 223, 1);background-color:rgba(235, 231, 223, var(--tw-bg-opacity))`
780. `.tw-bg-\[\#F0EAE0\]` → `--tw-bg-opacity:1;background-color:rgba(240, 234, 224, 1);background-color:rgba(240, 234, 224, var(--tw-bg-opacity))`
781. `.tw-bg-\[\#d3bc8e\]` → `--tw-bg-opacity:1;background-color:rgba(211, 188, 142, 1);background-color:rgba(211, 188, 142, var(--tw-bg-opacity))`
782. `.tw-bg-\[\#e6dfd299\]` → `background-color:rgba(230,223,210,0.6)`
783. `.tw-bg-\[rgba\(0\,0\,0\,0\.25\)\]` → `background-color:rgba(0,0,0,0.25)`
784. `.tw-bg-\[rgba\(0\,0\,0\,0\.48\)\]` → `background-color:rgba(0,0,0,0.48)`
785. `.tw-bg-\[rgba\(111\,79\,116\,0\.72\)\]` → `background-color:rgba(111,79,116,0.72)`
786. `.tw-bg-\[rgba\(118\,185\,154\,0\.20\)\]` → `background-color:rgba(118,185,154,0.20)`
787. `.tw-bg-\[rgba\(123\,76\,131\,0\.60\)\]` → `background-color:rgba(123,76,131,0.60)`
788. `.tw-bg-\[rgba\(146\,126\,222\,0\.10\)\]` → `background-color:rgba(146,126,222,0.10)`
789. `.tw-bg-\[rgba\(146\,126\,222\,0\.16\)\]` → `background-color:rgba(146,126,222,0.16)`
790. `.tw-bg-\[rgba\(153\,120\,89\,0\.6\)\]` → `background-color:rgba(153,120,89,0.6)`
791. `.tw-bg-\[rgba\(211\,188\,142\,0\.20\)\]` → `background-color:rgba(211,188,142,0.20)`
792. `.tw-bg-\[rgba\(255\,102\,64\,0\.20\)\]` → `background-color:rgba(255,102,64,0.20)`
793. `.tw-bg-\[rgba\(255\,255\,255\,0\.1\)\]` → `background-color:rgba(255,255,255,0.1)`
794. `.tw-bg-\[rgba\(74\,83\,102\,0\.8\)\]` → `background-color:rgba(74,83,102,0.8)`
795. `.tw-bg-gt-g-func-toast` → `background-color:rgba(0, 0, 0, 0.75)`
796. `.tw-bg-gt-g-grey-black-2` → `background-color:rgba(0, 0, 0, 0.48)`
797. `.tw-bg-gt-g-grey-black-3` → `background-color:rgba(0, 0, 0, 0.32)`
798. `.tw-bg-gt-g-grey-black-4` → `background-color:rgba(0, 0, 0, 0.16)`
799. `.tw-bg-gt-g-grey-black-5` → `background-color:rgba(0, 0, 0, 0.12)`
800. `.tw-bg-gt-g-grey-black-6` → `background-color:rgba(0, 0, 0, 0.08)`
801. `.tw-bg-gt-g-grey-black-9` → `background-color:rgba(0, 0, 0, 0.64)`
802. `.tw-bg-gt-g-grey-white-1` → `background-color:rgba(255, 255, 255, 1)`
803. `.tw-bg-gt-g-grey-white-2` → `background-color:rgba(255, 255, 255, 0.48)`
804. `.tw-bg-gt-g-grey-white-4` → `background-color:rgba(255, 255, 255, 0.16)`
805. `.tw-bg-gt-g-grey-white-5` → `background-color:rgba(255, 255, 255, 0.12)`
806. `.tw-bg-gt-g-grey-white-6` → `background-color:rgba(255, 255, 255, 0.08)`
807. `.tw-bg-gt-g-grey-white-7` → `background-color:rgba(255, 255, 255, 0.04)`
808. `.tw-bg-gt-g-grey-white-9` → `background-color:rgba(255, 255, 255, 0.64)`
809. `.tw-bg-transparent` → `background-color:rgba(255, 255, 255, 0)`
810. `.tw-bg-white` → `background-color:rgba(255, 255, 255, 1)`
811. `.tw-bg-ys-d-grey-blue-1` → `--tw-bg-opacity:1;background-color:rgba(44, 50, 61, 1);background-color:rgba(44, 50, 61, var(--tw-bg-opacity))`
812. `.tw-bg-ys-d-grey-blue-2` → `--tw-bg-opacity:1;background-color:rgba(59, 66, 82, 1);background-color:rgba(59, 66, 82, var(--tw-bg-opacity))`
813. `.tw-bg-ys-d-grey-blue-3` → `--tw-bg-opacity:1;background-color:rgba(74, 83, 102, 1);background-color:rgba(74, 83, 102, var(--tw-bg-opacity))`
814. `.tw-bg-ys-d-grey-blue-6` → `--tw-bg-opacity:1;background-color:rgba(117, 124, 139, 1);background-color:rgba(117, 124, 139, var(--tw-bg-opacity))`
815. `.tw-bg-ys-g-func-green-2` → `--tw-bg-opacity:1;background-color:rgba(121, 178, 48, 1);background-color:rgba(121, 178, 48, var(--tw-bg-opacity))`
816. `.tw-bg-ys-g-func-red` → `--tw-bg-opacity:1;background-color:rgba(255, 94, 65, 1);background-color:rgba(255, 94, 65, var(--tw-bg-opacity))`
817. `.tw-bg-ys-l-brand-golden-3` → `--tw-bg-opacity:1;background-color:rgba(211, 188, 142, 1);background-color:rgba(211, 188, 142, var(--tw-bg-opacity))`
818. `.tw-bg-ys-l-brand-yellow-3` → `--tw-bg-opacity:1;background-color:rgba(211, 188, 142, 1);background-color:rgba(211, 188, 142, var(--tw-bg-opacity))`
819. `.tw-bg-ys-l-brand-yellow-5` → `--tw-bg-opacity:1;background-color:rgba(178, 154, 118, 1);background-color:rgba(178, 154, 118, var(--tw-bg-opacity))`
820. `.tw-bg-ys-l-grey-white-3` → `--tw-bg-opacity:1;background-color:rgba(236, 229, 216, 1);background-color:rgba(236, 229, 216, var(--tw-bg-opacity))`
821. `.tw-bg-ys-l-grey-white-6` → `--tw-bg-opacity:1;background-color:rgba(188, 168, 144, 1);background-color:rgba(188, 168, 144, var(--tw-bg-opacity))`
822. `.tw-bg-\[linear-gradient\(180deg\,transparent_0\%\,\#ECE5D8_100\%\)\]` → `background-image:linear-gradient(180deg,transparent 0%,#ECE5D8 100%)`
823. `.tw-bg-\[linear-gradient\(180deg\,transparent_0\%\,rgba\(236\,229\,216\,0\.7\)_50\%\,\#ECE5D8_100\%\)\]` → `background-image:linear-gradient(180deg,transparent 0%,rgba(236,229,216,0.7) 50%,#ECE5D8 100%)`
824. `.tw-bg-\[linear-gradient\(270deg\,rgba\(255\,186\,254\,0\.00\)_0\%\,rgba\(255\,186\,254\,0\.13\)_13\.09\%\,rgba\(255\,186\,254\,0\.13\)_86\.86\%\,rgba\(255\,186\,254\,0\.00\)_100\%\)\]` → `background-image:linear-gradient(270deg,rgba(255,186,254,0.00) 0%,rgba(255,186,254,0.13) 13.09%,rgba(255,186,254,0.13) 86.86%,rgba(255,186,254,0.00) 100%)`
825. `.tw-bg-\[length\:200px_52px\]` → `background-size:calc(200 * var(--gpx)) calc(52 * var(--gpx))`
826. `.tw-bg-contain` → `background-size:contain`
827. `.tw-bg-cover` → `background-size:cover`
828. `.tw-bg-left-top` → `background-position:left top`
829. `.tw-bg-right-top` → `background-position:right top`
830. `.tw-bg-no-repeat` → `background-repeat:no-repeat`
831. `.tw-p-0\.5` → `padding:calc(2 * var(--gpx))`
832. `.tw-p-2` → `padding:calc(8 * var(--gpx))`
833. `.tw-p-3` → `padding:calc(12 * var(--gpx))`
834. `.tw-p-4` → `padding:calc(16 * var(--gpx))`
835. `.tw-p-5` → `padding:calc(20 * var(--gpx))`
836. `.tw-p-6` → `padding:calc(24 * var(--gpx))`
837. `.tw-p-\[1px\]` → `padding:calc(1 * var(--gpx))`
838. `.tw-p-\[2px\]` → `padding:calc(2 * var(--gpx))`
839. `.tw-px-0` → `padding-left:calc(0 * var(--gpx));padding-right:calc(0 * var(--gpx))`
840. `.tw-px-1` → `padding-left:calc(4 * var(--gpx));padding-right:calc(4 * var(--gpx))`
841. `.tw-px-1\.5` → `padding-left:calc(6 * var(--gpx));padding-right:calc(6 * var(--gpx))`
842. `.tw-px-15` → `padding-left:calc(60 * var(--gpx));padding-right:calc(60 * var(--gpx))`
843. `.tw-px-18` → `padding-left:calc(72 * var(--gpx));padding-right:calc(72 * var(--gpx))`
844. `.tw-px-2` → `padding-left:calc(8 * var(--gpx));padding-right:calc(8 * var(--gpx))`
845. `.tw-px-3` → `padding-left:calc(12 * var(--gpx));padding-right:calc(12 * var(--gpx))`
846. `.tw-px-4` → `padding-left:calc(16 * var(--gpx));padding-right:calc(16 * var(--gpx))`
847. `.tw-px-5` → `padding-left:calc(20 * var(--gpx));padding-right:calc(20 * var(--gpx))`
848. `.tw-px-6` → `padding-left:calc(24 * var(--gpx));padding-right:calc(24 * var(--gpx))`
849. `.tw-px-9` → `padding-left:calc(36 * var(--gpx));padding-right:calc(36 * var(--gpx))`
850. `.tw-px-\[10px\]` → `padding-left:calc(10 * var(--gpx));padding-right:calc(10 * var(--gpx))`
851. `.tw-px-\[15px\]` → `padding-left:calc(15 * var(--gpx));padding-right:calc(15 * var(--gpx))`
852. `.tw-px-\[17\.5px\]` → `padding-left:calc(17.5 * var(--gpx));padding-right:calc(17.5 * var(--gpx))`
853. `.tw-px-\[180px\]` → `padding-left:calc(180 * var(--gpx));padding-right:calc(180 * var(--gpx))`
854. `.tw-px-\[22px\]` → `padding-left:calc(22 * var(--gpx));padding-right:calc(22 * var(--gpx))`
855. `.tw-px-\[30px\]` → `padding-left:calc(30 * var(--gpx));padding-right:calc(30 * var(--gpx))`
856. `.tw-px-\[3px\]` → `padding-left:calc(3 * var(--gpx));padding-right:calc(3 * var(--gpx))`
857. `.tw-px-\[49px\]` → `padding-left:calc(49 * var(--gpx));padding-right:calc(49 * var(--gpx))`
858. `.tw-px-\[56px\]` → `padding-left:calc(56 * var(--gpx));padding-right:calc(56 * var(--gpx))`
859. `.tw-px-\[7px\]` → `padding-left:calc(7 * var(--gpx));padding-right:calc(7 * var(--gpx))`
860. `.tw-px-\[8px\]` → `padding-left:calc(8 * var(--gpx));padding-right:calc(8 * var(--gpx))`
861. `.tw-px-px` → `padding-left:calc(1 * var(--gpx));padding-right:calc(1 * var(--gpx))`
862. `.tw-py-0\.5` → `padding-top:calc(2 * var(--gpx));padding-bottom:calc(2 * var(--gpx))`
863. `.tw-py-1` → `padding-top:calc(4 * var(--gpx));padding-bottom:calc(4 * var(--gpx))`
864. `.tw-py-1\.5` → `padding-top:calc(6 * var(--gpx));padding-bottom:calc(6 * var(--gpx))`
865. `.tw-py-10` → `padding-top:calc(40 * var(--gpx));padding-bottom:calc(40 * var(--gpx))`
866. `.tw-py-2` → `padding-top:calc(8 * var(--gpx));padding-bottom:calc(8 * var(--gpx))`
867. `.tw-py-2\.5` → `padding-top:calc(10 * var(--gpx));padding-bottom:calc(10 * var(--gpx))`
868. `.tw-py-3` → `padding-top:calc(12 * var(--gpx));padding-bottom:calc(12 * var(--gpx))`
869. `.tw-py-4` → `padding-top:calc(16 * var(--gpx));padding-bottom:calc(16 * var(--gpx))`
870. `.tw-py-6` → `padding-top:calc(24 * var(--gpx));padding-bottom:calc(24 * var(--gpx))`
871. `.tw-py-9` → `padding-top:calc(36 * var(--gpx));padding-bottom:calc(36 * var(--gpx))`
872. `.tw-py-\[15px\]` → `padding-top:calc(15 * var(--gpx));padding-bottom:calc(15 * var(--gpx))`
873. `.tw-py-\[1px\]` → `padding-top:calc(1 * var(--gpx));padding-bottom:calc(1 * var(--gpx))`
874. `.tw-py-\[23px\]` → `padding-top:calc(23 * var(--gpx));padding-bottom:calc(23 * var(--gpx))`
875. `.tw-py-\[3px\]` → `padding-top:calc(3 * var(--gpx));padding-bottom:calc(3 * var(--gpx))`
876. `.tw-py-\[6px\]` → `padding-top:calc(6 * var(--gpx));padding-bottom:calc(6 * var(--gpx))`
877. `.tw-py-px` → `padding-top:calc(1 * var(--gpx));padding-bottom:calc(1 * var(--gpx))`
878. `.\!tw-pb-3` → `padding-bottom:calc(12 * var(--gpx)) !important`
879. `.tw-pb-1` → `padding-bottom:calc(4 * var(--gpx))`
880. `.tw-pb-1\.5` → `padding-bottom:calc(6 * var(--gpx))`
881. `.tw-pb-11` → `padding-bottom:calc(44 * var(--gpx))`
882. `.tw-pb-12\.5` → `padding-bottom:calc(50 * var(--gpx))`
883. `.tw-pb-15` → `padding-bottom:calc(60 * var(--gpx))`
884. `.tw-pb-16` → `padding-bottom:calc(64 * var(--gpx))`
885. `.tw-pb-2` → `padding-bottom:calc(8 * var(--gpx))`
886. `.tw-pb-2\.5` → `padding-bottom:calc(10 * var(--gpx))`
887. `.tw-pb-3` → `padding-bottom:calc(12 * var(--gpx))`
888. `.tw-pb-4` → `padding-bottom:calc(16 * var(--gpx))`
889. `.tw-pb-40` → `padding-bottom:calc(160 * var(--gpx))`
890. `.tw-pb-5` → `padding-bottom:calc(20 * var(--gpx))`
891. `.tw-pb-6` → `padding-bottom:calc(24 * var(--gpx))`
892. `.tw-pb-8` → `padding-bottom:calc(32 * var(--gpx))`
893. `.tw-pb-\[102px\]` → `padding-bottom:calc(102 * var(--gpx))`
894. `.tw-pb-\[10px\]` → `padding-bottom:calc(10 * var(--gpx))`
895. `.tw-pb-\[138px\]` → `padding-bottom:calc(138 * var(--gpx))`
896. `.tw-pb-\[14px\]` → `padding-bottom:calc(14 * var(--gpx))`
897. `.tw-pb-\[15px\]` → `padding-bottom:calc(15 * var(--gpx))`
898. `.tw-pb-\[18px\]` → `padding-bottom:calc(18 * var(--gpx))`
899. `.tw-pb-\[6px\]` → `padding-bottom:calc(6 * var(--gpx))`
900. `.tw-pl-1` → `padding-left:calc(4 * var(--gpx))`
901. `.tw-pl-12` → `padding-left:calc(48 * var(--gpx))`
902. `.tw-pl-2` → `padding-left:calc(8 * var(--gpx))`
903. `.tw-pl-2\.5` → `padding-left:calc(10 * var(--gpx))`
904. `.tw-pl-3` → `padding-left:calc(12 * var(--gpx))`
905. `.tw-pl-4` → `padding-left:calc(16 * var(--gpx))`
906. `.tw-pl-5` → `padding-left:calc(20 * var(--gpx))`
907. `.tw-pl-9` → `padding-left:calc(36 * var(--gpx))`
908. `.tw-pl-\[15px\]` → `padding-left:calc(15 * var(--gpx))`
909. `.tw-pl-\[1px\]` → `padding-left:calc(1 * var(--gpx))`
910. `.tw-pl-\[46px\]` → `padding-left:calc(46 * var(--gpx))`
911. `.tw-pl-\[54px\]` → `padding-left:calc(54 * var(--gpx))`
912. `.tw-pr-1\.5` → `padding-right:calc(6 * var(--gpx))`
913. `.tw-pr-2` → `padding-right:calc(8 * var(--gpx))`
914. `.tw-pr-3` → `padding-right:calc(12 * var(--gpx))`
915. `.tw-pr-6` → `padding-right:calc(24 * var(--gpx))`
916. `.tw-pr-\[22px\]` → `padding-right:calc(22 * var(--gpx))`
917. `.tw-pr-\[27px\]` → `padding-right:calc(27 * var(--gpx))`
918. `.tw-pr-\[34px\]` → `padding-right:calc(34 * var(--gpx))`
919. `.tw-pr-\[50px\]` → `padding-right:calc(50 * var(--gpx))`
920. `.tw-pr-\[6px\]` → `padding-right:calc(6 * var(--gpx))`
921. `.tw-pr-\[8px\]` → `padding-right:calc(8 * var(--gpx))`
922. `.tw-pr-\[9px\]` → `padding-right:calc(9 * var(--gpx))`
923. `.tw-pt-1` → `padding-top:calc(4 * var(--gpx))`
924. `.tw-pt-10` → `padding-top:calc(40 * var(--gpx))`
925. `.tw-pt-14` → `padding-top:calc(56 * var(--gpx))`
926. `.tw-pt-2` → `padding-top:calc(8 * var(--gpx))`
927. `.tw-pt-2\.5` → `padding-top:calc(10 * var(--gpx))`
928. `.tw-pt-3` → `padding-top:calc(12 * var(--gpx))`
929. `.tw-pt-4` → `padding-top:calc(16 * var(--gpx))`
930. `.tw-pt-5` → `padding-top:calc(20 * var(--gpx))`
931. `.tw-pt-6` → `padding-top:calc(24 * var(--gpx))`
932. `.tw-pt-8` → `padding-top:calc(32 * var(--gpx))`
933. `.tw-pt-9` → `padding-top:calc(36 * var(--gpx))`
934. `.tw-pt-\[178px\]` → `padding-top:calc(178 * var(--gpx))`
935. `.tw-pt-\[1px\]` → `padding-top:calc(1 * var(--gpx))`
936. `.\!tw-text-left` → `text-align:left !important`
937. `.tw-text-center` → `text-align:center`
938. `.tw-text-right` → `text-align:right`
939. `.tw-font-genshinfont` → `font-family:HYWenHei, ysfont`
940. `.\!tw-text-\[11px\]` → `font-size:calc(11 * var(--gpx)) !important`
941. `.\!tw-text-\[12px\]` → `font-size:calc(12 * var(--gpx)) !important`
942. `.\!tw-text-\[14px\]` → `font-size:calc(14 * var(--gpx)) !important`
943. `.\!tw-text-\[15px\]` → `font-size:calc(15 * var(--gpx)) !important`
944. `.\!tw-text-\[8px\]` → `font-size:calc(8 * var(--gpx)) !important`
945. `.\!tw-text-sm-pc` → `font-size:calc(14 * var(--gpx)) !important;line-height:calc(20 * var(--gpx)) !important`
946. `.tw-text-2xl-pc` → `font-size:calc(24 * var(--gpx));line-height:calc(32 * var(--gpx))`
947. `.tw-text-\[0\]` → `font-size:0`
948. `.tw-text-\[0px\]` → `font-size:calc(0 * var(--gpx))`
949. `.tw-text-\[10px\]` → `font-size:calc(10 * var(--gpx))`
950. `.tw-text-\[11px\]` → `font-size:calc(11 * var(--gpx))`
951. `.tw-text-\[12px\]` → `font-size:calc(12 * var(--gpx))`
952. `.tw-text-\[13px\]` → `font-size:calc(13 * var(--gpx))`
953. `.tw-text-\[14px\]` → `font-size:calc(14 * var(--gpx))`
954. `.tw-text-\[16px\]` → `font-size:calc(16 * var(--gpx))`
955. `.tw-text-\[18px\]` → `font-size:calc(18 * var(--gpx))`
956. `.tw-text-\[19px\]` → `font-size:calc(19 * var(--gpx))`
957. `.tw-text-\[20px\]` → `font-size:calc(20 * var(--gpx))`
958. `.tw-text-\[21px\]` → `font-size:calc(21 * var(--gpx))`
959. `.tw-text-\[22px\]` → `font-size:calc(22 * var(--gpx))`
960. `.tw-text-\[24px\]` → `font-size:calc(24 * var(--gpx))`
961. `.tw-text-\[30px\]` → `font-size:calc(30 * var(--gpx))`
962. `.tw-text-\[48px\]` → `font-size:calc(48 * var(--gpx))`
963. `.tw-text-\[6px\]` → `font-size:calc(6 * var(--gpx))`
964. `.tw-text-\[7px\]` → `font-size:calc(7 * var(--gpx))`
965. `.tw-text-\[8px\]` → `font-size:calc(8 * var(--gpx))`
966. `.tw-text-\[9px\]` → `font-size:calc(9 * var(--gpx))`
967. `.tw-text-base-m` → `font-size:calc(16 * var(--gpx));line-height:calc(20 * var(--gpx))`
968. `.tw-text-base-pc` → `font-size:calc(16 * var(--gpx));line-height:calc(24 * var(--gpx))`
969. `.tw-text-lg-m` → `font-size:calc(18 * var(--gpx));line-height:calc(24 * var(--gpx))`
970. `.tw-text-lg-pc` → `font-size:calc(18 * var(--gpx));line-height:calc(26 * var(--gpx))`
971. `.tw-text-sm` → `font-size:calc(14 * var(--gpx));line-height:calc(20 * var(--gpx))`
972. `.tw-text-sm-m` → `font-size:calc(14 * var(--gpx));line-height:calc(18 * var(--gpx))`
973. `.tw-text-sm-pc` → `font-size:calc(14 * var(--gpx));line-height:calc(20 * var(--gpx))`
974. `.tw-text-xl-pc` → `font-size:calc(22 * var(--gpx));line-height:calc(28 * var(--gpx))`
975. `.tw-text-xs` → `font-size:calc(12 * var(--gpx));line-height:calc(16 * var(--gpx))`
976. `.tw-text-xs-m` → `font-size:calc(12 * var(--gpx));line-height:calc(16 * var(--gpx))`
977. `.tw-text-xs-pc` → `font-size:calc(12 * var(--gpx));line-height:calc(16 * var(--gpx))`
978. `.tw-text-xxs-m` → `font-size:calc(10 * var(--gpx));line-height:calc(12 * var(--gpx))`
979. `.\!tw-font-medium` → `font-weight:500 !important`
980. `.tw-font-\[13px\]` → `font-weight:calc(13 * var(--gpx))`
981. `.tw-font-\[24px\]` → `font-weight:calc(24 * var(--gpx))`
982. `.tw-font-bold` → `font-weight:700`
983. `.tw-font-medium` → `font-weight:500`
984. `.tw-font-normal` → `font-weight:400`
985. `.tw-font-semibold` → `font-weight:600`
986. `.tw-leading-3` → `line-height:calc(12 * var(--gpx))`
987. `.tw-leading-4` → `line-height:calc(16 * var(--gpx))`
988. `.tw-leading-4\.5` → `line-height:calc(18 * var(--gpx))`
989. `.tw-leading-\[10px\]` → `line-height:calc(10 * var(--gpx))`
990. `.tw-leading-\[12px\]` → `line-height:calc(12 * var(--gpx))`
991. `.tw-leading-\[14px\]` → `line-height:calc(14 * var(--gpx))`
992. `.tw-leading-\[15px\]` → `line-height:calc(15 * var(--gpx))`
993. `.tw-leading-\[16px\]` → `line-height:calc(16 * var(--gpx))`
994. `.tw-leading-\[18px\]` → `line-height:calc(18 * var(--gpx))`
995. `.tw-leading-\[19px\]` → `line-height:calc(19 * var(--gpx))`
996. `.tw-leading-\[20px\]` → `line-height:calc(20 * var(--gpx))`
997. `.tw-leading-\[24px\]` → `line-height:calc(24 * var(--gpx))`
998. `.tw-leading-\[26px\]` → `line-height:calc(26 * var(--gpx))`
999. `.tw-leading-\[7px\]` → `line-height:calc(7 * var(--gpx))`
1000. `.tw-leading-\[8px\]` → `line-height:calc(8 * var(--gpx))`
1001. `.tw-leading-\[9px\]` → `line-height:calc(9 * var(--gpx))`
1002. `.\!tw-text-gt-g-grey-white-5` → `color:rgba(255, 255, 255, 0.12) !important`
1003. `.\!tw-text-gt-l-text-white-2` → `color:rgba(255, 255, 255, 0.90) !important`
1004. `.\!tw-text-gt-l-text-white-4` → `color:rgba(255, 255, 255, 0.45) !important`
1005. `.tw-text-\[\#2C323D\]` → `--tw-text-opacity:1;color:rgba(44, 50, 61, 1);color:rgba(44, 50, 61, var(--tw-text-opacity))`
1006. `.tw-text-\[\#33347F\]` → `--tw-text-opacity:1;color:rgba(51, 52, 127, 1);color:rgba(51, 52, 127, var(--tw-text-opacity))`
1007. `.tw-text-\[\#4A5366\]` → `--tw-text-opacity:1;color:rgba(74, 83, 102, 1);color:rgba(74, 83, 102, var(--tw-text-opacity))`
1008. `.tw-text-\[\#4dffff\]` → `--tw-text-opacity:1;color:rgba(77, 255, 255, 1);color:rgba(77, 255, 255, var(--tw-text-opacity))`
1009. `.tw-text-\[\#6A5443\]` → `--tw-text-opacity:1;color:rgba(106, 84, 67, 1);color:rgba(106, 84, 67, var(--tw-text-opacity))`
1010. `.tw-text-\[\#7A7A7A\]` → `--tw-text-opacity:1;color:rgba(122, 122, 122, 1);color:rgba(122, 122, 122, var(--tw-text-opacity))`
1011. `.tw-text-\[\#7f858a\]` → `--tw-text-opacity:1;color:rgba(127, 133, 138, 1);color:rgba(127, 133, 138, var(--tw-text-opacity))`
1012. `.tw-text-\[\#88816F\]` → `--tw-text-opacity:1;color:rgba(136, 129, 111, 1);color:rgba(136, 129, 111, var(--tw-text-opacity))`
1013. `.tw-text-\[\#997754\]` → `--tw-text-opacity:1;color:rgba(153, 119, 84, 1);color:rgba(153, 119, 84, var(--tw-text-opacity))`
1014. `.tw-text-\[\#9B9894\]` → `--tw-text-opacity:1;color:rgba(155, 152, 148, 1);color:rgba(155, 152, 148, var(--tw-text-opacity))`
1015. `.tw-text-\[\#D3BC8E\]` → `--tw-text-opacity:1;color:rgba(211, 188, 142, 1);color:rgba(211, 188, 142, var(--tw-text-opacity))`
1016. `.tw-text-\[\#DDCA9F\]` → `--tw-text-opacity:1;color:rgba(221, 202, 159, 1);color:rgba(221, 202, 159, var(--tw-text-opacity))`
1017. `.tw-text-\[\#E7D8B34D\]` → `color:rgba(231,216,179,0.30196)`
1018. `.tw-text-\[\#E7D8B380\]` → `color:rgba(231,216,179,0.50196)`
1019. `.tw-text-\[\#E7D8B3\]` → `--tw-text-opacity:1;color:rgba(231, 216, 179, 1);color:rgba(231, 216, 179, var(--tw-text-opacity))`
1020. `.tw-text-\[\#EBE5D7\]` → `--tw-text-opacity:1;color:rgba(235, 229, 215, 1);color:rgba(235, 229, 215, var(--tw-text-opacity))`
1021. `.tw-text-\[\#F4EFE8\]` → `--tw-text-opacity:1;color:rgba(244, 239, 232, 1);color:rgba(244, 239, 232, var(--tw-text-opacity))`
1022. `.tw-text-\[\#FFF8EA\]` → `--tw-text-opacity:1;color:rgba(255, 248, 234, 1);color:rgba(255, 248, 234, var(--tw-text-opacity))`
1023. `.tw-text-\[\#FFF9E6\]` → `--tw-text-opacity:1;color:rgba(255, 249, 230, 1);color:rgba(255, 249, 230, var(--tw-text-opacity))`
1024. `.tw-text-\[\#d3bc8e\]` → `--tw-text-opacity:1;color:rgba(211, 188, 142, 1);color:rgba(211, 188, 142, var(--tw-text-opacity))`
1025. `.tw-text-\[\#ddca9f\]` → `--tw-text-opacity:1;color:rgba(221, 202, 159, 1);color:rgba(221, 202, 159, var(--tw-text-opacity))`
1026. `.tw-text-\[\#e5d7bb\]` → `--tw-text-opacity:1;color:rgba(229, 215, 187, 1);color:rgba(229, 215, 187, var(--tw-text-opacity))`
1027. `.tw-text-\[\#fff\]` → `--tw-text-opacity:1;color:rgba(255, 255, 255, 1);color:rgba(255, 255, 255, var(--tw-text-opacity))`
1028. `.tw-text-\[rgba\(255\,255\,255\,0\.90\)\]` → `color:rgba(255,255,255,0.90)`
1029. `.tw-text-gt-d-text-black-3` → `color:rgba(0, 0, 0, 0.65)`
1030. `.tw-text-gt-g-grey-white-1` → `color:rgba(255, 255, 255, 1)`
1031. `.tw-text-gt-g-grey-white-2` → `color:rgba(255, 255, 255, 0.48)`
1032. `.tw-text-gt-g-grey-white-4` → `color:rgba(255, 255, 255, 0.16)`
1033. `.tw-text-gt-g-grey-white-5` → `color:rgba(255, 255, 255, 0.12)`
1034. `.tw-text-gt-g-grey-white-8` → `color:rgba(255, 255, 255, 0.8)`
1035. `.tw-text-gt-g-grey-white-9` → `color:rgba(255, 255, 255, 0.64)`
1036. `.tw-text-gt-l-text-tertiary` → `color:rgba(255, 255, 255, 0.45)`
1037. `.tw-text-gt-l-text-white-1` → `--tw-text-opacity:1;color:rgba(255, 255, 255, 1);color:rgba(255, 255, 255, var(--tw-text-opacity))`
1038. `.tw-text-gt-l-text-white-2` → `color:rgba(255, 255, 255, 0.90)`
1039. `.tw-text-gt-l-text-white-3` → `color:rgba(255, 255, 255, 0.65)`
1040. `.tw-text-gt-l-text-white-4` → `color:rgba(255, 255, 255, 0.45)`
1041. `.tw-text-gt-l-text-white-5` → `color:rgba(255, 255, 255, 0.35)`
1042. `.tw-text-white` → `color:rgba(255, 255, 255, 1)`
1043. `.tw-text-ys-b-brand-brown-3` → `--tw-text-opacity:1;color:rgba(178, 134, 89, 1);color:rgba(178, 134, 89, var(--tw-text-opacity))`
1044. `.tw-text-ys-d-grey-blue-2` → `--tw-text-opacity:1;color:rgba(59, 66, 82, 1);color:rgba(59, 66, 82, var(--tw-text-opacity))`
1045. `.tw-text-ys-d-text-yellow-4` → `--tw-text-opacity:1;color:rgba(129, 100, 71, 1);color:rgba(129, 100, 71, var(--tw-text-opacity))`
1046. `.tw-text-ys-g-func-cyan` → `--tw-text-opacity:1;color:rgba(77, 255, 255, 1);color:rgba(77, 255, 255, var(--tw-text-opacity))`
1047. `.tw-text-ys-g-func-green` → `--tw-text-opacity:1;color:rgba(155, 229, 61, 1);color:rgba(155, 229, 61, var(--tw-text-opacity))`
1048. `.tw-text-ys-g-func-green-2` → `--tw-text-opacity:1;color:rgba(121, 178, 48, 1);color:rgba(121, 178, 48, var(--tw-text-opacity))`
1049. `.tw-text-ys-g-func-orange` → `--tw-text-opacity:1;color:rgba(244, 143, 1, 1);color:rgba(244, 143, 1, var(--tw-text-opacity))`
1050. `.tw-text-ys-g-func-yellow` → `--tw-text-opacity:1;color:rgba(255, 204, 51, 1);color:rgba(255, 204, 51, var(--tw-text-opacity))`
1051. `.tw-text-ys-l-brand-golden-3` → `--tw-text-opacity:1;color:rgba(211, 188, 142, 1);color:rgba(211, 188, 142, var(--tw-text-opacity))`
1052. `.tw-text-ys-l-brand-yellow-3` → `--tw-text-opacity:1;color:rgba(211, 188, 142, 1);color:rgba(211, 188, 142, var(--tw-text-opacity))`
1053. `.tw-text-ys-l-grey-white-1` → `--tw-text-opacity:1;color:rgba(244, 239, 232, 1);color:rgba(244, 239, 232, var(--tw-text-opacity))`
1054. `.tw-text-ys-l-grey-white-2` → `--tw-text-opacity:1;color:rgba(240, 234, 224, 1);color:rgba(240, 234, 224, var(--tw-text-opacity))`
1055. `.tw-text-ys-l-grey-white-3` → `--tw-text-opacity:1;color:rgba(236, 229, 216, 1);color:rgba(236, 229, 216, var(--tw-text-opacity))`
1056. `.tw-text-ys-l-grey-white-5` → `--tw-text-opacity:1;color:rgba(203, 189, 167, 1);color:rgba(203, 189, 167, var(--tw-text-opacity))`
1057. `.tw-text-ys-l-grey-white-6` → `--tw-text-opacity:1;color:rgba(188, 168, 144, 1);color:rgba(188, 168, 144, var(--tw-text-opacity))`
1058. `.tw-text-ys-l-text-yellow-3` → `--tw-text-opacity:1;color:rgba(211, 188, 142, 1);color:rgba(211, 188, 142, var(--tw-text-opacity))`
1059. `.tw-opacity-0` → `opacity:0`
1060. `.tw-opacity-50` → `opacity:0.5`
1061. `.tw-opacity-80` → `opacity:0.8`
1062. `.tw-opacity-\[0\.15\]` → `opacity:0.15`
1063. `.tw-opacity-\[0\.2\]` → `opacity:0.2`
1064. `.tw-opacity-\[0\.4\]` → `opacity:0.4`
1065. `.tw-opacity-\[0\.5\]` → `opacity:0.5`
1066. `.tw-opacity-\[0\.65\]` → `opacity:0.65`
1067. `.tw-opacity-\[0\.8\]` → `opacity:0.8`
1068. `.tw-backdrop-blur` → `--tw-backdrop-blur:blur(calc(8 * var(--gpx)));-webkit-backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia);backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia)`
1069. `.tw-backdrop-blur-\[2px\]` → `--tw-backdrop-blur:blur(calc(2 * var(--gpx)));-webkit-backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia);backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia)`
1070. `.tw-backdrop-blur-\[30px\]` → `--tw-backdrop-blur:blur(calc(30 * var(--gpx)));-webkit-backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia);backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia)`
1071. `.tw-backdrop-blur-\[32px\]` → `--tw-backdrop-blur:blur(calc(32 * var(--gpx)));-webkit-backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia);backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia)`
1072. `.tw-backdrop-blur-\[5px\]` → `--tw-backdrop-blur:blur(calc(5 * var(--gpx)));-webkit-backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia);backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia)`
1073. `.tw-backdrop-blur-\[8px\]` → `--tw-backdrop-blur:blur(calc(8 * var(--gpx)));-webkit-backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia);backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia)`
1074. `.tw-backdrop-blur-lg` → `--tw-backdrop-blur:blur(calc(16 * var(--gpx)));-webkit-backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia);backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia)`
1075. `.tw-backdrop-blur-sm` → `--tw-backdrop-blur:blur(calc(4 * var(--gpx)));-webkit-backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia);backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia)`
1076. `.tw-transition-all` → `transition-property:all;transition-timing-function:cubic-bezier(0.4, 0, 0.2, 1);transition-duration:150ms`
1077. `.tw-transition-colors` → `transition-property:color, background-color, border-color, fill, stroke, -webkit-text-decoration-color;transition-property:color, background-color, border-color, text-decoration-color, fill, stroke;transition-property:color, background-color, border-color, text-decoration-color, fill, stroke, -webkit-text-decoration-color;transition-timing-function:cubic-bezier(0.4, 0, 0.2, 1);transition-duration:150ms`
1078. `.tw-duration-300` → `transition-duration:300ms`
1079. `.tw-duration-\[400ms\]` → `transition-duration:400ms`
1080. `.\!tw-flex-center` → `display:flex !important;align-items:center !important;justify-content:center !important`
1081. `.tw-flex-center` → `display:flex;align-items:center;justify-content:center`
1082. `.tw-flex-items-center` → `display:flex;align-items:center`
1083. `.tw-flex-justify-center` → `display:flex;justify-content:center`
1084. `.tw-scrollbar-hide` → `scrollbar-width:none;`
1085. `.tw-scrollbar-hide::-webkit-scrollbar` → `display:none`
1086. `.tw-absolute-center` → `position:absolute;left:50%;top:50%;-webkit-transform:translate(-50%,-50%);transform:translate(-50%,-50%)`
1087. `.tw-absolute-x-center` → `position:absolute;left:50%;-webkit-transform:translateX(-50%);transform:translateX(-50%)`
1088. `.\!tw-absolute-y-center` → `position:absolute !important;top:50% !important;-webkit-transform:translateY(-50%) !important;transform:translateY(-50%) !important`
1089. `.tw-absolute-y-center` → `position:absolute;top:50%;-webkit-transform:translateY(-50%);transform:translateY(-50%)`
1090. `html,body` → `font-size:calc(14 * var(--gpx));color:#333`
1091. `*` → `box-sizing:border-box;margin:0`
1092. `ul` → `list-style:none;padding:0`
1093. `.van-popup-lang` → `font-size:calc(12 * var(--gpx));height:calc(30 * var(--gpx));line-height:calc(30 * var(--gpx))`
1094. `.van-popup-lang.active` → `color:#d3bc8d`
1095. `.mhy-picker-modal` → `position:fixed;top:0;left:0;right:0;bottom:0;background:#000;opacity:.1;z-index:20;display:none`
1096. `.pswp` → `z-index:3000`
1097. `.mhy-game-role-message-box-mask.mhy-game-role-selector-mask` → `z-index:3000`
1098. `.skill-popover .popper[x-placement^=top] .popper__arrow` → `border-top-color:#f6f3ed`
1099. `.role-selector-sea-dropdown-menu .dropdown-menu__list` → `overflow-y:auto !important`
1100. `.custom-preview-mask-mihoyo-common-mask:has(.me-share-preview)` → `display:none !important`
1101. `.custom-preview-mask-mihoyo-common-mask` → `display:none !important`
1102. `.custom-preview-mask-mihoyo-common-mask .me-share-preview` → `display:none !important`
1103. `.mhy-account-flow` → `z-index:2000 !important`
1104. `.van-toast__text` → `word-break:break-word`
1105. `html[gt-platform=pc] body::-webkit-scrollbar` → `background-color:rgba(0,0,0,0);width:calc(5 * var(--gpx));height:calc(5 * var(--gpx))`
1106. `html[gt-platform=pc] body::-webkit-scrollbar-corner` → `background-color:rgba(0,0,0,0)`
1107. `html[gt-platform=pc] body::-webkit-scrollbar-thumb` → `border-radius:calc(2 * var(--gpx));background:#4c4c4c`
1108. `.tooltip` → `display:block !important;z-index:10000`
1109. `.tooltip .tooltip-inner` → `background-color:rgba(0, 0, 0, 0.75);color:rgba(255, 255, 255, 0.90);border-radius:calc(8 * var(--gpx));padding-left:calc(16 * var(--gpx));padding-right:calc(16 * var(--gpx));padding-top:calc(12 * var(--gpx));padding-bottom:calc(12 * var(--gpx));font-size:calc(14 * var(--gpx));line-height:calc(20 * var(--gpx))`
1110. `.tooltip .tooltip-arrow` → `position:absolute;z-index:1;margin:calc(8 * var(--gpx));height:calc(0 * var(--gpx));width:calc(0 * var(--gpx));border-style:solid;border-color:rgba(0, 0, 0, 0.75)`
1111. `.tooltip[x-placement^=top]` → `margin-bottom:calc(8 * var(--gpx))`
1112. `.tooltip[x-placement^=top] .tooltip-arrow` → `border-width:calc(8 * var(--gpx)) calc(6 * var(--gpx)) 0 calc(6 * var(--gpx));border-left-color:rgba(0,0,0,0) !important;border-right-color:rgba(0,0,0,0) !important;border-bottom-color:rgba(0,0,0,0) !important;bottom:calc(-8 * var(--gpx));left:calc(50% - (6 * var(--gpx)));left:calc(50% - calc(6 * var(--gpx)));margin-top:0;margin-bottom:0`
1113. `.tooltip[x-placement^=bottom]` → `margin-top:calc(8 * var(--gpx))`
1114. `.tooltip[x-placement^=bottom] .tooltip-arrow` → `border-width:0 calc(6 * var(--gpx)) calc(8 * var(--gpx)) calc(6 * var(--gpx));border-left-color:rgba(0,0,0,0) !important;border-right-color:rgba(0,0,0,0) !important;border-top-color:rgba(0,0,0,0) !important;top:calc(-8 * var(--gpx));left:calc(50% - (6 * var(--gpx)));left:calc(50% - calc(6 * var(--gpx)));margin-top:0;margin-bottom:0`
1115. `.tooltip[x-placement^=right]` → `margin-left:calc(8 * var(--gpx))`
1116. `.tooltip[x-placement^=right] .tooltip-arrow` → `border-width:calc(6 * var(--gpx)) calc(8 * var(--gpx)) calc(6 * var(--gpx)) 0;border-left-color:rgba(0,0,0,0) !important;border-top-color:rgba(0,0,0,0) !important;border-bottom-color:rgba(0,0,0,0) !important;left:calc(-8 * var(--gpx));top:calc(50% - (6 * var(--gpx)));top:calc(50% - calc(6 * var(--gpx)));margin-left:0;margin-right:0`
1117. `.tooltip[x-placement^=left]` → `margin-right:calc(8 * var(--gpx))`
1118. `.tooltip[x-placement^=left] .tooltip-arrow` → `border-width:calc(6 * var(--gpx)) 0 calc(6 * var(--gpx)) calc(8 * var(--gpx));border-top-color:rgba(0,0,0,0) !important;border-right-color:rgba(0,0,0,0) !important;border-bottom-color:rgba(0,0,0,0) !important;right:calc(-8 * var(--gpx));top:calc(50% - (6 * var(--gpx)));top:calc(50% - calc(6 * var(--gpx)));margin-left:0;margin-right:0`
1119. `.tooltip.popover[x-placement^=top]` → `margin-bottom:0`
1120. `.tooltip.popover[x-placement^=top] .popover-inner` → `margin-bottom:calc(8 * var(--gpx))`
1121. `.tooltip.popover[x-placement^=bottom]` → `margin-top:0`
1122. `.tooltip.popover[x-placement^=bottom] .popover-inner` → `margin-top:calc(8 * var(--gpx))`
1123. `.tooltip.popover[x-placement^=left]` → `margin-right:0`
1124. `.tooltip.popover[x-placement^=left] .popover-inner` → `margin-right:calc(8 * var(--gpx))`
1125. `.tooltip.popover[x-placement^=right]` → `margin-left:0`
1126. `.tooltip.popover[x-placement^=right] .popover-inner` → `margin-left:calc(8 * var(--gpx))`
1127. `.tooltip.popover .popover-inner` → `--tw-bg-opacity:1;background-color:rgba(44, 50, 61, 1);background-color:rgba(44, 50, 61, var(--tw-bg-opacity));border-radius:calc(16 * var(--gpx));padding:calc(12 * var(--gpx));--tw-backdrop-blur:blur(calc(5 * var(--gpx)));-webkit-backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia);backdrop-filter:var(--tw-backdrop-blur) var(--tw-backdrop-brightness) var(--tw-backdrop-contrast) var(--tw-backdrop-grayscale) var(--tw-backdrop-hue-rotate) var(--tw-backdrop-invert) var(--tw-backdrop-opacity) var(--tw-backdrop-saturate) var(--tw-backdrop-sepia)`
1128. `.tooltip.popover .popover-arrow` → `display:none`
1129. `.tooltip[aria-hidden=true]` → `visibility:hidden;opacity:0;transition:opacity .15s, visibility .15s`
1130. `.tooltip[aria-hidden=false]` → `visibility:visible;opacity:1;transition:opacity .15s`
1131. `.global-share-container` → `position:absolute;z-index:-9999;left:calc(3000 * var(--gpx));top:0;box-sizing:border-box;-webkit-transform:translate3d(0, 0, calc(10 * var(--gpx)));transform:translate3d(0, 0, calc(10 * var(--gpx)))`
1132. `html[gt-platform=mob] .global-share-container` → `width:calc(375 * var(--gpx))`
1133. `.last\:tw-mb-0:last-child` → `margin-bottom:calc(0 * var(--gpx))`
1134. `.hover\:tw-scale-\[1\.1\]:hover` → `--tw-scale-x:1.1;--tw-scale-y:1.1;-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(1.1) scaleY(1.1);transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(1.1) scaleY(1.1);-webkit-transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y));transform:translate(var(--tw-translate-x), var(--tw-translate-y)) rotate(var(--tw-rotate)) skewX(var(--tw-skew-x)) skewY(var(--tw-skew-y)) scaleX(var(--tw-scale-x)) scaleY(var(--tw-scale-y))`
1135. `.hover\:tw-border-ys-l-brand-golden-3:hover` → `--tw-border-opacity:1;border-color:rgba(211, 188, 142, 1);border-color:rgba(211, 188, 142, var(--tw-border-opacity))`
1136. `.hover\:tw-border-ys-l-brand-yellow-2:hover` → `--tw-border-opacity:1;border-color:rgba(220, 201, 165, 1);border-color:rgba(220, 201, 165, var(--tw-border-opacity))`
1137. `.hover\:tw-border-ys-l-grey-white-3:hover` → `--tw-border-opacity:1;border-color:rgba(236, 229, 216, 1);border-color:rgba(236, 229, 216, var(--tw-border-opacity))`
1138. `.hover\:tw-bg-gt-g-grey-white-6:hover` → `background-color:rgba(255, 255, 255, 0.08)`
1139. `.hover\:tw-text-ys-l-brand-golden-3:hover` → `--tw-text-opacity:1;color:rgba(211, 188, 142, 1);color:rgba(211, 188, 142, var(--tw-text-opacity))`
1140. `.tw-group:hover .group-hover\:tw-text-ys-l-brand-golden-3` → `--tw-text-opacity:1;color:rgba(211, 188, 142, 1);color:rgba(211, 188, 142, var(--tw-text-opacity))`
1141. `.tw-group:hover .group-hover\:tw-text-ys-l-brand-yellow-2` → `--tw-text-opacity:1;color:rgba(220, 201, 165, 1);color:rgba(220, 201, 165, var(--tw-text-opacity))`
1142. `.tw-group:hover .group-hover\:tw-text-ys-l-text-yellow-2` → `--tw-text-opacity:1;color:rgba(221, 202, 159, 1);color:rgba(221, 202, 159, var(--tw-text-opacity))`
1143. `.tw-group:hover .group-hover\:tw-opacity-100` → `opacity:1`
1144. `.\[\&\>div\]\:tw-flex-1>div` → `flex:1 1 0%`
1145. `html,body` → `height:100%;background-color:#2a2a2a`

## Interpretation and known limits

- This pass inventories source declarations, HTML classes/IDs, inline styles, and referenced image assets. It does not execute the page, resolve the CSS cascade, calculate computed styles, or evaluate Vue/runtime-generated markup.
- Some classes are generated/hash-like and may depend on framework/runtime code not present here.
- CSS selectors may target components absent from the four HTML fragments; the complete CSS inventory intentionally retains them as source evidence.
- HTML class names without matching supplied CSS rules are structural evidence only. Their exact styling is unresolved.
- Inline percentages, viewport units, rem values, dynamic colors, gradients, and region artwork remain contextual. They are not normalized into the global token set.
- Calculator character/weapon selector grids and material detail screens are not present in the supplied calculator fragment. No exact sub-screen layout can be extracted from these five files.
- CSS asset paths are references only. The actual binary assets are not included in this extraction unless separately present in the repository.

## Relationship to the token layer

- Curated reusable token reference: `docs/HOYOLAB_DESIGN_TOKENS.md`
- Kotlin token implementation: `app/src/main/java/com/irminsul/codex/presentation/HoYoLabTokens.kt`
- This inventory: exhaustive parsed source evidence for subsequent review and targeted token additions.

