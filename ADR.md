# Architecture Decision Records（架构决策记录）

> 记录 Portfolio Manager 项目的关键技术决策，格式：背景 → 决策 → 原因。

---

## ADR-001 后端框架：Spring Boot 3.3.2 + Java 17
**决策**：使用 Spring Boot 3.3.2。  
**原因**：培训使用的技术栈，团队熟悉，JPA/Validation/Test 开箱即用。

---

## ADR-002 前端框架：Vue 3 + Vite
**决策**：使用 Vue 3 + Vite，绑定 `127.0.0.1:5173`。  
**原因**：培训使用的技术栈，Vite 热更新快，组件化开发清晰。

---

## ADR-003 数据库：MySQL 8
**决策**：开发/生产用 MySQL 8，测试用 H2 内存库。  
**原因**：MySQL 是培训数据库；H2 让测试无需本地 MySQL 依赖，`mvn test` 可独立运行。

---

## ADR-004 数据库迁移：Flyway
**决策**：使用 Flyway 管理 Schema，脚本放在 `db/migration/V{n}__*.sql`。  
**原因**：Spring Boot 原生支持，Schema 变更可版本控制，避免手动 ALTER TABLE。

---

## ADR-005 数据模型：两张表（portfolio + position）
**决策**：`portfolios` 一对多 `positions`，通过外键关联。  
**原因**：讲师建议，符合业务语义，扩展性更好；删除组合时需先删关联持仓。

---

## ADR-006 API 版本化：URL 前缀 `/api/v1`
**决策**：所有接口统一加 `/api/v1/` 前缀。  
**原因**：最直观，便于测试和文档，未来版本升级不破坏现有调用方。

---

## ADR-007 API 文档：SpringDoc OpenAPI（Swagger UI）
**决策**：引入 `springdoc-openapi-starter-webmvc-ui:2.6.0`。  
**原因**：讲师要求可在浏览器直接测试接口（US-007）；访问地址 `/swagger-ui/index.html`。

---

## ADR-008 跨域：后端统一 CORS 配置
**决策**：在 `WebConfig.java` 放行 `localhost:5173` 和 `127.0.0.1:5173`。  
**原因**：前后端不同端口，集中配置比每个接口加 `@CrossOrigin` 更易维护。

---

## ADR-009 认证：无认证，单用户
**决策**：不引入 Spring Security，所有接口公开。  
**原因**：讲师明确说明单用户假设，省时间做核心功能（见 `meeting-notes-kickoff.md`）。

---

## ADR-010 行情价格：外部 API + 买入价回退
**决策**：优先调外部行情 API，失败时自动回退到 `avgBuyPrice`，不报错。  
**原因**：US-009 要求；外部 API 仅支持 5 个 Ticker，回退保证 summary 接口始终可用。

---

## ADR-011 错误响应：统一 JSON 格式
**决策**：用 `@RestControllerAdvice` 统一返回 `{ timestamp, status, error, message }`。  
**原因**：前端统一处理错误提示（US-008），不暴露 Java 异常堆栈。

---

*最后更新：2026-07-28 | Best-Rookies Team*
