<!-- 此文件是前端项目说明文档，用于介绍运行方式、依赖和构建命令。 -->
# Frontend (Vue 3)

这是一个面向真实项目体验完善过的 Vue 3 前端，覆盖以下能力：

- Portfolio 列表页与详情页
- Position 新增与删除
- 异步请求的 loading、error、empty 状态
- 表单即时校验、内联错误提示、toast 反馈
- 资产配置图表与成本趋势图表
- 统一的 API 封装与可配置后端地址

## 环境要求

- Node.js 18+
- npm 9+
- 可访问的后端服务，默认地址为 `http://localhost:8080/api/v1`

## 安装依赖

```powershell
cd C:\Course\Best-Rookies\frontend
npm install
```

## 启动开发环境

```powershell
cd C:\Course\Best-Rookies\frontend
npm run dev
```

默认访问地址：`http://localhost:5173`

如果你使用后端的本地联调 profile，推荐先在另一个终端执行：

```powershell
cd C:\Course\Best-Rookies\backend
$env:SPRING_PROFILES_ACTIVE='local'
mvn spring-boot:run
```

前端默认就会请求：`http://localhost:8080/api/v1`

## 构建生产包

```powershell
cd C:\Course\Best-Rookies\frontend
npm run build
```

## 环境变量

如果后端地址不是默认值，可以创建 `.env` 文件并设置：

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## 说明

- 详情页的 “Performance baseline” 图表当前基于持仓成本累计趋势绘制。
- 真正的收益率或市值表现，需要后端提供价格、估值或历史净值相关接口后再进一步增强。
- Portfolio 编辑和删除按钮已经在 UI 中预留，后端接口就绪后即可接入。

