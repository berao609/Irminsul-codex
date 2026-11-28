# Native UI Standard - Borderless Tonal Surfaces

**Status:** Approved project-wide standard for new and modified native Compose UI.

Use the Account Overview surface treatment as the baseline and the four-page Battle Chronicles reference supplied on 2026-11-28 for calculator flow structure. This is a native Android adaptation, not a pixel-for-pixel reproduction of HoYoLAB artwork.

## Hard rules

1. **No default borders.** Do not add `.border(...)`, `BorderStroke(...)`, or `OutlinedCard` to ordinary containers. Remove these from modified files unless a documented functional distinction cannot be expressed with tonal contrast.
2. **No card-in-card nesting.** Surface nesting depth is at most one. Do not wrap a group of cards in another card.
3. **Tonal separation first.** Use Material 3 `surface`, `surfaceContainerLow`, and `surfaceContainer` with no border. Separate sections with 16-24 dp whitespace and clear typography.
4. **Flat dense lists.** Render repeated data as flat rows. Use clean spacing or a 0.5 dp divider tinted with `outlineVariant.copy(alpha = 0.3f)`; do not give every row its own card.
5. **No keyboard-triggering app inputs.** Do not use Compose `TextField` / `BasicTextField`, Android `EditText`, or editable controls that invoke the soft keyboard for app-managed values. Use icon chips, grids, steppers, and concealed wheel pickers. The HoYoLAB sign-in WebView is the sole intentional exception because remote authentication requires credential entry.
6. **Concealed wheel pickers.** Never render a NumberPicker inline in a screen. Show a compact, borderless value trigger; open the wheel only in a `ModalBottomSheet` or dialog after a tap. Dismiss after confirmation or outside tap.
7. **Minimal expansion controls.** Use a tiny centered chevron/handle, visually subdued but with an accessible description and usable touch target.
8. **No bloated nested surfaces.** Avoid unnecessary outlines, repeated background panels, large row padding, and excessive corner radii. Prefer a single tonal layer with whitespace.

## Calculator reference flow

- Selection: 4-column artwork grid; All / Already Owned tabs; element and weapon-type icon-only filter chips; weapon rarity chips; no search field or inline numeric controls.
- Target setup: selected item header, compact current → target level values, three independent talent rows for characters, optional weapon slot, and one calculation action.
- Numeric selection: compact value trigger opens a concealed wheel picker in a modal bottom sheet.
- Material details: compact scrollable flat list/table with each material icon and readable name, plus aligned consumed, owned, and still-needed values. Owned quantities use non-keyboard touch controls.
- Keep catalogue ownership and source completeness distinctions visible. Do not infer unknown inventory as zero or publish incomplete totals.

## Screen-level application

- World Exploration: region heading, subareas, and subregions sit on the page background. Use single-level borderless tonal tiles; no giant enclosing region card.
- Tools: flat section headings and single-level entries; never nest the calculator inside a Tools card.
- Spiral Abyss: flat chamber/half sections with spacing or subtle dividers, not bordered boxes inside a parent card.
- Account Overview: visual baseline for tonal contrast, whitespace, typography, and centered expansion affordances.

## Enforcement

For every touched Kotlin UI file, inspect for `.border(`, `BorderStroke(`, `OutlinedCard`, inline `WheelNumberPicker` / `NumberPicker`, and keyboard-opening text inputs. A grep hit must be reviewed; it is not automatically a violation if it belongs to the intentional HoYoLAB sign-in WebView or a non-editable display. Record exceptions explicitly. Run relevant unit tests and Debug build in CI; device acceptance remains separate.
