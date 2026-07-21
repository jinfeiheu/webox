package com.webox.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMenuRepository extends JpaRepository<DailyMenu, Long>, JpaSpecificationExecutor<DailyMenu> {

    List<DailyMenu> findByMenuDate(LocalDate menuDate);

    boolean existsByMenuDate(LocalDate menuDate);

    @Query("SELECT dm FROM DailyMenu dm JOIN FETCH dm.dish WHERE dm.menuDate = :date ORDER BY dm.dish.id")
    List<DailyMenu> findByMenuDateWithDish(@Param("date") LocalDate date);

    Optional<DailyMenu> findByMenuDateAndDishId(LocalDate menuDate, Long dishId);

    /**
     * Atomic stock decrement — returns 1 if enough units were available (the row is updated),
     * 0 otherwise.  The caller uses the zero return to detect oversell within the same DB
     * transaction without explicit locking (PRD §5.1).
     */
    @Modifying
    @Query("UPDATE DailyMenu dm SET dm.stockRemaining = dm.stockRemaining - :qty "
            + "WHERE dm.dish.id = :dishId AND dm.menuDate = :date AND dm.stockRemaining >= :qty")
    int decrementStock(@Param("dishId") Long dishId, @Param("date") LocalDate date,
                       @Param("qty") int qty);

    /** Stock restore on order cancel (PRD §5.1) — simpler: no WHERE guard needed. */
    @Modifying
    @Query("UPDATE DailyMenu dm SET dm.stockRemaining = dm.stockRemaining + :qty "
            + "WHERE dm.dish.id = :dishId AND dm.menuDate = :date")
    int incrementStock(@Param("dishId") Long dishId, @Param("date") LocalDate date,
                       @Param("qty") int qty);
}

