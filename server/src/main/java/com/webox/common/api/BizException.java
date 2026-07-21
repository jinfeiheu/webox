package com.webox.common.api;

import java.util.List;

/** Business exception carrying a stable {@link ErrorCode}; translated by GlobalExceptionHandler. */
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<String> details;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details = List.of();
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = List.of();
    }

    public BizException(ErrorCode errorCode, String message, List<String> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details == null ? List.of() : details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<String> getDetails() {
        return details;
    }
}
