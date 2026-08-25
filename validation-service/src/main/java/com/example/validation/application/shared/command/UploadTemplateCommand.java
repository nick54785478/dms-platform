package com.example.validation.application.shared.command;

public record UploadTemplateCommand(
	String name,
	String type,
	String fileType,
	String filePath,
	String fileName,
	byte[] fileContent
) {
}
