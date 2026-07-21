package com.webox.preference;

import com.webox.auth.UserRepository;
import com.webox.common.api.BizException;
import com.webox.common.api.ErrorCode;
import com.webox.preference.dto.PreferenceView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PreferenceService {

    private final PreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public PreferenceService(PreferenceRepository preferenceRepository, UserRepository userRepository) {
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
    }

    /** Employees without saved preferences get an empty view (never 404 — preferences are optional). */
    @Transactional(readOnly = true)
    public PreferenceView get(Long userId) {
        return preferenceRepository.findById(userId)
                .map(PreferenceView::of)
                .orElseGet(PreferenceView::empty);
    }

    @Transactional
    public PreferenceView put(Long userId, PreferenceView request) {
        validateBudget(request.budgetMin(), request.budgetMax());

        UserPreference preference = preferenceRepository.findById(userId).orElseGet(() -> {
            UserPreference created = new UserPreference();
            created.setUser(userRepository.getReferenceById(userId));
            return created;
        });
        preference.setAllergens(request.allergens() == null ? List.of() : request.allergens());
        preference.setCuisines(request.cuisines() == null ? List.of() : request.cuisines());
        preference.setSpiceLevel(request.spiceLevel());
        preference.setTaste(request.taste());
        preference.setBudgetMin(request.budgetMin());
        preference.setBudgetMax(request.budgetMax());
        return PreferenceView.of(preferenceRepository.save(preference));
    }

    private static void validateBudget(BigDecimal min, BigDecimal max) {
        if (min != null && min.signum() < 0 || max != null && max.signum() < 0) {
            throw new BizException(ErrorCode.VALIDATION_ERROR, "Budget cannot be negative.");
        }
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new BizException(ErrorCode.VALIDATION_ERROR,
                    "Budget minimum cannot exceed the maximum.");
        }
    }
}
