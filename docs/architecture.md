# WeBox — Architecture Notes

## Module breakdown

```
com.webox
├─ common/
│  ├─ enums/          Role, Category, SpiceLevel, Allergen, TasteLevel, MealSlot, OrderStatus
│  ├─ api/            ApiError (record), BizException, ErrorCode, GlobalExceptionHandler
│  ├─ config/         WebConfig (CORS + interceptor reg + resource handler), CacheConfig, TimeConfig
│  ├─ money/          Moneys — BigDecimal scale-2 HALF_UP, never float. DB stores DECIMAL(10,2).
│  └─ option/         SelectedOption (record), SelectedOptionsConverter — shared by cart & order snapshots
├─ auth/              User, UserRepository, JwtService, AuthService, AuthController,
│                     AuthInterceptor (Bearer → AuthUser), AuthContext (ThreadLocal), RequireRole
├─ menu/              Dish, OptionGroup, OptionItem, DailyMenu + repos + DTOs,
│                     MenuService (cached), MenuController, MenuSpecs (Criteria search), DishSpecs (admin)
├─ cart/              CartItem, CartItemRepository, CartService, CartController
├─ order/             Order, OrderItem, OrderRepository, OrderService, OrderController,
│                     CheckoutController, OrderSlotResolver
├─ preference/        UserPreference, PreferenceRepository, PreferenceService, PreferenceController
├─ admin/             DishAdminService, DishAdminController, MenuAdminService, MenuAdminController
└─ bootstrap/         DataSeeder (ApplicationRunner, idempotent re-seed)
```

## Data flow — order placement (critical path)

```
OrderController.placeOrder(req)
 └▶ OrderService.placeOrder(userId, req)
     ├─ 1. findByIdempotencyKey → HIT: replay existing (return, NO side effects)
     ├─ 2. OrderSlotResolver.resolve(date, slot) → effective slot + switched flag
     ├─ 3. cartItemRepository.findByUserIdWithDish(userId)
     │      empty? → 400 "Your cart is empty."
     │      totalQty > 5? → 400 ORDER_LIMIT_EXCEEDED
     ├─ 4. findActiveBySlot(userId, date, slot, [PENDING, CONFIRMED])
     │      present? → 409 ORDER_EXISTS (details carry existing order id)
     ├─ 5. Build order with SNAPSHOT lines (dish name/price/options frozen at this instant)
     ├─ 6. orderRepository.saveAndFlush(order)
     │      ⚡ unique violation (concurrent twin)?
     │         → REQUIRES_NEW replay: re-query idempotency → return winner,
     │           or re-query active → throw ORDER_EXISTS, else re-throw
     └─ 7. cartItemRepository.deleteByUserId(userId)
```

## Idempotency & one-active-order per slot — database-level enforcement

| Guarantee | Mechanism |
|---|---|
| One order per `idempotencyKey` | `UNIQUE (idempotency_key)` on `orders`. Concurrent insert → `DataIntegrityViolationException` caught and replayed via `REQUIRES_NEW` to read the winner. |
| One Pending/Confirmed order per `(user, date, meal_slot)` | MySQL **generated column**: `active_key VARCHAR GENERATED ALWAYS AS (IF(status IN ('PENDING','CONFIRMED'), CONCAT(user_id,'\|',delivery_date,'\|',meal_slot), NULL)) STORED` with `UNIQUE (active_key)`. Cancelled/Completed rows map to NULL → never collide. Multiple NULLs are allowed by MySQL's unique index. |

## Cache strategy

| Cache | Key | Eviction |
|---|---|---|
| `menuItems` | date (`LocalDate`) | Admin dish writes + daily-menu writes swipe `allEntries`; menu-items for a specific date evicted on daily-menu save by key. |
| `dishDetail` | dish id | Evicted (all entries) on dish write or image upload. |

Backed by Caffeine (in-process), 10-min TTL as safety net. Explicit eviction on every write keeps the cache fresh during the peak window and prevents stale stock/menu visibility.

## Order line snapshots

`order_items` stores `dish_name`, `unit_price`, and `options_json` as copies taken at checkout.
`order_items.dish_id` is kept as a soft reference for the business dashboard but is never used
to recompute anything from the current dish row — dish edits, renames, or price changes never
mutate history.

## Authorization

`AuthInterceptor` (registered for `/api/**` excluding `/auth/login`, `/auth/register`) parses
the Bearer JWT and stores the principal into `AuthContext` (a `ThreadLocal<AuthUser>`).
`@RequireRole(ADMIN)` on a controller class or individual method is checked by the interceptor;
a mismatch throws 403.  The interceptor layer is the ONLY place authentication happens —
services receive a userId and trust the caller.

## Deferred (Tier 3, designed but not implemented)

- **Stock deduction**: `daily_menus.stock_remaining` is populated and displayed but not
  decremented on order placement.  The intended atomic guard is
  `UPDATE daily_menus SET stock_remaining = stock_remaining - ? WHERE id = ? AND stock_remaining >= ?`
  (checking affected row count) — no migration needed.
- **Real-time stock push**: SSE via `SseEmitter`; emit events after order/cancel.
- **AI recommendation**: OpenAI-compatible streaming endpoint → SSE proxy to the browser,
  candidate dishes filtered by stock > 0, no user allergens, not in recent 7-day orders.
- **Business dashboard**: aggregates from `orders` + `order_items` with Recharts on the frontend.
