# WeBox API Reference

Base URL: `http://localhost:8080/api`

All endpoints except `/auth/login` and `/auth/register` require
`Authorization: Bearer <jwt>`（issued by login / register, 12 h TTL).
Admin-only endpoints return **403** for employee tokens.

Error body (uniform): `{"code":"ENUM_VALUE","message":"English description.","details":["field-level info"]}`

## Auth
| Method | Path | Auth | Body / Params | Response |
|--------|------|------|---------------|----------|
| POST | `/auth/register` | none | `{"email":"…","password":"…"}` | `{"token":"eyJ…","user":{"id":1,"email":"…","role":"Employee"}}` |
| POST | `/auth/login` | none | `{"email":"…","password":"…"}` | same shape |
| GET | `/auth/me` | token | — | `{"id":1,"email":"…","role":"Employee"}` |

Rules: email ≤ 200, password ≥ 8 + letters & digits, BCrypt stored, duplicate email → 409 `EMAIL_TAKEN`, wrong credentials → 401 `INVALID_CREDENTIALS`.

## Menu
| Method | Path | Auth | Params | Response |
|--------|------|------|--------|----------|
| GET | `/menu` | token | `?date=2026-07-21` (default today) | `{"date":"…","items":[{…MenuItem…}]}` |
| GET | `/dishes/search` | token | `?q=keyword&categories=Chinese,Western&date=` | same shape, filtered |
| GET | `/dishes/{id}` | token | — | `{dishId,…,optionGroups:[{id,name,required,items:[{id,name,extraPrice}]}]}` |

**MenuItem** shape: `{dishId, name, description, price, category, protein, spiceLevel, allergens[], imageUrl, stockRemaining, hasRequiredOptions}`.

Search uses JPA Criteria (parameterised); keyword ≤ 50 chars; categories comma-separated English labels.

## Cart
| Method | Path | Auth | Body | Response |
|--------|------|------|------|----------|
| GET | `/cart` | token | — | `{"items":[{CartItem}],"totalQty":3,"totalPrice":66.0}` |
| POST | `/cart/items` | token | `{"dishId":1,"selectedOptions":[{groupId,itemId}],"qty":1,"confirmed":false}` | `{"item":{CartItem},"matchedAllergens":["Egg"]}` |
| PATCH | `/cart/items/{id}` | token | `{"qty":2}` | `{CartItem}` |
| DELETE | `/cart/items/{id}` | token | — | 200 |

**CartItem**: `{cartItemId, dishId, dishName, imageUrl, category, unitPrice, selectedOptions:[{groupId,groupName,itemId,itemName,extraPrice}], qty, subtotal}`.

Allergen gate (PRD §4.1): if `matchedAllergens` is non-empty on an unconfirmed (`confirmed:false`) request, the item is **NOT saved** — the UI receives only the match list and must prompt the employee before re-posting with `confirmed:true`.

5-item cap enforced on every add/increment → 400 `ORDER_LIMIT_EXCEEDED`.

## Checkout & Orders
| Method | Path | Auth | Params / Body | Response |
|--------|------|------|---------------|----------|
| GET | `/checkout/summary` | token | `?date=&slot=` | `{date,slot,switched,items:[CartItem],totalQty,totalPrice,existingOrder:{orderId,orderNo}\|null}` |
| GET | `/addresses` | token | — | `["Building A…","…"]` |
| POST | `/orders` | token | `{"deliveryDate":"2026-07-21","mealSlot":"Lunch","address":"…","idempotencyKey":"uuid"}` | `{OrderView}` |
| GET | `/orders` | token | — | `[{OrderSummary}]` |
| GET | `/orders/{id}` | token | — | `{OrderView}` |
| POST | `/orders/{id}/cancel` | token | — | `{OrderView}` (status → Cancelled) |

**OrderView**: `{orderId, orderNo, deliveryDate, mealSlot, address, totalPrice, status, createdAt, items:[{dishId,dishName,unitPrice,options,qty,subtotal}]}`.

**OrderSummary**: `{orderId, orderNo, deliveryDate, mealSlot, totalPrice, status, createdAt, itemCount}`.

**Cutoff auto-switch** (PRD §4.2): Lunch orders by 10:00, dinner by 15:00 same-day. Past slot → auto-switch (switched:true).  `POST /orders` uses the resolved slot.

Each order placement is idempotent via `idempotencyKey` — double click, network retry → same order, no duplicate.  Concurrent same-slot attempts get **409** `ORDER_EXISTS` with the existing order id in `details[0]`.

Only `Pending` orders can be cancelled → 409 `ORDER_NOT_CANCELLABLE`.

## Preferences
| Method | Path | Auth | Body | Response |
|--------|------|------|------|----------|
| GET | `/preferences` | token | — | `{allergens:[],cuisines:[],spiceLevel,taste,budgetMin,budgetMax}` |
| PUT | `/preferences` | token | same shape | same shape (upsert) |

## Admin (Console — ADMIN only, else 403)
| Method | Path | Auth | Params / Body | Response |
|--------|------|------|---------------|----------|
| GET | `/admin/dishes` | ADMIN | `?q=&category=&page=0&size=20` | `Page<AdminDish>` |
| POST | `/admin/dishes` | ADMIN | `{name,description,price,category,protein,spiceLevel,allergens[],imageUrl?}` | `AdminDish` |
| PUT | `/admin/dishes/{id}` | ADMIN | same shape | `AdminDish` |
| PATCH | `/admin/dishes/{id}/status` | ADMIN | `{"active":true}` | `AdminDish` |
| POST | `/admin/dishes/{id}/image` | ADMIN | `multipart: file=<image>` | `{"imageUrl":"/images/dishes/…"}` |
| GET | `/admin/menus/{date}` | ADMIN | — | `{date,entries:[{dishId,name,imageUrl,category,selected,stockTotal}]}` |
| PUT | `/admin/menus/{date}` | ADMIN | `{entries:[{dishId,selected,stockTotal}]}` | 200 |

Write operations evict the menu cache for the affected dates.

## Enums (English labels only)
| Enum | Values |
|------|--------|
| Category | `Chinese`, `Western`, `Japanese`, `Light Meal`, `Korean`, `Southeast Asian` |
| SpiceLevel | `None`, `Mild`, `Medium`, `Hot` |
| Allergen | `Peanuts`, `Dairy`, `Egg`, `Gluten`, `Soy`, `Fish`, `Shellfish` |
| TasteLevel | `Light`, `Medium`, `Heavy` |
| MealSlot | `Lunch`, `Dinner` |
| OrderStatus | `Pending`, `Confirmed`, `Completed`, `Cancelled` |
