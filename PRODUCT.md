# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Employees using an internal self-service help desk to troubleshoot workplace IT or office issues with AI, submit repair requests, and check request status.

## Product Purpose

Provide an internal service desk entry point where employees can ask the AI assistant about common workplace IT and office problems and create, search, or manage repair tickets. The confirmed primary audience is employees using self-service. Employees should begin with AI troubleshooting and submit a ticket when the issue remains unresolved.

## Operating Context

The product has two frontend areas: an AI assistant for knowledge questions and ticket status queries, and a ticket list for creating, searching, editing, and deleting tickets. The AI uses the knowledge documents and can query ticket status through tools. Ticket data is stored by the separate ticket service and MySQL. The frontend is a React and Vite web app, with desktop as the primary viewport and mobile as a responsive secondary viewport per the redesign request. Chat exchanges need only be available in the current page session; they do not need to persist between visits.

## Capabilities and Constraints

- Creating a ticket requires a requester name and issue description; the service returns its identifier, category, status, and creation time.
- Ticket lists can be searched by ticket identifier, requester name, and issue description, and filtered by status.
- Tickets can be edited or deleted. Deletion is irreversible and currently requires confirmation.
- AI answers can include knowledge source filenames and tool usage metadata. The chat page keeps its conversation only for the current page session.
- Preserve the knowledge source filenames shown with AI answers. Displaying which tools were used is not required.
- Ticket status is read from the ticket service; the interface must not imply a status that was not returned by the service.
- The redesign should preserve existing product behavior and API contracts while organizing the interface around employee self-service tasks.
- Employees should start with AI troubleshooting; a ticket is the next step when the issue is unresolved.
- Chat history does not need to persist between visits; source filenames should remain available with the corresponding answer, while tool usage records need not be shown or retained.

## Evidence on Hand

The repository contains the React frontend, API clients, backend services, README architecture and workflow documentation, and the knowledge documents under `helpdesk-ai/src/main/resources/knowledge`. There are no confirmed customer testimonials, service-level commitments, or usage metrics; do not invent them.

## Product Principles

- Let employees reach a useful next step quickly.
- Make AI answers and their references distinguishable.
- Show ticket identifiers and service-returned status clearly.
- Keep ticket submission concise and explain what information is needed.
