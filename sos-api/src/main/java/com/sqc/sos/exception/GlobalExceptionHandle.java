package com.sqc.sos.exception;

import com.sqc.sos.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandle {

    // =============================================================================
    // CUSTOM APPLICATION EXCEPTIONS
    // =============================================================================
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.error("Application Exception: {} - {}", errorCode.getCode(), errorCode.getMessage(), e);

        return ResponseEntity.status(errorCode.getStatus()).body(
                ApiResponse.<Object>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    // =============================================================================
    // VALIDATION EXCEPTIONS
    // =============================================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation Error: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<Map<String, String>>builder()
                        .code(ErrorCode.VALIDATION_ERROR.getCode())
                        .message(ErrorCode.VALIDATION_ERROR.getMessage())
                        .data(errors)
                        .build()
        );
    }

    // =============================================================================
    // SECURITY EXCEPTIONS
    // =============================================================================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access Denied: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.<Object>builder()
                        .code(ErrorCode.ACCESS_DENIED.getCode())
                        .message(ErrorCode.ACCESS_DENIED.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication Error: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<Object>builder()
                        .code(ErrorCode.UNAUTHORIZED.getCode())
                        .message(ErrorCode.UNAUTHORIZED.getMessage())
                        .build()
        );
    }

    // =============================================================================
    // HTTP REQUEST EXCEPTIONS
    // =============================================================================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("Invalid JSON format: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<Object>builder()
                        .code(ErrorCode.INVALID_FORMAT.getCode())
                        .message("Invalid JSON format")
                        .build()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("Missing parameter: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<Object>builder()
                        .code(ErrorCode.MISSING_REQUIRED_FIELD.getCode())
                        .message("Missing required parameter: " + e.getParameterName())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch for parameter: {} - Expected: {}, Got: {}",
                e.getName(), e.getRequiredType(), e.getValue());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<Object>builder()
                        .code(ErrorCode.INVALID_FORMAT.getCode())
                        .message("Invalid parameter format for: " + e.getName())
                        .build()
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("No handler found for {} {}", e.getHttpMethod(), e.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.<Object>builder()
                        .code(40400)
                        .message("API endpoint not found")
                        .build()
        );
    }

    // =============================================================================
    // GENERAL EXCEPTIONS
    // =============================================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.<Object>builder()
                        .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                        .message("An internal server error occurred. Please try again later.")
                        .build()
        );
    }
}
