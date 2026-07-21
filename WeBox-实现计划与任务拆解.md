# WeBox 实现计划与任务拆解

> 依据《AI Vibe Coding V3.0 - PRD.md》编写。目标：交付完整可运行的全栈系统
> （React SPA + JDK 17/Spring Boot + 独立 MySQL），Tier 1+2 为必达主线，Tier 3 为独立加分阶段。
>
> 日期：2026-07-21 ｜ 前端栈：React 18 + Vite + TypeScript ｜ LLM：OpenAI 兼容接口（SSE 流式）

---

## 1. 技术选型与理由（写入 README，演示时口述）

| 层 | 选型 | 理由 |
|---|---|---|
| 前端 | React 18 + Vite + TS + React Router + TanStack Query + Zustand + Tailwind CSS | 生态成熟、TS 类型安全防低级错；TanStack Query 管服务端状态/缓存/轮询，Zustand 管购物车等客户端状态；Tailwind 快速出企业级 UI；Vite 秒级热更新 |
| 后端 | JDK 17 + Spring Boot 3.x + Spring Web + Spring Data JPA + Spring Validation + Spring Security（仅密码哈希/过滤器） | PRD 强制；JPA 快速建模，Validation 做输入校验 |
| 认证 | JWT（无状态）+ BCrypt 密码哈希 | SPA 前后端分离标准做法；刷新不丢登录态（token 存 localStorage） |
| 数据库 | MySQL 8（独立进程，docker-compose 启动） | PRD 强制独立部署，禁止嵌入式 |
| 金额 | DB 存 `DECIMAL(10,2)`，Java 用 `BigDecimal`，前端展示层格式化为分 | PRD 要求精确到分、避免浮点误差 |
| 缓存 | Caffeine 进程内缓存（菜单热点数据）+ 写时失效 | PRD 允许进程内方案；免去 Redis 部署成本，README 说明策略即可 |
| 实时推送 | SSE（Server-Sent Events） | 库存变化是服务端→客户端单向推送，SSE 比 WebSocket 实现简单、浏览器原生支持断线重连 |
| AI 推荐 | OpenAI 兼容 Chat Completions 接口（`base_url`/`api_key`/`model` 走环境变量），SSE 流式转发 | 一套代码通配 DeepSeek/通义/Kimi/OpenAI 等；要求模型输出 JSON 数组以渲染卡片 |

## 2. 架构与数据流

```
浏览器 (React SPA)
   │  HTTP/JSON + JWT Authorization header      ┌── SSE: /api/inventory/stream（库存推送）
   ▼                                            ├── SSE: /api/ai/recommend（AI 推荐流式）
Spring Boot 后端
   ├── AuthController / MenuController / CartController / OrderController
   ├── PreferenceController / AdminController / DashboardController / AiController
   ├── Service 层（规则与事务边界）── Caffeine 缓存（菜单查询）
   └── JPA ──► MySQL 8（独立进程）          外部 LLM（OpenAI 兼容，SSE）
```

模块划分：`auth`（认证/角色）、`menu`（菜品/选项/每日菜单）、`cart`、`order`（下单/规则/状态机）、`preference`、`admin`（菜品管理/每日菜单）、`dashboard`、`ai`（推荐）、`inventory`（库存/SSE 推送）、`common`（异常/金额/枚举/校验）。

## 3. 数据库 Schema（8 张表）

```sql
users(id PK, email UNIQUE NOT NULL, password_hash, role ENUM('EMPLOYEE','ADMIN'), created_at)
dishes(id PK, name, description, price DECIMAL(10,2), category VARCHAR(32),
       protein VARCHAR(64), spice_level ENUM('NONE','MILD','MEDIUM','HOT'),
       allergens JSON,            -- ["Peanuts","Dairy"...]
       image_url, is_active TINYINT(1) DEFAULT 1, created_at, updated_at)
option_groups(id PK, dish_id FK, name, is_required TINYINT(1), sort_order)
option_items(id PK, group_id FK, name, extra_price DECIMAL(10,2) DEFAULT 0)
daily_menus(id PK, menu_date DATE, dish_id FK, stock_total INT, stock_remaining INT,
            UNIQUE KEY uk_date_dish (menu_date, dish_id))
cart_items(id PK, user_id FK, dish_id FK, qty INT,
           selected_options JSON,  -- [{"group":"Bun","item":"Whole Wheat","extraPrice":0},...]
           options_hash VARCHAR(64), created_at,
           UNIQUE KEY uk_cart (user_id, dish_id, options_hash))   -- 同菜品同配置合并
orders(id PK, order_no VARCHAR(32) UNIQUE, user_id FK, delivery_date DATE,
       meal_slot ENUM('LUNCH','DINNER'), address VARCHAR(200),
       total DECIMAL(10,2), status ENUM('PENDING','CONFIRMED','COMPLETED','CANCELLED'),
       idempotency_key VARCHAR(64) UNIQUE NOT NULL, created_at, updated_at,
       -- 同日同餐次唯一有效订单（MySQL 无部分索引，用生成列 trick）：
       active_key VARCHAR(80) GENERATED ALWAYS AS
         (IF(status IN ('PENDING','CONFIRMED'),
             CONCAT(user_id,'|',delivery_date,'|',meal_slot), NULL)) STORED,
       UNIQUE KEY uk_active_order (active_key))
order_items(id PK, order_id FK, dish_id, dish_name VARCHAR(128),      -- 快照字段
            unit_price DECIMAL(10,2), options_json JSON, qty INT, subtotal DECIMAL(10,2))
user_preferences(user_id PK/FK, allergens JSON, cuisines JSON,
                 spice_level VARCHAR(16), taste VARCHAR(16),
                 budget_min DECIMAL(10,2), budget_max DECIMAL(10,2))
addresses(id PK, user_id FK, address_text VARCHAR(200), last_used_at,
          UNIQUE KEY uk_user_addr (user_id, address_text))
```

设计要点：
- **快照**：`order_items` 冗余菜品名/单价/选项，菜品改价改名不影响历史订单。
- **库存挂 `daily_menus`**：每日每菜独立库存，天然支撑"设置每日菜单+供应数量"。
- **防超卖**：`UPDATE daily_menus SET stock_remaining = stock_remaining - ? WHERE id = ? AND stock_remaining >= ?`，按影响行数判断成败，数据库原子性兜底。
- **幂等双保险**：`idempotency_key` 唯一索引 + `active_key` 生成列唯一索引，重复提交/并发提交最多落一单。

## 4. API 清单（REST，统一 `/api` 前缀，JWT 放 `Authorization: Bearer`）

| 模块 | 方法与路径 | 说明 |
|---|---|---|
| 认证 | `POST /auth/register` / `POST /auth/login` | 返回 `{token, user:{id,email,role}}` |
| 菜单 | `GET /menu?date=YYYY-MM-DD` | 当日菜单（含库存、选项组），走缓存 |
| 菜单 | `GET /dishes/{id}` | 菜品详情 |
| 搜索 | `GET /dishes/search?q=&categories=Chinese,Japanese` | 多分类筛选 + 关键词（参数化查询防注入） |
| 购物车 | `GET /cart` / `POST /cart/items` / `PATCH /cart/items/{id}` / `DELETE /cart/items/{id}` | POST 返回是否触发过敏原（前端弹窗依据） |
| 结算 | `GET /checkout/summary?date=&slot=` | 校验餐次、返回预算提示、是否已有有效订单 |
| 订单 | `POST /orders`（body 含 `idempotencyKey`） | 幂等创建；返回订单+配送信息 |
| 订单 | `GET /orders` / `GET /orders/{id}` / `POST /orders/{id}/cancel` | 仅 PENDING 可取消，取消回补库存 |
| 偏好 | `GET /preferences` / `PUT /preferences` | 过敏原/菜系/辣度/口味/预算 |
| 地址 | `GET /addresses` | 历史地址 |
| Console | `GET/POST /admin/dishes`、`PUT /admin/dishes/{id}`、`PATCH /admin/dishes/{id}/status`、`POST /admin/dishes/{id}/image` | 菜品 CRUD/上下架/图片上传（`ADMIN` 角色接口级鉴权） |
| Console | `GET /admin/menus/{date}` / `PUT /admin/menus/{date}` | 设置每日菜单与各菜供应数量 |
| 看板(T3) | `GET /admin/dashboard` | 一次聚合：今日概览/Top10/时段分布/7日趋势/低库存 |
| 库存(T3) | `GET /inventory/stream` | SSE 推送 `{dishId, menuDate, stockRemaining}` |
| AI(T3) | `POST /ai/recommend` | SSE 流式；末尾输出结构化推荐 JSON |

统一错误格式：`{"code":"EMAIL_TAKEN|STOCK_INSUFFICIENT|ORDER_EXISTS|...","message":"英文文案","details":[...]}`。

## 5. 核心业务规则（伪码，实现时照抄语义）

**① 截单自动切换（纯函数，重点测试）**
```
resolveSlot(now, requestedDate, requestedSlot):
  slotCutoff = {LUNCH: 10:00, DINNER: 15:00}
  if requestedSlot 未过当日 cutoff → (requestedDate, requestedSlot)
  else if requestedSlot == LUNCH 且 now < 当日15:00 → (当日, DINNER)
  else → (次日, LUNCH)
```

**② 下单事务（咽喉链路，顺序不可乱）**
```
placeOrder(user, cart, date, slot, address, idemKey):
  1. idemKey 查重 → 命中直接返回已有订单          # 幂等
  2. resolveSlot 校正餐次                          # §4.2 截单
  3. 校验总份数 ≤ 5                                # §4.2 上限
  4. 校验 (user, date, slot) 无 PENDING/CONFIRMED 订单 # §4.2 唯一；有则 409 + 返回已有订单
  5. 事务: 逐菜品原子扣库存（行数=0 → 收集缺货明细，回滚，400）
  6. 落 orders + order_items（快照），清空购物车
  7. 事务提交后发布库存变更 → SSE 广播；菜单缓存失效
```

**③ 过敏原**：后端在购物车 POST 响应中返回 `matchedAllergens:["Egg"]`，前端弹 `This dish contains an allergen you flagged: [Egg]. Add anyway?` 确认后重发 `confirmed:true`。**提醒不过滤**。

**④ 库存(T3)**：剩余 ≤3 前端低库存标红；=0 售罄禁下单；取消订单 `stock_remaining + qty`（同事务）；SSE 全量广播变化。

**⑤ AI 推荐(T3)**：后端先按硬条件过滤当日菜品（在售、未售罄、不含用户过敏原、不在该用户近 7 天订单中）→ 把候选菜品 JSON + 用户偏好拼入 prompt → 要求模型**只输出 JSON 数组** `[{dishId, reason}]` → SSE 逐条流出 → 前端渲染菜品卡片（可直接加购）。

## 6. 任务拆解（4 阶段 22 任务，含验收标准与时间盒）

> 优先级：**P0 = Tier 1 必做，P1 = Tier 2 必做，P2 = Tier 3 加分，P3 = 交付物（必做，贯穿全程）**。
> 时间盒按 1.5 小时挑战设计：P0+P1+最小 P3 ≈ 90 分钟，P2 为行有余力的加时项；非限时练习可忽略时间列。
> 每个任务完成即提交一次 git（对话记录同步导出，见 T21）。

### 阶段 0：脚手架与基建（目标 ~12 分钟）

| # | 优先级 | 任务 | 关键产出/要点 | 验收标准 |
|---|---|---|---|---|
| T00 | P0 | 仓库与工程初始化 | `git init`；monorepo：`server/`(Maven, Spring Boot 3, JDK17) + `web/`(Vite React TS)；`docker-compose.yml` 起 MySQL 8（`webox`库/root密码/端口3306）；`.gitignore`；`ai-conversations/` 目录占位 | `docker compose up -d` 后 MySQL 可连；`mvn spring-boot:run` 起空应用；`npm run dev` 出页面 |
| T01 | P0 | 后端地基 | 全局异常处理（统一错误格式）、`BigDecimal` 金额约定、枚举（Category/Spice/Allergen/MealSlot/OrderStatus/Role）、JPA 配置连 MySQL、CORS 放开前端端口 | 启动自动建表（`ddl-auto=update` 或 Flyway 任选其一并保持一致） |
| T02 | P0 | 前端地基 | Router 骨架 + 路由表（员工端/Console 两套布局）、API client（axios/fetch 封装：自动带 JWT、401 跳登录、错误 toast）、Tailwind 接入、响应式断点约定 | 空白页可路由跳转；未登录访问受限页被重定向 |

### 阶段 1：Tier 1 核心链路（目标 ~35 分钟）——**下单是咽喉，优先打通**

| # | 优先级 | 任务 | 关键产出/要点 | 验收标准 |
|---|---|---|---|---|
| T03 | P0 | 注册/登录/角色 | AuthService（BCrypt、邮箱唯一、密码≥8位含字母数字）、JWT 签发/校验过滤器、`@PreAuthorize` 或拦截器做角色鉴权；前端登录/注册页（英文、校验、长度上限：邮箱≤200）+ token 持久化 | 重复注册/错误密码有明确英文报错；刷新不掉登录；员工访问 `/api/admin/**` 返回 403 |
| T04 | P0 | 菜品/选项建模 + 种子数据 | 4 张表实体；`data.sql` 或 `DataSeeder`：PRD §6 全部 9 个菜品（含选项组）、**当日 daily_menus（每菜库存 10）**、admin 账号、demo 员工、若干历史订单（供 T3 看板/AI 用）；菜品图片解压进 `server/src/main/resources/static/images/` 并按名引用 | 启动后查 `/api/menu` 当日有 9 菜、带库存与选项组 |
| T05 | P0 | 菜单浏览/筛选/搜索 API | `GET /menu`（Caffeine 缓存 key=date，写操作失效）、`GET /dishes/search`（JPA Specification 参数化，q 限长 50） | 多分类并集筛选正确；关键词命中名称或描述；含 SQL 注入载荷的 q 不报错不泄露 |
| T06 | P0 | 菜单页 + 菜品详情（前端） | 卡片网格（图/名/¥价/菜系标签/库存角标）、分类多选 chips、搜索框（防抖 300ms、≤50 字符）、分页或虚拟滚动（50+ 菜品流畅）；详情页选项组渲染（必选单选/可选多选、加价、**选择后价格实时重算**） | 移动端+桌面端布局均可用；价格展示精确到分；选项加价计算正确 |
| T07 | P0 | 购物车（前后端） | CartService（同菜品同配置合并、异配置分行；数量增减/移除/总价）；前端购物车抽屉/页（Zustand 乐观更新 + 服务端同步，加购按钮防抖） | "全麦汉堡"与"普通汉堡"分两行；改数量总价实时正确；高频连点不产生错乱 |
| T08 | P0 | **下单（幂等）** | §5-② 事务链路第 1、5(不扣库存，T16 再加)、6 步；前端结算页（明细/日期默认当天/时段/地址历史+新输入）+ 成功页（订单号+配送信息）；提交按钮点击即禁用 + `idempotencyKey = crypto.randomUUID()` | 双击/慢网重试/刷新重提交 → 库中只有一单，响应均返回同一订单号 |
| T09 | P0 | 我的订单 + 取消 | 订单列表/详情（快照展示选项与小计）；`POST /orders/{id}/cancel` 仅 PENDING→CANCELLED | 非 PENDING 取消返回 409 英文提示；详情金额与下单时一致 |

### 阶段 2：Tier 2 进阶（目标 ~25 分钟）

| # | 优先级 | 任务 | 关键产出/要点 | 验收标准 |
|---|---|---|---|---|
| T10 | P1 | 订餐规则 | §5-② 第 2/3/4 步接入下单；结算页已有有效订单时按钮变 `View Existing Order` 并跳详情；菜单页加购按钮与购物车"+"在购物车总份数=5 时禁用并英文提示 | 10:30 下午餐单自动变当日晚餐；15:00 后变次日午餐；3+3 份提交被拒；同日同餐次第二单被拒并能看到已有单 |
| T11 | P1 | 饮食偏好 | 偏好 CRUD；购物车 POST 返回匹配过敏原 → 前端确认弹窗（确认后带 `confirmed:true` 重发）；菜单页 `For You` 开关：偏好菜系置顶 + 辣度匹配高亮（关闭恢复默认序） | 含 Egg 菜品加购弹英文确认；开关排序行为正确；不配置偏好时全流程无打扰 |
| T12 | P1 | 预算提示 | 偏好含 budget_min/max；结算页总价 > 上限时显示英文提示条（不阻止提交） | 超预算有提示且可正常下单 |
| T13 | P1 | Console 布局 + 菜品管理 | `/console` 独立导航布局 + 路由守卫；菜品表格（搜索/分类筛选/分页）、新增/编辑表单（含过敏原多选、选项组编辑）、图片上传（存 static/images）、上架/下架开关（下架即员工端不可见） | 员工 JWT 访问 console 页面与 API 均被拒；下架菜品从 `/api/menu` 消失；新菜即时出现在员工端 |
| T14 | P1 | 每日菜单设置 | `GET/PUT /admin/menus/{date}`：勾选菜品 + 填各菜供应数量，默认日期次日；同时可用于调整当日库存 | 为次日配菜单后，员工端 `?date=次日` 可见且库存为设定值 |

### 阶段 3：Tier 3 挑战（加时项，每个独立可裁）

| # | 优先级 | 任务 | 关键产出/要点 | 验收标准 |
|---|---|---|---|---|
| T15 | P2 | 库存扣减与防超卖 | 下单事务接入 §5-② 第 5 步原子扣减；提交时二次校验并在 400 响应里带缺货明细（菜品名+剩余数）；前端据此提示并允许改量重提 | JUnit 并发测试：10 线程抢 5 库存，恰好 5 单成功且无负库存 |
| T16 | P2 | 实时库存推送 | SSE `/inventory/stream`（Spring `SseEmitter`，心跳保活）；下单/取消/后台改库存后广播；前端 EventSource 收到即更新卡片角标；≤3 份低库存红标、0 售罄禁用加购 | 两个浏览器窗口：A 下单，B 无需刷新看到库存变化 |
| T17 | P2 | 取消回补库存 | 取消事务内 `stock_remaining += qty` 并广播 | 取消后菜单页实时恢复数量 |
| T18 | P2 | AI 智能推荐 | `/ai/recommend`：硬过滤（§5-⑤）→ prompt 注入候选菜品+偏好 → OpenAI 兼容流式调用 → SSE 转发；前端逐条渲染菜品卡片（图/名/价/英文推荐理由/Add to Cart）；Key 走环境变量，README 写配置方法 | 断网/无 Key 时优雅降级（英文提示）；推荐不含过敏原菜与近 7 天已点菜；流式逐条出现非长时间白屏 |
| T19 | P2 | 经营数据看板 | `GET /admin/dashboard` 聚合真实订单数据；前端：今日概览数字卡、Top10 柱状图、午/晚对比、7 日订单量+收入折线（图表用 Recharts）、低库存列表（≤3）；手动刷新按钮（或 30s 轮询） | 下一单后刷新看板，数字/图表同步变化 |

### 阶段 4：交付物与演示准备（贯穿，目标 ~15 分钟，**缺失直接影响评级**）

| # | 优先级 | 任务 | 关键产出/要点 | 验收标准 |
|---|---|---|---|---|
| T20 | P3 | 后端核心测试 | JUnit：`OrderSlotResolverTest`（截单切换全分支）、`OrderServiceTest`（幂等/5份上限/唯一有效单/并发不超卖/取消回补）、`PriceCalculationTest`（选项加价与分精度）、`AuthServiceTest`（密码策略/重复邮箱） | `mvn test` 全绿；覆盖 §4.2 与 §3.4 全部规则分支 |
| T21 | P3 | 文档四件套 + 对话记录 | README（项目介绍/技术栈与选型理由/**从零启动步骤**：MySQL compose → `mvn spring-boot:run` → `npm run dev` → 种子账号）；`docs/api.md`（接口清单+请求响应示例）；`docs/architecture.md`（模块划分+数据流+幂等/防超卖/缓存决策）；**开发全程每个 AI 会话结束即导出原始记录到 `ai-conversations/`** | 他机按 README 10 分钟内跑起来；对话记录从项目第一条开始连续完整 |
| T22 | P3 | 演示彩排 | 按演示脚本走查：注册→偏好→加购(过敏原弹窗)→规则(截单/5份/重复单)→下单幂等→Console 菜品与菜单→(T3: 双窗口库存/AI 推荐/看板)；准备技术选型与踩坑口述稿 | 全流程无手工改库操作；断点均有英文错误提示 |

## 7. 依赖关系与关键路径

```
T00→T01→T03(认证) ─┐
      T01→T04(数据) ├→ T05→T06(菜单) → T07(购物车) → T08(下单) → T09(订单/取消)
      T02(前端地基)─┘                                  │
                              T10(规则) ←──────────────┘   ← 规则挂在下单链路上
                              T11/T12(偏好/预算) ← T03,T07
                              T13/T14(Console) ← T03,T04
                              T15→T16→T17(库存三连) ← T08
                              T18(AI) ← T04,T11    T19(看板) ← T09
```

**关键路径**：T00→T01→T04→T08（下单）→T10（规则）→T20（测试）。下单链路不返工是 90 分钟完赛的前提——T08 先跑通最小闭环（不扣库存），T10/T15 再逐层挂规则。

**裁剪线**：时间吃紧时按 T19→T18→T16/17→T15 顺序放弃（P2 整体可弃，不影响 ≥45/50 门槛）；P0/P1/T20/T21 任何一项都不可裁。

## 8. 风险与应对

| 风险 | 应对 |
|---|---|
| MySQL 环境耽误时间 | docker-compose 一条命令起库；无 Docker 则 README 写明本地 MySQL 建库 SQL，提前备好 |
| AI 生成代码与 Schema/接口漂移 | T01/T04 先锁定枚举、错误格式、API 清单（本文件 §3/§4 即为契约，生成时贴给 AI） |
| LLM Key 现场不可用 | T18 做成可插拔：无 Key 返回明确英文提示，不影响其他功能评分 |
| 对话记录丢失 | 每完成一个任务立即导出一次（覆盖式累积），T21 只做归拢 |
| 选项组/快照导致下单复杂度爆炸 | 下单时直接以购物车行（已含选项 JSON 与单价）为输入做快照，不重算菜品定义 |
| 1.5 小时做不完 | 严格按优先级执行；每个任务完成即提交，保证任意时刻都有可演示版本 |

---

— 计划结束 —
