package com.example.validation.application.shared.command;

public class DeleteValidationPolicyCommand {
    private final Long id;

    public DeleteValidationPolicyCommand(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
