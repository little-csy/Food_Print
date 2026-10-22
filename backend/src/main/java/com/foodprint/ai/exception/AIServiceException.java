package com.foodprint.ai.exception;

/**
 * Custom exception for AI service errors.
 */
public class AIServiceException extends RuntimeException {

    private final String errorCode;

    public AIServiceException(String message) {
        super(message);
        this.errorCode = "AI_ERROR";
    }

    public AIServiceException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AI_ERROR";
    }

    public AIServiceException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
