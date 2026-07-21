package com.webox.menu;

import com.webox.common.api.BizException;
import com.webox.common.api.ErrorCode;
import com.webox.common.enums.Category;
import com.webox.menu.dto.DishDetailView;
import com.webox.menu.dto.MenuItemView;
import com.webox.menu.dto.MenuResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MenuService {

    public static final int SEARCH_KEYWORD_MAX = 50;

    private final DailyMenuRepository dailyMenuRepository;
    private final DishRepository dishRepository;
    private final OptionGroupRepository optionGroupRepository;

    public MenuService(DailyMenuRepository dailyMenuRepository, DishRepository dishRepository,
                       OptionGroupRepository optionGroupRepository) {
        this.dailyMenuRepository = dailyMenuRepository;
        this.dishRepository = dishRepository;
        this.optionGroupRepository = optionGroupRepository;
    }

    /** Today's (or the requested day's) menu. Cached — hot read path during the 9:30-10:00 peak. */
    @Cacheable(cacheNames = "menuItems", key = "#date")
    @Transactional(readOnly = true)
    public MenuResponse getMenu(LocalDate date) {
        List<DailyMenu> dailyMenus = dailyMenuRepository.findByMenuDateWithDish(date).stream()
                .filter(dm -> dm.getDish().isActive())
                .toList();
        return new MenuResponse(date, toViews(dailyMenus));
    }

    /** Keyword + multi-category search within a day's menu (parameterized, see MenuSpecs). */
    @Transactional(readOnly = true)
    public MenuResponse search(LocalDate date, String keyword, List<Category> categories) {
        String q = keyword == null ? null : keyword.trim();
        if (q != null && q.length() > SEARCH_KEYWORD_MAX) {
            throw new BizException(ErrorCode.VALIDATION_ERROR,
                    "Search keyword must be at most " + SEARCH_KEYWORD_MAX + " characters.");
        }
        List<DailyMenu> dailyMenus = dailyMenuRepository.findAll(MenuSpecs.menuSearch(date, q, categories));
        return new MenuResponse(date, toViews(dailyMenus));
    }

    private List<MenuItemView> toViews(List<DailyMenu> dailyMenus) {
        List<Long> dishIds = dailyMenus.stream().map(dm -> dm.getDish().getId()).toList();
        Set<Long> withRequired = dishIds.isEmpty()
                ? Set.of()
                : new HashSet<>(optionGroupRepository.findDishIdsWithRequiredGroups(dishIds));
        return dailyMenus.stream()
                .map(dm -> MenuItemView.of(dm, withRequired.contains(dm.getDish().getId())))
                .toList();
    }

    @Cacheable(cacheNames = "dishDetail", key = "#dishId")
    @Transactional(readOnly = true)
    public DishDetailView getDishDetail(long dishId) {
        Dish dish = dishRepository.findById(dishId)
                .filter(Dish::isActive)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Dish not found."));
        return DishDetailView.of(dish);
    }
}
