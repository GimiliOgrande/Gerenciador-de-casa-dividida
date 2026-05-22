# AGENTS.md

## Project Context

This repository contains a web application for managing shared rental houses. The main goal is to help people who live with roommates organize rent, bills, domestic tasks, household responsibilities, and payment tracking.

The project is split into:

- `backend`: Spring Boot API.
- `frontend`: React web application.

The application should be built with the expectation that more features will be added over time, such as task rotation, household finance control, rent and utility payment confirmation, notifications, roommate management, and shared expense history.

## Language And Naming

- Use English for code identifiers, variables, classes, methods, components, files, database columns, and API fields.
- Use `camelCase` for variables, methods, functions, object properties, and frontend state.
- Use `PascalCase` for Java classes, React components, DTO classes, and TypeScript types/interfaces.
- Use clear domain names such as `House`, `Roommate`, `Expense`, `Payment`, `Task`, `Bill`, `Rent`, and `Invitation`.
- Avoid abbreviations unless they are common and obvious, such as `id`, `DTO`, `URL`, or `JWT`.
- Keep user-facing text in Portuguese unless a specific feature requires another language.

## Backend Architecture

Preserve the existing layered architecture:

`Controller -> Service -> Repository -> Model`

Use each layer with clear responsibilities:

- Controllers handle HTTP requests, response status codes, request mapping, authentication context extraction, and DTO conversion when appropriate.
- Services contain business rules, permission checks, validation, transaction boundaries, and orchestration between repositories.
- Repositories only handle database access.
- Models represent persisted domain entities and should not contain request-specific logic.

Do not bypass the service layer from controllers. Controllers should not call repositories directly.

## Backend Packages

Prefer package organization by technical layer unless the existing codebase already uses another consistent pattern.

Recommended structure:

- `controller`
- `service`
- `repository`
- `model`
- `dto`
- `config`
- `security`
- `exception`
- `mapper` when mapping logic becomes repeated or complex

If a feature grows large, feature-specific subpackages are acceptable, but keep the layer responsibilities intact.

## Services, Validation, And Permissions

- Keep authorization, permission rules, and business validation in services whenever possible.
- Validate that the authenticated user has access to the target house, expense, task, payment, or invitation before reading or mutating it.
- Avoid trusting IDs received from the frontend without checking ownership or membership.
- Prefer explicit service methods such as `validateHouseAccess`, `getHouseForCurrentUser`, or `ensureUserCanManageExpense` when the same rule is reused.
- Use transactions in service methods that perform multiple related database operations.
- Keep validation messages clear and useful for the frontend.

## Authentication And Security

- Authentication uses a cookie named `token`.
- JWT validation is handled by `JwtUtil`.
- Do not introduce a second authentication mechanism unless explicitly requested.
- Do not read JWT claims manually in multiple places if `JwtUtil` or an existing security helper can centralize that behavior.
- Treat authentication and authorization as separate concerns:
  - Authentication confirms who the user is.
  - Authorization confirms what that user can access or change.
- Cookies should be configured with secure defaults appropriate to the environment.
- Avoid returning sensitive fields such as passwords, password hashes, internal tokens, or unnecessary user metadata.

## Database

- Use PostgreSQL as the database.
- Design entities with PostgreSQL compatibility in mind.
- Prefer stable primary keys, normally `Long` or `UUID`, following the existing backend style.
- Use proper relationships for domain concepts such as house membership, expenses, payments, tasks, and users.
- Avoid storing derived values when they can be calculated reliably, unless performance or audit history requires persistence.
- Use migrations if the project already has Flyway or Liquibase. If migrations are introduced later, keep schema changes explicit and versioned.

## API Design

- Prefer RESTful endpoints with clear resource names.
- Use plural nouns for collections, such as `/houses`, `/expenses`, `/tasks`, and `/payments`.
- Use DTOs for request and response bodies instead of exposing entities directly when the entity contains relationships, sensitive fields, or persistence details.
- Return appropriate HTTP status codes:
  - `200 OK` for successful reads and updates.
  - `201 Created` for successful creation.
  - `204 No Content` for successful deletion without a response body.
  - `400 Bad Request` for validation errors.
  - `401 Unauthorized` for missing or invalid authentication.
  - `403 Forbidden` for authenticated users without permission.
  - `404 Not Found` when a resource does not exist or should not be exposed.
- Keep error responses consistent across endpoints.

## Frontend Architecture

- Keep the React app organized by reusable components, pages, services/API clients, hooks, and shared types where applicable.
- Prefer calling the backend through centralized API utilities instead of scattering raw `fetch` or `axios` calls throughout components.
- Keep UI state local when it only affects one component.
- Move shared state into hooks or context only when multiple screens need it.
- Keep forms predictable, validated, and aligned with backend DTOs.
- Use English names for components, variables, functions, and files.
- Keep visible UI text in Portuguese for the target users.

## Frontend UX Direction

The product is a practical household management tool, so the interface should feel clear, organized, and efficient.

- Prioritize dashboards, lists, tables, forms, filters, and status indicators over marketing-style sections.
- Make important household status visible quickly, such as pending rent payments, overdue bills, upcoming tasks, and roommate responsibilities.
- Use clear visual states for paid, pending, overdue, completed, and assigned items.
- Avoid decorative UI that makes repeated daily use slower.
- Keep responsive behavior in mind from the start, especially for students using phones.

## Domain Rules To Preserve

When implementing features, consider these likely domain rules:

- A user may belong to one or more houses.
- A house can have multiple roommates.
- Only house members should access house data.
- Some actions may be restricted to house owners/admins, such as removing members, changing house settings, or deleting shared records.
- Expenses and bills should track who created them, who owes money, who has paid, and payment status.
- Domestic tasks should track assignment, due dates, recurrence when needed, and completion status.
- Important financial changes should be auditable where practical.

These rules can evolve, but do not ignore them when adding new features.

## Testing And Quality

- Add or update tests when changing business rules, permission behavior, validation, or shared utilities.
- Backend service tests are especially important for permissions and financial calculations.
- Keep controller tests focused on request/response behavior.
- Keep frontend tests focused on user-visible flows and important component states.
- Run relevant tests before considering a change complete when the project has test commands available.

## Error Handling

- Prefer centralized backend exception handling for predictable API responses.
- Do not leak stack traces or internal implementation details to the frontend.
- Use specific exceptions for common cases such as not found, forbidden access, invalid credentials, and validation failure.
- Keep frontend error messages friendly and actionable.

## Code Style

- Follow the existing project style before introducing new patterns.
- Keep methods small enough to read comfortably.
- Avoid unrelated refactors while implementing a feature.
- Prefer explicit, readable code over clever shortcuts.
- Add comments only when they explain non-obvious business decisions or complex logic.
- Do not leave dead code, unused imports, or debugging logs.

## Git And Change Safety

- Do not overwrite or revert user changes unless explicitly requested.
- Keep changes scoped to the requested feature or fix.
- Before large edits, inspect the existing structure and follow local conventions.
- If a requested change conflicts with existing code or data assumptions, explain the tradeoff before changing the design.

## Future Feature Ideas

Possible features to keep in mind when designing the system:

- House creation and roommate invitations.
- Shared rent and utility bill tracking.
- Payment confirmation per roommate.
- Domestic task board with recurring assignments.
- Dashboard with pending actions and alerts.
- Expense splitting by equal shares or custom values.
- Payment history and monthly summaries.
- Notifications for overdue payments or tasks.
- Role-based permissions inside a house.
- Attachments or receipts for bills and expenses.

These ideas are not mandatory yet, but the code should remain flexible enough to support them later.
