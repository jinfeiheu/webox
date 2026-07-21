package com.webox.menu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyMenuRepository extends JpaRepository<DailyMenu, Long> {

    List<DailyMenu> findByMenuDate(LocalDate menuDate);

    boolean existsByMenuDate(LocalDate menuDate);
}
