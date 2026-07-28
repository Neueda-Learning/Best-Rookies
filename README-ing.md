# README-ing（实现进度检查 v2）

## 范围
本文件对比 `Best-Rookies/` 当前代码与 `README.md`、`PROJECT-MVP.md` 的需求，并给出后续优化方向。

## 当前验证状态
- 后端：`mvn test` 可通过（本地最近一次验证时间：2026-07-28）。
- 前端：存在完整 Vue 页面与组件结构，可执行 `npm run build` / `npm run dev` 进行验证。

## 需求对比（按当前代码）

### 已完成

#### 1) 核心 REST API 闭环
- 组合：创建、列表、详情、摘要、更新、删除。
- 持仓：创建、列表、更新、删除。

证据：
- `backend/src/main/java/com/bestrookies/portfolio/controller/PortfolioController.java`
- `backend/src/main/java/com/bestrookies/portfolio/controller/PositionController.java`
- `backend/src/main/java/com/bestrookies/portfolio/service/PortfolioService.java`
- `backend/src/main/java/com/bestrookies/portfolio/service/PositionService.java`

#### 2) 列表分页与排序（后端）
- `GET /api/v1/portfolios`、`GET /api/v1/positions` 已支持 `Pageable`。
- 返回分页响应头（`X-Page-Number`、`X-Total-Elements` 等）。

证据：
- `backend/src/main/java/com/bestrookies/portfolio/controller/PortfolioController.java`
- `backend/src/main/java/com/bestrookies/portfolio/controller/PositionController.java`
- `backend/src/main/java/com/bestrookies/portfolio/controller/PaginationHeaders.java`

#### 3) OpenAPI/Swagger 基础接入
- 已引入 Springdoc 依赖。
- 控制器已有 `@Tag`、`@Operation`、`@ApiResponses` 注解。

证据：
- `backend/pom.xml`
- `backend/src/main/java/com/bestrookies/portfolio/controller/PortfolioController.java`
- `backend/src/main/java/com/bestrookies/portfolio/controller/PositionController.java`

#### 4) 前端最小工作流 + 基础图表
- 已实现组合浏览、详情、添加持仓、删除持仓。
- 已实现资产占比环图与基线趋势图。

证据：
- `frontend/src/views/PortfolioListView.vue`
- `frontend/src/views/PortfolioDetailView.vue`
- `frontend/src/components/PositionForm.vue`
- `frontend/src/components/AllocationDonutChart.vue`
- `frontend/src/components/TrendLineChart.vue`

#### 5) 数据库与迁移
- MySQL 数据源配置完成。
- Flyway `V1~V4` 迁移已存在（含索引和价格快照表）。

证据：
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/db/migration/V1__init_schema.sql`
- `backend/src/main/resources/db/migration/V2__add_indexes.sql`
- `backend/src/main/resources/db/migration/V3__add_price_snapshots.sql`
- `backend/src/main/resources/db/migration/V4__extend_asset_type.sql`

### 部分完成

#### 1) 行情基础对接
- 已有外部行情获取服务和价格快照落库能力。
- 但缺少独立行情 API、调度刷新策略、失败重试和可观测性指标。

证据：
- `backend/src/main/java/com/bestrookies/portfolio/service/MarketPriceService.java`
- `backend/src/main/java/com/bestrookies/portfolio/service/PriceSnapshotService.java`

#### 2) 完整收益计算
- `summary` 已返回 `marketValue`、`unrealizedPnL`。
- 当前估值存在回退逻辑（取不到行情时使用 `avgCost`），仍属于“基础收益计算”，不是完整收益体系。

证据：
- `backend/src/main/java/com/bestrookies/portfolio/dto/PortfolioSummaryResponse.java`
- `backend/src/main/java/com/bestrookies/portfolio/service/PortfolioService.java`

#### 3) API 文档质量
- 已有 Swagger 基础接入。
- 仍缺少统一示例请求/响应、错误码约定页、分页参数说明页。

### 未完成

#### 1) README 工程协作要求
- 仓库文档中缺少分支策略、PR 证据、评审流程记录。

#### 2) 前端组合编辑/删除能力
- 后端接口已具备，前端尚未提供组合级编辑/删除入口。

#### 3) README 附录 E（拉伸目标）
- AI 能力：未实现。
- 量子计算原型：未实现。

## 4 人工作分工（已完成 + 下一步）

### 后端工程师 A（组合领域）
已完成：
- 组合 CRUD（含 `PATCH` / `DELETE`）与摘要接口。
- 更新参数校验与异常路径测试。

下一步建议（优化类）：
1. 为组合删除增加“软删除或保护策略”评估文档。
2. 为组合更新补充幂等性与并发更新策略（如版本号）。

### 后端工程师 B（持仓 + API 质量 + 行情）
已完成：
- 持仓 CRUD、分页接口基础、Swagger 注解基础。
- 行情服务基础接入（外部拉取 + 快照存储）。

下一步建议（主责）：
1. 完成“完整收益计算”规则（汇率、缺失行情处理、口径统一）。
2. 增加行情刷新任务（定时拉取、重试、熔断、限流）。
3. 完善 OpenAPI 文档（示例、错误码、分页参数）。
4. 补强负载与边界测试（无行情、脏数据、并发写入）。

### 前端工程师（Vue UI）
已完成：
- 组合列表/详情、持仓新增删除、基础图表与状态面板。

下一步建议：
1. 接入组合编辑/删除 UI（对齐后端接口）。
2. 区分“成本趋势”与“真实收益趋势”图例与提示。
3. 加入分页参数联动（当前/总页数、下一页加载）。

### 数据库工程师（MySQL + 迁移）
已完成：
- 初始化表结构、索引、价格快照表、资产类型扩展迁移。

下一步建议：
1. 为快照表增加唯一性与去重策略（ticker + time bucket）。
2. 评估高频写入下的索引与归档策略。
3. 准备演示数据脚本与环境化凭证模板（dev/test/prod）。

## 可优化功能清单（优先级）

### P0（优先马上做）
1. 完整收益计算口径统一：成本、现值、未实现收益、收益率。
2. 前端接入组合编辑/删除，补全 MVP 操作闭环。
3. OpenAPI 文档补齐示例与错误码说明，便于联调和演示。

### P1（本周可推进）
1. 行情抓取任务化（定时刷新 + 重试 + 限流）。
2. 前端分页交互（列表接口已支持分页，UI 需承接）。
3. 增加监控日志字段（请求耗时、外部行情成功率、回退命中率）。

### P2（演示增强）
1. 导出 CSV/分享功能从“占位按钮”升级为真实功能。
2. 增加历史收益曲线（基于 `price_snapshots`）。
3. AI/量子附录做最小 PoC（哪怕是 notebook 级演示）。

## 建议执行顺序（更新）
1. 后端 B + 数据库：完成完整收益计算与行情刷新链路。
2. 前端：接入组合编辑/删除与真实收益图表口径。
3. 后端 B：补 OpenAPI 细节与边界测试。
4. 全员：补齐分支/PR 文档证据与演示材料。

