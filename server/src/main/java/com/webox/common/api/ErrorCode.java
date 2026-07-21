package com.webox.common.api;

import org.springframework.http.HttpStatus;

/**
 * Stable machine-readable error codes returned to the frontend (PRD §4 API contract).
 * Messages are English per PRD §1 language requirement.
 */
public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Invalid request parameters."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "You do not have permission to access this resource."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found."),

    EMAIL_TAKEN(HttpStatus.CONFLICT, "This email is already registered."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Incorrect email or password."),

    STOCK_INSUFFICIENT(HttpStatus.BAD_REQUEST, "Some dishes are out of stock."),
    ORDER_EXISTS(HttpStatus.CONFLICT, "You already have an active order for this meal."),
    ORDER_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "An order can contain at most 5 items in total."),
    CUTOFF_PASSED(HttpStatus.BAD_REQUEST, "The cutoff time for this meal slot has passed."),
    ORDER_NOT_CANCELLABLE(HttpStatus.CONFLICT, "Only pending orders can be cancelled."),

    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
