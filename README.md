# 企业财务管理系统（Spring Boot + MyBatis-Plus + Vue 3）

## 数据库初始化
1. 在 MySQL 8.0 中创建 `finance` 数据库。
2. 只执行 `create_tables.sql`。
3. `create_tables.sql` 已包含注册功能所需的库表设计说明、默认角色数据，以及最小可用的会计科目初始化数据。
4. 注册功能依赖的核心库表如下：
- `user`：用户基础信息、密码、邮箱、手机号、部门、状态
- `role`：角色定义
- `user_role`：用户与角色关联
5. 新注册用户默认绑定 `EMPLOYEE` 角色。

## 后端启动
1. 修改 `backend/src/main/resources/application.yml` 中的数据库连接和 JWT 密钥。
2. 在 `backend` 目录执行：

```bash
mvn -DskipTests compile
```

3. 运行 `com.enterprise.finance.FinanceApplication`。

后端默认端口为 `8080`。

## 前端启动
在 `frontend` 目录执行：

```bash
npm install
npm run dev
```

前端默认请求 `http://localhost:8080`。

## 默认管理员凭证
系统初始化后，可使用以下凭证登陆：
- **用户名**：`admin02`
- **密码**：`admin123`
- **角色**：`ADMIN`

## 认证接口
- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/logout`
- `GET /api/auth/info`

## 已实现业务接口
- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/logout`
- `GET /api/auth/info`
- `GET /api/users` - 分页查询用户列表
- `GET /api/users/{id}` - 获取用户详情
- `POST /api/users` - 创建用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户
- `PUT /api/users/{id}/resetPwd` - 重置用户密码
- `GET /api/operation-logs` - 分页查询操作日志
- `GET /api/operation-logs/export` - 导出操作日志（CSV）
- `GET /api/menus` - 查询菜单列表
- `GET /api/menus/{id}` - 查询菜单详情
- `POST /api/menus` - 创建菜单
- `PUT /api/menus/{id}` - 更新菜单
- `DELETE /api/menus/{id}` - 删除菜单
- `GET /api/roles` - 分页查询角色列表
- `POST /api/roles` - 创建角色
- `PUT /api/roles/{id}` - 更新角色
- `DELETE /api/roles/{id}` - 删除角色
- `GET /api/roles/{id}` - 获取角色详情
- `GET /api/roles/check-code/{roleCode}` - 检查角色编码唯一性
- `GET /api/roles/{id}/menus` - 查询角色菜单ID列表
- `PUT /api/roles/{id}/menus` - 分配角色菜单
- `GET /api/roles/current/menus` - 获取当前登录用户可见菜单
- `POST /api/vouchers`
- `GET /api/vouchers`
- `GET /api/vouchers/{id}`
- `PUT /api/vouchers/{id}/audit`
- `PUT /api/vouchers/{id}/bookkeep`
- `GET /api/reports/balance-sheet?period=YYYYMM`
- `GET /api/reports/profit-statement?period=YYYYMM`
- `POST /api/expense-claims`
- `GET /api/expense-claims`
- `GET /api/expense-claims/{id}`
- `PUT /api/expense-claims/{id}/submit`
- `PUT /api/expense-claims/{id}/approve`
- `PUT /api/expense-claims/{id}/pay`

## 注册功能设计说明
- 在不改变现有系统整体架构的前提下，注册功能扩展在认证模块中实现。
- 注册成功后，系统会往 `user` 表插入用户记录，并往 `user_role` 表写入默认角色关系。
- 凭证、报表、费用报销等原有模块保持现有目录结构和接口风格不变。
