package com.webox.common.option;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/** Stores the chosen option snapshots of a cart line as JSON in a TEXT column. */
@Converter
public class SelectedOptionsConverter implements AttributeConverter<List<SelectedOption>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<SelectedOption>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<SelectedOption> attribute) {
        try {
            return MAPPER.writeValueAsString(attribute == null ? List.of() : attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize selected options", e);
        }
    }

    @Override
    public List<SelectedOption> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) {
                return List.of();
            }
            return MAPPER.readValue(dbData, TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse selected options", e);
        }
    }
}
