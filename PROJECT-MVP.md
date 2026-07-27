# Portfolio Manager MVP (Spring Boot + Vue3 + MySQL)

该版本是从 0 到 1 的基础功能实现，重点是先跑通最小闭环：

1. 创建组合
2. 查看组合
3. 创建持仓
4. 删除持仓
5. 查看组合总持仓数和总成本

## 目录结构

- `backend/` Spring Boot API
- `frontend/` Vue3 页面

## 下一步建议

1. 增加 `PUT/PATCH /portfolios/{id}` 和 `DELETE /portfolios/{id}`。
2. 接入行情数据（Yahoo 或你们提供的样例 API）。
3. 在前端增加图表（收益走势、资产占比）。
4. 增加 Swagger/OpenAPI 文档。

