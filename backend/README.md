# Backend (Spring Boot)

这是第一阶段 MVP 后端，提供投资组合和持仓的基础 REST API。

## 1. 环境准备

- JDK 17+
- Maven 3.9+
- MySQL 8+

推荐通过环境变量覆盖本地默认数据源配置：

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## 2. 创建数据库

```sql
CREATE DATABASE portfolio_db;
```

> 默认连接信息在 `src/main/resources/application.yml`，并且可以通过上面的环境变量覆盖。

如果你想快速初始化演示数据，可以在 Flyway 迁移后手工执行：

```powershell
mysql -u root -p portfolio_db < src/main/resources/db/seed-demo.sql
```

## 3. 运行后端

```powershell
cd C:\Users\Administrator\Best-Rookies\backend
mvn spring-boot:run
```

## 4. 运行测试

```powershell
cd C:\Users\Administrator\Best-Rookies\backend
mvn test
```

## 5. 核心 API

- `GET /api/v1/health`
- `POST /api/v1/portfolios`
- `GET /api/v1/portfolios?page=0&size=20&sort=id,asc`
- `GET /api/v1/portfolios/{id}`
- `GET /api/v1/portfolios/{id}/summary`
- `PATCH /api/v1/portfolios/{id}`
- `DELETE /api/v1/portfolios/{id}`
- `POST /api/v1/positions`
- `GET /api/v1/positions?portfolioId=1&page=0&size=20&sort=id,asc`
- `PATCH /api/v1/positions/{id}`
- `DELETE /api/v1/positions/{id}`
- `GET /api/v1/prices/{ticker}/latest`

## 6. API 文档

启动后可以访问：

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

