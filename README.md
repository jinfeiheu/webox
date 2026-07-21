# WeBox — Employee Meal Ordering Platform

An enterprise-internal meal-ordering platform for ~200 employees. Employees browse the daily
menu, customize dishes, manage a cart and place orders; admins manage dishes and daily menus in a
separate **Console**. All user-facing content is **English** (per PRD §1).

> Delivery scope: **Tier 1 (core) + Tier 2 (advanced) fully implemented.**
> Tier 3 (stock deduction/realtime, AI recommendation, business dashboard) is intentionally
> deferred — see "Scope & deferrals" below.

## Tech stack & rationale

| Layer | Choice | Why |
|---|---|---|
| Frontend | React 18 + Vite + TypeScript, React Router, TanStack Query, Zustand, Tailwind CSS | Largest ecosystem, best AI-assist accuracy; Query handles server cache/refresh, Zustand holds client state; Tailwind ships an enterprise look fast. |
| Backend | **JDK 17 + Spring Boot 3.3**, Spring Web + Data JPA + Validation | PRD-mandated; JPA for fast modeling, Validation for input checks. |
| Auth | JWT (HS256, stateless) + BCrypt password hashing | Standard for SPA + REST; refresh keeps login via localStorage. |
| Database | **Standalone MySQL 8** (Docker Compose) | PRD-mandated standalone DB — no embedded/in-memory DB. |
| Money | `DECIMAL(10,2)` + Java `BigDecimal` + integer-cents math on the frontend | PRD §6: exact to the cent, never float. |
| Cache | Caffeine in-process cache (menu hot data) | PRD allows in-process caching; avoids extra infra for the 9:30–10:00 peak. |

## Architecture

```
Browser (React SPA)
   │  HTTP/JSON, Authorization: Bearer <jwt>
   ▼
Spring Boot :8080
   ├─ auth      (register/login, JWT issue/verify, role guard @RequireRole)
   ├─ menu      (dishes, option groups, daily menus, cached list/detail, search)
   ├─ cart      (option snapshot validation, 5-item cap, allergen warning)
   ├─ order     (cutoff slot resolver, idempotent placement, one-active-order, cancel)
   ├─ preference(allergens/cuisines/spice/taste/budget)
   └─ admin     (dish CRUD + image upload, daily menu setup)  ← ADMIN only
        │ JPA / Hibernate           Caffeine cache (menuItems, dishDetail)
        ▼
   MySQL 8 (standalone, docker-compose)
```

### Key mechanisms (see `docs/architecture.md` for detail)

- **Idempotent ordering** — client sends a per-checkout `idempotencyKey`; a unique index guarantees
  one order per key even under double-clicks/retries, and a concurrent-twin insert is recovered by
  replaying the winner instead of failing.
- **One active order per (user, date, meal slot)** — a MySQL *generated column* is non-NULL only
  while status is Pending/Confirmed, so its unique index enforces the rule at the DB level.
- **Cutoff auto-switch** — pure `OrderSlotResolver`: lunch by 10:00 / dinner by 15:00; a past-cutoff
  request auto-moves to today's dinner (if open) else tomorrow's lunch.
- **Order line snapshots** — dish name/price/options are copied onto order items at purchase time;
  later dish edits never rewrite history.
- **SQL-injection-safe search** — JPA Criteria/Specification with bound parameters + LIKE-escape.

## Quick start (from zero)

**Prerequisites:** JDK 17, Node 18+, Docker.

```bash
# 1. Start MySQL (creates the `webox` database automatically)
docker compose up -d

# 2. Backend (http://localhost:8080) — seeds demo data on first boot
cd server
mvn spring-boot:run

# 3. Frontend (http://localhost:5173), new terminal
cd web
npm install
npm run dev
```

Open http://localhost:5173 and log in with a seeded account:

| Role | Email | Password |
|---|---|---|
| Employee | `demo@webox.com` | `demo1234` |
| Admin (Console) | `admin@webox.com` | `admin1234` |

The seeder loads 20 dishes (8 with customization groups), today's and tomorrow's daily menus
(×10 stock each), and ~13 historical orders so every screen has data immediately.

## Tests

```bash
cd server
mvn test    # 21 unit tests: cutoff switching, idempotent/cap/one-active-order/cancel, auth
```

## Environment variables

| Var | Default | Purpose |
|---|---|---|
| `JWT_SECRET` | dev value in `application.yml` | **Override in production.** |
| `JWT_TTL` | 12 h | token lifetime |

## Project structure

```
webox/
├─ docker-compose.yml      # standalone MySQL 8
├─ server/                 # Spring Boot (JDK 17)
│  └─ src/main/java/com/webox/{auth,menu,cart,order,preference,admin,common,bootstrap}
├─ web/                    # React SPA (Vite + TS)
├─ docs/                   # api.md, architecture.md
└─ ai-conversations/       # full raw AI-coding transcript (PRD §7.2 deliverable)
```

## Scope & deferrals (honest list)

Implemented: Tier 1 (auth, menu browse/search, cart, idempotent ordering, orders/cancel) and
Tier 2 (dietary preferences + allergen warning, ordering rules — 5-item cap / cutoff auto-switch /
one-active-order, Console dish management, daily menu setup).

Deferred (Tier 3, bonus): stock deduction & realtime sync, AI recommendation, business dashboard.
The schema already carries per-day stock (`daily_menus.stock_remaining`), so stock deduction and
its concurrency guard (`UPDATE … WHERE stock_remaining >= n`) can be added without a migration.

## Documentation

- `docs/api.md` — REST API reference (endpoints, request/response samples)
- `docs/architecture.md` — module breakdown, data flow, concurrency/idempotency/cache decisions
- `ai-conversations/` — complete unedited AI-coding conversation export
