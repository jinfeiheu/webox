package com.webox.preference;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webox.common.enums.Category;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/** Stores preferred cuisine categories as a readable JSON array in a TEXT column. */
@Converter
public class CategoryListConverter implements AttributeConverter<List<Category>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<Category>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<Category> attribute) {
        try {
            return MAPPER.writeValueAsString(attribute == null ? List.of() : attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize categories", e);
        }
    }

    @Override
    public List<Category> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return List.of();
            }
            return MAPPER.readValue(dbData, TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse categories: " + dbData, e);
        }
    }
}
