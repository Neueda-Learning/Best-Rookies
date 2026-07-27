# README-ing (Current Status Summary)

## Scope
This file summarizes what is done and what still remains in `Best-Rookies/` based on `README.md` and `PROJECT-MVP.md`.

## Verification status
- Backend tests: passed with `mvn test`.
- Frontend build: not reverified in this update; the last local attempt failed because `vite` was not available in the environment.
- Cleanups completed earlier: `backend/target/`, `frontend/dist/`, `.idea/` removed and `.gitignore` added.

## What has been completed

### Backend and API
- Create / list / detail / summary portfolio APIs.
- `PATCH /api/v1/portfolios/{id}` for name/baseCurrency updates.
- `DELETE /api/v1/portfolios/{id}` with cascade deletion.
- Position create / list / patch / delete APIs.
- `GET /api/v1/prices/{ticker}/latest` for latest price snapshots.
- Pagination and sorting for list endpoints.
- Validation for empty or blank portfolio update payloads.
- Standardized API error handling and response schema.
- Swagger / OpenAPI support.

### Persistence / DB
- MySQL datasource configured.
- Flyway migrations for initial schema, indexes, price snapshots, and extended `AssetType` values.
- `price_snapshots` backend layer implemented: entity, repository, service, controller.
- Datasource now supports environment-variable overrides.
- Demo seed script added: `backend/src/main/resources/db/seed-demo.sql`.

### Testing and documentation
- Integration tests expanded for:
  - create / update / delete portfolio
  - not-found cases
  - invalid PATCH payloads
  - paginated / sorted lists
- `backend/README.md` updated with startup, seed data, and Swagger info.

## What still remains

### Frontend
- Portfolio performance visualization / charts.
- Asset allocation charts.
- Better loading / error / empty states.
- Improved form validation and user feedback.

### Market data automation
- External market-data fetch is not wired yet.
- Scheduled job to populate `price_snapshots` is still missing.

### Documentation / workflow
- Team workflow artifacts such as branching / PR evidence.

### Stretch goals
- AI features / PoC.
- Quantum proof-of-concept.

## 4-Person Work Split

### Backend Engineer A (Portfolio Domain)
Completed:
- Portfolio create / list / detail / summary.
- `PATCH /api/v1/portfolios/{id}`.
- `DELETE /api/v1/portfolios/{id}`.

Remaining:
1. ~~Implement `PATCH /api/v1/portfolios/{id}`~~
2. ~~Implement `DELETE /api/v1/portfolios/{id}`~~
3. ~~Add validations for update DTO and business error messages~~
4. ~~Add integration tests for update/delete success and not-found cases~~

### Backend Engineer B (Position + API Quality)
Completed:
- Position create / list / patch / delete APIs.
- Pagination / sorting for list endpoints.
- Swagger / OpenAPI docs and error schema.
- Invalid payload / validation boundary tests.

Remaining:
1. ~~Add pagination/sorting for list endpoints~~
2. ~~Introduce OpenAPI/Swagger docs for all endpoints~~
3. ~~Add endpoint-level examples and standardized error response schema docs~~
4. ~~Add test cases for invalid payloads and validation boundary conditions~~

### Frontend Engineer (Vue UI)
Completed:
- Portfolio list and detail pages.
- Position create/delete interaction.

Remaining:
1. Add error handling/loading/empty states for all async calls.
2. Implement charts for performance and asset allocation.
3. Add portfolio edit/delete UI.
4. Improve form validation and user feedback.

### Database Engineer (MySQL + Migration)
Completed:
- Initial schema migration.
- Runtime datasource / Flyway alignment.
- Index migrations and `price_snapshots` table.
- Extended `AssetType` values.
- `price_snapshots` backend layer.
- Cascade deletion strategy.
- Demo seed script and datasource env-var support.

Remaining:
1. ~~Add migration for indexes and constraints tuning~~
2. ~~Define deletion policy and migrate accordingly~~
3. ~~Add seed data migration/script for demo and testing~~
4. ~~Document local DB bootstrap and environment-based credentials strategy~~
5. Wire external price fetch (Yahoo Finance / sample API) to `PriceSnapshotService.saveSnapshot()` via scheduled job (`@Scheduled`).

## Suggested next steps
1. Frontend charts and UX states.
2. External market-data fetch + scheduler.
3. Team workflow documentation.

