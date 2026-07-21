package com.webox.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

/** Maps exceptions to the uniform {@link ApiError} body with English messages (PRD §1). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiError> handleBiz(BizException e) {
        return ResponseEntity.status(e.getErrorCode().getStatus())
                .body(ApiError.of(e.getErrorCode(), e.getMessage(), e.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiError.of(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.getDefaultMessage(), details));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest().body(ApiError.of(ErrorCode.VALIDATION_ERROR, e.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NoResourceFoundException e) {
        return ResponseEntity.status(ErrorCode.NOT_FOUND.getStatus()).body(ApiError.of(ErrorCode.NOT_FOUND));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus()).body(ApiError.of(ErrorCode.INTERNAL_ERROR));
    }

    private static String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + (fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage());
    }
}
