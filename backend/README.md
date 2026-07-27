# Backend (Spring Boot)

这是第一阶段 MVP 后端，提供投资组合和持仓的基础 REST API。

## 1. 环境准备

- JDK 17+
- Maven 3.9+
- MySQL 8+

## 2. 创建数据库

```sql
CREATE DATABASE portfolio_db;
```

> 默认连接信息在 `src/main/resources/application.yml`：`root/root`。
> 你可以按本机实际情况修改。

## 3. 运行后端

```powershell
cd C:\Users\Administrator\Desktop\Best-Rookies\backend
mvn spring-boot:run
```

## 4. 运行测试

```powershell
cd C:\Users\Administrator\Desktop\Best-Rookies\backend
mvn test
```

## 5. 核心 API

- `GET /api/v1/health`
- `POST /api/v1/portfolios`
- `GET /api/v1/portfolios`
- `GET /api/v1/portfolios/{id}`
- `GET /api/v1/portfolios/{id}/summary`
- `POST /api/v1/positions`
- `GET /api/v1/positions?portfolioId=1`
- `PATCH /api/v1/positions/{id}`
- `DELETE /api/v1/positions/{id}`

