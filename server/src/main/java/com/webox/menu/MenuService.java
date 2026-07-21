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
import java.util.List;

@Service
public class MenuService {

    public static final int SEARCH_KEYWORD_MAX = 50;

    private final DailyMenuRepository dailyMenuRepository;
    private final DishRepository dishRepository;

    public MenuService(DailyMenuRepository dailyMenuRepository, DishRepository dishRepository) {
        this.dailyMenuRepository = dailyMenuRepository;
        this.dishRepository = dishRepository;
    }

    /** Today's (or the requested day's) menu. Cached — hot read path during the 9:30-10:00 peak. */
    @Cacheable(cacheNames = "menuItems", key = "#date")
    @Transactional(readOnly = true)
    public MenuResponse getMenu(LocalDate date) {
        List<MenuItemView> items = dailyMenuRepository.findByMenuDateWithDish(date).stream()
                .filter(dm -> dm.getDish().isActive())
                .map(MenuItemView::of)
                .toList();
        return new MenuResponse(date, items);
    }

    /** Keyword + multi-category search within a day's menu (parameterized, see MenuSpecs). */
    @Transactional(readOnly = true)
    public MenuResponse search(LocalDate date, String keyword, List<Category> categories) {
        String q = keyword == null ? null : keyword.trim();
        if (q != null && q.length() > SEARCH_KEYWORD_MAX) {
            throw new BizException(ErrorCode.VALIDATION_ERROR,
                    "Search keyword must be at most " + SEARCH_KEYWORD_MAX + " characters.");
        }
        List<MenuItemView> items = dailyMenuRepository
                .findAll(MenuSpecs.menuSearch(date, q, categories)).stream()
                .map(MenuItemView::of)
                .toList();
        return new MenuResponse(date, items);
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
