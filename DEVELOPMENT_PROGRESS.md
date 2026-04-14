# 开发任务推进记录（2026-04-13）

## 本轮已完成
- 新增认证登出接口：`POST /api/auth/logout`
- 新增用户管理后端接口（管理员）：
  - `GET /api/users`
  - `GET /api/users/{id}`
  - `POST /api/users`
  - `PUT /api/users/{id}`
  - `DELETE /api/users/{id}`
  - `PUT /api/users/{id}/resetPwd`
- 新增用户管理前端页面与路由：
  - 路由：`/home/users`
  - 页面：用户分页、筛选、新增、编辑、删除、重置密码
- 新增报表导出接口：`GET /api/reports/export`（当前为 CSV）
- 新增操作日志模块：
  - AOP 自动记录接口访问到 `operation_log`
  - `GET /api/operation-logs` 分页查询
  - `GET /api/operation-logs/export` 导出 CSV
  - 前端日志页面与路由：`/home/operation-logs`
- 新增菜单管理模块：
  - `GET /api/menus` / `GET /api/menus/{id}`
  - `POST /api/menus` / `PUT /api/menus/{id}` / `DELETE /api/menus/{id}`
  - 前端菜单管理页面与路由：`/home/menus`
- 新增角色菜单关联能力：
  - `GET /api/roles/{id}/menus`
  - `PUT /api/roles/{id}/menus`
  - `GET /api/roles/current/menus`
  - 角色页新增“分配菜单”功能
  - 首页支持按当前用户角色菜单进行可见导航过滤（接口无数据时自动兜底显示全菜单）
- 顶部当前用户显示优化为：`昵称（用户名）`
- 前端登出改为调用后端 `logout`，并保留本地 token 清理兜底
- README 已同步新增接口清单

## 运行验证
- 后端：`mvn -DskipTests compile` 通过
- 前端：`npm run build` 通过

## 仍待开发（按设计文档差距）
- 系统管理：
  - 角色菜单关联（`role_menu`）与按角色动态菜单下发（已完成）
- 基础数据：
  - 往来单位管理
  - 结算方式管理
  - 账套期间（开账/结账）
- 账簿管理：
  - 总账、明细账、科目余额表、序时账查询
- 报表：
  - 导出能力升级为 Excel（当前为 CSV）
- 费用报销：
  - 附件上传
  - 关联凭证“一键生成”联动流程

## 当前估算完成度
- 核心可运行主链路（登录/注册、菜单、角色、用户、凭证、报表查询/导出、报销、日志）：约 **85%-88%**
- 对照完整设计文档（含账簿、附件、Excel 导出等）：约 **70%-75%**
