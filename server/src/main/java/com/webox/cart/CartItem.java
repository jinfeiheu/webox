package com.webox.cart;

import com.webox.auth.User;
import com.webox.menu.Dish;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.List;

/**
 * One cart line. Same dish with different configurations is a DIFFERENT line (PRD §3.3) —
 * enforced by the (user, dish, options_hash) unique key; identical configs merge by quantity.
 */
@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_cart",
                columnNames = {"user_id", "dish_id", "options_hash"}))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(nullable = false)
    private int qty;

    @Convert(converter = SelectedOptionsConverter.class)
    @Column(name = "selected_options", nullable = false, columnDefinition = "TEXT")
    private List<SelectedOption> selectedOptions = List.of();

    /** SHA-256 of the canonical option selection — dedup key for "same dish same config". */
    @Column(name = "options_hash", nullable = false, length = 64)
    private String optionsHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public List<SelectedOption> getSelectedOptions() {
        return selectedOptions;
    }

    public void setSelectedOptions(List<SelectedOption> selectedOptions) {
        this.selectedOptions = selectedOptions;
    }

    public String getOptionsHash() {
        return optionsHash;
    }

    public void setOptionsHash(String optionsHash) {
        this.optionsHash = optionsHash;
    }
}
