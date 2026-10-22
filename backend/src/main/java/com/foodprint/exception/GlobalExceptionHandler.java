package com.foodprint.exception;

// TODO [Shared]: Implement global exception handler
// Reference: BACKEND_DESIGN.md Section 9
//
// Catches all exceptions and returns unified ApiResponse format:
// { "code": 400/500, "message": "...", "data": null }

import com.foodprint.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // AI-specific errors are handled by AIController's own @ExceptionHandler.
    // This handler catches generic exceptions for all other modules.

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericError(Exception e) {
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, e.getMessage()));
    }

    // TODO: Add handlers for:
    // - MethodArgumentNotValidException (validation errors → 400)
    // - EntityNotFoundException (not found → 404)
    // - AccessDeniedException (unauthorized → 403)
}
