# Details

Date : 2026-07-21 15:06:52

Directory /home/jinfei/桌面/WeBox-PRD-候选人交付包./WeBox-PRD-候选人交付包/webox/server

Total : 67 files,  2645 codes, 170 comments, 654 blanks, all 3469 lines

[Summary](results.md) / Details / [Diff Summary](diff.md) / [Diff Details](diff-details.md)

## Files
| filename | language | code | comment | blank | total |
| :--- | :--- | ---: | ---: | ---: | ---: |
| [server/pom.xml](/server/pom.xml) | XML | 81 | 2 | 6 | 89 |
| [server/src/main/java/com/webox/WeboxApplication.java](/server/src/main/java/com/webox/WeboxApplication.java) | Java | 11 | 0 | 4 | 15 |
| [server/src/main/java/com/webox/admin/AdminPingController.java](/server/src/main/java/com/webox/admin/AdminPingController.java) | Java | 16 | 1 | 5 | 22 |
| [server/src/main/java/com/webox/auth/AuthContext.java](/server/src/main/java/com/webox/auth/AuthContext.java) | Java | 24 | 2 | 9 | 35 |
| [server/src/main/java/com/webox/auth/AuthController.java](/server/src/main/java/com/webox/auth/AuthController.java) | Java | 31 | 1 | 8 | 40 |
| [server/src/main/java/com/webox/auth/AuthInterceptor.java](/server/src/main/java/com/webox/auth/AuthInterceptor.java) | Java | 50 | 4 | 9 | 63 |
| [server/src/main/java/com/webox/auth/AuthService.java](/server/src/main/java/com/webox/auth/AuthService.java) | Java | 52 | 1 | 10 | 63 |
| [server/src/main/java/com/webox/auth/AuthUser.java](/server/src/main/java/com/webox/auth/AuthUser.java) | Java | 4 | 1 | 3 | 8 |
| [server/src/main/java/com/webox/auth/JwtService.java](/server/src/main/java/com/webox/auth/JwtService.java) | Java | 43 | 2 | 8 | 53 |
| [server/src/main/java/com/webox/auth/RequireRole.java](/server/src/main/java/com/webox/auth/RequireRole.java) | Java | 11 | 4 | 5 | 20 |
| [server/src/main/java/com/webox/auth/User.java](/server/src/main/java/com/webox/auth/User.java) | Java | 58 | 2 | 18 | 78 |
| [server/src/main/java/com/webox/auth/UserRepository.java](/server/src/main/java/com/webox/auth/UserRepository.java) | Java | 7 | 0 | 6 | 13 |
| [server/src/main/java/com/webox/auth/dto/AuthResponse.java](/server/src/main/java/com/webox/auth/dto/AuthResponse.java) | Java | 6 | 0 | 4 | 10 |
| [server/src/main/java/com/webox/auth/dto/LoginRequest.java](/server/src/main/java/com/webox/auth/dto/LoginRequest.java) | Java | 11 | 0 | 4 | 15 |
| [server/src/main/java/com/webox/auth/dto/RegisterRequest.java](/server/src/main/java/com/webox/auth/dto/RegisterRequest.java) | Java | 16 | 1 | 4 | 21 |
| [server/src/main/java/com/webox/bootstrap/DataSeeder.java](/server/src/main/java/com/webox/bootstrap/DataSeeder.java) | Java | 202 | 10 | 29 | 241 |
| [server/src/main/java/com/webox/cart/CartController.java](/server/src/main/java/com/webox/cart/CartController.java) | Java | 46 | 0 | 10 | 56 |
| [server/src/main/java/com/webox/cart/CartItem.java](/server/src/main/java/com/webox/cart/CartItem.java) | Java | 82 | 5 | 24 | 111 |
| [server/src/main/java/com/webox/cart/CartItemRepository.java](/server/src/main/java/com/webox/cart/CartItemRepository.java) | Java | 15 | 0 | 9 | 24 |
| [server/src/main/java/com/webox/cart/CartService.java](/server/src/main/java/com/webox/cart/CartService.java) | Java | 143 | 10 | 21 | 174 |
| [server/src/main/java/com/webox/cart/dto/AddCartItemRequest.java](/server/src/main/java/com/webox/cart/dto/AddCartItemRequest.java) | Java | 18 | 1 | 8 | 27 |
| [server/src/main/java/com/webox/cart/dto/AddCartItemResponse.java](/server/src/main/java/com/webox/cart/dto/AddCartItemResponse.java) | Java | 5 | 1 | 4 | 10 |
| [server/src/main/java/com/webox/cart/dto/CartItemView.java](/server/src/main/java/com/webox/cart/dto/CartItemView.java) | Java | 36 | 1 | 5 | 42 |
| [server/src/main/java/com/webox/cart/dto/CartView.java](/server/src/main/java/com/webox/cart/dto/CartView.java) | Java | 13 | 0 | 5 | 18 |
| [server/src/main/java/com/webox/cart/dto/UpdateCartItemRequest.java](/server/src/main/java/com/webox/cart/dto/UpdateCartItemRequest.java) | Java | 10 | 0 | 3 | 13 |
| [server/src/main/java/com/webox/common/api/ApiError.java](/server/src/main/java/com/webox/common/api/ApiError.java) | Java | 13 | 4 | 6 | 23 |
| [server/src/main/java/com/webox/common/api/BizException.java](/server/src/main/java/com/webox/common/api/BizException.java) | Java | 27 | 1 | 9 | 37 |
| [server/src/main/java/com/webox/common/api/ErrorCode.java](/server/src/main/java/com/webox/common/api/ErrorCode.java) | Java | 28 | 4 | 10 | 42 |
| [server/src/main/java/com/webox/common/api/GlobalExceptionHandler.java](/server/src/main/java/com/webox/common/api/GlobalExceptionHandler.java) | Java | 46 | 1 | 11 | 58 |
| [server/src/main/java/com/webox/common/config/CacheConfig.java](/server/src/main/java/com/webox/common/config/CacheConfig.java) | Java | 20 | 4 | 6 | 30 |
| [server/src/main/java/com/webox/common/config/TimeConfig.java](/server/src/main/java/com/webox/common/config/TimeConfig.java) | Java | 11 | 1 | 5 | 17 |
| [server/src/main/java/com/webox/common/config/WebConfig.java](/server/src/main/java/com/webox/common/config/WebConfig.java) | Java | 27 | 5 | 7 | 39 |
| [server/src/main/java/com/webox/common/enums/Allergen.java](/server/src/main/java/com/webox/common/enums/Allergen.java) | Java | 29 | 4 | 7 | 40 |
| [server/src/main/java/com/webox/common/enums/Category.java](/server/src/main/java/com/webox/common/enums/Category.java) | Java | 28 | 1 | 7 | 36 |
| [server/src/main/java/com/webox/common/enums/MealSlot.java](/server/src/main/java/com/webox/common/enums/MealSlot.java) | Java | 24 | 1 | 7 | 32 |
| [server/src/main/java/com/webox/common/enums/OrderStatus.java](/server/src/main/java/com/webox/common/enums/OrderStatus.java) | Java | 29 | 1 | 8 | 38 |
| [server/src/main/java/com/webox/common/enums/Role.java](/server/src/main/java/com/webox/common/enums/Role.java) | Java | 24 | 1 | 7 | 32 |
| [server/src/main/java/com/webox/common/enums/SpiceLevel.java](/server/src/main/java/com/webox/common/enums/SpiceLevel.java) | Java | 26 | 1 | 7 | 34 |
| [server/src/main/java/com/webox/common/money/Moneys.java](/server/src/main/java/com/webox/common/money/Moneys.java) | Java | 17 | 6 | 8 | 31 |
| [server/src/main/java/com/webox/common/option/SelectedOption.java](/server/src/main/java/com/webox/common/option/SelectedOption.java) | Java | 5 | 1 | 3 | 9 |
| [server/src/main/java/com/webox/common/option/SelectedOptionsConverter.java](/server/src/main/java/com/webox/common/option/SelectedOptionsConverter.java) | Java | 31 | 1 | 7 | 39 |
| [server/src/main/java/com/webox/menu/AllergenListConverter.java](/server/src/main/java/com/webox/menu/AllergenListConverter.java) | Java | 32 | 4 | 7 | 43 |
| [server/src/main/java/com/webox/menu/DailyMenu.java](/server/src/main/java/com/webox/menu/DailyMenu.java) | Java | 56 | 4 | 18 | 78 |
| [server/src/main/java/com/webox/menu/DailyMenuRepository.java](/server/src/main/java/com/webox/menu/DailyMenuRepository.java) | Java | 13 | 0 | 7 | 20 |
| [server/src/main/java/com/webox/menu/Dish.java](/server/src/main/java/com/webox/menu/Dish.java) | Java | 130 | 3 | 40 | 173 |
| [server/src/main/java/com/webox/menu/DishRepository.java](/server/src/main/java/com/webox/menu/DishRepository.java) | Java | 4 | 0 | 3 | 7 |
| [server/src/main/java/com/webox/menu/MenuController.java](/server/src/main/java/com/webox/menu/MenuController.java) | Java | 49 | 1 | 10 | 60 |
| [server/src/main/java/com/webox/menu/MenuService.java](/server/src/main/java/com/webox/menu/MenuService.java) | Java | 62 | 2 | 11 | 75 |
| [server/src/main/java/com/webox/menu/MenuSpecs.java](/server/src/main/java/com/webox/menu/MenuSpecs.java) | Java | 36 | 6 | 8 | 50 |
| [server/src/main/java/com/webox/menu/OptionGroup.java](/server/src/main/java/com/webox/menu/OptionGroup.java) | Java | 67 | 4 | 21 | 92 |
| [server/src/main/java/com/webox/menu/OptionGroupRepository.java](/server/src/main/java/com/webox/menu/OptionGroupRepository.java) | Java | 10 | 1 | 5 | 16 |
| [server/src/main/java/com/webox/menu/OptionItem.java](/server/src/main/java/com/webox/menu/OptionItem.java) | Java | 46 | 1 | 15 | 62 |
| [server/src/main/java/com/webox/menu/dto/DishDetailView.java](/server/src/main/java/com/webox/menu/dto/DishDetailView.java) | Java | 45 | 1 | 7 | 53 |
| [server/src/main/java/com/webox/menu/dto/MenuItemView.java](/server/src/main/java/com/webox/menu/dto/MenuItemView.java) | Java | 36 | 2 | 5 | 43 |
| [server/src/main/java/com/webox/menu/dto/MenuResponse.java](/server/src/main/java/com/webox/menu/dto/MenuResponse.java) | Java | 5 | 0 | 3 | 8 |
| [server/src/main/java/com/webox/order/CheckoutController.java](/server/src/main/java/com/webox/order/CheckoutController.java) | Java | 30 | 1 | 8 | 39 |
| [server/src/main/java/com/webox/order/Order.java](/server/src/main/java/com/webox/order/Order.java) | Java | 137 | 7 | 39 | 183 |
| [server/src/main/java/com/webox/order/OrderController.java](/server/src/main/java/com/webox/order/OrderController.java) | Java | 37 | 0 | 10 | 47 |
| [server/src/main/java/com/webox/order/OrderItem.java](/server/src/main/java/com/webox/order/OrderItem.java) | Java | 83 | 5 | 27 | 115 |
| [server/src/main/java/com/webox/order/OrderRepository.java](/server/src/main/java/com/webox/order/OrderRepository.java) | Java | 27 | 1 | 10 | 38 |
| [server/src/main/java/com/webox/order/OrderService.java](/server/src/main/java/com/webox/order/OrderService.java) | Java | 178 | 19 | 25 | 222 |
| [server/src/main/java/com/webox/order/OrderSlotResolver.java](/server/src/main/java/com/webox/order/OrderSlotResolver.java) | Java | 51 | 11 | 12 | 74 |
| [server/src/main/java/com/webox/order/dto/CheckoutSummaryView.java](/server/src/main/java/com/webox/order/dto/CheckoutSummaryView.java) | Java | 17 | 5 | 5 | 27 |
| [server/src/main/java/com/webox/order/dto/OrderSummaryView.java](/server/src/main/java/com/webox/order/dto/OrderSummaryView.java) | Java | 28 | 1 | 5 | 34 |
| [server/src/main/java/com/webox/order/dto/OrderView.java](/server/src/main/java/com/webox/order/dto/OrderView.java) | Java | 50 | 1 | 7 | 58 |
| [server/src/main/java/com/webox/order/dto/PlaceOrderRequest.java](/server/src/main/java/com/webox/order/dto/PlaceOrderRequest.java) | Java | 15 | 3 | 7 | 25 |
| [server/src/main/resources/application.yml](/server/src/main/resources/application.yml) | YAML | 25 | 1 | 3 | 29 |

[Summary](results.md) / Details / [Diff Summary](diff.md) / [Diff Details](diff-details.md)