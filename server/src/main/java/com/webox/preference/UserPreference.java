package com.webox.preference;

import com.webox.auth.User;
import com.webox.common.enums.Allergen;
import com.webox.common.enums.Category;
import com.webox.common.enums.SpiceLevel;
import com.webox.common.enums.TasteLevel;
import com.webox.menu.AllergenListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.List;

/** Employee dietary preferences (PRD §4.1). All fields optional; empty = no preference. */
@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Flagged allergens — dishes containing them trigger an add-to-cart warning (never filtered). */
    @Convert(converter = AllergenListConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private List<Allergen> allergens = List.of();

    /** Preferred cuisines — boost ordering when the "For You" switch is on. */
    @Convert(converter = CategoryListConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private List<Category> cuisines = List.of();

    @Enumerated(EnumType.STRING)
    @Column(name = "spice_level", length = 16)
    private SpiceLevel spiceLevel;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private TasteLevel taste;

    @Column(name = "budget_min", precision = 10, scale = 2)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", precision = 10, scale = 2)
    private BigDecimal budgetMax;

    public Long getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Allergen> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<Allergen> allergens) {
        this.allergens = allergens;
    }

    public List<Category> getCuisines() {
        return cuisines;
    }

    public void setCuisines(List<Category> cuisines) {
        this.cuisines = cuisines;
    }

    public SpiceLevel getSpiceLevel() {
        return spiceLevel;
    }

    public void setSpiceLevel(SpiceLevel spiceLevel) {
        this.spiceLevel = spiceLevel;
    }

    public TasteLevel getTaste() {
        return taste;
    }

    public void setTaste(TasteLevel taste) {
        this.taste = taste;
    }

    public BigDecimal getBudgetMin() {
        return budgetMin;
    }

    public void setBudgetMin(BigDecimal budgetMin) {
        this.budgetMin = budgetMin;
    }

    public BigDecimal getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(BigDecimal budgetMax) {
        this.budgetMax = budgetMax;
    }
}
