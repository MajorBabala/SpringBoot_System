package com.enterprise.finance.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusiness(BusinessException ex) {
        return ResponseEntity.ok(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.ok(ApiResponse.error(400, msg));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = "Database constraint violation";
        String rawMessage = ex.getMostSpecificCause() == null ? "" : ex.getMostSpecificCause().getMessage();
        if (rawMessage.contains("uk_voucher_period_no")) {
            message = "Voucher number already exists in this period";
        } else if (rawMessage.contains("fk_voucher_entry_subject")) {
            message = "Voucher subject does not exist";
        } else if (rawMessage.contains("fk_voucher_maker")) {
            message = "Current login user does not exist";
        }
        return ResponseEntity.ok(ApiResponse.error(400, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleOther(Exception ex) {
        return new ResponseEntity<>(ApiResponse.error(500, "Server error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
