package com.webox.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT c FROM CartItem c JOIN FETCH c.dish WHERE c.user.id = :userId ORDER BY c.id")
    List<CartItem> findByUserIdWithDish(@Param("userId") Long userId);

    Optional<CartItem> findByUserIdAndDishIdAndOptionsHash(Long userId, Long dishId, String optionsHash);

    Optional<CartItem> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(c.qty), 0) FROM CartItem c WHERE c.user.id = :userId")
    int totalQtyByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
