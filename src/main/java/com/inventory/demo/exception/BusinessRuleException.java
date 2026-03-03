package com.inventory.demo.exception;

/**
 * Thrown when a business rule or conflict prevents an operation.
 * Maps to HTTP 409 Conflict.
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessRuleException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
