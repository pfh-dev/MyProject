# 宠爱之家 · 宠物领养救助平台

一个整合了 **宠物领养 / 救助 / 志愿者 / 捐赠 / 消息通知 / 数据看板 / AI 咨询** 的全栈平台。后端基于 Spring Boot 3 + MyBatis，前端基于 Vue 3 + Element Plus，并内置 LangChain4j AI Agent（通义千问 Qwen）提供智能宠物咨询与匹配推荐。

---

## 1. 整体架构设计

采用前后端分离的经典三层架构，前端 SPA 通过 RESTful API 与后端通信，后端统一鉴权 + 角色控制 + 线程隔离。

```
┌─────────────────────────────────────────────────────────────┐
│                       前端 (Vue 3 + Vite)                    │
│  FrontLayout(前台) / AdminLayout(后台) / Pinia / axios / ECharts │
└──────────────────────────┬──────────────────────────────────┘
                           │ RESTful /api/**  (JWT 在 Authorization 头)
┌──────────────────────────▼──────────────────────────────────┐
│                    后端 (Spring Boot 3.5)                    │
│  Controller → Service → Mapper(MyBatis XML)                  │
│  拦截器链：LoginInterceptor(鉴权) → RoleInterceptor(角色)      │
│  UserContext(ThreadLocal) / JwtUtil / AesUtil / BCrypt       │
│  AI 模块：@AiService + @Tool + Flux 流式 + ChatMemory         │
└──────────────────────────┬──────────────────────────────────┘
                           │ MyBatis / JDBC
┌──────────────────────────▼──────────────────────────────────┐
│                      MySQL 8 (pet_adoption)                  │
│        14 张业务表 + 初始化数据（分类/轮播/资讯/示例宠物）        │
└─────────────────────────────────────────────────────────────┘
```

**安全链路**：`登录/注册 → 短期 JWT 签发 → 前端 axios 拦截器注入 Authorization → LoginInterceptor 校验(含 Redis 黑名单校验)并写入 ThreadLocal(UserContext) → RoleInterceptor 校验 @RequireRole 注解 → 业务层通过 UserContext 获取当前用户`。密码 BCrypt 单向加密，手机号 AES-GCM 加密存储；登出/改密时吊销令牌（jti 写入 Redis 黑名单，TTL = 剩余有效期）。

---

## 2. 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.5.x · Java 17 · MyBatis 3.0.5 · PageHelper 1.4.6 · MySQL 8 |
| 安全 | 短期 JWT(jjwt 0.12.6) + Redis 黑名单 · spring-security-crypto(BCrypt) · AES-GCM · ThreadLocal |
| AI | LangChain4j 1.0.1-beta6 · Qwen(OpenAI 兼容网关) · WebFlux 流式 · Redis 会话记忆 |
| 前端 | Vue 3.5 · Vite 6 · Element Plus 2.x · Pinia(持久化) · Vue Router 4 · axios · ECharts 5 |

---

## 3. 模块划分与目录结构

```
pet-adoption-platform/
├── backend/                          # Spring Boot 3 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/petadopt/
│       │   ├── PetAdoptApplication.java
│       │   ├── common/               # Result / PageResult / BusinessException / 全局异常 / 角色枚举
│       │   ├── config/               # WebConfig(CORS+拦截器) / PasswordConfig / DataInitializer
│       │   ├── security/             # UserContext / JwtUtil / AesUtil / LoginInterceptor / RoleInterceptor / @RequireRole
│       │   ├── entity/               # 14 个实体
│       │   ├── dto/ vo/              # 请求/响应对象
│       │   ├── mapper/               # MyBatis 接口
│       │   ├── service/              # 业务接口 + 实现
│       │   ├── controller/           # REST 控制器
│       │   └── ai/                   # PetAssistantService(@AiService) / PetTools(@Tool) / AiConfig / AiChat*
│       └── resources/
│           ├── application.yml
│           ├── mapper/*.xml
│           └── prompt/system-prompt.txt
├── frontend/                         # Vue 3 前端
│   ├── package.json / vite.config.js / index.html
│   └── src/
│       ├── main.js / App.vue / router/
│       ├── stores/ (user / token)    # Pinia 持久化
│       ├── utils/request.js          # axios 拦截器
│       ├── api/ (13 个模块)           # 接口封装
│       ├── assets/styles/            # 主题变量 + 全局样式
│       ├── layout/ (FrontLayout / AdminLayout)
│       ├── components/PetCard.vue
│       └── views/                    # 前台页面 + admin/ 后台页面
└── sql/pet_adoption.sql              # 建库 + 14 表 + 初始化数据（单文件）
```

**12 个业务模块**：认证授权 · 用户管理 · 宠物管理 · 分类管理 · 领养申请 · 审核流程 · 回访记录 · 救助线索 · 志愿者管理 · 捐赠管理 · 消息通知 · 数据看板 · AI 咨询（含智能匹配推荐）。

---

## 4. 数据库表设计（14 张表，库名 `pet_adoption`）

角色枚举：`ADMIN`(管理员) / `STAFF`(员工·审核员) / `VOLUNTEER`(志愿者) / `USER`(普通用户)。

| 表 | 用途 | 关键字段 |
|---|---|---|
| `user` | 统一用户+角色 | id, username, password(BCrypt), nickname, real_name, phone(AES), email, avatar, gender, age, address, description, role, status, create_time |
| `pet_category` | 宠物分类 | id, name, icon, sort |
| `pet` | 宠物档案 | id, name, category_id, breed, gender, age, vaccine, sterilization, description, detail, cover_img, images, status, source, review_status, create_user_id |
| `adopt_application` | 领养申请 | id, pet_id, user_id, applicant_name, phone, address, occupation, has_experience, reason, status, apply_time, audit_user_id, audit_time, audit_remark |
| `follow_up` | 回访记录 | id, adopt_id, volunteer_id, content, pet_condition, is_normal, follow_time |
| `rescue` | 救助线索 | id, title, description, images, location, contact, status, reporter_id, assignee_id, handle_remark, handle_time |
| `volunteer` | 志愿者档案 | id, user_id, real_name, phone, skills, available_time, status, apply_time, audit_time |
| `donation` | 捐赠 | id, user_id, type(资金/物资), amount, item_name, quantity, description, status, donate_time |
| `notification` | 消息通知 | id, user_id, title, content, type, biz_id, is_read, create_time |
| `banner` | 轮播图 | id, title, img, link, sort, status |
| `article` | 公告/资讯 | id, title, cover, content, type(公告/科普/资讯), author, view_count, create_time |
| `ai_chat_session` | AI 会话 | id, user_id, title, create_time |
| `ai_chat_message` | AI 消息 | id, session_id, role(user/assistant), content, create_time |
| `operation_log` | 操作日志 | id, user_id, username, module, operation, params, ip, create_time |

---

## 5. 前后端接口设计（前缀 `/api`，统一 `Result{code,msg,data}`）

### 认证 Auth
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/auth/register` | 注册（返回 JWT） |
| POST | `/auth/login` | 登录（返回 JWT） |
| POST | `/auth/logout` | 登出（吊销令牌，写入黑名单） |
| GET | `/auth/info` | 当前用户信息 |
| PUT | `/auth/password` | 修改密码（吊销旧令牌，需重新登录） |
| PUT | `/auth/profile` | 更新个人资料 |

### 宠物 Pet / 分类
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/pet/list` | 公开列表（筛选+分页） |
| GET | `/pet/detail/{id}` | 公开详情 |
| GET | `/pet/page` | 管理分页（ADMIN/STAFF） |
| POST / PUT / DELETE | `/pet` `/pet/{id}` | 增改删（ADMIN/STAFF） |
| PUT | `/pet/{id}/status` `/pet/{id}/review` | 改状态 / 审核 |
| GET | `/pet-category/all` | 分类列表（公开） |

### 领养 Adopt / 回访 FollowUp
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/adopt` | 提交申请 |
| GET | `/adopt/my` | 我的申请 |
| GET | `/adopt/page` | 管理分页 |
| PUT | `/adopt/{id}/audit` | 审核通过/拒绝 |
| POST | `/follow-up` | 新增回访（志愿者） |
| GET | `/follow-up/adopt/{adoptId}` | 某申请的跟进记录 |
| GET | `/follow-up/page` | 回访分页 |

### 救助 Rescue
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/rescue` | 上报线索 |
| GET | `/rescue/my` | 我的上报 |
| GET | `/rescue/page` | 管理分页 |
| PUT | `/rescue/{id}/assign` | 分配志愿者 |
| PUT | `/rescue/{id}/handle` | 处理（已救助/驳回） |

### 志愿者 Volunteer / 捐赠 Donation / 通知 Notification
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/volunteer/apply` | 申请志愿者 |
| GET | `/volunteer/page` `/volunteer/approved` | 管理分页 / 已通过列表 |
| PUT | `/volunteer/{id}/audit` | 审核 |
| POST | `/donation` | 发起捐赠 |
| GET | `/donation/my` `/donation/page` | 我的 / 管理 |
| PUT | `/donation/{id}/confirm` | 确认到账 |
| GET | `/notification/my` | 我的通知 |
| PUT | `/notification/{id}/read` `/notification/read-all` | 已读 |

### 看板 Stats / 轮播 Banner / 资讯 Article / 用户 User / AI
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/stats/overview` `/stats/trend` `/stats/distribution` | 看板统计 |
| GET | `/banner/all` | 公开轮播 |
| GET | `/article/list` `/article/detail/{id}` | 公开资讯 |
| GET | `/user/page` | 用户管理（ADMIN） |
| PUT | `/user/{id}/status` `/user/{id}/role` | 禁用 / 改角色 |
| GET | `/ai/chat?memoryId=&message=` | 流式对话（纯文本流） |
| GET | `/ai/match/{petId}` | 智能匹配推荐 |
| GET | `/ai/sessions` `/ai/messages/{id}` | AI 会话/消息 |

---

## 6. LangChain4j Agent 实现思路

1. **`PetAssistantService`**（`@AiService` 接口）
   - `Flux<String> chat(@MemoryId String memoryId, @UserMessage String message)` —— 流式对话
   - `@SystemMessage(fromResource = "prompt/system-prompt.txt")` —— 注入「小安」人设
   - 显式装配 `chatModel / streamingChatModel / chatMemory / chatMemoryProvider / tools`
2. **`PetTools`**（`@Component` + `@Tool`）
   - `searchPets` 查可领养宠物、`getPetDetail` 查详情、`getAdoptStatus` 查申请进度、`getRescueTips` 救助指引，让 Agent 可查库回答真实业务问题
3. **`AiConfig`**：`ChatMemory` + `ChatMemoryProvider` + `RedisChatMemoryStore`（`StringRedisTemplate` 按 memoryId 持久化对话记忆，TTL 1 天，多用户隔离；沿用原 `E:\Langchain4j` 的 `RedisChatMemoryStore` 模式）
4. **`AiChatController`**：`GET /api/ai/chat` 返回 `Flux<String>`（`text/plain` 纯文本流），前端 `fetch` + `ReadableStream` 逐 token 渲染；`match` 返回结构化推荐文本
5. **配置**：`application.yml` 中 `langchain4j.open-ai.*`，API Key 走环境变量 `QWEN_API_KEY`（缺省占位，缺 Key 时应用仍可启动，仅 AI 对话不可用）

---

## 7. 完整业务闭环流程图

```
注册/登录(JWT)
   │
   ├─► 浏览宠物 / 领养申请 ──► 管理员审核 ──┬─ 通过 → 宠物标记「已领养」→ 生成回访计划
   │                                      │                    └─ 志愿者执行回访(回访记录)
   │                                      └─ 拒绝 → 通知用户(附驳回原因)
   │
   ├─► 救助上报(USER) ──► 管理员审核/分配志愿者 ──► 志愿者处理 ──► 审核通过后转宠物档案
   │
   ├─► 捐赠(资金/物资) ──► 管理员确认到账 ──► 通知用户
   │
   ├─► 志愿者申请 ──► 管理员审核 ──► 参与回访 / 救助
   │
   └─► AI 助手(咨询 + 智能匹配推荐) ── 全流程消息通知推送 ── 数据看板聚合统计
```

---

## 8. 前端美化方案

- **组件库**：Element Plus 2.x（zh-cn locale）+ ECharts 5 图表 + 自定义 `PetCard` 宠物卡片。
- **配色**：主色暖橙 `#ff7a45`（温暖、契合救助主题）+ 辅助青绿 `#2ec4b6` + 中性灰白 `#f5f6f8` 背景；后台深色侧栏 `#1f2733`。
- **布局**：前台顶部导航 + 轮播 Hero + 分类入口 + 宠物卡片网格 + 数据亮点 + 底部信息栏；后台左侧深色菜单 + 顶栏 + 内容区。
- **关键页**：首页（Hero + 分类 + 最新待领养 + 数据亮点）、宠物详情（大图 + 信息 + 申请 + AI 匹配推荐）、领养申请（弹窗表单）、个人中心（资料/申请/救助/捐赠/消息 Tab）、管理后台（看板 + 各管理页）、AI 助手（侧栏会话 + 流式气泡）。

---

## 9. 开源项目参考结论

| 参考项 | 来源 | 借鉴内容 |
|---|---|---|
| 业务模块 | `MilesSG/AnimalRescueSystem`、`666bears/Smart-Community`、`gitee.com/shuaishuaiya/pet` | 救助/绝育/捐赠/领养/回访模块划分与状态流转 |
| AI Agent | `spring-petclinic/spring-petclinic-langchain4j` | `@AiService`+`@Tool`+RAG+流式的宠物场景范式 |
| Qwen 接入 | `nan3379/rabbit-ai-agent` | Qwen/DashScope OpenAI 兼容网关接入 |

> 结论：仅借鉴**模块划分**与 **Agent 范式**，业务字段/流程以原项目 + 需求为准，未照搬任何代码。

---

## 10. 整合步骤与开发优先级

1. SQL 单文件整合（14 表 + 初始化数据）✅
2. 后端骨架（pom / yml / 通用层 / 安全层 / 配置）✅
3. 后端实体 + Mapper + XML ✅
4. 后端 Service + Controller（全业务模块）✅
5. AI 模块（LangChain4j Agent + 流式）✅
6. 前端骨架（vite / router / stores / request / api / 样式 / Layout）✅
7. 前端页面（前台 + 后台全部页面）✅
8. 项目说明文档 ✅

---

## 11. 快速启动

### 1) 初始化数据库
```bash
mysql -uroot -p < sql/pet_adoption.sql
```
默认连接：`localhost:3306` / 库 `pet_adoption` / 用户 `root` / 密码 `123456`（可在 `backend/src/main/resources/application.yml` 修改）。

### 2) 启动后端（Java 17 + Maven + Redis）
```bash
cd backend
mvn spring-boot:run
```
- 启动时 `DataInitializer` 自动创建管理员账号：**`admin` / `admin123`**（BCrypt 加密）。
- 首次启动需联网拉取 Maven 依赖。
- 需本机运行 Redis（默认 `localhost:6379`，可在 `application.yml` 的 `spring.data.redis` 修改），用于 AI 会话记忆持久化与 JWT 黑名单吊销；Redis 未启动时应用仍可运行，仅 AI 对话记忆读写失败、令牌吊销降级为不生效。

### 3) 启动前端（Node 18+）
```bash
cd frontend
npm install
npm run dev
```
访问 http://localhost:5173，前端 `/api` 已代理至 `http://localhost:8080`。

### 4) 启用 AI 对话（可选）
```bash
# Windows (PowerShell)
$env:QWEN_API_KEY = "你的通义千问 Key"
# 或直接修改 application.yml 中 langchain4j.open-ai.chat-model.api-key
```
未配置 Key 时应用照常运行，仅 AI 对话返回占位/失败。

### 5) 验证主链路
登录 `admin/admin123` → 浏览宠物 → 申请领养 → 后台审核通过（宠物转「已领养」）→ 志愿者回访 → 消息通知推送 → 看板统计更新；前台 AI 助手流式对话正常。
