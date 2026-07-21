package com.webox.order;

import com.webox.common.enums.MealSlot;
import com.webox.common.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    List<Order> findByUserIdOrderByIdDesc(Long userId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findWithItemsById(@Param("id") Long id);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.deliveryDate = :date "
            + "AND o.mealSlot = :slot AND o.status IN :statuses")
    Optional<Order> findActiveBySlot(@Param("userId") Long userId,
                                     @Param("date") LocalDate date,
                                     @Param("slot") MealSlot slot,
                                     @Param("statuses") Collection<OrderStatus> statuses);

    /** Address history for the checkout picker, most recently used first. */
    @Query("SELECT o.address FROM Order o WHERE o.user.id = :userId "
            + "GROUP BY o.address ORDER BY MAX(o.id) DESC")
    List<String> findAddressHistory(@Param("userId") Long userId, Pageable pageable);

    // ---- dashboard aggregation queries (T19) ----

    @Query("SELECT COUNT(o), COALESCE(SUM(o.total),0) FROM Order o "
            + "WHERE o.deliveryDate = :date AND o.status <> com.webox.common.enums.OrderStatus.CANCELLED")
    List<Object[]> countAndRevenueByDeliveryDate(@Param("date") LocalDate date);

    long countByDeliveryDateAndStatus(LocalDate deliveryDate,
                                      com.webox.common.enums.OrderStatus status);

    long countByDeliveryDateAndMealSlot(LocalDate deliveryDate,
                                        com.webox.common.enums.MealSlot mealSlot);

    @Query("SELECT oi.dishId, oi.dishName, SUM(oi.qty) AS sold FROM OrderItem oi "
            + "JOIN oi.order o WHERE o.status <> com.webox.common.enums.OrderStatus.CANCELLED "
            + "GROUP BY oi.dishId, oi.dishName ORDER BY sold DESC")
    List<Object[]> findTopDishes(Pageable pageable);

    default List<Object[]> findTopDishes() {
        return findTopDishes(Pageable.ofSize(10));
    }

    @Query("SELECT o.deliveryDate, COUNT(o), COALESCE(SUM(o.total),0) FROM Order o "
            + "WHERE o.deliveryDate BETWEEN :start AND :end "
            + "AND o.status <> com.webox.common.enums.OrderStatus.CANCELLED "
            + "GROUP BY o.deliveryDate ORDER BY o.deliveryDate")
    List<Object[]> trendBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    /** Dish IDs the user ordered in the last N days (non-cancelled), for AI "don't repeat" filter. */
    @Query("SELECT DISTINCT oi.dishId FROM OrderItem oi JOIN oi.order o "
            + "WHERE o.user.id = :userId AND o.deliveryDate >= :since "
            + "AND o.status <> com.webox.common.enums.OrderStatus.CANCELLED")
    List<Long> findDishIdsOrderedByUserSince(@Param("userId") Long userId,
                                              @Param("since") LocalDate since);
}
