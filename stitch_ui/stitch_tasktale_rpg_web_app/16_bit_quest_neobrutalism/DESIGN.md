---
name: 16-Bit Quest Neobrutalism
colors:
  surface: '#fff8f5'
  surface-dim: '#e0d8d5'
  surface-bright: '#fff8f5'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#faf2ee'
  surface-container: '#f4ece8'
  surface-container-high: '#eee7e3'
  surface-container-highest: '#e9e1dd'
  on-surface: '#1e1b19'
  on-surface-variant: '#5b403e'
  inverse-surface: '#33302d'
  inverse-on-surface: '#f7efeb'
  outline: '#8f6f6d'
  outline-variant: '#e4beba'
  surface-tint: '#b91a24'
  primary: '#b61722'
  on-primary: '#ffffff'
  primary-container: '#da3437'
  on-primary-container: '#fffbff'
  inverse-primary: '#ffb3ad'
  secondary: '#0058be'
  on-secondary: '#ffffff'
  secondary-container: '#2170e4'
  on-secondary-container: '#fefcff'
  tertiary: '#825100'
  on-tertiary: '#ffffff'
  tertiary-container: '#a36700'
  on-tertiary-container: '#fffbff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad7'
  primary-fixed-dim: '#ffb3ad'
  on-primary-fixed: '#410004'
  on-primary-fixed-variant: '#930013'
  secondary-fixed: '#d8e2ff'
  secondary-fixed-dim: '#adc6ff'
  on-secondary-fixed: '#001a42'
  on-secondary-fixed-variant: '#004395'
  tertiary-fixed: '#ffddb8'
  tertiary-fixed-dim: '#ffb95f'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#fff8f5'
  on-background: '#1e1b19'
  surface-variant: '#e9e1dd'
typography:
  headline-xl:
    fontFamily: Space Grotesk
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Space Grotesk
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Space Grotesk
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Space Grotesk
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Space Grotesk
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Space Grotesk
    fontSize: 20px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: 0em
  title-retro:
    fontFamily: Space Mono
    fontSize: 16px
    fontWeight: '700'
    lineHeight: 24px
    letterSpacing: 0.08em
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '500'
    lineHeight: 28px
    letterSpacing: -0.01em
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
    letterSpacing: 0em
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0em
  stat-counter:
    fontFamily: Space Mono
    fontSize: 18px
    fontWeight: '700'
    lineHeight: 20px
    letterSpacing: 0.04em
  label-md:
    fontFamily: Space Mono
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.06em
  label-sm:
    fontFamily: Space Mono
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.08em
spacing:
  gutter: 1.5rem
  gutter-mobile: 1rem
  margin: 2rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

This design system synthesizes golden-era 16-bit console RPGs (SNES, Genesis) with modern neobrutalism. It pairs the high-agency, tactile feedback of tactile cartridges and dialogue text-boxes with crisp modern UI layouts. 

The aesthetic is characterized by:
- Pure black structural lines (`#000000`) functioning as pixel edges and grid frames.
- Hard, zero-blur cast shadows delivering immediate physical depth like chunky plastic cartridges and inventory tiles.
- A parchment-tinted base (`#FAFAF9` / Stone-50) emulating vintage game manuals and overworld maps without causing display fatigue.
- Punchy, high-saturation 16-bit HUD stat accents (HP red, MP blue, EXP gold, Quest emerald).
- Strict, zero-radius geometric edges evoking pixel grids and bitmap interfaces.

## Colors

The palette reproduces classic CRT/arcade status bars while maintaining AA/AAA contrast over light parchment surfaces.

### Primary Roles
- **Canvas Base (`#FAFAF9` / Stone-50):** The primary application canvas, avoiding pure stark white for a textured map/parchment quality.
- **Surface Elevation (`#FFFFFF`):** Interior container and dialog surface fill.
- **Black Outlines & Shadows (`#000000`):** Pure black for all borders, dividers, text glyphs on arcade badges, and projection drop shadows.
- **Dark Charcoal (`#1C1917`):** Primary body text and deep UI iconography to prevent eye strain while preserving crisp definition.

### RPG Status Spectrum
- **HP Crimson (`#EF4444`):** Health bars, boss alerts, destructive actions, high-priority quest items.
- **MP Mana Blue (`#3B82F6`):** Magic points, active filters, technical task tracking, information cues.
- **EXP Radiant Gold (`#F59E0B`):** Experience bars, level-up banners, coin counters, active toggle states.
- **Forest Emerald (`#10B981`):** Stamina meters, successful quest completion, green potions, safe zones.
- **Royal Purple (`#8B5CF6`):** Epic loot tiers, special spells, rare quest classifications.

All stateful interactions maintain full saturation without soft pastel washes.

## Typography

The type system addresses the tension between nostalgic retro gaming and readable product copy:
- **Display & Headings:** `Space Grotesk` delivers bold, angular, neobrutalist punch with mechanical geometry.
- **Retro Stats, Numbers, Badges & Microcopy:** `Space Mono` provides the monospaced, arcade HUD and character sheet readout aesthetic with zero horizontal misalignment across changing values.
- **Long-form Reading & Task Descriptions:** `Plus Jakarta Sans` provides high legibility, open apertures, and crisp readability for multi-line notes, quest descriptions, and productivity logs.

## Layout & Spacing

A strictly regimented 8-pixel base unit underpins all interface spacing.

### Grid Architecture
- **Desktop (1024px+):** 12-column fluid grid. Max container width: 1280px. Gutters: 24px (`1.5rem`), Margins: 32px (`2rem`). Panels align rigidly side-by-side like a multi-window battle layout.
- **Tablet (768px - 1023px):** 8-column grid. Gutters: 20px, Margins: 24px.
- **Mobile (<768px):** 4-column grid. Gutters: 16px (`1rem`), Margins: 16px (`1rem`). Complex inventory grids reflow into vertical inventory scrolls.

### Structure
Elements sit within well-defined structural borders rather than floating freely. Spacing within boxes stays compact to retain an authentic dialogue/inventory window density.

## Elevation & Depth

This system avoids Gaussian blurs, soft glows, and ambient light physics. Depth is exclusively conveyed through hard-edged directional drop shadows and heavy structural framing:

- **Resting Surface:** 2px solid `#000000` border with a 4px horizontal / 4px vertical hard offset shadow: `box-shadow: 4px 4px 0px 0px #000000`.
- **Raised / Hero Modal Surface:** 3px solid `#000000` border with an 8px offset: `box-shadow: 8px 8px 0px 0px #000000`.
- **Compact HUD / Chip / Tag:** 2px solid `#000000` border with a 2px offset: `box-shadow: 2px 2px 0px 0px #000000`.
- **Hover Transition:** Shadow extends by 2px (`6px 6px 0px 0px #000000`) and the element shifts -2px upward/leftward (`translate(-2px, -2px)`).
- **Pressed / Active Transition:** Shadow reduces to `0px 0px 0px 0px #000000` and the element translates directly into the baseline (`translate(4px, 4px)`), providing a mechanical "click" feedback identical to physical arcade switches.

## Shapes

The shape hierarchy is completely sharp (0px border radius across all elements). This matches 16-bit raster hardware limitations and sharp neobrutalist conventions. 

- Modals, action buttons, progress bars, profile avatars, chips, and tooltips are strictly square/rectangular.
- Cutout decorative accents (such as health bar caps or inventory slots) use strict 45-degree chamfers or pure orthogonal corners.
- Avatar containers must never clip to circles; they are square frames with 2px solid black borders.

## Components

### Buttons
- **Style:** 2px solid `#000000` border, sharp corners, bold `Space Mono` uppercase text, `box-shadow: 4px 4px 0px 0px #000000`.
- **Variants:**
  - *Primary Action (HP Crimson):* Background `#EF4444`, text `#FFFFFF`.
  - *Secondary Action (EXP Gold):* Background `#F59E0B`, text `#000000`.
  - *Neutral Action:* Background `#FFFFFF`, text `#000000`.
- **States:** Hover translates -2px/-2px with expanded shadow; Active translates +4px/+4px with zero shadow.

### Cards & Quest Logs
- **Style:** Background `#FFFFFF`, border `2px solid #000000`, `box-shadow: 4px 4px 0px 0px #000000`, padding `1.25rem`.
- **Headers:** Separated by a `2px solid #000000` horizontal rule.
- **Ribbons:** Stat badges anchored top-right with thick black outlines and solid fills (e.g., `#10B981` for "COMPLETED").

### HP / MP / EXP Meters
- **Track:** 2px solid `#000000`, background `#E7E5E4` (Stone-200), height 16px to 24px.
- **Fills:** Solid colors (`#EF4444` for HP, `#3B82F6` for MP, `#F59E0B` for EXP). No gradients.
- **Segmented Overlay:** Vertical 1px black division lines every 10% to emulate classic tiled gauge blocks.

### Input Fields
- **Style:** Background `#FFFFFF`, border `2px solid #000000`, padding `0.75rem 1rem`, `box-shadow: 2px 2px 0px 0px #000000`.
- **Focus State:** Background remains `#FFFFFF`, shadow shifts to `4px 4px 0px 0px #3B82F6` (Mana Blue outline shadow).
- **Placeholder:** Styled in `Space Mono`, color `#78716C` (Stone-500).

### Checkboxes & Radios
- **Checkboxes:** Pure 20x20px square, 2px solid `#000000`, 2px drop shadow. Checked state fills with `#10B981` (Forest Emerald) containing a sharp black checkmark glyph.
- **Radio Buttons:** Pure 20x20px square (never rounded). Selected state displays an inner solid black square (`8x8px`).

### Chips & Stat Badges
- **Style:** Border `2px solid #000000`, padding `0.25rem 0.5rem`, `Space Mono` uppercase text, `box-shadow: 2px 2px 0px 0px #000000`.
- **Color Fills:** Match status categories (e.g., `LVL 42` in EXP Gold, `DEF 18` in Mana Blue).

### Dialogue & Notification Modals
- **Style:** Background `#FFFFFF`, border `3px solid #000000`, `box-shadow: 8px 8px 0px 0px #000000`.
- **Decoration:** Top header bar features alternating diagonal black hatching or pure black banner with inverted white pixel type.