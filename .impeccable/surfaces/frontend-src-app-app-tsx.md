---
version: 1
slug: "frontend-src-app-app-tsx"
primary_target: "frontend/src/app/App.tsx"
related_targets: ["frontend/src/features/chat/ChatPage.tsx","frontend/src/features/tickets/TicketsPage.tsx"]
---

# Surface brief

## Scope & mode

Replace the visual organization of the React web help desk as an Operate surface. Desktop is primary; mobile must reflow cleanly for secondary use. Keep the company identity and all ticket management actions. Chat stays session-only.

## Audience and task

Employees first ask AI for help with common IT and office issues. They scan suggested questions or enter a problem, follow the answer, verify its source filename, and create a ticket if it remains unresolved. Ticket users can search, filter by status, create, edit, and delete tickets.

## Constraints and content

Preserve backend behavior, API contracts, field validation, current status values, confirmations, and Taipei timestamps. Keep the AI's knowledge source filenames with each answer. Tool usage records do not need to appear. Do not invent service response times, answer guarantees, live support health, or user metrics. Existing product name is 青禾科技 IT 服務台.

## Direction contract

THESIS: A quick employee guide turns a vague IT problem into a clear next step. It rejects a generic chatbot page with a detached ticket-management area.

OWN-WORLD: Use a compact quick-guide index language: deep teal #164E63, clear yellow-green #D8F05B, pale neutral #F6F8F4, and ink #16232A. Workhorse sans type, short section labels, open whitespace, and narrow category index bands carry the guide. Standard web navigation, buttons, fields, filters, and tables remain familiar.

STORY: Employees find a common issue or type a question, read ordered troubleshooting guidance, see the exact source filename beside the answer, then continue to a ticket form when the issue is unresolved. Existing ticket search, status filtering, create, edit, and delete stay available.

FIRST VIEWPORT: At desktop width, a fixed left rail identifies 青禾科技 and offers AI 問答 and 報修單. In the main area, a clear heading and one broad question field lead. A compact guide index for network, account, computer, and office topics sits beside or below the AI response. The response is readable at a comfortable measure, with numbered actions, a source filename directly below, and a visible 建立報修單 handoff. Mobile stacks the rail into a compact header and bottom navigation, then places the composer, index, answer, source, and handoff in a single column.

FORM: The chosen form is the employee quick guide from the dealt direction hand, one of seven grounded candidates; the user selected it over the assigned work-week schedule and the airport wayfinding alternate. Seed reference: none (unavailable).

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance

## Unresolved decisions

No additional product decisions are required for this refactor. The quick-guide visual world is approved for this surface only.
