package com.example.validation.domain.policy.aggregate.exception;


/**
 * 當找不到 ValidationPolicy 時拋出的領域例外
 */
public class ValidationPolicyNotFoundException extends RuntimeException {

    public ValidationPolicyNotFoundException(Long id) {
        super("ValidationPolicy with id " + id + " not found.");
    }

    public ValidationPolicyNotFoundException(String message) {
        super(message);
    }
}
