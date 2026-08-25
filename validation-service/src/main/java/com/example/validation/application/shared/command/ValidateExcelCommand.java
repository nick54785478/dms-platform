package com.example.validation.application.shared.command;

public record ValidateExcelCommand(String code, byte[] fileContent) {
}
