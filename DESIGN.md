---
name: 青禾科技 IT 服務台
description: An employee quick guide for finding IT troubleshooting steps and continuing to a ticket when needed.
colors:
  teal: "#164e63"
  teal-dark: "#103e50"
  lime: "#d8f05b"
  paper: "#f6f8f4"
  mist: "#e9f1ee"
  ink: "#16232a"
  muted: "#475e65"
  surface: "#ffffff"
  surface-soft: "#eef4f1"
  line: "#d6e2dd"
  line-strong: "#aabdb5"
  accent-soft: "#e2efec"
  accent-ink: "#123e4e"
  warning: "#795600"
  warning-soft: "#fff5d6"
  danger: "#a42d2b"
  danger-soft: "#fff0ee"
  success: "#176747"
  success-soft: "#e3f3e9"
  white: "#ffffff"
typography:
  body:
    fontFamily: "Noto Sans TC, PingFang TC, Microsoft JhengHei, sans-serif"
    fontSize: "15px"
    fontWeight: 400
    lineHeight: 1.6
    letterSpacing: "normal"
  headline:
    fontFamily: "Noto Sans TC, PingFang TC, Microsoft JhengHei, sans-serif"
    fontSize: "clamp(2rem, 3vw, 2.7rem)"
    fontWeight: 700
    lineHeight: 1.12
    letterSpacing: "-0.035em"
  title:
    fontFamily: "Noto Sans TC, PingFang TC, Microsoft JhengHei, sans-serif"
    fontSize: "1.13rem"
    fontWeight: 700
    lineHeight: 1.4
    letterSpacing: "-0.015em"
  label:
    fontFamily: "Noto Sans TC, PingFang TC, Microsoft JhengHei, sans-serif"
    fontSize: "12px"
    fontWeight: 650
    lineHeight: 1.4
    letterSpacing: "normal"
rounded:
  sm: "8px"
  md: "12px"
  lg: "16px"
  button: "10px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "32px"
components:
  button-primary:
    backgroundColor: "{colors.teal}"
    textColor: "{colors.white}"
    rounded: "{rounded.button}"
    padding: "9px 15px"
    height: "42px"
  button-secondary:
    backgroundColor: "{colors.white}"
    textColor: "{colors.ink}"
    rounded: "{rounded.button}"
    padding: "9px 15px"
    height: "42px"
  input:
    backgroundColor: "{colors.white}"
    textColor: "{colors.ink}"
    rounded: "{rounded.sm}"
    padding: "10px 12px"
    height: "44px"
  navigation-active:
    backgroundColor: "{colors.lime}"
    textColor: "{colors.ink}"
    rounded: "9px"
    padding: "10px 12px"
    height: "52px"
  tag-status:
    backgroundColor: "{colors.success-soft}"
    textColor: "{colors.success}"
    rounded: "999px"
    padding: "4px 10px"
    height: "27px"
  card:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.ink}"
    rounded: "{rounded.lg}"
    padding: "24px"
  handoff:
    backgroundColor: "#f4f8e7"
    textColor: "{colors.ink}"
    rounded: "9px"
    padding: "13px 14px"
---

# Design System: 青禾科技 IT 服務台

## Overview

**Creative North Star: "The Employee Quick Guide"**

This is a practical guide for turning a vague office or IT problem into a useful next step. Deep teal anchors the service identity, yellow-green marks the current route and key actions, and pale paper surfaces keep instructions easy to scan. Traditional Chinese uses the system sans stack already present in the app; no custom font assets are assumed.

The experience starts with AI question entry, a compact topic index, and readable troubleshooting responses. Answers retain their source filenames, and an unresolved issue can move into the ticket form with its description carried forward. The chat exists only in the current page session. Ticket search, status filtering, create, edit, and delete remain part of the same service desk. Desktop is the primary layout; mobile reflows the same tasks into a narrow column and bottom navigation. The responsive layout is specified from CSS; mobile has not been visually verified.

**Key Characteristics:**
- Teal service identity with a bright lime active-state accent.
- Familiar controls, restrained borders, and clear labels.
- Troubleshooting answer, source filename, and ticket handoff form one path.

## Colors

The palette pairs a stable deep teal with a high-visibility lime accent and cool, pale green-gray surfaces.

### Primary
- **Deep Guide Teal** (`{colors.teal}`): Brand rail, primary actions, and active links or icons.
- **Deep Teal Shade** (`{colors.teal-dark}`): Hover state for primary actions.
- **Clear Guide Lime** (`{colors.lime}`): Active navigation, focus ring, selection highlight, and brand mark.

### Neutral
- **Soft Paper** (`{colors.paper}`): Main page canvas.
- **Pale Mist** (`{colors.mist}`): Guide index and ticket table header tonal surface.
- **Soft Surface** (`{colors.surface-soft}`): Secondary control and question-band surface.
- **White Surface** (`{colors.surface}`): Cards and fields.
- **Ink** (`{colors.ink}`): Primary text.
- **Muted Teal Gray** (`{colors.muted}`): Supporting text.
- **Fine Divider** (`{colors.line}`) and **Strong Divider** (`{colors.line-strong}`): Borders and separators.
- **Soft Accent** (`{colors.accent-soft}`) and **Accent Ink** (`{colors.accent-ink}`): Teal-tinted supporting emphasis.

### Secondary
- **Caution Ochre** (`{colors.warning}`) with **Caution Wash** (`{colors.warning-soft}`): Waiting or caution status.
- **Alert Red** (`{colors.danger}`) with **Alert Wash** (`{colors.danger-soft}`): Errors, urgent statuses, and destructive action text.
- **Resolved Green** (`{colors.success}`) with **Resolved Wash** (`{colors.success-soft}`): Completed status and success notices.

**The Teal-and-Lime Wayfinding Rule.** Reserve lime for active, selected, or immediately actionable elements; use teal for stable service identity and primary action.

## Typography

**Display Font:** Noto Sans TC (with PingFang TC, Microsoft JhengHei, sans-serif fallbacks)
**Body Font:** Noto Sans TC (with PingFang TC, Microsoft JhengHei, sans-serif fallbacks)
**Label/Mono Font:** Cascadia Code (with Consolas, monospace) for source filenames.

**Character:** A workhorse system sans keeps Chinese instructions direct and familiar. Compact labels and stronger headings create hierarchy without relying on a display face.

### Hierarchy
- **Headline** (700, responsive `clamp(2rem, 3vw, 2.7rem)`, 1.12): Main page title with tight tracking.
- **Title** (700, 1.13rem, 1.4): Section headings for compose, history, and guide index.
- **Body** (400, 15px desktop and 16px at narrow widths, 1.6): Questions, instructions, and ticket content.
- **Label** (650, 12px, normal tracking): Field labels, badges, and supporting metadata.
- **Source** (600, .73rem): Exact source filenames in a monospace chip.

## Layout

Desktop uses a fixed 248px left rail and a centered content region capped at 1440px. Main padding scales from 28px to 64px. The help view pairs a flexible conversation column with a 292px quick index; the index narrows to 260px at the intermediate breakpoint. At 820px, the help view becomes one column, placing question entry, guide index, and response in sequence. At 720px, the rail becomes a sticky compact header and fixed two-item bottom navigation; at 420px, horizontal page padding tightens to 15px. Ticket rows become labeled stacked records at 1120px and below. Spacing uses a compact 4/8/16/24/32px rhythm, with card padding responsive to viewport width.

## Elevation & Depth

Cards use a quiet low shadow over a pale canvas, while borders and slight surface shifts do most of the structural work. The token set also defines restrained card and pop shadows for layered contexts; no dramatic floating panels are part of the current interface. Focus uses a distinct lime ring so keyboard position remains easy to find.

### Shadow Vocabulary
- **Small ambient** (`0 2px 7px rgb(22 47 53 / 5%)`): Subtle separation.
- **Card lift** (`0 7px 22px rgb(22 47 53 / 6%)`): Card depth.
- **Pop layer** (`0 16px 36px rgb(22 47 53 / 15%)`): Foreground layer when needed.

## Shapes

Controls and surfaces use gently rounded corners rather than pill-like geometry. Inputs use an 8px radius, buttons around 10px, common cards 16px, and status badges remain fully pill-shaped. Thin green-gray borders define fields and panels; list rows rely on horizontal dividers. Keep filenames readable as bounded, wrapping code chips.

## Components

### Buttons
- **Shape:** Compact rounded rectangles (10px); default minimum height is 42px, growing to 44px on mobile.
- **Primary:** Teal fill, white text, and 9px 15px padding; darkens on hover.
- **Secondary:** White fill, ink text, fine border; shifts to soft surface on hover.
- **Danger:** White surface with red text and a pale red hover surface.
- **Focus / Disabled:** Lime focus ring; disabled controls reduce opacity and suppress hover changes.

### Chips
- **Status badges:** Small pill with a matching dot and semantic tone for neutral, waiting, working, done, or urgent status.
- **Source filename:** Pale mist code chip in Cascadia Code/Consolas; preserve exact filenames and allow long names to wrap.

### Cards / Containers
- **Corner Style:** Soft 16px corners for compose, answer, editor, and list panels.
- **Background:** White on the paper canvas, with fine divider border.
- **Internal Padding:** Responsive 16–29px for compose; compact 18–20px inset for guide content.
- **Question band:** Soft surface distinguishes the employee's question from the answer body.

### Inputs / Fields
- **Style:** White field, strong divider stroke, 8px radius, and 10px 12px padding; textarea retains vertical resizing.
- **Focus:** Teal border plus the shared lime focus ring.
- **Labels:** Ink, 14px, semibold; hints use smaller muted text.

### Navigation
- **Desktop:** Fixed teal rail, identity at top, small letter-spaced section label, then familiar icon-and-label links. Active destination uses lime fill and ink text.
- **Mobile:** Sticky white brand header and fixed white bottom navigation with two equal destinations; active item uses lime fill.

### Signature: Guide-to-Ticket Handoff
The response keeps the exact source filenames directly below the answer, then shows a softly tinted unresolved-issue prompt and a secondary “建立報修單” action. This action carries the employee's question into the ticket form. Keep this order so provenance and the next action stay adjacent to the guidance.

## Do's and Don'ts

### Do:
- **Do** keep the AI question, ordered guidance, exact source filenames, and ticket handoff legible as one employee path.
- **Do** use the established teal and lime roles for primary action and current navigation.
- **Do** preserve clear field labels, visible keyboard focus, and the responsive stacking described in Layout.
- **Do** keep the current-page chat scope clear; conversation history does not persist across pages.

### Don't:
- **Don't** present tool-usage records as part of the employee-facing answer.
- **Don't** imply guaranteed answers, response times, live support health, or user metrics.
- **Don't** drop or paraphrase source filenames.
- **Don't** introduce decorative display fonts or font assets absent from the implementation.
