package com.webox.admin;

import com.webox.admin.dto.DailyMenuAdminView;
import com.webox.admin.dto.DailyMenuSetupRequest;
import com.webox.common.api.BizException;
import com.webox.common.api.ErrorCode;
import com.webox.menu.DailyMenu;
import com.webox.menu.DailyMenuRepository;
import com.webox.menu.Dish;
import com.webox.menu.DishRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MenuAdminService {

    private final DishRepository dishRepository;
    private final DailyMenuRepository dailyMenuRepository;

    public MenuAdminService(DishRepository dishRepository, DailyMenuRepository dailyMenuRepository) {
        this.dishRepository = dishRepository;
        this.dailyMenuRepository = dailyMenuRepository;
    }

    @Transactional(readOnly = true)
    public DailyMenuAdminView getMenuAdmin(LocalDate date) {
        List<Dish> dishes = dishRepository.findAll(Sort.by("id")).stream()
                .filter(Dish::isActive)
                .toList();
        Map<Long, DailyMenu> existing = dailyMenuRepository.findByMenuDate(date).stream()
                .collect(Collectors.toMap(dm -> dm.getDish().getId(), Function.identity()));
        List<DailyMenuAdminView.Entry> entries = dishes.stream()
                .map(d -> DailyMenuAdminView.Entry.of(d, existing.get(d.getId())))
                .toList();
        return new DailyMenuAdminView(date, entries);
    }

    @Transactional
    @CacheEvict(cacheNames = "menuItems", key = "#date")
    public void setMenu(LocalDate date, DailyMenuSetupRequest request) {
        List<DailyMenuSetupRequest.Entry> entries =
                request.entries() == null ? List.of() : request.entries();
        List<Long> dishIds = entries.stream().map(DailyMenuSetupRequest.Entry::dishId).toList();
        Map<Long, Dish> dishes = dishRepository.findAllById(dishIds).stream()
                .collect(Collectors.toMap(Dish::getId, Function.identity()));
        Map<Long, DailyMenu> existing = dailyMenuRepository.findByMenuDate(date).stream()
                .collect(Collectors.toMap(dm -> dm.getDish().getId(), Function.identity()));

        for (DailyMenuSetupRequest.Entry entry : entries) {
            Dish dish = dishes.get(entry.dishId());
            if (dish == null) {
                throw new BizException(ErrorCode.NOT_FOUND, "Dish not found: " + entry.dishId());
            }
            DailyMenu dm = existing.get(entry.dishId());
            if (entry.selected()) {
                int stock = Math.max(1, entry.stockTotal());
                if (dm == null) {
                    dm = new DailyMenu();
                    dm.setMenuDate(date);
                    dm.setDish(dish);
                }
                dm.setStockTotal(stock);
                dm.setStockRemaining(stock); // PUT reconfigures the day's supply
                dailyMenuRepository.save(dm);
            } else if (dm != null) {
                dailyMenuRepository.delete(dm);
            }
        }
    }
}
