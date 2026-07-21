# Diff Details

Date : 2026-07-21 15:07:07

Directory /home/jinfei/桌面/WeBox-PRD-候选人交付包./WeBox-PRD-候选人交付包/webox/web

Total : 111 files,  1475 codes, -103 comments, -454 blanks, all 918 lines

[Summary](results.md) / [Details](details.md) / [Diff Summary](diff.md) / Diff Details

## Files
| filename | language | code | comment | blank | total |
| :--- | :--- | ---: | ---: | ---: | ---: |
| [server/pom.xml](/server/pom.xml) | XML | -81 | -2 | -6 | -89 |
| [server/src/main/java/com/webox/WeboxApplication.java](/server/src/main/java/com/webox/WeboxApplication.java) | Java | -11 | 0 | -4 | -15 |
| [server/src/main/java/com/webox/admin/AdminPingController.java](/server/src/main/java/com/webox/admin/AdminPingController.java) | Java | -16 | -1 | -5 | -22 |
| [server/src/main/java/com/webox/auth/AuthContext.java](/server/src/main/java/com/webox/auth/AuthContext.java) | Java | -24 | -2 | -9 | -35 |
| [server/src/main/java/com/webox/auth/AuthController.java](/server/src/main/java/com/webox/auth/AuthController.java) | Java | -31 | -1 | -8 | -40 |
| [server/src/main/java/com/webox/auth/AuthInterceptor.java](/server/src/main/java/com/webox/auth/AuthInterceptor.java) | Java | -50 | -4 | -9 | -63 |
| [server/src/main/java/com/webox/auth/AuthService.java](/server/src/main/java/com/webox/auth/AuthService.java) | Java | -52 | -1 | -10 | -63 |
| [server/src/main/java/com/webox/auth/AuthUser.java](/server/src/main/java/com/webox/auth/AuthUser.java) | Java | -4 | -1 | -3 | -8 |
| [server/src/main/java/com/webox/auth/JwtService.java](/server/src/main/java/com/webox/auth/JwtService.java) | Java | -43 | -2 | -8 | -53 |
| [server/src/main/java/com/webox/auth/RequireRole.java](/server/src/main/java/com/webox/auth/RequireRole.java) | Java | -11 | -4 | -5 | -20 |
| [server/src/main/java/com/webox/auth/User.java](/server/src/main/java/com/webox/auth/User.java) | Java | -58 | -2 | -18 | -78 |
| [server/src/main/java/com/webox/auth/UserRepository.java](/server/src/main/java/com/webox/auth/UserRepository.java) | Java | -7 | 0 | -6 | -13 |
| [server/src/main/java/com/webox/auth/dto/AuthResponse.java](/server/src/main/java/com/webox/auth/dto/AuthResponse.java) | Java | -6 | 0 | -4 | -10 |
| [server/src/main/java/com/webox/auth/dto/LoginRequest.java](/server/src/main/java/com/webox/auth/dto/LoginRequest.java) | Java | -11 | 0 | -4 | -15 |
| [server/src/main/java/com/webox/auth/dto/RegisterRequest.java](/server/src/main/java/com/webox/auth/dto/RegisterRequest.java) | Java | -16 | -1 | -4 | -21 |
| [server/src/main/java/com/webox/bootstrap/DataSeeder.java](/server/src/main/java/com/webox/bootstrap/DataSeeder.java) | Java | -202 | -10 | -29 | -241 |
| [server/src/main/java/com/webox/cart/CartController.java](/server/src/main/java/com/webox/cart/CartController.java) | Java | -46 | 0 | -10 | -56 |
| [server/src/main/java/com/webox/cart/CartItem.java](/server/src/main/java/com/webox/cart/CartItem.java) | Java | -82 | -5 | -24 | -111 |
| [server/src/main/java/com/webox/cart/CartItemRepository.java](/server/src/main/java/com/webox/cart/CartItemRepository.java) | Java | -15 | 0 | -9 | -24 |
| [server/src/main/java/com/webox/cart/CartService.java](/server/src/main/java/com/webox/cart/CartService.java) | Java | -143 | -10 | -21 | -174 |
| [server/src/main/java/com/webox/cart/dto/AddCartItemRequest.java](/server/src/main/java/com/webox/cart/dto/AddCartItemRequest.java) | Java | -18 | -1 | -8 | -27 |
| [server/src/main/java/com/webox/cart/dto/AddCartItemResponse.java](/server/src/main/java/com/webox/cart/dto/AddCartItemResponse.java) | Java | -5 | -1 | -4 | -10 |
| [server/src/main/java/com/webox/cart/dto/CartItemView.java](/server/src/main/java/com/webox/cart/dto/CartItemView.java) | Java | -36 | -1 | -5 | -42 |
| [server/src/main/java/com/webox/cart/dto/CartView.java](/server/src/main/java/com/webox/cart/dto/CartView.java) | Java | -13 | 0 | -5 | -18 |
| [server/src/main/java/com/webox/cart/dto/UpdateCartItemRequest.java](/server/src/main/java/com/webox/cart/dto/UpdateCartItemRequest.java) | Java | -10 | 0 | -3 | -13 |
| [server/src/main/java/com/webox/common/api/ApiError.java](/server/src/main/java/com/webox/common/api/ApiError.java) | Java | -13 | -4 | -6 | -23 |
| [server/src/main/java/com/webox/common/api/BizException.java](/server/src/main/java/com/webox/common/api/BizException.java) | Java | -27 | -1 | -9 | -37 |
| [server/src/main/java/com/webox/common/api/ErrorCode.java](/server/src/main/java/com/webox/common/api/ErrorCode.java) | Java | -28 | -4 | -10 | -42 |
| [server/src/main/java/com/webox/common/api/GlobalExceptionHandler.java](/server/src/main/java/com/webox/common/api/GlobalExceptionHandler.java) | Java | -46 | -1 | -11 | -58 |
| [server/src/main/java/com/webox/common/config/CacheConfig.java](/server/src/main/java/com/webox/common/config/CacheConfig.java) | Java | -20 | -4 | -6 | -30 |
| [server/src/main/java/com/webox/common/config/TimeConfig.java](/server/src/main/java/com/webox/common/config/TimeConfig.java) | Java | -11 | -1 | -5 | -17 |
| [server/src/main/java/com/webox/common/config/WebConfig.java](/server/src/main/java/com/webox/common/config/WebConfig.java) | Java | -27 | -5 | -7 | -39 |
| [server/src/main/java/com/webox/common/enums/Allergen.java](/server/src/main/java/com/webox/common/enums/Allergen.java) | Java | -29 | -4 | -7 | -40 |
| [server/src/main/java/com/webox/common/enums/Category.java](/server/src/main/java/com/webox/common/enums/Category.java) | Java | -28 | -1 | -7 | -36 |
| [server/src/main/java/com/webox/common/enums/MealSlot.java](/server/src/main/java/com/webox/common/enums/MealSlot.java) | Java | -24 | -1 | -7 | -32 |
| [server/src/main/java/com/webox/common/enums/OrderStatus.java](/server/src/main/java/com/webox/common/enums/OrderStatus.java) | Java | -29 | -1 | -8 | -38 |
| [server/src/main/java/com/webox/common/enums/Role.java](/server/src/main/java/com/webox/common/enums/Role.java) | Java | -24 | -1 | -7 | -32 |
| [server/src/main/java/com/webox/common/enums/SpiceLevel.java](/server/src/main/java/com/webox/common/enums/SpiceLevel.java) | Java | -26 | -1 | -7 | -34 |
| [server/src/main/java/com/webox/common/money/Moneys.java](/server/src/main/java/com/webox/common/money/Moneys.java) | Java | -17 | -6 | -8 | -31 |
| [server/src/main/java/com/webox/common/option/SelectedOption.java](/server/src/main/java/com/webox/common/option/SelectedOption.java) | Java | -5 | -1 | -3 | -9 |
| [server/src/main/java/com/webox/common/option/SelectedOptionsConverter.java](/server/src/main/java/com/webox/common/option/SelectedOptionsConverter.java) | Java | -31 | -1 | -7 | -39 |
| [server/src/main/java/com/webox/menu/AllergenListConverter.java](/server/src/main/java/com/webox/menu/AllergenListConverter.java) | Java | -32 | -4 | -7 | -43 |
| [server/src/main/java/com/webox/menu/DailyMenu.java](/server/src/main/java/com/webox/menu/DailyMenu.java) | Java | -56 | -4 | -18 | -78 |
| [server/src/main/java/com/webox/menu/DailyMenuRepository.java](/server/src/main/java/com/webox/menu/DailyMenuRepository.java) | Java | -13 | 0 | -7 | -20 |
| [server/src/main/java/com/webox/menu/Dish.java](/server/src/main/java/com/webox/menu/Dish.java) | Java | -130 | -3 | -40 | -173 |
| [server/src/main/java/com/webox/menu/DishRepository.java](/server/src/main/java/com/webox/menu/DishRepository.java) | Java | -4 | 0 | -3 | -7 |
| [server/src/main/java/com/webox/menu/MenuController.java](/server/src/main/java/com/webox/menu/MenuController.java) | Java | -49 | -1 | -10 | -60 |
| [server/src/main/java/com/webox/menu/MenuService.java](/server/src/main/java/com/webox/menu/MenuService.java) | Java | -62 | -2 | -11 | -75 |
| [server/src/main/java/com/webox/menu/MenuSpecs.java](/server/src/main/java/com/webox/menu/MenuSpecs.java) | Java | -36 | -6 | -8 | -50 |
| [server/src/main/java/com/webox/menu/OptionGroup.java](/server/src/main/java/com/webox/menu/OptionGroup.java) | Java | -67 | -4 | -21 | -92 |
| [server/src/main/java/com/webox/menu/OptionGroupRepository.java](/server/src/main/java/com/webox/menu/OptionGroupRepository.java) | Java | -10 | -1 | -5 | -16 |
| [server/src/main/java/com/webox/menu/OptionItem.java](/server/src/main/java/com/webox/menu/OptionItem.java) | Java | -46 | -1 | -15 | -62 |
| [server/src/main/java/com/webox/menu/dto/DishDetailView.java](/server/src/main/java/com/webox/menu/dto/DishDetailView.java) | Java | -45 | -1 | -7 | -53 |
| [server/src/main/java/com/webox/menu/dto/MenuItemView.java](/server/src/main/java/com/webox/menu/dto/MenuItemView.java) | Java | -36 | -2 | -5 | -43 |
| [server/src/main/java/com/webox/menu/dto/MenuResponse.java](/server/src/main/java/com/webox/menu/dto/MenuResponse.java) | Java | -5 | 0 | -3 | -8 |
| [server/src/main/java/com/webox/order/CheckoutController.java](/server/src/main/java/com/webox/order/CheckoutController.java) | Java | -30 | -1 | -8 | -39 |
| [server/src/main/java/com/webox/order/Order.java](/server/src/main/java/com/webox/order/Order.java) | Java | -137 | -7 | -39 | -183 |
| [server/src/main/java/com/webox/order/OrderController.java](/server/src/main/java/com/webox/order/OrderController.java) | Java | -37 | 0 | -10 | -47 |
| [server/src/main/java/com/webox/order/OrderItem.java](/server/src/main/java/com/webox/order/OrderItem.java) | Java | -83 | -5 | -27 | -115 |
| [server/src/main/java/com/webox/order/OrderRepository.java](/server/src/main/java/com/webox/order/OrderRepository.java) | Java | -27 | -1 | -10 | -38 |
| [server/src/main/java/com/webox/order/OrderService.java](/server/src/main/java/com/webox/order/OrderService.java) | Java | -178 | -19 | -25 | -222 |
| [server/src/main/java/com/webox/order/OrderSlotResolver.java](/server/src/main/java/com/webox/order/OrderSlotResolver.java) | Java | -51 | -11 | -12 | -74 |
| [server/src/main/java/com/webox/order/dto/CheckoutSummaryView.java](/server/src/main/java/com/webox/order/dto/CheckoutSummaryView.java) | Java | -17 | -5 | -5 | -27 |
| [server/src/main/java/com/webox/order/dto/OrderSummaryView.java](/server/src/main/java/com/webox/order/dto/OrderSummaryView.java) | Java | -28 | -1 | -5 | -34 |
| [server/src/main/java/com/webox/order/dto/OrderView.java](/server/src/main/java/com/webox/order/dto/OrderView.java) | Java | -50 | -1 | -7 | -58 |
| [server/src/main/java/com/webox/order/dto/PlaceOrderRequest.java](/server/src/main/java/com/webox/order/dto/PlaceOrderRequest.java) | Java | -15 | -3 | -7 | -25 |
| [server/src/main/resources/application.yml](/server/src/main/resources/application.yml) | YAML | -25 | -1 | -3 | -29 |
| [web/.oxlintrc.json](/web/.oxlintrc.json) | JSON | 8 | 0 | 1 | 9 |
| [web/README.md](/web/README.md) | Markdown | 23 | 0 | 10 | 33 |
| [web/index.html](/web/index.html) | HTML | 13 | 0 | 1 | 14 |
| [web/package-lock.json](/web/package-lock.json) | JSON | 2,485 | 0 | 1 | 2,486 |
| [web/package.json](/web/package.json) | JSON | 32 | 0 | 1 | 33 |
| [web/public/favicon.svg](/web/public/favicon.svg) | XML | 1 | 0 | 0 | 1 |
| [web/public/icons.svg](/web/public/icons.svg) | XML | 24 | 0 | 1 | 25 |
| [web/src/api/auth.ts](/web/src/api/auth.ts) | TypeScript | 26 | 0 | 5 | 31 |
| [web/src/api/cart.ts](/web/src/api/cart.ts) | TypeScript | 49 | 1 | 10 | 60 |
| [web/src/api/client.ts](/web/src/api/client.ts) | TypeScript | 38 | 5 | 6 | 49 |
| [web/src/api/menu.ts](/web/src/api/menu.ts) | TypeScript | 63 | 0 | 9 | 72 |
| [web/src/api/orders.ts](/web/src/api/orders.ts) | TypeScript | 52 | 3 | 7 | 62 |
| [web/src/components/AuthShell.tsx](/web/src/components/AuthShell.tsx) | TypeScript JSX | 17 | 1 | 2 | 20 |
| [web/src/components/ConfirmDialog.tsx](/web/src/components/ConfirmDialog.tsx) | TypeScript JSX | 39 | 1 | 2 | 42 |
| [web/src/components/DishCard.tsx](/web/src/components/DishCard.tsx) | TypeScript JSX | 65 | 2 | 5 | 72 |
| [web/src/components/PlaceholderPage.tsx](/web/src/components/PlaceholderPage.tsx) | TypeScript JSX | 12 | 1 | 2 | 15 |
| [web/src/components/RequireAuth.tsx](/web/src/components/RequireAuth.tsx) | TypeScript JSX | 18 | 1 | 4 | 23 |
| [web/src/hooks/useAddToCart.tsx](/web/src/hooks/useAddToCart.tsx) | TypeScript JSX | 39 | 5 | 6 | 50 |
| [web/src/index.css](/web/src/index.css) | PostCSS | 4 | 5 | 3 | 12 |
| [web/src/layouts/ConsoleLayout.tsx](/web/src/layouts/ConsoleLayout.tsx) | TypeScript JSX | 42 | 1 | 4 | 47 |
| [web/src/layouts/EmployeeLayout.tsx](/web/src/layouts/EmployeeLayout.tsx) | TypeScript JSX | 63 | 1 | 5 | 69 |
| [web/src/lib/errors.ts](/web/src/lib/errors.ts) | TypeScript | 12 | 1 | 2 | 15 |
| [web/src/lib/money.ts](/web/src/lib/money.ts) | TypeScript | 9 | 7 | 4 | 20 |
| [web/src/lib/validators.ts](/web/src/lib/validators.ts) | TypeScript | 16 | 3 | 7 | 26 |
| [web/src/main.tsx](/web/src/main.tsx) | TypeScript JSX | 20 | 0 | 3 | 23 |
| [web/src/pages/CartPage.tsx](/web/src/pages/CartPage.tsx) | TypeScript JSX | 116 | 1 | 12 | 129 |
| [web/src/pages/CheckoutPage.tsx](/web/src/pages/CheckoutPage.tsx) | TypeScript JSX | 165 | 6 | 12 | 183 |
| [web/src/pages/DishDetailPage.tsx](/web/src/pages/DishDetailPage.tsx) | TypeScript JSX | 156 | 6 | 12 | 174 |
| [web/src/pages/LoginPage.tsx](/web/src/pages/LoginPage.tsx) | TypeScript JSX | 95 | 0 | 7 | 102 |
| [web/src/pages/MenuPage.tsx](/web/src/pages/MenuPage.tsx) | TypeScript JSX | 93 | 3 | 14 | 110 |
| [web/src/pages/OrderDetailPage.tsx](/web/src/pages/OrderDetailPage.tsx) | TypeScript JSX | 4 | 0 | 2 | 6 |
| [web/src/pages/OrderSuccessPage.tsx](/web/src/pages/OrderSuccessPage.tsx) | TypeScript JSX | 51 | 1 | 6 | 58 |
| [web/src/pages/OrdersPage.tsx](/web/src/pages/OrdersPage.tsx) | TypeScript JSX | 4 | 0 | 2 | 6 |
| [web/src/pages/RegisterPage.tsx](/web/src/pages/RegisterPage.tsx) | TypeScript JSX | 111 | 0 | 8 | 119 |
| [web/src/pages/SettingsPage.tsx](/web/src/pages/SettingsPage.tsx) | TypeScript JSX | 4 | 0 | 2 | 6 |
| [web/src/pages/console/ConsoleDashboardPage.tsx](/web/src/pages/console/ConsoleDashboardPage.tsx) | TypeScript JSX | 4 | 0 | 2 | 6 |
| [web/src/pages/console/ConsoleDishesPage.tsx](/web/src/pages/console/ConsoleDishesPage.tsx) | TypeScript JSX | 4 | 0 | 2 | 6 |
| [web/src/pages/console/ConsoleMenusPage.tsx](/web/src/pages/console/ConsoleMenusPage.tsx) | TypeScript JSX | 4 | 0 | 2 | 6 |
| [web/src/router.tsx](/web/src/router.tsx) | TypeScript JSX | 53 | 4 | 2 | 59 |
| [web/src/stores/authStore.ts](/web/src/stores/authStore.ts) | TypeScript | 26 | 1 | 5 | 32 |
| [web/tsconfig.app.json](/web/tsconfig.app.json) | JSON | 22 | 2 | 3 | 27 |
| [web/tsconfig.json](/web/tsconfig.json) | JSON with Comments | 7 | 0 | 1 | 8 |
| [web/tsconfig.node.json](/web/tsconfig.node.json) | JSON | 19 | 2 | 3 | 24 |
| [web/vite.config.ts](/web/vite.config.ts) | TypeScript | 12 | 3 | 2 | 17 |

[Summary](results.md) / [Details](details.md) / [Diff Summary](diff.md) / Diff Details