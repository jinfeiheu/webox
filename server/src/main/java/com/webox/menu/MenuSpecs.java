package com.webox.menu;

import com.webox.common.enums.Category;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Criteria/Specification queries — always parameterized, never string-concatenated SQL
 * (SQL-injection-safe search, PRD §3.2 / Appendix A.2).
 */
public final class MenuSpecs {

    private MenuSpecs() {
    }

    /** Active dishes on the given date, optionally filtered by keyword and category set. */
    public static Specification<DailyMenu> menuSearch(LocalDate date, String keyword,
                                                      List<Category> categories) {
        return (root, query, cb) -> {
            Join<DailyMenu, Dish> dish = root.join("dish", JoinType.INNER);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("menuDate"), date));
            predicates.add(cb.isTrue(dish.get("active")));

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + escapeLike(keyword.trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(dish.get("name")), pattern, '\\'),
                        cb.like(cb.lower(dish.get("description")), pattern, '\\')));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(dish.get("category").in(categories));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Escapes LIKE wildcards so user input is matched literally. */
    private static String escapeLike(String input) {
        return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
