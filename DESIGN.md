# DESIGN.md — CampusComplaint Design Contract

This file is the binding constraint for **all** UI work. Every page, component
and tweak must obey it. If a change seems to need a rule broken here, change
this file first — never diverge silently.

References: shadcn/ui visual style (https://ui.shadcn.com) + Tailwind CSS
utility conventions (https://tailwindcss.com). Components are hand-built in
Thymeleaf markup — shadcn's React code is **not** imported.

---

## 1. Color tokens

| Token | Hex | Usage |
|---|---|---|
| `primary` | `#4338CA` | buttons (solid), links, active nav, numbered circles, headings accents, Thank You |
| `primary-hover` | `#3730A3` | primary button hover |
| `accent` | `#EAB308` | eyebrow dots, underline bars, subsection subheads, deadline branch |
| `surface` | `#EEF2FF` | card fills (highlight), table alt rows, badge backgrounds, swimlane fills |
| `danger` | `#DC2626` | errors, reject actions, invalid transitions |
| `success` | `#16A34A` | accepted/closed badges |
| `warning` | `#D97706` | resolved / auto-closed badges |
| `bg` | `#FAFAFA` | page background |
| `card` | `#FFFFFF` | card/panel background |
| `border` | `#E4E4E7` | all 1px borders, dividers, input borders |
| `text` | `#18181B` | primary text |
| `muted` | `#71717A` | secondary text, placeholders, timestamps |
| `nav-bg` | `#FFFFFF` | sidebar background |

**Rules:** no other colors anywhere. Status badges map to tokens only
(FILED→surface/primary text, ASSIGNED→surface/primary, IN PROGRESS→surface/
warning, RESOLVED→surface/warning, CLOSED→surface/success, AUTO
CLOSED→surface/muted, REOPENED→surface/accent text).

## 2. Font

**Plus Jakarta Sans** — single family for everything.
- Files bundled locally: `static/fonts/plus-jakarta-sans-{400,500,600,700}.woff2`
- Fallback: `system-ui, sans-serif`
- Weights: 400 body · 500 inputs/badges · 600 subheads/buttons/table headers ·
  700 page titles & numerals

## 3. Type scale (only these sizes)

| Token | Size | Line-height | Use |
|---|---|---|---|
| `text-xs` | 12px | 16px | timestamps, table meta, eyebrow labels |
| `text-sm` | 14px | 20px | secondary text, table cells, badges |
| `text-base` | 16px | 24px | body, form inputs, bullets |
| `text-lg` | 20px | 28px | card titles, subsection subheads |
| `text-xl` | 24px | 32px | section headings on pages |
| `text-2xl` | 32px | 40px | page titles, stat numbers |
| `text-3xl` | 40px | 48px | login title, Thank You |

## 4. Spacing scale (4px base — only these values)

`4 · 8 · 12 · 16 · 24 · 32 · 48 · 64`
- Card padding: 24 · gap between cards: 16 · page padding: 32 (desktop) / 16
  (mobile) · form field gap: 16 · label→input gap: 6 (use 4+2 via padding)
- Section gap on page: 32

## 5. Shape & depth

- Radius: cards/buttons/badges `8px` (`rounded-lg`), inputs `6px`
  (`rounded-md`), avatars/circles full
- Borders: `1px solid #E4E4E7` — **no heavy shadows**. Cards: border only.
  Dropdowns/modals (if any): `0 4px 16px rgba(24,24,27,0.08)`
- Focus ring: `2px solid #4338CA` offset 2px

## 6. Component specs (shadcn styling rules)

| Component | Spec |
|---|---|
| **Button primary** | bg `#4338CA`, text white, h-40px, px-16, radius 8, weight 600, hover `#3730A3`, disabled 50% opacity |
| **Button outline** | white bg, 1px border, text `#18181B`, hover bg `#FAFAFA` |
| **Button danger** | white bg, 1px border `#DC2626`, text `#DC2626`, hover bg `#FEF2F2` |
| **Button ghost** | transparent, text `#4338CA`, hover bg `#EEF2FF` |
| **Input** | h-40px, px-12, border 1px `#E4E4E7`, radius 6, text-base, placeholder `#71717A`, focus border `#4338CA` |
| **Textarea** | same as input, min-h 96px, py-8 |
| **Label** | text-sm weight 600, color `#18181B`, mb-6 |
| **Card** | bg white, border 1px, radius 8, padding 24 |
| **Stat card** | Card + text-xs muted uppercase label + text-2xl weight-700 number |
| **Table** | full width; header: text-xs uppercase muted weight-600, bottom border; rows: h-48px, bottom border `#E4E4E7`, alt rows bg `#FAFAFA`; hover row bg `#EEF2FF` |
| **Badge** | inline, h-22px, px-8, radius 8, text-xs weight-600, bg/text per status map (§1) |
| **Alert (error)** | bg `#FEF2F2`, border `#DC2626`, text `#DC2626`, radius 8, padding 12 |
| **Eyebrow** | text-xs uppercase letter-spaced muted + gold `#EAB308` dot separator |
| **Page heading** | title text-2xl weight-700 + short gold underline bar (4px tall, 48px wide, radius full) |

## 7. Page layouts (dictated — do not infer)

**Login** (`/login`)
- Centered vertically+horizontally; card width 400px; above card: brand
  `CampusComplaint` text-3xl weight-700 + tagline muted; below card: demo
  credentials hint text-xs muted.

**Dashboards** (student / admin / department share skeleton)
- Fixed left sidebar 240px: brand top, nav links (text-sm, active = bg
  `#EEF2FF` + text `#4338CA`), user block bottom with Logout (ghost button)
- Main area: page heading row (title + primary action button right), then
  4-column stat card grid (gap 16), then full-width table card.

**New complaint** (`/complaints/new`)
- Single column, max-width 640px, centered; card with fields: title (input),
  category (select), priority (select), description (textarea); footer row:
  Cancel (outline) + Submit (primary).

**Complaint detail** (`/complaints/{id}`)
- 2-column grid `1fr 320px`, gap 24.
  - Left: status timeline card (vertical steps with dots/lines) + remarks
    thread card (list + add-remark form).
  - Right: status badge card (big), meta list (id, category, priority,
    assignee, dates), action buttons stacked full-width (role-gated:
    student → Accept primary / Reject danger / Reopen outline; dept →
    Start Work / Mark Resolved; admin → Assign select + button).

**Search** (`/search`)
- Filter bar card: keyword input + status select + category select + Filter
  button (one row, gap 12); results = standard table card below.

## 8. Interaction rules

- Illegal action → red alert text from server (`InvalidStatusException` message)
- Success → inline green confirmation strip under page heading
- Buttons disabled (50% opacity, `not-allowed`) when action not permitted —
  never hide context, show why via title tooltip
- Empty states: centered muted text `Nothing here yet.` + icon dot

## 9. Hard rules (anti-slop)

1. No color outside §1, no font size outside §3, no spacing outside §4
2. One font family only (§2)
3. No gradients, no emojis in UI, no stock illustrations
4. No component built ad-hoc — use §6 spec or update this file first
5. Section layouts come from §7 — changes are scoped to one template at a time
6. Tailwind utilities in templates; custom CSS only for @font-face and tokens

## 10. Logo

- **Asset:** `static/img/logo-mark.svg` — 32×32, `rx=8` tile filled `primary`
  `#4338CA`, white stroked "C" (stroke 5, round caps, 270° arc opening right),
  `accent` `#EAB308` dot (r=3) in the top-right corner.
- **Favicon:** `<link rel="icon" type="image/svg+xml">` lives in
  `fragments/layout :: head` — every page inherits it.
- **Usage:** replace the asset with `<img th:src="@{/img/logo-mark.svg}">` at
  `h-8 w-8` (sidebar) or `h-12 w-12` (login, error pages). Letter tiles rendered
  as text spans are forbidden — always use the asset. No other colors inside
  the SVG.

## 11. Responsive (mobile-first)

Breakpoints: Tailwind defaults — `sm` 640px, `lg` 1024px. **Desktop
(≥1024px) must stay pixel-identical to the §7 layout.**

- **Sidebar ≥1024:** fixed 240px as §7. **<1024:** off-canvas drawer
  (`-translate-x-full`, slides in via hamburger), closed by scrim tap or
  tapping a nav link.
- **Mobile top bar <1024:** fixed `h-14`, `z-30`, bg `card`, border-b —
  hamburger + logo + brand + notification bell (panel anchors `right-3
  top-full`). Scrim: `bg-ink/40` — the only overlay color allowed.
- **Content offset:** `px-4 pt-16 pb-8` on mobile (pt clears the top bar,
  leaves an 8px gap) → `lg:ml-60 lg:px-8 lg:py-8`.
- **Stat cards:** `grid-cols-2` → `lg:grid-cols-4`.
  **Detail page:** single column → `lg:grid-cols-[1fr_320px]`.
- **Tables:** outer wrapper keeps `overflow-hidden rounded-lg`, add inner
  `overflow-x-auto`, table gets `min-w-[900px]` (9-col) / `min-w-[720px]`
  (7-col) — swipe to scroll, never squash columns below readable width.
- **Heading rows:** add `flex-wrap gap-4` so action buttons drop below the
  title. **Search filter row:** `flex-wrap`.
- **new.html** category/priority pair: `grid-cols-1 sm:grid-cols-2`.
- Touch targets: buttons stay `h-10` (40px) minimum.
