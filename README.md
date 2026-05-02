# Coflxl API Platform (动态 API 开放平台)

## 1. 项目简介
Coflxl API Platform 是一个基于数据驱动的动态 API 生成与管理平台。它允许开发者或实施人员通过编写 SQL 语句，快速发布为标准的 HTTP API 接口，无需编写任何 Java 代码。平台支持多数据源管理、多系统归属管理、API 动态发布与下线、SQL 在线调试、接口调用日志记录、**主题切换**、**面包屑导航**和 **Element Plus 组件国际化**。

![首页](doc/首页.png)

## 2. 技术栈
*   **后端**: Java 17+, Spring Boot 3, SqlToy (ORM), H2 (内置数据库，支持扩展 MySQL/Oracle/PostgreSQL)
*   **前端**: Vue 3, Vite, TypeScript, Element Plus, Tailwind CSS, Vue Router, Monaco Editor (SQL 编辑器)

## 3. 项目结构

```text
/
├── coflxl-api-platform/                              # 后端工程 (Spring Boot 3.2.3)
│   ├── coflxl-api-platform-admin/                    # 管理后台模块 (提供系统、数据源、API 管理接口)
│   │   ├── src/main/java/com/coflxl/api/admin/
│   │   │   ├── controller/                           # 控制器层
│   │   │   │   ├── auth/                             # 认证授权控制器
│   │   │   │   ├── system/                           # 系统管理控制器 (用户、角色、菜单、部门、数据源、代码生成等)
│   │   │   │   ├── api/                              # API 定义与调用日志控制器
│   │   │   │   ├── report/                           # 报表设计器与实例控制器
│   │   │   │   ├── wf/                               # 工作流控制器 (Flowable)
│   │   │   │   └── yzt/                              # 通用查询控制器
│   │   │   ├── service/                              # 业务服务层 (代码生成、报表执行等)
│   │   │   ├── config/                               # 配置类 (拦截器、AOP)
│   │   │   ├── aspect/                               # 切面 (操作日志)
│   │   │   └── dto/                                  # 数据传输对象
│   │   └── src/main/resources/
│   │       ├── templates/codegen/                    # 代码生成模板 (Controller, Entity, Vue 等)
│   │       └── application*.yml                      # 配置文件 (继承自父工程)
│   │
│   ├── coflxl-api-platform-common/                   # 公共模块
│   │   └── src/main/java/com/coflxl/api/common/
│   │       ├── response/                             # 统一响应结构 (ApiResponse)
│   │       ├── exception/                            # 全局异常定义 (ApiException)
│   │       ├── enums/                                # 枚举 (错误码等)
│   │       ├── utils/                                # 工具类 (JWT 工具、ID 生成器等)
│   │       └── context/                              # 用户上下文持有者
│   │
│   ├── coflxl-api-platform-core/                     # 核心模块 (无 Web 依赖，作为库)
│   │   └── src/main/java/com/coflxl/api/core/
│   │       ├── domain/entity/                        # JPA 实体类 (数据源、API 定义、用户、角色、工作流等)
│   │       ├── datasource/                           # 动态多数据源核心逻辑
│   │       │   ├── DynamicRoutingDataSource.java     # 运行时数据源路由器
│   │       │   ├── DataSourceManager.java            # 数据源连接池管理
│   │       │   └── DynamicDataSourceContextHolder.java # 数据源上下文切换
│   │       ├── config/                               # SqlToy 配置、统一字段处理器
│   │       └── service/                              # 核心服务 (API 执行器)
│   │
│   └── coflxl-api-platform-gateway/                  # 网关/执行模块 (Spring Cloud Gateway 或独立 Web 服务)
│       ├── src/main/java/com/coflxl/api/gateway/
│       │   ├── GatewayApplication.java               # 启动类
│       │   ├── controller/                           # 开放 API 入口 (OpenApiExecuteController)
│       │   └── exception/                            # 全局异常处理
│       └── src/main/resources/
│           ├── application.yml                       # 主配置文件
│           ├── application-dev.yml                   # 开发环境配置 (H2 数据库)
│           ├── application-glj.yml                   # 生产环境配置 (外部数据库)
│           ├── schema.sql                            # 数据库初始化脚本
│           └── target_schema.sql                     # 目标库表结构示例
│
└── coflxl-front/                                     # 前端工程 (Vue 3 + Vite + TypeScript + Element Plus)
    ├── src/
    │   ├── main.ts                                   # 入口文件 (配置 Element Plus 中文语言包、路由等)
    │   ├── App.vue                                   # 根组件 (主题切换与布局)
    │   ├── index.css                                 # 全局样式 (CSS 变量定义主题)
    │   ├── components/                               # 公共组件
    │   │   ├── ProTable.vue                          # 高级表格组件
    │   │   ├── MonacoEditor.vue                      # SQL 编辑器 (Monaco Editor)
    │   │   ├── Breadcrumb.vue                        # 面包屑导航
    │   │   ├── DynamicTableColumn.vue                # 动态列配置
    │   │   └── ChartRenderer.vue                     # 图表渲染器
    │   ├── views/                                    # 页面视图
    │   │   ├── Login.vue                             # 登录页
    │   │   ├── Home.vue                              # 主页布局
    │   │   ├── api/                                  # 接口管理模块
    │   │   │   ├── ApiManage.vue                     # 接口发布管理
    │   │   │   ├── SqlWorkbench.vue                  # SQL 工作台
    │   │   │   └── CallLog.vue                       # 调用日志
    │   │   ├── system/                               # 系统管理模块
    │   │   │   ├── SystemManage.vue                  # 系统管理
    │   │   │   ├── DataSourceManage.vue              # 数据源管理
    │   │   │   ├── CodeGen.vue                       # 代码生成器
    │   │   │   ├── UserManage.vue                    # 用户管理
    │   │   │   ├── RoleManage.vue                    # 角色管理
    │   │   │   ├── MenuManage.vue                    # 菜单管理
    │   │   │   ├── DeptManage.vue                    # 部门管理
    │   │   │   └── OperLog.vue                       # 操作日志
    │   │   ├── report/                               # 报表模块
    │   │   │   ├── ReportTemplateManage.vue          # 报表模板管理
    │   │   │   ├── ReportDesigner.vue                # 报表设计器
    │   │   │   ├── ReportInstanceManage.vue          # 报表实例管理
    │   │   │   └── ReportViewer.vue                  # 报表查看器
    │   │   ├── workflow/                             # 工作流模块
    │   │   │   ├── WfDefManage.vue                   # 流程定义管理
    │   │   │   ├── ProcessDesigner.vue               # 流程设计器
    │   │   │   ├── ProcessStart.vue                  # 启动流程
    │   │   │   ├── MyTasks.vue                       # 我的任务
    │   │   │   ├── LeaveRequest.vue                  # 请假申请示例
    │   │   │   └── FormManage.vue                    # 表单管理
    │   │   ├── yzt/                                  # 通用查询模块
    │   │   │   └── SysCommonSearchManage.vue
    │   │   └── IFrameView.vue                        # 嵌入式页面容器
    │   ├── router/
    │   │   └── index.ts                              # 路由配置 (含 meta.title 供面包屑)
    │   ├── utils/
    │   │   ├── request.ts                            # Axios 封装 (请求/响应拦截器)
    │   │   ├── format.ts                             # 格式化工具
    │   │   └── permission.ts                         # 权限工具
    │   └── vite-env.d.ts                             # Vite 类型声明
    ├── index.html
    ├── package.json
    └── vite.config.ts
```

## 4. 核心功能模块

### 4.1 系统管理 (System Management)
管理接入平台的各个业务系统（如：财审项目、计划养护系统等）。数据源和 API 接口均可归属到特定的系统下，方便分类检索和维护。

### 4.2 数据源管理 (Data Source Management)
支持动态配置多种类型的数据库连接（MySQL, PostgreSQL, Oracle 等）。平台在执行 API 时，会根据 API 配置的数据源编码，动态切换到目标数据库执行 SQL。

![数据连接](doc/数据连接.png)

### 4.3 接口管理 (API Management)
核心模块。定义 API 的基础信息（路径、请求方式）、关联数据源、编写 SQL 语句（支持 SqlToy 动态 SQL 语法）并定义入参。

![接口管理](doc/接口管理.png)
![接口配置](doc/接口配置.png)
*   **状态流转**: 草稿 (DRAFT) -> 已发布 (PUBLISHED) -> 已下线 (OFFLINE)。
*   **参数自动解析**: 前端支持根据 SQL 文本（如 `:name`）自动提取并同步参数列表。

### 4.4 SQL 工作台 (SQL Workbench)
提供 Monaco Editor 强力支持的 SQL 编写环境，可直接选择已配置的数据源进行 SQL 语句的在线测试和调试。

![接口调试](doc/接口调试.png)

### 4.5 代码生成 (Code Generation)
提供可视化的代码生成工具，支持根据数据库表结构一键生成后端的 Controller、Service、Entity 以及前端的 Vue 页面。极大减少重复劳动，统一项目规范。

![代码生成](doc/代码生成.png)
![代码生成](doc/代码生成1.png)

### 4.6 调用日志 (Call Logs)
记录所有通过网关执行的 API 请求细节。包括请求时间、API 编码、入参、执行耗时、响应状态及异常堆栈，方便进行审计、性能分析及线上问题排查。

![调用日志](doc/调用日志.png)

### 4.7 主题切换 (Theming)
前端支持多套主题（默认深蓝、商务蓝灰、简洁浅色）动态切换，通过修改 `App.vue` 中的 `data-theme` 属性和 `index.css` 中定义的 CSS 变量实现全局样式更新。

### 4.8 面包屑导航 (Breadcrumb Navigation)
应用集成面包屑导航，通过解析 Vue Router 配置中的 `meta.title` 属性，动态生成当前页面的路径层级，提升用户体验和导航清晰度。

### 4.9 Element Plus 国际化 (i18n)
通过在 `main.ts` 中引入并配置 Element Plus 的中文语言包 `zhCn`，实现了对所有 Element Plus 组件（如分页、日期选择器等）的本地化支持。

## 5. 开放接口请求说明 (Open API)

当在“接口管理”中将一个接口状态设置为 **已发布 (PUBLISHED)** 后，外部系统即可调用该接口。

### 5.1 请求地址格式
```http
POST /api/open/{apiCode}
```
*   `{apiCode}`: 在接口管理中定义的唯一接口编码（例如：`getUserInfo`）。

### 5.2 请求头 (Headers)
*   `Content-Type`: `application/json`
*   *(预留)* `Authorization`: `Bearer {token}` (如果接口配置了鉴权)

### 5.3 请求体 (Body)
请求体为 JSON 格式，键值对需与“参数定义”中配置的参数名（`paramCode`）保持一致。

**示例请求:**
```json
{
  "user_name": "张三",
  "user_code": "U1001",
  "pageNo": 1,
  "pageSize": 10
}
```

### 5.4 响应格式 (Response)
接口统一返回标准的 JSON 结构：

**成功响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "user_name": "张三",
      "age": 25
    }
  ]
}
```
*(注：如果配置的操作类型为 `PAGE` 分页查询，`data` 字段将包含 `rows` 和 `recordCount` 等分页属性)*

**失败响应示例:**
```json
{
  "code": 500,
  "message": "接口未发布或不存在",
  "data": null
}
```

## 6. 管理端 API 接口目录 (Admin API)

前端页面与后端管理模块交互的内部接口：

*   **系统管理**:
    *   `GET /admin/system/page` (分页查询)
    *   `GET /admin/system/list` (列表查询)
    *   `POST /admin/system/save` (新增/修改)
    *   `POST /admin/system/delete/{systemCode}` (删除)
*   **数据源管理**:
    *   `GET /admin/data-source/page`
    *   `GET /admin/data-source/list`
    *   `POST /admin/data-source/save`
    *   `POST /admin/data-source/delete/{code}`
*   **接口管理**:
    *   `GET /admin/api/page`
    *   `GET /admin/api/detail/{apiCode}`
    *   `POST /admin/api/save`
    *   `POST /admin/api/publish/{apiCode}`
    *   `POST /admin/api/offline/{apiCode}`
*   **网关测试**:\
    *   `POST /admin/api/test` (工作台 SQL 测试)

## 7. 数据库核心表结构说明

*   `sys_api_system`: 系统配置表
*   `sys_api_data_source`: 数据源配置表
*   `sys_api_definition`: API 基础信息定义表
*   `sys_api_sql_definition`: API 关联的 SQL 脚本定义表
*   `sys_api_param_definition`: API 入参规则定义表
*   `sys_api_call_log`: API 调用日志记录表

## 8. 高级特性与扩展

### 8.1 Flowable 工作流集成

平台内置 Flowable 工作流引擎（版本 7.0.0），支持流程定义、部署、执行和任务管理。通过 `AdminWorkflowController` 提供流程操作接口：

- 流程定义部署与查询
- 流程实例启动与管理
- 任务查询、认领、完成
- 流程变量存取
- 流程图实时追踪

工作流与动态 API 可以结合使用，实现审批流、数据状态流转等业务场景。

### 8.2 JWT 认证与鉴权

基于 `java-jwt`（版本 4.4.0）实现无状态认证：

- **登录接口**：`AuthController.login()` 验证用户名密码，生成 JWT Token
- **Token 解析**：拦截器提取 `Authorization` 头，验证签名和有效期
- **权限控制**：基于角色的访问控制（RBAC），结合 `@PreAuthorize` 注解
- **无状态设计**：服务端不存储 Session，适合集群部署

管理端 API（`/admin/**`）需要携带 Token 访问，开放 API（`/api/open/**`）可根据接口配置决定是否需要鉴权。

### 8.3 SqlToy 动态 SQL 支持

使用 Sagacity SqlToy（版本 5.6.41）作为 ORM 框架，支持：

- **动态 SQL 语法**：`#if`、`#else`、`#foreach` 等条件判断和循环
- **参数自动注入**：SQL 中 `:paramName` 占位符自动映射请求参数
- **分页查询**：自动处理方言分页，返回 `rows` + `recordCount`
- **多数据源适配**：根据不同数据源类型（MySQL、Oracle、PostgreSQL）自动转换 SQL 方言

在接口管理的 SQL 编辑器中可以直接编写动态 SQL，极大增强了接口的灵活性和复用性。

### 8.4 多数据源动态路由

核心模块实现动态数据源路由：

- **数据源注册**：管理员在「数据源管理」页面配置数据库连接信息（URL、用户名、密码、驱动类名）
- **连接池管理**：使用 HikariCP，每个数据源独立连接池
- **动态切换**：API 执行时根据 `api_definition.data_source_code` 字段，运行时获取对应数据源连接
- **连接测试**：提供测试接口验证数据源连通性

支持 MySQL、Oracle、PostgreSQL、H2 等多种数据库。

### 8.5 操作日志记录

区别于 API 调用日志（`sys_api_call_log`），操作日志（`OperLogController`）记录管理后台的用户操作行为：

- **日志内容**：操作人、IP 地址、请求 URL、请求方式、操作方法（类+方法名）、请求参数、执行耗时、操作状态（成功/失败）
- **AOP 切面**：通过 `@Log` 注解标记需要记录的方法，使用 AOP 自动拦截
- **检索与清理**：支持按时间、操作人、操作类型查询日志，配置定时清理策略

操作日志有助于安全审计和问题追溯。

### 8.6 代码生成器详解

`AdminCodeGenController` 提供可视化代码生成：

- **表选择**：前端展示数据库表结构，勾选需要生成的表
- **生成配置**：可配置包名、模块名、作者、父类等
- **生成内容**：Entity（实体类）、Mapper、Service、ServiceImpl、Controller、VO
- **前端页面**：同步生成 Vue3 列表页、表单页、API 调用封装
- **下载**：生成后可下载为 ZIP 压缩包，解压后复制到项目对应模块

使用时先在「数据源管理」选择目标数据源，然后在代码生成页面执行生成操作。

### 8.7 环境配置与部署

#### 开发环境 (application-dev.yml)

- 内置 H2 数据库，自动执行 `schema.sql` 初始化表结构
- 日志级别 DEBUG，输出到控制台
- JWT 密钥使用测试密钥

#### 生产环境 (application-glj.yml)

- 配置外部 MySQL / Oracle 数据源（需在配置文件中设置连接信息）
- 日志级别 INFO，按天滚动输出到文件
- JWT 密钥使用强密码（环境变量注入）

#### 启动命令

后端：
```bash
# 进入后端根目录
cd coflxl-api-platform
# 打包
mvn clean package -DskipTests
# 启动（默认 dev 环境）
java -jar coflxl-api-platform-gateway/target/coflxl-api-platform-gateway.jar
# 指定环境
java -jar coflxl-api-platform-gateway/target/coflxl-api-platform-gateway.jar --spring.profiles.active=glj
```

前端：
```bash
cd coflxl-front
npm install
npm run dev
```

前端打包：
```bash
npm run build
# 产物在 dist/ 目录，可部署到 Nginx
```

### 8.8 构建与运行要求

- **JDK**：17 及以上
- **Maven**：3.6+
- **Node.js**：18+ (推荐 20)
- **数据库**：H2（开发）/ MySQL 5.7+ / Oracle 11g+ / PostgreSQL 12+
- **操作系统**：Windows / Linux / macOS

---
报表模块使用说明                                                                        
核心概念
- 模板 (ReportDefinition)：定义报表的数据源、SQL 查询、参数、可视化配置和布局。
- 实例 (ReportInstance)：基于模板创建的具体报表实例，可配置实际参数、发布状态及访问权限。
- 执行控制器 (ReportExecutionController)：负责预览 SQL、执行已发布的实例并提供 JSON 数据。
- 前端设计器 (ReportDesigner.vue)：可视化设计界面，支持配置组件和布局。
- 查看器 (ReportViewer.vue)：展示已发布报表，支持参数输入、数据导出（Excel/PDF）和打印。

使用流程

步骤1：创建报表模板

1. 进入 报表模板管理 → 新建模板
2. 填写基础信息（名称、描述、分类）
3. 编写 SQL（支持 :paramName 占位符），点击 解析SQL参数 自动生成参数列表
4. 定义参数：类型（String/Number/Date）、组件类型（文本框/下拉框/日期选择）、默认值、可见性
5. 配置可视化组件：
   - 点击 添加组件，选择表格或图表
   - 表格可配置显示字段（格式 label:prop，每行一个）；不配置时自动读取 SQL 结果的所有字段
   - 图表需选择类型（柱状图/折线图/饼图），并指定 X 轴、Y 轴字段和系列名称
   - 支持数据过滤：通过 过滤字段 + 过滤值 实现同一 SQL 返回多数据集
6. 配置布局（栅格系统，12列）：
   - 点击 添加布局项，关联组件 ID，设置宽度占位（span 1~12）和高度
7. 点击 预览数据 实时查看效果（需选择测试数据源）
8. 保存模板

步骤2：创建报表实例

1. 进入 报表实例管理 → 创建实例
2. 选择已保存的模板
3. 填写实例名称、描述，选择实际执行的数据源
4. 配置实际参数：为每个模板参数赋值
5. 保存后，实例状态为 DRAFT（草稿）

步骤3：发布与分享

1. 在实例列表中点击 发布，状态变为 PUBLISHED
2. 点击 复制链接，获取形如 http://域名/report/view/{encryptedId} 的永久分享链接
3. 将链接分享给用户，无需登录即可访问

步骤4：查看报表

- 用户打开链接后，自动显示可见参数输入框
- 输入参数后点击 查询，系统执行 SQL 并替换占位符
- 结果数据根据可视化配置自动渲染为表格或图表
- 点击 导出 Excel 或 导出 PDF 保存当前数据

高级用法

- 多数据集支持：SQL 中使用 UNION ALL 合并，增加标志列，然后在每个组件的 dataFilter 中按该列过滤
- 图表聚合：SQL 返回原始明细数据，前端图表组件按配置的 X/Y 轴自动聚合
- 访问控制：链接不校验登录态；如需权限控制，可在 SQL 中通过参数传递用户标识并关联权限表

后端 API

┌───────────────┬──────┬──────────────────────────────────────────────┬───────────────────────────────┐
│     操作      │ 方法 │                     路径                     │             说明              │
├───────────────┼──────┼──────────────────────────────────────────────┼───────────────────────────────┤
│ 模板分页      │ GET  │ /admin/report/template/page                  │ 支持按名称搜索                │
├───────────────┼──────┼──────────────────────────────────────────────┼───────────────────────────────┤
│ 模板详情      │ GET  │ /admin/report/template/detail/{id}           │ 获取模板完整定义              │
├───────────────┼──────┼──────────────────────────────────────────────┼───────────────────────────────┤
│ 保存模板      │ POST │ /admin/report/template/save                  │ 创建或更新模板                │
├───────────────┼──────┼──────────────────────────────────────────────┼───────────────────────────────┤
│ 删除模板      │ POST │ /admin/report/template/delete/{id}           │ 删除模板                      │
├───────────────┼──────┼──────────────────────────────────────────────┼───────────────────────────────┤
│ 实例分页      │ GET  │ /admin/report/instance/page                  │ 支持按名称搜索                │
├───────────────┼──────┼──────────────────────────────────────────────┼───────────────────────────────┤
│ 创建/更新实例 │ POST │ /admin/report/instance/save                  │ 关联模板并存储实际参数        │
├───────────────┼──────┼──────────────────────────────────────────────┼───────────────────────────────┤
│ 发布实例      │ POST │ /admin/report/instance/publish/{id}          │ 改为 PUBLISHED 状态           │
├───────────────┼──────┼──────────────────────────────────────────────┼───────────────────────────────┤
│ 下线实例      │ POST │ /admin/report/instance/offline/{id}          │ 改为 OFFLINE 状态             │
├───────────────┼──────┼──────────────────────────────────────────────┼───────────────────────────────┤
│ 获取加密 ID   │ POST │ /admin/report/instance/encryptId/{id}        │ 用于生成分享链接              │
├───────────────┼──────┼──────────────────────────────────────────────┼───────────────────────────────┤
│ 执行报表      │ POST │ /admin/report/execute/instance/{encryptedId} │ 接收参数并返回数据+可视化配置 │
└───────────────┴──────┴──────────────────────────────────────────────┴───────────────────────────────┘

注意事项

- SQL 中 必须使用 SELECT 语句，不支持 DML
- 参数占位符格式为 :paramName（冒号前缀），区分大小写
- 数据源需在 数据源管理 中预先配置并测试连通性
- 实例发布后，模板的修改 不会自动同步，需重新编辑实例才能生效
- 报表执行时使用的数据源是实例配置的数据源，与模板配置的数据源可以不同
