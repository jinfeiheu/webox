package com.webox.admin.dto;

import com.webox.common.enums.Allergen;
import com.webox.common.enums.Category;
import com.webox.common.enums.SpiceLevel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** Create/update dish form (PRD §4.3). Option groups are out of scope for the admin form. */
public record DishFormRequest(
        @NotBlank(message = "Name is required.")
        @Size(max = 128)
        String name,

        @NotBlank(message = "Description is required.")
        @Size(max = 512)
        String description,

        @NotNull
        @DecimalMin(value = "0", message = "Price cannot be negative.")
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @NotNull(message = "Category is required.")
        Category category,

        @NotBlank(message = "Protein is required.")
        @Size(max = 64)
        String protein,

        @NotNull(message = "Spice level is required.")
        SpiceLevel spiceLevel,

        List<Allergen> allergens,

        /** Optional; set separately via the image-upload endpoint. */
        String imageUrl) {
}
