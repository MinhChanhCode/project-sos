package com.sqc.sos.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    // =============================================================================
    // USER ERRORS (406xx)
    // =============================================================================
    USER_NOT_EXIST(40601, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXIST(40602, "User already exists", HttpStatus.CONFLICT),
    USER_INVALID_CREDENTIALS(40603, "Invalid login credentials", HttpStatus.UNAUTHORIZED),

    // =============================================================================
    // AUTHENTICATION ERRORS (401xx)
    // =============================================================================
    UNAUTHORIZED(40101, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(40102, "Token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(40103, "Invalid token", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED(40104, "Access denied", HttpStatus.FORBIDDEN),

    // =============================================================================
    // VALIDATION ERRORS (400xx)
    // =============================================================================
    VALIDATION_ERROR(40001, "Invalid input data", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELD(40002, "Missing required field", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT(40003, "Invalid data format", HttpStatus.BAD_REQUEST),

    // =============================================================================
    // MENU ITEM ERRORS (407xx)
    // =============================================================================
    MENU_ITEM_NOT_FOUND(40701, "Menu item not found", HttpStatus.NOT_FOUND),
    MENU_ITEM_ALREADY_EXIST(40702, "Menu item already exists", HttpStatus.CONFLICT),
    
    // =============================================================================
    // CATEGORY ERRORS (408xx)
    // =============================================================================
    CATEGORY_NOT_FOUND(40801, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXIST(40802, "Category already exists", HttpStatus.CONFLICT),

    // =============================================================================
    // CART ERRORS (409xx)
    // =============================================================================
    CART_NOT_FOUND(40901, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ALREADY_EXISTS(40902, "Cart already exists", HttpStatus.CONFLICT),
    CART_ITEM_NOT_FOUND(40903, "Cart item not found", HttpStatus.NOT_FOUND),

    // =============================================================================
    // TABLE ERRORS (410xx)
    // =============================================================================
    TABLE_NOT_FOUND(41001, "Table not found", HttpStatus.NOT_FOUND),
    INVALID_OPERATION(41002, "Invalid operation", HttpStatus.BAD_REQUEST),
    TABLE_CANNOT_CLEAR(41003, "Không thể dọn bàn! Vẫn còn món chưa được phục vụ.", HttpStatus.BAD_REQUEST),

    // =============================================================================
    // SERVICE REQUEST ERRORS (411xx)
    // =============================================================================
    SERVICE_REQUEST_NOT_FOUND(41101, "Service request not found", HttpStatus.NOT_FOUND),
    SERVICE_REQUEST_ALREADY_EXISTS(41102, "Service request already exists", HttpStatus.CONFLICT),
    SERVICE_REQUEST_INVALID_STATUS(41103, "Invalid service request status", HttpStatus.BAD_REQUEST),

    // =============================================================================
    // AREA ERRORS (412xx)
    // =============================================================================
    AREA_NOT_FOUND(41201, "Area not found", HttpStatus.NOT_FOUND),

    // =============================================================================
    // INVOICE / PAYMENT ERRORS (413xx)
    // =============================================================================
    INVOICE_NOT_FOUND(41301, "Invoice not found", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND(41302, "Payment not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(41303, "Order not found", HttpStatus.NOT_FOUND),

    // =============================================================================
    // REVIEW ERRORS (414xx)
    // =============================================================================
    REVIEW_NOT_FOUND(41401, "Review not found", HttpStatus.NOT_FOUND),

    // =============================================================================
    // EMPLOYEE ERRORS (415xx)
    // =============================================================================
    EMPLOYEE_NOT_FOUND(41501, "Employee not found", HttpStatus.NOT_FOUND),
    SHIFT_NOT_FOUND(41502, "Shift not found", HttpStatus.NOT_FOUND),

    // =============================================================================
    // QR ERRORS (416xx)
    // =============================================================================
    QR_NOT_FOUND(41601, "QR code not found", HttpStatus.NOT_FOUND),

    // =============================================================================
    // ROLE ERRORS (417xx)
    // =============================================================================
    ROLE_NOT_FOUND(41701, "Role not found", HttpStatus.NOT_FOUND),

    // =============================================================================
    // SERVER ERRORS (500xx)
    // =============================================================================
    INTERNAL_SERVER_ERROR(50001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(50002, "Database error", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_SERVICE_ERROR(50003, "External service error", HttpStatus.INTERNAL_SERVER_ERROR);

    Integer code;
    String message;
    HttpStatus status;
}
