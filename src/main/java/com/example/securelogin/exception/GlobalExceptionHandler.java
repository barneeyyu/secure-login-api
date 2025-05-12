// src/main/java/com/example/securelogin/exception/GlobalExceptionHandler.java
package com.example.securelogin.exception;

import com.example.securelogin.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;
import org.postgresql.util.PSQLException;
import java.util.NoSuchElementException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

@RestControllerAdvice // @RestControllerAdvice 包含了 @ControllerAdvice 和 @ResponseBody
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        /**
         * 這個 @ExceptionHandler 專門處理由 @Valid 註解觸發的驗證錯誤。
         * 當任何 Controller 中的 @Valid 驗證失敗時，Spring 會自動調用此方法。
         * HTTP 狀態碼會是 400 Bad Request (由 @ResponseStatus 指定)。
         */
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                logger.warn("Validation failed for request URI {}: {} - Errors: {}", request.getRequestURI(),
                                ex.getMessage(), ex);
                String errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining("; "));

                return new ErrorResponse(
                                400,
                                "Input validation failed: " + errors);
        }

        /**
         * 這個@ExceptionHandler 專門處理資料完整性違規（例如：email 唯一性違規）。
         * 當資料庫拋出DataIntegrityViolationException，Spring 會自動調用此方法。
         * HTTP 狀態碼會是 409 Conflict (由 @ResponseStatus 指定)。
         */
        @ResponseStatus(HttpStatus.CONFLICT)
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex,
                        HttpServletRequest request) {
                logger.error("Database constraint violation at URI {}: {}", request.getRequestURI(), ex.getMessage(),
                                ex);

                // 檢查是否是 email unique 約束違規
                if (ex.getCause() != null && ex.getCause().getCause() instanceof PSQLException) {
                        PSQLException psqlException = (PSQLException) ex.getCause().getCause();
                        String message = psqlException.getMessage().toLowerCase();

                        if (message.contains("duplicate key") && message.contains("email")) {
                                return new ErrorResponse(
                                                409,
                                                "This email is already registered");
                        }
                }

                // 其他欄位違規
                return new ErrorResponse(
                                409,
                                "A database constraint was violated");
        }

        /**
         * 處理由 Service 層拋出的 IllegalArgumentException 和 IllegalStateException。
         * 這些異常已經包含了 HTTP 狀態碼和原因。
         */
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
        public ErrorResponse handleIllegalArgumentException(Exception ex, HttpServletRequest request) {
                logger.warn("IllegalArgumentException at URI {}: {}", request.getRequestURI(), ex.getMessage(), ex);
                return new ErrorResponse(
                                400,
                                ex.getMessage());
        }

        /**
         * 處理找不到元素的異常。
         * 這些異常已經包含了 HTTP 狀態碼和原因。
         */
        @ResponseStatus(HttpStatus.NOT_FOUND)
        @ExceptionHandler(NoSuchElementException.class)
        public ErrorResponse handleNoSuchElementException(NoSuchElementException ex,
                        HttpServletRequest request) {
                logger.warn("NoSuchElementException at URI {}: {}", request.getRequestURI(), ex.getMessage(), ex);
                // Typically, "not found" maps to HTTP 404
                ErrorResponse errorResponse = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                ex.getMessage());
                return errorResponse;
        }

        /**
         * 處理 BadCredentialsException 異常。
         * 這些異常已經包含了 HTTP 狀態碼和原因。
         */
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        @ExceptionHandler(BadCredentialsException.class)
        public ErrorResponse handleBadCredentialsException(BadCredentialsException ex,
                        HttpServletRequest request) {
                logger.warn("BadCredentialsException at URI {}: {}", request.getRequestURI(), ex.getMessage(), ex);
                return new ErrorResponse(
                                401,
                                ex.getMessage());
        }

        /**
         * 處理其他未被特定處理器捕獲的通用異常。
         * 這是一個很好的兜底機制。
         */
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        @ExceptionHandler(Exception.class)
        public ErrorResponse handleGenericException(Exception ex, HttpServletRequest request) {
                logger.error("An unexpected error occurred at URI {}: {}", request.getRequestURI(), ex.getMessage(),
                                ex);
                String errorMessage = "An unexpected error occurred. Please try again later.";
                String specificMessage = ex.getMessage();

                // 如果異常訊息不為空，則將其附加到通用訊息中，或用更具體的訊息
                if (specificMessage != null && !specificMessage.trim().isEmpty()) {
                        errorMessage = "Error: " + specificMessage;
                }

                return new ErrorResponse(
                                500,
                                errorMessage);
        }
}