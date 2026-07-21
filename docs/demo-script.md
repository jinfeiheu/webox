# Demo Script (~5 minutes)

Environment: `http://localhost:5173` (frontend proxy → `:8080` backend), MySQL `:3306`.

Seeded accounts: `demo@webox.com` / `demo1234` (Employee), `admin@webox.com` / `admin1234` (Admin).

---

## 1. Employee flow (~130 s)

**Login & menu** (30 s)
- Open http://localhost:5173 → redirect to /login
- Log in as `demo@webox.com` / `demo1234`
  - Wrong password → 401 "Incorrect email or password." in red banner
  - Correct → land on **Today's Menu**, 20 dish cards with images & prices
- Show: category chips (multi-select → scrolling grid shrinks to match), search box
  (type `chicken` → 4 results, `pasta` → 1 result)

**Dish detail – customization** (25 s)
- Click "Classic Beef Burger" → detail page: Bun (required), Sauce (required), Add-ons
- Choose "Whole Wheat" + "BBQ" + "Cheese (+¥3)" → price updates live to ¥41.00
- Qty: 2 → total ¥82.00 → click "Add to cart"

**Cart & 5-item cap** (15 s)
- Top nav: **Cart** shows count badge (2)
- Cart page: option summary "Whole Wheat + BBQ + Cheese", unit ¥41, subtotal ¥82
- Add **Kung Pao Chicken** ×4 via the menu (quick-add, no options) → total 6 items
  → the 6th add is silently rejected (cart caps at 5), cart banner shows the limit
- Reduce Kung Pao to 1, keep Burger ×2 → total Qty 3, ¥104.00

**Checkout – cutoff & idempotent ordering** (30 s)
- **Go to Checkout** → if it's after 10:00, the slot auto-switches to Dinner or next-day
  Lunch with a blue banner explaining
- Delivery date & address picker (suggestions from history)
- Click **Place Order** (disabled while placing) → success page with order number,
  delivery date & slot
- **Back → Cart is empty** (order cleared it)

**My Orders & cancel** (15 s)
- **My Orders** → list with status chips & totals
- Click the order → detail page with snapshot lines (same options you selected)
- **Cancel Order** → confirm → status changes to Cancelled

**Preferences & allergen warning** (15 s)
- **Settings** → Preferences page
- Flag `Egg` as an allergen; set cuisine = `Chinese`, budget cap = ¥30
- Back to menu → toggle **For You** → Chinese dishes sort to the top, and
  spice-matching cards receive a "For you" badge
- Add **Korean Bibimbap** (contains Egg) → confirm dialog appears:
  "This dish contains an allergen you flagged: [Egg]. Add anyway?" → confirm → added
- Close dialog, go to **Cart** (shows Bibimbap)

---

## 2. Admin / Console (~60 s)

- Log in as `admin@webox.com` / `admin1234`
- Top nav shows **Console** link → opens the admin layout with sidebar

**Dish management** (25 s)
- **Dishes** page: table with image, name, category, price, spice, on/off-shelf toggle
- Search `chicken` → filtered rows
- Click a row's **Edit** → modal: change price or allergens → Save
- Toggle a dish **Off shelf** → it disappears from the employee menu (refresh employee tab)
- **+ Add Dish** → fill form → Save (image upload available post-create)

**Daily menu setup** (20 s)
- **Daily Menus** page → date picker (default tomorrow)
- List of all active dishes with checkboxes & stock inputs
- Uncheck a few, change stock numbers → **Save daily menu**
- Employee menus for that date now reflect the changes (verified via /api/menu?date=...)

**Role enforcement** (10 s)
- In a private window: log in as employee → visit `/console` → redirected to `/`
  (client guard)
- `curl -H "Authorization: Bearer <emp-token>" /api/admin/dishes` → 403

---

## 3. Selling points to call out (60 s)

- **Idempotent ordering**: open DevTools Network tab, place an order with client
  throttling (Slow 3G) → double-click → only ONE order created, same response
- **Order snapshots**: edit a dish price in Console → existing order detail shows the
  old price (history preserved)
- **Active-order guard**: place an order for today lunch, try another → 409 with
  link to the existing one
- **21 tests pass:** `mvn test`
- **AI conversation**: entire Claude Code session exported raw to `ai-conversations/`
