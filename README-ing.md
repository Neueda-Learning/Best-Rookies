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
- **Update portfolio** (name/baseCurrency): `PATCH /api/v1/portfolios/{id}`. *(new)*
- **Delete portfolio** (cascade): `DELETE /api/v1/portfolios/{id}`. *(new)*
- Create position: `POST /api/v1/positions`.
- List positions: `GET /api/v1/positions?portfolioId={id}`.
- Delete position: `DELETE /api/v1/positions/{id}`.
- Patch position: `PATCH /api/v1/positions/{id}`.
- **Get latest price snapshot**: `GET /api/v1/prices/{ticker}/latest`. *(new)*

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
- Flyway migration V2: performance indexes on `positions(portfolio_id)` and `positions(ticker)`.
- Flyway migration V3: `price_snapshots` table for storing market price data (supports performance/P&L calculation).
- Flyway migration V4: `asset_type` column documented for extended enum values (ETF, FUND, CRYPTO).
- `AssetType` enum extended with `ETF`, `FUND`, `CRYPTO` in addition to original `STOCK`, `BOND`, `CASH`.
- `PATCH /api/v1/portfolios/{id}` implemented (name / baseCurrency update).
- `DELETE /api/v1/portfolios/{id}` implemented (cascades to positions via JPA `CascadeType.ALL`).
- `GET /api/v1/prices/{ticker}/latest` implemented — returns most recent price snapshot for a ticker.
- Full backend layer added for price snapshots: `PriceSnapshot` entity, `PriceSnapshotRepository`, `PriceSnapshotService`, `PriceSnapshotController`.

Evidence:
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/db/migration/V1__init_schema.sql`
- `backend/src/main/resources/db/migration/V2__add_indexes.sql`
- `backend/src/main/resources/db/migration/V3__add_price_snapshots.sql`
- `backend/src/main/resources/db/migration/V4__extend_asset_type.sql`
- `backend/src/main/java/com/bestrookies/portfolio/entity/AssetType.java`
- `backend/src/main/java/com/bestrookies/portfolio/entity/PriceSnapshot.java`
- `backend/src/main/java/com/bestrookies/portfolio/repository/PriceSnapshotRepository.java`
- `backend/src/main/java/com/bestrookies/portfolio/service/PriceSnapshotService.java`
- `backend/src/main/java/com/bestrookies/portfolio/controller/PriceSnapshotController.java`
- `backend/src/main/java/com/bestrookies/portfolio/dto/PortfolioUpdateRequest.java`

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
- ~~`PUT/PATCH /portfolios/{id}` not implemented.~~ **Done: `PATCH /api/v1/portfolios/{id}`.**
- ~~`DELETE /portfolios/{id}` not implemented.~~ **Done: `DELETE /api/v1/portfolios/{id}`.**
- Market data integration (Yahoo/sample API) not implemented — `price_snapshots` table and API endpoint ready; external fetch/scheduler not yet wired.
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
- **`PATCH /api/v1/portfolios/{id}`** — name and baseCurrency update with PATCH semantics.
- **`DELETE /api/v1/portfolios/{id}`** — cascades to all child positions.

Remaining tasks:
1. ~~Implement `PATCH /api/v1/portfolios/{id}`~~ **Done.**
2. ~~Implement `DELETE /api/v1/portfolios/{id}`~~ **Done.**
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
- **V2**: Performance indexes on `positions(portfolio_id)` and `positions(ticker)`.
- **V3**: `price_snapshots` table with ticker/fetched_at indexes for market price storage.
- **V4**: Column definition update documenting extended `AssetType` enum (ETF, FUND, CRYPTO).
- **Full JPA backend layer** for `price_snapshots`: entity, repository, service, controller, DTO.
- **`ON DELETE CASCADE`** cascade strategy implemented via JPA `CascadeType.ALL` on `Portfolio.positions`.

Remaining tasks:
1. ~~Add migration for indexes and constraints tuning~~ **Done in V2.**
2. ~~Define deletion policy and migrate accordingly~~ **Done via JPA cascade.**
3. Add seed data migration/script for demo and testing.
4. Document local DB bootstrap and environment-based credentials strategy.
5. Wire external price fetch (Yahoo Finance / sample API) to `PriceSnapshotService.saveSnapshot()` via scheduled job (`@Scheduled`).

## Suggested Execution Order
1. Backend A: portfolio update/delete APIs + tests.
2. Database: deletion/index strategy migration.
3. Backend B: Swagger/OpenAPI + validation test hardening.
4. Frontend: edit/delete portfolio + charts + UX states.

