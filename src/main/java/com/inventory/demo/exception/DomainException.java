package com.inventory.demo.exception;

/**
 * Base exception for all domain-specific exceptions.
 * All domain exceptions extend this class.
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;

    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Returns the machine-readable error code.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}
