package com.webox.common.api;

import java.util.List;

/**
 * Uniform error body: {"code":"...","message":"...","details":[...]}.
 * `details` carries field-level or item-level info (e.g. out-of-stock dishes).
 */
public record ApiError(String code, String message, List<String> details) {

    public static ApiError of(ErrorCode code) {
        return new ApiError(code.name(), code.getDefaultMessage(), List.of());
    }

    public static ApiError of(ErrorCode code, String message) {
        return new ApiError(code.name(), message, List.of());
    }

    public static ApiError of(ErrorCode code, String message, List<String> details) {
        return new ApiError(code.name(), message, details);
    }
}
