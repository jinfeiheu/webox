package com.webox.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyMenuRepository extends JpaRepository<DailyMenu, Long>, JpaSpecificationExecutor<DailyMenu> {

    List<DailyMenu> findByMenuDate(LocalDate menuDate);

    boolean existsByMenuDate(LocalDate menuDate);

    @Query("SELECT dm FROM DailyMenu dm JOIN FETCH dm.dish WHERE dm.menuDate = :date ORDER BY dm.dish.id")
    List<DailyMenu> findByMenuDateWithDish(@Param("date") LocalDate date);
}
