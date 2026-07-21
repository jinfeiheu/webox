package com.webox.menu;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webox.common.enums.Allergen;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/**
 * Stores dish allergens as a readable JSON array (e.g. ["Peanuts","Dairy"]) in a TEXT column.
 * Enum (de)serialization uses the English labels via @JsonValue/@JsonCreator.
 */
@Converter
public class AllergenListConverter implements AttributeConverter<List<Allergen>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<Allergen>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<Allergen> attribute) {
        try {
            return MAPPER.writeValueAsString(attribute == null ? List.of() : attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize allergens", e);
        }
    }

    @Override
    public List<Allergen> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return List.of();
            }
            return MAPPER.readValue(dbData, TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse allergens: " + dbData, e);
        }
    }
}
