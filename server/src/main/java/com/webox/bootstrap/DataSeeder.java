package com.webox.bootstrap;

import com.webox.auth.User;
import com.webox.auth.UserRepository;
import com.webox.common.enums.Allergen;
import com.webox.common.enums.Category;
import com.webox.common.enums.Role;
import com.webox.common.enums.SpiceLevel;
import com.webox.common.money.Moneys;
import com.webox.menu.DailyMenu;
import com.webox.menu.DailyMenuRepository;
import com.webox.menu.Dish;
import com.webox.menu.DishRepository;
import com.webox.menu.OptionGroup;
import com.webox.menu.OptionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds demo data on startup (idempotent):
 * - accounts admin@webox.com/admin1234 (ADMIN) and demo@webox.com/demo1234 (EMPLOYEE)
 * - 20 dishes: the 9 from PRD §6 (with customization groups) + 11 supplementary
 * - today's AND tomorrow's daily menu with stock 10 per dish — so the demo can order,
 *   deduct stock and feed the dashboard on day one (PRD §4.3 数据初始化说明).
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final int DAILY_STOCK = 10;
    private static final String IMG = "/images/dishes/dish-%02d.jpg";

    private final UserRepository userRepository;
    private final DishRepository dishRepository;
    private final DailyMenuRepository dailyMenuRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataSeeder(UserRepository userRepository, DishRepository dishRepository,
                      DailyMenuRepository dailyMenuRepository) {
        this.userRepository = userRepository;
        this.dishRepository = dishRepository;
        this.dailyMenuRepository = dailyMenuRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUsers();
        List<Dish> dishes = seedDishes();
        seedDailyMenus(dishes);
    }

    private void seedUsers() {
        createUserIfAbsent("admin@webox.com", "admin1234", Role.ADMIN);
        createUserIfAbsent("demo@webox.com", "demo1234", Role.EMPLOYEE);
    }

    private void createUserIfAbsent(String email, String rawPassword, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);
        log.info("Seeded account {} ({})", email, role);
    }

    private List<Dish> seedDishes() {
        if (dishRepository.count() > 0) {
            return dishRepository.findAll();
        }

        // ---- PRD §6 dishes (1-9) ----
        dish("Kung Pao Chicken", "Classic Sichuan dish, chicken stir-fried with peanuts and dried chili",
                "22.00", Category.CHINESE, "Chicken", SpiceLevel.MEDIUM, 1,
                List.of(Allergen.PEANUTS));

        dish("Caesar Salad", "Fresh romaine lettuce with Parmesan and Caesar dressing",
                "28.50", Category.LIGHT_MEAL, "None", SpiceLevel.NONE, 2,
                List.of(Allergen.DAIRY, Allergen.EGG),
                group("Add-ons", false,
                        item("Grilled Chicken", "6"), item("Bacon", "5"), item("Avocado", "4")));

        dish("Salmon Sashimi Set", "Fresh salmon sashimi with rice and miso soup",
                "45.00", Category.JAPANESE, "Fish", SpiceLevel.NONE, 3,
                List.of(Allergen.FISH));

        dish("Tomato Pasta", "Classic Italian tomato pasta with fresh basil",
                "26.50", Category.WESTERN, "None", SpiceLevel.NONE, 4,
                List.of(Allergen.GLUTEN),
                group("Pasta Type", true, item("Spaghetti"), item("Fusilli"), item("Penne")),
                group("Add-ons", false, item("Bacon", "5"), item("Cheese", "3")));

        dish("Tom Yum Soup", "Thai hot & sour shrimp soup with lemongrass, galangal, lime leaves",
                "32.00", Category.SOUTHEAST_ASIAN, "Shrimp", SpiceLevel.HOT, 5,
                List.of(Allergen.SHELLFISH));

        dish("Chicken Quinoa Bowl", "Low-fat high-protein grilled chicken with quinoa, avocado, veggies",
                "35.80", Category.LIGHT_MEAL, "Chicken", SpiceLevel.NONE, 6,
                List.of(),
                group("Base", true, item("Quinoa"), item("Brown Rice"), item("Mixed Grains")));

        dish("Mapo Tofu", "Sichuan classic, soft tofu with spicy minced meat",
                "18.00", Category.CHINESE, "Tofu, Pork", SpiceLevel.MEDIUM, 7,
                List.of(Allergen.SOY));

        dish("Korean Bibimbap", "Stone-pot mixed rice with veggies, fried egg, and chili sauce",
                "30.00", Category.KOREAN, "Egg", SpiceLevel.MILD, 8,
                List.of(Allergen.EGG, Allergen.SOY),
                group("Add-ons", false,
                        item("Cheese", "3"), item("Fried Egg", "2"), item("Beef Slices", "8")));

        dish("Classic Beef Burger", "Angus beef patty with lettuce, tomato, onion",
                "38.00", Category.WESTERN, "Beef", SpiceLevel.NONE, 9,
                List.of(Allergen.GLUTEN, Allergen.DAIRY),
                group("Bun", true, item("Plain"), item("Whole Wheat")),
                group("Sauce", true, item("Ketchup"), item("Mustard"), item("Mayo"), item("BBQ")),
                group("Add-ons", false, item("Cheese", "3"), item("Bacon", "5"), item("Fried Egg", "2")));

        // ---- Supplementary dishes (10-20), same enums, no option groups ----
        dish("Beef Noodle Soup", "Braised beef brisket with hand-pulled noodles in rich broth",
                "32.00", Category.CHINESE, "Beef", SpiceLevel.MILD, 10, List.of(Allergen.GLUTEN));
        dish("Grilled Chicken Sandwich", "Charcoal-grilled chicken with lettuce and chipotle mayo",
                "29.50", Category.WESTERN, "Chicken", SpiceLevel.NONE, 11,
                List.of(Allergen.GLUTEN, Allergen.DAIRY));
        dish("Teriyaki Chicken Don", "Grilled chicken glazed with teriyaki sauce over steamed rice",
                "33.00", Category.JAPANESE, "Chicken", SpiceLevel.NONE, 12, List.of(Allergen.SOY));
        dish("Greek Yogurt Bowl", "Greek yogurt with granola, honey and seasonal berries",
                "22.00", Category.LIGHT_MEAL, "None", SpiceLevel.NONE, 13, List.of(Allergen.DAIRY));
        dish("Kimchi Fried Rice", "Wok-fried rice with aged kimchi, pork belly and a fried egg",
                "26.00", Category.KOREAN, "Pork", SpiceLevel.MEDIUM, 14,
                List.of(Allergen.SOY, Allergen.EGG));
        dish("Pad Thai", "Stir-fried rice noodles with shrimp, peanuts, bean sprouts and tamarind",
                "34.00", Category.SOUTHEAST_ASIAN, "Shrimp", SpiceLevel.MILD, 15,
                List.of(Allergen.PEANUTS, Allergen.SHELLFISH, Allergen.EGG));
        dish("Spicy Cumin Lamb", "Sizzling lamb slices tossed with cumin and dried chili",
                "42.00", Category.CHINESE, "Lamb", SpiceLevel.HOT, 16, List.of());
        dish("Mushroom Risotto", "Creamy Arborio rice with porcini mushrooms and Parmesan",
                "36.50", Category.WESTERN, "None", SpiceLevel.NONE, 17, List.of(Allergen.DAIRY));
        dish("Tuna Poke Bowl", "Marinated tuna cubes with sushi rice, edamame and seaweed",
                "39.00", Category.JAPANESE, "Fish", SpiceLevel.NONE, 18,
                List.of(Allergen.FISH, Allergen.SOY));
        dish("Avocado Toast Set", "Sourdough toast with smashed avocado, poached egg and greens",
                "24.50", Category.LIGHT_MEAL, "Egg", SpiceLevel.NONE, 19,
                List.of(Allergen.GLUTEN, Allergen.EGG));
        dish("Vietnamese Pho", "Slow-simmered beef broth with rice noodles, herbs and lime",
                "35.00", Category.SOUTHEAST_ASIAN, "Beef", SpiceLevel.MILD, 20, List.of());

        List<Dish> dishes = dishRepository.findAll();
        log.info("Seeded {} dishes", dishes.size());
        return dishes;
    }

    private void seedDailyMenus(List<Dish> dishes) {
        LocalDate today = LocalDate.now();
        for (LocalDate date : List.of(today, today.plusDays(1))) {
            if (dailyMenuRepository.existsByMenuDate(date)) {
                continue;
            }
            for (Dish dish : dishes) {
                DailyMenu dm = new DailyMenu();
                dm.setMenuDate(date);
                dm.setDish(dish);
                dm.setStockTotal(DAILY_STOCK);
                dm.setStockRemaining(DAILY_STOCK);
                dailyMenuRepository.save(dm);
            }
            log.info("Seeded daily menu for {} ({} dishes x {} in stock)", date, dishes.size(), DAILY_STOCK);
        }
    }

    // ---- builders ----

    private int groupSort;

    private Dish dish(String name, String description, String price, Category category,
                      String protein, SpiceLevel spiceLevel, int imageIndex,
                      List<Allergen> allergens, OptionGroup... groups) {
        Dish dish = new Dish();
        dish.setName(name);
        dish.setDescription(description);
        dish.setPrice(Moneys.of(price));
        dish.setCategory(category);
        dish.setProtein(protein);
        dish.setSpiceLevel(spiceLevel);
        dish.setAllergens(allergens);
        dish.setImageUrl(IMG.formatted(imageIndex));
        dish.setActive(true);
        groupSort = 0;
        for (OptionGroup group : groups) {
            group.setDish(dish);
            group.setSortOrder(groupSort++);
            for (OptionItem item : group.getItems()) {
                item.setGroup(group);
            }
            dish.getOptionGroups().add(group);
        }
        return dishRepository.save(dish);
    }

    private OptionGroup group(String name, boolean required, OptionItem... items) {
        OptionGroup group = new OptionGroup();
        group.setName(name);
        group.setRequired(required);
        group.setItems(List.of(items));
        return group;
    }

    private OptionItem item(String name) {
        return item(name, "0");
    }

    private OptionItem item(String name, String extraPrice) {
        OptionItem item = new OptionItem();
        item.setName(name);
        item.setExtraPrice(new BigDecimal(extraPrice));
        return item;
    }
}
