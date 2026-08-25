package com.example.validation.presentation.resource.out;

public class ValidationPolicyUpdatedResource {
    private boolean success;

    public ValidationPolicyUpdatedResource(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
