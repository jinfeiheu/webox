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
}
