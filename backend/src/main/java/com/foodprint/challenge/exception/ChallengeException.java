package com.foodprint.challenge.exception;

/**
 * Business-logic exception for the challenge module.
 * Handled by ChallengeController's @ExceptionHandler, which returns HTTP 400.
 */
public class ChallengeException extends RuntimeException {

    private final String errorCode;

    public ChallengeException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
