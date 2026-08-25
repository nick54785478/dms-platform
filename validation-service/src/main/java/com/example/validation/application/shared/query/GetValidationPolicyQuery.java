package com.example.validation.application.shared.query;

public class GetValidationPolicyQuery {
    private final Long id;

    public GetValidationPolicyQuery(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
