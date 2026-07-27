# README-ing（实现进度检查）

## 范围
本文件对比 `Best-Rookies/` 中的当前代码与 `README.md` 和 `PROJECT-MVP.md` 中的需求。

## 快速健康检查（已验证）
- 后端测试：`backend` 通过 `mvn test` 测试。
- 前端构建：`frontend` 通过 `npm run build` 构建。
- 清理冗余输出：`backend/target/`、`frontend/dist/`、`.idea/`。
- 添加忽略规则：`.gitignore`。

## 需求对比

### 已完成的需求

#### 核心 API（保存和检索组合记录）
- 创建组合：`POST /api/v1/portfolios`。
- 列出组合：`GET /api/v1/portfolios`。
- 获取组合详情：`GET /api/v1/portfolios/{id}`。
- 组合摘要（总持仓数 + 总成本）：`GET /api/v1/portfolios/{id}/summary`。
- 创建持仓：`POST /api/v1/positions`。
- 列出持仓：`GET /api/v1/positions?portfolioId={id}`。
- 删除持仓：`DELETE /api/v1/positions/{id}`。
- 更新持仓：`PATCH /api/v1/positions/{id}`。

证据：
- `backend/src/main/java/com/bestrookies/portfolio/controller/PortfolioController.java`
- `backend/src/main/java/com/bestrookies/portfolio/controller/PositionController.java`
- `backend/src/main/java/com/bestrookies/portfolio/service/PortfolioService.java`

#### 前端最小工作流
- 浏览组合。
- 打开组合详情。
- 添加持仓。
- 删除持仓。

证据：
- `frontend/src/views/PortfolioListView.vue`
- `frontend/src/views/PortfolioDetailView.vue`
- `frontend/src/components/PositionForm.vue`

#### 数据持久化 / 数据库
- MySQL 数据源已配置。
- Flyway 初始化迁移脚本存在且已启用。

证据：
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/db/migration/V1__init_schema.sql`

#### 测试基线
- 集成测试包含创建组合、创建持仓、查询摘要。

证据：
- `backend/src/test/java/com/bestrookies/portfolio/PortfolioApiIntegrationTest.java`

### 未完成 / 部分完成的需求

#### README.md（高优先级前端目标）
- 组合性能可视化（图表）未实现。

#### README.md（推荐工程目标）
- API 使用文档（Swagger/OpenAPI）未实现。
- 团队工作流产物（分支/PR 证据）未在仓库文档中体现。

#### PROJECT-MVP.md 后续步骤
- `PUT/PATCH /portfolios/{id}` 未实现。
- `DELETE /portfolios/{id}` 未实现。
- 行情数据集成（Yahoo/样例 API）未实现。
- 前端图表（性能/资产占比）未实现。
- Swagger/OpenAPI 未实现。

#### 拉伸目标（README 附录 E）
- AI 功能：未实现。
- 量子计算原型：未实现。

## 本次检查修复的代码问题
- 更新开发环境 CORS 规则，支持 `localhost` 和 `127.0.0.1` 的灵活端口：
  - `backend/src/main/java/com/bestrookies/portfolio/config/WebConfig.java`
- 添加根目录 `.gitignore` 以避免提交生成的产物。
- 删除工作区中冗余生成的目录：
  - `backend/target/`
  - `frontend/dist/`
  - `.idea/`

## 4 人工作分工（已完成 + 待完成）

### 后端工程师 A（组合领域）
已完成职责：
- 组合创建/列出/详情/摘要 API。
- 服务层摘要计算。
- ✅ **PATCH /api/v1/portfolios/{id}**（名称/基础币种更新）。
- ✅ **DELETE /api/v1/portfolios/{id}**（级联删除所有关联持仓）。
- ✅ 为更新 DTO 添加验证和业务错误消息。
- ✅ 添加了 6 个集成测试覆盖所有场景（创建、更新、删除、验证、异常）。

证据：
- `backend/src/main/java/.../dto/PortfolioUpdateRequest.java`（新建）
- `backend/src/main/java/.../service/PortfolioService.java`（新增 updatePortfolio、deletePortfolio 方法）
- `backend/src/main/java/.../controller/PortfolioController.java`（新增 @PatchMapping、@DeleteMapping）
- `backend/src/test/java/.../PortfolioApiIntegrationTest.java`（升级至 6 个测试用例）

待完成任务：
无（已全部完成）✅

### 后端工程师 B（持仓 + API 质量）
已完成职责：
- 持仓创建/列出/更新/删除 API。
- 全局异常处理基线。

待完成任务：
1. 为列表端点添加分页/排序。
2. 为所有端点引入 OpenAPI/Swagger 文档。
3. 添加端点级示例和标准化错误响应模式文档。
4. 为无效负载和验证边界条件添加测试用例。

### 前端工程师（Vue UI）
已完成职责：
- 组合列表和详情页面。
- 与后端的持仓创建/删除交互。

待完成任务：
1. 为所有异步调用添加错误处理/加载/空状态。
2. 实现性能和资产占比图表。
3. 在后端端点就绪后添加组合编辑/删除 UI。
4. 改进表单验证和用户反馈（提示/内联错误）。

### 数据库工程师（MySQL + 迁移）
已完成职责：
- 初始化架构迁移（`V1__init_schema.sql`）。
- 运行时数据源/Flyway 对齐。

待完成任务：
1. 为索引和约束优化添加迁移（例如 Ticker 查询、portfolio_id 索引审查）。
2. 定义删除政策（`ON DELETE CASCADE` vs 应用层限制）并相应迁移。
3. 添加演示数据迁移/脚本供演示和测试使用。
4. 记录本地数据库启动和基于环境的凭证策略。

## 建议执行顺序
1. 后端 A：组合更新/删除 API + 测试。
2. 数据库：删除/索引策略迁移。
3. 后端 B：Swagger/OpenAPI + 验证测试强化。
4. 前端：编辑/删除组合 + 图表 + UX 状态。

