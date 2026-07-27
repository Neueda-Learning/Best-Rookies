# README-ing (Implementation Gap Check)

## Scope
This file compares current code in `Best-Rookies/` against requirements in `README.md` and `PROJECT-MVP.md`.

## Quick Health Check (verified)
- Backend tests: `backend` passed with `mvn test`.
- Frontend build: `frontend` passed with `npm run build`.
- Redundant local outputs cleaned: `backend/target/`, `frontend/dist/`, `.idea/`.
- Added ignore rules: `.gitignore`.

## Requirement Comparison

### Completed Requirements

#### Core API (save/retrieve portfolio records)
- Create portfolio: `POST /api/v1/portfolios`.
- List portfolios: `GET /api/v1/portfolios`.
- Get portfolio detail: `GET /api/v1/portfolios/{id}`.
- Portfolio summary (total positions + total cost): `GET /api/v1/portfolios/{id}/summary`.
- Create position: `POST /api/v1/positions`.
- List positions: `GET /api/v1/positions?portfolioId={id}`.
- Delete position: `DELETE /api/v1/positions/{id}`.
- Patch position: `PATCH /api/v1/positions/{id}`.

Evidence:
- `backend/src/main/java/com/bestrookies/portfolio/controller/PortfolioController.java`
- `backend/src/main/java/com/bestrookies/portfolio/controller/PositionController.java`
- `backend/src/main/java/com/bestrookies/portfolio/service/PortfolioService.java`

#### Frontend minimal workflow
- Browse portfolios.
- Open portfolio detail.
- Add positions.
- Remove positions.

Evidence:
- `frontend/src/views/PortfolioListView.vue`
- `frontend/src/views/PortfolioDetailView.vue`
- `frontend/src/components/PositionForm.vue`

#### Persistence / DB
- MySQL datasource configured.
- Flyway migration for initial schema exists and is enabled.

Evidence:
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/db/migration/V1__init_schema.sql`

#### Testing baseline
- Integration test covers create portfolio, create position, query summary.

Evidence:
- `backend/src/test/java/com/bestrookies/portfolio/PortfolioApiIntegrationTest.java`

### Not Completed / Partially Completed Requirements

#### README.md (high-priority frontend goals)
- Portfolio performance visualization (graphical) is not implemented.

#### README.md (recommended engineering goals)
- API usage documentation (Swagger/OpenAPI) is not implemented.
- Team workflow artifacts (branching/PR evidence) are not represented in repo docs.

#### PROJECT-MVP.md next steps
- `PUT/PATCH /portfolios/{id}` not implemented.
- `DELETE /portfolios/{id}` not implemented.
- Market data integration (Yahoo/sample API) not implemented.
- Frontend charts (performance/asset allocation) not implemented.
- Swagger/OpenAPI not implemented.

#### Stretch goals (README Appendix E)
- AI features: not implemented.
- Quantum proof-of-concept: not implemented.

## Code Issues Fixed in This Check
- Updated dev CORS to support both `localhost` and `127.0.0.1` with flexible ports:
  - `backend/src/main/java/com/bestrookies/portfolio/config/WebConfig.java`
- Added root `.gitignore` to avoid committing generated artifacts.
- Removed redundant generated directories from working tree:
  - `backend/target/`
  - `frontend/dist/`
  - `.idea/`

## 4-Person Work Split (Completed + Remaining)

### Backend Engineer A (Portfolio Domain)
Completed ownership:
- Portfolio create/list/detail/summary API.
- Service-level summary calculation.

Remaining tasks:
1. Implement `PATCH /api/v1/portfolios/{id}` (name/baseCurrency updates).
2. Implement `DELETE /api/v1/portfolios/{id}` (with safe cascade rules).
3. Add validations for update DTO and business error messages.
4. Add integration tests for update/delete success and not-found cases.

### Backend Engineer B (Position + API Quality)
Completed ownership:
- Position create/list/patch/delete API.
- Global exception handling baseline.

Remaining tasks:
1. Add pagination/sorting for list endpoints.
2. Introduce OpenAPI/Swagger docs for all endpoints.
3. Add endpoint-level examples and standardized error response schema docs.
4. Add test cases for invalid payloads and validation boundary conditions.

### Frontend Engineer (Vue UI)
Completed ownership:
- Portfolio list and detail pages.
- Position create/delete interaction with backend.

Remaining tasks:
1. Add error handling/loading/empty states for all async calls.
2. Implement charts for performance and asset allocation.
3. Add portfolio edit/delete UI once backend endpoints are ready.
4. Improve form validation and user feedback (toast/inline errors).

### Database Engineer (MySQL + Migration)
Completed ownership:
- Initial schema migration (`V1__init_schema.sql`).
- Runtime datasource/flyway alignment.

Remaining tasks:
1. Add migration for indexes and constraints tuning (e.g., ticker lookup, portfolio_id index review).
2. Define deletion policy (`ON DELETE CASCADE` vs app-level restriction) and migrate accordingly.
3. Add seed data migration/script for demo and testing.
4. Document local DB bootstrap and environment-based credentials strategy.

## Suggested Execution Order
1. Backend A: portfolio update/delete APIs + tests.
2. Database: deletion/index strategy migration.
3. Backend B: Swagger/OpenAPI + validation test hardening.
4. Frontend: edit/delete portfolio + charts + UX states.

